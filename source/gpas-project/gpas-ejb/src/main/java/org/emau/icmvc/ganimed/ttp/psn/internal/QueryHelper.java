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
import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.metamodel.SingularAttribute;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.emau.icmvc.ganimed.ttp.psn.config.PSNField;
import org.emau.icmvc.ganimed.ttp.psn.config.PaginationConfig;
import org.emau.icmvc.ganimed.ttp.psn.model.Domain;
import org.emau.icmvc.ganimed.ttp.psn.model.MPSN;
import org.emau.icmvc.ganimed.ttp.psn.model.MPSNKey;
import org.emau.icmvc.ganimed.ttp.psn.model.MPSNKey_;
import org.emau.icmvc.ganimed.ttp.psn.model.MPSN_;
import org.emau.icmvc.ganimed.ttp.psn.model.PSN;
import org.emau.icmvc.ganimed.ttp.psn.model.PSNKey;
import org.emau.icmvc.ganimed.ttp.psn.model.SPSN;
import org.emau.icmvc.ganimed.ttp.psn.model.SPSNKey;
import org.emau.icmvc.ganimed.ttp.psn.model.SPSNKey_;
import org.emau.icmvc.ganimed.ttp.psn.model.SPSN_;

/**
 * Auxiliary class that forwards PSN queries for sPSN or mPSN domains to the relevant tables.
 *
 * @param psnClass the PSN class, either {@link SPSN} or {@link MPSN}
 * @param psnKeyClass the PSN key class, either {@link SPSNKey} or {@link MPSNKey}
 * @param keyAttr the PSN key attribute, either {@link SPSN_.key} or {@link MPSN_.key}
 * @param domainAttr the PSN domain attribute, either {@link SPSNKey_.domain} or {@link MPSNKey_.domain}
 * @param valueAttr the PSN value attribute, either {@link SPSNKey_.originalValue} or {@link MPSNKey_.originalValue}
 * @param spsnAttr the SPSN pseudonym attribute, either {@link SPSN_.pseudonym} or {@link null}
 * @param mpsnAttr the MPSN pseudonym attribute, either {@link MPSNKey_.pseudonym} or {@link null}
 * @param <P> the PSN type, either {@link SPSN} or {@link MPSN}
 * @param <PK> the PSN key type^, either {@link SPSNKey} or {@link MPSNKey}
 */
