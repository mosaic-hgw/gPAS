package org.emau.icmvc.ganimed.ttp.psn.test;

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

import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.persistence.jpa.jpql.Assert;
import org.emau.icmvc.ganimed.ttp.psn.DomainManager;
import org.emau.icmvc.ganimed.ttp.psn.PSNManager;
import org.emau.icmvc.ganimed.ttp.psn.alphabets.Numbers;
import org.emau.icmvc.ganimed.ttp.psn.alphabets.Symbol31;
import org.emau.icmvc.ganimed.ttp.psn.config.DomainConfig;
import org.emau.icmvc.ganimed.ttp.psn.config.DomainProperties;
import org.emau.icmvc.ganimed.ttp.psn.config.PaginationConfig;
import org.emau.icmvc.ganimed.ttp.psn.dto.DomainInDTO;
import org.emau.icmvc.ganimed.ttp.psn.dto.DomainOutDTO;
import org.emau.icmvc.ganimed.ttp.psn.dto.PSNDTO;
import org.emau.icmvc.ganimed.ttp.psn.enums.AnonymisationResult;
import org.emau.icmvc.ganimed.ttp.psn.enums.DeletionResult;
import org.emau.icmvc.ganimed.ttp.psn.enums.ForceCache;
import org.emau.icmvc.ganimed.ttp.psn.enums.GeneratorAlphabetRestriction;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.DeletionForbiddenException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.DomainInUseException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.DomainIsFullException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.InvalidParameterException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.PSNErrorStrings;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.UnknownDomainException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.UnknownValueException;
import org.emau.icmvc.ganimed.ttp.psn.generator.ReedSolomonLagrange;
import org.emau.icmvc.ganimed.ttp.psn.generator.Verhoeff;
import org.emau.icmvc.ganimed.ttp.psn.internal.AnonymDomain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

@Tag("Integration")
public class PSNManagerTests
{
	private static final String DOMAIN = "test12345_psn_test";
	private static final String DOMAIN_FOR_DELETE = "test12345_psn_test_delete";
	private static final String DOMAIN_FOR_DELETE_2 = "test12345_psn_test_delete_2";
	private static final String DOMAIN_FOR_GEN_TEST = "test12345_psn_test_gen";
	private static final String PSN_URL = "http://localhost:8080/gpas/gpasService?wsdl";
	private PSNManager psnManager;
	private static final String DOMAIN_MANAGER_URL = "http://localhost:8080/gpas/DomainService?wsdl";
	private DomainManager domainManager;
	private final DomainConfig config = new DomainConfig();
	private static final Logger logger = LogManager.getLogger(PSNManagerTests.class);

	// eventuelle alte eintraege nach moeglichkeit aus der tabelle 'psn' loeschen (alle mit der
	// obigen domain)
	// die domain selber kann ebenfalls geloescht werden
	/*-
	delete from psn where domain = 'test12345_psn_test';
	delete from psn where domain = 'test12345_psn_test_delete';
	delete from psn where domain = 'test12345_psn_test_gen';
	delete from domain where name = 'test12345_psn_test';
	delete from domain where name = 'test12345_psn_test_delete';
	delete from domain where name = 'test12345_psn_test_gen';
	*/

	@BeforeEach
	public void setup() throws Exception
	{
		QName serviceName = new QName("http://psn.ttp.ganimed.icmvc.emau.org/", "PSNManagerBeanService");
		URL wsdlURL = new URL(PSN_URL);
		Service service = Service.create(wsdlURL, serviceName);
		assertNotNull(service, "webservice object for PSNManager is null");
		psnManager = service.getPort(PSNManager.class);
		assertNotNull(psnManager, "psn manager object is null");

		serviceName = new QName("http://psn.ttp.ganimed.icmvc.emau.org/", "DomainManagerBeanService");
		wsdlURL = new URL(DOMAIN_MANAGER_URL);
		service = Service.create(wsdlURL, serviceName);
		assertNotNull(service, "webservice object for DomainManager is null");
		domainManager = service.getPort(DomainManager.class);
		assertNotNull(domainManager, "domain manager object is null");

		try
		{
			domainManager.getDomain(DOMAIN);
		}
		catch (UnknownDomainException maybe)
		{
			try
			{
				DomainInDTO domainDTO = new DomainInDTO(DOMAIN, DOMAIN, ReedSolomonLagrange.class.getName(), Symbol31.class.getName(), config,
						"test-kommentar", null);
				domainManager.addDomain(domainDTO);
			}
			catch (DomainInUseException ignore)
			{
				// darf nicht autreten
				throw ignore;
			}
		}
		try
		{
			domainManager.getDomain(DOMAIN_FOR_DELETE);
		}
		catch (UnknownDomainException maybe)
		{
			try
			{
				DomainConfig delConfig = new DomainConfig();
				delConfig.setPsnsDeletable(true);
				DomainInDTO domainDTO = new DomainInDTO(DOMAIN_FOR_DELETE, DOMAIN_FOR_DELETE, ReedSolomonLagrange.class.getName(), Symbol31.class.getName(), delConfig, "test-kommentar", null);
				domainManager.addDomain(domainDTO);
			}
			catch (DomainInUseException ignore)
			{
				// darf nicht autreten
				throw ignore;
			}
		}
	}

