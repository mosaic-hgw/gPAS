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

import org.emau.icmvc.ganimed.ttp.psn.config.DomainConfig;
import org.emau.icmvc.ganimed.ttp.psn.enums.GeneratorAlphabetRestriction;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.CharNotInAlphabetException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.InvalidAlphabetException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.InvalidPSNException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.InvalidParameterException;

public class HammingCode extends CheckDigits
{
	@Serial
	private static final long serialVersionUID = 2674937813802782625L;

	// zuordnung max. anzahl nachricht-zeichen und grad des generatorpolynoms (entspricht der anzahl pruefzeichen)
	private static final int[][] HAMMING_CODE_LENGTHS = { { 1, 2 }, { 4, 3 }, { 11, 4 }, { 26, 5 }, { 57, 6 }, { 120, 7 } };
	private final int alphaLength;

	public HammingCode()
	{
		alphaLength = 0;
	}

	public HammingCode(Alphabet alphabet, DomainConfig config) throws InvalidAlphabetException
	{
		super(alphabet);
		alphaLength = alphabet.length();
		getAlphabetRestriction().validateAlphabet(alphabet);
		logger.info("initialised {}", this);
	}

	@Override
	public GeneratorAlphabetRestriction getAlphabetRestriction()
	{
		// TODO der algorithmus ist noch nicht richtig verstanden; er funktioniert mit einem
		// 32-symboligen alphabet,
		// mit einigen anderen aber nicht; liegt wahrscheinlich an den generatorpolynomen -
		// eventuell muessen diese erzeugt werden
		// if(!MathUtil.isPrimePower(alphaLength)) {
		// String message = "the length of the given alphabet (" + alphaLength
		// + ") is not a prime power, but for mathematical reasons needs to be one (e.g. 8=2^3 or
		// 11=11^1)";
		// logger.error(message);
		// throw new InvalidAlphabetException(message);
		// }
		return GeneratorAlphabetRestriction.CONST_32;
	}

	@Override
	public String generateCheckDigits(String message) throws CharNotInAlphabetException, InvalidParameterException
	{
		// implementation nach dem algorithmus des schieberegister-hamming-decoders
		logger.debug("add check digigts for message '{}'", message);
		// hammingValues[0] = laenge nachricht; hammingValues[1] = anzahl pruefzeichen
		final int[] hammingValues = getHammingValuesFor(message);
		String paddedMessage = padMessage(message, hammingValues[1]);
		logger.trace("paddedMessage: {}", paddedMessage);
		int[] register = createRegister(paddedMessage, hammingValues);
		String checkDigits = registerToString(register);
		logger.trace("check digits: {}", checkDigits);
		return checkDigits;
	}

	@Override
	public int getMaxMessageLength()
	{
		return HAMMING_CODE_LENGTHS[5][0];
	}

	@Override
	public int getEffectiveMaxMessageLength(int total)
	{
		if (total >= HAMMING_CODE_LENGTHS[5][0] + HAMMING_CODE_LENGTHS[5][1])
		{
			return getMaxMessageLength();
		}
		for (int[] element : HAMMING_CODE_LENGTHS)
		{
			if (element[0] + element[1] >= total)
			{
				return total - element[1];
			}
		}
		return super.getEffectiveMaxMessageLength(total);
	}

	@Override
	public int getNumberOfCheckDigits(int messageLength)
	{
		return computeNumberOfCheckDigits(messageLength);
	}

	public static int computeNumberOfCheckDigits(int messageLength)
	{
		return getHammingValuesFor(messageLength)[1];
	}

	private static int[] getHammingValuesFor(int messageLength)
	{
		int[] result = { -1, -1 };
		for (int[] element : HAMMING_CODE_LENGTHS)
		{
			if (element[0] >= messageLength)
			{
				result = element;
				break;
			}
		}
		// in particular for messages lengths greater than 120 we will return [-1, -1]
		return result;
	}

	/**
	 * {@return the hamming values for a string WITHOUT checkdigits}
	 *
	 * @param message the message
	 * @throws InvalidParameterException for an invalid message
	 */
	private int[] getHammingValuesFor(String message) throws InvalidParameterException
	{
		int messageLength = message.length();
		int[] result = getHammingValuesFor(messageLength);
		if (result[0] == -1)
		{
			throw new InvalidParameterException(
					"message '" + message + "' is too long - max message length: " + HAMMING_CODE_LENGTHS[HAMMING_CODE_LENGTHS.length - 1][0]);
		}
		logger.trace("hamming values: {} {}", result[0], result[1]);
		return result;
	}

