package org.emau.icmvc.ganimed.ttp.psn.dto;

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
import java.io.Serializable;
import java.util.Objects;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class PSNDTO implements Serializable
{
	@Serial
	private static final long serialVersionUID = 207762696248224483L;
	private String domainName;
	private String originalValue;
	private String pseudonym;

	public PSNDTO()
	{}

	public PSNDTO(String domainName, String originalValue, String pseudonym)
	{
		this.domainName = domainName;
		this.originalValue = originalValue;
		this.pseudonym = pseudonym;
	}

	/**
	 * @return identifier of the parent domain
	 */
	public String getDomainName()
	{
		return domainName;
	}

	/**
	 * @param domainName
	 *            identifier of the parent domain
	 */
	public void setDomainName(String domainName)
	{
		this.domainName = domainName;
	}

	public String getOriginalValue()
	{
		return originalValue;
	}

	public void setOriginalValue(String originalValue)
	{
		this.originalValue = originalValue;
	}

	public String getPseudonym()
	{
		return pseudonym;
	}

	public void setPseudonym(String pseudonym)
	{
		this.pseudonym = pseudonym;
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o)
			return true;
		if (!(o instanceof PSNDTO psndto))
			return false;
		return  Objects.equals(domainName, psndto.domainName) &&
				Objects.equals(originalValue, psndto.originalValue) &&
				Objects.equals(pseudonym, psndto.pseudonym);
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(domainName, originalValue, pseudonym);
	}

	@Override
	public String toString()
	{
		return "pseudonym for domain '" + domainName
				+ "': original value = '" + originalValue
				+ "' -> pseudonym = '" + pseudonym + "'";
	}
}
