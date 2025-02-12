package org.emau.icmvc.ganimed.ttp.psn.internal;

/*-
 * ###license-information-start###
 * gPAS - a Generic Pseudonym Administration Service
 * __
 * Copyright (C) 2013 - 2024 Independent Trusted Third Party of the University Medicine Greifswald
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

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.AccessTimeout;
import jakarta.ejb.EJB;
import jakarta.ejb.EJBException;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import jakarta.persistence.NoResultException;
import org.apache.commons.collections4.ListValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.emau.icmvc.ganimed.ttp.psn.alphabets.Alphabets;
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
import org.emau.icmvc.ganimed.ttp.psn.exceptions.InvalidUpdateInUseOperationException;
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
import org.emau.icmvc.ganimed.ttp.psn.model.Statistic;
import org.emau.icmvc.ganimed.ttp.psn.utils.StatisticKeys;

import static org.emau.icmvc.ganimed.ttp.psn.GPASServiceBase.throwIllegalArgumentExceptionForMultiplePsnContext;

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
		LOGGER.info("filling cache");
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
		LOGGER.info("cache filled in {} s", (System.currentTimeMillis() - time) / 1000);
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

	public List<String> createPseudonymsFor(String value, String domainName, int number)
			throws InvalidParameterException, UnknownDomainException, DomainIsFullException
	{
		domainRWL.readLock().lock();
		try
		{
			Domain domain = getDomain(domainName);
			assertThatNumberOfPSNsIsValid(domain.getConfig().isMultiPsnDomain(), number, value);
			assertThatValueNotYetExistsForSinglePsnDomain(value, domain);
			psnRWL.get(domainName).writeLock().lock();
			try
			{
				List<PSN> result = new ArrayList<>();
				validateValue(domain, value);
				while (result.size() < number)
				{
					result.add(createPSN(domain, value));
				}
				LOGGER.debug("created {} pseudonym(s) for value {} within domain {}", number, value, domainName);
				return result.stream().map(PSN::getPseudonym).toList();
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

	public ListValuedMap<String, String> createPseudonymsForList(Set<String> values, String domainName, int number) throws DomainIsFullException, UnknownDomainException, InvalidParameterException
	{
		domainRWL.readLock().lock();
		try
		{
			Domain domain = getDomain(domainName);
			boolean multiPsnDomain = domain.getConfig().isMultiPsnDomain();
			assertThatNumberOfPSNsIsValid(multiPsnDomain, number, null);
			// Even if we do not yet know whether writing is taking place at all,
			// we immediately request a write lock, as an upgrade is not permitted
			// and would lead to a deadlock. A later temporary release of the read lock
			// before requesting the write lock is also not an option, as this opens up
			// a logical second in which data could be changed by other threads.
			psnRWL.get(domainName).writeLock().lock();
			try
			{
				ListValuedMap<String, String> result = createListValuedMultiMapFor(values);
				values = new HashSet<>(values); // to ensure, it's modifiable
				Set<String> invalid = new HashSet<>();
				Set<String> multiple = new HashSet<>();
				Map<String, Long> counts = !multiPsnDomain ? countPseudonymsForListInternal(values, domain) : Map.of();
				for (String value : values)
				{
					try
					{
						validateValue(domain, value);
						if (!multiPsnDomain && counts.getOrDefault(value, 0L) > 0)
						{
							multiple.add(value);
							result.put(value, PSNErrorStrings.MULTIPLE_PSNS);
						}
					}
					catch (InvalidParameterException e)
					{
						invalid.add(value);
						result.put(value, PSNErrorStrings.INVALID_VALUE);
					}
				}
				values.removeAll(invalid);
				values.removeAll(multiple);
				if (!values.isEmpty())
				{
					LOGGER.debug("creating {} psns for each of {} values", number, values.size());
					for (int i = 0; i < number; i++)
					{
						result.putAll(createPseudonymForList(values, domain));
					}
				}
				return result;
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

	public List<String> getOrCreatePseudonymsFor(String value, String domainName, int minNumber)
			throws DomainIsFullException, InvalidParameterException, UnknownDomainException
	{
		domainRWL.readLock().lock();
		try
		{
			Domain domain = getDomain(domainName);
			List<PSN> result = new ArrayList<>();
			psnRWL.get(domainName).writeLock().lock();
			try
			{
				validateValue(domain, value); // do not put the check into the try-finally-block (see gpas#280)
				try
				{
					result.addAll(dao.getPSNObjects(value, domain));
					// b.t.w, for spsn domains the result never will contain more than one single PSN because of primary key constraints in the spsn table
					assertThatNumberOfPSNsIsValid(domain.getConfig().isMultiPsnDomain(), Math.max(result.size(), minNumber), value);
					LOGGER.debug("pseudonym(s) for value {} within domain {} found in db", value, domainName);
				}
				catch (UnknownValueException maybe)
				{
					LOGGER.debug("pseudonym(s) for value {} within domain {} not found - generate new", value, domainName);
				}
				int missing = minNumber - result.size();
				if (missing > 0)
				{
					while (result.size() < minNumber)
					{
						result.add(createPSN(domain, value));
					}
					LOGGER.debug("created {} pseudonym(s) for value {} within domain {}", missing, value, domainName);
				}
			}
			finally
			{
				psnRWL.get(domainName).writeLock().unlock();
			}
			return result.stream().map(PSN::getPseudonym).toList();
		}
		finally
		{
			domainRWL.readLock().unlock();
		}
	}

	public ListValuedMap<String, String> getOrCreatePseudonymsForList(Set<String> values, String domainName, int minNumber)
			throws DomainIsFullException, UnknownDomainException, InvalidParameterException
	{
		domainRWL.readLock().lock();
		try
		{
			ListValuedMap<String, String> result;
			GetPSNsResult tempResult;
			Domain domain = getDomain(domainName);
			assertThatNumberOfPSNsIsValid(domain.getConfig().isMultiPsnDomain(), minNumber, null);
			psnRWL.get(domainName).readLock().lock();
			try
			{
				tempResult = getPseudonymForListWithErrorSets(values, domain);
			}
			finally
			{
				psnRWL.get(domainName).readLock().unlock();
			}
			result = tempResult.valuesWithPSNs();
			LOGGER.debug("found psns for {} values; now creating psns for the {} missing values", result.size(), tempResult.valuesNotFound().size());
			HashSet<String> missing = new HashSet<>(values);
			missing.removeAll(tempResult.valuesInvalid);
			missing.removeIf(v -> result.containsKey(v) && result.get(v).size() >= minNumber);
			int countdown = minNumber; // to avoid error-induced endless loop

			if (!missing.isEmpty() && countdown > 0)
			{
				psnRWL.get(domainName).writeLock().lock();
				try
				{
					while (!missing.isEmpty() && countdown > 0)
					{
						// if multiPSNs are not allowed, we know, that minNumber is 1
						result.putAll(createPseudonymForList(missing, domain));
						missing.removeIf(v -> result.containsKey(v) && result.get(v).size() >= minNumber);
						countdown--;
					}
				}
				finally
				{
					psnRWL.get(domainName).writeLock().unlock();
				}
			}
			tempResult.valuesInvalid.forEach(v -> result.put(v, PSNErrorStrings.INVALID_VALUE));
			return result;
		}
		finally
		{
			domainRWL.readLock().unlock();
		}
	}

	public long countPseudonymsFor(String value, String domainName) throws UnknownDomainException
	{
		domainRWL.readLock().lock();
		try
		{
			Domain domain = getDomain(domainName);
			psnRWL.get(domainName).readLock().lock();
			try
			{
				return countPseudonymsForValueInternal(value, domain);
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

	private long countPseudonymsForValueInternal(String value, Domain domain)
	{
		try
		{
			return dao.getPSNObjects(value, domain).size();
		}
		catch (UnknownValueException e)
		{
			return 0;
		}
	}

	public Map<String, Long> countPseudonymsForList(Set<String> values, String domainName) throws UnknownDomainException
	{
		domainRWL.readLock().lock();
		try
		{
			Domain domain = getDomain(domainName);
			psnRWL.get(domainName).readLock().lock();
			try
			{
				return countPseudonymsForListInternal(values, domain);
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

	private Map<String, Long> countPseudonymsForListInternal(Set<String> values, Domain domain)
	{
		Map<String, Long> counts = dao.getPSNObjectsForValues(domain, values).stream()
				.collect(Collectors.groupingBy(PSN::getOriginalValue, Collectors.counting()));
		values.forEach(value -> counts.putIfAbsent(value, 0L));
		return counts;
	}

	public List<String> getPseudonymsFor(String value, String domainName) throws InvalidParameterException, UnknownDomainException, UnknownValueException
	{
		domainRWL.readLock().lock();
		try
		{
			Domain domain = getDomain(domainName);
			psnRWL.get(domainName).readLock().lock();
			try
			{
				validateValue(domain, value);
				return  dao.getPSNObjects(value, domain).stream().map(PSN::getPseudonym).toList();
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

	private void anonymiseValueInternal(String value, Domain domain)
			throws DBException, UnknownValueException, ValueIsAnonymisedException
	{
		if (isAnonym(value))
		{
			String message = "psn " + value + " is already anonymised";
			LOGGER.debug(message);
			throw new ValueIsAnonymisedException(message);
		}
		anonymisePSNs(dao.getPSNObjects(value, domain), domain.getName());
	}

	public void anonymiseValue(String value, String domainName)
			throws DBException, UnknownValueException, ValueIsAnonymisedException, UnknownDomainException
	{
		domainRWL.readLock().lock();
		try
		{
			Domain domain = getDomain(domainName);
			psnRWL.get(domainName).writeLock().lock();
			psnRWL.get(AnonymDomain.NAME).writeLock().lock();
			try
			{
				anonymiseValueInternal(value, domain);
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
	public Map<String, AnonymisationResult> anonymiseValues(Set<String> values, String domainName)
			throws DBException, UnknownDomainException
	{
		domainRWL.readLock().lock();
		try
		{
			Domain domain = getDomain(domainName);// to ensure domain exists
			psnRWL.get(domainName).writeLock().lock();
			psnRWL.get(AnonymDomain.NAME).writeLock().lock();
			try
			{
				Map<String, AnonymisationResult> result = new HashMap<>();
				for (String value : values)
				{
					try
					{
						anonymiseValueInternal(value, domain);
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

	public void anonymisePseudonym(String pseudonym, String domainName) throws InvalidPSNException, PSNNotFoundException, DBException, UnknownDomainException
	{
		domainRWL.readLock().lock();
		try
		{
			Domain domain = getDomain(domainName);
			psnRWL.get(domainName).writeLock().lock();
			psnRWL.get(AnonymDomain.NAME).writeLock().lock();
			try
			{
				// getPSNObjectForPseudonym(...) never returns null but will throw an exception when the psn is not found
				anonymisePSNs(List.of(dao.getPSNObjectForPseudonym(domain, pseudonym)), domainName);
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

	public Map<String, AnonymisationResult> anonymisePseudonyms(Set<String> psns, String domainName) throws UnknownDomainException
	{
		domainRWL.readLock().lock();
		try
		{
			getDomain(domainName); // to ensure domain exists
			psnRWL.get(domainName).writeLock().lock();
			psnRWL.get(AnonymDomain.NAME).writeLock().lock();
			try
			{
				Map<String, AnonymisationResult> result = new HashMap<>();
				for (String psn : psns)
				{
					try
					{
						anonymisePseudonym(psn, domainName);
						result.put(psn, AnonymisationResult.SUCCESS);
					}
					catch (DBException | InvalidPSNException e)
					{
						result.put(psn, AnonymisationResult.ERROR);
					}
					catch (PSNNotFoundException e)
					{
						result.put(psn, AnonymisationResult.NOT_FOUND);
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

	private void anonymisePSNs(List<PSN> psns, String domainName) throws DBException
	{
		for (PSN origEntity : psns)
		{
			try
			{
				String anonym = getOrCreatePseudonymsFor(domainName + AnonymDomain.DELIMITER + origEntity.getPseudonym(), AnonymDomain.NAME, 1).getFirst();
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
			Domain domain = getDomain(domainName);
			psnRWL.get(domainName).readLock().lock();
			try
			{
				PSN origEntity = dao.getPSNObjectForPseudonym(domain, psn);
				return isAnonym(origEntity.getOriginalValue());
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

	public void deleteValue(String value, String domainName) throws DeletionForbiddenException, UnknownDomainException, UnknownValueException, InvalidParameterException
	{
		domainRWL.readLock().lock();
		try
		{
			assertThatPSNsAreDeletable(domainName);
			psnRWL.get(domainName).writeLock().lock();
			try
			{
				deleteValueInternal(value, getDomain(domainName));
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

	public Map<String, DeletionResult> deleteValues(Set<String> values, String domainName) throws DeletionForbiddenException, UnknownDomainException
	{
		int countSuccess = 0;
		int countFailures = 0;
		Map<String, DeletionResult> result = HashMap.newHashMap(values.size());
		domainRWL.readLock().lock();
		try
		{
			assertThatPSNsAreDeletable(domainName);
			psnRWL.get(domainName).writeLock().lock();
			try
			{
				for (String value : values)
				{
					try
					{
						deleteValueInternal(value, getDomain(domainName));
						result.put(value, DeletionResult.SUCCESS);
						countSuccess++;
					}
					catch (InvalidParameterException e)
					{
						String message = "value " + value + " refers to multiple psns in domain " + domainName;
						LOGGER.info(message);
						result.put(value, DeletionResult.ERROR);
						countFailures++;
					}
					catch (UnknownValueException e)
					{
						String message = "value " + value + " not found for domain " + domainName;
						LOGGER.info(message);
						result.put(value, DeletionResult.NOT_FOUND);
						countFailures++;
					}
				}
			}
			finally
			{
				psnRWL.get(domainName).writeLock().unlock();
			}
			LOGGER.debug("{} value-pseudonym-pair(s) removed from domain {}", countSuccess, domainName);
			if (countFailures > 0)
			{
				LOGGER.info("{} failure(s) while removing value-pseudonym-pairs from domain {}", countFailures, domainName);
			}
			return result;
		}
		finally
		{
			domainRWL.readLock().unlock();
		}
	}

	public void deletePseudonym(String pseudonym, String domainName)
			throws UnknownDomainException, DeletionForbiddenException, InvalidParameterException, InvalidPSNException, UnknownValueException, PSNNotFoundException
	{
		domainRWL.readLock().lock();
		try
		{
			assertThatPSNsAreDeletable(domainName);
			psnRWL.get(domainName).writeLock().lock();
			try
			{
				deletePSN(dao.getPSNObjectForPseudonym(getDomain(domainName), pseudonym));
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

	public Map<String, DeletionResult> deletePseudonyms(Set<String> psns, String domainName) throws UnknownDomainException, DeletionForbiddenException
	{
		int countSuccess = 0;
		int countFailures = 0;
		Map<String, DeletionResult> result = HashMap.newHashMap(psns.size());
		domainRWL.readLock().lock();
		try
		{
			Domain domain = getDomain(domainName);
			assertThatPSNsAreDeletable(domainName);
			psnRWL.get(domainName).writeLock().lock();
			try
			{
				for (String pseudonym : psns)
				{
					try
					{
						deletePSN(dao.getPSNObjectForPseudonym(domain, pseudonym));
						result.put(pseudonym, DeletionResult.SUCCESS);
						countSuccess++;
					}
					catch (InvalidParameterException | UnknownValueException e) // es macht probleme
					{
						String message = "pseudonym " + pseudonym + " in domain " + domainName + " caused problems with cascaded deletion of child pseudonyms";
						LOGGER.info(message);
						result.put(pseudonym, DeletionResult.ERROR);
						countFailures++;
					}
					catch (PSNNotFoundException | InvalidPSNException e)
					{
						String message = "pseudonym " + pseudonym + " not found for domain " + domainName;
						LOGGER.info(message);
						result.put(pseudonym, DeletionResult.NOT_FOUND);
						countFailures++;
					}
				}
			}
			finally
			{
				psnRWL.get(domainName).writeLock().unlock();
			}
			LOGGER.debug("{} value-pseudonym-pair(s) removed from domain {}", countSuccess, domainName);
			if (countFailures > 0)
			{
				LOGGER.info("{} failure(s) while removing value-pseudonym-pairs from domain {}", countFailures, domainName);
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
			Domain domain = getDomain(domainName);
			psnRWL.get(domainName).readLock().lock();
			try
			{
				validatePSN(psn, domainName);
				String result = dao.getPSNObjectForPseudonym(domain, psn).getOriginalValue();
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

	public Map<String, String> getValueForList(Set<String> psns, String domainName) throws UnknownDomainException
	{
		HashMap<String, String> result = HashMap.newHashMap(psns.size());
		Set<String> temp = new HashSet<>();
		domainRWL.readLock().lock();
		try
		{
			Domain domain = getDomain(domainName);
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
				LOGGER.debug("found {} valuesInvalid requested psns", temp.size());
				if (!psns.isEmpty())
				{
					List<PSN> foundPSNs = dao.getPSNObjectsForPSNs(domain, psns);
					int anoCounter = 0;
					for (PSN found : foundPSNs)
					{
						temp.add(found.getPseudonym());
						if (isAnonym(found.getOriginalValue()))
						{
							result.put(found.getPseudonym(), PSNErrorStrings.VALUE_IS_ANONYMISED);
							anoCounter++;
						}
						else
						{
							result.put(found.getPseudonym(), found.getOriginalValue());
						}
					}
					LOGGER.debug("found {} anonymised requested psns", anoCounter);
					for (String psn : psns)
					{
						if (!temp.contains(psn))
						{
							result.put(psn, PSNErrorStrings.PSN_NOT_FOUND);
						}
					}
					LOGGER.debug("couldn't find {} requested psns", psns.size() - temp.size());
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

	public ListValuedMap<String, String> getPseudonymsForList(Set<String> values, String domainName) throws UnknownDomainException
	{
		ListValuedMap<String, String> result = createListValuedMultiMapFor(values);
		Set<String> valuesWithPsn = new HashSet<>();
		domainRWL.readLock().lock();
		try
		{
			Domain domain = getDomain(domainName);
			psnRWL.get(domainName).readLock().lock();
			try
			{
				List<PSN> foundPSNs = dao.getPSNObjectsForValues(domain, values);
				int anoCounter = 0;
				for (PSN found : foundPSNs)
				{
					String value = found.getOriginalValue();
					valuesWithPsn.add(value);
					if (isAnonym(value))
					{
						result.put(PSNErrorStrings.VALUE_IS_ANONYMISED, found.getPseudonym());
						anoCounter++;
					}
					else
					{
						result.put(value, found.getPseudonym());
					}
				}
				LOGGER.debug("found {} anonymised requested values", anoCounter);
				for (String value : values)
				{
					if (!valuesWithPsn.contains(value))
					{
						try
						{
							validateValue(domain, value);
							result.put(value, PSNErrorStrings.VALUE_NOT_FOUND);
						}
						catch (InvalidParameterException e)
						{
							result.put(value, PSNErrorStrings.INVALID_VALUE);
						}
					}
				}
				LOGGER.debug("couldn't find {} requested values", values.size() - valuesWithPsn.size());
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

	public ListValuedMap<String, String> getPseudonymsForValuePrefix(String valuePrefix, String domainName) throws UnknownDomainException
	{
		domainRWL.readLock().lock();
		try
		{
			Domain domain = getDomain(domainName);
			psnRWL.get(domainName).readLock().lock();
			try
			{
				List<PSN> psnList = dao.getAllPSNObjectsForValuePrefix(domain, valuePrefix);
				ListValuedMap<String, String> result = createListValuedMultiMapFor(psnList);
				psnList.forEach(psn -> result.put(psn.getOriginalValue(), psn.getPseudonym()));
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
				validateValue(domain, value);
				if (dao.insertValuePseudonymPair(value, pseudonym, domain, domain.getConfig().isMultiPsnDomain()))
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

	public List<InsertPairExceptionDTO> insertValuePseudonymPairs(List<Entry<String, String>> pairs, String domainName) throws UnknownDomainException
	{
		LOGGER.debug("insertValuePseudonymPairs for {} value-pseudonym pairs within domain {}", pairs.size(), domainName);
		List<InsertPairExceptionDTO> result = new ArrayList<>();
		domainRWL.readLock().lock();
		try
		{
			Domain domain = getDomain(domainName);
			psnRWL.get(domainName).writeLock().lock();
			try
			{
				for (Entry<String, String> entry : pairs)
				{
					try
					{
						validatePSN(entry.getValue(), domainName);
						validateValue(domain, entry.getKey());
						// TODO optimieren: dao.insertValuePseudonymPairs?
						if (dao.insertValuePseudonymPair(entry.getKey(), entry.getValue(), domain, domain.getConfig().isMultiPsnDomain()))
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
						result.add(new InsertPairExceptionDTO(e));
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
		LOGGER.debug("getPSNTreeForPSN for {} within domain {}", psn, domainName);
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
				while (!done && currentProject.getParents() != null && !currentProject.getParents().isEmpty())
				{
					String tempPSN = getValueFor(currentPSN, currentProject.getName());
					Domain tempProject = getDomain(currentProject.getParents().getFirst().getName());
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
				LOGGER.debug("psn tree created");
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

	public PSNNetDTO getPSNNetFor(String valueOrPSN)
	{
		LOGGER.debug("getPSNNetFor for value or psn {}", valueOrPSN);
		PSNNetDTO net = new PSNNetDTO();
		domainRWL.readLock().lock();
		try
		{
			Set<PSN> psns = collectPSNsConnectedTo(valueOrPSN);
			if (!psns.isEmpty())
			{
				Map<PSNDTO, PSNNetNodeDTO> allNodes = createAllNodes(psns);
				Set<PSNNetNodeDTO> nodesToProcess = new HashSet<>(allNodes.values());
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
			LOGGER.debug("psn net {} created", net);
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
			Cache.testConfig(domain, true);
			dao.addDomain(domain, parents);
			addDomainToCaches(domain);
		}
		finally
		{
			domainRWL.writeLock().unlock();
		}
	}

	public void updateDomain(DomainInDTO domainDTO) throws DomainInUseException, InvalidAlphabetException, InvalidCheckDigitClassException,
			InvalidGeneratorException, InvalidParameterException, UnknownDomainException, InvalidParentDomainException, InvalidUpdateInUseOperationException
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
				updateDomainWithParents(domain, domainDTO, false);
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

	public void updateDomainInUse(String domainName, String label, String comment, List<String> parentDomainNames, boolean sendNotificationsWeb, boolean psnsDeletable)
			throws UnknownDomainException, InvalidParameterException, InvalidGeneratorException, InvalidAlphabetException, InvalidParentDomainException, InvalidCheckDigitClassException,
			InvalidUpdateInUseOperationException
	{
		domainRWL.writeLock().lock();
		try
		{
			Domain domain = getDomain(domainName);
			psnRWL.get(domainName).writeLock().lock();
			try
			{
				DomainInDTO domainDTO = createDomainDTO(domain);
				domainDTO.setParentDomainNames(parentDomainNames);
				domainDTO.setLabel(label);
				domainDTO.setComment(comment);
				domainDTO.getConfig().setSendNotificationsWeb(sendNotificationsWeb);
				domainDTO.getConfig().setPsnsDeletable(psnsDeletable);
				updateDomainWithParents(domain, domainDTO, true);
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

	private void updateDomainWithParents(Domain domain, DomainInDTO domainDTO, boolean updateInUse)
			throws InvalidParameterException, InvalidParentDomainException, InvalidGeneratorException, InvalidAlphabetException, InvalidCheckDigitClassException, InvalidUpdateInUseOperationException
	{
		List<Domain> parents = checkParentDomains(domainDTO);
		Domain testProject = new Domain(domainDTO, parents);
		Cache.testConfig(testProject, false);
		List<Domain> oldParents = domain.getParents();
		if (oldParents != null)
		{
			// Check that no old parents are removed, if the updated domain is in use and validateValuesViaParents is enabled
			if (updateInUse && !ValidateViaParents.OFF.equals(domain.getConfig().getValidateValuesViaParents()))
			{
				if (!new HashSet<>(parents).containsAll(oldParents))
				{
					String message = "Can't remove parents from domain in use ("+ domain.getName() + "), when validateValuesViaParents is enabled (" + domain.getConfig().getValidateValuesViaParents().name() + ")";
					LOGGER.warn(message);
					throw new InvalidUpdateInUseOperationException(message);
				}
			}
			
			for (Domain parent : oldParents)
			{
				parent.getChildren().remove(domain);
			}
		}
		if (updateInUse)
		{
			domain.updateInUse(domainDTO.getLabel(), domainDTO.getComment(), parents, domainDTO.getConfig().isSendNotificationsWeb(), domainDTO.getConfig().isPsnsDeletable());
		}
		else
		{
			domain.update(domainDTO, parents);
			updateDomainInCaches(domain);
		}

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
			if (!domain.getChildren().isEmpty())
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
				dao.deleteAllPSNForDomain(domain);
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
			return createDomainDTO(getDomain(domainName));
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
			Domain domain = getDomain(domainName);
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
							List<PSN> psns = dao.getPSNObjectsForDomainPaginated(domain, PAGE_SIZE_FOR_CACHE_INIT, lastOrigValue);
							for (PSN psn : psns)
							{
								result.add(psn.toPSNDTO());
							}
							lastOrigValue = psns.getLast().getOriginalValue();
						}
					}
				}
				else
				{
					for (PSN psn : dao.getPSNObjectsForDomain(domain))
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
			List<Domain> domains = new ArrayList<>();
			for (String domainName : domainNames)
			{
				domains.add(getDomain(domainName));
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
					return dao.countPSNObjectsForDomains(domains, config);
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
			List<Domain> domains = new ArrayList<>();
			for (String domainName : domainNames)
			{
				domains.add(getDomain(domainName));
				psnRWL.get(domainName).readLock().lock();
			}
			try
			{
				for (PSN psn : dao.getPSNObjectsForDomains(domains, config))
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

	public List<DomainOutDTO> getDomainsForPrefix(String prefix)
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

	public boolean isMultiPsnDomain(String domainName) throws UnknownDomainException
	{
		domainRWL.readLock().lock();
		try
		{
			Domain domain = getDomain(domainName);
			return domain.getConfig().isMultiPsnDomain();
		}
		finally
		{
			domainRWL.readLock().unlock();
		}
	}

	// --------------------- statistic ---------------------

	public StatisticDTO getFirstStats()
	{
		statisticRWL.readLock().lock();
		StatisticDTO result;
		try
		{
			Statistic stat = dao.getFirstStat();
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
	
	public StatisticDTO getLatestStats()
	{
		statisticRWL.readLock().lock();
		StatisticDTO result;
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

	public List<StatisticDTO> getStatsFromTo(Date from, Date to)
	{
		statisticRWL.readLock().lock();
		try
		{
			List<StatisticDTO> result = new ArrayList<>();
			List<Statistic> queryResult = dao.getStatsFromTo(from, to);
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
					result.getMappedStatValue().put(StatisticKeys.UTILIZATION_PER_DOMAIN + entry.getKey(), (long) getPercentPSNsUsed(entry.getKey()));
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
		LOGGER.debug("found {} psns for domain {}, loading them into cache now", psnCount, domain.getName());
		String lastPseudonym = null;
		for (int i = 0; i * PAGE_SIZE_FOR_CACHE_INIT < psnCount; i++)
		{
			int nextPageSize = (int) ((i + 1) * PAGE_SIZE_FOR_CACHE_INIT < psnCount ? PAGE_SIZE_FOR_CACHE_INIT
					: psnCount - i * PAGE_SIZE_FOR_CACHE_INIT);
			if (nextPageSize > 0)
			{
				List<String> psns = dao.getPseudonymsForDomainPaginated(domain, PAGE_SIZE_FOR_CACHE_INIT, lastPseudonym);
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
				lastPseudonym = psns.getLast();
			}
		}
		if (result.getCount() != psnCount)
		{
			LOGGER.fatal("unexpected result of create domain cache for domain {} - count within cache ({}) is not equal count of psns within db for that domain ({})", domain.getName(),
					result.getCount(), psnCount);
		}
		LOGGER.debug("cache initialised for domain {}", domain.getName());
		return result;
	}

	private void addDomainToCaches(Domain domain) throws InvalidCheckDigitClassException, InvalidAlphabetException, InvalidGeneratorException
	{
		domainRWL.writeLock().lock();
		try
		{
			domainCache.put(domain.getName(), domain);
			long count = dao.countAnonymsForDomain(domain);
			anoCounterCache.put(domain.getName(), count);
			count = dao.countPseudonymsForDomain(domain);
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

	private void updateDomainInCaches(Domain domain) throws InvalidAlphabetException, InvalidGeneratorException, InvalidCheckDigitClassException
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

	private static void testConfig(Domain domain, boolean newDomain) throws InvalidAlphabetException, InvalidGeneratorException, InvalidParameterException, InvalidCheckDigitClassException
	{
		CheckDigits checkDigits = createGenerator(domain).getCheckDigits();
		if (!ValidateViaParents.OFF.equals(domain.getConfig().getValidateValuesViaParents()) && domain.getParents().isEmpty())
		{
			throw new InvalidParameterException("isValidateValuesViaParents is true for domain " + domain.getName() + " but there are no parents set");
		}
		if (newDomain && checkDigits.isDeprecated())
		{
			throw new InvalidCheckDigitClassException(checkDigits + " is deprecated and not allowed for new domains.");
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
			return domain.toDTO(numberOfPSNs, numberOfAnos, generatorCache.get(domain.getName()).isUseCache(), getPercentPSNsUsed(domain.getName()));
		}
		finally
		{
			psnRWL.get(domain.getName()).readLock().unlock();
		}
	}
	
	private short getPercentPSNsUsed(String domainName)
	{
		long maxPSNs = generatorCache.get(domainName).getMaxNumberForPSN();
		return maxPSNs != -1L ? (short) (psnCounterCache.get(domainName) * 100L / maxPSNs) : (short) -1;
	}

	private boolean existsPseudonym(String domainName, String pseudonym, long number)
	{
		if (generatorCache.get(domainName).isUseCache())
		{
			try
			{
				number = number == -1 ? generatorCache.get(domainName).getPosNumberForPSN(pseudonym) : number;
				return psnCache.get(domainName).isPosSet(number);
			}
			catch (ArithmeticException | CharNotInAlphabetException e)
			{
				// https://logging.apache.org/log4j/2.x/manual/api.html#substituting-parameters
				LOGGER.fatal("program error - created an valuesInvalid pseudonym: {}  for domain {}", pseudonym, domainName, e);
				return true;
			}
		}
		else
		{
			domainRWL.readLock().lock();
			try
			{
				dao.getPSNObjectForPseudonym(getDomain(domainName), pseudonym);
			}
			catch (PSNNotFoundException | UnknownDomainException e)
			{
				return false;
			}
			catch (InvalidPSNException e)
			{
				// https://logging.apache.org/log4j/2.x/manual/api.html#substituting-parameters
				LOGGER.fatal("program error - created an invalid pseudonym: {} for domain {}", pseudonym, domainName, e);
			}
			finally
			{
				domainRWL.readLock().unlock();
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

	private void validateValue(Domain domain, String value) throws InvalidParameterException, UnknownDomainException
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

	private void deleteValueInternal(String value, Domain domain) throws UnknownDomainException, UnknownValueException, InvalidParameterException
	{
		for (PSN psn : dao.getPSNObjects(value, domain))
		{
			deletePSN(psn);
		}
	}

	private void deletePSN(PSN psn) throws UnknownDomainException, UnknownValueException, InvalidParameterException
	{
		String domainName = psn.getDomain().getName();
		String pseudonym = psn.getPseudonym();
		String value = psn.getOriginalValue();
		dao.deletePSN(psn);
		if (generatorCache.get(domainName).isUseCache())
		{
			try
			{
				psnCache.get(domainName).unsetPos(generatorCache.get(domainName).getPosNumberForPSN(pseudonym));
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
						deleteValueInternal(pseudonym, child);
					}
				}
				finally
				{
					psnRWL.get(child.getName()).writeLock().unlock();
				}
			}
		}
	}

	private PSN createPSN(Domain domain, String value) throws DomainIsFullException, InvalidParameterException
	{
		Generator generator = generatorCache.get(domain.getName());
		if (psnCounterCache.get(domain.getName()) + 1 >= generator.getMaxNumberForPSN())
		{
			String message = "can't create any more pseudonyms for domain " + domain.getName();
			LOGGER.error(message);
			throw new DomainIsFullException(message);
		}
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
				LOGGER.trace("duplicate pseudonym generated - attemp {} of {}", countCollisions, MAX_ATTEMPS_BEFORE_SEARCH_NEXT_FREE_PSN);
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
		PSN result = PSN.create(domain, value, pseudonym);
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
		ListValuedMap<String, String> psns = createListValuedMultiMapFor(values);
		dao.getPSNObjectsForValues(domain, values).forEach(psn -> psns.put(psn.getOriginalValue(), psn.getPseudonym()));
		values = values.stream().filter(v -> !psns.containsKey(v)).collect(Collectors.toSet());
		Set<String> valuesNotFound = HashSet.newHashSet(values.size());
		Set<String> valuesInvalid = HashSet.newHashSet(values.size());
		for (String value : values)
		{
			try
			{
				validateValue(domain, value);
				valuesNotFound.add(value);
			}
			catch (InvalidParameterException e)
			{
				valuesInvalid.add(value);
			}
		}
		return new GetPSNsResult(psns, valuesNotFound, valuesInvalid);
	}

	private static ListValuedMap<String, String> createListValuedMultiMapFor(Collection<?> values)
	{
		return new ArrayListValuedHashMap<>((int) (values.size() * 1.5), 1);
	}

	private Map<String, String> createPseudonymForList(Set<String> values, Domain domain) throws DomainIsFullException, InvalidParameterException
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
						LOGGER.trace("duplicate pseudonym generated - attemp {} of " + MAX_ATTEMPS_BEFORE_SEARCH_NEXT_FREE_PSN, countCollisions);
						countCollisions++;
						// TODO sollte zu oft ein schon vorhandener psn generiert worden sein ->
						// deterministische suche
						if (countCollisions > MAX_ATTEMPS_BEFORE_SEARCH_NEXT_FREE_PSN)
						{
							// TODO Wenn man hier her kommt wird trotzdem das MPSN gespeichert und
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
			generatedPSNs.removeAll(dao.getExistingPSNs(domain, generatedPSNs));
			while (generatedPSNs.size() < values.size())
			{
				Set<String> generatedPSNs2 = generatePossiblePSNs(domain.getName(), Math.round((values.size() - generatedPSNs.size()) * 1.1 + 100));
				generatedPSNs2.removeAll(dao.getExistingPSNs(domain, generatedPSNs2));
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

	private Set<String> generatePossiblePSNs(String domainName, long size) throws InvalidParameterException
	{
		Set<String> result = HashSet.newHashSet((int) size);
		Generator generator = generatorCache.get(domainName);
		for (long i = 0; i < size; i++)
		{
			result.add(generator.getNewPseudonym(-1L));
		}
		return result;
	}

	private static Generator createGenerator(Domain domain) throws InvalidAlphabetException, InvalidGeneratorException, InvalidCheckDigitClassException
	{
		LOGGER.debug("createGenerator for domain {} with check digit class {} and alphabet {}",
				domain.getName(), domain.getGeneratorClass(), domain.getAlphabet());
		Alphabet alphabet = Alphabets.createAlphabet(domain.getAlphabet());
		LOGGER.debug("creating generator");

		try
		{
			LOGGER.trace("create check digit class ({})", domain.getGeneratorClass());
			return new Generator(domain.getGeneratorClass(), alphabet, domain.getConfig(), domain.getName());
		}
		catch (InvalidCheckDigitClassException e)
		{
			LOGGER.fatal(e.getMessage(), e);
			throw e;
		}
	}

	// --------------------- private fuer tree / net ---------------------

	/**
	 * Recursively traverse deeper into domains until all domains related to the passed
	 * originalValue and domain are found
	 *
	 * @return a PSNTreeNode containing child nodes, child-child nodes and so on
	 */
	private PSNTreeDTO createPSNTree(String value, String psn, Domain domain, int i)
	{
		PSNTreeDTO currentNode = new PSNTreeDTO(domain.getName(), value, psn, i);
		for (Domain child : domain.getChildren())
		{
			try
			{
				for (PSN next : dao.getPSNObjects(psn, domain))
				{
					currentNode.addChild(createPSNTree(psn, next.getPseudonym(), child, i + 1));
				}
			}
			catch (UnknownValueException e)
			{
				LOGGER.trace("ignoring exception: no pseudonym available in domain: {} for value: {}", child.getName(), value);
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
				for (DomainOutDTO domainDTO : domains)
				{
					domainRWL.readLock().lock();
					try
					{
						Domain domain = getDomain(domainDTO.getName());
						try
						{

							for (PSN psn : dao.getPSNObjects(value, domain))
							{
								String pseudonym = psn.getPseudonym();
								if (!allValues.contains(pseudonym) && !currentValues.contains(pseudonym))
								{
									newValues.add(pseudonym);
								}
								result.add(psn);
							}
						}
						catch (UnknownValueException e)
						{
							LOGGER.debug("ignoring exception: no pseudonym available in domain: {} for value: {}", domainDTO.getName(), value);
						}
						try
						{
							PSN psn = dao.getPSNObjectForPseudonym(domain, value);
							String originalValue = psn.getOriginalValue();
							if (!allValues.contains(originalValue) && !currentValues.contains(originalValue))
							{
								newValues.add(originalValue);
							}
							result.add(psn);
						}
						catch (PSNNotFoundException | InvalidPSNException e)
						{
							// empty on purpose
						}
					}
					catch (UnknownDomainException e)
					{
						LOGGER.debug("ignoring exception: domain {} does not exist for value: {}", domainDTO.getName(), value);
					}
					finally
					{
						domainRWL.readLock().unlock();
					}
				}
			}
			allValues.addAll(currentValues);
			currentValues = newValues;
			newValues = new HashSet<>();
		}
		return result;
	}

	private Map<PSNDTO, PSNNetNodeDTO> createAllNodes(Set<PSN> psns)
	{
		Map<PSNDTO, PSNNetNodeDTO> result = new HashMap<>();
		for (PSN psn : psns)
		{
			result.put(psn.toPSNDTO(), new PSNNetNodeDTO(psn.getDomainName(), psn.getOriginalValue(), psn.getPseudonym(), -1));
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

	private Set<PSNNetNodeDTO> buildPSNNet(PSNNetDTO net, Map<PSNDTO, PSNNetNodeDTO> allNodes, Set<PSNNetNodeDTO> roots)
	{
		Set<PSNNetNodeDTO> processedNodes = new HashSet<>();
		for (PSNNetNodeDTO root : roots)
		{
			PSNNetNodeDTO child = allNodes.get(new PSNDTO(root.getDomainName(), root.getOriginalValue(), root.getPseudonym()));
			child.setLevel(0);
			net.getRoot().addChild(processNetNode(processedNodes, allNodes, new HashSet<>(Collections.singleton(child)), child, 0));
		}
		return processedNodes;
	}

	private PSNNetNodeDTO processNetNode(Set<PSNNetNodeDTO> processedNodes, Map<PSNDTO, PSNNetNodeDTO> allNodes, Set<PSNNetNodeDTO> currentBranch,
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

	public void assertThatPSNsAreDeletable(String domainName) throws UnknownDomainException, DeletionForbiddenException
	{
		if (!arePSNDeletable(domainName))
		{
			String message = "the domain " + domainName + " does not allow deletion of value-pseudonym-pairs";
			LOGGER.warn(message);
			throw new DeletionForbiddenException(message);
		}
	}

	private void assertThatNumberOfPSNsIsValid(boolean allowMultiPSNs, int numberOfPSNs, String value)
	{
		if (!allowMultiPSNs && numberOfPSNs > 1)
		{
			throwIllegalArgumentExceptionForMultiplePsnContext(value);
		}
		if (numberOfPSNs < 0)
		{
			throw new IllegalArgumentException("Number of PSNs must not be less than 0");
		}
	}

	private void assertThatValueNotYetExistsForSinglePsnDomain(String value, Domain domain)
	{
		if (!domain.getConfig().isMultiPsnDomain() && countPseudonymsForValueInternal(value, domain) > 0)
		{
			// b.t.w, count > 1 will never happen because of primary key constraints in the spsn table
			throwIllegalArgumentExceptionForMultiplePsnContext(value);
		}
	}

	record GetPSNsResult(ListValuedMap<String, String> valuesWithPSNs, Set<String> valuesNotFound, Set<String> valuesInvalid)
	{
		@Override
		public String toString()
		{
			return "GetPSNsResult [valuesWithPSNs=" + valuesWithPSNs + ", valuesNotFound=" + valuesNotFound + ", valuesInvalid=" + valuesInvalid + "]";
		}
	}
}
