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

import org.emau.icmvc.ganimed.ttp.psn.generator.GeneratorProperties;

/**
 * domain - identifier<br/>
 * checkDigitClass - generator class for check digits; for possible values see the docu :-)<br/>
 * alphabet - all chars which could be used for the pseudonyms; could be any class derived from org.emau.icmvc.ganimed.ttp.psn.alphabets<br>
 * or a comma separated list of chars<br/>
 * properties - semicolon separated key-value pairs; for valid keys see enum {@link GeneratorProperties}<br/>
 * comment - comment<br/>
 * 
 * @author geidell
 * 
 */
public class DomainLightDTO implements Serializable {

	private static final long serialVersionUID = -7598724294004880906L;
	private String domain;
	private String checkDigitClass;
	private String alphabet;
	private String properties;
	private String comment;
	private String parentDomain;

	public DomainLightDTO() {
	}

	public DomainLightDTO(String domain, String checkDigitClass, String alphabet, String properties, String comment,
			String parentDomain) {
		this.domain = domain;
		this.checkDigitClass = checkDigitClass;
		this.alphabet = alphabet;
		this.properties = properties;
		this.comment = comment;
		this.parentDomain = parentDomain;
	}

	/**
	 * @return identifier
	 */
	@XmlElement(required = true)
	public String getDomain() {
		return domain;
	}

	/**
	 * @param domain
	 *            identifier
	 */
	public void setDomain(String domain) {
		this.domain = domain;
	}

	/**
	 * @return generator class for check digits
	 */
	@XmlElement(required = true)
	public String getCheckDigitClass() {
		return checkDigitClass;
	}

	/**
	 * @param checkDigitClass
	 *            generator class for check digits
	 */
	public void setCheckDigitClass(String checkDigitClass) {
		this.checkDigitClass = checkDigitClass;
	}

	/**
	 * @return all chars which could be used for the pseudonyms; could be any class derived from org.emau.icmvc.ganimed.ttp.psn.alphabets<br>
	 *         or a comma separated list of chars
	 */
	@XmlElement(required = true)
	public String getAlphabet() {
		return alphabet;
	}

	/**
	 * @param alphabet
	 *            all chars which could be used for the pseudonyms; could be any class derived from org.emau.icmvc.ganimed.ttp.psn.alphabets<br>
	 *            or a comma separated list of chars
	 */
	public void setAlphabet(String alphabet) {
		this.alphabet = alphabet;
	}

	/**
	 * @return semicolon separated key-value pairs; for valid keys see enum {@link GeneratorProperties}
	 */
	public String getProperties() {
		return properties;
	}

	/**
	 * @param properties
	 *            semicolon separated key-value pairs; for valid keys see enum {@link GeneratorProperties}
	 */
	public void setProperties(String properties) {
		this.properties = properties;
	}

	/**
	 * @return comment
	 */
	public String getComment() {
		return comment;
	}

	/**
	 * @param comment
	 *            comment
	 */
	public void setComment(String comment) {
		this.comment = comment;
	}

	/**
	 * @return parentDomain
	 */
	public String getParentDomain() {
		return parentDomain;
	}

	/**
	 * @param parentDomain
	 */
	public void setParentDomain(String parentDomain) {
		this.parentDomain = parentDomain;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((alphabet == null) ? 0 : alphabet.hashCode());
		result = prime * result + ((checkDigitClass == null) ? 0 : checkDigitClass.hashCode());
		result = prime * result + ((comment == null) ? 0 : comment.hashCode());
		result = prime * result + ((domain == null) ? 0 : domain.hashCode());
		result = prime * result + ((parentDomain == null) ? 0 : parentDomain.hashCode());
		result = prime * result + ((properties == null) ? 0 : properties.hashCode());
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
		DomainLightDTO other = (DomainLightDTO) obj;
		if (alphabet == null) {
			if (other.alphabet != null)
				return false;
		} else if (!alphabet.equals(other.alphabet))
			return false;
		if (checkDigitClass == null) {
			if (other.checkDigitClass != null)
				return false;
		} else if (!checkDigitClass.equals(other.checkDigitClass))
			return false;
		if (comment == null) {
			if (other.comment != null)
				return false;
		} else if (!comment.equals(other.comment))
			return false;
		if (domain == null) {
			if (other.domain != null)
				return false;
		} else if (!domain.equals(other.domain))
			return false;
		if (parentDomain == null) {
			if (other.parentDomain != null)
				return false;
		} else if (!parentDomain.equals(other.parentDomain))
			return false;
		if (properties == null) {
			if (other.properties != null)
				return false;
		} else if (!properties.equals(other.properties))
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("psn-project for domain '");
		sb.append(domain);
		sb.append("' is configured with: alphabet = ");
		sb.append(alphabet);
		sb.append(", check digit generator class = ");
		sb.append(checkDigitClass);
		sb.append(", comment = '");
		sb.append(comment);
		sb.append("' and the following properties: '");
		sb.append(properties);
		sb.append("'. parent domain is: ");
		sb.append(parentDomain);
		return sb.toString();
	}
}
