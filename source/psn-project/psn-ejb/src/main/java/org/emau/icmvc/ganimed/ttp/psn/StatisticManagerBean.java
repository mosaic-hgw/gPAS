package org.emau.icmvc.ganimed.ttp.psn;
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

import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Remote;
import javax.ejb.Schedule;
import javax.ejb.Stateless;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.emau.icmvc.ganimed.ttp.psn.dto.StatisticDTO;
import org.emau.icmvc.ganimed.ttp.psn.internal.Cache;

@WebService(name = "statisticService")
@SOAPBinding(style = SOAPBinding.Style.RPC)
@Stateless
@Remote(StatisticManager.class)
public class StatisticManagerBean implements StatisticManager
{
	private static final Logger logger = LogManager.getLogger(StatisticManagerBean.class);
	@EJB
	protected Cache cache;
	private boolean enableAutoUpdate = true;

	@Override
	public StatisticDTO getLatestStats()
	{
		logger.debug("call to getLatestStats");
		StatisticDTO result = cache.getLatestStats();
		if (logger.isDebugEnabled())
		{
			logger.debug("result of getLatestStats: " + result);
		}
		return result;
	}

	@Override
	public List<StatisticDTO> getAllStats()
	{
		logger.debug("call to getAllStats");
		List<StatisticDTO> result = cache.getAllStats();
		if (logger.isDebugEnabled())
		{
			logger.debug("number of results: " + result.size());
		}
		return result;
	}

	@Override
	public StatisticDTO updateStats()
	{
		logger.debug("call to getLatestStats");
		StatisticDTO result = cache.updateStats();
		if (logger.isDebugEnabled())
		{
			logger.debug("result of getLatestStats: " + result);
		}
		return result;
	}

	@Override
	public void addStat(StatisticDTO statisticDTO)
	{
		if (logger.isDebugEnabled())
		{
			logger.info("call to addStat with " + statisticDTO);
		}
		cache.addStat(statisticDTO);
		if (logger.isDebugEnabled())
		{
			logger.info("stat for " + statisticDTO + " added");
		}
	}

	@Schedule(second = "0", minute = "0", hour = "4")
	public void autoUpdate()
	{
		if (enableAutoUpdate)
		{
			logger.debug("Scheduled execution of updateStats.");
			updateStats();
		}
		else
		{
			logger.debug("Scheduling execution of updateStats skipped because autoUpdate is disabled.");
		}
	}

	@Override
	public void enableScheduling(boolean status)
	{
		this.enableAutoUpdate = status;
		logger.debug("Scheduling Mode enabled: " + enableAutoUpdate);
	}
}
