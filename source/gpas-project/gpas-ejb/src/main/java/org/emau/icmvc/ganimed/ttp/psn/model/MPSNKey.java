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

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public non-sealed class MPSNKey implements PSNKey
{
	@Serial
	private static final long serialVersionUID = -4174150418025624450L;
	private String originalValue;
	private String pseudonym;
	@Column(name = "domain", insertable = false, updatable = false)
	private String domain;

	public MPSNKey()
	{}

	public MPSNKey(String originalValue, String pseudonym, String domain)
	{
		this.originalValue = originalValue;
		this.pseudonym = pseudonym;
		this.domain = domain;
	}

	public String getOriginalValue()
	{
		return originalValue;
	}

	public String getPseudonym()
	{
		return pseudonym;
	}

	public String getDomain()
	{
		return domain;
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o)
			return true;
		if (!(o instanceof MPSNKey psnKey))
			return false;
		return Objects.equals(originalValue, psnKey.originalValue) && Objects.equals(pseudonym, psnKey.pseudonym) && Objects.equals(domain, psnKey.domain);
	}

	@Override public int hashCode()
	{
		return Objects.hash(originalValue, pseudonym, domain);
	}

	@Override
	public String toString()
	{
		return "pk class for multi-psn with original value '" + originalValue + "' and pseudonym '" + pseudonym + "' in domain '" + domain + "'";
	}
}
