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

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.emau.icmvc.ganimed.ttp.psn.alphabets.Numbers;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.InvalidGeneratorException;
import org.emau.icmvc.ganimed.ttp.psn.generator.Alphabet;
import org.emau.icmvc.ganimed.ttp.psn.generator.Generator;
import org.emau.icmvc.ganimed.ttp.psn.generator.GeneratorProperties;
import org.emau.icmvc.ganimed.ttp.psn.generator.Verhoeff;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class GeneratorTest {

	private Alphabet alphabet;
	private Generator generator;
	private static final Logger logger = Logger.getLogger(GeneratorTest.class);

	@Before
	public void setup() throws Exception {
		alphabet = new Numbers();
		Map<GeneratorProperties, String> properties = new HashMap<GeneratorProperties, String>();
		properties.put(GeneratorProperties.PSN_LENGTH, "8");
		properties.put(GeneratorProperties.PSN_PREFIX, "preTest");
		properties.put(GeneratorProperties.INCLUDE_PREFIX_IN_CHECK_DIGIT_CALCULATION, "true");
		properties.put(GeneratorProperties.PSN_SUFFIX, "Suffix");
		try {
			generator = new Generator(Verhoeff.class, alphabet, properties);
			Assert.fail("could create a generator with a prefix with invalid chars and INCLUDE_PREFIX_IN_CHECK_DIGIT_CALCULATION set to true");
		} catch (InvalidGeneratorException expected) {
			logger.info("expected InvalidGeneratorException: " + expected);
		}
		properties.put(GeneratorProperties.PSN_PREFIX, "1");
		generator = new Generator(Verhoeff.class, alphabet, properties);
	}

	@Test
	public void checkSomePSN() throws Exception {
		for (int i = 1; i <= 100; i++) {
			String psn = generator.getNewPseudonym();
			logger.info("new psn: " + psn);
			generator.check(psn);
		}
	}
}
