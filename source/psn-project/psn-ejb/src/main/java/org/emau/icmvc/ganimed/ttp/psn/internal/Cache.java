package org.emau.icmvc.ganimed.ttp.psn.internal;

/*-
 * ###license-information-start###
 * gPAS - a Generic Pseudonym Administration Service
 * __
 * Copyright (C) 2013 - 2023 Independent Trusted Third Party of the University Medicine Greifswald
 * 							kontakt-ths@uni-greifswald.de
 * 							concept and implementation
 * 							l.geidel
 * 							web client
 * 							a.blumentritt
 * 							docker
 * 							r.schuldt
 * 							please cite our publications
 * 							http://dx.doi.org/10.3414/ME14-01-0133
 * 							http://dx.doi.org/10.1186/s12967-015-0545-6
 * __
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * ###license-information-end###
 */

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.annotation.PostConstruct;
import javax.ejb.AccessTimeout;
import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.persistence.NoResultException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.emau.icmvc.ganimed.ttp.psn.alphabets.GenericAlphabet;
import org.emau.icmvc.ganimed.ttp.psn.config.PaginationConfig;
import org.emau.icmvc.ganimed.ttp.psn.dto.DomainInDTO;
import org.emau.icmvc.ganimed.ttp.psn.dto.DomainOutDTO;
import org.emau.icmvc.ganimed.ttp.psn.dto.InsertPairExceptionDTO;
import org.emau.icmvc.ganimed.ttp.psn.dto.PSNDTO;
import org.emau.icmvc.ganimed.ttp.psn.dto.PSNNetDTO;
import org.emau.icmvc.ganimed.ttp.psn.dto.PSNNetNodeDTO;
import org.emau.icmvc.ganimed.ttp.psn.dto.PSNTreeDTO;
import org.emau.icmvc.ganimed.ttp.psn.dto.StatisticDTO;
import org.emau.icmvc.ganimed.ttp.psn.enums.AnonymisationResult;
import org.emau.icmvc.ganimed.ttp.psn.enums.DeletionResult;
import org.emau.icmvc.ganimed.ttp.psn.enums.GeneratorAlphabetRestriction;
import org.emau.icmvc.ganimed.ttp.psn.enums.InsertPairError;
import org.emau.icmvc.ganimed.ttp.psn.enums.ValidateViaParents;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.CharNotInAlphabetException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.DBException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.DeletionForbiddenException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.DomainInUseException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.DomainIsFullException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.InsertPairException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.InvalidAlphabetException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.InvalidCheckDigitClassException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.InvalidGeneratorException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.InvalidPSNException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.InvalidParameterException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.InvalidParentDomainException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.PSNErrorStrings;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.PSNNotFoundException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.UnknownDomainException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.UnknownValueException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.ValueIsAnonymisedException;
import org.emau.icmvc.ganimed.ttp.psn.generator.Alphabet;
import org.emau.icmvc.ganimed.ttp.psn.generator.CheckDigits;
import org.emau.icmvc.ganimed.ttp.psn.generator.Generator;
import org.emau.icmvc.ganimed.ttp.psn.model.Domain;
import org.emau.icmvc.ganimed.ttp.psn.model.PSN;
import org.emau.icmvc.ganimed.ttp.psn.model.PSNKey;
import org.emau.icmvc.ganimed.ttp.psn.model.Statistic;
import org.emau.icmvc.ganimed.ttp.psn.utils.StatisticKeys;

@Singleton
@Startup
@AccessTimeout(value = 1200, unit = TimeUnit.SECONDS)
public class Cache
{
	private static final Logger LOGGER = LogManager.getLogger(Cache.class);
	private static final int MAX_ATTEMPS_BEFORE_SEARCH_NEXT_FREE_PSN = 10000; // TODO ...
	private static final int PAGE_SIZE_FOR_CACHE_INIT = 100000;
	private static final HashMap<String, Domain> domainCache = new HashMap<>();
	private static final HashMap<String, Long> psnCounterCache = new HashMap<>();
	private static final HashMap<String, Long> anoCounterCache = new HashMap<>();
	private static final HashMap<String, Generator> generatorCache = new HashMap<>();
	private static final HashMap<String, PSNCacheObject> psnCache = new HashMap<>();
	private static final ReentrantReadWriteLock domainRWL = new ReentrantReadWriteLock();
	private static final HashMap<String, ReentrantReadWriteLock> psnRWL = new HashMap<>();
	private static final ReentrantReadWriteLock statisticRWL = new ReentrantReadWriteLock();
	@EJB
	private DAO dao;
	private static final SecureRandom rand = new SecureRandom();

	@PostConstruct
	private void initCache()
	{
		if (LOGGER.isInfoEnabled())
		{
			LOGGER.info("filling cache");
		}
		long time = System.currentTimeMillis();
		domainRWL.writeLock().lock();
		try
		{
			List<Domain> domains = dao.listDomains();
			for (Domain domain : domains)
			{
				addDomainToCaches(domain);
			}
		}
		catch (InvalidCheckDigitClassException | InvalidAlphabetException | InvalidGeneratorException e)
		{
			LOGGER.fatal("program error", e);
		}
		finally
		{
			domainRWL.writeLock().unlock();
		}
		if (LOGGER.isInfoEnabled())
		{
			LOGGER.info("cache filled in " + (System.currentTimeMillis() - time) / 1000 + " s");
		}
	}

	// --------------------- psns ---------------------

	public void validatePSN(String psn, String domainName) throws InvalidPSNException, UnknownDomainException
	{
		domainRWL.readLock().lock();
		try
		{
			checkForErrorKeyAsPSN(psn);
			getDomain(domainName);
			psnRWL.get(domainName).readLock().lock();
			try
			{
				generatorCache.get(domainName).check(psn);
			}
			catch (CharNotInAlphabetException e)
			{
				throw new InvalidPSNException(e);
			}
			finally
			{
				psnRWL.get(domainName).readLock().unlock();
			}
		}
		finally
		{
			domainRWL.readLock().unlock();
		}
	}

	public String getOrCreatePseudonymFor(String value, String domainName) throws DomainIsFullException, InvalidParameterException, UnknownDomainException
	{
		PSN result = null;
		domainRWL.readLock().lock();
		try
		{
			Domain domain = getDomain(domainName);
			psnRWL.get(domainName).writeLock().lock();
			try
			{
				checkParentRules(domain, value);
				result = dao.getPSNObject(value, domainName);
				if (LOGGER.isDebugEnabled())
				{
					LOGGER.debug("pseudonym for value " + value + " within domain " + domainName + " found in db");
				}
			}
			catch (UnknownValueException maybe)
			{
				if (LOGGER.isDebugEnabled())
				{
					LOGGER.debug("pseudonym for value " + value + " within domain " + domainName + " not found - generate new");
				}
				result = createPSN(domain, value);
				if (LOGGER.isDebugEnabled())
				{
					LOGGER.debug("pseudonym for value " + value + " within domain " + domainName + " created");
				}
			}
			finally
			{
				psnRWL.get(domainName).writeLock().unlock();
			}
		}
		finally
		{
			domainRWL.readLock().unlock();
		}
		return result.getPseudonym();
	}

	public String getPseudonymFor(String value, String domainName) throws InvalidParameterException, UnknownDomainException, UnknownValueException
	{
		domainRWL.readLock().lock();
		try
		{
			Domain domain = getDomain(domainName);
			psnRWL.get(domainName).readLock().lock();
			try
			{
				checkParentRules(domain, value);
				return dao.getPSNObject(value, domainName).getPseudonym();
			}
			finally
			{
				psnRWL.get(domainName).readLock().unlock();
			}
		}
		finally
		{
			domainRWL.readLock().unlock();
		}
	}

	public void anonymiseEntry(String value, String domainName) throws DBException, UnknownDomainException, UnknownValueException, ValueIsAnonymisedException
	{
		domainRWL.readLock().lock();
		try
		{
			getDomain(domainName);
			psnRWL.get(domainName).writeLock().lock();
			psnRWL.get(AnonymDomain.NAME).writeLock().lock();
			try
			{
				if (isAnonym(value))
				{
					String message = "psn " + value + " is already anonymised";
					LOGGER.debug(message);
					throw new ValueIsAnonymisedException(message);
				}
				PSN origEntity = dao.getPSNObject(value, domainName);
				try
				{
					String anonym = getOrCreatePseudonymFor(domainName + AnonymDomain.DELIMITER + origEntity.getPseudonym(), AnonymDomain.NAME);

					dao.deletePSN(origEntity);
					psnCounterCache.put(domainName, psnCounterCache.get(domainName) - 1);
					insertValuePseudonymPair(anonym, origEntity.getPseudonym(), domainName);
				}
				catch (Exception e)
				{
					LOGGER.error("error while anonymising psn entry", e);
					throw new DBException(e);
				}
			}
			finally
			{
				psnRWL.get(AnonymDomain.NAME).writeLock().unlock();
				psnRWL.get(domainName).writeLock().unlock();
			}
		}
		finally
		{
			domainRWL.readLock().unlock();
		}
	}

