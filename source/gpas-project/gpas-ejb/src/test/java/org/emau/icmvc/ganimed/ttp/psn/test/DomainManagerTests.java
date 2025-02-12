package org.emau.icmvc.ganimed.ttp.psn.test;

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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.emau.icmvc.ganimed.ttp.psn.alphabets.Numbers;
import org.emau.icmvc.ganimed.ttp.psn.alphabets.Symbol31;
import org.emau.icmvc.ganimed.ttp.psn.config.DomainConfig;
import org.emau.icmvc.ganimed.ttp.psn.config.PaginationConfig;
import org.emau.icmvc.ganimed.ttp.psn.dto.DomainInDTO;
import org.emau.icmvc.ganimed.ttp.psn.dto.DomainOutDTO;
import org.emau.icmvc.ganimed.ttp.psn.enums.ValidateViaParents;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.DomainInUseException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.InvalidAlphabetException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.InvalidCheckDigitClassException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.InvalidGeneratorException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.InvalidParameterException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.InvalidParentDomainException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.InvalidUpdateInUseOperationException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.UnknownDomainException;
import org.emau.icmvc.ganimed.ttp.psn.generator.ReedSolomonLagrange;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

@Tag("Integration")
public class DomainManagerTests extends AbstractSoapBasedTest
{
	private static final String PARENT = "Parent";
	private static final String CHILD = "Child";
	private static final String DOMAIN = "test12345_domain_test";
	private static final DomainConfig CONFIG = new DomainConfig();
	private static final DomainInDTO DOMAIN_DTO = new DomainInDTO(DOMAIN, DOMAIN, ReedSolomonLagrange.class.getName(), Symbol31.class.getName(), CONFIG, UNITTEST_MARKER_COMMENT, null);

	public static boolean isEnabled()
	{
		return isServiceAvailable(DOMAIN_MANAGER_WSDL_URL);
	}

	/**
	 * Clear all in unit tests created domains
	 */
	@BeforeEach
	public void setup() throws Exception
	{
		initDomainManager();
		deleteTestDomains();
	}

	@Test
	public void testValidDomain() throws Exception
	{
		logger.info("### test a valid domain start");
		domainManager.addDomain(DOMAIN_DTO);
		try
		{
			// dupletten-check
			logger.info("try to add a duplicate domain");
			domainManager.addDomain(DOMAIN_DTO);
			fail("duplicate domain persisted");
		}
		catch (DomainInUseException expected)
		{
			logger.info("duplicate domain not persisted - expected DomainInUseException was thrown: " + expected.getMessage());
		}
		// loeschen und neu anlegen
		logger.info("delete domain");
		domainManager.deleteDomain(DOMAIN);
		try
		{
			// dupletten-check
			logger.info("try to delete a deleted domain");
			domainManager.deleteDomain(DOMAIN);
			fail("not existing domain deleted");
		}
		catch (UnknownDomainException expected)
		{
			logger.info("not existing domain not deleted - expected UnknownDomainException was thrown: " + expected.getMessage());
		}
		logger.info("re-add domain with custom alphabet ...");
		// test mit generischem alphabet - achtung! anzahl buchstaben muss primzahl sein
		DomainInDTO domainDTO = new DomainInDTO(DOMAIN, DOMAIN, ReedSolomonLagrange.class.getName(), "a,b,c,d,e", CONFIG, UNITTEST_MARKER_COMMENT, null);
		domainManager.addDomain(domainDTO);
		logger.info("add a child domain");
		domainDTO = new DomainInDTO(DOMAIN + '1', DOMAIN + '1', ReedSolomonLagrange.class.getName(), "a,b,c,d,e", CONFIG, UNITTEST_MARKER_COMMENT, Collections.singletonList(DOMAIN));
		domainManager.addDomain(domainDTO);
		try
		{
			// dupletten-check
			logger.info("try to delete the parent domain");
			domainManager.deleteDomain(DOMAIN);
			fail("parent domain deleted");
		}
		catch (DomainInUseException expected)
		{
			logger.info("parent domain not deleted - expected DomainInUseException was thrown: " + expected.getMessage());
		}

		logger.info("### test a valid domain end");
	}

