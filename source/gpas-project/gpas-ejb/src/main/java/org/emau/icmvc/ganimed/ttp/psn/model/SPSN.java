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

import java.io.Serial;
import java.util.Objects;

import jakarta.persistence.Cacheable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "psn", uniqueConstraints = @UniqueConstraint(columnNames = { "domain", "pseudonym" }, name = "domain_pseudonym"))
@NamedQuery(name = "SPSN.findByValue", query = "select psn from SPSN psn where psn.key.domain = :domainName and psn.key.originalValue = :value")
@NamedQuery(name = "SPSN.findByValues", query = "select psn from SPSN psn where psn.key.domain = :domainName and psn.key.originalValue in :values")
@NamedQuery(name = "SPSN.findByPSN", query = "select psn from SPSN psn where psn.key.domain = :domainName and psn.pseudonym = :psn")
@NamedQuery(name = "SPSN.findByPSNs", query = "select psn from SPSN psn where psn.key.domain = :domainName and psn.pseudonym in :psns")
@NamedQuery(name = "SPSN.deleteByDomain", query = "delete from SPSN psn where psn.key.domain = :domainName")
@Cacheable(false)
public non-sealed class SPSN implements PSN
{
	@Serial
	private static final long serialVersionUID = -7938556142967084276L;
	@EmbeddedId
	private SPSNKey key;
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "domain", referencedColumnName = "name")
	@MapsId("domain")
	private Domain domain;
	private String pseudonym;

	/**
	 * this constructor is only for reflection-based instantiation - do not use in other cases!
	 */
	public SPSN()
	{}

	SPSN(Domain domain, String originalValue, String pseudonym)
	{
		this.key = new SPSNKey(originalValue, domain.getName());
		this.domain = domain;
		this.pseudonym = pseudonym;
	}

	public SPSNKey getKey()
	{
		return key;
	}

	public String getPseudonym()
	{
		return pseudonym;
	}

	public String getOriginalValue()
	{
		return key.getOriginalValue();
	}

	public Domain getDomain()
	{
		return domain;
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o)
			return true;
		if (!(o instanceof SPSN spsn))
			return false;
		return Objects.equals(key, spsn.key);
	}

	@Override
	public int hashCode()
	{
		return Objects.hashCode(key);
	}

	@Override
	public String toString()
	{
		if (key == null)
		{
			return "domain and original value are null for this SPSN object";
		}
		else
		{
			return "SPSN for domain '" + getDomainName() + "', "
					+ "original value '" + getOriginalValue() + "' and "
					+ "pseudonym '" + getPseudonym() + "'";
		}
	}
}
