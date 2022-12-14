package org.emau.icmvc.ttp.psn.frontend.controller;

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

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;

import org.emau.icmvc.ganimed.ttp.psn.StatisticManager;
import org.emau.icmvc.ganimed.ttp.psn.dto.DomainOutDTO;
import org.emau.icmvc.ganimed.ttp.psn.dto.StatisticDTO;
import org.emau.icmvc.ganimed.ttp.psn.utils.StatisticKeys;
import org.emau.icmvc.ttp.psn.frontend.controller.common.AbstractGPASBean;
import org.icmvc.ttp.web.controller.ThemeBean;
import org.icmvc.ttp.web.util.Chart;
import org.icmvc.ttp.web.util.File;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.charts.line.LineChartModel;
import org.primefaces.model.charts.pie.PieChartModel;

@ViewScoped
@ManagedBean(name = "dashboardController")
public class DashboardController extends AbstractGPASBean
{
	@EJB(lookup = "java:global/gpas/psn-ejb/StatisticManagerBean!org.emau.icmvc.ganimed.ttp.psn.StatisticManager")
	private StatisticManager statisticService;

	@ManagedProperty(value = "#{themeBean}")
	protected ThemeBean themeBean;

	Map<String, DomainOutDTO> domains;
	Map<String, String> domainColors;
	List<StatisticDTO> historyStats;
	StatisticDTO latestStats;

	@PostConstruct
	public void init()
	{
		domains = new LinkedHashMap<>();
		loadStats();
		if (getInit())
		{
			createDomainColors();
		}
	}

	public void updateStats()
	{
		statisticService.updateStats();
		init();
		logMessage(getCommonBundle().getString("page.dashboard.statistic.updated"), Severity.INFO);
	}

	/* Stats Overview */
	public Map<String, String> getLatestStatsAllDomainsLabels()
	{
		Map<String, String> result = new LinkedHashMap<>();
		result.put(StatisticKeys.DOMAINS, getBundle().getString("model.domain.domains"));
		result.put(StatisticKeys.PSEUDONYMS, getBundle().getString("model.psuedonym.pseudonyms"));
		result.put(StatisticKeys.ANONYMS, getBundle().getString("model.pseudonym.anonyms"));
		return result;
	}

	/* Pie Charts */
	public PieChartModel getPseudonymsChart(boolean mobile)
	{
		List<Number> values = new ArrayList<>();
		List<String> labels = new ArrayList<>();
		List<String> colors = new ArrayList<>();
		PieChartModel pieChartModel = Chart.initPieChart(values, labels, colors, mobile ? "top" : "left", themeBean.getDarkMode());

		for (DomainOutDTO domain : domains.values().stream().sorted().collect(Collectors.toList()))
		{
			long value = latestStats.getMappedStatValue().getOrDefault(StatisticKeys.PSEUDONYMS_PER_DOMAIN + domain.getName(), 0L);
			values.add(value);
			labels.add(domain.getLabel());
			colors.add(domainColors.get(domain.getName()));
		}

		return pieChartModel;
	}

	/* History Line Charts */
	public LineChartModel getPseudonymsHistoryChart()
	{
		Map<String, List<Object>> domainMap = new LinkedHashMap<>();
		List<String> dataSetLabels = new ArrayList<>();
		List<String> dataSetColors = new ArrayList<>();
		List<String> dataLabels = new ArrayList<>();

		// Add "all" domain
		domainMap.put("ALL", new ArrayList<>());
		dataSetLabels.add(getCommonBundle().getString("common.all"));
		dataSetColors.add("#7A7A7A");

		// Add domains
		for (DomainOutDTO domain : domains.values().stream().sorted().collect(Collectors.toList()))
		{
			domainMap.put(domain.getName(), new ArrayList<>());
			dataSetLabels.add(domain.getLabel());
			dataSetColors.add(domainColors.get(domain.getName()));
		}

		List<List<Object>> valuesLists = new ArrayList<>(domainMap.values());

		LineChartModel pseudonymsHistoryChart = Chart.initLineChartModel(valuesLists, dataSetLabels, dataSetColors, dataLabels, themeBean.getDarkMode());

		for (StatisticDTO statisticDTO : Chart.reduceStatistic(historyStats, 50))
		{
			for (Map.Entry<String, List<Object>> domain : domainMap.entrySet())
			{
				if (domain.getKey().equals("ALL"))
				{
					domain.getValue().add(statisticDTO.getMappedStatValue().getOrDefault(StatisticKeys.PSEUDONYMS, 0L));
				}
				else
				{
					domain.getValue().add(statisticDTO.getMappedStatValue().getOrDefault(StatisticKeys.PSEUDONYMS_PER_DOMAIN + domain.getKey(), 0L));
				}
			}

			dataLabels.add(dateToString(statisticDTO.getEntrydate(), "date"));
		}

		return pseudonymsHistoryChart;
	}

