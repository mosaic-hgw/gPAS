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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.emau.icmvc.ganimed.ttp.psn.enums.InsertPairError;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class InsertPairExceptionDTO implements Serializable
{
	private static final long serialVersionUID = 8335320840220868212L;
	private String message;
	private String value;
	private String pseudonym;
	private InsertPairError errorType;

	public InsertPairExceptionDTO()
	{}

	public InsertPairExceptionDTO(String message, String value, String pseudonym, InsertPairError errorType)
	{
		super();
		this.value = value;
		this.pseudonym = pseudonym;
		this.message = message;
		this.errorType = errorType;
	}

	public String getMessage()
	{
		return message;
	}

	public void setMessage(String message)
	{
		this.message = message;
	}

	public String getValue()
	{
		return value;
	}

	public void setValue(String value)
	{
		this.value = value;
	}

	public String getPseudonym()
	{
		return pseudonym;
	}

	public void setPseudonym(String pseudonym)
	{
		this.pseudonym = pseudonym;
	}

	public InsertPairError getErrorType()
	{
		return errorType;
	}

	public void setErrorType(InsertPairError errorType)
	{
		this.errorType = errorType;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + (errorType == null ? 0 : errorType.hashCode());
		result = prime * result + (message == null ? 0 : message.hashCode());
		result = prime * result + (pseudonym == null ? 0 : pseudonym.hashCode());
		result = prime * result + (value == null ? 0 : value.hashCode());
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
		InsertPairExceptionDTO other = (InsertPairExceptionDTO) obj;
		if (errorType != other.errorType)
		{
			return false;
		}
		if (message == null)
		{
			if (other.message != null)
			{
				return false;
			}
		}
		else if (!message.equals(other.message))
		{
			return false;
		}
		if (pseudonym == null)
		{
			if (other.pseudonym != null)
			{
				return false;
			}
		}
		else if (!pseudonym.equals(other.pseudonym))
		{
			return false;
		}
		if (value == null)
		{
			if (other.value != null)
			{
				return false;
			}
		}
		else if (!value.equals(other.value))
		{
			return false;
		}
		return true;
	}

	@Override
	public String toString()
	{
		return "InsertPairExceptionDTO [message=" + message + ", value=" + value + ", pseudonym=" + pseudonym + ", errorType=" + errorType + "]";
	}
}
