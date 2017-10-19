package org.emau.icmvc.ganimed.ttp.psn;

/*
 * ###license-information-start###
 * gPAS - a Generic Pseudonym Administration Service
 * __
 * Copyright (C) 2013 - 2017 The MOSAIC Project - Institut fuer Community Medicine der
 * 							Universitaetsmedizin Greifswald - mosaic-projekt@uni-greifswald.de
 * 							concept and implementation
 * 							l. geidel
 * 							web client
 * 							g. weiher
 * 							a. blumentritt
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import javax.ejb.EJB;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.log4j.Logger;
import org.emau.icmvc.ganimed.ttp.psn.dto.HashMapWrapper;
import org.emau.icmvc.ganimed.ttp.psn.dto.PSNTreeDTO;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.CharNotInAlphabetException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.DBException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.DeletionForbiddenException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.InvalidAlphabetException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.InvalidCheckDigitClassException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.InvalidGeneratorException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.InvalidPSNException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.PSNNotFoundException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.UnknownDomainException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.UnknownValueException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.ValueIsAnonymisedException;
import org.emau.icmvc.ganimed.ttp.psn.generator.Generator;
import org.emau.icmvc.ganimed.ttp.psn.generator.GeneratorProperties;
import org.emau.icmvc.ganimed.ttp.psn.internal.AnonymDomain;
import org.emau.icmvc.ganimed.ttp.psn.internal.PSNTreeNode;
import org.emau.icmvc.ganimed.ttp.psn.model.PSN;
import org.emau.icmvc.ganimed.ttp.psn.model.PSNKey;
import org.emau.icmvc.ganimed.ttp.psn.model.PSNKey_;
import org.emau.icmvc.ganimed.ttp.psn.model.PSNProject;
import org.emau.icmvc.ganimed.ttp.psn.model.PSN_;

/**
 * webservice for pseudonyms
 * 
 * @author geidell
 * 
 */
@WebService(name = "gpasService")
@SOAPBinding(style = SOAPBinding.Style.RPC)
@Stateless
@Remote(PSNManager.class)
@PersistenceContext(name = "psn")
public class PSNManagerBean implements PSNManager {

	private static final String PSN_NOT_FOUND = "*** PSN NOT FOUND ***";
	private static final String VALUE_NOT_FOUND = "*** VALUE NOT FOUND ***";
	private static final String VALUE_IS_ANONYMISED = "*** VALUE IS ANONYMISED ***";
	private static final Logger logger = Logger.getLogger(PSNManagerBean.class);
	@PersistenceContext
	private EntityManager em;
	private static final Object emSynchronizerDummy = new Object();
	@EJB
	private DomainManagerLocal domainManager;
	private static final int MAX_ATTEMPS_BEFORE_RESEED = 10;
	private static final int MAX_RESEEDS = 5;

	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public String getOrCreatePseudonymFor(String value, String domain) throws DBException, InvalidGeneratorException, UnknownDomainException {
		PSN result = null;
		if (logger.isDebugEnabled()) {
			logger.debug("pseudonym requested for value " + value + " within domain " + domain);
		}
		try {
			result = getPSN(value, domain);
			if (logger.isDebugEnabled()) {
				logger.debug("pseudonym for value '" + value + "' within domain '" + domain + "' found in db");
			}
		} catch (UnknownValueException maybe) {
			if (logger.isDebugEnabled()) {
				logger.debug("pseudonym for value " + value + " within domain " + domain + " not found - generate new");
			}
			PSNProject parent = getPSNProject(domain);
			result = createPSN(parent, value, null);
		}
		return result.getPseudonym();
	}

	private PSNProject getPSNProject(String domain) throws UnknownDomainException {
		PSNProject parent = em.find(PSNProject.class, domain);
		if (parent == null) {
			String message = "psn-project for domain '" + domain + "' not found";
			logger.error(message);
			throw new UnknownDomainException(message);
		}
		return parent;
	}

	private Generator getGeneratorFor(String domain) throws InvalidGeneratorException, UnknownDomainException {
		try {
			return domainManager.getGeneratorFor(domain);
		} catch (InvalidAlphabetException | InvalidCheckDigitClassException e) {
			String message = "exception while instanciating generator for domain '" + domain + "'";
			logger.fatal(message, e);
			throw new InvalidGeneratorException(message, e);
		}
	}

