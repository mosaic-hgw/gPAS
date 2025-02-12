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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.ejb.ConcurrencyManagement;
import jakarta.ejb.ConcurrencyManagementType;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.emau.icmvc.ganimed.ttp.psn.config.PaginationConfig;
import org.emau.icmvc.ganimed.ttp.psn.enums.InsertPairError;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.InsertPairException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.InvalidPSNException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.PSNNotFoundException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.UnknownDomainException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.UnknownValueException;
import org.emau.icmvc.ganimed.ttp.psn.model.Domain;
import org.emau.icmvc.ganimed.ttp.psn.model.PSN;
import org.emau.icmvc.ganimed.ttp.psn.model.Statistic;
import org.emau.icmvc.ganimed.ttp.psn.model.Statistic_;

import static org.emau.icmvc.ganimed.ttp.psn.internal.QueryHelper.helperFor;
import static org.emau.icmvc.ganimed.ttp.psn.internal.QueryHelper.helperForMPSN;
import static org.emau.icmvc.ganimed.ttp.psn.internal.QueryHelper.helperForSPSN;

@Stateless
@ConcurrencyManagement(ConcurrencyManagementType.BEAN) // wird ueber die rw-locks in cache gesichert
public class DAO
{
	private static final Logger LOGGER = LogManager.getLogger(DAO.class);

