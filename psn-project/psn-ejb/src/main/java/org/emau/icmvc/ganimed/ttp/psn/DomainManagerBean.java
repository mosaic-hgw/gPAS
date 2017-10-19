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
import java.util.List;

import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.apache.log4j.Logger;
import org.emau.icmvc.ganimed.ttp.psn.alphabets.GenericAlphabet;
import org.emau.icmvc.ganimed.ttp.psn.dto.DomainDTO;
import org.emau.icmvc.ganimed.ttp.psn.dto.DomainLightDTO;
import org.emau.icmvc.ganimed.ttp.psn.dto.PSNDTO;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.DomainInUseException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.InvalidAlphabetException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.InvalidCheckDigitClassException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.InvalidDomainNameException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.InvalidGeneratorException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.UnknownDomainException;
import org.emau.icmvc.ganimed.ttp.psn.generator.Alphabet;
import org.emau.icmvc.ganimed.ttp.psn.generator.CheckDigits;
import org.emau.icmvc.ganimed.ttp.psn.generator.Generator;
import org.emau.icmvc.ganimed.ttp.psn.generator.GeneratorProperties;
import org.emau.icmvc.ganimed.ttp.psn.internal.AnonymDomain;
import org.emau.icmvc.ganimed.ttp.psn.internal.Cache;
import org.emau.icmvc.ganimed.ttp.psn.model.PSN;
import org.emau.icmvc.ganimed.ttp.psn.model.PSNKey_;
import org.emau.icmvc.ganimed.ttp.psn.model.PSNProject;
import org.emau.icmvc.ganimed.ttp.psn.model.PSN_;

/**
 * webservice for domains (psn-projects)
 * <p>
 * including a cache for the generator classes
 * 
 * @author geidell
 * 
 */
@WebService(name = "DomainService")
@SOAPBinding(style = SOAPBinding.Style.RPC)
@Stateless
@Local(DomainManagerLocal.class)
@Remote(DomainManager.class)
@PersistenceContext(name = "psn")
public class DomainManagerBean implements DomainManagerLocal, DomainManager {

	private static final Logger logger = Logger.getLogger(DomainManagerBean.class);
	@PersistenceContext
	private EntityManager em;
	private static final Object emSynchronizerDummy = new Object();

	@Override
	public Generator getGeneratorFor(String domain)
			throws InvalidAlphabetException, InvalidCheckDigitClassException, InvalidGeneratorException, UnknownDomainException {
		Generator result = null;
		if (logger.isDebugEnabled()) {
			logger.debug("requested generator for domain '" + domain + "'");
		}
		result = Cache.getGenerator(domain);
		if (result == null) {
			if (logger.isDebugEnabled()) {
				logger.debug("generator not found within cache - create a new one");
			}
			PSNProject project = getPSNProject(domain);
			result = createGenerator(project);
		}
		return result;
	}

	private Generator createGenerator(PSNProject project)
			throws InvalidCheckDigitClassException, InvalidAlphabetException, InvalidGeneratorException {
		Generator result;
		if (logger.isInfoEnabled()) {
			logger.info("create new generator for domain '" + project.getDomain() + "' with check digit class '" + project.getGeneratorClass()
					+ "' and alphabet '" + project.getAlphabet() + "'");
		}
		Class<? extends CheckDigits> checkDigitClass = createCheckDigitClass(project.getGeneratorClass());
		Alphabet alphabet = createAlphabet(project.getDomain(), project.getAlphabet());
		if (logger.isDebugEnabled()) {
			logger.debug("creating generator");
		}
		result = new Generator(checkDigitClass, alphabet, project.getProperties());
		Cache.cacheGenerator(project.getDomain(), result);
		return result;
	}

	private Alphabet createAlphabet(String domain, String alphabetString) throws InvalidAlphabetException {
		Alphabet result;
		if (alphabetString == null) {
			String message = "alphabet is null";
			logger.error(message);
			throw new InvalidAlphabetException(message);
		}
		if (alphabetString.contains(",")) {
			try {
				if (logger.isDebugEnabled()) {
					logger.debug("creating generic alphabet with following chars: " + alphabetString);
				}
				result = new GenericAlphabet(alphabetString);
			} catch (Exception e) {
				String message = "exception while creating alphabet '" + alphabetString + "' - " + e
						+ (e.getCause() != null ? "(" + e.getCause() + ")" : "");
				logger.error(message, e);
				throw new InvalidAlphabetException(message, e);
			}
		} else {
			Class<? extends Alphabet> alphabetClass;
			try {
				logger.debug("creating alphabet class");
				Class<?> temp = Class.forName(alphabetString);
				alphabetClass = temp.asSubclass(Alphabet.class);
				result = alphabetClass.newInstance();
			} catch (Exception e) {
				String message = "exception while creating alphabet class '" + alphabetString + "' - " + e
						+ (e.getCause() != null ? "(" + e.getCause() + ")" : "");
				logger.error(message, e);
				throw new InvalidAlphabetException(message, e);
			}
		}
		return result;
	}