record QueryHelper<P extends PSN, PK extends PSNKey> (
		Class<P> psnClass,
		Class<PK> psnKeyClass,
		SingularAttribute<P, PK> keyAttr,
		SingularAttribute<PK, String> domainAttr,
		SingularAttribute<PK, String> valueAttr,
		SingularAttribute<P, String> spsnAttr,
		SingularAttribute<PK, String> mpsnAttr)
{
	private static final String PARAM_DOMAIN_NAME = "domainName";
	private static final String PARAM_PSN = "psn";
	private static final String PARAM_PSNS = "psns";
	private static final String PARAM_VALUE = "value";
	private static final String PARAM_VALUES = "values";

	private static final QueryHelper<SPSN, SPSNKey> SINGLE = new QueryHelper<>(
			SPSN.class, SPSNKey.class, SPSN_.key, SPSNKey_.domain, SPSNKey_.originalValue, SPSN_.pseudonym, null);
	private static final QueryHelper<MPSN, MPSNKey> MULTI = new QueryHelper<>(
			MPSN.class, MPSNKey.class, MPSN_.key, MPSNKey_.domain, MPSNKey_.originalValue, null, MPSNKey_.pseudonym);

	/**
	 * {@return the relevant query helper for the given domain}
	 * @param domain the domain to return a helper for
	 */
	public static QueryHelper<? extends PSN, ? extends PSNKey> helperFor(Domain domain)
	{
		return MULTI.isMatchingDomain(domain) ? MULTI : SINGLE;
	}

	/**
	 * {@return the relevant query helper for sPSN domains}
	 */
	public static QueryHelper<SPSN, SPSNKey> helperForSPSN()
	{
		return SINGLE;
	}

	/**
	 * {@return the relevant query helper for mPSN domains}
	 */
	public static QueryHelper<MPSN, MPSNKey> helperForMPSN()
	{
		return MULTI;
	}

	private Logger logger()
	{
		return LogManager.getLogger(getClass());
	}

	private Path<String> domainPath(Root<P> root)
	{
		return root.get(keyAttr).get(domainAttr);
	}

	private Path<String> valuePath(Root<P> root)
	{
		return root.get(keyAttr).get(valueAttr);
	}

	private Path<String> pseudonymPath(Root<P> root)
	{
		return spsnAttr != null ? root.get(spsnAttr) : root.get(keyAttr).get(mpsnAttr);
	}

	/**
	 * {@return true if this is a helper for mPSN domains}
	 */
	public boolean isMPSNHelper()
	{
		return psnClass.equals(MPSN.class);
	}

	/**
	 * {@return true if this is a helper for the given domain}
	 * @param domain the domain to test
	 */
	public boolean isMatchingDomain(Domain domain)
	{
		return domain.getConfig().isMultiPsnDomain() ? isMPSNHelper() : psnClass.equals(SPSN.class);
	}

	/**
	 * {@return the names of the given domains, for which this class is a matching helper for}
	 * @param domains the domains to filter
	 */
	public List<String> filterMatchingDomainNames(List<Domain> domains)
	{
		return domains.stream().filter(this::isMatchingDomain).map(Domain::getName).toList();
	}

	public List<PSN> queryAllPSNObjectsForValuePrefix(EntityManager em, Domain domain, String valuePrefix)
	{
		Query<P> q = new Query<>(em, psnClass);
		Predicate predicate = q.cb.and(q.cb.equal(q.domainPath, domain.getName()),
				q.cb.like(q.valuePath, valuePrefix + '%'));
		q.cq.select(q.root).where(predicate);
		return new ArrayList<>(em.createQuery(q.cq).getResultList());
	}

	public List<PSN> queryPSNObjectsForDomain(EntityManager em, Domain domain)
	{
		Query<P> q = new Query<>(em, psnClass);
		Predicate predicate = q.cb.equal(q.domainPath, domain.getName());
		q.cq.select(q.root).where(predicate);
		return new ArrayList<>(em.createQuery(q.cq).getResultList());
	}

	public List<PSN> queryPSNObjectsForDomains(EntityManager em, List<Domain> domains, PaginationConfig config)
	{
		if (domains == null)
		{
			return List.of();
		}
		List<String> domainNames = filterMatchingDomainNames(domains);
		if (domainNames.isEmpty())
		{
			return List.of();
		}
		Query<P> q = new Query<>(em, psnClass);
		q.cq.select(q.root).where(generateWhereForPSNs(q.cb, q.root, domainNames, config));
		Expression<?> order = generateSortExpressionForPSNs(config.getSortField(), q.root);
		if (order != null)
		{
			if (config.isSortIsAscending())
			{
				q.cq.orderBy(q.cb.asc(order));
			}
			else
			{
				q.cq.orderBy(q.cb.desc(order));
			}
		}
		return new ArrayList<>(em.createQuery(q.cq).setFirstResult(config.getFirstEntry()).setMaxResults(config.getPageSize()).getResultList());
	}

	public List<PSN> queryPSNObjectsForDomainPaginated(EntityManager em, Domain domain, int maxResults, String startAfterOrigValue)
	{
		Query<P> q = new Query<>(em, psnClass);
		Predicate predicate = q.cb.equal(q.domainPath, domain.getName());
		// beschleunigung
		if (startAfterOrigValue != null)
		{
			predicate = q.cb.and(predicate, q.cb.greaterThan(q.valuePath, startAfterOrigValue));
		}
		q.cq.select(q.root).where(predicate).orderBy(q.cb.asc(q.valuePath));
		TypedQuery<P> query = em.createQuery(q.cq);
		query.setMaxResults(maxResults);
		return new ArrayList<>(query.getResultList());
	}

	public List<String> queryPseudonymsForDomainPaginated(EntityManager em, Domain domain, int maxResults, String startAfterPseudonym)
	{
		Query<String> q = new Query<>(em, String.class);
		Predicate predicate = q.cb.equal(q.domainPath, domain.getName());
		// beschleunigung
		if (startAfterPseudonym != null)
		{
			predicate = q.cb.and(predicate, q.cb.greaterThan(q.pseudonymPath, startAfterPseudonym));
		}
		q.cq.select(q.pseudonymPath).where(predicate).orderBy(q.cb.asc(q.pseudonymPath));
		TypedQuery<String> query = em.createQuery(q.cq);
		query.setMaxResults(maxResults);
		List<String> result = query.getResultList();
		em.clear();
		return result;
	}

	public List<String> queryExistingPSNs(EntityManager em, Domain domain, Set<String> psns)
	{
		Query<String> q = new Query<>(em, String.class);
		Predicate predicate = q.cb.equal(q.domainPath, domain.getName());
		CriteriaBuilder.In<String> inClause = q.cb.in(q.pseudonymPath);
		psns.forEach(inClause::value);
		predicate = q.cb.and(predicate, inClause);
		q.cq.select(q.pseudonymPath).where(predicate);
		TypedQuery<String> query = em.createQuery(q.cq);
		return query.getResultList();
	}

	public long countPSNObjectsForDomains(EntityManager em, List<Domain> domains, PaginationConfig config)
	{
		if (domains == null)
		{
			return 0;
		}
		List<String> domainNames = filterMatchingDomainNames(domains);
		if (domainNames.isEmpty())
		{
			return 0;
		}
		Query<Long> q = new Query<>(em, Long.class);
		q.cq.select(q.cb.count(q.root)).where(generateWhereForPSNs(q.cb, q.root, domainNames, config));
		return em.createQuery(q.cq).getSingleResult();
	}

	public long countPseudonymsForDomain(EntityManager em, Domain domain)
	{
		Query<Long> q = new Query<>(em, Long.class);
		q.cq.select(q.cb.count(q.root));
		q.cq.where(q.cb.equal(q.domainPath, domain.getName()));
		return em.createQuery(q.cq).getSingleResult();
	}

	public long countAnonymsForDomain(EntityManager em, Domain domain)
	{
		Query<Long> q = new Query<>(em, Long.class);
		q.cq.select(q.cb.count(q.root));
		Predicate predicate = q.cb.equal(q.domainPath, AnonymDomain.NAME);
		predicate = q.cb.and(predicate, q.cb.like(q.valuePath, domain.getName() + AnonymDomain.DELIMITER + '%'));
		q.cq.where(predicate);
		return em.createQuery(q.cq).getSingleResult();
	}

	public List<PSN> findByPSN(EntityManager em, Domain domain, String pseudonym)
	{
		return PSN.createFindByPSNQuery(em, psnClass)
				.setParameter(PARAM_DOMAIN_NAME, domain.getName())
				.setParameter(PARAM_PSN, pseudonym)
				.getResultList();
	}

	public List<PSN> findByPSNs(EntityManager em, Domain domain, Set<String> psns)
	{
		if (psns == null || psns.isEmpty())
		{
			return List.of();
		}
		return PSN.createFindByPSNsQuery(em, psnClass)
				.setParameter(PARAM_DOMAIN_NAME, domain.getName())
				.setParameter(PARAM_PSNS, psns)
				.getResultList();
	}

	public List<PSN> findByValue(EntityManager em, Domain domain, String value)
	{
		if (isMPSNHelper())
		{
			return em.createNamedQuery("MPSN.findByValue", PSN.class)
					.setParameter(PARAM_DOMAIN_NAME, domain.getName())
					.setParameter(PARAM_VALUE, value)
					.getResultList();
		}
		else
		{
			SPSN spsn = em.find(SPSN.class, new SPSNKey(value, domain.getName()));
			return spsn != null ? List.of(spsn) : List.of();
		}
	}

	public List<PSN> findByValues(EntityManager em, Domain domain, Set<String> values)
	{
		if (values == null || values.isEmpty())
		{
			return List.of();
		}
		return PSN.createFindByValuesQuery(em, psnClass)
				.setParameter(PARAM_DOMAIN_NAME, domain.getName())
				.setParameter(PARAM_VALUES, values).getResultList();
	}

	public void deleteByDomain(EntityManager em, Domain domain)
	{
		PSN.createDeleteByDomainQuery(em, psnClass).setParameter(PARAM_DOMAIN_NAME, domain.getName()).executeUpdate();
		em.flush();
	}

	private Predicate generateWhereForPSNs(CriteriaBuilder cb, Root<P> root, List<String> domainNames, PaginationConfig config)
	{
		if (domainNames == null || domainNames.isEmpty())
		{
			return cb.disjunction();
		}

		Predicate predicate = null;

		for (String domainName : domainNames)
		{
			if (predicate == null)
			{
				predicate = cb.equal(domainPath(root), domainName);
			}
			else
			{
				predicate = cb.or(predicate, cb.equal(domainPath(root), domainName));
			}
		}

		Predicate orPredicate = null;
		if (!config.getFilter().isEmpty())
		{
			boolean asConjunction = config.isFilterFieldsAreTreatedAsConjunction();
			boolean caseSensitive = config.isFilterIsCaseSensitive();

			for (Map.Entry<PSNField, String> entry : config.getFilter().entrySet())
			{
				switch (entry.getKey())
				{
					case VALUE:
					{
						Predicate p = generateFilterPredicate(cb, valuePath(root), entry.getValue(),
								asConjunction ? predicate : orPredicate, caseSensitive, asConjunction, config.getMatchMode());

						if (asConjunction)
						{
							predicate = p;
						}
						else
						{
							orPredicate = p;
						}
					}
					break;
					case PSEUDONYM:
					{
						Predicate p = generateFilterPredicate(cb, pseudonymPath(root), entry.getValue(),
								asConjunction ? predicate : orPredicate, caseSensitive, asConjunction, config.getMatchMode());

						if (asConjunction)
						{
							predicate = p;
						}
						else
						{
							orPredicate = p;
						}
					}
					break;
					case NONE:
						break;
					default:
						logger().fatal("unimplemented PSNField '{}' for filter-clause within generateWhereForPSNs()", entry.getKey().name());
						break;
				}
			}
		}

		if (orPredicate != null)
		{
			predicate = cb.and(predicate, orPredicate);
		}
		return predicate;
	}

	private Predicate generateFilterPredicate(CriteriaBuilder cb, Path<String> path, String value,
			Predicate predicate, boolean caseSensitive, boolean asConjunction, PaginationConfig.MatchMode matchMode)
	{
		return switch (matchMode)
		{
			case CONTAINS -> generateLikePredicate(cb, path, value, predicate, caseSensitive, asConjunction);
			case EQUALS -> generateEqualsPredicate(cb, path, value, predicate, caseSensitive, asConjunction);
		};
	}

	private Predicate generateLikePredicate(CriteriaBuilder cb, Path<String> path, String value,
			Predicate predicate, boolean caseSensitive, boolean asConjunction)
	{
		if (predicate == null)
		{
			predicate = asConjunction ? cb.conjunction() : cb.disjunction();
		}

		String pattern = "%" + value + "%";

		if (caseSensitive)
		{
			return link(cb, asConjunction, predicate, cb.like(path, pattern));
		}
		else
		{
			return link(cb, asConjunction, predicate, cb.like(cb.lower(path), cb.lower(cb.literal(pattern))));
		}
	}

	private Predicate generateEqualsPredicate(CriteriaBuilder cb, Path<String> path, String value,
			Predicate predicate, boolean caseSensitive, boolean asConjunction)
	{
		if (predicate == null)
		{
			predicate = asConjunction ? cb.conjunction() : cb.disjunction();
		}

		if (caseSensitive)
		{
			return link(cb, asConjunction, predicate, cb.equal(path, value));
		}
		else
		{
			return link(cb, asConjunction, predicate, cb.equal(cb.lower(path), cb.lower(cb.literal(value))));
		}
	}

	private Path<?> generateSortExpressionForPSNs(PSNField sortField, Root<P> root)
	{
		Path<?> order = null;
		if (sortField != null)
		{
			switch (sortField)
			{
				case VALUE:
					order = valuePath(root);
					break;
				case PSEUDONYM:
					order = pseudonymPath(root);
					break;
				case NONE:
					break;
				default:
					logger().fatal("unimplemented ConsentField '{}' for order-by-clause within generateSortExpressionForConsent()", sortField.name());
					break;
			}
		}
		return order;
	}

	private Predicate link(CriteriaBuilder cb, boolean asConjunction, Predicate p1, Predicate p2)
	{
		return asConjunction ? cb.and(p1, p2) : cb.or(p1, p2);
	}

	private class Query<Q>
	{
		final CriteriaBuilder cb;
		final CriteriaQuery<Q> cq;
		final Root<P> root;
		final Path<String> domainPath;
		final Path<String> valuePath;
		final Path<String> pseudonymPath;

		Query(EntityManager em, Class<Q> queryClass)
		{
			cb = em.getCriteriaBuilder();
			cq = cb.createQuery(queryClass);
			root = cq.from(psnClass);
			domainPath = domainPath(root);
			valuePath = valuePath(root);
			pseudonymPath = pseudonymPath(root);
		}
	}
}