	@PersistenceContext(unitName = "gpas")
	private EntityManager em;

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public List<Domain> listDomains()
	{
		CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
		CriteriaQuery<Domain> criteriaQuery = criteriaBuilder.createQuery(Domain.class);
		Root<Domain> root = criteriaQuery.from(Domain.class);
		criteriaQuery.select(root);
		return em.createQuery(criteriaQuery).getResultList();
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public List<PSN> getPSNObjects(String value, Domain domain) throws UnknownValueException
	{
		List<PSN> result = helperFor(domain).findByValue(em, domain, value);
		if (result == null || result.isEmpty())
		{
			String message = "value " + value + " for domain " + domain.getName() + " not found";
			LOGGER.debug(message);
			throw new UnknownValueException(message);
		}
		return result;
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public Domain getDomainObject(String domainName) throws UnknownDomainException
	{
		Domain result = em.find(Domain.class, domainName);
		if (result == null)
		{
			String message = "domain " + domainName + " not found";
			LOGGER.warn(message);
			throw new UnknownDomainException(message);
		}
		return result;
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void addPSN(PSN psn)
	{
		em.persist(psn);
		em.flush();
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void deletePSN(PSN psn)
	{
		psn = em.merge(psn);
		em.remove(psn);
		em.flush();
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void deleteAllPSNForDomain(Domain domain)
	{
		helperFor(domain).deleteByDomain(em, domain);
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public PSN getPSNObjectForPseudonym(Domain domain, String pseudonym) throws PSNNotFoundException, InvalidPSNException
	{
		List<PSN> resultList = helperFor(domain).findByPSN(em, domain, pseudonym);

		if (resultList.size() == 1)
		{
			return resultList.getFirst();
		}
		else if (resultList.isEmpty())
		{
			String message = "value for pseudonym " + pseudonym + " not found within domain " + domain.getName();
			LOGGER.debug(message);
			throw new PSNNotFoundException(message);
		}
		else
		{
			String message = "found multiple values for pseudonym " + pseudonym + " within domain " + domain.getName() + " - could be a jpa-caching problem";
			LOGGER.fatal(message);
			throw new InvalidPSNException(message);
		}
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public List<PSN> getPSNObjectsForPSNs(Domain domain, Set<String> psns)
	{
		return helperFor(domain).findByPSNs(em, domain, psns);
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public List<PSN> getPSNObjectsForValues(Domain domain, Set<String> values)
	{
		return helperFor(domain).findByValues(em, domain, values);
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public List<PSN> getAllPSNObjectsForValuePrefix(Domain domain, String valuePrefix)
	{
		return helperFor(domain).queryAllPSNObjectsForValuePrefix(em, domain, valuePrefix);
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public boolean insertValuePseudonymPair(String value, String pseudonym, Domain domain, boolean multiPSN) throws InsertPairException
	{
		try
		{
			PSN psn = getPSNObjectForPseudonym(domain, pseudonym);
			// pseudonym already exists
			if (value.equals(psn.getOriginalValue()))
			{
				return false;
			}
			String message = "pseudonym " + pseudonym + " already exists within domain " + domain.getName() + ", but for another value";
			LOGGER.debug(message);
			throw new InsertPairException(message, value, pseudonym, InsertPairError.DIFFERENT_VALUE_FOR_PSEUDONYM_EXISTS);
		}
		catch (PSNNotFoundException expected)
		{
			// expected that the pseudonym does not yet exist
		}
		catch (InvalidPSNException e) // multiple pseudonyms
		{
			throw new InsertPairException(pseudonym + " is not a valid pseudonym for domain " + domain.getName(), value, pseudonym, InsertPairError.PSEUDONYM_INVALID);
		}
		if (!multiPSN)
		{
			List<PSN> psns = helperFor(domain).findByValue(em, domain, value);
			if (!psns.isEmpty())
			{
				String message = "a different pseudonym for value " + value + " already exists within single-PSN-domain " + domain.getName();
				LOGGER.error(message);
				throw new InsertPairException(message, value, pseudonym, InsertPairError.DIFFERENT_PSEUDONYM_FOR_VALUE_EXISTS);
			}
		}
		em.persist(PSN.create(domain, value, pseudonym));
		em.flush();
		return true;
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void insertValuePseudonymPairsWithoutCheck(Map<String, String> psnPairs, Domain domain)
	{
		PSN.create(domain, psnPairs).forEach(psn -> em.persist(psn));
		em.flush();
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void addDomain(Domain domain, List<Domain> parents)
	{
		em.persist(domain);
		if (parents != null)
		{
			for (Domain parent : parents)
			{
				parent.getChildren().add(domain);
				em.merge(parent);
			}
		}
		em.flush();
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void updateDomain(Domain domain, List<Domain> parents, List<Domain> oldParents)
	{
		em.merge(domain);
		if (parents != null)
		{
			for (Domain parent : parents)
			{
				em.merge(parent);
			}
		}
		if (oldParents != null)
		{
			for (Domain parent : oldParents)
			{
				em.merge(parent);
			}
		}
		em.flush();
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public Long countPseudonymsForDomain(Domain domain)
	{
		return helperFor(domain).countPseudonymsForDomain(em, domain);
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public long countAnonymsForDomain(Domain domain)
	{
		return helperFor(domain).countAnonymsForDomain(em, domain);
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void deleteDomain(Domain domain, List<Domain> parents)
	{
		domain = em.merge(domain);
		em.remove(domain);
		if (parents != null)
		{
			for (Domain p : parents)
			{
				em.merge(p);
			}
		}
		em.flush();
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public List<PSN> getPSNObjectsForDomain(Domain domain)
	{
		return helperFor(domain).queryPSNObjectsForDomain(em, domain);
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public long countPSNObjectsForDomains(List<Domain> domains, PaginationConfig config)
	{
		return
				helperForMPSN().countPSNObjectsForDomains(em, domains, config) +
				helperForSPSN().countPSNObjectsForDomains(em, domains, config);
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public List<PSN> getPSNObjectsForDomains(List<Domain> domains, PaginationConfig config)
	{
		List<PSN> result = new ArrayList<>();
		result.addAll(helperForMPSN().queryPSNObjectsForDomains(em, domains, config));
		result.addAll(helperForSPSN().queryPSNObjectsForDomains(em, domains, config));
		return result;
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public List<PSN> getPSNObjectsForDomainPaginated(Domain domain, int maxResults, String startAfterOrigValue)
	{
		return helperFor(domain).queryPSNObjectsForDomainPaginated(em, domain, maxResults, startAfterOrigValue);
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public List<String> getPseudonymsForDomainPaginated(Domain domain, int maxResults, String startAfterPseudonym)
	{
		return helperFor(domain).queryPseudonymsForDomainPaginated(em, domain, maxResults, startAfterPseudonym);
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public List<String> getExistingPSNs(Domain domain, Set<String> psns)
	{
		return helperFor(domain).queryExistingPSNs(em,domain, psns);
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public List<Statistic> getAllStats()
	{
		CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
		CriteriaQuery<Statistic> criteriaQuery = criteriaBuilder.createQuery(Statistic.class);
		Root<Statistic> root = criteriaQuery.from(Statistic.class);
		criteriaQuery.select(root);
		return em.createQuery(criteriaQuery).getResultList();
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public List<Statistic> getStatsFromTo(Date from, Date to)
	{
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Statistic> cq = cb.createQuery(Statistic.class);
		Root<Statistic> root = cq.from(Statistic.class);
		cq.select(root).where(cb.between(root.get(Statistic_.entrydate), from, to));
		return em.createQuery(cq).getResultList();
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void addStat(Statistic stat)
	{
		em.persist(stat);
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public Statistic getFirstStat() throws NoResultException
	{
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Statistic> cq = cb.createQuery(Statistic.class);
		Root<Statistic> root = cq.from(Statistic.class);
		cq.select(root).orderBy(cb.asc(root.get(Statistic_.stat_entry_id)));
		List<Statistic> result = em.createQuery(cq).setMaxResults(1).getResultList();
		return result.isEmpty() ? null : result.getFirst();
	}
	
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public Statistic getLatestStat() throws NoResultException
	{
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Statistic> cq = cb.createQuery(Statistic.class);
		Root<Statistic> root = cq.from(Statistic.class);
		cq.select(root).orderBy(cb.desc(root.get(Statistic_.stat_entry_id)));
		List<Statistic> result = em.createQuery(cq).setMaxResults(1).getResultList();
		return result.isEmpty() ? null : result.getFirst();
	}
}