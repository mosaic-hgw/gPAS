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

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.emau.icmvc.ganimed.ttp.psn.config.DomainConfig;
import org.emau.icmvc.ganimed.ttp.psn.generator.Alphabet;
import org.emau.icmvc.ganimed.ttp.psn.generator.CheckDigits;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class DomainOutDTO extends DomainInDTO implements Serializable
{
	private static final long serialVersionUID = 4987517459360776496L;
	private static final String NOT_SET = "not set";
	private static final transient SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
	private final List<String> childDomainNames = new ArrayList<>();
	private long numberOfPseudonyms;
	private long numberOfAnonyms;
	private boolean cacheUsed;
	private short percentPsnsUsed;
	private Date createDate;
	private Date updateDate;
	private String createDateString;
	private String updateDateString;

	public DomainOutDTO()
	{
		super();
	}

	public DomainOutDTO(String name, String checkDigitClass, String alphabet)
	{
		super(name, checkDigitClass, alphabet);
	}

	public DomainOutDTO(String name, Class<? extends CheckDigits> checkDigitClass, Class<? extends Alphabet> alphabetClass)
	{
		super(name, checkDigitClass, alphabetClass);
	}

	public DomainOutDTO(String name, String label, String checkDigitClass, String alphabet, DomainConfig config, String comment, List<String> parentDomainNames)
	{
		super(name, label, checkDigitClass, alphabet, config, comment, parentDomainNames);
	}

	public DomainOutDTO(String name, Class<? extends CheckDigits> checkDigitClass, Class<? extends Alphabet> alphabetClass, String label, DomainConfig config, String comment,
			List<String> parentDomainNames)
	{
		super(name, checkDigitClass, alphabetClass, label, config, comment, parentDomainNames);
	}

	public DomainOutDTO(String name, String label, String checkDigitClass, String alphabet, DomainConfig config, String comment, long numberOfPseudonyms, long numberOfAnonyms, boolean cacheUsed,
			short percentPsnsUsed, Date createDate, Date updateDate, List<String> parentDomainNames, List<String> childDomainNames)
	{
		super(name, label, checkDigitClass, alphabet, config, comment, parentDomainNames);
		this.numberOfPseudonyms = numberOfPseudonyms;
		this.numberOfAnonyms = numberOfAnonyms;
		this.cacheUsed = cacheUsed;
		this.percentPsnsUsed = percentPsnsUsed;
		setCreateDate(createDate);
		setUpdateDate(updateDate);
		if (childDomainNames != null)
		{
			this.childDomainNames.addAll(childDomainNames);
		}
	}

	public DomainOutDTO(String name, String label, Class<? extends CheckDigits> checkDigitClass, Class<? extends Alphabet> alphabetClass, DomainConfig config, String comment, long numberOfPseudonyms,
			long numberOfAnonyms, boolean cacheUsed, short percentPsnsUsed, Date createDate, Date updateDate, List<String> parentDomainNames, List<String> childDomainNames)
	{
		super(name, checkDigitClass, alphabetClass, label, config, comment, parentDomainNames);
		this.numberOfPseudonyms = numberOfPseudonyms;
		this.numberOfAnonyms = numberOfAnonyms;
		this.cacheUsed = cacheUsed;
		this.percentPsnsUsed = percentPsnsUsed;
		setCreateDate(createDate);
		setUpdateDate(updateDate);
		if (childDomainNames != null)
		{
			this.childDomainNames.addAll(childDomainNames);
		}
	}

	/**
	 * @param childDomainNames
	 *            list of names of the parent domains for this domain
	 */
	public void setChildDomainNames(List<String> childDomainNames)
	{
		this.childDomainNames.clear();
		if (childDomainNames != null)
		{
			this.childDomainNames.addAll(childDomainNames);
		}
	}

	/**
	 * @return childDomainNames
	 */
	public List<String> getChildDomainNames()
	{
		return childDomainNames;
	}

	/**
	 * @return how many pseudonyms are generated for that domain (includes the number of anonyms)
	 */
	public long getNumberOfPseudonyms()
	{
		return numberOfPseudonyms;
	}

	/**
	 * @param numberOfPseudonyms
	 *            how many pseudonyms (including anonyms) are generated for that domain
	 */
	public void setNumberOfPseudonyms(long numberOfPseudonyms)
	{
		this.numberOfPseudonyms = numberOfPseudonyms;
	}

	/**
	 * @return how many anonyms exists within that domain
	 */
	public long getNumberOfAnonyms()
	{
		return numberOfAnonyms;
	}

	/**
	 * @param numberOfAnonyms
	 *            how many anonyms exists for that domain
	 */
	public void setNumberOfAnonyms(long numberOfAnonyms)
	{
		this.numberOfAnonyms = numberOfAnonyms;
	}

	/**
	 * @return is the psn cache used for this domain
	 */
	public boolean isCacheUsed()
	{
		return cacheUsed;
	}

	/**
	 * @param cacheUsed
	 *            is the psn cache used for this domain
	 */
	public void setCacheUsed(boolean cacheUsed)
	{
		this.cacheUsed = cacheUsed;
	}

	/**
	 * @return how many percent of possible psns are used within this domain
	 */
	public short getPercentPsnsUsed()
	{
		return percentPsnsUsed;
	}

	/**
	 * @param percentPsnsUsed
	 *            how many percent of possible psns are used within this domain
	 */
	public void setPercentPsnsUsed(short percentPsnsUsed)
	{
		this.percentPsnsUsed = percentPsnsUsed;
	}

	/**
	 * @return date of creation of the domain
	 */
	public Date getCreateDate()
	{
		return createDate;
	}

	/**
	 * @param createDate
	 *            the date of creation of the domain
	 */
	public void setCreateDate(Date createDate)
	{
		this.createDate = createDate;
		synchronized (sdf)
		{
			if (createDate != null)
			{
				createDateString = sdf.format(createDate);
			}
			else
			{
				createDateString = NOT_SET;
			}
		}
	}

	/**
	 * @return the date of the last update of the domain
	 */
	public Date getUpdateDate()
	{
		return updateDate;
	}

	/**
	 * @param updateDate
	 *            the date of the last update of the domain
	 */
	public void setUpdateDate(Date updateDate)
	{
		this.updateDate = updateDate;
		synchronized (sdf)
		{
			if (updateDate != null)
			{
				updateDateString = sdf.format(updateDate);
			}
			else
			{
				updateDateString = NOT_SET;
			}
		}
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + (cacheUsed ? 1231 : 1237);
		result = prime * result + (childDomainNames == null ? 0 : childDomainNames.hashCode());
		result = prime * result + (createDate == null ? 0 : createDate.hashCode());
		result = prime * result + (int) (numberOfAnonyms ^ numberOfAnonyms >>> 32);
		result = prime * result + (int) (numberOfPseudonyms ^ numberOfPseudonyms >>> 32);
		result = prime * result + percentPsnsUsed;
		result = prime * result + (updateDate == null ? 0 : updateDate.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		if (!super.equals(obj))
		{
			return false;
		}
		if (getClass() != obj.getClass())
		{
			return false;
		}
		DomainOutDTO other = (DomainOutDTO) obj;
		if (cacheUsed != other.cacheUsed)
		{
			return false;
		}
		if (childDomainNames == null)
		{
			if (other.childDomainNames != null)
			{
				return false;
			}
		}
		else if (!childDomainNames.equals(other.childDomainNames))
		{
			return false;
		}
		if (createDate == null)
		{
			if (other.createDate != null)
			{
				return false;
			}
		}
		else if (!createDate.equals(other.createDate))
		{
			return false;
		}
		if (numberOfAnonyms != other.numberOfAnonyms)
		{
			return false;
		}
		if (numberOfPseudonyms != other.numberOfPseudonyms)
		{
			return false;
		}
		if (percentPsnsUsed != other.percentPsnsUsed)
		{
			return false;
		}
		if (updateDate == null)
		{
			if (other.updateDate != null)
			{
				return false;
			}
		}
		else if (!updateDate.equals(other.updateDate))
		{
			return false;
		}
		return true;
	}

	@Override
	public String toString()
	{
		return "DomainOutDTO [childDomainNames=" + childDomainNames + ", numberOfPseudonyms=" + numberOfPseudonyms + ", numberOfAnonyms=" + numberOfAnonyms + ", cacheUsed="
				+ cacheUsed + ", percentPsnsUsed=" + percentPsnsUsed + ", createDateString=" + createDateString + ", updateDateString=" + updateDateString + "]";
	}
}