	/**
	 * 
	 * @param parent
	 * @param value
	 * @param existingPseudonyms
	 *            can be null - check is then performed against the db
	 * @return
	 * @throws DBException
	 * @throws InvalidGeneratorException
	 * @throws UnknownDomainException
	 */
	private PSN createPSN(PSNProject parent, String value, HashSet<String> existingPseudonyms)
			throws DBException, InvalidGeneratorException, UnknownDomainException {
		PSN result;
		// countCollisions - zaehler fuer kollisionen generiertes pseudonym -
		// vorhandene pseudonyme (domain+pseudonym muss unique sein)
		int countCollisions = 0;
		// countReseeds - zaehler fuer generator.randomize() - sonst droht eine
		// endlosschleife
		int countReseeds = 0;
		boolean done = false;
		String pseudonym;
		synchronized (emSynchronizerDummy) {
			do {
				pseudonym = getGeneratorFor(parent.getDomain()).getNewPseudonym();
				if ((existingPseudonyms != null && existingPseudonyms.contains(pseudonym))
						|| (existingPseudonyms == null && existsPseudonym(parent.getDomain(), pseudonym))) {
					if (logger.isDebugEnabled()) {
						logger.debug("duplicate pseudonym generated - attemp " + countCollisions + " of " + MAX_ATTEMPS_BEFORE_RESEED);
					}
					countCollisions++;
					// sollte zu oft ein schon vorhandener psn generiert worden
					// sein, den generator neu initialisieren
					if (countCollisions > MAX_ATTEMPS_BEFORE_RESEED) {
						countReseeds++;
						if (countReseeds > MAX_RESEEDS) {
							// der generator wurde mehrfach neu initialisiert,
							// abbruch
							String message = "generator reseeded " + MAX_RESEEDS + " times but the generated pseudonym is still duplicate";
							logger.error(message);
							throw new DBException(message);
						}
						countCollisions = 0;
						if (logger.isInfoEnabled()) {
							logger.info("max attemps (" + MAX_ATTEMPS_BEFORE_RESEED + ") for generating a pseudonym expended - reseed the generator");
						}
						getGeneratorFor(parent.getDomain()).randomize();
					}
				} else {
					done = true;
				}
			} while (!done);
			result = new PSN(parent, value, pseudonym);
			em.persist(result);
			parent.getPsnList().add(result);
		}
		return result;
	}

	private boolean existsPseudonym(String domain, String pseudonym) {
		return !getPSNObjects(domain, pseudonym).isEmpty();
	}

	private List<PSN> getPSNObjects(String domain, String pseudonym) {
		CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
		CriteriaQuery<PSN> criteriaQuery = criteriaBuilder.createQuery(PSN.class);
		Root<PSN> root = criteriaQuery.from(PSN.class);
		Predicate predicate = criteriaBuilder.and(criteriaBuilder.equal(root.get(PSN_.pseudonym), pseudonym),
				criteriaBuilder.equal(root.get(PSN_.key).get(PSNKey_.domain), domain));
		criteriaQuery.select(root).where(predicate);
		return em.createQuery(criteriaQuery).getResultList();
	}

	private List<PSN> getAllPSNObjects(String domain) {
		if (logger.isDebugEnabled()) {
			logger.debug("get all entries for domain " + domain);
		}
		CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
		CriteriaQuery<PSN> criteriaQuery = criteriaBuilder.createQuery(PSN.class);
		Root<PSN> root = criteriaQuery.from(PSN.class);
		Predicate predicate = criteriaBuilder.equal(root.get(PSN_.key).get(PSNKey_.domain), domain);
		criteriaQuery.select(root).where(predicate);
		List<PSN> result = em.createQuery(criteriaQuery).getResultList();
		if (logger.isDebugEnabled()) {
			logger.debug("found " + result.size() + " entries for domain " + domain);
		}
		return result;
	}

	private List<PSN> getAllPSNObjectsForValuePrefix(String domain, String valuePrefix) {
		if (logger.isDebugEnabled()) {
			logger.debug("get all entries for domain '" + domain + "' where the value starts with '" + valuePrefix + "'");
		}
		CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
		CriteriaQuery<PSN> criteriaQuery = criteriaBuilder.createQuery(PSN.class);
		Root<PSN> root = criteriaQuery.from(PSN.class);
		Predicate predicate = criteriaBuilder.and(criteriaBuilder.equal(root.get(PSN_.key).get(PSNKey_.domain), domain),
				criteriaBuilder.like(root.get(PSN_.key).get(PSNKey_.originalValue), valuePrefix + '%'));
		criteriaQuery.select(root).where(predicate);
		List<PSN> result = em.createQuery(criteriaQuery).getResultList();
		if (logger.isDebugEnabled()) {
			logger.debug("found " + result.size() + " entries for domain '" + domain + "' where the value starts with '" + valuePrefix + "'");
		}
		return result;
	}

