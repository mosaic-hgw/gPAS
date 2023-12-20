package org.emau.icmvc.ganimed.ttp.psn.exceptions;

import org.emau.icmvc.ganimed.ttp.psn.enums.InsertPairError;

/*-
 * ###license-information-start###
 * gPAS - a Generic Pseudonym Administration Service
 * __
 * Copyright (C) 2013 - 2023 Independent Trusted Third Party of the University Medicine Greifswald
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

public class InsertPairException extends Exception
{
	private static final long serialVersionUID = 501148225660499858L;
	private final String value;
	private final String pseudonym;
	private final InsertPairError reason;

	public InsertPairException(String value, String pseudonym, InsertPairError reason)
	{
		super();
		this.value = value;
		this.pseudonym = pseudonym;
		this.reason = reason;
	}

	public InsertPairException(String message, Throwable cause, String value, String pseudonym, InsertPairError reason)
	{
		super(message, cause);
		this.reason = reason;
		this.value = value;
		this.pseudonym = pseudonym;
	}

	public InsertPairException(String message, String value, String pseudonym, InsertPairError reason)
	{
		super(message);
		this.reason = reason;
		this.value = value;
		this.pseudonym = pseudonym;
	}

	public InsertPairException(Throwable cause, String value, String pseudonym, InsertPairError reason)
	{
		super(cause);
		this.reason = reason;
		this.value = value;
		this.pseudonym = pseudonym;
	}

	public String getValue()
	{
		return value;
	}

	public String getPseudonym()
	{
		return pseudonym;
	}

	public InsertPairError getReason()
	{
		return reason;
	}
}
