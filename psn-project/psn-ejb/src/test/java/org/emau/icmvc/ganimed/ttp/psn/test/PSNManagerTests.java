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
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import org.apache.log4j.Logger;
import org.emau.icmvc.ganimed.ttp.psn.DomainManager;
import org.emau.icmvc.ganimed.ttp.psn.PSNManager;
import org.emau.icmvc.ganimed.ttp.psn.alphabets.Symbol31;
import org.emau.icmvc.ganimed.ttp.psn.dto.DomainDTO;
import org.emau.icmvc.ganimed.ttp.psn.dto.HashMapWrapper;
import org.emau.icmvc.ganimed.ttp.psn.dto.PSNDTO;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.DBException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.DeletionForbiddenException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.DomainInUseException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.InvalidPSNException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.UnknownDomainException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.UnknownValueException;
import org.emau.icmvc.ganimed.ttp.psn.generator.GeneratorProperties;
import org.emau.icmvc.ganimed.ttp.psn.generator.ReedSolomonLagrange;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class PSNManagerTests {

	private static final String DOMAIN = "test12345_psn_test";
	private static final String DOMAIN_FOR_DELETE = "test12345_psn_test_delete";
	private static final String PSN_URL = "http://localhost:8080/gpas/gpasService?wsdl";
	private PSNManager psnManager;
	private static final String DOMAIN_MANAGER_URL = "http://localhost:8080/gpas/DomainService?wsdl";
	private DomainManager domainManager;
	private String propertiesString = GeneratorProperties.PSN_LENGTH + " = 8; " + GeneratorProperties.MAX_DETECTED_ERRORS + " = 2;";
	private static final Logger logger = Logger.getLogger(PSNManagerTests.class);

	// eventuelle alte eintraege nach moeglichkeit aus der tabelle 'psn' loeschen (alle mit der obigen domain)
	// die domain selber kann ebenfalls geloescht werden

	@Before
	public void setup() throws Exception {
		QName serviceName = new QName("http://psn.ttp.ganimed.icmvc.emau.org/", "PSNManagerBeanService");
		URL wsdlURL = new URL(PSN_URL);
		Service service = Service.create(wsdlURL, serviceName);
		Assert.assertNotNull("webservice object for PSNManager is null", service);
		psnManager = (PSNManager) service.getPort(PSNManager.class);
		Assert.assertNotNull("psn manager object is null", psnManager);

		serviceName = new QName("http://psn.ttp.ganimed.icmvc.emau.org/", "DomainManagerBeanService");
		wsdlURL = new URL(DOMAIN_MANAGER_URL);
		service = Service.create(wsdlURL, serviceName);
		Assert.assertNotNull("webservice object for DomainManager is null", service);
		domainManager = (DomainManager) service.getPort(DomainManager.class);
		Assert.assertNotNull("domain manager object is null", domainManager);

		try {
			domainManager.getDomainObject(DOMAIN);
		} catch (UnknownDomainException maybe) {
			try {
				DomainDTO domainDTO = new DomainDTO(DOMAIN, ReedSolomonLagrange.class.getName(), Symbol31.class.getName(), propertiesString,
						"test-kommentar", 0, null);
				domainManager.addDomain(domainDTO);
			} catch (DomainInUseException ignore) {
				// geht nicht
			}
		}
		try {
			domainManager.getDomainObject(DOMAIN_FOR_DELETE);
		} catch (UnknownDomainException maybe) {
			try {
				DomainDTO domainDTO = new DomainDTO(DOMAIN_FOR_DELETE, ReedSolomonLagrange.class.getName(), Symbol31.class.getName(),
						propertiesString + GeneratorProperties.PSNS_DELETABLE + " = true;", "test-kommentar", 0, null);
				domainManager.addDomain(domainDTO);
			} catch (DomainInUseException ignore) {
				// geht nicht
			}
		}
	}

	@Test
	public void basicTest() throws Exception {
		logger.info("### basic test start");
		String origValue = "test123";
		String pseudonym = psnManager.getOrCreatePseudonymFor(origValue, DOMAIN);
		logger.info("generated pseudonym '" + pseudonym + "' for value " + origValue);
		String dataBaseValue = psnManager.getValueFor(pseudonym, DOMAIN);
		logger.info("queried value '" + dataBaseValue + "' for pseudonym '" + pseudonym + "'");
		Assert.assertEquals("retrieved value differs from the original", origValue, dataBaseValue);
		String pseudonym1 = psnManager.getOrCreatePseudonymFor(origValue, DOMAIN);
		logger.info("queried pseudonym '" + pseudonym1 + "' for value " + origValue);
		Assert.assertEquals("generated pseudonym differs from the retrieved one", pseudonym, pseudonym1);
		psnManager.anonymiseEntry(origValue, DOMAIN);
		try {
			psnManager.getValueFor(pseudonym, DOMAIN);
			Assert.fail("pseudonym not anonymised");
		} catch (Exception e) {
			// diese exception muss auftreten
			logger.info("pseudonym anonymised - expected exception was thrown: " + e);
		}
		int sizeBefore = domainManager.listPseudonymsFor(DOMAIN).size();
		String pseudonym2 = psnManager.getOrCreatePseudonymFor(origValue, DOMAIN);
		logger.info("generated new pseudonym '" + pseudonym2 + "' for value " + origValue);
		Assert.assertNotEquals("value not anonymised", pseudonym, pseudonym2);
		int sizeAfter = domainManager.listPseudonymsFor(DOMAIN).size();
		Assert.assertEquals("generated pseudonym is not added to parent-domain", sizeBefore + 1, sizeAfter);
		logger.info("### basic test end");
	}

	@Test
	public void testAdd() throws Exception {
		logger.info("### test adding pseudonyms start");
		long time = System.currentTimeMillis();
		long count = getCountForDomain(DOMAIN);
		logger.info("pseudonyms so far: " + count);
		logger.info("try to add a new pseudonym two times");
		String value = "add_" + time;
		psnManager.getOrCreatePseudonymFor(value, DOMAIN);
		Assert.assertEquals("pseudonym generated, but not added to domain.list", count + 1, getCountForDomain(DOMAIN));
		psnManager.getOrCreatePseudonymFor(value, DOMAIN);
		Assert.assertEquals("an existing pseudonym is added to domain.list by mistake", count + 1, getCountForDomain(DOMAIN));
		count = getCountForDomain(DOMAIN);
		logger.info("pseudonyms after adding: " + count);
		int n = 5;
		logger.info("add " + n + " pseudonyms");
		for (int i = 1; i <= n; i++) {
			Thread.sleep(1);
			psnManager.getOrCreatePseudonymFor("add_" + System.currentTimeMillis(), DOMAIN);
			Assert.assertEquals("pseudonym generated, but not added to domain.list", count + i, getCountForDomain(DOMAIN));
		}
		logger.info("time for adding: " + (System.currentTimeMillis() - time) / 1000 + " seconds");
		logger.info("pseudonyms after adding: " + getCountForDomain(DOMAIN));
		Assert.assertEquals("wrong number of pseudonyms assigned to domain (expected versus list)", count + n,
				domainManager.listPseudonymsFor(DOMAIN).size());
		Assert.assertEquals("wrong number of pseudonyms assigned to domain (relation versus list)", getCountForDomain(DOMAIN),
				domainManager.listPseudonymsFor(DOMAIN).size());
		logger.info("anonymise one entry");
		psnManager.anonymiseEntry(value, DOMAIN);
		logger.info("pseudonyms after anonymising: " + getCountForDomain(DOMAIN));
		// auch "count + n", da ja das pseudonym hinzugefuegt, der originaleintrag aber dafuer entfernt wurde
		Assert.assertEquals("pseudonym anonymised, but not removed from domain.list", count + n, domainManager.listPseudonymsFor(DOMAIN).size());
		Assert.assertEquals("wrong number of pseudonyms assigned to domain (relation versus list)", getCountForDomain(DOMAIN),
				domainManager.listPseudonymsFor(DOMAIN).size());
		logger.info("re-add anonymised entry");
		psnManager.getOrCreatePseudonymFor(value, DOMAIN);
		logger.info("add via map - duplicate value");
		HashMap<String, String> map = new HashMap<String, String>();
		logger.info("add via map - invalid pseudonym");
		map.clear();
		map.put(value + time, "%");
		try {
			psnManager.insertValuePseudonymPairs(new HashMapWrapper<String, String>(map), DOMAIN);
			Assert.fail("could store a invalid pseudonym via map");
		} catch (InvalidPSNException expected) {
			logger.info("expected InvalidPSNException was thrown: " + expected.getMessage());
		}
		logger.info("add 3 value-pseudonym pairs via map");
		map.clear();
		time = System.currentTimeMillis() % 100000000; // 8 stellen
		if (time < 10000000) {
			time += 10000000;
		}
		Map<GeneratorProperties, String> properties = new HashMap<GeneratorProperties, String>();
		properties.put(GeneratorProperties.MAX_DETECTED_ERRORS, "2");
		properties.put(GeneratorProperties.PSN_LENGTH, "8");
		ReedSolomonLagrange generator = new ReedSolomonLagrange(new Symbol31(), properties);
		String pseudo1 = time + generator.generateCheckDigits("" + time);
		map.put(value + time, pseudo1);
		map.put(value + time + 1, (time + 1) + generator.generateCheckDigits("" + (time + 1)));
		map.put(value + time + 2, (time + 2) + generator.generateCheckDigits("" + (time + 2)));
		logger.warn("testmethod is not safe - it's unlikely, but a duplicate pseudonym could occure");
		psnManager.insertValuePseudonymPairs(new HashMapWrapper<String, String>(map), DOMAIN);
		logger.info("try to add 1 already existing value-pseudonym pair via map");
		map.clear();
		map.put(value + time, pseudo1);
		psnManager.insertValuePseudonymPairs(new HashMapWrapper<String, String>(map), DOMAIN);
		logger.info("try to add 1 value-pseudonym pair via map with an already existing pseudonym");
		map.clear();
		map.put(value + time + "x", pseudo1);
		try {
			psnManager.insertValuePseudonymPairs(new HashMapWrapper<String, String>(map), DOMAIN);
			Assert.fail("could add 1 value-pseudonym pair via map with an already existing pseudonym");
		} catch (DBException expected) {
			logger.info("expected DBException was thrown: " + expected.getMessage());
		}
		logger.info("try to add a value-pseudonym pair via map for an already existing value");
		map.clear();
		map.put(value + time, (time + 3) + generator.generateCheckDigits("" + (time + 3)));
		try {
			psnManager.insertValuePseudonymPairs(new HashMapWrapper<String, String>(map), DOMAIN);
			Assert.fail("could add a value-pseudonym pair via map for an already existing value");
		} catch (DBException expected) {
			logger.info("expected DBException was thrown: " + expected.getMessage());
		}
		logger.info("pseudonyms after adding: " + getCountForDomain(DOMAIN));
		// + 1 re-added + 3 via map
		Assert.assertEquals("wrong number of pseudonyms assigned to domain (expected versus list)", count + n + 1 + 3,
				domainManager.listPseudonymsFor(DOMAIN).size());
		Assert.assertEquals("wrong number of pseudonyms assigned to domain (relation versus list)", getCountForDomain(DOMAIN),
				domainManager.listPseudonymsFor(DOMAIN).size());
		logger.info("### test adding pseudonyms end");
	}

	private long getCountForDomain(String domainID) throws Exception {
		return domainManager.getDomainObject(domainID).getNumberOfPseudonyms();
	}

	@Test
	public void listPSN() throws Exception {
		List<PSNDTO> psnList = domainManager.listPseudonymsFor(DOMAIN);
		logger.info("list the first 10 of " + psnList.size() + " psn for domain '" + DOMAIN + "'");
		int count = 0;
		for (PSNDTO dto : psnList) {
			logger.info(count++ + " : " + dto);
			if (count == 10) {
				break;
			}
		}
	}

	@Test
	public void listMethods() throws Exception {
		logger.info("### add pseudonyms with getPseudonymForList start");
		HashSet<String> values = new HashSet<String>(Arrays.asList("entry_1", "entry_2", "entry_3", "entry_2"));
		HashMapWrapper<String, String> pseudonymMap = psnManager.getOrCreatePseudonymForList(values, DOMAIN);
		HashSet<String> pseudonyms = new HashSet<String>();
		for (Entry<String, String> entry : pseudonymMap.getMap().entrySet()) {
			logger.info("pseudonym for value '" + entry.getKey() + "' is '" + entry.getValue() + "'");
			pseudonyms.add(entry.getValue());
		}
		logger.info("### add pseudonyms with getPseudonymForList end");
		logger.info("### list values for pseudonyms start");
		HashMapWrapper<String, String> pseudonymMap2 = psnManager.getValueForList(pseudonyms, DOMAIN);
		for (Entry<String, String> entry : pseudonymMap2.getMap().entrySet()) {
			logger.info("value for pseudonym '" + entry.getKey() + "' is '" + entry.getValue() + "'");
		}
		logger.info("### list values for pseudonyms end");
		logger.info("### list psn for valuesWithPrefix start");
		String wrong1 = "abc";
		String wrong2 = "xabcd";
		String right1 = "abcd";
		String right2 = "abcde";
		values = new HashSet<String>(Arrays.asList(wrong1, right1, right2, wrong2));
		psnManager.getOrCreatePseudonymForList(values, DOMAIN);
		pseudonymMap2 = psnManager.getPseudonymForValuePrefix("abcd", DOMAIN);
		Assert.assertFalse("wrong psn returned by getPseudonymForValuePrefix",
				pseudonymMap2.getMap().containsKey(wrong1) || pseudonymMap2.getMap().containsKey(wrong2));
		Assert.assertTrue("correct psn not returned by getPseudonymForValuePrefix",
				pseudonymMap2.getMap().containsKey(right1) && pseudonymMap2.getMap().containsKey(right2));
		logger.info("### list psn for valuesWithPrefix end");
	}

	@Test
	public void testDelete() throws Exception {
		logger.info("### delete tests start");
		logger.info("### try to delete an entry within a domain where this is forbidden");
		List<PSNDTO> psnList = domainManager.listPseudonymsFor(DOMAIN);
		if (!psnList.isEmpty()) {
			try {
				psnManager.deleteEntry(psnList.get(0).getOriginalValue(), DOMAIN);
				Assert.fail("could delete a value-pseudonym pair within a domain where this is forbidden");
			} catch (DeletionForbiddenException expected) {
				logger.info("expected DeletionForbiddenException was thrown: " + expected.getMessage());
			}
		}
		logger.info("### try to delete an entry within a domain where this is allowed");
		logger.info("### add entry");
		long time = System.currentTimeMillis();
		String value = "add_" + time;
		psnManager.getOrCreatePseudonymFor(value, DOMAIN_FOR_DELETE);
		logger.info("### try to delete a non-existing entry");
		try {
			psnManager.deleteEntry("non-existing-dummy-value", DOMAIN_FOR_DELETE);
			Assert.fail("could delete a non-exisiting value-pseudonym pair");
		} catch (UnknownValueException expected) {
			logger.info("expected UnknownValueException was thrown: " + expected.getMessage());
		}
		logger.info("### try to delete an entry within a non-existing domain");
		try {
			psnManager.deleteEntry(value, "non-existing-dummy-domain");
			Assert.fail("could delete an entry within a non-existing domain");
		} catch (UnknownDomainException expected) {
			logger.info("expected UnknownDomainException was thrown: " + expected.getMessage());
		}
		logger.info("### try to delete an entry");
		psnManager.deleteEntry(value, DOMAIN_FOR_DELETE);
		Assert.assertTrue("entry was not deleted", domainManager.listPseudonymsFor(DOMAIN_FOR_DELETE).isEmpty());
		logger.info("### delete tests end");
	}
}