	@Override
	public String getPseudonymFor(String value, String domain) throws UnknownValueException {
		if (logger.isDebugEnabled()) {
			logger.debug("pseudonym requested for value " + value + " within domain " + domain);
		}
		PSN result = getPSN(value, domain);
		return result.getPseudonym();
	}

	private PSN getPSN(String value, String domain) throws UnknownValueException {
		// zusammengesetzter primary key
		PSNKey key = new PSNKey(value, domain);
		PSN result = em.find(PSN.class, key);
		if (result == null) {
			String message = "value '" + value + "' for domain '" + domain + "' not found";
			logger.error(message);
			throw new UnknownValueException(message);
		}
		return result;
	}

	@Override
	public void anonymiseEntry(String value, String domain) throws DBException, UnknownValueException, ValueIsAnonymisedException {
		if (logger.isDebugEnabled()) {
			logger.debug("anonymising a pseudonym for domain '" + domain + "'");
		}
		if (AnonymDomain.NAME.equals(domain)) {
			logger.warn("it's not possible to anonymise values for the intern domain '" + AnonymDomain.NAME + "'");
			return;
		}
		if (isAnonymised(value)) {
			String message = "value " + value + " is already anonymised";
			logger.info(message);
			throw new ValueIsAnonymisedException(message);
		}
		synchronized (emSynchronizerDummy) {
			PSN origEntity = getPSN(value, domain);
			if (origEntity != null) {
				try {
					String anonym = getOrCreatePseudonymFor(domain + AnonymDomain.DELIMITER + origEntity.getPseudonym(), AnonymDomain.NAME);

					PSNProject parent = origEntity.getPSNProject();
					em.remove(origEntity);
					parent.getPsnList().remove(origEntity);
					em.flush();

					insertValuePseudonymPair(anonym, origEntity.getPseudonym(), parent.getDomain());

					if (logger.isInfoEnabled()) {
						logger.info("pseudonym '" + origEntity.getPseudonym() + "' within domain '" + domain + "' anonymised");
					}
				} catch (Exception e) {
					logger.error("error while anonymising psn entry", e);
					throw new DBException(e);
				}
			} else {
				String message = "pseudonym for value '" + value + "' not found within domain '" + domain + "'";
				logger.warn(message);
				throw new DBException(message);
			}
		}
	}

	@Override
	public void deleteEntry(String value, String domain) throws DeletionForbiddenException, UnknownDomainException, UnknownValueException {
		if (logger.isDebugEnabled()) {
			logger.debug("removing value-pseudonym-pair for value '" + value + "' from domain '" + domain + "'");
		}
		if (!deletablePSNsForDomain(domain)) {
			String message = "the domain '" + domain + "' does not allow deletion of value-pseudonym-pairs";
			logger.warn(message);
			throw new DeletionForbiddenException(message);
		} else {
			PSN psn = getPSN(value, domain);
			synchronized (emSynchronizerDummy) {
				psn.getPSNProject().getPsnList().remove(psn);
				em.remove(psn);
			}
		}
		logger.warn("value-pseudonym-pair for value '" + value + "' removed from domain '" + domain + "'");
	}

	private boolean deletablePSNsForDomain(String domain) throws UnknownDomainException {
		PSNProject project = getPSNProject(domain);
		String property = project.getProperties().getOrDefault(GeneratorProperties.PSNS_DELETABLE, "");
		return "true".equalsIgnoreCase(property);
	}

	@Override
	public void validatePSN(String psn, String domain) throws InvalidPSNException, InvalidGeneratorException, UnknownDomainException {
		if (logger.isDebugEnabled()) {
			logger.debug("validate pseudonym '" + psn + "' within domain '" + domain + "'");
		}
		try {
			getGeneratorFor(domain).check(psn);
		} catch (CharNotInAlphabetException e) {
			throw new InvalidPSNException(e);
		}
	}

