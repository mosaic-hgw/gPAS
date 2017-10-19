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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.PostLoad;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.emau.icmvc.ganimed.ttp.psn.dto.DomainDTO;
import org.emau.icmvc.ganimed.ttp.psn.dto.DomainLightDTO;
import org.emau.icmvc.ganimed.ttp.psn.generator.GeneratorProperties;

/**
 * a psn-project ("domain") contains the settings for a type of pseudonyms
 * <p>
 * these settings must not be changed once they're used
 * 
 * @author geidell
 * 
 */
@Entity
@Table(name = "psn_projects")
public class PSNProject implements Serializable {

	private static final long serialVersionUID = -5215471242600038352L;
	private static final String PROPERTY_DELIMITER = ";";

	@Id
	private String domain;
	private String generatorClass;
	private String alphabet;
	private String parentDomain;
	@ManyToOne(fetch = FetchType.EAGER)
	@PrimaryKeyJoinColumn(name = "parentDomain", referencedColumnName = "domain")
	private PSNProject parent;
	@OneToMany(mappedBy = "parent", fetch = FetchType.EAGER)
	private List<PSNProject> children = new ArrayList<PSNProject>();
	@OneToMany(mappedBy = "psnProject", fetch = FetchType.LAZY)
	private List<PSN> psnList = new ArrayList<PSN>();
	// die properties werden als semikolon-getrennte liste in der db gespeichert
	// dafuer sorgen die funktionen "persistPropertiesToString" und "loadPropertiesFromString"
	// ausser in eclipselink:
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=273304
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=336066
	// deswegen das "loadPropertiesFromString" in "getProperties"
	@Transient
	private Map<GeneratorProperties, String> properties;
	@Column(name = "properties")
	private String propertiesString;
	private String comment;

	/**
	 * this constructor is only for reflection-based instantiation - do not use in other cases!
	 */
	public PSNProject() {
	}

	public PSNProject(DomainLightDTO domainDTO, PSNProject parent) {
		this.domain = domainDTO.getDomain();
		this.generatorClass = domainDTO.getCheckDigitClass();
		this.alphabet = domainDTO.getAlphabet();
		this.propertiesString = domainDTO.getProperties();
		loadPropertiesFromString();
		this.comment = domainDTO.getComment();
		this.parentDomain = parent != null ? parent.getDomain() : null;
		this.parent = parent;
	}

	/**
	 * this method is called by jpa
	 */
	@PreUpdate
	@PrePersist
	public void persistPropertiesToString() {
		StringBuilder sb = new StringBuilder();
		for (Entry<GeneratorProperties, String> property : properties.entrySet()) {
			sb.append(property.getKey());
			sb.append("=");
			sb.append(property.getValue());
			sb.append(PROPERTY_DELIMITER);
		}
		propertiesString = sb.toString();
	}

	/**
	 * this method is called by jpa
	 */
	@PostLoad
	public void loadPropertiesFromString() {
		properties = new HashMap<GeneratorProperties, String>();
		if (propertiesString != null) {
			String[] propertyList = propertiesString.split(PROPERTY_DELIMITER);
			for (String property : propertyList) {
				String[] propertyParts = property.split("=");
				if (propertyParts.length == 2) {
					GeneratorProperties propertyName = GeneratorProperties.valueOf(propertyParts[0].trim().toUpperCase());
					properties.put(propertyName, propertyParts[1].trim());
				}
			}
		}
	}

	public String getDomain() {
		return domain;
	}

	public String getGeneratorClass() {
		return generatorClass;
	}

	public String getAlphabet() {
		return alphabet;
	}

	public Map<GeneratorProperties, String> getProperties() {
		loadPropertiesFromString();
		return properties;
	}

	public String getPropertiesString() {
		return propertiesString;
	}

	public String getComment() {
		return comment;
	}

	public List<PSN> getPsnList() {
		return psnList;
	}

	public String getParentDomain() {
		return parentDomain;
	}

	public PSNProject getParent() {
		return parent;
	}

	public List<PSNProject> getChildren() {
		return children;
	}

	public DomainDTO toDTO(Long numberOfPseudonyms) {
		return new DomainDTO(domain, generatorClass, alphabet, propertiesString, comment, numberOfPseudonyms, parentDomain);
	}

	public DomainLightDTO toLightDTO() {
		return new DomainLightDTO(domain, generatorClass, alphabet, propertiesString, comment, parentDomain);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((domain == null) ? 0 : domain.hashCode());
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
		PSNProject other = (PSNProject) obj;
		if (domain == null) {
			if (other.domain != null)
				return false;
		} else if (!domain.equals(other.domain))
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
		sb.append(generatorClass);
		sb.append(", parent domain = '");
		sb.append(parentDomain);
		sb.append("|, comment = '");
		sb.append(comment);
		sb.append("' and the following properties: '");
		sb.append(propertiesString);
		sb.append("'");
		return sb.toString();
	}
}