	public Map<String, AnonymisationResult> anonymiseEntries(Set<String> values, String domainName) throws DBException, UnknownDomainException
	{
		domainRWL.readLock().lock();
		try
		{
			getDomain(domainName);
			psnRWL.get(domainName).writeLock().lock();
			psnRWL.get(AnonymDomain.NAME).writeLock().lock();
			try
			{
				Map<String, AnonymisationResult> result = new HashMap<>();
				for (String value : values)
				{
					try
					{
						anonymiseEntry(value, domainName);
						result.put(value, AnonymisationResult.SUCCESS);
					}
					catch (UnknownValueException e)
					{
						result.put(value, AnonymisationResult.NOT_FOUND);
					}
					catch (ValueIsAnonymisedException e)
					{
						result.put(value, AnonymisationResult.ALREADY_ANONYMISED);
					}
				}
				return result;
			}
			finally
			{
				psnRWL.get(AnonymDomain.NAME).writeLock().unlock();
				psnRWL.get(domainName).writeLock().unlock();
			}
		}
		finally
		{
			domainRWL.readLock().unlock();
		}
	}

	public boolean isAnonym(String value)
	{
		return value.startsWith(AnonymDomain.PREFIX) && value.endsWith(AnonymDomain.SUFFIX);
	}

	public boolean isAnonymised(String psn, String domainName) throws InvalidPSNException, UnknownDomainException, PSNNotFoundException
	{
		domainRWL.readLock().lock();
		try
		{
			getDomain(domainName);
			psnRWL.get(domainName).readLock().lock();
			try
			{
				PSN origEntity = dao.getPSNObjectForPseudonym(domainName, psn);
				return isAnonym(origEntity.getKey().getOriginValue());
			}
			finally
			{
				psnRWL.get(domainName).readLock().unlock();
			}
		}
		finally
		{
			domainRWL.readLock().unlock();
		}
	}

	public void deleteEntry(String value, String domainName) throws DeletionForbiddenException, UnknownDomainException, UnknownValueException
	{
		domainRWL.readLock().lock();
		try
		{
			if (!arePSNDeletable(domainName))
			{
				String message = "the domain " + domainName + " does not allow deletion of value-pseudonym-pairs";
				LOGGER.warn(message);
				throw new DeletionForbiddenException(message);
			}
			else
			{
				psnRWL.get(domainName).writeLock().lock();
				try
				{
					deleteEntryInternal(value, domainName);
				}
				finally
				{
					psnRWL.get(domainName).writeLock().unlock();
				}
			}
		}
		finally
		{
			domainRWL.readLock().unlock();
		}
	}

	public Map<String, DeletionResult> deleteEntries(Set<String> values, String domainName) throws DeletionForbiddenException, UnknownDomainException
	{
		int countSuccess = 0;
		int countFailures = 0;
		Map<String, DeletionResult> result = new HashMap<>(values.size());
		domainRWL.readLock().lock();
		try
		{
			if (!arePSNDeletable(domainName))
			{
				String message = "the domain " + domainName + " does not allow deletion of value-pseudonym-pairs";
				LOGGER.warn(message);
				throw new DeletionForbiddenException(message);
			}
			else
			{
				psnRWL.get(domainName).writeLock().lock();
				try
				{
					for (String value : values)
					{
						try
						{
							deleteEntryInternal(value, domainName);
							result.put(value, DeletionResult.SUCCESS);
							countSuccess++;
						}
						catch (UnknownValueException e)
						{
							String message = "value " + value + " not found for domain " + domainName;
							if (LOGGER.isInfoEnabled())
							{
								LOGGER.info(message);
							}
							result.put(value, DeletionResult.NOT_FOUND);
							countFailures++;
						}
					}
				}
				finally
				{
					psnRWL.get(domainName).writeLock().unlock();
				}
			}
			if (LOGGER.isDebugEnabled())
			{
				LOGGER.debug(countSuccess + " value-pseudonym-pair(s) removed from domain " + domainName);
			}
			if (countFailures > 0 && LOGGER.isInfoEnabled())
			{
				LOGGER.info(countFailures + " failure(s) while removing value-pseudonym-pairs from domain " + domainName);
			}
			return result;
		}
		finally

		{
			domainRWL.readLock().unlock();
		}
	}

	public String getValueFor(String psn, String domainName)
			throws InvalidPSNException, PSNNotFoundException, UnknownDomainException, ValueIsAnonymisedException
	{
		domainRWL.readLock().lock();
		try
		{
			getDomain(domainName);
			psnRWL.get(domainName).readLock().lock();
			try
			{
				validatePSN(psn, domainName);
				String result = dao.getPSNObjectForPseudonym(domainName, psn).getKey().getOriginValue();
				if (isAnonym(result))
				{
					String message = "requested value for pseudonym " + psn + " can't be retrieved - it is anonymised";
					LOGGER.info(message);
					throw new ValueIsAnonymisedException(message);
				}
				return result;
			}
			finally
			{
				psnRWL.get(domainName).readLock().unlock();
			}
		}
		finally
		{
			domainRWL.readLock().unlock();
		}
	}

	public Map<String, String> getOrCreatePseudonymForList(Set<String> values, String domainName) throws DomainIsFullException, UnknownDomainException
	{
		Map<String, String> result;
		GetPSNsResult tempResult;
		domainRWL.readLock().lock();
		try
		{
			Domain domain = getDomain(domainName);
			psnRWL.get(domainName).readLock().lock();
			try
			{
				tempResult = getPseudonymForListWithErrorSets(values, domain);
			}
			finally
			{
				psnRWL.get(domainName).readLock().unlock();
			}
			result = tempResult.getPsns();
			if (LOGGER.isDebugEnabled())
			{
				LOGGER.debug("found " + result.size() + " psns; now creating " + tempResult.getNotFound().size() + " new ones");
			}
			if (!tempResult.getNotFound().isEmpty())
			{
				psnRWL.get(domainName).writeLock().lock();
				try
				{
					result.putAll(createPseudonymForList(tempResult.getNotFound(), domain));
				}
				finally
				{
					psnRWL.get(domainName).writeLock().unlock();
				}
			}
			for (String value : tempResult.getInvalid())
			{
				result.put(value, PSNErrorStrings.INVALID_VALUE);
			}
			return result;
		}
		finally
		{
			domainRWL.readLock().unlock();
		}
	}

	public Map<String, String> getValueForList(Set<String> psns, String domainName) throws UnknownDomainException
	{
		HashMap<String, String> result = new HashMap<>((int) Math.ceil(psns.size() / 0.75));
		Set<String> temp = new HashSet<>();
		domainRWL.readLock().lock();
		try
		{
			getDomain(domainName);
			psnRWL.get(domainName).readLock().lock();
			try
			{
				for (String psn : psns)
				{
					try
					{
						validatePSN(psn, domainName);
					}
					catch (InvalidPSNException e)
					{
						temp.add(psn);
						result.put(psn, PSNErrorStrings.INVALID_PSN);
					}
				}
				if (LOGGER.isDebugEnabled())
				{
					LOGGER.debug("found " + temp.size() + " invalid requested psns");
				}
				if (!psns.isEmpty())
				{
					List<PSN> foundPSNs = dao.getPSNObjectsForPSNs(domainName, psns);
					int anoCounter = 0;
					for (PSN found : foundPSNs)
					{
						temp.add(found.getPseudonym());
						if (isAnonym(found.getKey().getOriginValue()))
						{
							result.put(found.getPseudonym(), PSNErrorStrings.VALUE_IS_ANONYMISED);
							anoCounter++;
						}
						else
						{
							result.put(found.getPseudonym(), found.getKey().getOriginValue());
						}
					}
					if (LOGGER.isDebugEnabled())
					{
						LOGGER.debug("found " + anoCounter + " anonymised requested psns");
					}
					for (String psn : psns)
					{
						if (!temp.contains(psn))
						{
							result.put(psn, PSNErrorStrings.PSN_NOT_FOUND);
						}
					}
					if (LOGGER.isDebugEnabled())
					{
						LOGGER.debug("couldn't find " + (psns.size() - temp.size()) + " requested psns");
					}
				}
				return result;
			}
			finally
			{
				psnRWL.get(domainName).readLock().unlock();
			}
		}
		finally
		{
			domainRWL.readLock().unlock();
		}
	}

