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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import com.sun.istack.NotNull;
import org.emau.icmvc.ganimed.ttp.psn.config.DomainConfig;
import org.emau.icmvc.ganimed.ttp.psn.generator.Alphabet;
import org.emau.icmvc.ganimed.ttp.psn.generator.CheckDigits;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class DomainInDTO implements Serializable, Comparable<DomainInDTO>
{

	private static final Comparator<DomainInDTO> COMPARATOR = Comparator
			.comparing((DomainInDTO d) -> d != null && d.getLabel() != null ? d.getLabel().toLowerCase() : null, Comparator.nullsLast(Comparator.naturalOrder()))
			.thenComparing((DomainInDTO d) -> d.getName().toLowerCase());

	private static final long serialVersionUID = -6534876415337982554L;
	private String name;
	private String label;
	private String checkDigitClass;
	private String alphabet;
	private DomainConfig config;
	private String comment;
	private final List<String> parentDomainNames = new ArrayList<>();

	public DomainInDTO()
	{}

	public DomainInDTO(String name, String checkDigitClass, String alphabet)
	{
		this.name = name;
		this.label = name;
		this.checkDigitClass = checkDigitClass;
		this.alphabet = alphabet;
	}

	public DomainInDTO(String name, String label, String checkDigitClass, String alphabet, DomainConfig config, String comment, List<String> parentDomainNames)
	{
		this.name = name;
		this.label = label;
		this.checkDigitClass = checkDigitClass;
		this.alphabet = alphabet;
		this.config = config;
		this.comment = comment;
		if (parentDomainNames != null)
		{
			this.parentDomainNames.addAll(parentDomainNames);
		}
	}

	public DomainInDTO(String name, Class<? extends CheckDigits> checkDigitClass, Class<? extends Alphabet> alphabetClass)
	{
		this.name = name;
		this.label = name;
		setCheckDigitClass(checkDigitClass);
		setAlphabetClass(alphabetClass);
	}

	public DomainInDTO(String name, Class<? extends CheckDigits> checkDigitClass, Class<? extends Alphabet> alphabetClass, String label, DomainConfig config, String comment,
			List<String> parentDomainNames)
	{
		this.name = name;
		this.label = label;
		setCheckDigitClass(checkDigitClass);
		setAlphabetClass(alphabetClass);
		this.config = config;
		this.comment = comment;
		if (parentDomainNames != null)
		{
			this.parentDomainNames.addAll(parentDomainNames);
		}
	}

	/**
	 * @return name
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * @param name
	 *            identifier
	 */
	public void setName(String name)
	{
		this.name = name;
	}

	/**
	 * human readable name of the domain
	 *
	 * @return label
	 */
	public String getLabel()
	{
		return label;
	}

	/**
	 * human readable name of the domain
	 *
	 * @param label
	 *            readable name of the domain
	 */
	public void setLabel(String label)
	{
		this.label = label;
	}

	/**
	 * @return generator class for check digits; could be any class derived from
	 *         {@link CheckDigits}<br>
	 */
	public String getCheckDigitClass()
	{
		return checkDigitClass;
	}

	/**
	 * @param checkDigitClass
	 *            generator class for check digits; could be any class derived from
	 *            {@link CheckDigits}<br>
	 */
	public void setCheckDigitClass(String checkDigitClass)
	{
		this.checkDigitClass = checkDigitClass;
	}

	/**
	 * @param checkDigitClass
	 *            generator class for check digits; could be any class derived from
	 *            {@link CheckDigits}<br>
	 */
	public void setCheckDigitClass(Class<? extends CheckDigits> checkDigitClass)
	{
		if (checkDigitClass != null)
		{
			this.checkDigitClass = checkDigitClass.getName();
		}
	}

	/**
	 * @return all chars which could be used for the pseudonyms; could be any class derived from
	 *         {@link Alphabet}<br>
	 *         or a comma separated list of chars
	 */
	public String getAlphabet()
	{
		return alphabet;
	}

	/**
	 * @param alphabet
	 *            all chars which could be used for the pseudonyms; could be any class derived from
	 *            {@link Alphabet}<br>
	 *            or a comma separated list of chars
	 */
	public void setAlphabet(String alphabet)
	{
		this.alphabet = alphabet;
	}

	/**
	 * @param alphabetClass
	 *            class containing the alphabet for pseudonyms; could be any class derived from {@link Alphabet
	 */
	public void setAlphabetClass(Class<? extends Alphabet> alphabetClass)
	{
		if (alphabetClass != null)
		{
			this.alphabet = alphabetClass.getName();
		}
	}

	/**
	 * @return config for this domain, see {@link DomainConfig}
	 */
	public DomainConfig getConfig()
	{
		return config;
	}

	/**
	 * @param config
	 *            config for this domain, see {@link DomainConfig}
	 */
	public void setConfig(DomainConfig config)
	{
		this.config = config;
	}

	/**
	 * @return comment
	 */
	public String getComment()
	{
		return comment;
	}

	/**
	 * @param comment
	 *            comment
	 */
	public void setComment(String comment)
	{
		this.comment = comment;
	}

	/**
	 * @return parentDomainNames
	 */
	public List<String> getParentDomainNames()
	{
		return parentDomainNames;
	}

	/**
	 * @param parentDomainNames
	 *            list of names of the parent domains for this domain
	 */
	public void setParentDomainNames(List<String> parentDomainNames)
	{
		this.parentDomainNames.clear();
		if (parentDomainNames != null)
		{
			this.parentDomainNames.addAll(parentDomainNames);
		}
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + (alphabet == null ? 0 : alphabet.hashCode());
		result = prime * result + (checkDigitClass == null ? 0 : checkDigitClass.hashCode());
		result = prime * result + (comment == null ? 0 : comment.hashCode());
		result = prime * result + (config == null ? 0 : config.hashCode());
		result = prime * result + (label == null ? 0 : label.hashCode());
		result = prime * result + (name == null ? 0 : name.hashCode());
		result = prime * result + parentDomainNames.hashCode();
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
		DomainInDTO other = (DomainInDTO) obj;
		if (alphabet == null)
		{
			if (other.alphabet != null)
			{
				return false;
			}
		}
		else if (!alphabet.equals(other.alphabet))
		{
			return false;
		}
		if (checkDigitClass == null)
		{
			if (other.checkDigitClass != null)
			{
				return false;
			}
		}
		else if (!checkDigitClass.equals(other.checkDigitClass))
		{
			return false;
		}
		if (comment == null)
		{
			if (other.comment != null)
			{
				return false;
			}
		}
		else if (!comment.equals(other.comment))
		{
			return false;
		}
		if (config == null)
		{
			if (other.config != null)
			{
				return false;
			}
		}
		else if (!config.equals(other.config))
		{
			return false;
		}
		if (label == null)
		{
			if (other.label != null)
			{
				return false;
			}
		}
		else if (!label.equals(other.label))
		{
			return false;
		}
		if (name == null)
		{
			if (other.name != null)
			{
				return false;
			}
		}
		else if (!name.equals(other.name))
		{
			return false;
		}
		return parentDomainNames.equals(other.parentDomainNames);
	}

	@Override
	public String toString()
	{
		return "DomainInDTO [name=" + name + ", label=" + label + ", checkDigitClass=" + checkDigitClass + ", alphabet=" + alphabet + ", config=" + config + ", comment=" + comment
				+ ", parentDomainNames=" + parentDomainNames + "]";
	}

	@Override
	public int compareTo(@NotNull DomainInDTO o)
	{
		return COMPARATOR.compare(this, o);
	}
}
