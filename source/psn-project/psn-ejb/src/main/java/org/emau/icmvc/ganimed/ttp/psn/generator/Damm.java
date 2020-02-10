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
 * check digit generation and validation (damm-algorithm)<br>
 * improvement over verhoeff: permutation isn't required anymore see Damm, H. Michael (2004). Total anti-symmetrische Quasigruppen (http://archiv.ub.uni-marburg.de/diss/z2004/0516/pdf/dhmd.pdf)
 * 
 * @author geidell
 */
public class Damm extends CheckDigits {

	private static final int[][] op = { { 0, 3, 1, 7, 5, 9, 8, 6, 4, 2 },//
			{ 7, 0, 9, 2, 1, 5, 4, 8, 6, 3 },//
			{ 4, 2, 0, 6, 8, 7, 1, 3, 5, 9 },//
			{ 1, 7, 5, 0, 9, 8, 3, 4, 2, 6 },//
			{ 6, 1, 2, 3, 0, 4, 5, 9, 7, 8 },//
			{ 3, 6, 7, 4, 2, 0, 9, 5, 8, 1 },//
			{ 5, 8, 6, 9, 7, 2, 0, 1, 3, 4 },//
			{ 8, 9, 4, 5, 3, 6, 2, 0, 1, 7 },//
			{ 9, 4, 3, 8, 6, 1, 7, 2, 0, 5 },//
			{ 2, 5, 8, 1, 4, 3, 6, 7, 9, 0 } };

	private static final Logger logger = Logger.getLogger(Damm.class);

	public Damm(Alphabet alphabet, Map<GeneratorProperties, String> properties) throws InvalidAlphabetException {
		super(alphabet, properties);
		if (alphabet.length() != 10) {
			String message = "the length of the given alphabet needs to be 10";
			logger.error(message);
			throw new InvalidAlphabetException(message);
		}
		logger.info("initialised damm generator with the alphabet: " + alphabet);
	}

	@Override
	public String generateCheckDigits(String message) throws CharNotInAlphabetException {
		if (logger.isDebugEnabled()) {
			logger.debug("add check digigts for message '" + message + "'");
		}
		return "" + calculateCheckDigit(message);
	}

	@Override
	public void check(String value, int messageLength) throws CharNotInAlphabetException, InvalidPSNException {
		if (logger.isDebugEnabled()) {
			logger.debug("check message '" + value + "'");
		}
		if (value.length() != messageLength + 1) {
			String message = "invalid value '" + value + "' - it should have a length of " + (messageLength + 1);
			logger.info(message);
			throw new InvalidPSNException(message);
		}
		// letztes zeichen des gegebenen strings und neu berechnetes pruefzeichen
		int lastPos = value.length() - 1;
		if (!(value.charAt(lastPos) == calculateCheckDigit(value.substring(0, lastPos)))) {
			String message = "invalid check digits for '" + value + "'";
			logger.info(message);
			throw new InvalidPSNException(message);
		}
	}

	private char calculateCheckDigit(String message) throws CharNotInAlphabetException {
		int check = 0;
		for (int i = message.length(); i > 0; i--) {
			check = op[check][getAlphabet().getPosForSymbol(message.charAt(i - 1))];
		}
		return getAlphabet().getSymbol(check);
	}
}