	public Map<String, String> getPseudonymForList(Set<String> values, String domainName) throws UnknownDomainException
	{
		HashMap<String, String> result = new HashMap<>((int) Math.ceil(values.size() / 0.75));
		Set<String> valuesWithPsn = new HashSet<>();
		domainRWL.readLock().lock();
		try
		{
			Domain domain = getDomain(domainName);
			psnRWL.get(domainName).readLock().lock();
			try
			{
				List<PSN> foundPSNs = dao.getPSNObjectsForValues(domainName, values);
				int anoCounter = 0;
				for (PSN found : foundPSNs)
				{
					valuesWithPsn.add(found.getKey().getOriginValue());
					if (isAnonym(found.getKey().getOriginValue()))
					{
						result.put(PSNErrorStrings.VALUE_IS_ANONYMISED, found.getPseudonym());
						anoCounter++;
					}
					else
					{
						result.put(found.getKey().getOriginValue(), found.getPseudonym());
					}
				}
				if (LOGGER.isDebugEnabled())
				{
					LOGGER.debug("found " + anoCounter + " anonymised requested values");
				}
				for (String value : values)
				{
					if (!valuesWithPsn.contains(value))
					{
						try
						{
							checkParentRules(domain, value);
							result.put(value, PSNErrorStrings.VALUE_NOT_FOUND);
						}
						catch (InvalidParameterException e)
						{
							result.put(value, PSNErrorStrings.INVALID_VALUE);
						}
					}
				}
				if (LOGGER.isDebugEnabled())
				{
					LOGGER.debug("couldn't find " + (values.size() - valuesWithPsn.size()) + " requested values");
				}
				return result;
			}
			finally
			{
				psnRWL.get(domainName).readLock().unlock();
			}
		}
		finally
		{
			domainRWL.readLock().unlock();
		}
	}

	public Map<String, String> getPseudonymForValuePrefix(String valuePrefix, String domainName) throws UnknownDomainException
	{
		HashMap<String, String> result = new HashMap<>();
		domainRWL.readLock().lock();
		try
		{
			getDomain(domainName);
			psnRWL.get(domainName).readLock().lock();
			try
			{
				List<PSN> psnList = dao.getAllPSNObjectsForValuePrefix(domainName, valuePrefix);
				for (PSN psn : psnList)
				{
					result.put(psn.getKey().getOriginValue(), psn.getPseudonym());
				}
				return result;
			}
			finally
			{
				psnRWL.get(domainName).readLock().unlock();
			}
		}
		finally
		{
			domainRWL.readLock().unlock();
		}
	}

	public void insertValuePseudonymPair(String value, String pseudonym, String domainName) throws InsertPairException, UnknownDomainException
	{
		domainRWL.readLock().lock();
		try
		{
			Domain domain = getDomain(domainName);
			psnRWL.get(domainName).writeLock().lock();
			try
			{
				validatePSN(pseudonym, domainName);
				checkParentRules(domain, value);
				if (dao.insertValuePseudonymPair(value, pseudonym, domain))
				{
					if (generatorCache.get(domainName).isUseCache())
					{
						try
						{
							psnCache.get(domainName).setPos(generatorCache.get(domainName).getPosNumberForPSN(pseudonym));
						}
						catch (ArithmeticException | CharNotInAlphabetException e)
						{
							LOGGER.fatal("program error", e);
						}
					}
					psnCounterCache.put(domainName, psnCounterCache.get(domainName) + 1);
					if (isAnonym(value))
					{
						anoCounterCache.put(domainName, anoCounterCache.get(domainName) + 1);
					}
				}
			}
			catch (InvalidParameterException e)
			{
				throw new InsertPairException(value + " is not a valid value for domain " + domain.getName() + " (validation check for pseudonym rules for parent domains is enabled)", e, value,
						pseudonym, InsertPairError.VALUE_INVALID);
			}
			catch (InvalidPSNException e)
			{
				throw new InsertPairException(e.getMessage(), e, value, pseudonym, InsertPairError.PSEUDONYM_INVALID);
			}
			finally
			{
				psnRWL.get(domainName).writeLock().unlock();
			}
		}
		finally
		{
			domainRWL.readLock().unlock();
		}
	}

	public List<InsertPairExceptionDTO> insertValuePseudonymPairs(Map<String, String> pairs, String domainName) throws UnknownDomainException
	{
		if (LOGGER.isDebugEnabled())
		{
			LOGGER.debug("insertValuePseudonymPairs for " + pairs.size() + " value-pseudonym pairs within domain " + domainName);
		}
		List<InsertPairExceptionDTO> result = new ArrayList<>();
		domainRWL.readLock().lock();
		try
		{
			Domain domain = getDomain(domainName);
			psnRWL.get(domainName).writeLock().lock();
			try
			{
				for (Entry<String, String> entry : pairs.entrySet())
				{
					try
					{
						validatePSN(entry.getValue(), domainName);
						checkParentRules(domain, entry.getKey());
						// TODO optimieren: dao.insertValuePseudonymPairs?
						if (dao.insertValuePseudonymPair(entry.getKey(), entry.getValue(), domain))
						{
							if (generatorCache.get(domainName).isUseCache())
							{
								try
								{
									psnCache.get(domainName).setPos(generatorCache.get(domainName).getPosNumberForPSN(entry.getValue()));
								}
								catch (ArithmeticException | CharNotInAlphabetException e)
								{
									LOGGER.fatal("program error", e);
								}
							}
							psnCounterCache.put(domainName, psnCounterCache.get(domainName) + 1);
							if (isAnonym(entry.getValue()))
							{
								anoCounterCache.put(domainName, anoCounterCache.get(domainName) + 1);
							}
						}
					}
					catch (InvalidPSNException e)
					{
						result.add(new InsertPairExceptionDTO(e.getMessage(), entry.getKey(), entry.getValue(), InsertPairError.PSEUDONYM_INVALID));
					}
					catch (InvalidParameterException e)
					{
						result.add(
								new InsertPairExceptionDTO(entry.getKey() + " is not a valid value for domain " + domainName + " (validation check for pseudonym rules for parent domains is enabled)",
										entry.getKey(), entry.getValue(), InsertPairError.VALUE_INVALID));
					}
					catch (InsertPairException e)
					{
						result.add(new InsertPairExceptionDTO(e.getMessage(), e.getValue(), e.getPseudonym(), e.getReason()));
					}
				}

			}
			finally
			{
				psnRWL.get(domainName).writeLock().unlock();
			}
		}
		finally
		{
			domainRWL.readLock().unlock();
		}
		return result;
	}

	public PSNTreeDTO getPSNTreeForPSN(String psn, String domainName) throws InvalidPSNException, PSNNotFoundException, UnknownDomainException, ValueIsAnonymisedException
	{
		if (LOGGER.isDebugEnabled())
		{
			LOGGER.debug("getPSNTreeForPSN for " + psn + " within domain " + domainName);
		}
		PSNTreeDTO rootNode;
		domainRWL.readLock().lock();
		try
		{
			Domain currentProject = getDomain(domainName);
			psnRWL.get(domainName).readLock().lock();
			try
			{
				String currentPSN = psn;
				boolean done = false;
				// Zum Root Projekt zurueckiterieren
				while (!done && currentProject.getParents() != null && currentProject.getParents().size() > 0)
				{
					String tempPSN = getValueFor(currentPSN, currentProject.getName());
					Domain tempProject = getDomain(currentProject.getParents().get(0).getName());
					try
					{
						// test, ob eintrag in parentdomain vorhanden ist
						getValueFor(tempPSN, tempProject.getName());
						currentPSN = tempPSN;
						currentProject = tempProject;
					}
					catch (InvalidPSNException | PSNNotFoundException maybe)
					{
						done = true;
					}
				}
				// Initiales hinzufuegen des Root Nodes. Bei diesem wird der originalValue des
				// aktuellen
				// Projektes verwendet und nicht das Pseudonym
				String currentValue = getValueFor(currentPSN, currentProject.getName());
				rootNode = new PSNTreeDTO(PSNTreeDTO.ROOT, "", currentValue, 0);
				rootNode.addChild(createPSNTree(currentValue, currentPSN, currentProject, 1));
				if (LOGGER.isDebugEnabled())
				{
					LOGGER.debug("psn tree created");
				}
				return rootNode;
			}
			finally
			{
				psnRWL.get(domainName).readLock().unlock();
			}
		}
		finally
		{
			domainRWL.readLock().unlock();
		}
	}