	@Test
	public void basicTest() throws Exception
	{
		logger.info("### basic test start");
		String origValue = "test123";
		String pseudonym = psnManager.getOrCreatePseudonymFor(origValue, DOMAIN);
		logger.info("generated pseudonym '" + pseudonym + "' for value " + origValue);
		String dataBaseValue = psnManager.getValueFor(pseudonym, DOMAIN);
		logger.info("queried value '" + dataBaseValue + "' for pseudonym '" + pseudonym + "'");
		assertEquals(origValue, dataBaseValue, "retrieved value differs from the original");
		String pseudonym1 = psnManager.getOrCreatePseudonymFor(origValue, DOMAIN);
		logger.info("queried pseudonym '" + pseudonym1 + "' for value " + origValue);
		assertEquals(pseudonym, pseudonym1, "generated pseudonym differs from the retrieved one");
		DomainOutDTO domainDTO = domainManager.getDomain(DOMAIN);
		long psnCount = domainDTO.getNumberOfPseudonyms();
		long anoCount = domainDTO.getNumberOfAnonyms();
		domainDTO = domainManager.getDomain(AnonymDomain.NAME);
		long allAnoCount = domainDTO.getNumberOfPseudonyms();
		assertFalse(psnManager.isAnonymised(pseudonym, DOMAIN), "pseudonym is erroneously considered as anonymised");
		psnManager.anonymiseEntry(origValue, DOMAIN);
		assertTrue(psnManager.isAnonymised(pseudonym, DOMAIN), "pseudonym is erroneously considered as not anonymised");
		try
		{
			psnManager.getValueFor(pseudonym, DOMAIN);
			fail("pseudonym not anonymised");
		}
		catch (Exception e)
		{
			// diese exception muss auftreten
			logger.info("pseudonym anonymised - expected exception was thrown: " + e);
		}
		domainDTO = domainManager.getDomain(DOMAIN);
		assertEquals(psnCount, domainDTO.getNumberOfPseudonyms(), "wrong number of pseudonyms after anonymisation");
		assertEquals(anoCount + 1, domainDTO.getNumberOfAnonyms(), "wrong number of anonyms after anonymisation");
		domainDTO = domainManager.getDomain(AnonymDomain.NAME);
		assertEquals(allAnoCount + 1, domainDTO.getNumberOfPseudonyms(), "wrong number of anonyms within internal anonymisation domain after anonymisation");
		psnManager.getOrCreatePseudonymFor(origValue + "x", DOMAIN);
		String anonym = "###_anonym_###_123_###_anonym_###";
		Set<String> psnsForAnonymisation = new HashSet<>();
		psnsForAnonymisation.add(origValue);
		psnsForAnonymisation.add(origValue + "x");
		psnsForAnonymisation.add(anonym);
		Map<String, AnonymisationResult> anoResult = psnManager.anonymiseEntries(psnsForAnonymisation, DOMAIN);
		assertEquals(AnonymisationResult.SUCCESS, anoResult.get(origValue + "x"), "value was not anonymised as it should have been");
		assertEquals(AnonymisationResult.ALREADY_ANONYMISED, anoResult.get(anonym),
				"value was not recognised as already anonymised as it should have been");
		assertEquals(AnonymisationResult.NOT_FOUND, anoResult.get(origValue), "unknown value was anonymised");

		int sizeBefore = domainManager.listPSNs(DOMAIN).size();
		String pseudonym2 = psnManager.getOrCreatePseudonymFor(origValue, DOMAIN);
		logger.info("generated new pseudonym '" + pseudonym2 + "' for value " + origValue);
		assertNotEquals("value not anonymised", pseudonym, pseudonym2);
		int sizeAfter = domainManager.listPSNs(DOMAIN).size();
		assertEquals(sizeBefore + 1, sizeAfter, "generated pseudonym is not added to parent-domain");
		GeneratorAlphabetRestriction restriction = domainManager.getRestrictionForCheckDigitClass(Verhoeff.class.getName());
		assertEquals(GeneratorAlphabetRestriction.CONST_10, restriction, "restriction for Verhoeff arent the expected");
		logger.info("### basic test end");
	}

