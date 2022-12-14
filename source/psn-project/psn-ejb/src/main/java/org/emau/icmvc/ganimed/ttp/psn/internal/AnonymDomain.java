package org.emau.icmvc.ganimed.ttp.psn.internal;

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

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.Startup;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.emau.icmvc.ganimed.ttp.psn.alphabets.Symbol31;
import org.emau.icmvc.ganimed.ttp.psn.config.DomainConfig;
import org.emau.icmvc.ganimed.ttp.psn.dto.DomainInDTO;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.InvalidParameterException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.UnknownDomainException;
import org.emau.icmvc.ganimed.ttp.psn.generator.ReedSolomonLagrange;

@Singleton
@Startup
public class AnonymDomain
{
	private static final Logger logger = LogManager.getLogger(AnonymDomain.class);
	public final static String NAME = "internal_anonymisation_domain";
	public final static String PREFIX = "###_anonym_###_";
	public final static String SUFFIX = "_###_anonym_###";
	public final static String DELIMITER = "_#*#_";
	@EJB
	private Cache cache;

	@PostConstruct
	public void setup()
	{
		if (logger.isDebugEnabled())
		{
			logger.debug("initialising anonymisation domain");
		}
		DomainConfig config = new DomainConfig();
		try
		{
			config.setPsnLength(10);
			config.setPsnPrefix(PREFIX);
			config.setPsnSuffix(SUFFIX);
		}
		catch (InvalidParameterException e)
		{
			logger.error("impossible exception", e);
		}
		try
		{
			cache.getDomainDTO(NAME);
		}
		catch (UnknownDomainException maybe)
		{
			try
			{
				DomainInDTO anoDomain = new DomainInDTO(NAME, NAME, ReedSolomonLagrange.class.getName(), Symbol31.class.getName(), config, "internal domain used to create unique anonyms", null);
				cache.addDomain(anoDomain);
				if (logger.isDebugEnabled())
				{
					logger.debug("anonymisation domain created");
				}
			}
			catch (Exception e)
			{
				logger.error("exception while creating special anonymisation domain", e);
			}
		}
	}
}
