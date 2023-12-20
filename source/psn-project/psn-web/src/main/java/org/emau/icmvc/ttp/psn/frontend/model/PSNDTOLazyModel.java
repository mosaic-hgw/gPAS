package org.emau.icmvc.ttp.psn.frontend.model;

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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.emau.icmvc.ganimed.ttp.psn.DomainManager;
import org.emau.icmvc.ganimed.ttp.psn.config.PSNField;
import org.emau.icmvc.ganimed.ttp.psn.config.PaginationConfig;
import org.emau.icmvc.ganimed.ttp.psn.dto.DomainOutDTO;
import org.emau.icmvc.ganimed.ttp.psn.dto.PSNDTO;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.InvalidParameterException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.UnknownDomainException;
import org.primefaces.model.FilterMeta;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortMeta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PSNDTOLazyModel extends LazyDataModel<PSNDTO>
{
	private final transient DomainManager service;

	private final transient Map<String, PSNDTO> resultMap;
	private final transient List<PSNDTO> resultList;
	private transient PaginationConfig lastConfig;
	private transient long lastRowCount = getRowCount();
	private transient long lastUnfilteredRowCount;
	protected final transient Logger logger = LoggerFactory.getLogger(getClass());

	private final transient List<DomainOutDTO> domains;

	public PSNDTOLazyModel(DomainManager service, List<DomainOutDTO> domains)
	{
		this.service = service;
		this.domains = new ArrayList<>(domains);
		this.resultMap = new LinkedHashMap<>();
		this.resultList = new ArrayList<>();
	}

	public List<DomainOutDTO> getDomains()
	{
		return domains;
	}

	public void setDomains(List<DomainOutDTO> domains)
	{
		if (!this.domains.equals(domains))
		{
			synchronized (resultList)
			{
				this.domains.clear();
				if (domains != null)
				{
					this.domains.addAll(domains);
				}
				updateResult(null, null, 0, 0);
			}
		}
	}

	public List<String> getDomainNames()
	{
		return domains.stream().map(DomainOutDTO::getName).collect(Collectors.toList());
	}

	public List<Long> getDomainCounts()
	{
		return getDomainCounts(getDomainNames());
	}

	private List<Long> getDomainCounts(List<String> domainNames)
	{
		return getRefreshedDomains(domainNames).stream().map(DomainOutDTO::getNumberOfPseudonyms).collect(Collectors.toList());
	}

	private List<DomainOutDTO> getRefreshedDomains(List<String> domainNames)
	{
		List<DomainOutDTO> domains = new ArrayList<>();
		domainNames.forEach(d ->
		{
			try
			{
				domains.add(service.getDomain(d));
			}
			catch (InvalidParameterException e)
			{
				logger.error("Invalid parameter " + d, e);
			}
			catch (UnknownDomainException e)
			{
				logger.error("Unknown domain " + d, e);
			}
		});
		return domains;
	}

	public long getUnfilteredRowCount()
	{
		return getUnfilteredRowCount(getDomainNames());
	}

	private long getUnfilteredRowCount(List<String> domainNames)
	{
		return getDomainCounts(domainNames).stream().reduce(0L, Long::sum);
	}

	@Override
	public PSNDTO getRowData(String rowKey)
	{
		return resultMap.get(rowKey);
	}

	@Override
	public String getRowKey(PSNDTO psn)
	{
		return (psn.getDomainName() + ": " + psn.getOriginalValue() + " -> " + psn.getPseudonym()).replace(",", "~");
	}

	@Override
	public List<PSNDTO> load(int first, int pageSize, Map<String, SortMeta> sortMetaMap, Map<String, FilterMeta> filterMetaMap)
	{
		logger.debug("load: first={}, pageSize={}, sorter={}, filter={}", first, pageSize, sortMetaMap, filterMetaMap);

		List<PSNDTO> matchingPSNs = new ArrayList<>();

		List<String> domainNames = getDomainNames();
		PaginationConfig config = new PaginationConfig(first, pageSize);
		FilterMeta globalFilter = filterMetaMap.get(FilterMeta.GLOBAL_FILTER_KEY);

		long unfilteredRowCount = getUnfilteredRowCount(domainNames);
		long filteredRowCount;

		// if the global filter is set, all other filters will be ignored
		if (globalFilter != null)
		{
			Object filterValue = globalFilter.getFilterValue();

			if (filterValue != null && StringUtils.isNotEmpty(filterValue.toString()))
			{
				String pattern = filterValue.toString();
				logger.debug("load: search with global filtering: pattern={}", pattern);
				Map<PSNField, String> filterMap = new HashMap<>();
				filterMap.put(PSNField.PSEUDONYM, pattern);
				filterMap.put(PSNField.VALUE, pattern);
				config.setFilter(filterMap);
				config.setFilterFieldsAreTreatedAsConjunction(false);
				config.setFilterIsCaseSensitive(false);
			}
		}

		try
		{
			PaginationConfig lastConfig = this.lastConfig;
			long lastUnfilteredRowCount = this.lastUnfilteredRowCount;

			// avoid unnecessary querying
			if (config.equals(lastConfig) && unfilteredRowCount == lastUnfilteredRowCount)
			{
				logger.debug("load: return with last result and last rowCount " + lastRowCount);
				setRowCount((int) lastRowCount); // see comment in #count()
				return getLastResult();
			}

			logger.debug("load: query new result");
			matchingPSNs.addAll(service.listPSNsForDomainsPaginated(domainNames, config));

			// avoid unnecessary counting
			if (config.equalsWhenPagingAndSortingIsIgnored(lastConfig) && lastRowCount > 0
					&& lastUnfilteredRowCount == unfilteredRowCount)
			{
				logger.debug("load: return with new result and last rowCount " + lastRowCount);
				filteredRowCount = lastRowCount;
			}
			else if (!config.isUsingFiltering())
			{
				logger.debug("load: return with new result and unfilteredRowCount " + unfilteredRowCount);
				filteredRowCount = unfilteredRowCount;
			}
			else
			{
				logger.debug("load: query new rowCount");
				filteredRowCount = (int) service.countPSNsForDomains(domainNames, config);
				logger.debug("load: return with new result and new rowCount " + filteredRowCount);
			}
			logger.debug("load: got {} (of overall {}) filtered pseudonyms for the current page (psns={})", matchingPSNs.size(), filteredRowCount, matchingPSNs);
			updateResult(config, matchingPSNs, filteredRowCount, unfilteredRowCount);
		}
		catch (InvalidParameterException | UnknownDomainException e)
		{
			logger.error("load: Invalid parameters for loading pseudonyms", e);
			updateResult(null, null, 0, 0);
		}

		return matchingPSNs;
	}

	private void updateResult(PaginationConfig config, List<PSNDTO> matchingPSNs, long rowCount, long unfilteredRowCount)
	{
		synchronized (resultList)
		{
			resultList.clear();
			if (matchingPSNs != null)
			{
				resultList.addAll(matchingPSNs);
			}
			resultMap.clear();
			resultList.forEach(psn -> resultMap.put(getRowKey(psn), psn));
			setRowCount((int) rowCount);
			lastUnfilteredRowCount = unfilteredRowCount;
			lastConfig = config;
			lastRowCount = rowCount;
		}
	}

	/**
	 * Returns the last result (as an unmodifiable list).
	 *
	 * @return the last result (as an unmodifiable list)
	 */
	protected List<PSNDTO> getLastResult()
	{
		return Collections.unmodifiableList(resultList);
	}

	/**
	 * Returns the count of items in the database wrt. the filter configuration.
	 * It is legal to implement this method as a dummy e.g. always returning 0 (like this implementation does),
	 * as long as {@link #setRowCount(int)} is used correctly in {@link #load(int, int, Map, Map)}.
	 * In other words, when this method is implemented correctly, there is no need to call
	 * {@link #setRowCount(int)} in {@link #load(int, int, Map, Map)} anymore.
	 *
	 * @see <a href="https://primefaces.github.io/primefaces/11_0_0/#/../migrationguide/11_0_0?id=datatable-dataview-datagrid-datalist">DataTable section in PF Migration guide 10 -> 11</a>
	 * @see <a href="https://primefaces.github.io/primefaces/11_0_0/#/components/datatable?id=lazy-loading">Lazy Loading in DataTable part of PF Documentation</a>
	 *
	 * @param filterBy
	 *            the filter map
	 * @return the number of items in the database wrt. the filter configuration or any arbitrary value, when {@link #setRowCount(int)} is used correctly
	 */
	@Override
	public int count(Map<String, FilterMeta> filterBy)
	{
		return 0;
	}
}