	@Override
	public String getValueFor(String psn, String domain)
			throws InvalidPSNException, PSNNotFoundException, InvalidGeneratorException, UnknownDomainException, ValueIsAnonymisedException {
		String result = "";
		if (logger.isDebugEnabled()) {
			logger.debug("find value for pseudonym '" + psn + "' within domain '" + domain + "'");
		}
		validatePSN(psn, domain);
		List<PSN> resultList = getPSNObjects(domain, psn);
		if (resultList.size() == 1) {
			result = resultList.get(0).getKey().getOriginValue();
		} else if (resultList.isEmpty()) {
			String message = "value for pseudonym '" + psn + "' not found within domain '" + domain + "'";
			logger.warn(message);
			throw new PSNNotFoundException(message);
		} else {
			String message = "found multiple values for pseudonym '" + psn + "' within domain '" + domain + "' - may be a jpa-caching problem";
			logger.fatal(message);
			throw new InvalidPSNException(message);
		}
		if (isAnonymised(result)) {
			String message = "requested value for pseudonym " + psn + " can't be retrieved - it is anonymised";
			logger.info(message);
			throw new ValueIsAnonymisedException(message);
		}
		return result;
	}

	private boolean isAnonymised(String value) {
		return value.startsWith(AnonymDomain.PREFIX) && value.endsWith(AnonymDomain.SUFFIX);
	}

	@Override
	public HashMapWrapper<String, String> getOrCreatePseudonymForList(Set<String> values, String domain)
			throws DBException, InvalidGeneratorException, UnknownDomainException {
		if (values == null) {
			logger.warn("parameter 'values' must not be null");
			values = new HashSet<String>();
		}
		if (logger.isInfoEnabled()) {
			logger.info("get or create pseudonyms for " + values.size() + " values within domain '" + domain + "'");
		}
		HashMap<String, String> result = new HashMap<String, String>((int) Math.ceil(values.size() / 0.75));
		if (values.size() < 100) {
			// wenig eintraege - einzeln generieren
			for (String value : values) {
				result.put(value, getOrCreatePseudonymFor(value, domain));
			}
		} else {
			// viele eintraege - alle holen und cachen
			if (logger.isDebugEnabled()) {
				logger.debug("many entries requested - using cache");
			}
			PSNProject parent = getPSNProject(domain);
			List<PSN> psns = parent.getPsnList();
			// genug platz, damit kein rehash passiert
			HashSet<String> allPseudonyms = new HashSet<String>((int) Math.ceil((psns.size() + values.size()) / 0.75));
			HashMap<String, String> valuePsnMap = new HashMap<String, String>((int) Math.ceil((psns.size() + values.size()) / 0.75));
			for (PSN psn : psns) {
				valuePsnMap.put(psn.getKey().getOriginValue(), psn.getPseudonym());
				allPseudonyms.add(psn.getPseudonym());
			}
			int count = 0;
			int countForBeep = values.size() / 50;
			int beepNumber = 1;
			for (String value : values) {
				String pseudonym = valuePsnMap.get(value);
				if (pseudonym != null) {
					result.put(value, pseudonym);
				} else {
					pseudonym = createPSN(parent, value, allPseudonyms).getPseudonym();
					result.put(value, pseudonym);
					valuePsnMap.put(value, pseudonym);
					allPseudonyms.add(pseudonym);
				}
				count++;
				if (count == countForBeep) {
					if (logger.isInfoEnabled()) {
						logger.info("proceeded " + beepNumber * count + " of " + values.size() + " values");
					}
					beepNumber++;
					count = 0;
				}
			}
		}
		return new HashMapWrapper<String, String>(result);
	}

