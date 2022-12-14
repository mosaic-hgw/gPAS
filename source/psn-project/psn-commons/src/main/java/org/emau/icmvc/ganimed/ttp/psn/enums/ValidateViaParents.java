package org.emau.icmvc.ganimed.ttp.psn.enums;

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

import org.emau.icmvc.ganimed.ttp.psn.config.DomainProperties;

/**
 * checks if a given value is valid in one of the parent domains<br>
 * {@link ValidateViaParents#OFF} - no validation
 * {@link ValidateViaParents#VALIDATE} - the given value must be a valid pseudonym of one parent domain
 * {@link ValidateViaParents#ENSURE_EXISTS} - the given value must exist as a pseudonym in one parent domain
 * {@link ValidateViaParents#CASCADE_DELETE} - the given value must exist as a pseudonym in one parent domain and if that's no longer the case, the entry is delete from the child domain (ignoring
 * {@link DomainProperties#PSNS_DELETABLE})
 *
 * @author geidell
 *
 */
public enum ValidateViaParents
{
	OFF, VALIDATE, ENSURE_EXISTS, CASCADE_DELETE;
}