	public PSNNetDTO getPSNNetFor(String valueOrPSN) throws InvalidParameterException
	{
		if (LOGGER.isDebugEnabled())
		{
			LOGGER.debug("getPSNNetFor for value or psn " + valueOrPSN);
		}
		PSNNetDTO net = new PSNNetDTO();
		domainRWL.readLock().lock();
		try
		{
			Set<PSN> psns = collectPSNsConnectedTo(valueOrPSN);
			if (!psns.isEmpty())
			{
				Map<PSNKey, PSNNetNodeDTO> allNodes = createAllNodes(psns);
				Set<PSNNetNodeDTO> nodesToProcess = new HashSet<>();
				nodesToProcess.addAll(allNodes.values());
				net.getNodes().addAll(allNodes.values());
				Set<PSNNetNodeDTO> roots = getRootsForRelations(nodesToProcess);
				do
				{
					Set<PSNNetNodeDTO> processedNodes = buildPSNNet(net, allNodes, roots);
					nodesToProcess.removeAll(processedNodes);
					processedNodes.clear();
					roots.clear();
					if (!nodesToProcess.isEmpty())
					{
						// geschlossener kreis ohne verbindung zu anderen psn; ein psn aus einem
						// kreis
						// suchen - deckt folgendes ab:
						// 1 -> 2 -> 3 -> 1
						// 3 -> 4 -> 5
						// 4 oder 5 darf hier nicht gewaehlt werden
						roots.add(getCircleNode(nodesToProcess));
					}
				}
				while (!roots.isEmpty());
			}
			if (LOGGER.isDebugEnabled())
			{
				LOGGER.debug("psn net " + net + " created");
			}
			return net;
		}
		finally
		{
			domainRWL.readLock().unlock();
		}
	}

	// --------------------- domains ---------------------

	public void addDomain(DomainInDTO domainDTO) throws DomainInUseException, InvalidAlphabetException, InvalidCheckDigitClassException,
			InvalidGeneratorException, InvalidParameterException, InvalidParentDomainException, UnknownDomainException
	{
		domainRWL.writeLock().lock();
		try
		{
			if (domainCache.containsKey(domainDTO.getName()))
			{
				String message = "domain " + domainDTO.getName() + " already exists";
				LOGGER.warn(message);
				throw new DomainInUseException(message);
			}
			List<Domain> parents = checkParentDomains(domainDTO);
			Domain domain = new Domain(domainDTO, parents);
			Cache.testConfig(domain);
			dao.addDomain(domain, parents);
			addDomainToCaches(domain);
		}
		finally
		{
			domainRWL.writeLock().unlock();
		}
	}

	public void updateDomain(DomainInDTO domainDTO) throws DomainInUseException, InvalidAlphabetException, InvalidCheckDigitClassException,
			InvalidGeneratorException, InvalidParameterException, UnknownDomainException, InvalidParentDomainException
	{
		domainRWL.writeLock().lock();
		try
		{
			Domain domain = getDomain(domainDTO.getName());
			psnRWL.get(domainDTO.getName()).writeLock().lock();
			try
			{
				if (psnCounterCache.get(domainDTO.getName()) > 0)
				{
					String message = "at least one pseudonym belongs to domain " + domainDTO.getName() + " which therefore can't be updated";
					LOGGER.warn(message);
					throw new DomainInUseException(message);
				}
				updateDomainWithParents(domain, domainDTO);
			}
			finally
			{
				psnRWL.get(domainDTO.getName()).writeLock().unlock();
			}
		}
		finally
		{
			domainRWL.writeLock().unlock();
		}
	}

	public void updateDomainInUse(String domainName, String label, String comment, boolean sendNotificationsWeb, boolean psnsDeletable)
			throws UnknownDomainException
	{
		domainRWL.writeLock().lock();
		try
		{
			Domain domain = getDomain(domainName);
			psnRWL.get(domainName).writeLock().lock();
			try
			{
				domain.updateInUse(label, comment, sendNotificationsWeb, psnsDeletable);
				dao.updateDomain(domain, null, null);
			}
			finally
			{
				psnRWL.get(domainName).writeLock().unlock();
			}
		}
		finally
		{
			domainRWL.writeLock().unlock();
		}
	}

	private void updateDomainWithParents(Domain domain, DomainInDTO domainDTO)
			throws InvalidParameterException, InvalidParentDomainException, InvalidGeneratorException, InvalidAlphabetException, InvalidCheckDigitClassException
	{
		List<Domain> parents = checkParentDomains(domainDTO);
		Domain testProject = new Domain(domainDTO, parents);
		Cache.testConfig(testProject);
		List<Domain> oldParents = domain.getParents();
		if (oldParents != null)
		{
			for (Domain parent : oldParents)
			{
				parent.getChildren().remove(domain);
			}
		}
		domain.update(domainDTO, parents);
		updateDomainInCaches(domain);

		for (Domain parent : parents)
		{
			parent.getChildren().add(domain);
		}
		dao.updateDomain(domain, parents, oldParents);
	}

	public void deleteDomain(String domainName, boolean force) throws DomainInUseException, UnknownDomainException
	{
		domainRWL.writeLock().lock();
		try
		{
			Domain domain = getDomain(domainName);
			if (!force && psnCounterCache.get(domainName) > 0)
			{
				String message = "at least one pseudonym belongs to domain " + domainName + " which therefore can't be deleted";
				LOGGER.warn(message);
				throw new DomainInUseException(message);
			}
			if (domain.getChildren().size() > 0)
			{
				String message = "at least one domain is a child of domain " + domainName + " which therefore can't be deleted";
				LOGGER.warn(message);
				throw new DomainInUseException(message);
			}
			List<Domain> parents = domain.getParents();
			for (Domain parent : parents)
			{
				parent.getChildren().remove(domain);
			}
			if (force && psnCounterCache.get(domainName) > 0)
			{
				dao.deleteAllPSNForDomain(domainName);
			}
			dao.deleteDomain(domain, parents);
			removeDomain(domainName);
		}
		finally
		{
			domainRWL.writeLock().unlock();
		}
	}

	public DomainOutDTO getDomainDTO(String domainName) throws UnknownDomainException
	{
		domainRWL.readLock().lock();
		try
		{
			DomainOutDTO result = createDomainDTO(getDomain(domainName));
			return result;
		}
		finally
		{
			domainRWL.readLock().unlock();
		}
	}

	public List<DomainOutDTO> listDomains()
	{
		domainRWL.readLock().lock();
		try
		{
			List<DomainOutDTO> result = new ArrayList<>();
			for (Domain domain : domainCache.values())
			{
				if (!AnonymDomain.NAME.equals(domain.getName()))
				{
					result.add(createDomainDTO(domain));
				}
			}
			return result;
		}
		finally
		{
			domainRWL.readLock().unlock();
		}
	}

	public List<PSNDTO> listPSNs(String domainName) throws UnknownDomainException
	{
		List<PSNDTO> result = new ArrayList<>();
		domainRWL.readLock().lock();
		try
		{
			getDomain(domainName);
			psnRWL.get(domainName).readLock().lock();
			try
			{
				long psnCount = psnCounterCache.get(domainName);
				if (psnCount > PAGE_SIZE_FOR_CACHE_INIT)
				{
					String lastOrigValue = null;
					for (int i = 0; i * PAGE_SIZE_FOR_CACHE_INIT < psnCount; i++)
					{
						int nextPageSize = (int) ((i + 1) * PAGE_SIZE_FOR_CACHE_INIT < psnCount ? PAGE_SIZE_FOR_CACHE_INIT
								: psnCount - i * PAGE_SIZE_FOR_CACHE_INIT);
						if (nextPageSize > 0)
						{
							List<PSN> psns = dao.getPSNObjectsForDomainPaginated(domainName, PAGE_SIZE_FOR_CACHE_INIT, lastOrigValue);
							for (PSN psn : psns)
							{
								result.add(psn.toPSNDTO());
							}
							lastOrigValue = psns.get(psns.size() - 1).getKey().getOriginValue();
						}
					}
				}
				else
				{
					for (PSN psn : dao.getPSNObjectsForDomain(domainName))
					{
						result.add(psn.toPSNDTO());
					}
				}
				return result;
			}
			finally
			{
				psnRWL.get(domainName).readLock().unlock();
			}
		}
		finally
		{
			domainRWL.readLock().unlock();
		}
	}

