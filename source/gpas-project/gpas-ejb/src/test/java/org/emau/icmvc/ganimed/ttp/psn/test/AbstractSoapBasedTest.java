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

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import javax.xml.namespace.QName;

import jakarta.xml.ws.Service;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.emau.icmvc.ganimed.ttp.psn.DomainManager;
import org.emau.icmvc.ganimed.ttp.psn.PSNManager;
import org.emau.icmvc.ganimed.ttp.psn.dto.DomainOutDTO;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.DomainInUseException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.UnknownDomainException;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

public abstract class AbstractSoapBasedTest
{
	public static final String DOMAIN_MANAGER_WSDL_URL = "http://localhost:8080/gpas/DomainService?wsdl";
	public static final String PSN_MANAGER_WSDL_URL = "http://localhost:8080/gpas/gpasService?wsdl";
	public static final String UNITTEST_MARKER_COMMENT = "unittest-4165016973913426982";
	private static final Logger LOGGER = LogManager.getLogger(AbstractSoapBasedTest.class);
	private static PSNManager PSN_MANAGER;
	private static DomainManager DOMAIN_MANAGER;

	public static boolean isServiceAvailable(String wsdlURL)
	{
		boolean success = false;
		try
		{
			URL url = new URI(wsdlURL).toURL();
			HttpURLConnection huc = (HttpURLConnection) url.openConnection();

			success = huc.getResponseCode() == HttpURLConnection.HTTP_OK;
		}
		catch (IOException | URISyntaxException e)
		{
			// intentionally empty
		}
		if (!success)
		{
			LOGGER.info("Service not available at {} - skipping manual test cases", wsdlURL);
		}
		return success;
	}

	public static <T> T setupService(Class<T> serviceClass, String wsdlUrl, String localPart) throws Exception
	{
		QName serviceName = new QName("http://psn.ttp.ganimed.icmvc.emau.org/", localPart);
		Service service = Service.create(new URI(wsdlUrl).toURL(), serviceName);
		assertNotNull(service, "service object for is null");
		T port = service.getPort(serviceClass);
		assertNotNull(port, "service object is null");
		return port;
	}

	protected final Logger logger = LogManager.getLogger(getClass());
	protected PSNManager psnManager;
	protected DomainManager domainManager;

	protected void deleteTestDomains() throws Exception
	{
		initDomainManager();
		for (DomainOutDTO domainDTO : domainManager.listDomains())
		{
			if (UNITTEST_MARKER_COMMENT.equals(domainDTO.getComment()))
			{
				deleteWithChildren(domainDTO);
			}
		}
	}

	protected void deleteWithChildren(DomainOutDTO parent) throws Exception
	{
		initDomainManager();
		for (String child : parent.getChildDomainNames())
		{
			try
			{
				deleteWithChildren(domainManager.getDomain(child));
			}
			catch (UnknownDomainException ignore)
			{
				// ignored
			}
		}
		try
		{
			domainManager.deleteDomainWithPSNs(parent.getName());
		}
		catch (UnknownDomainException ignore)
		{
			// ignored
		}
		catch (DomainInUseException e)
		{
			fail("couldn't delete a test domain", e);
		}
	}

	protected DomainManager initDomainManager() throws Exception
	{
		if (domainManager == null)
		{
			if (DOMAIN_MANAGER == null)
			{
				DOMAIN_MANAGER = setupService(DomainManager.class, DOMAIN_MANAGER_WSDL_URL, "DomainManagerBeanService");
			}
			domainManager = DOMAIN_MANAGER;
		}
		return domainManager;
	}

	protected PSNManager initPSNManager() throws Exception
	{
		if (psnManager == null)
		{
			if (PSN_MANAGER == null)
			{
				PSN_MANAGER = setupService(PSNManager.class, PSN_MANAGER_WSDL_URL, "PSNManagerBeanService");
			}
			psnManager = PSN_MANAGER;
		}
		return psnManager;
	}
}
