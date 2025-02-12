package org.emau.icmvc.ganimed.ttp.psn.model;
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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import org.emau.icmvc.ganimed.ttp.psn.dto.PSNDTO;

public sealed interface PSN extends Serializable permits SPSN, MPSN
{
	/**
	 * Default factory method for creating an PSN object,
	 * depending on whether the domain is configured as a multi-PSN or single-PSN domain.
	 *
	 * @param domain the domain
	 * @param originalValue the original value
	 * @param pseudonym the pseudonym
	 * @return the PSN object
	 */
	static PSN create(Domain domain, String originalValue, String pseudonym)
	{
		return create(domain, originalValue, pseudonym, domain.getConfig().isMultiPsnDomain());
	}

	/**
	 * Default factory method for creating PSN objects,
	 * depending on whether the domain is configured as a multi-PSN or single-PSN domain.
	 *
	 * @param domain the domain
	 * @param psnPairs the map with psn pairs
	 * @return the list of PSN object
	 */
	static List<PSN> create(Domain domain, Map<String, String> psnPairs)
	{
		boolean multiPsnDomain = domain.getConfig().isMultiPsnDomain();
		List<PSN> psns = new ArrayList<>(psnPairs.size());
		psnPairs.forEach((key, value) -> psns.add(create(domain, key, value, multiPsnDomain)));
		return psns;
	}

	static PSN create(Domain domain, String originalValue, String pseudonym, boolean multiPsnDomain)
	{
		return multiPsnDomain ?
				new MPSN(domain, originalValue, pseudonym) :
				new SPSN(domain, originalValue, pseudonym);
	}

	String getPseudonym();

	String getOriginalValue();

	Domain getDomain();

	PSNKey getKey();

	default String getDomainName()
	{
		return getDomain().getName();
	}

	default PSNDTO toPSNDTO()
	{
		return new PSNDTO(getDomain().getName(), getOriginalValue(), getPseudonym());
	}

	static TypedQuery<PSN> createFindByPSNQuery(EntityManager em, Class<? extends PSN> psnClass)
	{
		return isMPSNClass(psnClass) ?
				em.createNamedQuery("MPSN.findByPSN", PSN.class) :
				em.createNamedQuery("SPSN.findByPSN", PSN.class);
	}

	static TypedQuery<PSN> createFindByPSNsQuery(EntityManager em, Class<? extends PSN> psnClass)
	{
		return isMPSNClass(psnClass) ?
				em.createNamedQuery("MPSN.findByPSNs", PSN.class) :
				em.createNamedQuery("SPSN.findByPSNs", PSN.class);
	}

	static TypedQuery<PSN> createFindByValuesQuery(EntityManager em, Class<? extends PSN> psnClass)
	{
		return isMPSNClass(psnClass) ?
				em.createNamedQuery("MPSN.findByValues", PSN.class) :
				em.createNamedQuery("SPSN.findByValues", PSN.class);
	}

	static Query createDeleteByDomainQuery(EntityManager em, Class<? extends PSN> psnClass)
	{
		return isMPSNClass(psnClass) ?
				em.createNamedQuery("MPSN.deleteByDomain") :
				em.createNamedQuery("SPSN.deleteByDomain");
	}

	private static boolean isMPSNClass(Class<? extends PSN> psnClass)
	{
		return psnClass.equals(MPSN.class);
	}
}