	private Class<? extends CheckDigits> createCheckDigitClass(String checkDigitClass) throws InvalidCheckDigitClassException {
		Class<? extends CheckDigits> result;
		if (checkDigitClass == null) {
			String message = "check digit class is null";
			logger.error(message);
			throw new InvalidCheckDigitClassException(message);
		}
		try {
			if (logger.isDebugEnabled()) {
				logger.debug("loading check digit class");
			}
			Class<?> temp = Class.forName(checkDigitClass);
			result = temp.asSubclass(CheckDigits.class);
		} catch (Exception e) {
			String message = "exception while loading check digit class '" + checkDigitClass + "' - " + e
					+ (e.getCause() != null ? "(" + e.getCause() + ")" : "");
			logger.error(message, e);
			throw new InvalidCheckDigitClassException(message, e);
		}
		return result;
	}

	@Override
	public void addDomain(DomainLightDTO domainDTO) throws InvalidDomainNameException, InvalidAlphabetException, InvalidCheckDigitClassException,
			InvalidGeneratorException, DomainInUseException, UnknownDomainException {
		if (logger.isInfoEnabled()) {
			logger.info("try to create a new PSNProject for domain " + domainDTO.getDomain());
		}
		if (domainDTO.getDomain() == null || domainDTO.getDomain().isEmpty()) {
			String message = "the given domain name is invalid (null or empty)";
			logger.error(message);
			throw new InvalidDomainNameException(message);
		}
		if (Cache.getGenerator(domainDTO.getDomain()) != null) {
			String message = "object for domain '" + domainDTO.getDomain() + "' exists within cache";
			logger.error(message);
			throw new DomainInUseException(message);
		}
		synchronized (emSynchronizerDummy) {
			PSNProject duplicate = em.find(PSNProject.class, domainDTO.getDomain());
			if (duplicate != null) {
				String message = "psn-project with domain '" + domainDTO.getDomain() + "' already exists";
				logger.error(message);
				throw new DomainInUseException(message);
			}
			String parentDomainName = domainDTO.getParentDomain();
			PSNProject parentDomain = null;
			if (parentDomainName != null && !parentDomainName.isEmpty()) {
				parentDomain = getPSNProject(parentDomainName);
			}
			PSNProject project = new PSNProject(domainDTO, parentDomain);
			if (logger.isDebugEnabled()) {
				logger.debug("new PSNProject created: " + project);
			}
			createGenerator(project); // einstellungen testen und generator in cache legen
			em.persist(project);
			if (parentDomain != null) {
				parentDomain.getChildren().add(project);
			}
		}
		if (logger.isInfoEnabled()) {
			logger.info("new PSNProject for domain '" + domainDTO.getDomain() + "' persisted");
		}
	}

	@Override
	public void deleteDomain(String domain) throws DomainInUseException, UnknownDomainException {
		if (logger.isDebugEnabled()) {
			logger.debug("trying to delete psn-project for domain: " + domain);
		}
		synchronized (emSynchronizerDummy) {
			PSNProject project = getPSNProject(domain);
			if (project.getPsnList().size() > 0) {
				String message = "at least one pseudonym belongs to domain '" + domain + "' which therefore can't be deleted";
				logger.warn(message);
				throw new DomainInUseException(message);
			}
			if (project.getChildren().size() > 0) {
				String message = "at least one domain is a child of domain '" + domain + "' which therefore can't be deleted";
				logger.warn(message);
				throw new DomainInUseException(message);
			}
			PSNProject parent = project.getParent();
			em.remove(project);
			Cache.removeGenerator(domain);
			if (parent != null) {
				parent.getChildren().remove(project);
			}
			if (logger.isInfoEnabled()) {
				logger.info("psn-project for domain '" + domain + "' deleted");
			}
		}
	}

	@Override
	public DomainDTO getDomainObject(String domain) throws UnknownDomainException {
		return getPSNProject(domain).toDTO(countPseudonymsForDomain(domain));
	}

	@Override
	public DomainLightDTO getDomainLightObject(String domain) throws UnknownDomainException {
		return getPSNProject(domain).toLightDTO();
	}

	private PSNProject getPSNProject(String domain) throws UnknownDomainException {
		PSNProject result = em.find(PSNProject.class, domain);
		if (result == null) {
			String message = "psn-project for domain '" + domain + "' not found";
			logger.error(message);
			throw new UnknownDomainException(message);
		}
		return result;
	}

