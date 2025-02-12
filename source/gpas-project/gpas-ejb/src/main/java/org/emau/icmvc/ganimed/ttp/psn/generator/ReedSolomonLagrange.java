package org.emau.icmvc.ganimed.ttp.psn.generator;
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

import java.io.Serial;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.emau.icmvc.ganimed.ttp.psn.config.DomainConfig;
import org.emau.icmvc.ganimed.ttp.psn.enums.GeneratorAlphabetRestriction;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.CharNotInAlphabetException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.InvalidAlphabetException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.InvalidPSNException;

import static org.emau.icmvc.ttp.util.FlexibleToStringStyle.THS_TO_STRING_STYLE;

public class ReedSolomonLagrange extends CheckDigits
{
	@Serial
	private static final long serialVersionUID = 7443742410187249321L;

	// regex zum trennen nach x zeichen
	// siehe
	// http://stackoverflow.com/questions/2206378/how-to-split-a-string-but-also-keep-the-delimiters
	// und http://stackoverflow.com/questions/2297347/splitting-a-string-at-every-n-th-character
	private final static String SPLIT_AFTER_FIXED_LENGTH = "(?<=\\G.{%1$d})";
	private final int alphaLength;
	private final int maxDetectedErrors;

	public ReedSolomonLagrange()
	{
		alphaLength = 0;
		maxDetectedErrors = 0;
	}

	public ReedSolomonLagrange(Alphabet alphabet, DomainConfig config) throws InvalidAlphabetException
	{
		super(alphabet);
		getAlphabetRestriction().validateAlphabet(alphabet);
		alphaLength = alphabet.length();
		if (alphaLength < 3)
		{
			throw new InvalidAlphabetException("the length of the given alphabet (" + alphaLength + ") must not be less than 3");
		}
		maxDetectedErrors = config.getMaxDetectedErrors();
		if (alphaLength <= maxDetectedErrors + 1)
		{
			throw new InvalidAlphabetException("it's not possible to create a polynomial for reed-solomon wich can detect " + maxDetectedErrors
					+ " errors with an alphabet of length " + alphaLength);
		}
		logger.info("initialised {}", this);
	}

	@Override
	public GeneratorAlphabetRestriction getAlphabetRestriction()
	{
		return GeneratorAlphabetRestriction.PRIME;
	}

	@Override
	public String generateCheckDigits(String message) throws CharNotInAlphabetException
	{
		logger.debug("add check digigts for message '{}'", message);
		// maximale laenge fuer einen nachrichtenteil im reed-solomon code: laenge_alphabet -
		// anzahl_check_digits (entspricht anzahl erkennbarer fehler)
		// -> blocklaenge im rs = alphabetlaenge (anzahl elemente im endlichen feld des ringes)
		String[] parts = message.split(String.format(SPLIT_AFTER_FIXED_LENGTH, alphaLength - maxDetectedErrors));
		String checkDigits = "";
		for (String part : parts)
		{
			// werte der buchstaben im endlichen feld
			int partLength = part.length();
			int[] values = new int[partLength];
			for (int j = 0; j < partLength; j++)
			{
				values[j] = getAlphabet().getPosForSymbol(part.charAt(j));
			}
			LagrangeForReedSolomon lagrange = new LagrangeForReedSolomon(values, alphaLength);
			logger.trace("add check digigts for message part '{}'", part);
			for (int j = 0; j < maxDetectedErrors; j++)
			{
				checkDigits += getAlphabet().getSymbol(lagrange.calculateFor(partLength + j));
			}
			logger.trace("check digigts so far: '{}'", checkDigits);
		}
		return checkDigits;
	}

	@Override
	public void check(String value, int messageLength) throws CharNotInAlphabetException, InvalidPSNException
	{
		logger.debug("check message '{}'", value);
		int checkDigitCount = (int) Math.ceil(value.length() / (double) getAlphabet().length()) * maxDetectedErrors;
		if (value.length() != messageLength + checkDigitCount)
		{
			String message = "invalid value '" + value + "' - it should have a length of " + (messageLength + checkDigitCount);
			logger.info(message);
			throw new InvalidPSNException(message);
		}
		for (int i = 0; i < value.length(); i++)
		{
			getAlphabet().getPosForSymbol(value.charAt(i));
		}
		// laenge der nachricht / alphabetlaenge (=blocklaenge) -> anzahl pruefzeichen
		String realValue = value.substring(0, value.length() - checkDigitCount);
		if (!value.equals(realValue + generateCheckDigits(realValue)))
		{
			String message = "invalid check digits for '" + value + "'";
			logger.debug(message);
			throw new InvalidPSNException(message);
		}
	}

	@Override
	public int getNumberOfCheckDigits(int messageLength)
	{
		return computeNumberOfCheckDigits(messageLength, alphaLength, maxDetectedErrors);
	}

	public static int computeNumberOfCheckDigits(int messageLength, int alphabetLength, int maxDetectedErrors)
	{
		return Math.ceilDiv(messageLength, alphabetLength - maxDetectedErrors) * maxDetectedErrors;
	}

	@Override
	public String toString()
	{
		return new ToStringBuilder(this, THS_TO_STRING_STYLE)
				.append("alphabet", getAlphabet())
				.append("maxDetectedErrors", maxDetectedErrors)
				.toString();
	}
}
