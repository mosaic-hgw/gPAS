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
import org.emau.icmvc.ganimed.ttp.psn.exceptions.MessageTooLargeException;

/**
 * hamming code implementation
 * <p>
 * code generation via generator polynomial (depends on message length - see functions "hamming_x")
 * 
 * @author geidell
 * 
 */
public class HammingCode extends CheckDigits {

	// zuordnung max. anzahl nachricht-zeichen und grad des generatorpolynoms (entspricht der anzahl pruefzeichen)
	private static final int[][] HAMMING_CODE_LENGTHS = { { 1, 2 }, { 4, 3 }, { 11, 4 }, { 26, 5 }, { 57, 6 }, { 120, 7 } };
	private static final Logger logger = Logger.getLogger(HammingCode.class);

	private final int alphaLength;

	public HammingCode(Alphabet alphabet, Map<GeneratorProperties, String> properties) throws InvalidAlphabetException {
		super(alphabet, properties);
		alphaLength = alphabet.length();
		// TODO der algorithmus ist noch nicht richtig verstanden; er funktioniert mit einem 32-symboligen alphabet,
		// mit einigen anderen aber nicht; liegt wahrscheinlich an den generatorpolynomen - eventuell muessen diese erzeugt werden
		// if(!MathUtil.isPrimePower(alphaLength)) {
		// String message = "the length of the given alphabet (" + alphaLength
		// + ") is not a prime power, but for mathematical reasons needs to be one (e.g. 8=2^3 or 11=11^1)";
		// logger.error(message);
		// throw new InvalidAlphabetException(message);
		// }
		if (alphaLength != 32) {
			String message = "the length of the given alphabet (" + alphaLength + ") needs to be 32";
			logger.error(message);
			throw new InvalidAlphabetException(message);
		}
		logger.info("initialised hamming code generator with the alphabet: " + alphabet);
	}

	@Override
	public String generateCheckDigits(String message) throws CharNotInAlphabetException, MessageTooLargeException {
		// implementation nach dem algorithmus des schieberegister-hamming-decoders
		if (logger.isDebugEnabled()) {
			logger.debug("add check digigts for message '" + message + "'");
		}
		// hammingValues[0] = laenge nachricht; hammingValues[1] = anzahl pruefzeichen
		final int[] hammingValues = getHammingValuesFor(message);
		int[] register = new int[hammingValues[1]];
		String paddedMessage = padMessage(message, hammingValues[1]);
		if (logger.isTraceEnabled()) {
			logger.trace("paddedMessage: " + paddedMessage);
		}
		// die zeichen wandern richtung register[0]
		// register vorfuellen - es passiert erst etwas, wenn das erste zeichen das register[0] verlaesst
		for (int i = 0; i < register.length; i++) {
			register[i] = getAlphabet().getPosForSymbol(paddedMessage.charAt(i));
		}
		switch (hammingValues[1]) {
			case 2:
				register = hamming_2(paddedMessage, register);
			break;
			case 3:
				register = hamming_3(paddedMessage, register);
			break;
			case 4:
				register = hamming_4(paddedMessage, register);
			break;
			case 5:
				register = hamming_5(paddedMessage, register);
			break;
			case 6:
				register = hamming_6(paddedMessage, register);
			break;
			case 7:
				register = hamming_7(paddedMessage, register);
			break;
		}
		String checkDigits = registerToString(register);
		if (logger.isTraceEnabled()) {
			logger.trace("check digits: " + checkDigits);
		}
		return checkDigits;
	}

	/**
	 * returns the hamming values for a string WITHOUT checkdigits
	 * 
	 * @param message
	 * @return
	 * @throws MessageTooLargeException
	 */
	private int[] getHammingValuesFor(String message) throws MessageTooLargeException {
		int[] result = { -1, -1 };
		for (int i = 0; i < HAMMING_CODE_LENGTHS.length; i++) {
			if (HAMMING_CODE_LENGTHS[i][0] >= message.length()) {
				result = HAMMING_CODE_LENGTHS[i];
				break;
			}
		}
		if (result[0] == -1) {
			throw new MessageTooLargeException("message '" + message + "' is too long - max message length: "
					+ HAMMING_CODE_LENGTHS[HAMMING_CODE_LENGTHS.length - 1][0]);
		}
		if (logger.isTraceEnabled()) {
			logger.trace("hamming values: " + result[0] + " " + result[1]);
		}
		return result;
	}

