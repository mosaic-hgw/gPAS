package org.emau.icmvc.ganimed.ttp.psn.internal;

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
import java.util.ArrayList;
import java.util.List;

import org.emau.icmvc.ganimed.ttp.psn.dto.PSNTreeDTO;

/**
 * Node of a psn tree that can be the root node or a child node each node can contain a number of child nodes
 * 
 * @author Robert Wolff
 *
 */
public class PSNTreeNode implements Serializable {

	private static final long serialVersionUID = 8793711916344280259L;
	private String domain;
	private String value;
	private List<PSNTreeNode> children = new ArrayList<PSNTreeNode>();

	public PSNTreeNode(String domain, String value) {
		super();
		this.domain = domain;
		this.value = value;
	}

	public String getDomain() {
		return domain;
	}

	public String getValue() {
		return value;
	}

	public List<PSNTreeNode> getChildren() {
		return children;
	}

	public void addChild(PSNTreeNode node) {
		children.add(node);
	}

	public PSNTreeDTO toDTO() {
		PSNTreeDTO dto = new PSNTreeDTO(domain, value);
		for (PSNTreeNode node : children) {
			dto.addChild(node.toDTO());
		}
		return dto;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((children == null) ? 0 : children.hashCode());
		result = prime * result + ((domain == null) ? 0 : domain.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
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
		PSNTreeNode other = (PSNTreeNode) obj;
		if (children == null) {
			if (other.children != null)
				return false;
		} else if (!children.equals(other.children))
			return false;
		if (domain == null) {
			if (other.domain != null)
				return false;
		} else if (!domain.equals(other.domain))
			return false;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("psn tree node for domain '");
		sb.append(domain);
		sb.append("' with value '");
		sb.append(value);
		sb.append("', contains ");
		sb.append(children.size());
		sb.append(" children");
		return sb.toString();
	}
}
