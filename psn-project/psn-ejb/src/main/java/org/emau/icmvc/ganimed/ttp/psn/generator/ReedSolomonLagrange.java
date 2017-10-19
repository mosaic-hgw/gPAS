package org.emau.icmvc.ganimed.ttp.psn.generator;

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

import java.util.Map;

import org.apache.log4j.Logger;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.CharNotInAlphabetException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.InvalidAlphabetException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.InvalidPSNException;

/**
 * generates a reed-solomon code; polynom-generation via lagrange-interpolation -> systematic code
 * 
 * @author geidell
 */
public class ReedSolomonLagrange extends CheckDigits {

	private static final Logger logger = Logger.getLogger(ReedSolomonLagrange.class);

	// regex zum trennen nach x zeichen
	// siehe http://stackoverflow.com/questions/2206378/how-to-split-a-string-but-also-keep-the-delimiters
	// und http://stackoverflow.com/questions/2297347/splitting-a-string-at-every-n-th-character
	private final static String SPLIT_AFTER_FIXED_LENGTH = "(?<=\\G.{%1$d})";

	private final int alphaLength;
	private final int numberOfCheckDigits;

	public ReedSolomonLagrange(Alphabet alphabet, Map<GeneratorProperties, String> properties) throws InvalidAlphabetException {
		super(alphabet, properties);
		alphaLength = alphabet.length();
		if (alphaLength < 3 || !MathUtil.isPrime(alphaLength)) {
			String message = "the length of the given alphabet (" + alphaLength + ") is not a prime, but for mathematical reasons needs to be one";
			logger.error(message);
			throw new InvalidAlphabetException(message);
		}
		String number = properties.get(GeneratorProperties.MAX_DETECTED_ERRORS);
		if (number != null) {
			int temp = 0;
			try {
				temp = Integer.parseInt(number);
			} catch (NumberFormatException e) {
				logger.error("exception while parsing the property '" + GeneratorProperties.MAX_DETECTED_ERRORS + "' - the default (2) will be used",
						e);
				temp = 2;
			}
			numberOfCheckDigits = temp;
		} else {
			logger.warn("property '" + GeneratorProperties.MAX_DETECTED_ERRORS + "' is not set - the default (2) will be used");
			numberOfCheckDigits = 2;
		}
		if (alphaLength <= numberOfCheckDigits + 1) {
			throw new InvalidAlphabetException("it's not possible to create a polymonial for reed-solomon witch can detect " + numberOfCheckDigits
					+ " errors with an alphabet length of " + alphaLength);
		}
		logger.info("initialised ReedSolomonLagrange generator with the alphabet: " + alphabet + " and " + numberOfCheckDigits
				+ " as number of check digits");
	}

	@Override
	public String generateCheckDigits(String message) throws CharNotInAlphabetException {
		if (logger.isDebugEnabled()) {
			logger.debug("add check digigts for message '" + message + "'");
		}
		// maximale laenge fuer einen nachrichtenteil im reed-solomon code: laenge_alphabet - anzahl_check_digits (entspricht anzahl erkennbarer fehler)
		// -> blocklaenge im rs = alphabetlaenge (anzahl elemente im endlichen feld des ringes)
		String[] parts = message.split(String.format(SPLIT_AFTER_FIXED_LENGTH, alphaLength - numberOfCheckDigits));
		String checkDigits = "";
		for (int i = 0; i < parts.length; i++) {
			// werte der buchstaben im endlichen feld
			int partLength = parts[i].length();
			int[] values = new int[partLength];
			for (int j = 0; j < partLength; j++) {
				values[j] = getAlphabet().getPosForSymbol(parts[i].charAt(j));
			}
			LagrangeForReedSolomon lagrange = new LagrangeForReedSolomon(values, alphaLength);
			if (logger.isTraceEnabled()) {
				logger.trace("add check digigts for message part '" + parts[i] + "'");
			}
			for (int j = 0; j < numberOfCheckDigits; j++) {
				checkDigits += getAlphabet().getSymbol(lagrange.calculateFor(partLength + j));
			}
			if (logger.isTraceEnabled()) {
				logger.trace("check digigts so far: '" + checkDigits + "'");
			}
		}
		return checkDigits;
	}

	@Override
	public void check(String value, int messageLength) throws CharNotInAlphabetException, InvalidPSNException {
		if (logger.isDebugEnabled()) {
			logger.debug("check message '" + value + "'");
		}
		int checkDigitCount = (int) Math.ceil(value.length() / (double) (getAlphabet().length())) * numberOfCheckDigits;
		if (value.length() != messageLength + checkDigitCount) {
			String message = "invalid value '" + value + "' - it should have a length of " + (messageLength + checkDigitCount);
			logger.info(message);
			throw new InvalidPSNException(message);
		}
		for (int i = 0; i < value.length(); i++) {
			getAlphabet().getPosForSymbol(value.charAt(i));
		}
		// laenge der nachricht / alphabetlaenge (=blocklaenge) -> anzahl pruefzeichen
		String realValue = value.substring(0, value.length() - checkDigitCount);
		if (!value.equals(realValue + generateCheckDigits(realValue))) {
			String message = "invalid check digits for '" + value + "'";
			logger.info(message);
			throw new InvalidPSNException(message);
		}
	}
}