	@Override
	public HashMapWrapper<String, String> getValueForList(Set<String> psnList, String domain)
			throws InvalidGeneratorException, InvalidPSNException, UnknownDomainException {
		if (psnList == null) {
			logger.warn("parameter 'psnList' must not be null");
			psnList = new HashSet<String>();
		}
		if (logger.isDebugEnabled()) {
			logger.debug("get original values for " + psnList.size() + " pseudonyms within domain '" + domain + "'");
		}
		HashMap<String, String> result = new HashMap<String, String>((int) Math.ceil(psnList.size() / 0.75));
		if (psnList.size() < 100) {
			// wenig eintraege - einzeln holen
			for (String pseudonym : psnList) {
				try {
					result.put(pseudonym, getValueFor(pseudonym, domain));
				} catch (PSNNotFoundException e) {
					result.put(pseudonym, PSN_NOT_FOUND);
				} catch (ValueIsAnonymisedException e) {
					result.put(pseudonym, VALUE_IS_ANONYMISED);
				}
			}
		} else {
			// viele eintraege - alle holen und cachen
			if (logger.isDebugEnabled()) {
				logger.debug("many entries requested - using cache");
			}
			List<PSN> psns = getAllPSNObjects(domain);
			// genug platz, damit kein rehash passiert
			HashMap<String, String> psnValueMap = new HashMap<String, String>((int) Math.ceil(psns.size() / 0.75));
			for (PSN psn : psns) {
				psnValueMap.put(psn.getPseudonym(), psn.getKey().getOriginValue());
			}
			for (String pseudonym : psnList) {
				String value = psnValueMap.get(pseudonym);
				if (value == null) {
					logger.warn("value for pseudonym '" + pseudonym + "' not found within domain '" + domain + "'");
					result.put(pseudonym, PSN_NOT_FOUND);
				} else if (isAnonymised(value)) {
					if (logger.isInfoEnabled()) {
						logger.info("requested value for pseudonym " + pseudonym + " can't be retrieved - it is anonymised");
					}
					result.put(pseudonym, VALUE_IS_ANONYMISED);
				} else {
					result.put(pseudonym, value);
				}
			}
		}
		return new HashMapWrapper<String, String>(result);
	}

	@Override
	public HashMapWrapper<String, String> getPseudonymForList(Set<String> values, String domain) {
		if (values == null) {
			logger.warn("parameter 'psnList' must not be null");
			values = new HashSet<String>();
		}
		if (logger.isInfoEnabled()) {
			logger.info("get pseudonyms for " + values.size() + " values within domain '" + domain + "'");
		}
		HashMap<String, String> result = new HashMap<String, String>((int) Math.ceil(values.size() / 0.75));
		if (values.size() < 100) {
			for (String value : values) {
				try {
					result.put(value, getPseudonymFor(value, domain));
				} catch (UnknownValueException e) {
					result.put(value, VALUE_NOT_FOUND);
				}
			}
		} else {
			// viele eintraege - alle holen und cachen
			if (logger.isDebugEnabled()) {
				logger.debug("many entries requested - using cache");
			}
			List<PSN> psns = getAllPSNObjects(domain);
			// genug platz, damit kein rehash passiert
			HashMap<String, String> valuePsnMap = new HashMap<String, String>((int) Math.ceil(psns.size() / 0.75));
			for (PSN psn : psns) {
				valuePsnMap.put(psn.getKey().getOriginValue(), psn.getPseudonym());
			}
			for (String value : values) {
				String pseudonym = valuePsnMap.get(value);
				if (pseudonym == null) {
					logger.warn("pseudonym for value '" + value + "' not found within domain '" + domain + "'");
					result.put(value, VALUE_NOT_FOUND);
				} else {
					result.put(value, pseudonym);
				}
			}
		}
		return new HashMapWrapper<String, String>(result);
	}

	@Override
	public HashMapWrapper<String, String> getPseudonymForValuePrefix(String valuePrefix, String domain) {
		if (logger.isInfoEnabled()) {
			logger.info("get pseudonyms for values which starts with '" + valuePrefix + "' within domain '" + domain + "'");
		}
		HashMap<String, String> result = new HashMap<String, String>();
		List<PSN> psnList = getAllPSNObjectsForValuePrefix(domain, valuePrefix);
		for (PSN psn : psnList) {
			result.put(psn.getKey().getOriginValue(), psn.getPseudonym());
		}
		return new HashMapWrapper<String, String>(result);
	}

	@Override
	public void insertValuePseudonymPair(String value, String pseudonym, String domain)
			throws DBException, InvalidGeneratorException, InvalidPSNException, UnknownDomainException {
		if (logger.isInfoEnabled()) {
			logger.info("insert pseudonym for '" + value + "' in domain '" + domain + "'");
		}
		PSNProject parent = getPSNProject(domain);
		validatePSN(pseudonym, domain);
		PSNKey key = new PSNKey(value, domain);
		PSN psn = em.find(PSN.class, key);
		if (psn != null) {
			if (psn.getPseudonym().equals(pseudonym)) {
				logger.warn("pseudonym for value '" + value + "' already exists within domain '" + domain + "'");
				return;
			} else {
				String message = "a different pseudonym for value '" + value + "' already exists within domain '" + domain + "'";
				logger.error(message);
				throw new DBException(message);
			}
		} else if (existsPseudonym(domain, pseudonym)) {
			// erst nach der pruefung auf value, da nur eine warnung
			// kommen soll, wenn das paar genau so schon exisitert
			String message = "pseudonym '" + pseudonym + "' already exists within domain '" + domain + "'";
			logger.error(message);
			throw new DBException(message);
		}
		synchronized (emSynchronizerDummy) {
			psn = new PSN(parent, value, pseudonym);
			em.persist(psn);
			parent.getPsnList().add(psn);
		}
		if (logger.isInfoEnabled()) {
			logger.info("pseudonym for '" + value + "' in domain '" + domain + "' inserted");
		}
	}