	@Test
	public void testAdd() throws Exception
	{
		logger.info("### test adding pseudonyms start");
		long time = System.currentTimeMillis();
		long count = getCountForDomain(DOMAIN);
		assertEquals(count, domainManager.listPSNs(DOMAIN).size(),
				"wrong number of pseudonyms assigned to domain (count versus list)");
		logger.info("pseudonyms so far: " + count);
		logger.info("try to add a new pseudonym two times");
		String value = "add_" + time;
		psnManager.getOrCreatePseudonymFor(value, DOMAIN);
		assertEquals(count + 1, getCountForDomain(DOMAIN), "pseudonym generated, but not added to domain.list");
		psnManager.getOrCreatePseudonymFor(value, DOMAIN);
		assertEquals(count + 1, getCountForDomain(DOMAIN), "an existing pseudonym is added to domain.list by mistake");
		count = getCountForDomain(DOMAIN);
		logger.info("pseudonyms after adding: " + count);
		int n = 5;
		logger.info("add " + n + " pseudonyms");
		for (int i = 1; i <= n; i++)
		{
			Thread.sleep(1);
			psnManager.getOrCreatePseudonymFor("add_" + System.currentTimeMillis(), DOMAIN);
			assertEquals(count + i, getCountForDomain(DOMAIN), "pseudonym generated, but not added to domain.list");
		}
		logger.info("time for adding: " + (System.currentTimeMillis() - time) / 1000 + " seconds");
		logger.info("pseudonyms after adding: " + getCountForDomain(DOMAIN));
		assertEquals(count + n, domainManager.listPSNs(DOMAIN).size(),
				"wrong number of pseudonyms assigned to domain (expected versus list)");
		assertEquals(getCountForDomain(DOMAIN), domainManager.listPSNs(DOMAIN).size(),
				"wrong number of pseudonyms assigned to domain (count versus list)");
		logger.info("anonymise one entry");
		psnManager.anonymiseEntry(value, DOMAIN);
		logger.info("pseudonyms after anonymising: " + getCountForDomain(DOMAIN));
		// auch "count + n", da ja das pseudonym hinzugefuegt, der originaleintrag aber dafuer
		// entfernt wurde
		assertEquals(count + n, domainManager.listPSNs(DOMAIN).size(), "pseudonym anonymised, but not removed from domain.list");
		assertEquals(getCountForDomain(DOMAIN), domainManager.listPSNs(DOMAIN).size(),
				"wrong number of pseudonyms assigned to domain (relation versus list)");
		logger.info("re-add anonymised entry");
		psnManager.getOrCreatePseudonymFor(value, DOMAIN);
		logger.info("add via map - duplicate value");
		HashMap<String, String> map = new HashMap<>();
		logger.info("add via map - invalid pseudonym");
		map.clear();
		map.put(value + time, "%");
		// TODO
		// try
		// {
		// psnManager.insertValuePseudonymPairs(map, DOMAIN);
		// fail("could store a invalid pseudonym via map");
		// }
		// catch (InvalidPSNException expected)
		// {
		// logger.info("expected InvalidPSNException was thrown: " + expected.getMessage());
		// }
		assertEquals(getCountForDomain(DOMAIN), domainManager.listPSNs(DOMAIN).size(),
				"wrong number of pseudonyms assigned to domain (count versus list)");
		logger.info("add 3 value-pseudonym pairs via map");
		map.clear();
		time = System.currentTimeMillis() % 100000000; // 8 stellen
		if (time < 10000000)
		{
			time += 10000000;
		}
		Map<DomainProperties, String> properties = new HashMap<>();
		properties.put(DomainProperties.MAX_DETECTED_ERRORS, "2");
		properties.put(DomainProperties.PSN_LENGTH, "8");
		ReedSolomonLagrange generator = new ReedSolomonLagrange(new Symbol31(), config);
		String pseudo1 = time + generator.generateCheckDigits("" + time);
		map.put(value + time, pseudo1);
		map.put(value + time + 1, time + 1 + generator.generateCheckDigits("" + (time + 1)));
		map.put(value + time + 2, time + 2 + generator.generateCheckDigits("" + (time + 2)));
		logger.warn("testmethod is not safe - it's unlikely, but a duplicate pseudonym could occure");
		psnManager.insertValuePseudonymPairs(map, DOMAIN);
		assertEquals(getCountForDomain(DOMAIN), domainManager.listPSNs(DOMAIN).size(),
				"wrong number of pseudonyms assigned to domain (count versus list)");
		logger.info("try to add 1 already existing value-pseudonym pair via map");
		map.clear();
		map.put(value + time, pseudo1);
		psnManager.insertValuePseudonymPairs(map, DOMAIN);
		assertEquals(getCountForDomain(DOMAIN), domainManager.listPSNs(DOMAIN).size(),
				"wrong number of pseudonyms assigned to domain (count versus list)");
		logger.info("try to add 1 value-pseudonym pair via map with an already existing pseudonym");
		map.clear();
		map.put(value + time + "x", pseudo1);
		// TODO
		// try
		// {
		// psnManager.insertValuePseudonymPairs(map, DOMAIN);
		// fail("could add 1 value-pseudonym pair via map with an already existing pseudonym");
		// }
		// catch (DBException expected)
		// {
		// logger.info("expected DBException was thrown: " + expected.getMessage());
		// }
		assertEquals(getCountForDomain(DOMAIN), domainManager.listPSNs(DOMAIN).size(),
				"wrong number of pseudonyms assigned to domain (count versus list)");
		logger.info("try to add a value-pseudonym pair via map for an already existing value");
		map.clear();
		map.put(value + time, time + 3 + generator.generateCheckDigits("" + (time + 3)));
		// TODO
		// try
		// {
		// psnManager.insertValuePseudonymPairs(map, DOMAIN);
		// fail("could add a value-pseudonym pair via map for an already existing value");
		// }
		// catch (DBException expected)
		// {
		// logger.info("expected DBException was thrown: " + expected.getMessage());
		// }
		logger.info("pseudonyms after adding: " + getCountForDomain(DOMAIN));
		// + 1 re-added + 3 via map
		assertEquals(count + n + 1 + 3, domainManager.listPSNs(DOMAIN).size(),
				"wrong number of pseudonyms assigned to domain (expected versus list)");
		assertEquals(getCountForDomain(DOMAIN), domainManager.listPSNs(DOMAIN).size(),
				"wrong number of pseudonyms assigned to domain (relation versus list)");
		PaginationConfig pConfig = new PaginationConfig();
		count = domainManager.countPSNsForDomains(List.of(DOMAIN), pConfig);
		assertEquals(getCountForDomain(DOMAIN), domainManager.listPSNs(DOMAIN).size(),
				"wrong number of pseudonyms assigned to domain (relation versus count)");
		value = "count_" + time;
		psnManager.getOrCreatePseudonymFor(value, DOMAIN_FOR_DELETE);
		long count2 = domainManager.countPSNsForDomains(List.of(DOMAIN_FOR_DELETE), pConfig);
		DomainConfig config = new DomainConfig();
		config.setPsnsDeletable(true);
		config.setPsnLength(2);
		DomainInDTO domainDTO = new DomainInDTO(DOMAIN_FOR_GEN_TEST, DOMAIN_FOR_GEN_TEST, Verhoeff.class.getName(), Numbers.class.getName(), config, "test-kommentar", null);
		domainManager.addDomain(domainDTO);
		assertEquals(count + count2, domainManager.countPSNsForDomains(List.of(DOMAIN, DOMAIN_FOR_DELETE, DOMAIN_FOR_GEN_TEST), pConfig),
				"wrong number of pseudonyms counted for multiple domains");
		psnManager.deleteEntry(value, DOMAIN_FOR_DELETE);
		domainManager.deleteDomain(DOMAIN_FOR_DELETE);
		domainManager.deleteDomain(DOMAIN_FOR_GEN_TEST);
		logger.info("### test adding pseudonyms end");
	}

