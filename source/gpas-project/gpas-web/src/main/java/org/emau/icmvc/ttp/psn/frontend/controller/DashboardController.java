package org.emau.icmvc.ttp.psn.frontend.controller;

/*-
 * ###license-information-start###
 * gPAS - a Generic Pseudonym Administration Service
 * __
 * Copyright (C) 2013 - 2024 Independent Trusted Third Party of the University Medicine Greifswald
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

import java.io.Serial;
import java.io.Serializable;
import java.text.MessageFormat;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.annotation.ManagedProperty;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.emau.icmvc.ganimed.ttp.psn.dto.DomainOutDTO;
import org.emau.icmvc.ganimed.ttp.psn.dto.StatisticDTO;
import org.emau.icmvc.ganimed.ttp.psn.utils.StatisticKeys;
import org.emau.icmvc.ttp.psn.frontend.controller.common.AbstractGPASBean;
import org.icmvc.ttp.web.controller.ThemeBean;
import org.icmvc.ttp.web.util.Chart;
import org.icmvc.ttp.web.util.File;
import org.primefaces.model.StreamedContent;
import software.xdev.chartjs.model.charts.LineChart;
import software.xdev.chartjs.model.charts.PieChart;

@SessionScoped
@Named("dashboardController")
public class DashboardController extends AbstractGPASBean implements Serializable
{
	@Serial
	private static final long serialVersionUID = 8104506743831626219L;

	@Inject
	@ManagedProperty(value = "#{themeBean}")
	protected ThemeBean themeBean;

	private Map<String, DomainOutDTO> historicDomains;
	private Map<String, String> domainColors;
	private List<StatisticDTO> rangeStats;
	private StatisticDTO stats;
	private boolean hasStats;

	private Date rangeStartDate;
	private Date rangeEndDate;
	private Date statsDate;
	private Date statsMinDate;
	private Date statsMaxDate;

	private boolean groupOtherDomains = true;
	private boolean showDeletedDomains = false;

	private static final String ALL_DOMAINS = "ALL";
	private static final String OTHER_DOMAINS = "OTHER";

	private boolean rangeStatsLoaded = false;
	
	@PostConstruct
	public void init()
	{
		historicDomains = new LinkedHashMap<>();
		statsDate = null;
		statsMinDate = null;
		statsMaxDate = null;
		rangeStartDate = null;
		rangeEndDate = null;
		rangeStatsLoaded = false;
		loadStats();
		if (getDomains().size() != getStatisticService().getLatestStats().getMappedStatValue().getOrDefault(StatisticKeys.DOMAINS, 0L))
		{
			logMessage(getBundle().getString("page.dashboard.domains.changed"), Severity.INFO);
		}
	}

	public void updateStats()
	{
		getStatisticService().updateStats();
		init();
		logMessage(getCommonBundle().getString("page.dashboard.statistic.updated"), Severity.INFO);
	}
	
	public void loadRangeStats()
	{
		rangeStats = getStatisticService().getStatsFromTo(rangeStartDate, rangeEndDate);

		// Load all domains that ever existed
		Map<String, DomainOutDTO> currentDomains = new HashMap<>();
		for (DomainOutDTO domain : getManager().listDomains())
		{
			currentDomains.put(domain.getName(), domain);
		}
		
		// Load domains that existed in range
		if (rangeStats != null)
		{
			for (StatisticDTO statisticDTO : rangeStats)
			{
				for (Map.Entry<String, Long> stat : statisticDTO.getMappedStatValue().entrySet())
				{
					if (stat.getKey().contains(StatisticKeys.PSEUDONYMS_PER_DOMAIN))
					{
						String domainName = stat.getKey().replace(StatisticKeys.PSEUDONYMS_PER_DOMAIN, "");
						historicDomains.put(domainName, currentDomains.getOrDefault(domainName, new DomainOutDTO(domainName, domainName, null, null, null, null, null)));
					}
				}
			}
		}

		if (isHasStatsInTimespan())
		{
			createDomainColors();
		}

		rangeStatsLoaded = true;
	}

	public void onDateChange()
	{
		Calendar statsCal = Calendar.getInstance();
		statsCal.setTime(statsDate);
		statsCal.set(Calendar.HOUR_OF_DAY, 23);
		statsCal.set(Calendar.MINUTE, 59);
		statsCal.set(Calendar.SECOND, 59);
		statsDate = statsCal.getTime();

		Calendar rangeEndCal = Calendar.getInstance();
		rangeEndCal.setTime(rangeEndDate);
		rangeEndCal.set(Calendar.HOUR_OF_DAY, 23);
		rangeEndCal.set(Calendar.MINUTE, 59);
		rangeEndCal.set(Calendar.SECOND, 59);
		rangeEndDate = rangeEndCal.getTime();
		rangeStatsLoaded = false;
		loadStats();
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
	public PieChart getPseudonymsChart(boolean mobile)
	{
		List<Number> values = new ArrayList<>();
		List<String> labels = new ArrayList<>();
		List<String> colors = new ArrayList<>();
		
		List<Long> otherValues = new ArrayList<>();
		
		// Iterate over all domains (also deleted ones)
		for (DomainOutDTO domain : historicDomains.values().stream().sorted(Comparator.comparing(DomainOutDTO::getNumberOfPseudonyms).reversed()).toList())
		{
			// Add value if current stats exist for the domain (might contain recent deleted ones)
			if (stats.getMappedStatValue().containsKey(StatisticKeys.PSEUDONYMS_PER_DOMAIN + domain.getName()))
			{
				long value = stats.getMappedStatValue().get(StatisticKeys.PSEUDONYMS_PER_DOMAIN + domain.getName());

				if (groupOtherDomains && values.size() >= 10)
				{
					otherValues.add(value);
				}
				else
				{
					values.add(value);
					labels.add(getDomainLabel(domain));
					colors.add(domainColors.get(domain.getName()));
				}
			}
		}
		
		if (!otherValues.isEmpty())
		{
			values.add(otherValues.stream().reduce(0L, Long::sum));
			Object[] args = { otherValues.size() };
			labels.add(new MessageFormat(getCommonBundle().getString("page.dashboard.other")).format(args));
			colors.add("#AAAAAA");
		}

		return Chart.initPieChart(values, labels, colors, mobile ? Chart.LegendPosition.TOP : Chart.LegendPosition.LEFT, themeBean.getDarkMode());
	}

	/* History Line Charts */
	public LineChart getPseudonymsHistoryChart()
	{
		Map<String, List<Number>> domainMap = new LinkedHashMap<>();
		List<String> dataSetLabels = new ArrayList<>();
		List<String> dataSetColors = new ArrayList<>();
		List<String> dataLabels = new ArrayList<>();
		
		// Get "other" domains
		Set<String> otherDomains = new HashSet<>();

		// Add "all" domain
		domainMap.put(ALL_DOMAINS, new ArrayList<>());
		dataSetLabels.add(getCommonBundle().getString("common.all"));
		dataSetColors.add("#6A6A6A");
		
		// Add domains
		for (DomainOutDTO domain : historicDomains.values().stream().sorted(Comparator.comparing(DomainOutDTO::getNumberOfPseudonyms).reversed()).toList())
		{
			// Only add domain to chart if domain is not deleted or if deleted domains should be shown
			if (showDeletedDomains || getDomains().stream().map(DomainOutDTO::getName).toList().contains(domain.getName()))
			{
				if (groupOtherDomains && domainMap.size() > 10)
				{
					otherDomains.add(domain.getName());
				}
				else {
					domainMap.put(domain.getName(), new ArrayList<>());
					dataSetLabels.add(getDomainLabel(domain));
					dataSetColors.add(domainColors.get(domain.getName()));
				}
			}
		}
		
		if (!otherDomains.isEmpty())
		{
				domainMap.put(OTHER_DOMAINS, new ArrayList<>());
				Object[] args = { otherDomains.size() };
				dataSetLabels.add(new MessageFormat(getCommonBundle().getString("page.dashboard.other")).format(args));
				dataSetColors.add("#AAAAAA");
		}

		List<List<Number>> valuesLists = new ArrayList<>(domainMap.values());
		
		for (StatisticDTO statisticDTO : Chart.reduceStatistic(rangeStats, 50))
		{
			for (Map.Entry<String, List<Number>> domain : domainMap.entrySet())
			{
				if (domain.getKey().equals(ALL_DOMAINS))
				{
					domain.getValue().add(statisticDTO.getMappedStatValue().getOrDefault(StatisticKeys.PSEUDONYMS, 0L));
				}
				else if (!domain.getKey().equals(OTHER_DOMAINS))
				{
					domain.getValue().add(statisticDTO.getMappedStatValue().getOrDefault(StatisticKeys.PSEUDONYMS_PER_DOMAIN + domain.getKey(), 0L));
				}
			}
			
			if (!otherDomains.isEmpty())
			{
				long otherDomainsSum = 0L;
				for (String domain : otherDomains)
				{
					otherDomainsSum += statisticDTO.getMappedStatValue().getOrDefault(StatisticKeys.PSEUDONYMS_PER_DOMAIN + domain, 0L);
				}
				domainMap.get(OTHER_DOMAINS).add(otherDomainsSum);
			}

			dataLabels.add(dateToString(statisticDTO.getEntrydate(), "date"));
		}

		return Chart.initLineChart(valuesLists, dataSetLabels, dataSetColors, dataLabels, themeBean.getDarkMode());
	}
	
	public boolean isDeletedDomains()
	{
		return historicDomains.size() > getDomains().size();
	}

	/* Downloads */
	public StreamedContent getLatestStatsAllDomains()
	{
		Map<String, Number> valueMap = new HashMap<>();
		for (String key : getLatestStatsAllDomainsLabels().keySet())
		{
			valueMap.put(key, stats.getMappedStatValue().getOrDefault(key, 0L));
		}
		return getMapAsCsv(valueMap, stats.getEntrydate(), "all_domains stats latest");
	}

	public StreamedContent getHistoryStatsAllDomains()
	{
		return getHistoryStats(new ArrayList<>(getLatestStatsAllDomainsLabels().keySet()), "all_domains stats history");
	}

	/* Private methods */
	private void loadStats()
	{
		// Look if any stats exist
		stats = getStatisticService().getLatestStats();
		hasStats = stats != null && stats.getMappedStatValue().containsKey(StatisticKeys.CALCULATION_TIME);

		if (hasStats)
		{
			// Set min and max date for stats
			statsMinDate = getStatisticService().getFirstStats().getEntrydate();
			statsMaxDate = stats.getEntrydate();

			// Get stats for custom date
			if (statsDate != null && !stats.getEntrydate().equals(statsDate))
			{
				List<StatisticDTO> historyForCustomStatsDate = getStatisticService().getStatsFromTo(new Date(0), statsDate);
				stats = historyForCustomStatsDate.get(historyForCustomStatsDate.size() - 1);
			}
			else
			{
				statsDate = stats.getEntrydate();
			}

			// Set range start date if not set
			rangeStartDate = rangeStartDate != null ? rangeStartDate : statsMinDate;

			// set range end date if not set or if range ends after custom statsDate
			rangeEndDate = rangeEndDate != null && !rangeEndDate.after(statsDate) ? rangeEndDate : statsDate;
		}
	}

	private void createDomainColors()
	{
		domainColors = new HashMap<>();
		int i = 0;
		for (DomainOutDTO domain : historicDomains.values())
		{
			domainColors.put(domain.getName(), "hsl(" + 359 / historicDomains.size() * i + ", 83%, 72%)");
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
		for (StatisticDTO statisticDTO : rangeStats)
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
		return historicDomains.values().stream()
				.filter(d -> stats.getMappedStatValue().containsKey(StatisticKeys.UTILIZATION_PER_DOMAIN + d.getName()))
				.map(d -> new DomainOutDTO(d.getName(), d.getLabel(),
						(String) null, null, null, null, 0L, 0L, false, stats.getMappedStatValue().get(StatisticKeys.UTILIZATION_PER_DOMAIN + d.getName()).shortValue(), null, null, null, null))
				.sorted(Comparator.comparingInt(DomainOutDTO::getPercentPsnsUsed).reversed())
				.collect(Collectors.toList());
	}

	public StatisticDTO getStats()
	{
		return stats;
	}

	public String getLatestStatsDateTimeString()
	{
		if (stats.getEntrydate().toInstant().truncatedTo(ChronoUnit.DAYS).equals(new Date().toInstant().truncatedTo(ChronoUnit.DAYS)))
		{
			return getCommonBundle().getString("ui.date.today") + " " + getLatestStatsTimeString();
		}
		else
		{
			return dateToString(stats.getEntrydate(), "date") + " " + getLatestStatsTimeString();
		}
	}

	public String getLatestStatsTimeString()
	{
		return dateToString(stats.getEntrydate(), "time");
	}

	public long getLatestStatsCalculationTime()
	{
		return stats.getMappedStatValue().getOrDefault(StatisticKeys.CALCULATION_TIME, -1L);
	}

	public boolean isHasStats()
	{
		return hasStats;
	}

	public boolean isHasStatsInTimespan()
	{
		return hasStats && stats != null;
	}

	public Date getStatsDate()
	{
		return statsDate;
	}

	public void setStatsDate(Date statsDate)
	{
		this.statsDate = statsDate;
	}

	public Date getRangeStartDate()
	{
		return rangeStartDate;
	}

	public void setRangeStartDate(Date rangeStartDate)
	{
		this.rangeStartDate = rangeStartDate;
	}

	public Date getRangeEndDate()
	{
		return rangeEndDate;
	}

	public void setRangeEndDate(Date rangeEndDate)
	{
		this.rangeEndDate = rangeEndDate;
	}

	public Date getStatsMinDate()
	{
		return statsMinDate;
	}

	public Date getStatsMaxDate()
	{
		return statsMaxDate;
	}

	public boolean isGroupOtherDomains()
	{
		return groupOtherDomains;
	}

	public void setGroupOtherDomains(boolean groupOtherDomains)
	{
		this.groupOtherDomains = groupOtherDomains;
	}

	public boolean isShowDeletedDomains()
	{
		return showDeletedDomains;
	}

	public void setShowDeletedDomains(boolean showDeletedDomains)
	{
		this.showDeletedDomains = showDeletedDomains;
	}

	public void setThemeBean(ThemeBean themeBean)
	{
		this.themeBean = themeBean;
	}

	public boolean isRangeStatsLoaded()
	{
		return rangeStatsLoaded;
	}
}
