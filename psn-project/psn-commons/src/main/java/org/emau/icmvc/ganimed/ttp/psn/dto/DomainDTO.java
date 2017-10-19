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

/**
 * numberOfPseudonyms - how many pseudonyms are generated for that domain<br/>
 * 
 * @author geidell
 * 
 */
public class DomainDTO extends DomainLightDTO implements Serializable {

	private static final long serialVersionUID = 6153699547859545098L;
	private long numberOfPseudonyms;

	public DomainDTO() {
	}

	public DomainDTO(String domain, String checkDigitClass, String alphabet, String properties, String comment, long numberOfPseudonyms,
			String parentDomain) {
		setDomain(domain);
		setCheckDigitClass(checkDigitClass);
		setAlphabet(alphabet);
		setProperties(properties);
		setComment(comment);
		setParentDomain(parentDomain);
		this.numberOfPseudonyms = numberOfPseudonyms;
	}

	public DomainDTO(DomainLightDTO lightDTO, long numberOfPseudonyms) {
		setDomain(lightDTO.getDomain());
		setCheckDigitClass(lightDTO.getCheckDigitClass());
		setAlphabet(lightDTO.getAlphabet());
		setProperties(lightDTO.getProperties());
		setComment(lightDTO.getComment());
		setParentDomain(lightDTO.getParentDomain());
		this.numberOfPseudonyms = numberOfPseudonyms;
	}

	/**
	 * @return how many pseudonyms are generated for that domain
	 */
	public long getNumberOfPseudonyms() {
		return numberOfPseudonyms;
	}

	/**
	 * @param numberOfPseudonyms
	 *            how many pseudonyms are generated for that domain
	 */
	public void setNumberOfPseudonyms(long numberOfPseudonyms) {
		this.numberOfPseudonyms = numberOfPseudonyms;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + (int) (numberOfPseudonyms ^ (numberOfPseudonyms >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		DomainDTO other = (DomainDTO) obj;
		if (numberOfPseudonyms != other.numberOfPseudonyms)
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(super.toString());
		sb.append(" there are ");
		sb.append(numberOfPseudonyms);
		sb.append(" pseudonyms bound to that domain.");
		return sb.toString();
	}
}