	private long getCountForDomain(String domainID) throws Exception
	{
		return domainManager.getDomain(domainID).getNumberOfPseudonyms();
	}

	@Test
	public void listPSN() throws Exception
	{
		List<PSNDTO> psnList = domainManager.listPSNs(DOMAIN);
		logger.info("list the first 10 of " + psnList.size() + " psn for domain '" + DOMAIN + "'");
		int count = 0;
		for (PSNDTO dto : psnList)
		{
			logger.info(count++ + " : " + dto);
			if (count == 10)
			{
				break;
			}
		}
	}

	@Test
	public void listMethods() throws Exception
	{
		logger.info("### add pseudonyms with getOrCreatePseudonymForList start");
		HashSet<String> values = new HashSet<>(Arrays.asList("entry_1", "entry_2", "entry_3"));
		Map<String, String> pseudonymMap = psnManager.getOrCreatePseudonymForList(values, DOMAIN);
		HashSet<String> pseudonyms = new HashSet<>();
		for (Entry<String, String> entry : pseudonymMap.entrySet())
		{
			logger.info("pseudonym for value '" + entry.getKey() + "' is '" + entry.getValue() + "'");
			pseudonyms.add(entry.getValue());
		}
		logger.info("### add pseudonyms with getOrCreatePseudonymForList end");
		logger.info("### list values for pseudonyms start");
		Map<String, String> pseudonymMap2 = psnManager.getValueForList(pseudonyms, DOMAIN);
		Map<String, String> pseudonymMap3 = psnManager.getPseudonymForList(values, DOMAIN);
		Assert.isTrue(values.size() == pseudonymMap.size(), "wrong numer of elements returned via getOrCreatePseudonymForList");
		Assert.isTrue(values.size() == pseudonymMap2.size(), "wrong numer of elements returned via getValueForList");
		Assert.isTrue(values.size() == pseudonymMap3.size(), "wrong numer of elements returned via getPseudonymForList");
		for (Entry<String, String> entry : pseudonymMap2.entrySet())
		{
			values.remove(entry.getValue());
		}
		for (Entry<String, String> entry : pseudonymMap3.entrySet())
		{
			if (!PSNErrorStrings.VALUE_NOT_FOUND.equals(entry.getValue()))
			{
				pseudonyms.remove(entry.getValue());
			}
		}
		Assert.isTrue(values.isEmpty(), "not all values returned by getValueForList");
		Assert.isTrue(pseudonyms.isEmpty(), "not all pseudonyms returned by getPseudonymForList");
		logger.info("### list values for pseudonyms end");
		logger.info("### list psn for valuesWithPrefix start");
		String wrong1 = "abc";
		String wrong2 = "xabcd";
		String right1 = "abcd";
		String right2 = "abcde";
		values = new HashSet<>(Arrays.asList(wrong1, right1, right2, wrong2));
		psnManager.getOrCreatePseudonymForList(values, DOMAIN);
		pseudonymMap2 = psnManager.getPseudonymForValuePrefix("abcd", DOMAIN);
		assertFalse(pseudonymMap2.containsKey(wrong1) || pseudonymMap2.containsKey(wrong2), "wrong psn returned by getPseudonymForValuePrefix");
		assertTrue(pseudonymMap2.containsKey(right1) && pseudonymMap2.containsKey(right2), "correct psn not returned by getPseudonymForValuePrefix");
		logger.info("### list psn for valuesWithPrefix end");
	}

