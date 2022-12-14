package org.emau.icmvc.ganimed.ttp.psn.test;

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

import java.security.SecureRandom;
import java.util.Random;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.emau.icmvc.ganimed.ttp.psn.generator.MathUtil;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MathUtilTests
{
	private static final Logger logger = LogManager.getLogger(MathUtilTests.class);
	private static final Random rand = new SecureRandom();

	@Test
	public void checkPrimFactors()
	{
		assertTrue(checkPrimFactorsFor(1));
		assertTrue(checkPrimFactorsFor(2));
		assertTrue(checkPrimFactorsFor(11));
		assertTrue(checkPrimFactorsFor(54));
		assertTrue(checkPrimFactorsFor(1024));
		for (int i = 0; i < 10; i++)
		{
			assertTrue(checkPrimFactorsFor(rand.nextInt(Integer.MAX_VALUE)));
		}
		assertTrue(MathUtil.isPrimePower(2));
		assertTrue(MathUtil.isPrimePower(11));
		assertTrue(MathUtil.isPrimePower(4));
		assertTrue(MathUtil.isPrimePower(1024));
		assertFalse(MathUtil.isPrimePower(6));
		assertFalse(MathUtil.isPrimePower(36));
		assertFalse(MathUtil.isPrimePower(54));
	}

	private boolean checkPrimFactorsFor(int number)
	{
		int[] primFactors = MathUtil.getPrimeFactors(number);
		logger.info(getPrimString(number, primFactors));
		int test = 1;
		for (int primFactor : primFactors)
		{
			if (primFactor != 0)
			{
				test *= primFactor;
			}
			else
			{
				break;
			}
		}
		return test == number;
	}

	private String getPrimString(int number, int[] primFactors)
	{
		StringBuilder result = new StringBuilder(number + " = ");
		for (int i = 0; i < primFactors.length; i++)
		{
			if (i == 0)
			{
				result.append(primFactors[i]);
			}
			else
			{
				result.append(" * " + primFactors[i]);
			}
		}
		return result.toString();
	}
}