	/**
	 * {@return the hamming values for a string INCLUDING check digits}
	 *
	 * @param psn the pseudonym (including check digits
	 * @throws InvalidPSNException for an invalid pseudonym
	 */
	private int[] getHammingValuesForPSN(String psn) throws InvalidPSNException
	{
		int[] result = { -1, -1 };
		if (HAMMING_CODE_LENGTHS[0][0] + HAMMING_CODE_LENGTHS[0][1] > psn.length())
		{
			throw new InvalidPSNException("length of the given psn (" + psn.length() + ") can't belong to a valid hamming-code");
		}
		for (int[] element : HAMMING_CODE_LENGTHS)
		{
			if (element[0] + element[1] >= psn.length())
			{
				result = element;
				break;
			}
			else if (element[0] + element[1] + 1 == psn.length())
			{
				throw new InvalidPSNException("length of the given psn (" + psn.length() + ") can't belong to a valid hamming-code");
			}
		}
		if (result[0] == -1)
		{
			throw new InvalidPSNException("psn '" + psn + "' is too long - max psn length: "
					+ (HAMMING_CODE_LENGTHS[HAMMING_CODE_LENGTHS.length - 1][0] + HAMMING_CODE_LENGTHS[HAMMING_CODE_LENGTHS.length - 1][1]));
		}
		logger.trace("selected hamming values for psn '{}': {} {}", psn, result[0], result[1]);
		return result;
	}

	/**
	 * pad the message with null-symbols for the generation of check digits by polynomial division
	 *
	 * @param message the message to pad
	 * @param count the number of characters to pad
	 * @return the padded message
	 */
	private String padMessage(String message, int count)
	{
		StringBuilder sb = new StringBuilder(message);
		char nullSymbol = getAlphabet().getSymbol(0);
		sb.append(String.valueOf(nullSymbol).repeat(Math.max(0, count)));
		return sb.toString();
	}

	/**
	 * {@return check digits (int values) for generator polynomial: x^2+x+1}
	 *
	 * @param message the message
	 * @param register the register
	 * @throws CharNotInAlphabetException if message contains illegal characters
	 */
	private int[] hamming_2(String message, int[] register) throws CharNotInAlphabetException
	{
		for (int i = register.length; i < message.length(); i++)
		{
			// schieben, bis das letzte zeichen im register ist
			logger.trace("register: {} {} <-- {}", register[0], register[1], message.charAt(i));
			int quotient = register[0];
			// + alphaLength, damit das nicht negativ wird
			register[0] = (register[1] + alphaLength - quotient) % alphaLength;
			register[1] = (getAlphabet().getPosForSymbol(message.charAt(i)) - quotient + alphaLength) % alphaLength;
		}
		return register;
	}

	/**
	 * {@return check digits (int values) for generator polynomial: x^3+x+1}
	 *
	 * @param message the message
	 * @param register the register
	 * @throws CharNotInAlphabetException if message contains illegal characters
	 */
	private int[] hamming_3(String message, int[] register) throws CharNotInAlphabetException
	{
		for (int i = register.length; i < message.length(); i++)
		{
			// schieben, bis das letzte zeichen im register ist
			logger.trace("register: {} {} {} <-- {}", register[0], register[1], register[2], message.charAt(i));
			int quotient = register[0];
			register[0] = register[1];
			// + alphaLength, damit das nicht negativ wird
			register[1] = (register[2] + alphaLength - quotient) % alphaLength;
			register[2] = (getAlphabet().getPosForSymbol(message.charAt(i)) - quotient + alphaLength) % alphaLength;
		}
		return register;
	}

	/**
	 * {@return check digits (int values) for generator polynomial: x^4+x+1}
	 *
	 * @param message the message
	 * @param register the register
	 * @throws CharNotInAlphabetException if message contains illegal characters
	 */
	private int[] hamming_4(String message, int[] register) throws CharNotInAlphabetException
	{
		for (int i = register.length; i < message.length(); i++)
		{
			// schieben, bis das letzte zeichen im register ist
			logger.trace("register: {} {} {} {} <-- {}", register[0], register[1], register[2], register[3], message.charAt(i));
			int quotient = register[0];
			register[0] = register[1];
			register[1] = register[2];
			// + alphaLength, damit das nicht negativ wird
			register[2] = (register[3] + alphaLength - quotient) % alphaLength;
			register[3] = (getAlphabet().getPosForSymbol(message.charAt(i)) - quotient + alphaLength) % alphaLength;
		}
		return register;
	}

	/**
	 * {@return check digits (int values) for generator polynomial: x^5+x^2+1}
	 *
	 * @param message the message
	 * @param register the register
	 * @throws CharNotInAlphabetException if message contains illegal characters
	 */
	private int[] hamming_5(String message, int[] register) throws CharNotInAlphabetException
	{
		for (int i = register.length; i < message.length(); i++)
		{
			// schieben, bis das letzte zeichen im register ist
			logger.trace("register: {} {} {} {} {} <-- {}", register[0], register[1], register[2], register[3], register[4], message.charAt(i));
			int quotient = register[0];
			register[0] = register[1];
			register[1] = register[2];
			// + alphaLength, damit das nicht negativ wird
			register[2] = (register[3] + alphaLength - quotient) % alphaLength;
			register[3] = register[4];
			register[4] = (getAlphabet().getPosForSymbol(message.charAt(i)) - quotient + alphaLength) % alphaLength;
		}
		return register;
	}