	/* Downloads */
	public StreamedContent getLatestStatsAllDomains()
	{
		Map<String, Number> valueMap = new HashMap<>();
		for (String key : getLatestStatsAllDomainsLabels().keySet())
		{
			valueMap.put(key, latestStats.getMappedStatValue().getOrDefault(key, 0L));
		}
		return getMapAsCsv(valueMap, latestStats.getEntrydate(), "all_domains stats latest");
	}

	public StreamedContent getHistoryStatsAllDomains()
	{
		return getHistoryStats(new ArrayList<>(getLatestStatsAllDomainsLabels().keySet()), "all_domains stats history");
	}

	/* Private methods */
	private void loadStats()
	{
		historyStats = statisticService.getAllStats();
		Collections.reverse(historyStats);
		historyStats = historyStats.stream()
				.filter(distinctByKey(StatisticDTO::getEntrydateWithoutTime))
				.collect(Collectors.toList());
		Collections.reverse(historyStats);

		// Load all domains that ever existed
		Map<String, DomainOutDTO> currentDomains = new HashMap<>();
		for (DomainOutDTO domain : domainService.listDomains())
		{
			currentDomains.put(domain.getName(), domain);
		}
		for (StatisticDTO statisticDTO : historyStats)
		{
			for (Map.Entry<String, Long> stat : statisticDTO.getMappedStatValue().entrySet())
			{
				if (stat.getKey().contains(StatisticKeys.PSEUDONYMS_PER_DOMAIN))
				{
					String domainName = stat.getKey().replace(StatisticKeys.PSEUDONYMS_PER_DOMAIN, "");
					domains.put(domainName, currentDomains.getOrDefault(domainName, new DomainOutDTO(domainName, domainName, null, null, null, null, null)));
				}
			}
		}

		latestStats = statisticService.getLatestStats();
	}

	private void createDomainColors()
	{
		domainColors = new HashMap<>();
		int i = 0;
		for (DomainOutDTO domain : domains.values())
		{
			domainColors.put(domain.getName(), "hsl(" + 359 / domains.size() * i + ", 83%, 72%)");
			i++;
		}
	}

	private StreamedContent getMapAsCsv(Map<String, Number> map, Date date, String details)
	{
		return File.get2DDataAsCsv(new ArrayList<>(map.values()), new ArrayList<>(map.keySet()), date, details, TOOL);
	}

	private StreamedContent getHistoryStats(List<String> keys, String details)
	{
		// Prepare lists
		List<String> dates = new ArrayList<>();
		Map<String, List<Object>> valueMap = new LinkedHashMap<>();
		for (String key : keys)
		{
			valueMap.put(key, new ArrayList<>());
		}

		// Fill lists
		for (StatisticDTO statisticDTO : historyStats)
		{
			dates.add(dateToString(statisticDTO.getEntrydate(), "date"));
			for (Map.Entry<String, List<Object>> entry : valueMap.entrySet())
			{
				entry.getValue().add(statisticDTO.getMappedStatValue().getOrDefault(entry.getKey(), 0L));
			}
		}

		return File.get3DDataAsCSV(valueMap, dates, details, TOOL);
	}

	public List<DomainOutDTO> getDomainsByUsage()
	{
		return super.getDomains().stream()
				.sorted(Comparator.comparingInt(DomainOutDTO::getPercentPsnsUsed).reversed())
				.collect(Collectors.toList());
	}

	public StatisticDTO getLatestStats()
	{
		return latestStats;
	}

	public String getLatestStatsDate()
	{
		if (latestStats.getEntrydate().toInstant().truncatedTo(ChronoUnit.DAYS).equals((new Date()).toInstant().truncatedTo(ChronoUnit.DAYS)))
		{
			return getCommonBundle().getString("ui.date.today");
		}
		else
		{
			return dateToString(latestStats.getEntrydate(), "date");
		}
	}

	public String getLatestStatsTime()
	{
		return dateToString(latestStats.getEntrydate(), "time");
	}

	public long getLatestStatsCalculationTime()
	{
		return latestStats.getMappedStatValue().getOrDefault(StatisticKeys.CALCULATION_TIME, -1L);
	}

	public boolean getInit()
	{
		return latestStats != null && latestStats.getMappedStatValue().containsKey(StatisticKeys.CALCULATION_TIME);
	}

	public void setThemeBean(ThemeBean themeBean)
	{
		this.themeBean = themeBean;
	}
}