	@Test
	public void testDelete() throws Exception
	{
		logger.info("### delete tests start");
		logger.info("### try to delete an entry within a domain where this is forbidden");
		List<PSNDTO> psnList = domainManager.listPSNs(DOMAIN);
		if (!psnList.isEmpty())
		{
			try
			{
				psnManager.deleteEntry(psnList.get(0).getOriginalValue(), DOMAIN);
				fail("could delete a value-pseudonym pair within a domain where this is forbidden");
			}
			catch (DeletionForbiddenException expected)
			{
				logger.info("expected DeletionForbiddenException was thrown: " + expected.getMessage());
			}
		}
		logger.info("### try to delete an entry within a domain where this is allowed");
		logger.info("### add entry");
		long time = System.currentTimeMillis();
		String value = "add_" + time;
		psnManager.getOrCreatePseudonymFor(value, DOMAIN_FOR_DELETE);
		logger.info("### try to delete a non-existing entry");
		try
		{
			psnManager.deleteEntry("non-existing-dummy-value", DOMAIN_FOR_DELETE);
			fail("could delete a non-exisiting value-pseudonym pair");
		}
		catch (UnknownValueException expected)
		{
			logger.info("expected UnknownValueException was thrown: " + expected.getMessage());
		}
		logger.info("### try to delete an entry within a non-existing domain");
		try
		{
			psnManager.deleteEntry(value, "non-existing-dummy-domain");
			fail("could delete an entry within a non-existing domain");
		}
		catch (UnknownDomainException expected)
		{
			logger.info("expected UnknownDomainException was thrown: " + expected.getMessage());
		}
		logger.info("### try to delete an entry");
		psnManager.deleteEntry(value, DOMAIN_FOR_DELETE);
		List<PSNDTO> psns = domainManager.listPSNs(DOMAIN_FOR_DELETE);
		assertTrue(psns == null || psns.isEmpty(), "entry was not deleted");
		logger.info("### add entry");
		psnManager.getOrCreatePseudonymFor(value, DOMAIN_FOR_DELETE);
		logger.info("### try to delete values via list funktion");
		Set<String> values = new HashSet<>();
		String valueDontExists = "DON'T_EXISTS";
		values.add(valueDontExists);
		values.add(value);
		Map<String, DeletionResult> deletionResults = psnManager.deleteEntries(values, DOMAIN_FOR_DELETE);
		assertEquals(2, deletionResults.size(), "unexpected number of deletion results");
		assertEquals(DeletionResult.NOT_FOUND, deletionResults.get(valueDontExists), "unexpected deletion result for non-existing value");
		assertEquals(DeletionResult.SUCCESS, deletionResults.get(value), "unexpected deletion result for existing value");

		logger.info("### try to delete a domain with psns");
		try
		{
			DomainInDTO domainDTO = new DomainInDTO(DOMAIN_FOR_DELETE_2, DOMAIN_FOR_DELETE_2, Verhoeff.class.getName(), Numbers.class.getName(), new DomainConfig(), "test-kommentar", null);
			domainManager.addDomain(domainDTO);
		}
		catch (DomainInUseException ignore)
		{
			// darf nicht autreten
			throw ignore;
		}
		psnManager.getOrCreatePseudonymFor("1", DOMAIN_FOR_DELETE_2);
		psnManager.getOrCreatePseudonymFor("2", DOMAIN_FOR_DELETE_2);
		domainManager.deleteDomainWithPSNs(DOMAIN_FOR_DELETE_2);
		logger.info("### delete tests end");
	}

