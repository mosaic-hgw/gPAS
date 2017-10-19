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


import org.apache.log4j.Logger;

/**
 * 
 * @author geidell
 *
 */
public class LagrangeForReedSolomon {

	private static final Logger logger = Logger.getLogger(LagrangeForReedSolomon.class);

	private final int[] values;
	private final int n;
	private final int[] inverses;
	private final int[][] multiplication;

	public LagrangeForReedSolomon(int[] values, int n) {
		if (logger.isTraceEnabled()) {
			StringBuilder sb = new StringBuilder("create lagrange interpolation for reed solomon for value pairs");
			for (int i = 0; i < values.length; i++) {
				sb.append(" (" + i + "," + values[i] + ")");
			}
			logger.trace(sb.toString());
		}
		this.values = values;
		this.n = n;
		inverses = new int[n];
		// TODO brute force, um die inversen zu finden - besser was intelligentes nutzen (z.b. extended euclidean)
		inverses[1] = 1;
		for (int i = 2; i < n; i++) {
			if (inverses[i] == 0) {
				for (int j = 2; j < n; j++) {
					if (i * j % n == 1) {
						inverses[i] = j;
						inverses[j] = i;
						break;
					}
				}
			}
		}
		multiplication = new int[n][n];
		for (int i = 1; i < n; i++) {
			for (int j = i; j < n; j++) {
				int multi = (i * j) % n;
				multiplication[i][j] = multi;
				multiplication[j][i] = multi;
			}
		}
	}

	public int calculateFor(int value) {
		if (logger.isTraceEnabled()) {
			logger.trace("calculate for value " + value);
		}
		int result = 0;
		if (value < values.length) {
			// stuetzstelle des polynoms -> wert des polynoms = originalwert
			result = values[value];
		} else {
			// lagrange-interpolation; stuetzstellen 0..value.length-1
			for (int i = 0; i < values.length; i++) {
				if (values[i] != 0) {
					int product = 1;
					for (int j = 0; j < values.length; j++) {
						if (j != i) {
							// die hier eigentlich hingehoerende division erfolgt durch die multiplikation mit dem inversen
							int productTerm = multiplication[(value - j + n) % n][inverses[(i - j + n) % n]];
							product = multiplication[product][productTerm];
						}
					}
					result += multiplication[product][values[i]];
					result %= n;
				}
			}
		}
		if (logger.isTraceEnabled()) {
			logger.trace("calculated value for " + value + " = " + result);
		}
		return result;
	}
}
