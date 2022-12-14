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

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.faces.application.FacesMessage;

import org.emau.icmvc.ganimed.ttp.psn.dto.DomainOutDTO;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.InvalidParameterException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.PSNErrorStrings;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.UnknownDomainException;
import org.emau.icmvc.ttp.psn.frontend.controller.testtools.gPASWebTest;
import org.icmvc.ttp.web.model.WebFile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.file.UploadedFile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class BatchControllerTest extends gPASWebTest
{
	WebFile webFile;
	DomainOutDTO selectedDomain;
	UploadedFile uploadFile;
	String fileContent = "";
	FileUploadEvent event;

	@InjectMocks
	private BatchController batchController;

	@BeforeEach
	void setUpBatchControllerTest()
	{
		batchController.init();

		webFile = new WebFile("gPAS");
		batchController.setWebFile(webFile);

		selectedDomain = mock(DomainOutDTO.class);
		when(selectedDomain.getName()).thenReturn("Domain");
		batchController.setSelectedDomain(selectedDomain.getName());
	}
	
	@Test
	void onUpload()
	{
		// Arrange
		fileContent = "test";
		uploadFile = mock(UploadedFile.class);
		when(uploadFile.getContent()).thenReturn(fileContent.getBytes());

		event = mock(FileUploadEvent.class);
		when(event.getFile()).thenReturn(uploadFile);

		// Act
		webFile.onUpload(event);

		// Assert
		assertEquals(1, batchController.getWebFile().getElements().size());
		assertEquals(fileContent, batchController.getWebFile().getElements().get(0).get(0));
	}

	@Test
	void onPseudonymiseInvalidValues() throws UnknownDomainException, InvalidParameterException
	{
		// Arrange
		String invalidValue = "123";
		webFile.getElements().add(Collections.singletonList(invalidValue));
		webFile.getColumns().add("Column 1");

		batchController.setSelectedAction(BatchController.Action.PSEUDONYMISE);
		batchController.setGenerateNewPseudonyms(false);

		Map<String, String> result = new LinkedHashMap<>();
		result.put(invalidValue, PSNErrorStrings.INVALID_VALUE);
		when(service.getPseudonymForList(any(), any())).thenReturn(result);

		// Act
		assertFalse(webFile.isProcessed());
		batchController.onDoAction();

		// Assert
		assertTrue(webFile.isProcessed()); // File should be processed
		verify(service).getPseudonymForList(any(), any()); // PSN List should be gathered
		assertEquals(invalidValue, webFile.getElements().get(0).get(0)); // Value should be in result list
		assertEquals(PSNErrorStrings.INVALID_VALUE, webFile.getElements().get(0).get(1)); // Value should be marked as invalid 

		FacesMessage message0 = facesContext.getMessageList().get(0); // Info processed message
		assertEquals(FacesMessage.SEVERITY_INFO, message0.getSeverity());

		FacesMessage message1 = facesContext.getMessageList().get(1); // Warning invalid values message
		assertEquals(FacesMessage.SEVERITY_WARN, message1.getSeverity());
		assertTrue(message1.getSummary().contains(bundleDe.getString("batch.message.warn.invalid." + batchController.getSelectedAction().name())));
	}
}
