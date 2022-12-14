package org.emau.icmvc.ganimed.ttp.psn.exceptions;

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

/**
 * Thrown if one or more of the given parent domains is invalid, e.g. duplicated
 */
public class InvalidParentDomainException extends Exception
{
	private static final long serialVersionUID = 1226470799393233381L;
	private final String parentDomainName;

	public InvalidParentDomainException()
	{
		super();
		parentDomainName = null;
	}

	public InvalidParentDomainException(String message, Throwable cause)
	{
		super(message, cause);
		parentDomainName = null;
	}

	public InvalidParentDomainException(Throwable cause)
	{
		super(cause);
		parentDomainName = null;
	}

	public InvalidParentDomainException(String message)
	{
		super(message);
		parentDomainName = null;
	}

	public InvalidParentDomainException(String parentDomainName, String message)
	{
		super(message);
		this.parentDomainName = parentDomainName;
	}

	public String getParentDomainName()
	{
		return parentDomainName;
	}
}
