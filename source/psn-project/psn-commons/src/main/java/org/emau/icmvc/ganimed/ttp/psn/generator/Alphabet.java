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


import java.util.HashMap;
import java.util.Map;

import org.emau.icmvc.ganimed.ttp.psn.exceptions.CharNotInAlphabetException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.InvalidAlphabetException;

public abstract class Alphabet {

	private final char[] symbols;
	private final Map<Character, Integer> mapForFasterAccess = new HashMap<Character, Integer>();

	public Alphabet() {
		symbols = getAlphabet();
		for (int i = 0; i < symbols.length; i++) {
			mapForFasterAccess.put(symbols[i], i);
		}
	}

	protected Alphabet(String charString) throws InvalidAlphabetException {
		String[] chars = charString.split(",");
		for (int i = 0; i < chars.length; i++) {
			chars[i] = chars[i].trim();
		}
		symbols = new char[chars.length];
		for (int i = 0; i < chars.length; i++) {
			if (chars[i].length() != 1) {
				throw new InvalidAlphabetException("invalid char: " + chars[i]);
			}
			for (int j = i + 1; j < chars.length; j++) {
				if (chars[i].equals(chars[j])) {
					throw new InvalidAlphabetException("duplicate char: " + chars[i]);
				}
			}
			symbols[i] = chars[i].charAt(0);
		}
		for (int i = 0; i < symbols.length; i++) {
			mapForFasterAccess.put(symbols[i], i);
		}
	}

	protected abstract char[] getAlphabet();

	/**
	 * 
	 * @return length of the alphabet
	 */
	public int length() {
		return symbols.length;
	}

	/**
	 * 
	 * @param pos
	 *            position of the requested symbol (0 - length-1)
	 * @return symbol at the given position
	 * @throws IndexOutOfBoundsException
	 *             if the given position is larger then {@link Alphabet#length()}
	 */
	public char getSymbol(int pos) throws IndexOutOfBoundsException {
		if (pos >= symbols.length || pos < 0) {
			throw new IndexOutOfBoundsException("requested pos: " + pos + " max pos: " + (symbols.length - 1));
		}
		return symbols[pos];
	}

	/**
	 * 
	 * @param symbol
	 *            symbol for which the position is requested
	 * @return position of the requested symbol
	 * @throws CharNotInAlphabetException
	 *             if the given symbol is not an element of this alphabet
	 */
	public int getPosForSymbol(char symbol) throws CharNotInAlphabetException {
		Integer result = mapForFasterAccess.get(symbol);
		if (result == null) {
			throw new CharNotInAlphabetException("char " + symbol + " is not an element of this alphabet");
		}
		return result;
	}

	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		for (int i = 0; i < symbols.length; i++) {
			if (i + 1 == symbols.length) {
				result.append(symbols[i]);
			} else {
				result.append(symbols[i] + " ");
			}
		}
		return result.toString();
	}
}
