package org.emau.icmvc.ganimed.ttp.psn;
/*-
 * ###license-information-start###
 * gPAS - a Generic Pseudonym Administration Service
 * __
 * Copyright (C) 2013 - 2024 Independent Trusted Third Party of the University Medicine Greifswald
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

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import jakarta.ejb.EJB;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.emau.icmvc.ganimed.ttp.psn.config.PSNField;
import org.emau.icmvc.ganimed.ttp.psn.config.PaginationConfig;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.InvalidParameterException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.UnknownDomainException;
import org.emau.icmvc.ganimed.ttp.psn.internal.AnonymDomain;
import org.emau.icmvc.ganimed.ttp.psn.internal.Cache;

public abstract class GPASServiceBase
{
	public static final String THIS_CONTEXT_DOES_NOT_SUPPORT_MULTI_PSN_DOMAINS = "This context does not support multi-PSN domains";
	public static final String THIS_CONTEXT_DOES_NOT_SUPPORT_MULTIPLE_PSNS = "This context does not support multiple PSNs";
	private static final String PARAMETER_MISSING_MESSAGE = "invalid parameter: ";
	protected final Logger LOGGER = LogManager.getLogger(getClass());
	@EJB
	protected Cache cache;

	protected void checkParameter(Object parameter, String paramName) throws InvalidParameterException
	{
		switch (parameter)
		{
			case null -> throwIPE(paramName);
			case String s ->
			{
				if (s.isEmpty())
				{
					throwIPE(paramName);
				}
			}
			case List<?> list ->
			{
				if (list.isEmpty())
				{
					throwIPE(paramName);
				}
				for (Object element : list)
				{
					checkParameter(element, paramName);
				}
			}
			case Map<?,?> map ->
			{
				if (map.isEmpty())
				{
					throwIPE(paramName);
				}
			}
			case PaginationConfig pc ->
			{
				for (Entry<PSNField, String> entry : pc.getFilter().entrySet())
				{
					if (entry.getKey() == null)
					{
						throwIPE(paramName, "a key of a filter entry within the given pagination config is null");
					}
				}
			}
			default ->
			{
			}
		}
	}

	protected void checkThatNotAnonymDomain(String domainName) throws InvalidParameterException
	{
		if (AnonymDomain.NAME.equals(domainName))
		{
			String message = "it's not possible to anonymise values for the intern domain " + AnonymDomain.NAME;
			LOGGER.error(message);
			throw new InvalidParameterException(message);
		}
	}

	protected void checkSinglePsnDomain(String domainName) throws UnknownDomainException, InvalidParameterException
	{
		checkSinglePsnDomain(domainName, THIS_CONTEXT_DOES_NOT_SUPPORT_MULTI_PSN_DOMAINS);
	}

	protected void checkSinglePsnDomain(String domainName, String msg) throws UnknownDomainException, InvalidParameterException
	{
		if (cache.isMultiPsnDomain(domainName))
		{
			throw new InvalidParameterException(msg +  " (domain: " + domainName + "'");
		}
	}

	private void throwIPE(String paramName) throws InvalidParameterException
	{
		String message = PARAMETER_MISSING_MESSAGE + paramName;
		LOGGER.warn(message);
		throw new InvalidParameterException(paramName, message);
	}

	private void throwIPE(String paramName, String message) throws InvalidParameterException
	{
		LOGGER.warn(message);
		throw new InvalidParameterException(paramName, message);
	}

	public static void throwIllegalArgumentExceptionForMultiplePsnContext(String value)
	{
		String msg = THIS_CONTEXT_DOES_NOT_SUPPORT_MULTIPLE_PSNS;

		if (StringUtils.isNotBlank(value))
		{
			msg = msg + " (value: " + value + ")";
		}
		throw new IllegalArgumentException(msg);
	}
}
