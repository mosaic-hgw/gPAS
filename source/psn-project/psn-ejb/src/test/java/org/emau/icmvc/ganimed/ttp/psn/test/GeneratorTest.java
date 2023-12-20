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

import java.security.SecureRandom;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.persistence.jpa.jpql.Assert;
import org.emau.icmvc.ganimed.ttp.psn.alphabets.Numbers;
import org.emau.icmvc.ganimed.ttp.psn.alphabets.Symbol32;
import org.emau.icmvc.ganimed.ttp.psn.config.DomainConfig;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.CharNotInAlphabetException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.InvalidGeneratorException;
import org.emau.icmvc.ganimed.ttp.psn.generator.Alphabet;
import org.emau.icmvc.ganimed.ttp.psn.generator.Generator;
import org.emau.icmvc.ganimed.ttp.psn.generator.HammingCode;
import org.emau.icmvc.ganimed.ttp.psn.generator.Verhoeff;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.fail;

public class GeneratorTest
{
	private Alphabet alphabet;
	private Generator generator, generatorWithDelimiter, generatorWithHugeNumberOfPSN;
	private static final Logger logger = LogManager.getLogger(GeneratorTest.class);
	private static final SecureRandom rand = new SecureRandom();

	@BeforeEach
	public void setup() throws Exception
	{
		alphabet = new Numbers();
		DomainConfig config = new DomainConfig();
		config.setPsnLength(8);
		config.setPsnPrefix("preTest");
		config.setPsnSuffix("Suffix");
		config.setIncludePrefixInCheckDigitCalculation(true);
		config.setUseLastCharAsDelimiterAfterXChars(3);
		try
		{
			generatorWithDelimiter = new Generator(Verhoeff.class, alphabet, config, "dummy domain");
			fail("could create a generator with a prefix with invalid chars and INCLUDE_PREFIX_IN_CHECK_DIGIT_CALCULATION set to true");
		}
		catch (InvalidGeneratorException expected)
		{
			logger.info("expected InvalidGeneratorException: " + expected);
		}
		config.setPsnPrefix("1");
		generatorWithDelimiter = new Generator(Verhoeff.class, alphabet, config, "dummy domain");
		config.setUseLastCharAsDelimiterAfterXChars(0);
		generator = new Generator(Verhoeff.class, alphabet, config, "dummy domain");
		config.setPsnLength(16);
		alphabet = new Symbol32();
		config.setPsnPrefix("PSN-MIRACUM-EID-");
		config.setPsnSuffix("");
		config.setIncludePrefixInCheckDigitCalculation(false);
		config.setUseLastCharAsDelimiterAfterXChars(8);
		generatorWithHugeNumberOfPSN = new Generator(HammingCode.class, alphabet, config, "dummy domain");
	}

	@Test
	public void checkSomePSN() throws Exception
	{
		logger.info("start generator test");
		long time = System.currentTimeMillis();
		for (int i = 1; i <= 100; i++)
		{
			String psn = generator.getNewPseudonym(-1L);
			generator.check(psn);
		}
		logger.info("benoetigte millisekunden:" + (System.currentTimeMillis() - time));
		long number, number2;
		for (int i = 1; i <= 100; i++)
		{
			number = nextLong(0, generatorWithDelimiter.getMaxNumberForPSN());
			String pseudonym = generatorWithDelimiter.getNewPseudonym(number);
			try
			{
				number2 = generatorWithDelimiter.getPosNumberForPSN(pseudonym);
				if (number != number2)
				{
					fail("number for generation WITH delimiter (" + number + ") differs from calculated number (" + number2 + ")");
				}
			}
			catch (ArithmeticException | CharNotInAlphabetException e)
			{
				fail("exception while calculating the number for a pseudonym", e);
			}
		}
		for (int i = 1; i <= 100; i++)
		{
			number = nextLong(0, generator.getMaxNumberForPSN());
			String pseudonym = generator.getNewPseudonym(number);
			try
			{
				number2 = generator.getPosNumberForPSN(pseudonym);
				if (number != number2)
				{
					fail("number for generation WITHOUT delimiter (" + number + ") differs from calculated number (" + number2 + ")");
				}
			}
			catch (ArithmeticException | CharNotInAlphabetException e)
			{
				fail("exception while calculating the number for a pseudonym", e);
			}
		}
		logger.info("end generator test");
	}

	// java-api function for longStreams
	private long nextLong(long origin, long bound)
	{
		long r = rand.nextLong();
		long n = bound - origin, m = n - 1;
		if ((n & m) == 0L)
		{
			r = (r & m) + origin;
		}
		else if (n > 0L)
		{ // reject over-represented candidates
			for (long u = r >>> 1; // ensure nonnegative
					u + m - (r = u % n) < 0L; // rejection check
					u = rand.nextLong() >>> 1)
			{

			}
			r += origin;
		}
		else
		{ // range not representable as long
			while (r < origin || r >= bound)
			{
				r = rand.nextLong();
			}
		}
		return r;
	}

	@Test
	public void checkMaxPSN() throws Exception
	{
		logger.info("calculate max psn");
		long maxPSN = generator.getMaxNumberForPSN();
		Assert.isTrue(maxPSN == 100000000l, "wrong number of max PSN for generator without delimiter, should be 100.000.000, but is " + maxPSN);
		maxPSN = generatorWithDelimiter.getMaxNumberForPSN();
		long expectedMaxPSN = (long) Math.pow(9, 6); // laenge alphabet - 1 (delimiter) ^ anzahl zeichen (laenge psn - delimiter)
		Assert.isTrue(maxPSN == expectedMaxPSN, "wrong number of max PSN for generator with delimiter, should be " + expectedMaxPSN + " , but is " + maxPSN);
		maxPSN = generatorWithHugeNumberOfPSN.getMaxNumberForPSN();
		Assert.isTrue(maxPSN == Long.MAX_VALUE, "wrong number of max PSN for generator with huge number of psn, should be " + Long.MAX_VALUE + " , but is " + maxPSN);
	}
}
