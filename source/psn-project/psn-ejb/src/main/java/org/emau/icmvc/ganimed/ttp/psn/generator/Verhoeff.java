package org.emau.icmvc.ganimed.ttp.psn.generator;

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
import org.emau.icmvc.ganimed.ttp.psn.config.DomainConfig;
import org.emau.icmvc.ganimed.ttp.psn.enums.GeneratorAlphabetRestriction;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.CharNotInAlphabetException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.InvalidAlphabetException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.InvalidPSNException;

public class Verhoeff extends CheckDigits
{
	private static final int[][] op = { { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 }, //
			{ 1, 2, 3, 4, 0, 6, 7, 8, 9, 5 }, //
			{ 2, 3, 4, 0, 1, 7, 8, 9, 5, 6 }, //
			{ 3, 4, 0, 1, 2, 8, 9, 5, 6, 7 }, //
			{ 4, 0, 1, 2, 3, 9, 5, 6, 7, 8 }, //
			{ 5, 9, 8, 7, 6, 0, 4, 3, 2, 1 }, //
			{ 6, 5, 9, 8, 7, 1, 0, 4, 3, 2 }, //
			{ 7, 6, 5, 9, 8, 2, 1, 0, 4, 3 }, //
			{ 8, 7, 6, 5, 9, 3, 2, 1, 0, 4 }, //
			{ 9, 8, 7, 6, 5, 4, 3, 2, 1, 0 } };
	private static final int[][] F = new int[8][];
	private static final int[] F0 = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 };
	private static final int[] F1 = { 1, 5, 7, 6, 2, 8, 3, 0, 9, 4 };
	private static final Logger logger = LogManager.getLogger(Verhoeff.class);

	public Verhoeff()
	{
		super();
	}

	public Verhoeff(Alphabet alphabet, DomainConfig config) throws InvalidAlphabetException
	{
		super(alphabet, config);
		if (alphabet.length() != 10)
		{
			String message = "the length of the given alphabet needs to be 10";
			logger.error(message);
			throw new InvalidAlphabetException(message);
		}
		logger.info("initialised verhoeff generator with the alphabet: " + alphabet);
	}

	static
	{
		F[0] = F0;
		F[1] = F1;
		for (int i = 2; i < 8; i++)
		{
			F[i] = new int[10];
			for (int j = 0; j < 10; j++)
			{
				F[i][j] = F[i - 1][F[1][j]];
			}
		}
	}

	@Override
	public String generateCheckDigits(String message) throws CharNotInAlphabetException
	{
		if (logger.isDebugEnabled())
		{
			logger.debug("add check digigts for message '" + message + "'");
		}
		return "" + calculateCheckDigit(message);
	}

	@Override
	public void check(String value, int messageLength) throws CharNotInAlphabetException, InvalidPSNException
	{
		if (logger.isDebugEnabled())
		{
			logger.debug("check message '" + value + "'");
		}
		if (value.length() != messageLength + 1)
		{
			String message = "invalid value '" + value + "' - it should have a length of " + (messageLength + 1);
			logger.info(message);
			throw new InvalidPSNException(message);
		}
		// letztes zeichen des gegebenen strings und neu berechnetes pruefzeichen
		int lastPos = value.length() - 1;
		if (!(value.charAt(lastPos) == calculateCheckDigit(value.substring(0, lastPos))))
		{
			String message = "invalid check digits for '" + value + "'";
			logger.info(message);
			throw new InvalidPSNException(message);
		}
	}

	private char calculateCheckDigit(String message) throws CharNotInAlphabetException
	{
		int check = 0;
		for (int i = message.length(); i > 0; i--)
		{
			// F(number.length()-i): position is counted from right to left
			check = op[check][F[(message.length() - i) % 8][getAlphabet().getPosForSymbol(message.charAt(i - 1))]];
		}
		return getAlphabet().getSymbol(check);
	}

	@Override
	public GeneratorAlphabetRestriction getAlphabetRestriction()
	{
		return GeneratorAlphabetRestriction.CONST_10;
	}
}