	public long countPSNsForDomainsPaginated(List<String> domainNames, PaginationConfig config) throws UnknownDomainException
	{
		domainRWL.readLock().lock();
		try
		{
			for (String domainName : domainNames)
			{
				getDomain(domainName);
				psnRWL.get(domainName).readLock().lock();
			}
			try
			{
				if (config.getFilter().isEmpty())
				{
					long count = 0;
					for (String domainName : domainNames)
					{
						count += psnCounterCache.get(domainName);
					}
					return count;
				}
				else
				{
					return dao.countPSNObjectsForDomains(domainNames, config);
				}
			}
			finally
			{
				for (String domainName : domainNames)
				{
					try
					{
						psnRWL.get(domainName).readLock().unlock();
					}
					catch (IllegalMonitorStateException ignore)
					{
						// if an UnknownDomainException is thrown while acquiring the locks
					}
				}
			}
		}
		finally
		{
			domainRWL.readLock().unlock();
		}
	}

	public List<PSNDTO> listPSNsForDomainsPaginated(List<String> domainNames, PaginationConfig config) throws UnknownDomainException
	{
		List<PSNDTO> result = new ArrayList<>();
		domainRWL.readLock().lock();
		try
		{
			for (String domainName : domainNames)
			{
				getDomain(domainName);
				psnRWL.get(domainName).readLock().lock();
			}
			try
			{
				for (PSN psn : dao.getPSNObjectsForDomains(domainNames, config))
				{
					result.add(psn.toPSNDTO());
				}
				return result;
			}
			finally
			{
				for (String domainName : domainNames)
				{
					try
					{
						psnRWL.get(domainName).readLock().unlock();
					}
					catch (IllegalMonitorStateException ignore)
					{
						// if an UnknownDomainException is thrown while acquiring the locks
					}
				}
			}
		}
		finally
		{
			domainRWL.readLock().unlock();
		}
	}

	public List<DomainOutDTO> getDomainsForPrefix(String prefix) throws InvalidParameterException
	{
		List<DomainOutDTO> result = new ArrayList<>();
		domainRWL.readLock().lock();
		try
		{
			for (Domain domain : domainCache.values())
			{
				if (AnonymDomain.NAME.equals(domain.getName()))
				{
					continue;
				}
				if (domain.getConfig().getPsnPrefix().equals(prefix))
				{
					result.add(createDomainDTO(domain));
				}
			}
			return result;
		}
		finally
		{
			domainRWL.readLock().unlock();
		}
	}

	public List<DomainOutDTO> getDomainsForSuffix(String suffix)
	{
		List<DomainOutDTO> result = new ArrayList<>();
		domainRWL.readLock().lock();
		try
		{
			for (Domain domain : domainCache.values())
			{
				if (AnonymDomain.NAME.equals(domain.getName()))
				{
					continue;
				}
				if (domain.getConfig().getPsnSuffix().equals(suffix))
				{
					result.add(createDomainDTO(domain));
				}
			}
			return result;
		}
		finally
		{
			domainRWL.readLock().unlock();
		}
	}

	public GeneratorAlphabetRestriction getRestrictionForCheckDigitClass(String checkDigitClassName) throws InvalidCheckDigitClassException
	{
		Class<? extends CheckDigits> checkDigitClass = createCheckDigitClass(checkDigitClassName);
		GeneratorAlphabetRestriction result = null;
		try
		{
			Constructor<? extends CheckDigits> constructor = checkDigitClass.getConstructor();
			CheckDigits tempCheckDigit = constructor.newInstance();
			result = tempCheckDigit.getAlphabetRestriction();
		}
		catch (NoSuchMethodException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e)
		{
			String message = "can't create temp instance of check digit class '" + checkDigitClassName + "': " + e.getMessage();
			LOGGER.fatal(message, e);
			throw new InvalidCheckDigitClassException(message, e);
		}
		return result;
	}

	public boolean arePSNDeletable(String domainName) throws UnknownDomainException
	{
		domainRWL.readLock().lock();
		try
		{
			Domain domain = getDomain(domainName);
			psnRWL.get(domainName).readLock().lock();
			try
			{
				return domain.getConfig().isPsnsDeletable();
			}
			finally
			{
				psnRWL.get(domainName).readLock().unlock();
			}
		}
		finally
		{
			domainRWL.readLock().unlock();
		}
	}

	// --------------------- statistic ---------------------

	public StatisticDTO getLatestStats()
	{
		statisticRWL.readLock().lock();
		StatisticDTO result = null;
		try
		{
			Statistic stat = dao.getLatestStat();
			result = stat == null ? new StatisticDTO() : stat.toDTO();
		}
		catch (NoResultException | EJBException e)
		{
			// see #132
			// since (at least) wildfly 24 the expected NoResultException is wrapped into an EJBException
			if (e instanceof EJBException && !(e.getCause() instanceof NoResultException))
			{
				// only catch the case of a wrapped NoResultException, otherwise rethrow
				throw e;
			}
			result = new StatisticDTO();
		}
		finally
		{
			statisticRWL.readLock().unlock();
		}
		return result;
	}

	public List<StatisticDTO> getAllStats()
	{
		statisticRWL.readLock().lock();
		try
		{
			List<StatisticDTO> result = new ArrayList<>();
			List<Statistic> queryResult = dao.getAllStats();
			for (Statistic stats : queryResult)
			{
				result.add(stats.toDTO());
			}
			return result;
		}
		finally
		{
			statisticRWL.readLock().unlock();
		}
	}

	public StatisticDTO updateStats()
	{
		Instant start = Instant.now();

		StatisticDTO result = new StatisticDTO();
		long allPseudonyms = 0L;

		domainRWL.readLock().lock();
		try
		{
			// Create map with real pseudonym domains
			for (Entry<String, Long> entry : psnCounterCache.entrySet())
			{
				if (AnonymDomain.NAME.equals(entry.getKey()))
				{
					result.getMappedStatValue().put(StatisticKeys.ANONYMS, entry.getValue());
				}
				else
				{
					long pseudonyms = entry.getValue() - anoCounterCache.get(entry.getKey());
					allPseudonyms += pseudonyms;
					result.getMappedStatValue().put(StatisticKeys.PSEUDONYMS_PER_DOMAIN + entry.getKey(), pseudonyms);
					result.getMappedStatValue().put(StatisticKeys.ANONYMS_PER_DOMAIN + entry.getKey(), anoCounterCache.get(entry.getKey()));
				}
			}
			result.getMappedStatValue().put(StatisticKeys.PSEUDONYMS, allPseudonyms);
			result.getMappedStatValue().put(StatisticKeys.DOMAINS, (long) domainCache.size() - 1);
		}
		finally
		{
			domainRWL.readLock().unlock();
		}

		Instant finish = Instant.now();
		result.getMappedStatValue().put(StatisticKeys.CALCULATION_TIME, Duration.between(start, finish).toMillis());

		addStat(result);
		return result;
	}

	public void addStat(StatisticDTO statisticDTO)
	{
		statisticRWL.writeLock().lock();
		try
		{
			Statistic stat = new Statistic(statisticDTO);
			dao.addStat(stat);
		}
		finally
		{
			statisticRWL.writeLock().unlock();
		}
	}

	// --------------------- private ---------------------

	private PSNCacheObject createPSNCache(Domain domain, long psnCount)
	{
		Generator generator = generatorCache.get(domain.getName());
		PSNCacheObject result = new PSNCacheObject(generator.getMaxNumberForPSN());
		if (LOGGER.isDebugEnabled())
		{
			LOGGER.debug("found " + psnCount + " psns for domain " + domain.getName() + ", loading them into cache now");
		}
		String lastPseudonym = null;
		for (int i = 0; i * PAGE_SIZE_FOR_CACHE_INIT < psnCount; i++)
		{
			int nextPageSize = (int) ((i + 1) * PAGE_SIZE_FOR_CACHE_INIT < psnCount ? PAGE_SIZE_FOR_CACHE_INIT
					: psnCount - i * PAGE_SIZE_FOR_CACHE_INIT);
			if (nextPageSize > 0)
			{
				List<String> psns = dao.getPseudonymsForDomainPaginated(domain.getName(), PAGE_SIZE_FOR_CACHE_INIT, lastPseudonym);
				for (String psn : psns)
				{
					try
					{
						result.setPos(generator.getPosNumberForPSN(psn));
					}
					catch (ArithmeticException | CharNotInAlphabetException e)
					{
						LOGGER.fatal("program error", e);
					}
				}
				lastPseudonym = psns.get(psns.size() - 1);
			}
		}
		if (result.getCount() != psnCount)
		{
			LOGGER.fatal("unexpected result of create domain cache for domain " + domain.getName() + " - count within cache (" + result.getCount()
					+ ") is not equal count of psns within db for that domain (" + psnCount + ")");
		}
		if (LOGGER.isDebugEnabled())
		{
			LOGGER.debug("cache initialised for domain " + domain.getName());
		}
		return result;
	}

