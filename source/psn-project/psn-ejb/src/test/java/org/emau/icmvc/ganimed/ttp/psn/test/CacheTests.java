package org.emau.icmvc.ganimed.ttp.psn.test;

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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.emau.icmvc.ganimed.ttp.psn.internal.PSNCacheObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CacheTests
{
	private static final Logger logger = LogManager.getLogger(CacheTests.class);
	private static final int size = 1_000_000;

	@Test
	public void testCache() throws Exception
	{
		logger.info("start cache test");
		PSNCacheObject cache = new PSNCacheObject(size);
		for (int i = 0; i < size; i++)
		{
			int pos = i;
			Assertions.assertFalse(cache.isPosSet(pos), "empty cache has value set, pos=" + pos);
			cache.setPos(pos);
			Assertions.assertTrue(cache.isPosSet(pos), "cache hasn't set value, pos=" + pos);
		}

		for (int i = size - 1; i >= 0; i--)
		{
			int pos = i;
			Assertions.assertTrue(cache.isPosSet(pos), "full cache has value not set, pos=" + pos);
			cache.unsetPos(pos);
			Assertions.assertFalse(cache.isPosSet(pos), "cache hasn't removed value, pos=" + pos);
		}
		logger.info("cache test ended");
	}
}
