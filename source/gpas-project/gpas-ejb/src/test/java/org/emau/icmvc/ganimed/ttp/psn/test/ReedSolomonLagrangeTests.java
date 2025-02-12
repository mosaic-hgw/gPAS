package org.emau.icmvc.ganimed.ttp.psn.test;

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

import java.util.Arrays;

import org.emau.icmvc.ganimed.ttp.psn.alphabets.Alphabets;
import org.emau.icmvc.ganimed.ttp.psn.alphabets.GenericAlphabet;
import org.emau.icmvc.ganimed.ttp.psn.alphabets.NumbersX;
import org.emau.icmvc.ganimed.ttp.psn.alphabets.Symbol31;
import org.emau.icmvc.ganimed.ttp.psn.config.DomainConfig;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.InvalidAlphabetException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.InvalidParameterException;
import org.emau.icmvc.ganimed.ttp.psn.generator.Alphabet;
import org.emau.icmvc.ganimed.ttp.psn.generator.CheckDigits;
import org.emau.icmvc.ganimed.ttp.psn.generator.LagrangeForReedSolomon;
import org.emau.icmvc.ganimed.ttp.psn.generator.ReedSolomonLagrange;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ReedSolomonLagrangeTests extends CheckDigitTests
{
	ReedSolomonLagrangeTests() throws Exception
	{
		Alphabet tempAlphabet;
		try
		{
			tempAlphabet = new GenericAlphabet("0,1,2,3,4");
		}
		catch (InvalidAlphabetException e)
		{
			System.out.println("fehler beim erstellen des generischen alphabetes: " + e);
			tempAlphabet = new NumbersX();
		}
		CheckDigits temp;
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

	private CheckDigits createCheckDigits(Alphabet alphabet, int detectedErrors) throws InvalidAlphabetException, InvalidParameterException
	{
		DomainConfig domainConfig = new DomainConfig();
		domainConfig.setMaxDetectedErrors(detectedErrors);
		return new ReedSolomonLagrange(alphabet, domainConfig);
	}

	private static Alphabet createAlphabet(int alphabetLength) throws InvalidAlphabetException
	{
		if (alphabetLength < 1)
		{
			throw new InvalidAlphabetException("Empty alphabet not allowed");
		}
		if (alphabetLength == 11)
		{
			return new NumbersX();
		}
		if (alphabetLength == 31)
		{
			return new Symbol31();
		}

		StringBuilder chars = null;

		for (int i = 0; i < alphabetLength; i++)
		{
			if (chars != null)
			{
				chars.append(",").append(i);
			}
			else
			{
				chars = new StringBuilder("" + i);
			}
		}
		return Alphabets.createAlphabet(chars.toString());
	}

	static int[] PRIMES = new int[] {3, 5, 7, 11, 31};

	@Test
	void testGetNumberOfCheckDigits() throws Exception
	{
		int successfulTestCount = 0;
		for (int alphabetLength : PRIMES)
		{
			Alphabet a = createAlphabet(alphabetLength);
			assertEquals(alphabetLength, a.length());
			for (int maxDetectedErrors = 1; maxDetectedErrors < 10; maxDetectedErrors++)
			{
				try
				{
					if (alphabetLength <= maxDetectedErrors + 1)
					{
						continue;
					}
					CheckDigits cd = createCheckDigits(a, maxDetectedErrors);
					int maxMessageLength = cd.getMaxMessageLength();
					int effectiveMaxMessageLength = cd.getEffectiveMaxMessageLength();
					int numberOfCheckDigits = cd.getNumberOfCheckDigits(effectiveMaxMessageLength);

					// assert that effectiveMaxMessageLength actually is the maximum working number
					assertTrue(maxMessageLength >= numberOfCheckDigits + effectiveMaxMessageLength);
					effectiveMaxMessageLength++; // should be too big
					numberOfCheckDigits = cd.getNumberOfCheckDigits(effectiveMaxMessageLength);
					assertFalse(maxMessageLength >= numberOfCheckDigits + effectiveMaxMessageLength);

					for (int messageLength = 1; messageLength <= 100; messageLength++)
					{
						String message = generateNewPseudonym(messageLength, a);
						String digits = cd.generateCheckDigits(message);
						int numDigitsComputed = Math.ceilDiv(messageLength, alphabetLength - maxDetectedErrors) * maxDetectedErrors;
						if (numDigitsComputed != digits.length())
						{
							String[] parts = message.split(String.format("(?<=\\G.{%1$d})", alphabetLength - maxDetectedErrors));
							logger.info("{} -> {}\nAL: {}, NE: {}, ML: {}, ND: {}, NDC: {}, PLM: {}\n{}",
									message, digits, alphabetLength, maxDetectedErrors, messageLength, digits.length(),
									numDigitsComputed, parts.length * maxDetectedErrors, Arrays.toString(parts));
						}
						assertEquals(digits.length(), numDigitsComputed);
						assertEquals(digits.length(), cd.getNumberOfCheckDigits(messageLength));
						successfulTestCount++;
					}
				}
				catch (InvalidAlphabetException e)
				{
					logger.error(e);
				}
			}
		}
		assertEquals(2700, successfulTestCount);
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
		logger.info("check ok - code is systematic: {} -> {}", message, lagrangeResult);
	}

	@Test
	void testOneExampleForEveryLength() throws Exception
	{
		checkOneExampleForEveryLength(false);
	}

	@Test
	@Disabled("only for manual testing, too slow!")
	void testOneExampleForEveryLengthWithAll2DigitErrors() throws Exception
	{
		checkOneExampleForEveryLength(true);
	}
}
