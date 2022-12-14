package org.emau.icmvc.ganimed.ttp.psn.config;

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

import org.emau.icmvc.ganimed.ttp.psn.enums.ForceCache;
import org.emau.icmvc.ganimed.ttp.psn.enums.ValidateViaParents;

public enum DomainProperties
{
	/**
	 * int<br>
	 * numbers of check digits for {@link org.emau.icmvc.ganimed.ttp.psn.generator.ReedSolomonLagrange}<br>
	 * default 2
	 */
	MAX_DETECTED_ERRORS,
	/**
	 * int<br>
	 * length of the generated pseudonym<br>
	 * default 8
	 */
	PSN_LENGTH,
	/**
	 * string<br>
	 * additional prefix<br>
	 * default ""
	 */
	PSN_PREFIX,
	/**
	 * string<br>
	 * additional suffix<br>
	 * default ""
	 */
	PSN_SUFFIX,
	/**
	 * boolean<br>
	 * should the prefix be used to calculate the check digit(s)<br>
	 * default false
	 */
	INCLUDE_PREFIX_IN_CHECK_DIGIT_CALCULATION,
	/**
	 * boolean<br>
	 * should the suffix be used to calculate the check digit(s)<br>
	 * default false
	 */
	INCLUDE_SUFFIX_IN_CHECK_DIGIT_CALCULATION,
	/**
	 * int<br>
	 * use last char of the given alphabet as delimiter symbol after the given number of other chars within the pseudonym<br>
	 * e.g. 123.456.789 or abcd-efgh-ijkl<br>
	 * default 0 (which means dont use ...)
	 */
	USE_LAST_CHAR_AS_DELIMITER_AFTER_X_CHARS,
	/**
	 * boolean<br>
	 * is it allowed to delete entries within this domain<br>
	 * attention! {@link ValidateViaParents#CASCADE_DELETE} ignores this config entry<br>
	 * default false
	 */
	PSNS_DELETABLE,
	/**
	 * enum<br>
	 * should a cache be used for faster psn generation, see {@link ForceCache}<br>
	 * memory consumption is one bit per possible pseudonym: mem_for_cache = alphabet_length ^ pseudonym_length / 8 / 1024 / 1024 MB<br>
	 * e.g. alphabet = numbers, length = 8 -> mem_for_cache = 10 ^ 8 / (8 * 1024 * 1024) = 11.92 MB<br>
	 * default {@link ForceCache#DEFAULT} if memory consumption < 120 MB (numbers with length = 9) then true, else false
	 */
	FORCE_CACHE,
	/**
	 * enum<br>
	 * should the values in this domain be validated against the rules of their parent domains, see {@link ValidateViaParents}<br>
	 * throws an {@link InvalidParameterException} if there's no parent domain set<br>
	 * default {@link ValidateViaParents#OFF}
	 */
	VALIDATE_VALUES_VIA_PARENTS;
}
