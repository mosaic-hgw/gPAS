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
import org.emau.icmvc.ganimed.ttp.psn.alphabets.GenericAlphabet;
import org.emau.icmvc.ganimed.ttp.psn.alphabets.NumbersX;
import org.emau.icmvc.ganimed.ttp.psn.alphabets.Symbol31;
import org.emau.icmvc.ganimed.ttp.psn.config.DomainConfig;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.InvalidAlphabetException;
import org.emau.icmvc.ganimed.ttp.psn.generator.Alphabet;
import org.emau.icmvc.ganimed.ttp.psn.generator.CheckDigits;
import org.emau.icmvc.ganimed.ttp.psn.generator.LagrangeForReedSolomon;
import org.emau.icmvc.ganimed.ttp.psn.generator.ReedSolomonLagrange;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Tag("CheckDigit")
public class ReedSolomonLagrangeTests extends CheckDigitTests
{
	private static final Logger logger = LogManager.getLogger(ReedSolomonLagrangeTests.class);

	public ReedSolomonLagrangeTests() throws Exception
	{
		Alphabet tempAlphabet = null;
		try
		{
			tempAlphabet = new GenericAlphabet("0,1,2,3,4");
		}
		catch (InvalidAlphabetException e)
		{
			System.out.println("fehler beim erstellen des generischen alphabetes: " + e);
			tempAlphabet = new NumbersX();
		}
		CheckDigits temp = null;
		DomainConfig config = new DomainConfig();
		config.setPsnLength(8);
		try
		{
			temp = new ReedSolomonLagrange(tempAlphabet, config);
		}
		catch (InvalidAlphabetException e)
		{
			System.out.println("error while creating code-generator; will use default (reed solomon with a 31-symbol alphabet): " + e.getMessage());
			tempAlphabet = new Symbol31();
			temp = new ReedSolomonLagrange(tempAlphabet, config);
		}
		alphabet = tempAlphabet;
		checkDigits = temp;
	}

	@Test
	public void checkSystematicCode() throws Exception
	{
		logger.info("checking if this code is systematic");
		String message = generateNewPseudonym(10);
		String lagrangeResult = "";
		int messageLength = message.length();
		int[] values = new int[messageLength];
		for (int i = 0; i < messageLength; i++)
		{
			values[i] = alphabet.getPosForSymbol(message.charAt(i));
		}
		LagrangeForReedSolomon lagrange = new LagrangeForReedSolomon(values, alphabet.length());
		for (int i = 0; i < messageLength; i++)
		{
			lagrangeResult += alphabet.getSymbol(lagrange.calculateFor(i));
		}
		assertEquals(message, lagrangeResult, "generated reed solomon is not a systematic code: " + message + " != " + lagrangeResult);
		if (logger.isInfoEnabled())
		{
			logger.info("check ok - code is systematic: " + message + " -> " + lagrangeResult);
		}
	}

	@Test
	public void test() throws Exception
	{
		checkOneExampleForEveryLength(true);
	}
}