	@Test
	public void testInvalidDomain() throws Exception
	{
		logger.info("### test some invalid domains start");
		try
		{
			// falsche pruefziffernklasse
			logger.info("trying to add a domain with wrong check digit class");
			domainManager.addDomain(
					new DomainInDTO(DOMAIN, DOMAIN, "CheckDigitClassDontExist", Symbol31.class.getName(), CONFIG, UNITTEST_MARKER_COMMENT, null));
			fail("domain with wrong check digit class persisted");
		}
		catch (InvalidCheckDigitClassException e)
		{
			// diese exception muss auftreten
			logger.info("domain with wrong check digit class not persisted - expected InvalidCheckDigitClassException was thrown: " + e.getMessage());
		}
		try
		{
			// falsches alphabet
			logger.info("trying to add a domain with wrong alphabet");
			domainManager.addDomain(
					new DomainInDTO(DOMAIN, DOMAIN, ReedSolomonLagrange.class.getName(), "AlphabetDontExist", CONFIG, UNITTEST_MARKER_COMMENT, null));
			fail("domain with wrong alphabet persisted");
		}
		catch (InvalidAlphabetException e)
		{
			// diese exception muss auftreten
			logger.info("domain with wrong alphabet not persisted - expected InvalidAlphabetException was thrown: " + e.getMessage());
		}
		try
		{
			// alphabet passt nicht zur pruefziffernklasse
			logger.info("trying to add a domain with an alphabet which doesn't fit to the check digit class");
			domainManager.addDomain(
					new DomainInDTO(DOMAIN, DOMAIN, ReedSolomonLagrange.class.getName(), Numbers.class.getName(), CONFIG, UNITTEST_MARKER_COMMENT, null));
			fail("domain with an alphabet which doesn't fit to the check digit class persisted");
		}
		catch (InvalidGeneratorException e)
		{
			// diese exception muss auftreten
			logger.info(
					"domain with an alphabet which doesn't fit to the check digit class not persisted - expected InvalidGeneratorException was thrown: "
							+ e.getMessage());
		}
		try
		{
			// alphabet passt nicht zur pruefziffernklasse
			logger.info("trying to add a domain with an unknown parent domain");
			domainManager.addDomain(new DomainInDTO(DOMAIN, DOMAIN, ReedSolomonLagrange.class.getName(), Symbol31.class.getName(), CONFIG,
					UNITTEST_MARKER_COMMENT, Collections.singletonList("abcdef_gibt_es_nicht")));
			fail("domain with an unknown parent domain persisted");
		}
		catch (InvalidParentDomainException e)
		{
			// diese exception muss auftreten
			logger.info("domain with an unknown parent domain not persisted - expected InvalidParentDomainException was thrown: " + e.getMessage());
		}
		logger.info("### test some invalid domains end");
	}

	@Test
	public void testDateFields() throws Exception
	{
		logger.info("### test date fields of domains start");
		domainManager.addDomain(DOMAIN_DTO);
		DomainOutDTO domainDTO = domainManager.getDomain(DOMAIN);
		assertEquals(domainDTO.getCreateDate(), domainDTO.getUpdateDate(), "update date of domain is =/= create date");
		Date updateDate = domainDTO.getUpdateDate();
		domainManager.updateDomainInUse(DOMAIN, "", UNITTEST_MARKER_COMMENT, null, false, false);
		domainDTO = domainManager.getDomain(DOMAIN);
		assertTrue(updateDate.compareTo(domainDTO.getUpdateDate()) < 0, "update date of domain isn't updated on update - what a message ;-)");
		logger.info("### test date fields of domains end");
	}

	@Test
	public void testLists() throws Exception
	{
		logger.info("### test list functionality start");
		logger.info("domains:");
		List<DomainOutDTO> domains = domainManager.listDomains();
		for (DomainOutDTO domain : domains)
		{
			logger.info(domain);
		}
		logger.info("possible properties:");
		logger.info("test getDomainsForPrefix");
		final String PREFIX = "test_123";
		DomainConfig dConfig = new DomainConfig();
		dConfig.setPsnPrefix(PREFIX);
		DomainInDTO domainDTO = new DomainInDTO(DOMAIN, DOMAIN, ReedSolomonLagrange.class.getName(), Symbol31.class.getName(), dConfig, UNITTEST_MARKER_COMMENT, null);
		List<DomainOutDTO> list = domainManager.getDomainsForPrefix(PREFIX);
		int size = list == null ? 0 : list.size();
		domainManager.addDomain(domainDTO);
		String additionalDomainName = DOMAIN + '1';
		domainDTO.setName(additionalDomainName);
		domainManager.addDomain(domainDTO);
		list = domainManager.getDomainsForPrefix(PREFIX);
		assertEquals(list.size(), size + 2);
		list = domainManager.getDomainsForPrefix(PREFIX + "dummy which don't exists");
		assertNotNull(list);
		assertTrue(list.isEmpty(), "getDomainsForPrefix found domains with a not-existing prefix");
		logger.info("### test list functionality end");
	}