	private void addDomainToCaches(Domain domain) throws InvalidCheckDigitClassException, InvalidAlphabetException, InvalidGeneratorException
	{
		domainRWL.writeLock().lock();
		try
		{
			domainCache.put(domain.getName(), domain);
			long count = dao.countAnonymsForDomain(domain.getName());
			anoCounterCache.put(domain.getName(), count);
			count = dao.countPseudonymsForDomain(domain.getName());
			psnCounterCache.put(domain.getName(), count);
			generatorCache.put(domain.getName(), createGenerator(domain));
			if (generatorCache.get(domain.getName()).isUseCache())
			{
				psnCache.put(domain.getName(), createPSNCache(domain, count));
			}
			psnRWL.put(domain.getName(), new ReentrantReadWriteLock());
		}
		finally
		{
			domainRWL.writeLock().unlock();
		}
	}

	private void updateDomainInCaches(Domain domain) throws InvalidCheckDigitClassException, InvalidAlphabetException, InvalidGeneratorException
	{
		domainRWL.writeLock().lock();
		try
		{
			generatorCache.remove(domain.getName());
			psnCache.remove(domain.getName());
			generatorCache.put(domain.getName(), createGenerator(domain));
			if (generatorCache.get(domain.getName()).isUseCache())
			{
				psnCache.put(domain.getName(), createPSNCache(domain, 0));
			}
		}
		finally
		{
			domainRWL.writeLock().unlock();
		}
	}

	private static void removeDomain(String domainName)
	{
		domainRWL.writeLock().lock();
		try
		{
			domainCache.remove(domainName);
			psnCounterCache.remove(domainName);
			anoCounterCache.remove(domainName);
			generatorCache.remove(domainName);
			psnCache.remove(domainName);
			psnRWL.remove(domainName);
		}
		finally
		{
			domainRWL.writeLock().unlock();
		}
	}

	private static void testConfig(Domain domain) throws InvalidAlphabetException, InvalidCheckDigitClassException, InvalidGeneratorException, InvalidParameterException
	{
		createGenerator(domain);
		if (!ValidateViaParents.OFF.equals(domain.getConfig().getValidateValuesViaParents()) && domain.getParents().isEmpty())
		{
			throw new InvalidParameterException("isValidateValuesViaParents is true for domain " + domain.getName() + " but there are no parents set");
		}
	}

	private Domain getDomain(String domainName) throws UnknownDomainException
	{
		Domain result = domainCache.get(domainName);
		if (result == null)
		{
			String message = "db object for domain " + domainName + " not found";
			LOGGER.warn(message);
			throw new UnknownDomainException(message);
		}
		return result;
	}

	private DomainOutDTO createDomainDTO(Domain domain)
	{
		psnRWL.get(domain.getName()).readLock().lock();
		try
		{
			long maxPSNs = generatorCache.get(domain.getName()).getMaxNumberForPSN();
			long numberOfPSNs = psnCounterCache.get(domain.getName());
			long numberOfAnos = anoCounterCache.get(domain.getName());
			short percentPSNsUsed = maxPSNs != -1L ? (short) (numberOfPSNs * 100L / maxPSNs) : (short) -1;
			return domain.toDTO(numberOfPSNs, numberOfAnos, generatorCache.get(domain.getName()).isUseCache(), percentPSNsUsed);
		}
		finally
		{
			psnRWL.get(domain.getName()).readLock().unlock();
		}
	}

	private boolean existsPseudonym(String domain, String pseudonym, long number)
	{
		if (generatorCache.get(domain).isUseCache())
		{
			try
			{
				number = number == -1 ? generatorCache.get(domain).getPosNumberForPSN(pseudonym) : number;
				return psnCache.get(domain).isPosSet(number);
			}
			catch (ArithmeticException | CharNotInAlphabetException e)
			{
				LOGGER.fatal("program error - created an invalid pseudonym: " + pseudonym + "  for domain " + domain, e);
				return true;
			}
		}
		else
		{
			try
			{
				dao.getPSNObjectForPseudonym(domain, pseudonym);
			}
			catch (PSNNotFoundException e)
			{
				return false;
			}
			catch (InvalidPSNException e)
			{
				LOGGER.fatal("program error - created an invalid pseudonym: " + pseudonym + "  for domain " + domain, e);
			}
			return true;
		}
	}

	private void checkForErrorKeyAsValue(String value) throws InvalidParameterException
	{
		if (PSNErrorStrings.isPSNErrorString(value))
		{
			throw new InvalidParameterException("the given value " + value + " denotes an error state");
		}
	}

	private void checkForErrorKeyAsPSN(String psn) throws InvalidPSNException
	{
		if (PSNErrorStrings.isPSNErrorString(psn))
		{
			throw new InvalidPSNException("the given pseudonym " + psn + " denotes an error state");
		}
	}

	private void checkParentRules(Domain domain, String value) throws InvalidParameterException, UnknownDomainException
	{
		checkForErrorKeyAsValue(value);
		boolean valid = false;
		switch (domain.getConfig().getValidateValuesViaParents())
		{
			case CASCADE_DELETE:
			case ENSURE_EXISTS:
				for (Domain parent : domain.getParents())
				{
					try
					{
						validatePSN(value, parent.getName());
						valid = existsPseudonym(parent.getName(), value, -1);
						if (valid)
						{
							break;
						}
					}
					catch (InvalidPSNException ignore)
					{
						// could be if there are multiple parents
					}
				}
				if (!valid)
				{
					throw new InvalidParameterException("the given value " + value + " doesn't exist as pseudonym in any parent domain of domain " + domain.getName());
				}
				break;
			case VALIDATE:
				for (Domain parent : domain.getParents())
				{
					try
					{
						validatePSN(value, parent.getName());
						valid = true;
						break;
					}
					catch (InvalidPSNException ignore)
					{
						// could be if there are multiple parents
					}
				}
				if (!valid)
				{
					throw new InvalidParameterException("the given value " + value + " is no valid pseudonym in any parent domain of domain " + domain.getName());
				}
				break;
			case OFF:
				break;
			default:
				LOGGER.fatal("unimplemented case for ValidateViaParents: " + domain.getConfig().getValidateValuesViaParents());
		}
	}

	private void deleteEntryInternal(String value, String domainName) throws UnknownDomainException, UnknownValueException
	{
		PSN psn = dao.getPSNObject(value, domainName);
		String pseudonym = psn.getPseudonym();
		dao.deletePSN(value, domainName);
		if (generatorCache.get(domainName).isUseCache())
		{
			try
			{
				psnCache.get(domainName).unsetPos(generatorCache.get(domainName).getPosNumberForPSN(psn.getPseudonym()));
			}
			catch (ArithmeticException | CharNotInAlphabetException e)
			{
				LOGGER.fatal("program error", e);
			}
		}
		psnCounterCache.put(domainName, psnCounterCache.get(domainName) - 1);
		if (isAnonym(value))
		{
			anoCounterCache.put(domainName, anoCounterCache.get(domainName) - 1);
		}
		// children pruefen und dort eintraege loeschen, wenn cascade delete configuriert ist und das pseudonym bei keinem weiteren parent vorkommt
		Domain domain = getDomain(domainName);
		for (Domain child : domain.getChildren())
		{
			if (ValidateViaParents.CASCADE_DELETE.equals(child.getConfig().getValidateValuesViaParents()))
			{
				psnRWL.get(child.getName()).writeLock().lock();
				try
				{
					boolean found = false;
					for (Domain childParent : child.getParents())
					{
						psnRWL.get(childParent.getName()).readLock().lock();
						try
						{
							if (existsPseudonym(domainName, pseudonym, generatorCache.get(childParent.getName()).getPosNumberForPSN(pseudonym)))
							{
								found = true;
								break;
							}
						}
						catch (ArithmeticException | CharNotInAlphabetException ignore)
						{
							// moeglich bei mehreren parents
						}
						finally
						{
							psnRWL.get(childParent.getName()).readLock().unlock();
						}
					}
					if (!found)
					{
						deleteEntryInternal(pseudonym, child.getName());
					}
				}
				finally
				{
					psnRWL.get(child.getName()).writeLock().unlock();
				}
			}
		}
	}

