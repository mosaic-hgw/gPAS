package org.emau.icmvc.ganimed.ttp.psn.generator;

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

import java.lang.reflect.Constructor;
import java.security.SecureRandom;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.emau.icmvc.ganimed.ttp.psn.config.DomainConfig;
import org.emau.icmvc.ganimed.ttp.psn.enums.ForceCache;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.CharNotInAlphabetException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.InvalidGeneratorException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.InvalidPSNException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.InvalidParameterException;
import org.emau.icmvc.ganimed.ttp.psn.internal.PSNCacheObject;

public class Generator
{
	private static final Logger logger = LogManager.getLogger(Generator.class);
	private final CheckDigits checkDigits;
	private final boolean includePrefixInCheckDigitCalculation;
	private final boolean includeSuffixInCheckDigitCalculation;
	private final String prefix;
	private final String suffix;
	private final int length;
	private final int useLastCharAsDelimiterAfterXChars;
	private final SecureRandom rand = new SecureRandom();
	private final long maxNumberForPSN;
	private final boolean useCache;
	private final char lastChar;

	public Generator(Class<? extends CheckDigits> checkDigitClass, Alphabet alphabet, DomainConfig config, String domainName)
			throws InvalidGeneratorException
	{
		if (logger.isDebugEnabled())
		{
			logger.debug("create check digit class (" + checkDigitClass + ")");
		}
		includePrefixInCheckDigitCalculation = config.isIncludePrefixInCheckDigitCalculation();
		includeSuffixInCheckDigitCalculation = config.isIncludeSuffixInCheckDigitCalculation();
		prefix = config.getPsnPrefix();
		suffix = config.getPsnSuffix();
		length = config.getPsnLength();
		useLastCharAsDelimiterAfterXChars = config.getUseLastCharAsDelimiterAfterXChars();

		CheckDigits tempCheckDigit = null;
		try
		{
			Constructor<? extends CheckDigits> constructor = checkDigitClass.getConstructor(Alphabet.class, DomainConfig.class);
			tempCheckDigit = constructor.newInstance(alphabet, config);
		}
		catch (Exception e)
		{
			String message = "can't create generator for class " + checkDigitClass + " with alphabet " + alphabet + " and properties "
					+ config;
			logger.fatal(message, e);
			throw new InvalidGeneratorException(message, e);
		}
		checkDigits = tempCheckDigit;
		if (config.isIncludePrefixInCheckDigitCalculation() && !prefix.isEmpty())
		{
			for (int i = 0; i < prefix.length(); i++)
			{
				try
				{
					checkDigits.getAlphabet().getPosForSymbol(prefix.charAt(i));
				}
				catch (CharNotInAlphabetException e)
				{
					String message = "prefix '" + prefix + "' must only contain chars of the given alphabet '" + alphabet
							+ "' if INCLUDE_PREFIX_IN_CHECK_DIGIT_CALCULATION is set to 'true'";
					logger.fatal(message, e);
					throw new InvalidGeneratorException(message, e);
				}
			}
		}
		if (config.isIncludeSuffixInCheckDigitCalculation() && !suffix.isEmpty())
		{
			for (int i = 0; i < suffix.length(); i++)
			{
				try
				{
					checkDigits.getAlphabet().getPosForSymbol(suffix.charAt(i));
				}
				catch (CharNotInAlphabetException e)
				{
					String message = "suffix '" + suffix + "' must only contain chars of the given alphabet '" + alphabet
							+ "' if INCLUDE_SUFFIX_IN_CHECK_DIGIT_CALCULATION is set to 'true'";
					logger.fatal(message, e);
					throw new InvalidGeneratorException(message, e);
				}
			}
		}
		long tempMaxNumber = -1;
		boolean tempUseCache = !ForceCache.OFF.equals(config.getForceCache());
		try
		{
			tempMaxNumber = calculateMaxNumberForPSN();
			if (tempMaxNumber > (long) Integer.MAX_VALUE * PSNCacheObject.BITS_PER_FIELD
					|| tempMaxNumber > DomainConfig.MAX_PSEUDONYMS_FOR_DEFAULT_CACHE_ON && ForceCache.DEFAULT.equals(config.getForceCache()))
			{
				tempUseCache = false;
			}
			if (logger.isInfoEnabled())
			{
				logger.info("\ncache info for domain '" + domainName + "'\nmax. number of pseudonyms for domain:            " + tempMaxNumber
						+ "\nmax. numbers of pseudonyms for default cache on: " + DomainConfig.MAX_PSEUDONYMS_FOR_DEFAULT_CACHE_ON + "\nmax. numbers fo pseudonyms for forced cache on:  "
						+ (long) Integer.MAX_VALUE * PSNCacheObject.BITS_PER_FIELD + "\nDomainConfig.ForceCache: " + config.getForceCache() + "\n--> cache is " + (tempUseCache ? "ON" : "OFF")
						+ "\napprox. mem consumption for cache: " + tempMaxNumber / 1024 / 1024 / 8 + " MB");
			}
		}
		catch (ArithmeticException e)
		{
			tempMaxNumber = Long.MAX_VALUE;
			tempUseCache = false;
			if (logger.isInfoEnabled())
			{
				logger.info("\ncache info for domain '" + domainName + "'\nmax. number of pseudonyms for domain: way too high (higher than " + tempMaxNumber + ") --> cache is OFF");
			}
		}
		maxNumberForPSN = tempMaxNumber;
		useCache = tempUseCache;
		lastChar = checkDigits.getAlphabet().getSymbol(checkDigits.getAlphabet().length() - 1);
	}

