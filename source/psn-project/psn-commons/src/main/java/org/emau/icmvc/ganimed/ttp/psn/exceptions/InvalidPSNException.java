package org.emau.icmvc.ganimed.ttp.psn.exceptions;

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


import javax.xml.bind.annotation.XmlType;

/**
 * should be thrown, if the length of the given alphabet is not valid for the hamming-code algorithm (needs to be a power of a prime)
 * 
 * @author geidell
 *
 */
@XmlType(name = "InvalidPSNExceptionType", namespace = "http://psn.ttp.ganimed.icmvc.emau.org/")
public class InvalidPSNException extends Exception {

	private static final long serialVersionUID = 9049957235690186823L;

	public InvalidPSNException() {
		super();
	}

	public InvalidPSNException(String message, Throwable cause) {
		super(message, cause);
	}

	public InvalidPSNException(String message) {
		super(message);
	}

	public InvalidPSNException(Throwable cause) {
		super(cause);
	}
}
