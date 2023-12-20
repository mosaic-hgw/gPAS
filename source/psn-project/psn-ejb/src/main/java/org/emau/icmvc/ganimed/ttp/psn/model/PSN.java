package org.emau.icmvc.ganimed.ttp.psn.model;
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

import java.io.Serializable;

import javax.persistence.Cacheable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.emau.icmvc.ganimed.ttp.psn.dto.PSNDTO;

@Entity
@Table(name = "psn", uniqueConstraints = @UniqueConstraint(columnNames = { "domain", "pseudonym" }, name = "domain_pseudonym"))
@NamedQueries({
		@NamedQuery(name = "PSN.findByValues", query = "select psn from PSN psn where psn.key.domain = :domainName and psn.key.originalValue in :values"),
		@NamedQuery(name = "PSN.findByPSNs", query = "select psn from PSN psn where psn.key.domain = :domainName and psn.pseudonym in :psns"),
		@NamedQuery(name = "PSN.deleteByDomain", query = "delete from PSN psn where psn.key.domain = :domainName") })
@Cacheable(false)
public class PSN implements Serializable
{
	private static final long serialVersionUID = -8436376242959982100L;
	@EmbeddedId
	private PSNKey key;
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "domain", referencedColumnName = "name")
	@MapsId("domain")
	private Domain domain;
	private String pseudonym;

	/**
	 * this constructor is only for reflection-based instantiation - do not use in other cases!
	 */
	public PSN()
	{}

	public PSN(Domain domain, String originalValue, String pseudonym)
	{
		super();
		this.key = new PSNKey(originalValue, domain.getName());
		this.pseudonym = pseudonym;
		this.domain = domain;
	}

	public PSNKey getKey()
	{
		return key;
	}

	public String getPseudonym()
	{
		return pseudonym;
	}

	public Domain getDomain()
	{
		return domain;
	}

	public PSNDTO toPSNDTO()
	{
		return new PSNDTO(key.getDomain(), key.getOriginValue(), pseudonym);
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + (key == null ? 0 : key.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		if (obj == null)
		{
			return false;
		}
		if (getClass() != obj.getClass())
		{
			return false;
		}
		PSN other = (PSN) obj;
		if (key == null)
		{
			if (other.key != null)
			{
				return false;
			}
		}
		else if (!key.equals(other.key))
		{
			return false;
		}
		return true;
	}

	@Override
	public String toString()
	{
		String result;
		if (key == null)
		{
			result = "domain and original value are null for this PSN object";
		}
		else
		{
			StringBuilder sb = new StringBuilder();
			sb.append("PSN for domain '");
			sb.append(key.getDomain());
			sb.append("' and original value '");
			sb.append(key.getOriginValue());
			sb.append("'");
			result = sb.toString();
		}
		return result;
	}
}