	private long calculateMaxNumberForPSN() throws ArithmeticException
	{
		int realLength = length;
		if (useLastCharAsDelimiterAfterXChars > 0)
		{
			// weniger zeichen, die zufaellig bestimmt werden
			realLength -= length / useLastCharAsDelimiterAfterXChars;
		}
		return checkDigits.getAlphabet().getMaxNumberOfPSN(realLength, useLastCharAsDelimiterAfterXChars > 0);
	}

	public long getMaxNumberForPSN()
	{
		return maxNumberForPSN;
	}

	public boolean isUseCache()
	{
		return useCache;
	}

	/**
	 * generates a pseudo-random string with x check digits corresponding to the given number. use "-1" to generate a random one
	 *
	 * @return
	 */
	public String getNewPseudonym(long number)
	{
		if (logger.isDebugEnabled())
		{
			logger.debug("generate new pseudonym");
		}
		String result = generateNewPseudonym(number);
		if (logger.isTraceEnabled())
		{
			if (includePrefixInCheckDigitCalculation && includeSuffixInCheckDigitCalculation)
			{
				logger.trace("generate check-digits for " + prefix + result + suffix);
			}
			else if (includePrefixInCheckDigitCalculation)
			{
				logger.trace("generate check-digits for " + prefix + result);
			}
			else if (includeSuffixInCheckDigitCalculation)
			{
				logger.trace("generate check-digits for " + result + suffix);
			}
			else
			{
				logger.trace("generate check-digits for " + result);
			}
		}
		try
		{
			if (includePrefixInCheckDigitCalculation && includeSuffixInCheckDigitCalculation)
			{
				result = prefix + result + checkDigits.generateCheckDigits(prefix + result + suffix) + suffix;
			}
			else if (includePrefixInCheckDigitCalculation)
			{
				result = prefix + result + checkDigits.generateCheckDigits(prefix + result) + suffix;
			}
			else if (includeSuffixInCheckDigitCalculation)
			{
				result = prefix + result + checkDigits.generateCheckDigits(result + suffix) + suffix;
			}
			else
			{
				result = prefix + result + checkDigits.generateCheckDigits(result) + suffix;
			}
		}
		catch (CharNotInAlphabetException e)
		{
			logger.error("this should never happen - must be a program error", e);
			return null;
		}
		catch (InvalidParameterException e)
		{
			logger.error("this should never happen - must be a program error", e);
			return null;
		}
		if (logger.isDebugEnabled())
		{
			logger.debug("new pseudonym: " + result);
		}
		return result;
	}

	private String generateNewPseudonym(long number)
	{
		StringBuilder result = new StringBuilder();
		int nextCharPos;
		long numberCopy = number;
		if (useLastCharAsDelimiterAfterXChars <= 0)
		{
			for (int i = 0; i < length; i++)
			{
				if (number == -1l)
				{
					nextCharPos = rand.nextInt(checkDigits.getAlphabet().length());
				}
				else
				{
					nextCharPos = (int) (numberCopy % checkDigits.getAlphabet().length());
					numberCopy /= checkDigits.getAlphabet().length();
				}
				result.append(checkDigits.getAlphabet().getSymbol(nextCharPos));
			}
		}
		else
		{
			int count = 0;
			for (int i = 0; i < length; i++)
			{
				if (count < useLastCharAsDelimiterAfterXChars)
				{
					if (number == -1l)
					{
						nextCharPos = rand.nextInt(checkDigits.getAlphabet().length() - 1);
					}
					else
					{
						nextCharPos = (int) (numberCopy % (checkDigits.getAlphabet().length() - 1));
						numberCopy /= checkDigits.getAlphabet().length() - 1;
					}
					result.append(checkDigits.getAlphabet().getSymbol(nextCharPos));
					count++;
				}
				else
				{
					result.append(lastChar);
					count = 0;
				}
			}
		}
		return result.toString();
	}

	/**
	 * @param value
	 *            string (including check digits) to be checked
	 * @throws CharNotInAlphabetException
	 * @throws InvalidPSNException
	 */
	public void check(String value) throws CharNotInAlphabetException, InvalidPSNException
	{
		if (logger.isDebugEnabled())
		{
			logger.debug("check pseudonym '" + value + "'");
		}
		if (!prefix.isEmpty() && !value.startsWith(prefix))
		{
			throw new InvalidPSNException("unknown prefix - '" + prefix + "' expected");
		}
		if (!suffix.isEmpty() && !value.endsWith(suffix))
		{
			throw new InvalidPSNException("unknown suffix - '" + suffix + "' expected");
		}
		int realLength = length;
		if (includePrefixInCheckDigitCalculation && includeSuffixInCheckDigitCalculation)
		{
			realLength = value.length();
		}
		else if (includePrefixInCheckDigitCalculation)
		{
			realLength += prefix.length();
			value = value.substring(0, value.length() - suffix.length());
		}
		else if (includeSuffixInCheckDigitCalculation)
		{
			realLength += suffix.length();
			value = value.substring(prefix.length(), value.length());
		}
		else
		{
			value = value.substring(prefix.length(), value.length() - suffix.length());
		}
		checkDigits.check(value, realLength);
	}

	public long getPosNumberForPSN(String psn) throws ArithmeticException, CharNotInAlphabetException
	{
		String purePsn = psn.substring(prefix.length(), prefix.length() + length);
		if (useLastCharAsDelimiterAfterXChars > 0)
		{
			purePsn = purePsn.replace("" + lastChar, "");
		}
		return checkDigits.getAlphabet().getPosNumberForPSN(purePsn, useLastCharAsDelimiterAfterXChars > 0);
	}
}