	/* Create Domain Tests */

	@Test
	void createDomainWithParent() throws Exception
	{
		// Arrange
		DomainInDTO parent = new DomainInDTO(PARENT, PARENT, ReedSolomonLagrange.class.getName(), Symbol31.class.getName(), CONFIG, UNITTEST_MARKER_COMMENT, null);
		domainManager.addDomain(parent);

		DomainInDTO child = new DomainInDTO(CHILD, CHILD, ReedSolomonLagrange.class.getName(), Symbol31.class.getName(), CONFIG, UNITTEST_MARKER_COMMENT, Collections.singletonList(PARENT));

		// Act
		domainManager.addDomain(child);

		// Assert
		assertEquals(1, domainManager.getDomain(CHILD).getParentDomainNames().size());
		assertEquals(parent.getName(), domainManager.getDomain(CHILD).getParentDomainNames().getFirst());
	}

	@Test
	void createDomainWithMultipleParents() throws Exception
	{
		// Arrange
		DomainInDTO parent1 = new DomainInDTO("Parent1", "Parent1", ReedSolomonLagrange.class.getName(), Symbol31.class.getName(), CONFIG, UNITTEST_MARKER_COMMENT, null);
		domainManager.addDomain(parent1);
		DomainInDTO parent2 = new DomainInDTO("Parent2", "Parent2", ReedSolomonLagrange.class.getName(), Symbol31.class.getName(), CONFIG, UNITTEST_MARKER_COMMENT, null);
		domainManager.addDomain(parent2);

		DomainInDTO child = new DomainInDTO(CHILD, CHILD, ReedSolomonLagrange.class.getName(), Symbol31.class.getName(), CONFIG, UNITTEST_MARKER_COMMENT, Arrays.asList("Parent1", "Parent2"));

		// Act
		domainManager.addDomain(child);

		// Assert
		assertEquals(2, domainManager.getDomain(CHILD).getParentDomainNames().size());
		assertEquals(parent1.getName(), domainManager.getDomain(CHILD).getParentDomainNames().get(0));
		assertEquals(parent2.getName(), domainManager.getDomain(CHILD).getParentDomainNames().get(1));
	}

	@Test
	void createDomainWithSelfAsParent()
	{
		// Arrange
		DomainInDTO child = new DomainInDTO(CHILD, CHILD, ReedSolomonLagrange.class.getName(), Symbol31.class.getName(), CONFIG, UNITTEST_MARKER_COMMENT, Collections.singletonList(CHILD));

		// Act & Assert
		assertThrows(InvalidParentDomainException.class, () -> domainManager.addDomain(child));
	}

	@Test
	void checkFunctionParameters() throws Exception
	{
		DomainInDTO domain = new DomainInDTO(null, "Domain", ReedSolomonLagrange.class.getName(), Symbol31.class.getName(), CONFIG, UNITTEST_MARKER_COMMENT, null);
		assertThrows(InvalidParameterException.class, () -> domainManager.addDomain(domain));
		domain.setName(DOMAIN);
		domainManager.addDomain(domain);
		assertThrows(InvalidParameterException.class, () -> domainManager.countPSNsForDomains(new ArrayList<>(), new PaginationConfig()));
		List<String> domainNames = new ArrayList<>();
		domainNames.add(DOMAIN);
		domainNames.add("");
		assertThrows(InvalidParameterException.class, () -> domainManager.countPSNsForDomains(domainNames, new PaginationConfig()));
	}

	@Test
	void createDomainWithSameParentTwice() throws Exception
	{
		// Arrange
		DomainInDTO parent = new DomainInDTO(PARENT, PARENT, ReedSolomonLagrange.class.getName(), Symbol31.class.getName(), CONFIG, UNITTEST_MARKER_COMMENT, null);
		domainManager.addDomain(parent);

		DomainInDTO child = new DomainInDTO(CHILD, CHILD, ReedSolomonLagrange.class.getName(), Symbol31.class.getName(), CONFIG, UNITTEST_MARKER_COMMENT, Arrays.asList(PARENT, PARENT));

		// Act & Assert
		assertThrows(InvalidParentDomainException.class, () -> domainManager.addDomain(child));
	}

