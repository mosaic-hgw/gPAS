package org.emau.icmvc.ganimed.ttp.psn.test;

/*
 * ###license-information-start###
 * gPAS - a Generic Pseudonym Administration Service
 * __
 * Copyright (C) 2013 - 2017 The MOSAIC Project - Institut fuer Community Medicine der
 * 							Universitaetsmedizin Greifswald - mosaic-projekt@uni-greifswald.de
 * 							concept and implementation
 * 							l. geidel
 * 							web client
 * 							g. weiher
 * 							a. blumentritt
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


import java.util.Random;

import org.apache.log4j.Logger;
import org.emau.icmvc.ganimed.ttp.psn.generator.MathUtil;
import org.junit.Assert;
import org.junit.Test;

public class MathUtilTests {

	private static final Logger logger = Logger.getLogger(MathUtilTests.class);
	private final Random rand = new Random();

	@Test
	public void checkPrimFactors() {
		Assert.assertTrue(checkPrimFactorsFor(1));
		Assert.assertTrue(checkPrimFactorsFor(2));
		Assert.assertTrue(checkPrimFactorsFor(11));
		Assert.assertTrue(checkPrimFactorsFor(54));
		Assert.assertTrue(checkPrimFactorsFor(1024));
		for(int i=0; i<10; i++) {
			Assert.assertTrue(checkPrimFactorsFor(rand.nextInt(Integer.MAX_VALUE)));
		}
		Assert.assertTrue(MathUtil.isPrimePower(2));
		Assert.assertTrue(MathUtil.isPrimePower(11));
		Assert.assertTrue(MathUtil.isPrimePower(4));
		Assert.assertTrue(MathUtil.isPrimePower(1024));
		Assert.assertFalse(MathUtil.isPrimePower(6));
		Assert.assertFalse(MathUtil.isPrimePower(36));
		Assert.assertFalse(MathUtil.isPrimePower(54));
	}

	private boolean checkPrimFactorsFor(int number) {
		int[] primFactors = MathUtil.getPrimeFactors(number);
		logger.info(getPrimString(number, primFactors));
		int test = 1;
		for(int i=0; i<primFactors.length; i++) {
			if(primFactors[i] != 0) {
				test *= primFactors[i];
			} else {
				break;
			}
		}
		return test == number;
	}

	private String getPrimString(int number, int[] primFactors) {
		StringBuilder result = new StringBuilder(number + " = ");
		for(int i=0; i<primFactors.length; i++) {
			if(i == 0) {
				result.append(primFactors[i]);
			} else {
				result.append(" * " + primFactors[i]);
			}
		}
		return result.toString();
	}
}
