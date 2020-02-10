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

/**
 * possible properties for a generator for pseudo-random strings
 * 
 * @author geidell
 * 
 */
public enum GeneratorProperties {

	/**
	 * numbers of check digits
	 */
	MAX_DETECTED_ERRORS,
	/**
	 * length of the generated pseudonym
	 */
	PSN_LENGTH,
	/**
	 * additional prefix
	 */
	PSN_PREFIX,
	/**
	 * additional suffix
	 */
	PSN_SUFFIX,
	/**
	 * should the prefix be used to calculate the check digit(s)
	 */
	INCLUDE_PREFIX_IN_CHECK_DIGIT_CALCULATION,
	/**
	 * is it allowed to delete entries within this project
	 */
	PSNS_DELETABLE;
}
