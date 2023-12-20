package org.emau.icmvc.ganimed.ttp.psn.config;

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
import java.util.HashMap;
import java.util.Map;

/**
 * parameter for get...Paginated functions
 * <p>
 * <b>firstEntry</b><br>
 * default = 0
 * <p>
 * <b>pageSize</b><br>
 * default = 10
 * <p>
 * <b>filter</b><br>
 * default = empty map<br>
 * filter values; for possible keys see {@link PSNField}
 * <p>
 * <b>startDate</b><br>
 * default = null<br>
 * consents with date >= startDate are returned
 * <p>
 * <b>endDate</b><br>
 * default = null<br>
 * consents with date <= endDate are returned
 * <p>
 * <b>templateType</b><br>
 * default = null<br>
 * <p>
 * <b>filterFieldsAreTreatedAsConjuction</b><br>
 * default = true<br>
 * has no effect on template type
 * <p>
 * <b>filterIsCaseSensitive</b><br>
 * default = true
 * <p>
 * <b>sortField</b><br>
 * see {@link PSNField}<br>
 * default = null
 * <p>
 * <b>sortIsAscending</b><br>
 * default = true
 * <p>
 *
 * @author geidell
 *
 */
public class PaginationConfig implements Serializable
{
	private static final long serialVersionUID = 5400581744252774912L;
	private int firstEntry = 0;
	private int pageSize = 10;
	private final Map<PSNField, String> filter = new HashMap<>();
	private boolean filterFieldsAreTreatedAsConjunction = true;
	private boolean filterIsCaseSensitive = true;
	private PSNField sortField = null;
	private boolean sortIsAscending = true;

	public PaginationConfig()
	{
		super();
	}

	public PaginationConfig(int firstEntry, int pageSize)
	{
		super();
		this.firstEntry = firstEntry;
		this.pageSize = pageSize;
	}

	public PaginationConfig(int firstEntry, int pageSize, PSNField sortField, boolean sortIsAscending)
	{
		super();
		this.firstEntry = firstEntry;
		this.pageSize = pageSize;
		this.sortField = sortField;
		this.sortIsAscending = sortIsAscending;
	}

	public PaginationConfig(int firstEntry, int pageSize, Map<PSNField, String> filter, boolean filterFieldsAreTreatedAsConjuction,
			boolean filterIsCaseSensitive)
	{
		super();
		this.firstEntry = firstEntry;
		this.pageSize = pageSize;
		setFilter(filter);
		this.filterFieldsAreTreatedAsConjunction = filterFieldsAreTreatedAsConjuction;
		this.filterIsCaseSensitive = filterIsCaseSensitive;
	}

	public PaginationConfig(int firstEntry, int pageSize, Map<PSNField, String> filter, boolean filterFieldsAreTreatedAsConjuction,
			boolean filterIsCaseSensitive, PSNField sortField, boolean sortIsAscending)
	{
		super();
		this.firstEntry = firstEntry;
		this.pageSize = pageSize;
		setFilter(filter);
		this.filterFieldsAreTreatedAsConjunction = filterFieldsAreTreatedAsConjuction;
		this.filterIsCaseSensitive = filterIsCaseSensitive;
		this.sortField = sortField;
		this.sortIsAscending = sortIsAscending;
	}

	public int getFirstEntry()
	{
		return firstEntry;
	}

	public void setFirstEntry(int firstEntry)
	{
		this.firstEntry = firstEntry;
	}

	public int getPageSize()
	{
		return pageSize;
	}

	public void setPageSize(int pageSize)
	{
		this.pageSize = pageSize;
	}

	public boolean isUsingPaging()
	{
		return pageSize > 0;
	}

	public PSNField getSortField()
	{
		return sortField;
	}

	public void setSortField(PSNField sortField)
	{
		this.sortField = sortField;
	}

	public boolean isUsingSorting()
	{
		return sortField != null;
	}

	public Map<PSNField, String> getFilter()
	{
		return new HashMap<>(filter);
	}

	public void setFilter(Map<PSNField, String> filter)
	{
		if (filter != null && this.filter != filter)
		{
			this.filter.clear();
			this.filter.putAll(filter);
		}
	}

	public boolean isUsingFiltering()
	{
		return !filter.isEmpty();
	}

	public boolean isFilterFieldsAreTreatedAsConjunction()
	{
		return filterFieldsAreTreatedAsConjunction;
	}

	public void setFilterFieldsAreTreatedAsConjunction(boolean filterFieldsAreTreatedAsConjuction)
	{
		this.filterFieldsAreTreatedAsConjunction = filterFieldsAreTreatedAsConjuction;
	}

	public boolean isFilterIsCaseSensitive()
	{
		return filterIsCaseSensitive;
	}

	public void setFilterIsCaseSensitive(boolean filterIsCaseSensitive)
	{
		this.filterIsCaseSensitive = filterIsCaseSensitive;
	}

	public boolean isSortIsAscending()
	{
		return sortIsAscending;
	}

	public void setSortIsAscending(boolean sortIsAscending)
	{
		this.sortIsAscending = sortIsAscending;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + filter.hashCode();
		result = prime * result + (filterFieldsAreTreatedAsConjunction ? 1231 : 1237);
		result = prime * result + (filterIsCaseSensitive ? 1231 : 1237);
		result = prime * result + firstEntry;
		result = prime * result + pageSize;
		result = prime * result + (sortField == null ? 0 : sortField.hashCode());
		result = prime * result + (sortIsAscending ? 1231 : 1237);
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (!equalsWhenPagingAndSortingIsIgnored(obj))
		{
			return false;
		}
		// now we know for sure that 'obj' is not null and an instance of PaginationConfig
		PaginationConfig other = (PaginationConfig) obj;
		if (firstEntry != other.firstEntry)
		{
			return false;
		}
		if (pageSize != other.pageSize)
		{
			return false;
		}
		if (sortField != other.sortField)
		{
			return false;
		}
		if (sortIsAscending != other.sortIsAscending)
		{
			return false;
		}
		return true;
	}

	public boolean equalsWhenPagingAndSortingIsIgnored(Object obj)
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
		PaginationConfig other = (PaginationConfig) obj;
		if (!filter.equals(other.filter))
		{
			return false;
		}
		if (filterFieldsAreTreatedAsConjunction != other.filterFieldsAreTreatedAsConjunction)
		{
			return false;
		}
		if (filterIsCaseSensitive != other.filterIsCaseSensitive)
		{
			return false;
		}
		return true;
	}

	@Override
	public String toString()
	{
		return "PaginationConfig [firstEntry=" + firstEntry + ", pageSize=" + pageSize + ", filter=" + filter + ", filterFieldsAreTreatedAsConjunction=" + filterFieldsAreTreatedAsConjunction
				+ ", filterIsCaseSensitive=" + filterIsCaseSensitive + ", sortField=" + sortField + ", sortIsAscending=" + sortIsAscending + "]";
	}
}
