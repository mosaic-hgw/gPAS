package org.emau.icmvc.ganimed.ttp.psn.dto;

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

import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class PSNNetNodeDTO extends PSNTreeDTO
{
	private static final long serialVersionUID = -7523114235898808862L;
	private final Set<PSNDTO> circleChildren = new HashSet<PSNDTO>();

	public PSNNetNodeDTO()
	{}

	public PSNNetNodeDTO(String domain, String value, String pseudonym, int level)
	{
		super(domain, value, pseudonym, level);
	}

	public Set<PSNDTO> getCircleChildren()
	{
		return circleChildren;
	}

	public PSNDTO toPSNDTO()
	{
		return new PSNDTO(getDomainName(), getOriginalValue(), getPseudonym());
	}

	@Override
	public String toString()
	{
		return "PSNNetNodeDTO [including: " + super.toString() + " and " + circleChildren.size() + " circle children]";
	}
}
