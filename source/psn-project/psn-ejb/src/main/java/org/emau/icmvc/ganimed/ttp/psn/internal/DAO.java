package org.emau.icmvc.ganimed.ttp.psn.internal;

/*-
 * ###license-information-start###
 * gPAS - a Generic Pseudonym Administration Service
 * __
 * Copyright (C) 2013 - 2022 Independent Trusted Third Party of the University Medicine Greifswald
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
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaBuilder.In;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.emau.icmvc.ganimed.ttp.psn.config.PSNField;
import org.emau.icmvc.ganimed.ttp.psn.config.PaginationConfig;
import org.emau.icmvc.ganimed.ttp.psn.enums.InsertPairError;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.InsertPairException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.InvalidPSNException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.PSNNotFoundException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.UnknownDomainException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.UnknownValueException;
import org.emau.icmvc.ganimed.ttp.psn.model.Domain;
import org.emau.icmvc.ganimed.ttp.psn.model.PSN;
import org.emau.icmvc.ganimed.ttp.psn.model.PSNKey;
import org.emau.icmvc.ganimed.ttp.psn.model.PSNKey_;
import org.emau.icmvc.ganimed.ttp.psn.model.PSN_;
import org.emau.icmvc.ganimed.ttp.psn.model.Statistic;
import org.emau.icmvc.ganimed.ttp.psn.model.Statistic_;

@Stateless
@ConcurrencyManagement(ConcurrencyManagementType.BEAN) // wird ueber die rw-locks in cache gesichert
public class DAO
{
	private static final String PERCENT = "%";
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
		List<Domain> result = em.createQuery(criteriaQuery).getResultList();
		return result;
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public PSN getPSNObject(String value, String domainName) throws UnknownValueException
	{
		PSNKey key = new PSNKey(value, domainName);
		PSN result = em.find(PSN.class, key);
		if (result == null)
		{
			String message = "value " + value + " for domain " + domainName + " not found";
			if (LOGGER.isDebugEnabled())
			{
				LOGGER.debug(message);
			}
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
	public void deletePSN(String value, String domainName) throws UnknownValueException
	{
		em.remove(getPSNObject(value, domainName));
		em.flush();
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public PSN getPSNObjectForPseudonym(String domainName, String pseudonym) throws PSNNotFoundException, InvalidPSNException
	{
		CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
		CriteriaQuery<PSN> criteriaQuery = criteriaBuilder.createQuery(PSN.class);
		Root<PSN> root = criteriaQuery.from(PSN.class);
		Predicate predicate = criteriaBuilder.and(criteriaBuilder.equal(root.get(PSN_.pseudonym), pseudonym),
				criteriaBuilder.equal(root.get(PSN_.key).get(PSNKey_.domain), domainName));
		criteriaQuery.select(root).where(predicate);
		List<PSN> resultList = em.createQuery(criteriaQuery).getResultList();
		if (resultList.size() == 1)
		{
			return resultList.get(0);
		}
		else if (resultList.isEmpty())
		{
			String message = "value for pseudonym " + pseudonym + " not found within domain " + domainName;
			if (LOGGER.isDebugEnabled())
			{
				LOGGER.debug(message);
			}
			throw new PSNNotFoundException(message);
		}
		else
		{
			String message = "found multiple values for pseudonym " + pseudonym + " within domain " + domainName + " - could be a jpa-caching problem";
			LOGGER.fatal(message);
			throw new InvalidPSNException(message);
		}
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	@SuppressWarnings("unchecked")
	public List<PSN> getPSNObjectsForPSNs(String domainName, Set<String> psns)
	{
		List<PSN> result = new ArrayList<>();
		if (!psns.isEmpty())
		{
			result = em.createNamedQuery("PSN.findByPSNs").setParameter("domainName", domainName).setParameter("psns", psns).getResultList();
		}
		return result;
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	@SuppressWarnings("unchecked")
	public List<PSN> getPSNObjectsForValues(String domainName, Set<String> values)
	{
		return em.createNamedQuery("PSN.findByValues").setParameter("domainName", domainName).setParameter("values", values).getResultList();
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public List<PSN> getAllPSNObjectsForValuePrefix(String domainName, String valuePrefix)
	{
		CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
		CriteriaQuery<PSN> criteriaQuery = criteriaBuilder.createQuery(PSN.class);
		Root<PSN> root = criteriaQuery.from(PSN.class);
		Predicate predicate = criteriaBuilder.and(criteriaBuilder.equal(root.get(PSN_.key).get(PSNKey_.domain), domainName),
				criteriaBuilder.like(root.get(PSN_.key).get(PSNKey_.originalValue), valuePrefix + '%'));
		criteriaQuery.select(root).where(predicate);
		List<PSN> result = em.createQuery(criteriaQuery).getResultList();
		return result;
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public boolean insertValuePseudonymPair(String value, String pseudonym, Domain domain) throws InsertPairException
	{
		PSNKey key = new PSNKey(value, domain.getName());
		PSN psn = em.find(PSN.class, key);
		if (psn != null)
		{
			if (psn.getPseudonym().equals(pseudonym))
			{
				if (LOGGER.isDebugEnabled())
				{
					LOGGER.debug("pseudonym for value " + value + " already exists within domain " + domain.getName());
				}
				return false;
			}
			else
			{
				String message = "a different pseudonym for value " + value + " already exists within domain " + domain.getName();
				LOGGER.error(message);
				throw new InsertPairException(message, value, pseudonym, InsertPairError.DIFFERENT_PSEUDONYM_FOR_VALUE_EXISTS);
			}
		}
		else
		{
			try
			{
				if (existsPseudonym(domain.getName(), pseudonym))
				{
					String message = "pseudonym " + pseudonym + " already exists within domain " + domain.getName() + ", but for another value";
					if (LOGGER.isDebugEnabled())
					{
						LOGGER.debug(message);
					}
					throw new InsertPairException(message, value, pseudonym, InsertPairError.DIFFERENT_VALUE_FOR_PSEUDONYM_EXISTS);
				}
			}
			catch (InvalidPSNException e)
			{
				throw new InsertPairException(pseudonym + " is not a valid pseudonym for domain " + domain.getName(), value, pseudonym, InsertPairError.PSEUDONYM_INVALID);
			}
		}
		psn = new PSN(domain, value, pseudonym);
		em.persist(psn);
		em.flush();
		return true;
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void insertValuePseudonymPairsWithoutCheck(Map<String, String> psnPairs, Domain domain)
	{
		for (Entry<String, String> entry : psnPairs.entrySet())
		{
			PSN psn = new PSN(domain, entry.getKey(), entry.getValue());
			em.persist(psn);
		}
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
	public Long countPseudonymsForDomain(String domainName)
	{
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Long> cq = cb.createQuery(Long.class);
		Root<PSN> root = cq.from(PSN.class);
		cq.select(cb.count(root));
		cq.where(cb.equal(root.get(PSN_.key).get(PSNKey_.domain), domainName));
		return em.createQuery(cq).getSingleResult();
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public long countAnonymsForDomain(String domainName)
	{
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Long> cq = cb.createQuery(Long.class);
		Root<PSN> root = cq.from(PSN.class);
		cq.select(cb.count(root));
		Predicate predicate = cb.equal(root.get(PSN_.key).get(PSNKey_.domain), domainName);
		predicate = cb.and(predicate, cb.like(root.get(PSN_.key).get(PSNKey_.originalValue), AnonymDomain.PREFIX + '%' + AnonymDomain.SUFFIX));
		cq.where(predicate);
		return em.createQuery(cq).getSingleResult();
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
	public List<PSN> getPSNObjectsForDomain(String domainName)
	{
		CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
		CriteriaQuery<PSN> criteriaQuery = criteriaBuilder.createQuery(PSN.class);
		Root<PSN> root = criteriaQuery.from(PSN.class);
		Predicate predicate = criteriaBuilder.equal(root.get(PSN_.key).get(PSNKey_.domain), domainName);
		criteriaQuery.select(root).where(predicate);
		return em.createQuery(criteriaQuery).getResultList();
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public long countPSNObjectsForDomains(List<String> domainNames, PaginationConfig config)
	{
		CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
		CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
		Root<PSN> root = criteriaQuery.from(PSN.class);
		criteriaQuery.select(criteriaBuilder.count(root)).where(generateWhereForPSNs(criteriaBuilder, root, domainNames, config));
		return em.createQuery(criteriaQuery).getSingleResult();
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public List<PSN> getPSNObjectsForDomains(List<String> domainNames, PaginationConfig config)
	{
		CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
		CriteriaQuery<PSN> criteriaQuery = criteriaBuilder.createQuery(PSN.class);
		Root<PSN> root = criteriaQuery.from(PSN.class);
		criteriaQuery.select(root).where(generateWhereForPSNs(criteriaBuilder, root, domainNames, config));
		Expression<?> order = generateSortExpressionForPSNs(config.getSortField(), root);
		if (order != null)
		{
			if (config.isSortIsAscending())
			{
				criteriaQuery.orderBy(criteriaBuilder.asc(order));
			}
			else
			{
				criteriaQuery.orderBy(criteriaBuilder.desc(order));
			}
		}
		return em.createQuery(criteriaQuery).setFirstResult(config.getFirstEntry()).setMaxResults(config.getPageSize()).getResultList();
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public List<PSN> getPSNObjectsForDomainPaginated(String domainName, int startPosition, int maxResults)
	{
		CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
		CriteriaQuery<PSN> criteriaQuery = criteriaBuilder.createQuery(PSN.class);
		Root<PSN> root = criteriaQuery.from(PSN.class);
		Predicate predicate = criteriaBuilder.equal(root.get(PSN_.key).get(PSNKey_.domain), domainName);
		criteriaQuery.select(root).where(predicate);
		TypedQuery<PSN> query = em.createQuery(criteriaQuery);
		query.setFirstResult(startPosition);
		query.setMaxResults(maxResults);
		return query.getResultList();
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public List<String> getPseudonymsForDomainPaginated(String domainName, int startPosition, int maxResults)
	{
		CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
		CriteriaQuery<String> criteriaQuery = criteriaBuilder.createQuery(String.class);
		Root<PSN> root = criteriaQuery.from(PSN.class);
		Predicate predicate = criteriaBuilder.equal(root.get(PSN_.key).get(PSNKey_.domain), domainName);
		criteriaQuery.select(root.get(PSN_.pseudonym)).where(predicate);
		TypedQuery<String> query = em.createQuery(criteriaQuery);
		query.setFirstResult(startPosition);
		query.setMaxResults(maxResults);
		List<String> result = query.getResultList();
		em.clear();
		return result;
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public List<String> getExistingPSNs(String domainName, Set<String> psnsToTest)
	{
		CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
		CriteriaQuery<String> criteriaQuery = criteriaBuilder.createQuery(String.class);
		Root<PSN> root = criteriaQuery.from(PSN.class);
		Predicate predicate = criteriaBuilder.equal(root.get(PSN_.key).get(PSNKey_.domain), domainName);
		In<String> inClause = criteriaBuilder.in(root.get(PSN_.pseudonym));
		for (String pseudonym : psnsToTest)
		{
			inClause.value(pseudonym);
		}
		predicate = criteriaBuilder.and(predicate, inClause);
		criteriaQuery.select(root.get(PSN_.pseudonym)).where(predicate);
		TypedQuery<String> query = em.createQuery(criteriaQuery);
		return query.getResultList();
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
	public void addStat(Statistic stat)
	{
		em.persist(stat);
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public Statistic getLatestStat() throws NoResultException
	{
		CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
		CriteriaQuery<Statistic> criteriaQuery = criteriaBuilder.createQuery(Statistic.class);
		Root<Statistic> root = criteriaQuery.from(Statistic.class);
		criteriaQuery.select(root).orderBy(criteriaBuilder.desc(root.get(Statistic_.stat_entry_id)));
		return em.createQuery(criteriaQuery).setMaxResults(1).getSingleResult();
	}

	private Predicate generateWhereForPSNs(CriteriaBuilder criteriaBuilder, Root<PSN> root, List<String> domainNames, PaginationConfig config)
	{
		Predicate predicate = null;

		for (String domainName : domainNames)
		{
			if (predicate == null)
			{
				predicate = criteriaBuilder.equal(root.get(PSN_.key).get(PSNKey_.domain), domainName);
			}
			else
			{
				predicate = criteriaBuilder.or(predicate, criteriaBuilder.equal(root.get(PSN_.key).get(PSNKey_.domain), domainName));
			}
		}

		Predicate orPredicate = null;
		if (!config.getFilter().isEmpty())
		{
			for (Entry<PSNField, String> entry : config.getFilter().entrySet())
			{
				switch (entry.getKey())
				{
					case VALUE:
						if (config.isFilterIsCaseSensitive())
						{
							if (config.isFilterFieldsAreTreatedAsConjunction())
							{
								predicate = criteriaBuilder.and(predicate, criteriaBuilder.like(root.get(PSN_.key).get(PSNKey_.originalValue), PERCENT + entry.getValue() + PERCENT));
							}
							else
							{
								if (orPredicate == null)
								{
									orPredicate = criteriaBuilder.like(root.get(PSN_.key).get(PSNKey_.originalValue), PERCENT + entry.getValue() + PERCENT);
								}
								else
								{
									orPredicate = criteriaBuilder.or(orPredicate, criteriaBuilder.like(root.get(PSN_.key).get(PSNKey_.originalValue), PERCENT + entry.getValue() + PERCENT));
								}
							}
						}
						else
						{
							if (config.isFilterFieldsAreTreatedAsConjunction())
							{
								predicate = criteriaBuilder.and(predicate, criteriaBuilder.like(criteriaBuilder.lower(root.get(PSN_.key).get(PSNKey_.originalValue)),
										criteriaBuilder.lower(criteriaBuilder.literal(PERCENT + entry.getValue() + PERCENT))));
							}
							else
							{
								if (orPredicate == null)
								{
									orPredicate = criteriaBuilder.like(criteriaBuilder.lower(root.get(PSN_.key).get(PSNKey_.originalValue)),
											criteriaBuilder.lower(criteriaBuilder.literal(PERCENT + entry.getValue() + PERCENT)));
								}
								else
								{
									orPredicate = criteriaBuilder.or(orPredicate, criteriaBuilder.like(criteriaBuilder.lower(root.get(PSN_.key).get(PSNKey_.originalValue)),
											criteriaBuilder.lower(criteriaBuilder.literal(PERCENT + entry.getValue() + PERCENT))));
								}
							}
						}
						break;
					case PSEUDONYM:
						if (config.isFilterIsCaseSensitive())
						{
							if (config.isFilterFieldsAreTreatedAsConjunction())
							{
								predicate = criteriaBuilder.and(predicate, criteriaBuilder.like(root.get(PSN_.pseudonym), PERCENT + entry.getValue() + PERCENT));
							}
							else
							{
								if (orPredicate == null)
								{
									orPredicate = criteriaBuilder.like(root.get(PSN_.pseudonym), PERCENT + entry.getValue() + PERCENT);
								}
								else
								{
									orPredicate = criteriaBuilder.or(orPredicate, criteriaBuilder.like(root.get(PSN_.pseudonym), PERCENT + entry.getValue() + PERCENT));
								}
							}
						}
						else
						{
							if (config.isFilterFieldsAreTreatedAsConjunction())
							{
								predicate = criteriaBuilder.and(predicate, criteriaBuilder.like(criteriaBuilder.lower(root.get(PSN_.pseudonym)),
										criteriaBuilder.lower(criteriaBuilder.literal(PERCENT + entry.getValue() + PERCENT))));
							}
							else
							{
								if (orPredicate == null)
								{
									orPredicate = criteriaBuilder.like(criteriaBuilder.lower(root.get(PSN_.pseudonym)),
											criteriaBuilder.lower(criteriaBuilder.literal(PERCENT + entry.getValue() + PERCENT)));
								}
								else
								{
									orPredicate = criteriaBuilder.or(orPredicate, criteriaBuilder.like(criteriaBuilder.lower(root.get(PSN_.pseudonym)),
											criteriaBuilder.lower(criteriaBuilder.literal(PERCENT + entry.getValue() + PERCENT))));
								}
							}
						}
						break;
					case NONE:
						break;
					default:
						LOGGER.fatal("unimplemented PSNField '" + entry.getKey().name() + "' for filter-clause within generateWhereForPSNs()");
						break;
				}
			}
		}
		if (orPredicate != null)
		{
			predicate = criteriaBuilder.and(predicate, orPredicate);
		}

		return predicate;
	}

	private static Path<?> generateSortExpressionForPSNs(PSNField sortField, Root<PSN> root)
	{
		Path<?> order = null;
		if (sortField != null)
		{
			switch (sortField)
			{
				case VALUE:
					order = root.get(PSN_.key).get(PSNKey_.originalValue);
					break;
				case PSEUDONYM:
					order = root.get(PSN_.pseudonym);
					break;
				case NONE:
					break;
				default:
					LOGGER.fatal("unimplemented ConsentField '" + sortField.name() + "' for order-by-clause within generateSortExpressionForConsent()");
					break;
			}
		}
		return order;
	}

	private boolean existsPseudonym(String domainName, String pseudonym) throws InvalidPSNException
	{
		try
		{
			getPSNObjectForPseudonym(domainName, pseudonym);
			return true;
		}
		catch (PSNNotFoundException expected)
		{
			return false;
		}
	}
}