	private void addErrorStrings(Set<String> values)
	{
		values.add(PSNErrorStrings.INVALID_VALUE);
		values.add(PSNErrorStrings.INVALID_PSN);
		values.add(PSNErrorStrings.PSN_NOT_FOUND);
		values.add(PSNErrorStrings.VALUE_NOT_FOUND);
		values.add(PSNErrorStrings.VALUE_IS_ANONYMISED);
	}

	@Test
	public void testPSNGen() throws Exception
	{
		logger.info("### test one domain start");
		logger.info("### create domain for test");
		try
		{
			domainManager.getDomain(DOMAIN_FOR_GEN_TEST);
		}
		catch (UnknownDomainException maybe)
		{
			try
			{
				DomainConfig config = new DomainConfig();
				config.setPsnsDeletable(true);
				config.setPsnLength(2);
				DomainInDTO domainDTO = new DomainInDTO(DOMAIN_FOR_GEN_TEST, DOMAIN_FOR_GEN_TEST, Verhoeff.class.getName(), Numbers.class.getName(), config, "test-kommentar", null);
				domainManager.addDomain(domainDTO);
			}
			catch (DomainInUseException ignore)
			{
				// darf nicht autreten
				throw ignore;
			}
		}
		Set<String> values = new HashSet<>();
		Set<String> values2 = new HashSet<>();
		Set<String> values3 = new HashSet<>();
		Set<String> values4 = new HashSet<>();
		for (int i = 0; i < 50; i++)
		{
			values.add("" + i);
			values2.add("" + (i + 40));
			values3.add("" + (i + 45));
			values4.add("" + (i + 55));
		}

		addErrorStrings(values);
		addErrorStrings(values2);
		addErrorStrings(values3);
		addErrorStrings(values4);

		logger.info("### create 50 psns");
		Map<String, String> createResult = psnManager.getOrCreatePseudonymForList(values, DOMAIN_FOR_GEN_TEST);
		assertEquals(55, createResult.size());
		List<PSNDTO> psnList = domainManager.listPSNs(DOMAIN_FOR_GEN_TEST);
		assertEquals(50, psnList.size());
		logger.info("### create 40 more psns");
		createResult = psnManager.getOrCreatePseudonymForList(values2, DOMAIN_FOR_GEN_TEST);
		assertEquals(55, createResult.size());
		psnList = domainManager.listPSNs(DOMAIN_FOR_GEN_TEST);
		assertEquals(90, psnList.size());
		logger.info("### create 5 more psns");
		createResult = psnManager.getOrCreatePseudonymForList(values3, DOMAIN_FOR_GEN_TEST);
		assertEquals(55, createResult.size());
		psnList = domainManager.listPSNs(DOMAIN_FOR_GEN_TEST);
		assertEquals(95, psnList.size());
		logger.info("### try to create to much pseudonyms");
		try
		{
			psnManager.getOrCreatePseudonymForList(values4, DOMAIN_FOR_GEN_TEST);
			Assert.fail("could create more pseudonyms than possible");
		}
		catch (DomainIsFullException expected)
		{
			logger.info("expected exception occured: " + expected.getMessage());
		}
		logger.info("### some single create / delete");
		psnManager.getOrCreatePseudonymFor("X", DOMAIN_FOR_GEN_TEST);
		psnManager.getOrCreatePseudonymFor("X", DOMAIN_FOR_GEN_TEST);
		psnManager.getOrCreatePseudonymFor("Y", DOMAIN_FOR_GEN_TEST);
		try
		{
			psnManager.getOrCreatePseudonymFor(PSNErrorStrings.INVALID_VALUE, DOMAIN_FOR_GEN_TEST);
			Assert.fail("could create a pseudonym for an error string");
		}
		catch (InvalidParameterException expected)
		{
			logger.info("expected exception occured: " + expected.getMessage());
		}
		psnList = domainManager.listPSNs(DOMAIN_FOR_GEN_TEST);
		assertEquals(97, psnList.size());
		DomainOutDTO domainDTO = domainManager.getDomain(DOMAIN_FOR_GEN_TEST);
		assertEquals(97, domainDTO.getPercentPsnsUsed());
		psnManager.deleteEntry("X", DOMAIN_FOR_GEN_TEST);
		psnManager.deleteEntry("Y", DOMAIN_FOR_GEN_TEST);
		logger.info("### delete some psns");
		Map<String, DeletionResult> delResult = psnManager.deleteEntries(values, DOMAIN_FOR_GEN_TEST);
		assertEquals(55, delResult.size());
		int count = 0;
		for (DeletionResult del : delResult.values())
		{
			if (DeletionResult.SUCCESS.equals(del))
			{
				count++;
			}
		}
		assertEquals(50, count);
		psnList = domainManager.listPSNs(DOMAIN_FOR_GEN_TEST);
		assertEquals(45, psnList.size());
		logger.info("### delete all remaining");
		delResult = psnManager.deleteEntries(values2, DOMAIN_FOR_GEN_TEST);
		assertEquals(55, delResult.size());
		count = 0;
		for (DeletionResult del : delResult.values())
		{
			if (DeletionResult.SUCCESS.equals(del))
			{
				count++;
			}
		}
		assertEquals(40, count);
		psnManager.deleteEntries(values3, DOMAIN_FOR_GEN_TEST);
		psnList = domainManager.listPSNs(DOMAIN_FOR_GEN_TEST);
		assertNotNull(psnList);
		assertTrue(psnList.isEmpty());
		logger.info("### delete domain");
		domainManager.deleteDomain(DOMAIN_FOR_GEN_TEST);
		logger.info("### recreate domain for test without cache");
		try
		{
			domainManager.getDomain(DOMAIN_FOR_GEN_TEST);
		}
		catch (UnknownDomainException maybe)
		{
			try
			{
				DomainConfig config = new DomainConfig();
				config.setPsnsDeletable(true);
				config.setPsnLength(2);
				config.setForceCache(ForceCache.OFF);
				DomainInDTO domain2DTO = new DomainInDTO(DOMAIN_FOR_GEN_TEST, DOMAIN_FOR_GEN_TEST, Verhoeff.class.getName(), Numbers.class.getName(), config, "test-kommentar", null);
				domainManager.addDomain(domain2DTO);
			}
			catch (DomainInUseException ignore)
			{
				// darf nicht autreten
				throw ignore;
			}
		}
		logger.info("### create 50 psns");
		createResult = psnManager.getOrCreatePseudonymForList(values, DOMAIN_FOR_GEN_TEST);
		assertEquals(55, createResult.size());
		psnList = domainManager.listPSNs(DOMAIN_FOR_GEN_TEST);
		assertEquals(50, psnList.size());
		logger.info("### create 40 more psns");
		createResult = psnManager.getOrCreatePseudonymForList(values2, DOMAIN_FOR_GEN_TEST);
		assertEquals(55, createResult.size());
		psnList = domainManager.listPSNs(DOMAIN_FOR_GEN_TEST);
		assertEquals(90, psnList.size());
		logger.info("### some single create / delete");
		psnManager.getOrCreatePseudonymFor("X", DOMAIN_FOR_GEN_TEST);
		psnManager.getOrCreatePseudonymFor("X", DOMAIN_FOR_GEN_TEST);
		psnManager.getOrCreatePseudonymFor("Y", DOMAIN_FOR_GEN_TEST);
		psnList = domainManager.listPSNs(DOMAIN_FOR_GEN_TEST);
		assertEquals(92, psnList.size());
		domainDTO = domainManager.getDomain(DOMAIN_FOR_GEN_TEST);
		assertEquals(92, domainDTO.getPercentPsnsUsed());
		psnManager.deleteEntry("X", DOMAIN_FOR_GEN_TEST);
		psnManager.deleteEntry("Y", DOMAIN_FOR_GEN_TEST);
		logger.info("### delete some psns");
		delResult = psnManager.deleteEntries(values, DOMAIN_FOR_GEN_TEST);
		assertEquals(55, delResult.size());
		count = 0;
		for (DeletionResult del : delResult.values())
		{
			if (DeletionResult.SUCCESS.equals(del))
			{
				count++;
			}
		}
		assertEquals(50, count);
		psnList = domainManager.listPSNs(DOMAIN_FOR_GEN_TEST);
		assertEquals(40, psnList.size());
		logger.info("### delete all remaining");
		delResult = psnManager.deleteEntries(values2, DOMAIN_FOR_GEN_TEST);
		assertEquals(55, delResult.size());
		count = 0;
		for (DeletionResult del : delResult.values())
		{
			if (DeletionResult.SUCCESS.equals(del))
			{
				count++;
			}
		}
		assertEquals(40, count);
		psnList = domainManager.listPSNs(DOMAIN_FOR_GEN_TEST);
		assertNotNull(psnList);
		assertTrue(psnList.isEmpty());
		logger.info("### delete domain");
		domainManager.deleteDomain(DOMAIN_FOR_GEN_TEST);
	}