	/**
	 * returns the hamming values for a string INCLUDING checkdigits
	 * 
	 * @param psn
	 * @return
	 * @throws InvalidPSNException
	 * @throws MessageTooLargeException
	 */
	private int[] getHammingValuesForPSN(String psn) throws InvalidPSNException {
		int[] result = { -1, -1 };
		if (HAMMING_CODE_LENGTHS[0][0] + HAMMING_CODE_LENGTHS[0][1] > psn.length()) {
			throw new InvalidPSNException("length of the given psn (" + psn.length() + ") can't belong to a valid hamming-code");
		}
		for (int i = 0; i < HAMMING_CODE_LENGTHS.length; i++) {
			if (HAMMING_CODE_LENGTHS[i][0] + HAMMING_CODE_LENGTHS[i][1] >= psn.length()) {
				result = HAMMING_CODE_LENGTHS[i];
				break;
			} else if (HAMMING_CODE_LENGTHS[i][0] + HAMMING_CODE_LENGTHS[i][1] + 1 == psn.length()) {
				throw new InvalidPSNException("length of the given psn (" + psn.length() + ") can't belong to a valid hamming-code");
			}
		}
		if (result[0] == -1) {
			throw new InvalidPSNException("psn '" + psn + "' is too long - max psn length: "
					+ (HAMMING_CODE_LENGTHS[HAMMING_CODE_LENGTHS.length - 1][0] + HAMMING_CODE_LENGTHS[HAMMING_CODE_LENGTHS.length - 1][1]));
		}
		if (logger.isTraceEnabled()) {
			logger.trace("selected hamming values for psn '" + psn + "': " + result[0] + " " + result[1]);
		}
		return result;
	}

	/**
	 * pad the message with null-symbols for the generation of check digits by polynomial division
	 * 
	 * @param message
	 * @param count
	 * @return
	 */
	private String padMessage(String message, int count) {
		StringBuilder sb = new StringBuilder(message);
		char nullSymbol = getAlphabet().getSymbol(0);
		for (int i = 0; i < count; i++) {
			sb.append(nullSymbol);
		}
		return sb.toString();
	}

	/**
	 * generator polynomial: x^2+x+1
	 * 
	 * @param message
	 * @param register
	 * @return check digits (int values)
	 * @throws CharNotInAlphabetException
	 */
	private int[] hamming_2(String message, int[] register) throws CharNotInAlphabetException {
		for (int i = register.length; i < message.length(); i++) {
			// schieben, bis das letzte zeichen im register ist
			if (logger.isTraceEnabled()) {
				logger.trace("register: " + register[0] + " " + register[1] + " <-- " + message.charAt(i));
			}
			int quotient = register[0];
			// + alphaLength, damit das nicht negativ wird
			register[0] = (register[1] + alphaLength - quotient) % alphaLength;
			register[1] = (getAlphabet().getPosForSymbol(message.charAt(i)) - quotient + alphaLength) % alphaLength;
		}
		return register;
	}

	/**
	 * generator polynomial: x^3+x+1
	 * 
	 * @param message
	 * @param register
	 * @return check digits (int values)
	 * @throws CharNotInAlphabetException
	 */
	private int[] hamming_3(String message, int[] register) throws CharNotInAlphabetException {
		for (int i = register.length; i < message.length(); i++) {
			// schieben, bis das letzte zeichen im register ist
			if (logger.isTraceEnabled()) {
				logger.trace("register: " + register[0] + " " + register[1] + " " + register[2] + " <-- " + message.charAt(i));
			}
			int quotient = register[0];
			register[0] = register[1];
			// + alphaLength, damit das nicht negativ wird
			register[1] = (register[2] + alphaLength - quotient) % alphaLength;
			register[2] = (getAlphabet().getPosForSymbol(message.charAt(i)) - quotient + alphaLength) % alphaLength;
		}
		return register;
	}

