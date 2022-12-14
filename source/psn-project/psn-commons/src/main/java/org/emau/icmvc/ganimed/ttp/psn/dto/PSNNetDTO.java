package org.emau.icmvc.ganimed.ttp.psn.dto;

/*-
 * ###license-information-start###
 * gPAS - a Generic Pseudonym Administration Service
 * __
 * Copyright (C) 2013 - 2022 Independent Trusted Third Party of the University Medicine Greifswald
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
import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.emau.icmvc.ganimed.ttp.psn.exceptions.PSNNotFoundException;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class PSNNetDTO implements Serializable
{
	private static final long serialVersionUID = -4871476057536682234L;
	public static final String IMAGINARY_ROOT_DOMAIN = "IMAGINARY_ROOT_DOMAIN";
	private final PSNNetNodeDTO root = new PSNNetNodeDTO(IMAGINARY_ROOT_DOMAIN, "", "", -1);
	private final Set<PSNNetNodeDTO> nodes = new HashSet<PSNNetNodeDTO>();

	public PSNNetDTO()
	{
		nodes.add(root);
	}

	public Set<PSNNetNodeDTO> getNodes()
	{
		return nodes;
	}

	public PSNNetNodeDTO getNodeFor(PSNDTO psnDTO) throws PSNNotFoundException
	{
		for (PSNNetNodeDTO node : nodes)
		{
			if (node.getDomainName().equals(psnDTO.getDomainName()) && node.getOriginalValue().equals(psnDTO.getOriginalValue())
					&& node.getPseudonym().equals(psnDTO.getPseudonym()))
			{
				return node;
			}
		}
		throw new PSNNotFoundException(psnDTO + " is no node of this net");
	}

	public PSNNetNodeDTO getRoot()
	{
		return root;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + nodes.hashCode();
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PSNNetDTO other = (PSNNetDTO) obj;
		if (!nodes.equals(other.nodes))
			return false;
		return true;
	}

	@Override
	public String toString()
	{
		return "PSNNetDTO [with " + nodes.size() + " nodes]";
	}
}