	@Test
	public void testTreeAndNet() throws Exception
	{
		logger.info("### tree and net tests start");
		// ein paar domains anlegen, beispiel (achtung! bei untergeordneten domains die parentdomain
		// setzen!):
		// try {
		// domainManager.getDomainObject("domain_for_tree_root");
		// } catch (UnknownDomainException maybe) {
		// try {
		// DomainDTO domainDTO = new DomainDTO(DOMAIN_FOR_DELETE,
		// ReedSolomonLagrange.class.getName(), Symbol31.class.getName(),
		// propertiesString + GeneratorProperties.PSNS_DELETABLE + " = true;", "test-kommentar", 0,
		// null);
		// domainManager.addDomain(domainDTO);
		// } catch (DomainInUseException ignore) {
		// // darf nicht autreten
		// throw ignore;
		// }
		// }

		// pseudonyme in den domains anlegen
		// hier baum aufbauen (PSNTreeDTO)
		// baum aus gpas abfragen
		// baeume mit equals vergleichen

		// noch mehr pseudonyme anlegen (mit insertPSN): alle denkbaren faelle abdecken
		// * kreis
		// * n-m beziehung
		// * selbstreferenz
		// * was noch?
		// hier netz aufbauen (PSNNetDTO)
		// netz vom gpas abfragen
		// netze mit equals vergleichen

		// im finally:
		// pseudonyme loeschen
		// domains loeschen
		logger.info("### tree and net tests end");
	}
}