	private PSN createPSN(Domain domain, String value) throws DomainIsFullException
	{
		Generator generator = generatorCache.get(domain.getName());
		if (psnCounterCache.get(domain.getName()) + 1 >= generator.getMaxNumberForPSN())
		{
			String message = "can't create any more pseudonyms for domain " + domain.getName();
			LOGGER.error(message);
			throw new DomainIsFullException(message);
		}
		PSN result;
		// countCollisions - zaehler fuer kollisionen generiertes pseudonym -
		// vorhandene pseudonyme (domain+pseudonym muss unique sein)
		int countCollisions = 0;
		boolean done = false;
		String pseudonym;
		long number = -1;
		do
		{
			if (!generator.isUseCache())
			{
				pseudonym = generator.getNewPseudonym(number);
			}
			else
			{
				number = nextLong(0, generator.getMaxNumberForPSN());
				pseudonym = generator.getNewPseudonym(number);
			}
			if (existsPseudonym(domain.getName(), pseudonym, number))
			{
				if (LOGGER.isTraceEnabled())
				{
					LOGGER.trace("duplicate pseudonym generated - attemp " + countCollisions + " of " + MAX_ATTEMPS_BEFORE_SEARCH_NEXT_FREE_PSN);
				}
				countCollisions++;
				// sollte zu oft ein schon vorhandener psn generiert worden sein -> deterministische
				// suche
				if (countCollisions > MAX_ATTEMPS_BEFORE_SEARCH_NEXT_FREE_PSN)
				{
					// TODO
				}
			}
			else
			{
				done = true;
			}
		}
		while (!done);
		result = new PSN(domain, value, pseudonym);
		dao.addPSN(result);
		if (generator.isUseCache())
		{
			psnCache.get(domain.getName()).setPos(number);
		}
		psnCounterCache.put(domain.getName(), psnCounterCache.get(domain.getName()) + 1);
		return result;
	}

	// java-api function for longStreams
	private long nextLong(long origin, long bound)
	{
		long r = rand.nextLong();
		long n = bound - origin, m = n - 1;
		if ((n & m) == 0L)
		{
			r = (r & m) + origin;
		}
		else if (n > 0L)
		{ // reject over-represented candidates
			for (long u = r >>> 1; // ensure nonnegative
					u + m - (r = u % n) < 0L; // rejection check
					u = rand.nextLong() >>> 1)
			{

			}
			r += origin;
		}
		else
		{ // range not representable as long
			while (r < origin || r >= bound)
			{
				r = rand.nextLong();
			}
		}
		return r;
	}

	private GetPSNsResult getPseudonymForListWithErrorSets(Set<String> values, Domain domain) throws UnknownDomainException
	{
		HashMap<String, String> psns = new HashMap<>((int) Math.ceil(values.size() / 0.75));
		Set<String> notFound = new HashSet<>();
		Set<String> invalid = new HashSet<>();
		List<PSN> existingPsns = dao.getPSNObjectsForValues(domain.getName(), values);
		for (PSN psn : existingPsns)
		{
			psns.put(psn.getKey().getOriginValue(), psn.getPseudonym());
		}
		for (String value : values)
		{
			try
			{
				checkParentRules(domain, value);
				String pseudonym = psns.get(value);
				if (pseudonym == null)
				{
					notFound.add(value);
				}
			}
			catch (InvalidParameterException e)
			{
				invalid.add(value);
			}
		}
		return new GetPSNsResult(psns, notFound, invalid);
	}

	private Map<String, String> createPseudonymForList(Set<String> values, Domain domain) throws DomainIsFullException
	{
		Generator generator = generatorCache.get(domain.getName());
		if (psnCounterCache.get(domain.getName()) + values.size() >= generator.getMaxNumberForPSN())
		{
			String message = "can't create any more pseudonyms for domain " + domain.getName();
			LOGGER.error(message);
			throw new DomainIsFullException(message);
		}
		Map<String, String> result = new HashMap<>();
		if (generator.isUseCache())
		{
			int countCollisions = 0;
			boolean done = false;
			String pseudonym;
			Set<Long> numbers = new HashSet<>();
			for (String value : values)
			{
				long number;
				do
				{
					number = nextLong(0, generator.getMaxNumberForPSN());
					pseudonym = generator.getNewPseudonym(number);
					if (existsPseudonym(domain.getName(), pseudonym, number) || numbers.contains(number))
					{
						if (LOGGER.isTraceEnabled())
						{
							LOGGER.trace("duplicate pseudonym generated - attemp " + countCollisions + " of " + MAX_ATTEMPS_BEFORE_SEARCH_NEXT_FREE_PSN);
						}
						countCollisions++;
						// TODO sollte zu oft ein schon vorhandener psn generiert worden sein ->
						// deterministische suche
						if (countCollisions > MAX_ATTEMPS_BEFORE_SEARCH_NEXT_FREE_PSN)
						{
							// TODO Wenn man hier her kommt wird trotzdem das PSN gespeichert und
							// dann wird es knallen am ende
							// noe.
							// LOGGER.warn("max collisions " + countCollisions + " reached for value
							// " + value);
						}
					}
					else
					{
						done = true;
					}
				}
				while (!done);
				done = false;
				result.put(value, pseudonym);
				numbers.add(number);
			}
			dao.insertValuePseudonymPairsWithoutCheck(result, domain);
			PSNCacheObject psnCacheObject = psnCache.get(domain.getName());
			for (Long number : numbers)
			{
				psnCacheObject.setPos(number);
			}
		}
		else
		{
			Set<String> generatedPSNs = generatePossiblePSNs(domain.getName(), Math.round(values.size() * 1.1 + 100));
			generatedPSNs.removeAll(dao.getExistingPSNs(domain.getName(), generatedPSNs));
			while (generatedPSNs.size() < values.size())
			{
				Set<String> generatedPSNs2 = generatePossiblePSNs(domain.getName(), Math.round((values.size() - generatedPSNs.size()) * 1.1 + 100));
				generatedPSNs2.removeAll(dao.getExistingPSNs(domain.getName(), generatedPSNs2));
				generatedPSNs.addAll(generatedPSNs2);
			}
			Iterator<String> psns = generatedPSNs.iterator();
			for (String value : values)
			{
				result.put(value, psns.next());
			}
			dao.insertValuePseudonymPairsWithoutCheck(result, domain);
		}
		psnCounterCache.put(domain.getName(), psnCounterCache.get(domain.getName()) + result.size());
		return result;
	}

	private Set<String> generatePossiblePSNs(String domainName, long size)
	{
		Set<String> result = new HashSet<>((int) Math.round(size * 1.34));
		Generator generator = generatorCache.get(domainName);
		for (long i = 0; i < size; i++)
		{
			result.add(generator.getNewPseudonym(-1L));
		}
		return result;
	}

	private static Generator createGenerator(Domain domain) throws InvalidCheckDigitClassException, InvalidAlphabetException, InvalidGeneratorException
	{
		if (LOGGER.isDebugEnabled())
		{
			LOGGER.debug("createGenerator for domain " + domain.getName() + " with check digit class " + domain.getGeneratorClass()
					+ " and alphabet " + domain.getAlphabet());
		}
		Class<? extends CheckDigits> checkDigitClass = createCheckDigitClass(domain.getGeneratorClass());
		Alphabet alphabet = createAlphabet(domain.getName(), domain.getAlphabet());
		if (LOGGER.isDebugEnabled())
		{
			LOGGER.debug("creating generator");
		}
		return new Generator(checkDigitClass, alphabet, domain.getConfig(), domain.getName());
	}

	private static Alphabet createAlphabet(String domainName, String alphabetString) throws InvalidAlphabetException
	{
		Alphabet result;
		if (alphabetString == null)
		{
			String message = "alphabet is null";
			LOGGER.error(message);
			throw new InvalidAlphabetException(message);
		}
		if (alphabetString.contains(","))
		{
			try
			{
				if (LOGGER.isDebugEnabled())
				{
					LOGGER.debug("creating generic alphabet with following chars: " + alphabetString);
				}
				result = new GenericAlphabet(alphabetString);
			}
			catch (Exception e)
			{
				String message = "exception while creating alphabet '" + alphabetString + "' - " + e
						+ (e.getCause() != null ? "(" + e.getCause() + ")" : "");
				LOGGER.error(message, e);
				throw new InvalidAlphabetException(message, e);
			}
		}
		else
		{
			Class<? extends Alphabet> alphabetClass;
			try
			{
				LOGGER.debug("creating alphabet class");
				Class<?> temp = Class.forName(alphabetString);
				alphabetClass = temp.asSubclass(Alphabet.class);
				result = alphabetClass.getDeclaredConstructor().newInstance();
			}
			catch (Exception e)
			{
				String message = "exception while creating alphabet class '" + alphabetString + "' - " + e
						+ (e.getCause() != null ? "(" + e.getCause() + ")" : "");
				LOGGER.error(message, e);
				throw new InvalidAlphabetException(message, e);
			}
		}
		return result;
	}