	/**
	 * generator polynomial: x^4+x+1
	 * 
	 * @param message
	 * @param register
	 * @return check digits (int values)
	 * @throws CharNotInAlphabetException
	 */
	private int[] hamming_4(String message, int[] register) throws CharNotInAlphabetException {
		for (int i = register.length; i < message.length(); i++) {
			// schieben, bis das letzte zeichen im register ist
			if (logger.isTraceEnabled()) {
				logger.trace("register: " + register[0] + " " + register[1] + " " + register[2] + " " + register[3] + " <-- " + message.charAt(i));
			}
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
	 * generator polynomial: x^5+x^2+1
	 * 
	 * @param message
	 * @param register
	 * @return check digits (int values)
	 * @throws CharNotInAlphabetException
	 */
	private int[] hamming_5(String message, int[] register) throws CharNotInAlphabetException {
		for (int i = register.length; i < message.length(); i++) {
			// schieben, bis das letzte zeichen im register ist
			if (logger.isTraceEnabled()) {
				logger.trace("register: " + register[0] + " " + register[1] + " " + register[2] + " " + register[3] + " " + register[4] + " <-- "
						+ message.charAt(i));
			}
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
	 * generator polynomial: x^6+x+1
	 * 
	 * @param message
	 * @param register
	 * @return check digits (int values)
	 * @throws CharNotInAlphabetException
	 */
	private int[] hamming_6(String message, int[] register) throws CharNotInAlphabetException {
		for (int i = register.length; i < message.length(); i++) {
			// schieben, bis das letzte zeichen im register ist
			if (logger.isTraceEnabled()) {
				logger.trace("register: " + register[0] + " " + register[1] + " " + register[2] + " " + register[3] + " " + register[4] + " "
						+ register[5] + " <-- " + message.charAt(i));
			}
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
	 * generator polynomial: x^7+x^3+1
	 * 
	 * @param message
	 * @param register
	 * @return check digits (int values)
	 * @throws CharNotInAlphabetException
	 */
	private int[] hamming_7(String message, int[] register) throws CharNotInAlphabetException {
		for (int i = register.length; i < message.length(); i++) {
			// schieben, bis das letzte zeichen im register ist
			if (logger.isTraceEnabled()) {
				logger.trace("register: " + register[0] + " " + register[1] + " " + register[2] + " " + register[3] + " " + register[4] + " "
						+ register[5] + " " + register[6] + " <-- " + message.charAt(i));
			}
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
	 * @param register
	 * @return
	 */
	private String registerToString(int[] register) {
		// fuer ein beliebiges wort w und das generatorpolynom g gilt: w = x*g+r
		// -> darstellung von w als element des hamming-codes: w-r = x*g
		// rest r der polynomdivision steht im register
		// deswegen werden die pruefzeichen mit "(alphaLength - register[i]) % alphaLength)" berechnet
		// in register[0] steht der koeffizient zum hoechstwertigen term, deswegen bleibt die reihenfolge bestehen
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < register.length; i++) {
			sb.append(getAlphabet().getSymbol((alphaLength - register[i]) % alphaLength));
		}
		return sb.toString();
	}

	@Override
	public void check(String value, int messageLength) throws CharNotInAlphabetException, InvalidPSNException {
		// implementation nach dem algorithmus des schieberegister-hamming-decoders
		boolean result = false;
		if (logger.isDebugEnabled()) {
			logger.debug("check message '" + value + "'");
		}
		int[] hammingValues;
		try {
			hammingValues = getHammingValuesForPSN(value);
		} catch (InvalidPSNException e) {
			logger.info("error while checking psn", e);
			throw e;
		}
		if (value.length() != messageLength + hammingValues[1]) {
			String message = "invalid value '" + value + "' - it should have a length of " + (messageLength + hammingValues[1]);
			logger.info(message);
			throw new InvalidPSNException(message);
		}
		try {
			// hammingValues[0] = max. laenge nachricht; hammingValues[1] = anzahl pruefzeichen
			int[] register = new int[hammingValues[1]];
			// die zeichen wandern richtung register[0]
			// register vorfuellen - es passiert erst etwas, wenn das erste zeichen das register[0] verlaesst
			for (int i = 0; i < register.length; i++) {
				register[i] = getAlphabet().getPosForSymbol(value.charAt(i));
			}
			switch (hammingValues[1]) {
				case 2:
					register = hamming_2(value, register);
				break;
				case 3:
					register = hamming_3(value, register);
				break;
				case 4:
					register = hamming_4(value, register);
				break;
				case 5:
					register = hamming_5(value, register);
				break;
				case 6:
					register = hamming_6(value, register);
				break;
				case 7:
					register = hamming_7(value, register);
				break;
			}
			if (logger.isTraceEnabled()) {
				StringBuilder sb = new StringBuilder("register:");
				for (int i = 0; i < register.length; i++) {
					sb.append(" " + register[i]);
				}
				logger.trace(sb.toString());
			}
			int check = 0;
			// wenn der string ein valider hamming-code ist, dann sind jetzt im register nur nullen
			// ( -> division durch das generator-polynom ergibt rest 0)
			for (int i = 0; i < register.length; i++) {
				check |= register[i];
			}
			result = check == 0;
			if (logger.isDebugEnabled()) {
				logger.debug("result of check: " + result);
			}
		} catch (CharNotInAlphabetException e) {
			logger.info("error while checking psn", e);
			throw e;
		}
		if (!result) {
			String message = "invalid check digits for '" + value + "'";
			logger.info(message);
			throw new InvalidPSNException(message);
		}
	}
}
