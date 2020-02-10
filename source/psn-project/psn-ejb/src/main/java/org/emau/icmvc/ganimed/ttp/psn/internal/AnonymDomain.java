package org.emau.icmvc.ganimed.ttp.psn.internal;

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

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.log4j.Logger;
import org.emau.icmvc.ganimed.ttp.psn.DomainManager;
import org.emau.icmvc.ganimed.ttp.psn.alphabets.Symbol31;
import org.emau.icmvc.ganimed.ttp.psn.dto.DomainDTO;
import org.emau.icmvc.ganimed.ttp.psn.generator.GeneratorProperties;
import org.emau.icmvc.ganimed.ttp.psn.generator.ReedSolomonLagrange;
import org.emau.icmvc.ganimed.ttp.psn.model.PSNProject;

/**
 * special domain for anonyms
 * <p>
 * is used to generate unique anonyms to replace the original values. uniqueness is required because "value" is a key-field
 * 
 * <pre>
 * example - before anonymisation
 * --------------------------------
 * | domain | value   | pseudonym |
 * --------------------------------
 * | xxx    | 123     | abc       |
 * --------------------------------
 * 
 * example - after anonymisation
 * --------------------------------
 * | domain | value   | pseudonym |
 * --------------------------------
 * | ano    | xxx_abc | _uvw_     |
 * | xxx    | _uvw_   | abc       |
 * --------------------------------
 * </pre>
 * 
 * @author geidell
 * 
 */
@Singleton
@Startup
@PersistenceContext(name = "psn")
public class AnonymDomain {

	private static final Logger logger = Logger.getLogger(AnonymDomain.class);
	public final static String NAME = "internal_anonymisation_domain";
	public final static String PREFIX = "###_anonym_###_";
	public final static String SUFFIX = "_###_anonym_###";
	public final static String DELIMITER = "_#*#_";
	@EJB
	private DomainManager domainManager;
	@PersistenceContext
	private EntityManager em;

	@PostConstruct
	public void setup() {
		if (logger.isDebugEnabled()) {
			logger.debug("initialising anonymisation domain");
		}
		String propertiesString = GeneratorProperties.PSN_LENGTH + " = 10; " + GeneratorProperties.MAX_DETECTED_ERRORS + " = 2; "
				+ GeneratorProperties.PSN_PREFIX + " = " + PREFIX + "; " + GeneratorProperties.PSN_SUFFIX + " = " + SUFFIX;
		if (em.find(PSNProject.class, NAME) == null) {
			try {
				DomainDTO anoDomain = new DomainDTO(NAME, ReedSolomonLagrange.class.getName(), Symbol31.class.getName(), propertiesString,
						"internal domain used to create unique anonyms", 0, null);
				domainManager.addDomain(anoDomain);
				if (logger.isDebugEnabled()) {
					logger.debug("anonymisation domain created");
				}
			} catch (Exception e) {
				logger.error("exception while creating special anonymisation domain", e);
			}
		}
	}
}
