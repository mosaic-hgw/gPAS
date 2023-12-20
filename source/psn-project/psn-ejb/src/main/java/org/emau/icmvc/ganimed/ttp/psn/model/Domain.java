package org.emau.icmvc.ganimed.ttp.psn.model;

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

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.PostLoad;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.emau.icmvc.ganimed.ttp.psn.config.DomainConfig;
import org.emau.icmvc.ganimed.ttp.psn.dto.DomainInDTO;
import org.emau.icmvc.ganimed.ttp.psn.dto.DomainOutDTO;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.InvalidParameterException;

@Entity
@Table(name = "domain")
public class Domain implements Serializable
{
	private static final long serialVersionUID = 8862938470475350856L;
	private static final transient Logger LOGGER = LogManager.getLogger(Domain.class);
	@Id
	private String name;
	private String label;
	private String generatorClass;
	private String alphabet;
	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "domain_parents", joinColumns = {
			@JoinColumn(name = "domain", referencedColumnName = "name", nullable = false) }, inverseJoinColumns = {
					@JoinColumn(name = "parentDomain", referencedColumnName = "name", nullable = false) })
	private List<Domain> parents = new ArrayList<>();
	@ManyToMany(mappedBy = "parents", fetch = FetchType.LAZY)
	private List<Domain> children = new ArrayList<>();

	// die properties werden als semikolon-getrennte liste in der db gespeichert
	// dafuer sorgen die funktionen "persistPropertiesToString" und "loadPropertiesFromString"
	// ausser in eclipselink:
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=273304
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=336066
	// deswegen das "loadPropertiesFromString" in "getProperties" und der null-test in
	// loadPropertiesFromString
	@Transient
	private DomainConfig config;
	@Column(name = "properties")
	private String propertiesString;
	private String comment;
	@Column(name = "create_timestamp", nullable = false)
	private Timestamp createTimestamp;
	@Column(name = "update_timestamp", nullable = false)
	private Timestamp updateTimestamp;

	/**
	 * this constructor is only for reflection-based instantiation - do not use in other cases!
	 */
	public Domain()
	{}

	public Domain(DomainInDTO domainDTO, List<Domain> parents) throws InvalidParameterException
	{
		name = domainDTO.getName();
		label = domainDTO.getLabel();
		generatorClass = domainDTO.getCheckDigitClass();
		alphabet = domainDTO.getAlphabet();
		config = new DomainConfig(domainDTO.getConfig());
		loadPropertiesFromString();
		persistPropertiesToString();
		comment = domainDTO.getComment();
		this.createTimestamp = new Timestamp(System.currentTimeMillis());
		this.updateTimestamp = this.createTimestamp;
		this.parents = parents;
	}

	// eigentlich @PrePersist und @PreUpdate, aber em.merge beachtet nicht das transient feld "config"
	@SuppressWarnings("deprecation")
	public void persistPropertiesToString()
	{
		propertiesString = config.getPropertiesString();
	}

	/**
	 * this method is called by jpa
	 *
	 * @throws InvalidParameterException
	 */
	@SuppressWarnings("deprecation")
	@PostLoad
	public void loadPropertiesFromString() throws InvalidParameterException
	{
		if (config == null)
		{
			config = new DomainConfig(propertiesString);
		}
	}

	public String getName()
	{
		return name;
	}

	public String getLabel()
	{
		return label;
	}

	public String getGeneratorClass()
	{
		return generatorClass;
	}

	public String getAlphabet()
	{
		return alphabet;
	}

	public DomainConfig getConfig()
	{
		try
		{
			loadPropertiesFromString();
		}
		catch (InvalidParameterException e)
		{
			LOGGER.error("exception while parsing config - corrupt db entry?", e);
		}
		return config;
	}

	public String getPropertiesString()
	{
		return propertiesString;
	}

	public String getComment()
	{
		return comment;
	}

	public Timestamp getCreateTimestamp()
	{
		return createTimestamp;
	}

	public Timestamp getUpdateTimestamp()
	{
		return updateTimestamp;
	}

	public List<Domain> getParents()
	{
		return parents;
	}

	public void setParents(List<Domain> parents)
	{
		this.parents = parents;
	}

	public List<Domain> getChildren()
	{
		return children;
	}

	public void setChildren(List<Domain> children)
	{
		this.children = children;
	}

	public DomainOutDTO toDTO(long numberOfPseudonyms, long numberOfAnonyms, boolean cacheUsed, short percentPsnsUsed)
	{
		try
		{
			return new DomainOutDTO(name, label, generatorClass, alphabet, new DomainConfig(config), comment, numberOfPseudonyms, numberOfAnonyms, cacheUsed, percentPsnsUsed, createTimestamp,
					updateTimestamp, parents.stream().map(Domain::getName).collect(Collectors.toList()), children.stream().map(Domain::getName).collect(Collectors.toList()));
		}
		catch (InvalidParameterException e)
		{
			// impossible
			return null;
		}
	}

	public void update(DomainInDTO domainDTO, List<Domain> parents) throws InvalidParameterException
	{
		label = domainDTO.getLabel();
		generatorClass = domainDTO.getCheckDigitClass();
		alphabet = domainDTO.getAlphabet();
		config = new DomainConfig(domainDTO.getConfig());
		persistPropertiesToString();
		comment = domainDTO.getComment();
		this.updateTimestamp = new Timestamp(System.currentTimeMillis());
		this.parents = parents;
	}

	public void updateInUse(String label, String comment, boolean sendNotificationsWeb, boolean psnsDeletable)
	{
		this.label = label;
		this.comment = comment;
		this.config.setSendNotificationsWeb(sendNotificationsWeb);
		this.config.setPsnsDeletable(psnsDeletable);
		persistPropertiesToString();
		this.updateTimestamp = new Timestamp(System.currentTimeMillis());
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + (name == null ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		if (obj == null)
		{
			return false;
		}
		if (getClass() != obj.getClass())
		{
			return false;
		}
		Domain other = (Domain) obj;
		if (!Objects.equals(name, other.name))
		{
			return false;
		}
		return true;
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("domain '");
		sb.append(name);
		sb.append("' and label '");
		sb.append(label);
		sb.append("' is configured with: alphabet = ");
		sb.append(alphabet);
		sb.append(", check digit generator class = ");
		sb.append(generatorClass);
		sb.append(", parent domains = '");
		sb.append(parents);
		sb.append("|, comment = '");
		sb.append(comment);
		sb.append("|, createTimestamp = '");
		sb.append(createTimestamp);
		sb.append("|, updateTimestamp = '");
		sb.append(updateTimestamp);
		sb.append("' and the following properties: '");
		sb.append(propertiesString);
		sb.append("'");
		return sb.toString();
	}
}