	@Test
	void createDomainWithUnknownParent()
	{
		// Arrange
		DomainInDTO child = new DomainInDTO(CHILD, CHILD, ReedSolomonLagrange.class.getName(), Symbol31.class.getName(), CONFIG, UNITTEST_MARKER_COMMENT, Arrays.asList(PARENT, PARENT));

		// Act & Assert
		assertThrows(InvalidParentDomainException.class, () -> domainManager.addDomain(child));
	}

	/* Delete Domain Tests */

	@Test
	void deleteDomainInUseAsParent() throws Exception
	{
		// Arrange
		DomainInDTO parent = new DomainInDTO(PARENT, PARENT, ReedSolomonLagrange.class.getName(), Symbol31.class.getName(), CONFIG, UNITTEST_MARKER_COMMENT, null);
		domainManager.addDomain(parent);

		DomainInDTO child = new DomainInDTO(CHILD, CHILD, ReedSolomonLagrange.class.getName(), Symbol31.class.getName(), CONFIG, UNITTEST_MARKER_COMMENT, Collections.singletonList(PARENT));
		domainManager.addDomain(child);

		// Act & Assert
		assertThrows(DomainInUseException.class, () -> domainManager.deleteDomain(parent.getName()));
	}

	/* Update Domain Tests */

	@Test
	void updateDomainAddParent() throws Exception
	{
		// Arrange
		DomainInDTO parent = new DomainInDTO(PARENT, PARENT, ReedSolomonLagrange.class.getName(), Symbol31.class.getName(), CONFIG, UNITTEST_MARKER_COMMENT, null);
		domainManager.addDomain(parent);

		DomainInDTO child = new DomainInDTO(CHILD, CHILD, ReedSolomonLagrange.class.getName(), Symbol31.class.getName(), CONFIG, UNITTEST_MARKER_COMMENT, null);
		domainManager.addDomain(child);

		// Act
		child.setParentDomainNames(Collections.singletonList(PARENT));
		domainManager.updateDomain(child);

		// Assert
		assertEquals(1, domainManager.getDomain(CHILD).getParentDomainNames().size());
		assertEquals(parent.getName(), domainManager.getDomain(CHILD).getParentDomainNames().getFirst());
	}

	@Test
	void updateDomainAddSelfAsParent() throws Exception
	{
		// Arrange
		DomainInDTO child = new DomainInDTO(CHILD, CHILD, ReedSolomonLagrange.class.getName(), Symbol31.class.getName(), CONFIG, UNITTEST_MARKER_COMMENT, null);
		domainManager.addDomain(child);
		child.setParentDomainNames(Collections.singletonList(CHILD));

		// Act & Assert
		assertThrows(InvalidParentDomainException.class, () -> domainManager.updateDomain(child));
	}

	@Test
	void updateDomainAddSameParentTwice() throws Exception
	{
		// Arrange
		DomainInDTO parent = new DomainInDTO(PARENT, PARENT, ReedSolomonLagrange.class.getName(), Symbol31.class.getName(), CONFIG, UNITTEST_MARKER_COMMENT, null);
		domainManager.addDomain(parent);

		DomainInDTO child = new DomainInDTO(CHILD, CHILD, ReedSolomonLagrange.class.getName(), Symbol31.class.getName(), CONFIG, UNITTEST_MARKER_COMMENT, null);
		domainManager.addDomain(child);
		child.setParentDomainNames(Arrays.asList(PARENT, PARENT));

		// Act & Assert
		assertThrows(InvalidParentDomainException.class, () -> domainManager.updateDomain(child));
	}

	@Test
	void updateDomainAddSameParentAgain() throws Exception
	{
		// Arrange
		DomainInDTO parent = new DomainInDTO(PARENT, PARENT, ReedSolomonLagrange.class.getName(), Symbol31.class.getName(), CONFIG, UNITTEST_MARKER_COMMENT, null);
		domainManager.addDomain(parent);

		DomainInDTO child = new DomainInDTO(CHILD, CHILD, ReedSolomonLagrange.class.getName(), Symbol31.class.getName(), CONFIG, UNITTEST_MARKER_COMMENT, Collections.singletonList(PARENT));
		domainManager.addDomain(child);
		child.setParentDomainNames(Arrays.asList(PARENT, PARENT));

		// Act & Assert
		assertThrows(InvalidParentDomainException.class, () -> domainManager.updateDomain(child));
	}