	private Long countPseudonymsForDomain(String domain) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Long> cq = cb.createQuery(Long.class);
		Root<PSN> root = cq.from(PSN.class);
		cq.select(cb.count(root));
		cq.where(cb.equal(root.get(PSN_.key).get(PSNKey_.domain), domain));
		return em.createQuery(cq).getSingleResult();
	}

	@Override
	public List<DomainDTO> listDomains() {
		List<DomainDTO> result = new ArrayList<DomainDTO>();
		if (logger.isDebugEnabled()) {
			logger.debug("listDomains called");
		}
		CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
		CriteriaQuery<PSNProject> criteriaQuery = criteriaBuilder.createQuery(PSNProject.class);
		Root<PSNProject> root = criteriaQuery.from(PSNProject.class);
		criteriaQuery.select(root);
		List<PSNProject> projects = em.createQuery(criteriaQuery).getResultList();
		for (PSNProject project : projects) {
			if (!AnonymDomain.NAME.equals(project.getDomain())) {
				result.add(project.toDTO(countPseudonymsForDomain(project.getDomain())));
			}
		}
		if (logger.isDebugEnabled()) {
			logger.debug("listDomains returns " + result.size() + " results");
		}
		return result;
	}

	@Override
	public List<DomainLightDTO> listDomainsLight() {
		List<DomainLightDTO> result = new ArrayList<DomainLightDTO>();
		if (logger.isDebugEnabled()) {
			logger.debug("listDomainsLight called");
		}
		CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
		CriteriaQuery<PSNProject> criteriaQuery = criteriaBuilder.createQuery(PSNProject.class);
		Root<PSNProject> root = criteriaQuery.from(PSNProject.class);
		criteriaQuery.select(root);
		List<PSNProject> projects = em.createQuery(criteriaQuery).getResultList();
		for (PSNProject project : projects) {
			if (!AnonymDomain.NAME.equals(project.getDomain())) {
				result.add(project.toLightDTO());
			}
		}
		if (logger.isDebugEnabled()) {
			logger.debug("listDomainsLight returns " + result.size() + " results");
		}
		return result;
	}

	@Override
	public List<String> listPossibleProperties() {
		if (logger.isDebugEnabled()) {
			logger.debug("listPossibleProperties called");
		}
		List<String> result = Cache.getPossibleGeneratorProperties();
		if (logger.isDebugEnabled()) {
			logger.debug("listPossibleProperties returns " + result.size() + " results");
		}
		return result;
	}

	@Override
	public List<PSNDTO> listPseudonymsFor(String domain) throws UnknownDomainException {
		List<PSNDTO> result = new ArrayList<PSNDTO>();
		if (logger.isDebugEnabled()) {
			logger.debug("listPseudonyms called for domain '" + domain + "'");
		}
		PSNProject parent = getPSNProject(domain);
		for (PSN psn : parent.getPsnList()) {
			result.add(psn.toPSNDTO());
		}
		if (logger.isDebugEnabled()) {
			logger.debug("listPseudonyms returns " + result.size() + " results");
		}
		return result;
	}

	@Override
	public List<DomainLightDTO> getDomainsForPrefix(String prefix) {
		List<DomainLightDTO> result = new ArrayList<DomainLightDTO>();
		if (logger.isDebugEnabled()) {
			logger.debug("getDomainsForPrefix called with prefix '" + prefix + "'");
		}
		if (prefix == null) {
			logger.error("prefix must not be null");
		} else {
			CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
			CriteriaQuery<PSNProject> criteriaQuery = criteriaBuilder.createQuery(PSNProject.class);
			Root<PSNProject> root = criteriaQuery.from(PSNProject.class);
			criteriaQuery.select(root);
			List<PSNProject> projects = em.createQuery(criteriaQuery).getResultList();
			for (PSNProject project : projects) {
				if (!AnonymDomain.NAME.equals(project.getDomain()) && prefix.equals(project.getProperties().get(GeneratorProperties.PSN_PREFIX))) {
					result.add(project.toLightDTO());
				} else if (prefix.isEmpty() && project.getProperties().get(GeneratorProperties.PSN_PREFIX) == null) {
					result.add(project.toLightDTO());
				}
			}
			if (logger.isDebugEnabled()) {
				logger.debug("getDomainsForPrefix returns " + result.size() + " results");
			}
		}
		return result;
	}

	@Override
	public List<DomainLightDTO> getDomainsForSuffix(String suffix) {
		List<DomainLightDTO> result = new ArrayList<DomainLightDTO>();
		if (logger.isDebugEnabled()) {
			logger.debug("getDomainsForSuffix called with suffix '" + suffix + "'");
		}
		if (suffix == null) {
			logger.error("prefix must not be null");
		} else {
			CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
			CriteriaQuery<PSNProject> criteriaQuery = criteriaBuilder.createQuery(PSNProject.class);
			Root<PSNProject> root = criteriaQuery.from(PSNProject.class);
			criteriaQuery.select(root);
			List<PSNProject> projects = em.createQuery(criteriaQuery).getResultList();
			for (PSNProject project : projects) {
				if (!AnonymDomain.NAME.equals(project.getDomain()) && suffix.equals(project.getProperties().get(GeneratorProperties.PSN_SUFFIX))) {
					result.add(project.toLightDTO());
				} else if (suffix.isEmpty() && project.getProperties().get(GeneratorProperties.PSN_SUFFIX) == null) {
					result.add(project.toLightDTO());
				}
			}
			if (logger.isDebugEnabled()) {
				logger.debug("getDomainsForSuffix returns " + result.size() + " results");
			}
		}
		return result;
	}
}
