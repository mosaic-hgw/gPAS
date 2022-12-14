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
import org.eclipse.persistence.jpa.jpql.Assert;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.InvalidPSNException;
import org.emau.icmvc.ganimed.ttp.psn.generator.Alphabet;
import org.emau.icmvc.ganimed.ttp.psn.generator.CheckDigits;

public abstract class CheckDigitTests
{
	protected Alphabet alphabet;
	protected CheckDigits checkDigits;
	private static final Random rand = new SecureRandom();
	private static final Logger logger = LogManager.getLogger(CheckDigitTests.class);

	protected void checkAllNumbers() throws Exception
	{
		for (long l = 1; l <= 2000000; l++)
		{
			if (l % 100000 == 0)
			{
				logger.info("checking " + l);
			}
			String message = "" + l;
			String messageWithCheckDigits = message + checkDigits.generateCheckDigits(message);
			checkDigits.check(messageWithCheckDigits, message.length());
			test1Error(messageWithCheckDigits, message.length());
			testTransposition(messageWithCheckDigits, message.length());
		}
	}

	protected void checkOneExampleForEveryLength(boolean checkAll2DigitErrors) throws Exception
	{
		for (int i = 1; i <= 120; i++)
		{
			String message = generateNewPseudonym(i);
			String messageWithCheckDigits = message + checkDigits.generateCheckDigits(message);
			logger.info("checking " + i + " chars: " + messageWithCheckDigits);
			checkDigits.check(messageWithCheckDigits, i);
			test1Error(messageWithCheckDigits, i);
			if (checkAll2DigitErrors)
			{
				test2Errors(messageWithCheckDigits, i);
			}
			else
			{
				testTransposition(messageWithCheckDigits, i);
			}
		}
	}

	protected void test1Error(String messageWithCheckDigits, int length) throws Exception
	{
		for (int i = 0; i < messageWithCheckDigits.length(); i++)
		{
			for (int j = 1; j < alphabet.length(); j++)
			{
				char errorneousChar = alphabet.getSymbol((alphabet.getPosForSymbol(messageWithCheckDigits.charAt(i)) + j) % alphabet.length());
				String errorneousMessage = messageWithCheckDigits.substring(0, i) + errorneousChar + messageWithCheckDigits.substring(i + 1);
				try
				{
					checkDigits.check(errorneousMessage, length);
					System.out.println("algorithm didn't recognize invalid check sum for: " + errorneousMessage);
					Assert.fail("algorithm didn't recognize invalid check sum for: " + errorneousMessage);
				}
				catch (InvalidPSNException expected)
				{}
			}
		}
	}

	protected void testTransposition(String messageWithCheckDigits, int length) throws Exception
	{
		for (int i = 0; i < messageWithCheckDigits.length() - 2; i++)
		{
			if (messageWithCheckDigits.charAt(i + 1) != messageWithCheckDigits.charAt(i))
			{
				String errorneousMessage = messageWithCheckDigits.substring(0, i) + messageWithCheckDigits.charAt(i + 1)
						+ messageWithCheckDigits.charAt(i) + messageWithCheckDigits.substring(i + 2);
				try
				{
					checkDigits.check(errorneousMessage, length);
					System.out.println("algorithm didn't recognize invalid check sum for: " + errorneousMessage);
					Assert.fail("algorithm didn't recognize invalid check sum for: " + errorneousMessage);
				}
				catch (InvalidPSNException expected)
				{}
			}
		}
	}

	protected void test2Errors(String messageWithCheckDigits, int length) throws Exception
	{
		for (int i = 0; i < messageWithCheckDigits.length() - 1; i++)
		{
			for (int j = 1; j < alphabet.length(); j++)
			{
				char errorneousChar1 = alphabet.getSymbol((alphabet.getPosForSymbol(messageWithCheckDigits.charAt(i)) + j) % alphabet.length());
				for (int k = i + 1; k < messageWithCheckDigits.length(); k++)
				{
					for (int l = 1; l < alphabet.length(); l++)
					{
						char errorneousChar2 = alphabet
								.getSymbol((alphabet.getPosForSymbol(messageWithCheckDigits.charAt(k)) + l) % alphabet.length());
						String errorneousMessage = messageWithCheckDigits.substring(0, i) + errorneousChar1
								+ messageWithCheckDigits.substring(i + 1, k) + errorneousChar2 + messageWithCheckDigits.substring(k + 1);
						try
						{
							checkDigits.check(errorneousMessage, length);
							System.out.println("algorithm didn't recognize invalid check sum for: " + errorneousMessage);
							Assert.fail("algorithm didn't recognize invalid check sum for: " + errorneousMessage);
						}
						catch (InvalidPSNException expected)
						{}
					}
				}
			}
		}
	}

	protected String generateNewPseudonym(int length)
	{
		StringBuilder result = new StringBuilder();
		for (int i = 0; i < length; i++)
		{
			result.append(alphabet.getSymbol(rand.nextInt(alphabet.length())));
		}
		return result.toString();
	}
}