	@Test
	void updateDomainAddUnknownParent() throws Exception
	{
		// Arrange
		DomainInDTO child = new DomainInDTO(CHILD, CHILD, ReedSolomonLagrange.class.getName(), Symbol31.class.getName(), CONFIG, UNITTEST_MARKER_COMMENT, null);
		domainManager.addDomain(child);
		child.setParentDomainNames(Collections.singletonList(PARENT));

		// Act & Assert
		assertThrows(InvalidParentDomainException.class, () -> domainManager.updateDomain(child));
	}

	@Test
	void updateDomainRemoveParent() throws Exception
	{
		// Arrange
		DomainInDTO parent = new DomainInDTO(PARENT, PARENT, ReedSolomonLagrange.class.getName(), Symbol31.class.getName(), CONFIG, UNITTEST_MARKER_COMMENT, null);
		domainManager.addDomain(parent);

		DomainInDTO child = new DomainInDTO(CHILD, CHILD, ReedSolomonLagrange.class.getName(), Symbol31.class.getName(), CONFIG, UNITTEST_MARKER_COMMENT, Collections.singletonList(PARENT));
		domainManager.addDomain(child);

		// Act
		child.setParentDomainNames(null);
		domainManager.updateDomain(child);

		// Assert
		assertEquals(0, domainManager.getDomain(CHILD).getParentDomainNames().size());
	}

	@Test
	void updateDomainInUseRemoveParentWithDisabledValidation() throws Exception
	{
		// Arrange
		DomainInDTO parent1 = new DomainInDTO(PARENT, PARENT, ReedSolomonLagrange.class.getName(), Symbol31.class.getName(), CONFIG, UNITTEST_MARKER_COMMENT, null);
		domainManager.addDomain(parent1);
		DomainInDTO parent2 = new DomainInDTO(PARENT + 2, PARENT + 2, ReedSolomonLagrange.class.getName(), Symbol31.class.getName(), CONFIG, UNITTEST_MARKER_COMMENT, null);
		domainManager.addDomain(parent2);
		
		DomainInDTO child = new DomainInDTO(CHILD, CHILD, ReedSolomonLagrange.class.getName(), Symbol31.class.getName(), CONFIG, UNITTEST_MARKER_COMMENT, null);
		child.setParentDomainNames(Arrays.asList(PARENT, PARENT + 2));
		domainManager.addDomain(child);

		// Act
		domainManager.updateDomainInUse(CHILD, CHILD, UNITTEST_MARKER_COMMENT, Collections.singletonList(PARENT), false, false);
		
		// Assert
		assertEquals(1, domainManager.getDomain(CHILD).getParentDomainNames().size());
	}

	@Test
	void updateDomainInUseRemoveParentWithEnabledValidation() throws Exception
	{
		// Arrange
		DomainInDTO parent1 = new DomainInDTO(PARENT, PARENT, ReedSolomonLagrange.class.getName(), Symbol31.class.getName(), CONFIG, UNITTEST_MARKER_COMMENT, null);
		domainManager.addDomain(parent1);
		DomainInDTO parent2 = new DomainInDTO(PARENT + 2, PARENT + 2, ReedSolomonLagrange.class.getName(), Symbol31.class.getName(), CONFIG, UNITTEST_MARKER_COMMENT, null);
		domainManager.addDomain(parent2);

		DomainConfig configWithValidateInParents = new DomainConfig();
		configWithValidateInParents.setValidateValuesViaParents(ValidateViaParents.VALIDATE);
		DomainInDTO child = new DomainInDTO(CHILD, CHILD, ReedSolomonLagrange.class.getName(), Symbol31.class.getName(), configWithValidateInParents, UNITTEST_MARKER_COMMENT, null);
		child.setParentDomainNames(Arrays.asList(PARENT, PARENT + 2));
		domainManager.addDomain(child);

		// Act & Assert
		assertThrows(InvalidUpdateInUseOperationException.class, () ->
				domainManager.updateDomainInUse(CHILD, CHILD, UNITTEST_MARKER_COMMENT, Collections.singletonList(PARENT), false, false));
	}
}
