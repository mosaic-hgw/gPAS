package org.emau.icmvc.ttp.psn.frontend.controller;

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

import java.text.MessageFormat;
import java.util.Collections;

import javax.faces.application.FacesMessage;

import org.emau.icmvc.ganimed.ttp.psn.dto.DomainOutDTO;
import org.emau.icmvc.ganimed.ttp.psn.dto.PSNDTO;
import org.emau.icmvc.ganimed.ttp.psn.dto.PSNNetDTO;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.DBException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.DomainIsFullException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.InvalidGeneratorException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.InvalidParameterException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.UnknownDomainException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.UnknownValueException;
import org.emau.icmvc.ttp.psn.frontend.controller.testtools.gPASWebTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

class PSNControllerTest extends gPASWebTest
{
	@InjectMocks
	private PSNController psnController;

	@BeforeEach
	void setUpPsnControllerTest()
	{
		initMocks(this);
	}

	@Test
	void onSavePseudonym() throws UnknownDomainException, InvalidParameterException, UnknownValueException, DBException, InvalidGeneratorException, DomainIsFullException
	{
		// Arrange
		String originalValue = "1";
		String pseudonym = "psn_1";
		String domainName = "Test";
		Object[] args = { originalValue, pseudonym, domainName };
		DomainOutDTO domainDTO = mock(DomainOutDTO.class);

		psnController.setOriginalValue(originalValue);
		psnController.setDomain(domainDTO);
		psnController.setSelectedDomain(domainDTO);

		when(domainDTO.getName()).thenReturn(domainName);
		when(service.getPseudonymFor(originalValue, domainName)).thenThrow(UnknownValueException.class);
		when(service.getOrCreatePseudonymFor(originalValue, domainName)).thenReturn(pseudonym);
		when(service.getPSNNetFor(any())).thenReturn(new PSNNetDTO());

		// Act
		psnController.onSavePseudonym();

		// Assert
		verify(service).getOrCreatePseudonymFor(originalValue, domainName); // PSN should be created
		verify(domainService).listDomains(); // Domains should be reloaded afterwards (for counting)

		// Success message should be created, containing generated PSN
		FacesMessage message = facesContext.getMessageList().get(0);
		assertEquals(message.getSeverity(), FacesMessage.SEVERITY_INFO);
		assertTrue(message.getSummary().contains(new MessageFormat(bundleDe.getString("edit.message.info.pseudonymSaved")).format(args)));

		// Form values should be cleared
		assertNull(psnController.getPseudonym());
		assertNull(psnController.getOriginalValue());
	}

	@Test
	void onSavePseudonymForExistingOriginalValue()
	{
		// Arrange

		// Act

		// Assert
	}

	@Test
	void onSavePseudonymPair()
	{
		// Arrange

		// Act

		// Assert
	}

	@Test
	void onSavePseudonymPairForExistingOriginalValue()
	{
		// Arrange

		// Act

		// Assert
	}

	@Test
	void onSavePseudonymPairForExistingPseudonym()
	{
		// Arrange

		// Act

		// Assert
	}

	@Test
	void onPseudonymiseOriginalValue() throws UnknownDomainException, InvalidParameterException
	{
		// Arrange
		DomainOutDTO domain = mock(DomainOutDTO.class);
		PSNDTO selectedPseudonym = new PSNDTO("Domain 1", "Value", "Pseudonym");
		psnController.setSelectedRow(selectedPseudonym);
		psnController.setSelectedPseudonymFromDataTable();

		when(domain.getName()).thenReturn("Domain 2");
		when(domainService.listDomains()).thenReturn(Collections.singletonList(domain));
		when(domainService.getDomain(any())).thenReturn(domain);

		// Act
		psnController.onPseudonymiseOriginalValue();

		// Assert
		assertEquals(selectedPseudonym.getOriginalValue(), psnController.getOriginalValue());
		assertEquals(domain, psnController.getDomain());
		assertNull(psnController.getPseudonym());
	}

	@Test
	void onPseudonymisePseudonym() throws UnknownDomainException, InvalidParameterException
	{
		// Arrange
		DomainOutDTO domain = mock(DomainOutDTO.class);
		PSNDTO selectedPseudonym = new PSNDTO("Domain 1", "Value", "Pseudonym");
		psnController.setSelectedRow(selectedPseudonym);
		psnController.setSelectedPseudonymFromDataTable();

		when(domain.getName()).thenReturn("Domain 1.1");
		when(domain.getParentDomainNames()).thenReturn(Collections.singletonList("Domain 1"));
		when(domainService.listDomains()).thenReturn(Collections.singletonList(domain));
		when(domainService.getDomain(any())).thenReturn(domain);

		// Act
		psnController.onPseudonymisePseudonym();

		// Assert
		assertEquals(selectedPseudonym.getPseudonym(), psnController.getOriginalValue());
		assertEquals(domain, psnController.getDomain());
		assertNull(psnController.getPseudonym());
	}
}
