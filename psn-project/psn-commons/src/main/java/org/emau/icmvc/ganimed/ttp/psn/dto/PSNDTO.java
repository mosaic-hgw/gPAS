package org.emau.icmvc.ganimed.ttp.psn.dto;

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

import java.io.Serializable;

import javax.xml.bind.annotation.XmlElement;

/**
 * dto for pseudonyms
 * 
 * @author geidell
 * 
 */
public class PSNDTO implements Serializable {

	private static final long serialVersionUID = 2948724166721271521L;
	private String domain;
	private String originalValue;
	private String pseudonym;

	public PSNDTO() {
	}

	public PSNDTO(String domain, String originalValue, String pseudonym) {
		super();
		this.domain = domain;
		this.originalValue = originalValue;
		this.pseudonym = pseudonym;
	}

	/**
	 * @return identifier of the parent domain
	 */
	@XmlElement(required = true)
	public String getDomain() {
		return domain;
	}

	/**
	 * @param domain
	 *            identifier of the parent domain
	 */
	public void setDomain(String domain) {
		this.domain = domain;
	}

	@XmlElement(required = true)
	public String getOriginalValue() {
		return originalValue;
	}

	public void setOriginalValue(String originalValue) {
		this.originalValue = originalValue;
	}

	@XmlElement(required = true)
	public String getPseudonym() {
		return pseudonym;
	}

	public void setPseudonym(String pseudonym) {
		this.pseudonym = pseudonym;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((domain == null) ? 0 : domain.hashCode());
		result = prime * result + ((originalValue == null) ? 0 : originalValue.hashCode());
		result = prime * result + ((pseudonym == null) ? 0 : pseudonym.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PSNDTO other = (PSNDTO) obj;
		if (domain == null) {
			if (other.domain != null)
				return false;
		} else if (!domain.equals(other.domain))
			return false;
		if (originalValue == null) {
			if (other.originalValue != null)
				return false;
		} else if (!originalValue.equals(other.originalValue))
			return false;
		if (pseudonym == null) {
			if (other.pseudonym != null)
				return false;
		} else if (!pseudonym.equals(other.pseudonym))
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("pseudonym for domain '");
		sb.append(domain);
		sb.append("': original value = '");
		sb.append(originalValue);
		sb.append("' -> pseudonym = '");
		sb.append(pseudonym);
		sb.append("'");
		return sb.toString();
	}
}