	/**
	 * {@return check digits (int values) for generator polynomial: x^6+x+1}
	 *
	 * @param message the message
	 * @param register the register
	 * @throws CharNotInAlphabetException if message contains illegal characters
	 */
	private int[] hamming_6(String message, int[] register) throws CharNotInAlphabetException
	{
		for (int i = register.length; i < message.length(); i++)
		{
			// schieben, bis das letzte zeichen im register ist
			logger.trace("register: {} {} {} {} {} {} <-- {}", register[0], register[1], register[2], register[3], register[4], register[5], message.charAt(i));
			int quotient = register[0];
			register[0] = register[1];
			register[1] = register[2];
			register[2] = register[3];
			register[3] = register[4];
			// + alphaLength, damit das nicht negativ wird
			register[4] = (register[5] + alphaLength - quotient) % alphaLength;
			register[5] = (getAlphabet().getPosForSymbol(message.charAt(i)) - quotient + alphaLength) % alphaLength;
		}
		return register;
	}

	/**
	 * {@return check digits (int values) for generator polynomial: x^7+x^3+1}
	 *
	 * @param message the message
	 * @param register the register
	 * @throws CharNotInAlphabetException if message contains illegal characters
	 */
	private int[] hamming_7(String message, int[] register) throws CharNotInAlphabetException
	{
		for (int i = register.length; i < message.length(); i++)
		{
			// schieben, bis das letzte zeichen im register ist
			logger.trace("register: {} {} {} {} {} {} {} <-- {}", register[0], register[1], register[2], register[3], register[4], register[5], register[6], message.charAt(i));
			int quotient = register[0];
			register[0] = register[1];
			register[1] = register[2];
			register[2] = register[3];
			// + alphaLength, damit das nicht negativ wird
			register[3] = (register[4] + alphaLength - quotient) % alphaLength;
			register[4] = register[5];
			register[5] = register[6];
			register[6] = (getAlphabet().getPosForSymbol(message.charAt(i)) - quotient + alphaLength) % alphaLength;
		}
		return register;
	}

	/**
	 * converts a filled register to a string within the given alphabet
	 *
	 * @param register the filled register
	 * @return the string representation for the register
	 */
	private String registerToString(int[] register)
	{
		// fuer ein beliebiges wort w und das generatorpolynom g gilt: w = x*g+r
		// -> darstellung von w als element des hamming-codes: w-r = x*g
		// rest r der polynomdivision steht im register
		// deswegen werden die pruefzeichen mit "(alphaLength - register[i]) % alphaLength)"
		// berechnet
		// in register[0] steht der koeffizient zum hoechstwertigen term, deswegen bleibt die
		// reihenfolge bestehen
		StringBuilder sb = new StringBuilder();
		for (int element : register)
		{
			sb.append(getAlphabet().getSymbol((alphaLength - element) % alphaLength));
		}
		return sb.toString();
	}

	@Override
	public void check(String value, int messageLength) throws CharNotInAlphabetException, InvalidPSNException
	{
		// implementation nach dem algorithmus des schieberegister-hamming-decoders
		boolean result;
		logger.debug("check message '{}'", value);
		int[] hammingValues;
		hammingValues = getHammingValuesForPSN(value);

		if (value.length() != messageLength + hammingValues[1])
		{
			String message = "invalid value '" + value + "' - it should have a length of " + (messageLength + hammingValues[1]);
			logger.info(message);
			throw new InvalidPSNException(message);
		}

		// hammingValues[0] = max. laenge nachricht; hammingValues[1] = anzahl pruefzeichen
		int[] register = createRegister(value, hammingValues);
		if (logger.isTraceEnabled())
		{
			StringBuilder sb = new StringBuilder("register:");
			for (int element : register)
			{
				sb.append(" ").append(element);
			}
			logger.trace(sb.toString());
		}
		int check = 0;
		// wenn der string ein valider hamming-code ist, dann sind jetzt im register nur nullen
		// ( -> division durch das generator-polynom ergibt rest 0)
		for (int element : register)
		{
			check |= element;
		}
		result = check == 0;
		logger.debug("result of check: {}", result);

		if (!result)
		{
			String message = "invalid check digits for '" + value + "'";
			logger.info(message);
			throw new InvalidPSNException(message);
		}
	}

	private int[] createRegister(String value, int[] hammingValues) throws CharNotInAlphabetException
	{
		int[] register = new int[hammingValues[1]];
		// die zeichen wandern richtung register[0]
		// register vorfuellen - es passiert erst etwas, wenn das erste zeichen das register[0]
		// verlaesst
		for (int i = 0; i < register.length; i++)
		{
			register[i] = getAlphabet().getPosForSymbol(value.charAt(i));
		}
		return switch (hammingValues[1])
		{
			case 2 -> hamming_2(value, register);
			case 3 -> hamming_3(value, register);
			case 4 -> hamming_4(value, register);
			case 5 -> hamming_5(value, register);
			case 6 -> hamming_6(value, register);
			case 7 -> hamming_7(value, register);
			default -> register;
		};
	}
}
