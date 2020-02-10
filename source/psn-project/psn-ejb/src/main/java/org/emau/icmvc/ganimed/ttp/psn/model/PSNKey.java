package org.emau.icmvc.ganimed.ttp.psn.model;

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

import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 * the persistence class for the compound primary key for the psn table
 * 
 * @author geidell
 * 
 */
@Embeddable
public class PSNKey implements Serializable {

	private static final long serialVersionUID = 8958544740633136680L;
	private String originalValue;
    @Column(name = "domain", insertable = false, updatable = false)
	private String domain;

	public PSNKey() {
	}

	public PSNKey(String originalValue, String domain) {
		this.originalValue = originalValue;
		this.domain = domain;
	}

	public String getOriginValue() {
		return originalValue;
	}

	public String getDomain() {
		return domain;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (domain == null ? 0 : domain.hashCode());
		result = prime * result + (originalValue == null ? 0 : originalValue.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		PSNKey other = (PSNKey) obj;
		if (domain == null) {
			if (other.domain != null) {
				return false;
			}
		} else if (!domain.equals(other.domain)) {
			return false;
		}
		if (originalValue == null) {
			if (other.originalValue != null) {
				return false;
			}
		} else if (!originalValue.equals(other.originalValue)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "pk class for psn with original value '" + originalValue + "' and domain '" + domain + "'";
	}
}
