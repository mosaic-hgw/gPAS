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

import java.lang.reflect.Constructor;
import java.util.Map;
import java.util.Random;

import org.apache.log4j.Logger;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.CharNotInAlphabetException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.InvalidGeneratorException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.InvalidPSNException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.MessageTooLargeException;

/**
 * a generator for pseudo-random strings with the given alphabet and length
 * 
 * @author geidell
 * 
 */
public class Generator {

	private static final Logger logger = Logger.getLogger(Generator.class);
	private final Random rand = new Random(System.currentTimeMillis());
	private final CheckDigits checkDigits;
	private final int length;
	private final String prefix;
	private final String suffix;
	private final static int DEFAULT_PSEUDONYM_LENGTH = 8;
	private final boolean includePrefixInCheckDigitCalculation;

	public Generator(Class<? extends CheckDigits> checkDigitClass, Alphabet alphabet, Map<GeneratorProperties, String> properties)
			throws InvalidGeneratorException {
		length = readPSNLengthFromProperty(properties.get(GeneratorProperties.PSN_LENGTH));
		prefix = (properties.get(GeneratorProperties.PSN_PREFIX) == null ? "" : properties.get(GeneratorProperties.PSN_PREFIX));
		suffix = (properties.get(GeneratorProperties.PSN_SUFFIX) == null ? "" : properties.get(GeneratorProperties.PSN_SUFFIX));
		String temp = (properties.get(GeneratorProperties.INCLUDE_PREFIX_IN_CHECK_DIGIT_CALCULATION));
		includePrefixInCheckDigitCalculation = (temp != null) && temp.equalsIgnoreCase("true");
		if (logger.isDebugEnabled()) {
			logger.debug("create check digit class (" + checkDigitClass + ")");
		}
		CheckDigits tempCheckDigit = null;
		try {
			Constructor<? extends CheckDigits> constructor = checkDigitClass.getConstructor(Alphabet.class, Map.class);
			tempCheckDigit = constructor.newInstance(alphabet, properties);
		} catch (Exception e) {
			String message = "can't create generator for class " + checkDigitClass + " with alphabet " + alphabet + " and properties "
					+ properties.toString();
			logger.fatal(message, e);
			throw new InvalidGeneratorException(message, e);
		}
		checkDigits = tempCheckDigit;
		if (includePrefixInCheckDigitCalculation && prefix != null) {
			for (int i = 0; i < prefix.length(); i++) {
				try {
					checkDigits.getAlphabet().getPosForSymbol(prefix.charAt(i));
				} catch (CharNotInAlphabetException e) {
					String message = "prefix '" + prefix + "' must only contain chars of the given alphabet '" + alphabet
							+ "' if INCLUDE_PREFIX_IN_CHECK_DIGIT_CALCULATION is set to 'true'";
					logger.fatal(message, e);
					throw new InvalidGeneratorException(message, e);
				}
			}
		}
	}

	private int readPSNLengthFromProperty(String psnLength) {
		if (logger.isDebugEnabled()) {
			logger.debug("read pseudonym length from property: " + psnLength);
		}
		int result = DEFAULT_PSEUDONYM_LENGTH;
		if (psnLength != null) {
			try {
				result = Integer.parseInt(psnLength);
			} catch (NumberFormatException e) {
				logger.error("exception while parsing the property '" + GeneratorProperties.PSN_LENGTH + "' - the default (8) will be used", e);
			}
			if (result < 1) {
				logger.warn("the property '" + GeneratorProperties.PSN_LENGTH + "' is smaller than 1 - it is set to 1");
				result = 1;
			}
		} else {
			logger.warn("property '" + GeneratorProperties.PSN_LENGTH + "' is not set - the default (" + DEFAULT_PSEUDONYM_LENGTH + ") will be used");
		}
		return result;
	}

	/**
	 * generates a pseudo-random string with x check digits
	 * 
	 * @return
	 */
	public String getNewPseudonym() {
		if (logger.isDebugEnabled()) {
			logger.debug("generate new pseudonym");
		}
		String result = generateNewPseudonym();
		if (logger.isTraceEnabled()) {
			if (includePrefixInCheckDigitCalculation) {
				logger.trace("generate check-digits for " + prefix + result);
			} else {
				logger.trace("generate check-digits for " + result);
			}
		}
		try {
			if (includePrefixInCheckDigitCalculation) {
				result = prefix + result + checkDigits.generateCheckDigits(prefix + result) + suffix;
			} else {
				result = prefix + result + checkDigits.generateCheckDigits(result) + suffix;
			}
		} catch (CharNotInAlphabetException e) {
			logger.error("this should never happen - must be a program error", e);
			return null;
		} catch (MessageTooLargeException e) {
			logger.error("this should never happen - must be a program error", e);
			return null;
		}
		if (logger.isDebugEnabled()) {
			logger.debug("new pseudonym: " + result);
		}
		return result;
	}

	private String generateNewPseudonym() {
		StringBuilder result = new StringBuilder();
		for (int i = 0; i < length; i++) {
			result.append(checkDigits.getAlphabet().getSymbol(rand.nextInt(checkDigits.getAlphabet().length())));
		}
		return result.toString();
	}

	/**
	 * 
	 * @param value
	 *            string (including check digits) to be checked
	 * @throws CharNotInAlphabetException
	 * @throws InvalidPSNException
	 */
	public void check(String value) throws CharNotInAlphabetException, InvalidPSNException {
		if (logger.isDebugEnabled()) {
			logger.debug("check pseudonym '" + value + "'");
		}
		if (!prefix.isEmpty() && !value.startsWith(prefix)) {
			throw new InvalidPSNException("unknown prefix - '" + prefix + "' expected");
		}
		if (!suffix.isEmpty() && !value.endsWith(suffix)) {
			throw new InvalidPSNException("unknown suffix - '" + suffix + "' expected");
		}
		int realLength = length;
		if (includePrefixInCheckDigitCalculation) {
			realLength += prefix.length();
			value = value.substring(0, value.length() - suffix.length());
		} else {
			value = value.substring(prefix.length(), value.length() - suffix.length());
		}
		checkDigits.check(value, realLength);
	}

	/**
	 * reseed the random number generator with the current system time
	 */
	public void randomize() {
		rand.setSeed(System.currentTimeMillis());
	}
}
