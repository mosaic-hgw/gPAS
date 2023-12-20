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

import javax.jws.WebService;

import org.emau.icmvc.ganimed.ttp.psn.dto.StatisticDTO;

@WebService
public interface StatisticManager
{
	/**
	 * get last generated statistic
	 *
	 * @return last generated stat
	 */
	StatisticDTO getLatestStats();

	/**
	 * get all generated statistics
	 *
	 * @return list of stats
	 */
	List<StatisticDTO> getAllStats();

	/**
	 * creates a new stat entry
	 *
	 * @return created stat entry
	 */
	StatisticDTO updateStats();

	/**
	 * insert a new statisic bean into the database
	 *
	 * @param stat
	 *            statistical data
	 */
	void addStat(StatisticDTO stat);

	void enableScheduling(boolean enable);
}