	@Override
	public void insertValuePseudonymPairs(HashMapWrapper<String, String> pairs, String domain)
			throws DBException, InvalidGeneratorException, InvalidPSNException, UnknownDomainException {
		if (pairs == null) {
			logger.warn("parameter 'pairs' should not be null");
			pairs = new HashMapWrapper<String, String>();
		}
		if (logger.isInfoEnabled()) {
			logger.info("insert " + pairs.getMap().size() + " values-pseudonym pairs in domain '" + domain + "'");
		}
		PSNProject parent = getPSNProject(domain);
		synchronized (emSynchronizerDummy) {
			List<String> duplicates = new ArrayList<String>();
			for (Entry<String, String> entry : pairs.getMap().entrySet()) {
				validatePSN(entry.getValue(), domain);
				PSNKey key = new PSNKey(entry.getKey(), domain);
				PSN psn = em.find(PSN.class, key);
				if (psn != null) {
					if (psn.getPseudonym().equals(entry.getValue())) {
						logger.warn("pseudonym for value '" + entry.getKey() + "' already exists within domain '" + domain + "'");
						duplicates.add(entry.getKey());
					} else {
						String message = "a different pseudonym for value '" + entry.getKey() + "' already exists within domain '" + domain + "'";
						logger.error(message);
						throw new DBException(message);
					}
				} else if (existsPseudonym(domain, entry.getValue())) {
					// erst nach der pruefung auf value, da nur eine warnung
					// kommen soll, wenn das paar genau so schon exisitert
					String message = "pseudonym '" + entry.getValue() + "' already exists within domain '" + domain + "'";
					logger.error(message);
					throw new DBException(message);
				}
			}
			for (String duplicate : duplicates) {
				pairs.getMap().remove(duplicate);
			}
			synchronized (emSynchronizerDummy) {
				for (Entry<String, String> entry : pairs.getMap().entrySet()) {
					PSN psn = new PSN(parent, entry.getKey(), entry.getValue());
					em.persist(psn);
					parent.getPsnList().add(psn);
				}
			}
		}
		if (logger.isInfoEnabled()) {
			logger.info("inserted " + pairs.getMap().size() + " values-pseudonym pairs in domain '" + domain + "'");
		}
	}

	@Override
	public PSNTreeDTO getPSNTreeForPSN(String psn, String domain) throws DBException, UnknownDomainException, InvalidPSNException,
			PSNNotFoundException, InvalidGeneratorException, ValueIsAnonymisedException {
		PSNProject currentProject = getPSNProject(domain);
		String currentPSN = psn;

		// Zum Root Projekt zurueckiterieren
		while (currentProject.getParent() != null) {
			currentPSN = getValueFor(currentPSN, currentProject.getDomain());
			currentProject = getPSNProject(currentProject.getParent().getDomain());
		}
		// Initiales hinzufuegen des Root Nodes. Bei diesem wird der originalValue des aktuellen Projektes verwendet und nicht das Pseudonym
		PSNTreeNode rootNode = new PSNTreeNode("ROOT", getValueFor(currentPSN, currentProject.getDomain()));
		rootNode.getChildren().add(createPSNTree(currentPSN, currentProject));
		return rootNode.toDTO();
	}

	/**
	 * Recursively traverse deeper into psn_projects until all projects related to the passed originalValue and project are found
	 * 
	 * @return a PSNTreeNode containing child nodes, child child nodes and so on
	 */
	private PSNTreeNode createPSNTree(String originalValue, PSNProject project) {
		PSNTreeNode currentNode = new PSNTreeNode(project.getDomain(), originalValue);
		for (PSNProject child : project.getChildren()) {
			try {
				String nextPSN = getPseudonymFor(originalValue, child.getDomain());
				currentNode.getChildren().add(createPSNTree(nextPSN, child));
			} catch (UnknownValueException e) {
				logger.warn("Unexpected exception: no pseudonym available in domain: " + child.getDomain() + " for originalValue: " + originalValue);
			}
		}
		return currentNode;
	}
}
