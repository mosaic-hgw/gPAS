package org.emau.icmvc.ganimed.ttp.psn;
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

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.ejb.EJB;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.emau.icmvc.ganimed.ttp.psn.config.PSNField;
import org.emau.icmvc.ganimed.ttp.psn.config.PaginationConfig;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.InvalidParameterException;
import org.emau.icmvc.ganimed.ttp.psn.internal.Cache;

public abstract class GPASServiceBase
{
	private final Logger logger = LogManager.getLogger(GPASServiceBase.class);
	private static final String PARAMETER_MISSING_MESSAGE = "invalid parameter: ";
	@EJB
	protected Cache cache;

	protected void checkParameter(Object parameter, String paramName) throws InvalidParameterException
	{
		if (parameter == null)
		{
			throwIPE(paramName);
		}
		else if (parameter instanceof String)
		{
			if (((String) parameter).isEmpty())
			{
				throwIPE(paramName);
			}
		}
		else if (parameter instanceof List)
		{
			if (((List<?>) parameter).isEmpty())
			{
				throwIPE(paramName);
			}
			for (Object element : (List<?>) parameter)
			{
				checkParameter(element, paramName);
			}
		}
		else if (parameter instanceof Map)
		{
			if (((Map<?, ?>) parameter).isEmpty())
			{
				throwIPE(paramName);
			}
		}
		else if (parameter instanceof PaginationConfig)
		{
			PaginationConfig pc = (PaginationConfig) parameter;
			for (Entry<PSNField, String> entry : pc.getFilter().entrySet())
			{
				if (entry.getKey() == null)
				{
					throwIPE(paramName, "a key of a filter entry within the given pagination config is null");
				}
			}
		}
	}

	private void throwIPE(String paramName) throws InvalidParameterException
	{
		String message = PARAMETER_MISSING_MESSAGE + paramName;
		logger.warn(message);
		throw new InvalidParameterException(paramName, message);
	}

	private void throwIPE(String paramName, String message) throws InvalidParameterException
	{
		logger.warn(message);
		throw new InvalidParameterException(paramName, message);
	}
}
