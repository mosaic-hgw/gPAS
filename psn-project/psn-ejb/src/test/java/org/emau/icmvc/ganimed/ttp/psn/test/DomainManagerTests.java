package org.emau.icmvc.ganimed.ttp.psn.test;

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

import java.net.URL;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import org.apache.log4j.Logger;
import org.emau.icmvc.ganimed.ttp.psn.DomainManager;
import org.emau.icmvc.ganimed.ttp.psn.alphabets.Numbers;
import org.emau.icmvc.ganimed.ttp.psn.alphabets.Symbol31;
import org.emau.icmvc.ganimed.ttp.psn.dto.DomainDTO;
import org.emau.icmvc.ganimed.ttp.psn.dto.DomainLightDTO;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.DomainInUseException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.InvalidAlphabetException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.InvalidCheckDigitClassException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.InvalidGeneratorException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.UnknownDomainException;
import org.emau.icmvc.ganimed.ttp.psn.generator.GeneratorProperties;
import org.emau.icmvc.ganimed.ttp.psn.generator.ReedSolomonLagrange;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class DomainManagerTests {

	private static final String DOMAIN = "test12345_domain_test";
	private static final String DOMAIN_MANAGER_URL = "http://localhost:8080/gpas/DomainService?wsdl";
	private DomainManager domainManager;
	private String propertiesString = GeneratorProperties.PSN_LENGTH + " = 8; " + GeneratorProperties.MAX_DETECTED_ERRORS + " = 2";
	private static final Logger logger = Logger.getLogger(DomainManagerTests.class);

	@Before
	public void setup() throws Exception {
		QName serviceName = new QName("http://psn.ttp.ganimed.icmvc.emau.org/", "DomainManagerBeanService");
		URL wsdlURL = new URL(DOMAIN_MANAGER_URL);
		Service service = Service.create(wsdlURL, serviceName);
		Assert.assertNotNull("webservice object for DomainManager is null", service);
		domainManager = (DomainManager) service.getPort(DomainManager.class);
		Assert.assertNotNull("domain manager object is null", domainManager);
	}

	@Test
	public void testValidDomain() throws Exception {
		logger.info("### test a valid domain start");
		logger.info("add domain");
		DomainDTO domainDTO = new DomainDTO(DOMAIN, ReedSolomonLagrange.class.getName(), Symbol31.class.getName(), propertiesString, "test-kommentar",
				0, null);
		domainManager.addDomain(domainDTO);
		try {
			// dupletten-check
			logger.info("try to add a duplicate domain");
			domainManager.addDomain(domainDTO);
			Assert.fail("duplicate domain persisted");
		} catch (DomainInUseException expected) {
			logger.info("duplicate domain not persisted - expected DomainInUseException was thrown: " + expected.getMessage());
		}
		// loeschen und neu anlegen
		logger.info("delete domain");
		domainManager.deleteDomain(DOMAIN);
		try {
			// dupletten-check
			logger.info("try to delete a deleted domain");
			domainManager.deleteDomain(DOMAIN);
			Assert.fail("not existing domain deleted");
		} catch (UnknownDomainException expected) {
			logger.info("not existing domain not deleted - expected UnknownDomainException was thrown: " + expected.getMessage());
		}
		logger.info("re-add domain with custom alphabet ...");
		// test mit generischem alphabet - achtung! anzahl buchstaben muss primzahl sein
		domainDTO = new DomainDTO(DOMAIN, ReedSolomonLagrange.class.getName(), "a,b,c,d,e", propertiesString, "test-kommentar", 0, null);
		domainManager.addDomain(domainDTO);
		logger.info("add a child domain");
		domainDTO = new DomainDTO(DOMAIN + '1', ReedSolomonLagrange.class.getName(), "a,b,c,d,e", propertiesString, "test-kommentar", 0, DOMAIN);
		domainManager.addDomain(domainDTO);
		try {
			// dupletten-check
			logger.info("try to delete the parent domain");
			domainManager.deleteDomain(DOMAIN);
			Assert.fail("parent domain deleted");
		} catch (DomainInUseException expected) {
			logger.info("parent domain not deleted - expected DomainInUseException was thrown: " + expected.getMessage());
		}
		logger.info("... and the final delete");
		domainManager.deleteDomain(DOMAIN + '1');
		domainManager.deleteDomain(DOMAIN);
		logger.info("### test a valid domain end");
	}

	@Test
	public void testInvalidDomain() throws Exception {
		logger.info("### test some invalid domains start");
		try {
			// falsche pruefziffernklasse
			logger.info("trying to add a domain with wrong check digit class");
			domainManager.addDomain(
					new DomainDTO(DOMAIN, "CheckDigitClassDontExist", Symbol31.class.getName(), propertiesString, "test-kommentar", 0, null));
			Assert.fail("domain with wrong check digit class persisted");
		} catch (InvalidCheckDigitClassException e) {
			// diese exception muss auftreten
			logger.info("domain with wrong check digit class not persisted - expected InvalidCheckDigitClassException was thrown: " + e.getMessage());
		}
		try {
			// falsches alphabet
			logger.info("trying to add a domain with wrong alphabet");
			domainManager.addDomain(
					new DomainDTO(DOMAIN, ReedSolomonLagrange.class.getName(), "AlphabetDontExist", propertiesString, "test-kommentar", 0, null));
			Assert.fail("domain with wrong alphabet persisted");
		} catch (InvalidAlphabetException e) {
			// diese exception muss auftreten
			logger.info("domain with wrong alphabet not persisted - expected InvalidAlphabetException was thrown: " + e.getMessage());
		}
		try {
			// alphabet passt nicht zur pruefziffernklasse
			logger.info("trying to add a domain with an alphabet which doesn't fit to the check digit class");
			domainManager.addDomain(
					new DomainDTO(DOMAIN, ReedSolomonLagrange.class.getName(), Numbers.class.getName(), propertiesString, "test-kommentar", 0, null));
			Assert.fail("domain with an alphabet which doesn't fit to the check digit class persisted");
		} catch (InvalidGeneratorException e) {
			// diese exception muss auftreten
			logger.info(
					"domain with an alphabet which doesn't fit to the check digit class not persisted - expected InvalidGeneratorException was thrown: "
							+ e.getMessage());
		}
		try {
			// alphabet passt nicht zur pruefziffernklasse
			logger.info("trying to add a domain with an unknown parent domain");
			domainManager.addDomain(new DomainDTO(DOMAIN, ReedSolomonLagrange.class.getName(), Symbol31.class.getName(), propertiesString,
					"test-kommentar", 0, "abcdef_gibt_es_nicht"));
			Assert.fail("domain with an unknown parent domain persisted");
		} catch (UnknownDomainException e) {
			// diese exception muss auftreten
			logger.info("domain with an unknown parent domain not persisted - expected UnknownDomainException was thrown: " + e.getMessage());
		}
		logger.info("### test some invalid domains end");
	}

	@Test
	public void testLists() throws Exception {
		logger.info("### test list functionality start");
		logger.info("domains:");
		List<DomainDTO> domains = domainManager.listDomains();
		for (DomainDTO domain : domains) {
			logger.info(domain);
		}
		List<DomainLightDTO> domainsLight = domainManager.listDomainsLight();
		Assert.assertTrue("result of listDomains differs from result of listDomainsLight - different size", domains.size() == domainsLight.size());
		for (DomainDTO domain : domains) {
			DomainLightDTO domainLight = new DomainLightDTO(domain.getDomain(), domain.getCheckDigitClass(), domain.getAlphabet(),
					domain.getProperties(), domain.getComment(), domain.getParentDomain());
			if (!domainsLight.contains(domainLight)) {
				Assert.fail("result of listDomains differs from result of listDomainsLight - '" + domainLight.getDomain()
						+ "' is missing withhin listDomainsLight");
			}
		}
		logger.info("possible properties:");
		logger.info(domainManager.listPossibleProperties());
		logger.info("test getDomainsForPrefix");
		final String PREFIX = "test_123";
		DomainDTO domainDTO = new DomainDTO(DOMAIN, ReedSolomonLagrange.class.getName(), Symbol31.class.getName(),
				propertiesString + ";" + GeneratorProperties.PSN_PREFIX + "=" + PREFIX, "test-kommentar", 0, null);
		List<DomainLightDTO> list = domainManager.getDomainsForPrefix(PREFIX);
		int size = list.size();
		domainManager.addDomain(domainDTO);
		list = domainManager.getDomainsForPrefix(PREFIX);
		Assert.assertTrue("", list.size() == size + 1);
		domainManager.deleteDomain(DOMAIN);
		logger.info("### test list functionality end");
	}
}
