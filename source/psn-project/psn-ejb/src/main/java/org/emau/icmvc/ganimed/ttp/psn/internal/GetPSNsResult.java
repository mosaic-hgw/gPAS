package org.emau.icmvc.ganimed.ttp.psn.internal;
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
import java.util.Map;
import java.util.Set;

public class GetPSNsResult implements Serializable
{
	private static final long serialVersionUID = -5866388503697760550L;
	private final Map<String, String> psns;
	private final Set<String> notFound;
	private final Set<String> invalid;

	public GetPSNsResult(Map<String, String> psns, Set<String> notFound, Set<String> invalid)
	{
		super();
		this.psns = psns;
		this.notFound = notFound;
		this.invalid = invalid;
	}

	public Map<String, String> getPsns()
	{
		return psns;
	}

	public Set<String> getNotFound()
	{
		return notFound;
	}

	public Set<String> getInvalid()
	{
		return invalid;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + (invalid == null ? 0 : invalid.hashCode());
		result = prime * result + (notFound == null ? 0 : notFound.hashCode());
		result = prime * result + (psns == null ? 0 : psns.hashCode());
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
		GetPSNsResult other = (GetPSNsResult) obj;
		if (invalid == null)
		{
			if (other.invalid != null)
			{
				return false;
			}
		}
		else if (!invalid.equals(other.invalid))
		{
			return false;
		}
		if (notFound == null)
		{
			if (other.notFound != null)
			{
				return false;
			}
		}
		else if (!notFound.equals(other.notFound))
		{
			return false;
		}
		if (psns == null)
		{
			if (other.psns != null)
			{
				return false;
			}
		}
		else if (!psns.equals(other.psns))
		{
			return false;
		}
		return true;
	}

	@Override
	public String toString()
	{
		return "GetAndCreateResult [psns=" + psns + ", notFound=" + notFound + ", invalid=" + invalid + "]";
	}
}