	private static Class<? extends CheckDigits> createCheckDigitClass(String checkDigitClass) throws InvalidCheckDigitClassException
	{
		Class<? extends CheckDigits> result;
		if (checkDigitClass == null)
		{
			String message = "check digit class is null";
			LOGGER.error(message);
			throw new InvalidCheckDigitClassException(message);
		}
		try
		{
			if (LOGGER.isDebugEnabled())
			{
				LOGGER.debug("loading check digit class");
			}
			Class<?> temp = Class.forName(checkDigitClass);
			result = temp.asSubclass(CheckDigits.class);
		}
		catch (Exception e)
		{
			String message = "exception while loading check digit class " + checkDigitClass + " - " + e + (e.getCause() != null ? "(" + e.getCause() + ")" : "");
			LOGGER.error(message, e);
			throw new InvalidCheckDigitClassException(message, e);
		}
		return result;
	}

	// --------------------- private fuer tree / net ---------------------

	/**
	 * Recursively traverse deeper into domains until all domains related to the passed
	 * originalValue and domain are found
	 *
	 * @return a PSNTreeNode containing child nodes, child child nodes and so on
	 */
	private PSNTreeDTO createPSNTree(String value, String psn, Domain domain, int i)
	{
		PSNTreeDTO currentNode = new PSNTreeDTO(domain.getName(), value, psn, i);
		for (Domain child : domain.getChildren())
		{
			try
			{
				String nextPSN = dao.getPSNObject(psn, child.getName()).getPseudonym();
				currentNode.addChild(createPSNTree(psn, nextPSN, child, i + 1));
			}
			catch (UnknownValueException e)
			{
				LOGGER.debug("ignoring exception: no pseudonym available in domain: " + child.getName() + " for value: " + value);
			}
		}
		return currentNode;
	}

	private Set<PSN> collectPSNsConnectedTo(String valueOrPSN)
	{
		Set<PSN> result = new HashSet<>();
		Set<String> allValues = new HashSet<>(); // abgearbeitet
		Set<String> currentValues = new HashSet<>(); // werden aktuell bearbeitet
		Set<String> newValues = new HashSet<>(); // neu gefunden - werden in der naechsten iteration
		// bearbeitet
		currentValues.add(valueOrPSN);
		List<DomainOutDTO> domains = listDomains();
		// liste aller elemente, fuer jedes neues alle domains durchsuchen, ob das dort value oder
		// psn ist -> eventuell neue werte -> solange, bis nix neues mehr dazu kommt
		while (!currentValues.isEmpty())
		{
			for (String value : currentValues)
			{
				for (DomainOutDTO domain : domains)
				{
					try
					{
						PSN psn = dao.getPSNObject(value, domain.getName());
						if (!allValues.contains(psn.getPseudonym()) && !currentValues.contains(psn.getPseudonym()))
						{
							newValues.add(psn.getPseudonym());
						}
						result.add(psn);
					}
					catch (UnknownValueException e)
					{
						LOGGER.debug("ignoring exception: no pseudonym available in domain: " + domain.getName() + " for value: " + value);
					}
					try
					{
						PSN psn = dao.getPSNObjectForPseudonym(domain.getName(), value);
						if (!allValues.contains(psn.getKey().getOriginValue()) && !currentValues.contains(psn.getKey().getOriginValue()))
						{
							newValues.add(psn.getKey().getOriginValue());
						}
						result.add(psn);
					}
					catch (PSNNotFoundException | InvalidPSNException e)
					{}
				}
			}
			allValues.addAll(currentValues);
			currentValues = newValues;
			newValues = new HashSet<>();
		}
		return result;
	}

	private Map<PSNKey, PSNNetNodeDTO> createAllNodes(Set<PSN> psns)
	{
		Map<PSNKey, PSNNetNodeDTO> result = new HashMap<>();
		for (PSN psn : psns)
		{
			result.put(psn.getKey(), new PSNNetNodeDTO(psn.getKey().getDomain(), psn.getKey().getOriginValue(), psn.getPseudonym(), -1));
		}
		return result;
	}

	private Set<PSNNetNodeDTO> getRootsForRelations(Set<PSNNetNodeDTO> nodes)
	{
		Set<PSNNetNodeDTO> result = new HashSet<>();
		Set<String> values = new HashSet<>();
		Set<String> pseudonyms = new HashSet<>();
		for (PSNNetNodeDTO node : nodes)
		{
			values.add(node.getOriginalValue());
			pseudonyms.add(node.getPseudonym());
		}
		values.removeAll(pseudonyms);
		for (String value : values)
		{
			for (PSNNetNodeDTO node : nodes)
			{
				if (value.equals(node.getOriginalValue()))
				{
					result.add(node); // kein break, koennen mehrere sein
				}
			}
		}
		return result;
	}

	private Set<PSNNetNodeDTO> buildPSNNet(PSNNetDTO net, Map<PSNKey, PSNNetNodeDTO> allNodes, Set<PSNNetNodeDTO> roots)
	{
		Set<PSNNetNodeDTO> processedNodes = new HashSet<>();
		for (PSNNetNodeDTO root : roots)
		{
			PSNNetNodeDTO child = allNodes.get(new PSNKey(root.getOriginalValue(), root.getDomainName()));
			child.setLevel(0);
			net.getRoot().addChild(processNetNode(processedNodes, allNodes, new HashSet<>(Collections.singleton(child)), child, 0));
		}
		return processedNodes;
	}

	private PSNNetNodeDTO processNetNode(Set<PSNNetNodeDTO> processedNodes, Map<PSNKey, PSNNetNodeDTO> allNodes, Set<PSNNetNodeDTO> currentBranch,
			PSNNetNodeDTO currentNode, int i)
	{
		if (currentNode.getLevel() < i)
		{
			currentNode.setLevel(i);
		}
		for (PSNNetNodeDTO node : allNodes.values())
		{
			if (node.getOriginalValue().equals(currentNode.getPseudonym()))
			{
				if (currentBranch.contains(node))
				{
					currentNode.getCircleChildren().add(node.toPSNDTO());
				}
				else
				{
					currentBranch.add(node);
					currentNode.addChild(processNetNode(processedNodes, allNodes, currentBranch, node, i + 1));
					currentBranch.remove(node);
				}
			}
		}
		processedNodes.add(currentNode);
		return currentNode;
	}

	private PSNNetNodeDTO getCircleNode(Set<PSNNetNodeDTO> allNodes)
	{
		for (PSNNetNodeDTO node : allNodes)
		{
			if (searchCircleNode(allNodes, node, new HashSet<>(Collections.singleton(node))))
			{
				return node;
			}
		}
		return null; // kann nicht passieren
	}

	private boolean searchCircleNode(Set<PSNNetNodeDTO> allNodes, PSNNetNodeDTO currentNode, Set<PSNNetNodeDTO> collectedNodes)
	{
		for (PSNNetNodeDTO node : allNodes)
		{
			if (node.getOriginalValue().equals(currentNode.getPseudonym()))
			{
				if (collectedNodes.contains(node))
				{
					return true;
				}
				else
				{
					collectedNodes.add(node);
					if (searchCircleNode(allNodes, node, collectedNodes))
					{
						return true;
					}
					collectedNodes.remove(node);
				}
			}
		}
		return false;
	}

	private List<Domain> checkParentDomains(DomainInDTO domainDTO) throws InvalidParentDomainException
	{
		List<Domain> parents = new ArrayList<>();
		if (domainDTO.getParentDomainNames() != null)
		{
			for (String parentDomainName : domainDTO.getParentDomainNames())
			{
				try
				{
					Domain parent = getDomain(parentDomainName);
					if (parentDomainName.equals(domainDTO.getName()))
					{
						String message = "parent domain " + parentDomainName + " cannot be same as current domain";
						LOGGER.warn(message);
						throw new InvalidParentDomainException(message);
					}
					if (parents.contains(parent))
					{
						String message = "parent domain " + parentDomainName + " is assigned twice as parent in current domain";
						LOGGER.warn(message);
						throw new InvalidParentDomainException(message);
					}
					parents.add(parent);
				}
				catch (UnknownDomainException e)
				{
					String message = "parent domain " + parentDomainName + " cannot be found";
					LOGGER.warn(message);
					throw new InvalidParentDomainException(message);
				}
			}
		}

		return parents;
	}
}
