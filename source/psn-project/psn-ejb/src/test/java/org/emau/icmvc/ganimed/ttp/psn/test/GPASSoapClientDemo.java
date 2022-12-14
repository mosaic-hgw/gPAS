package org.emau.icmvc.ganimed.ttp.psn.test;

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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.emau.icmvc.ganimed.ttp.psn.DomainManager;
import org.emau.icmvc.ganimed.ttp.psn.PSNManager;
import org.emau.icmvc.ganimed.ttp.psn.alphabets.Numbers;
import org.emau.icmvc.ganimed.ttp.psn.config.DomainConfig;
import org.emau.icmvc.ganimed.ttp.psn.config.PaginationConfig;
import org.emau.icmvc.ganimed.ttp.psn.dto.DomainInDTO;
import org.emau.icmvc.ganimed.ttp.psn.dto.PSNDTO;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.DBException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.DomainInUseException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.DomainIsFullException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.InvalidAlphabetException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.InvalidCheckDigitClassException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.InvalidGeneratorException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.InvalidParameterException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.InvalidParentDomainException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.UnknownDomainException;

public class GPASSoapClientDemo
{
	private static final String DOMAIN_NAME = "TestDomain";
	private static final String GPAS_URL = "http://localhost:8080/gpas/gpasService?wsdl";
	private static final String DOMAIN_URL = "http://localhost:8080/gpas/DomainService?wsdl";
	private static final String QNAME_URL = "http://psn.ttp.ganimed.icmvc.emau.org/";
	private static final Logger logger = LogManager.getLogger(GPASSoapClientDemo.class);
	private static final int CXF_STAX_MAX_CHILD_ELEMENTS = 11000000; // default 50000
	private static final int CXF_CLIENT_CONNECT_TIMEOUT = 30000; // default 30000L
	private static final int CXF_CLIENT_RECEIVE_TIMEOUT = 180000; // default 60000L


	public static void main(String[] args)
	{
		try
		{
			logger.info("Start gPAS demo...");

			final GPASSoapClientDemo demo = new GPASSoapClientDemo(DOMAIN_NAME,
					CXF_STAX_MAX_CHILD_ELEMENTS, CXF_CLIENT_CONNECT_TIMEOUT, CXF_CLIENT_RECEIVE_TIMEOUT);

			final int startValue = 10000000;
			final int batchCount = 1;
			final int batchSize = 100000;

			timedCall("counted",
					() -> (int) demo.countPSNs());
			timedRun("requested single",
					() -> demo.requestPSN("wert_xyz"));
			timedRun("requested set of 2",
					() -> demo.requestPSNs(Set.of("wert_123", "wert_456")));
			timedRun("requested " + batchCount + " batche(s) of " + batchSize,
					() -> demo.requestPSNBatches(startValue, batchSize, batchCount));
			timedCall("counted",
					() -> (int) demo.countPSNs());
			timedCall("listed a page of",
					() -> demo.listPSNsPaginated(50000).size());
			timedCall("listed a page of",
					() -> demo.listPSNsPaginated(1000000).size());
			timedCall("listed all",
					() -> demo.listAllPSNs().size());

			logger.info("End of gPAS demo.");
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
		}
	}

	private static void timedRun(String msg, Runnable c)
	{
		long timeMillis = System.currentTimeMillis();
		c.run();
		logger.info(msg + " pseudonym(s) in " + (System.currentTimeMillis() - timeMillis) + " ms.");
	}

	private static void timedCall(String msg, Callable<Integer> c) throws Exception
	{
		long timeMillis = System.currentTimeMillis();
		logger.info(msg + " " + c.call() + " pseudonym(s) in " + (System.currentTimeMillis() - timeMillis) + " ms.");
	}

	private final DomainManager domainManager;
	private final PSNManager psnManager;
	private final String domainName;
	private final int cxfClientConnectTimeout; // default 30000L
	private final int cxfClientReceiveTimeout; // default 60000L

	public GPASSoapClientDemo(String domainName,
			int cxfStaxMaxChildElements, int cxfClientConnectTimeout, int cxfClientReceiveTimeout) throws Exception
	{
		System.setProperty("org.apache.cxf.stax.maxChildElements", "" + cxfStaxMaxChildElements);
		this.domainName = domainName;
		this.cxfClientConnectTimeout = cxfClientConnectTimeout;
		this.cxfClientReceiveTimeout = cxfClientReceiveTimeout;
		psnManager = createPSNManager();
		domainManager = createDomainManager();
		ensureDomainExists(domainManager);
	}

	private PSNManager createPSNManager() throws MalformedURLException
	{
		QName serviceName = new QName(QNAME_URL, "PSNManagerBeanService");
		URL wsdlURL = new URL(GPAS_URL);
		Service service = Service.create(wsdlURL, serviceName);
		PSNManager psnManager = service.getPort(PSNManager.class);
		fixHttpClientTimeouts(psnManager);
		logger.info("gpas web service manager created");
		return psnManager;
	}

	private DomainManager createDomainManager() throws MalformedURLException
	{
		QName serviceName = new QName(QNAME_URL, "DomainManagerBeanService");
		URL wsdlURL = new URL(DOMAIN_URL);
		Service service = Service.create(wsdlURL, serviceName);
		DomainManager domainManager = service.getPort(DomainManager.class);
		fixHttpClientTimeouts(domainManager);
		logger.info("domain web service manager created");
		return domainManager;
	}

	private void ensureDomainExists(DomainManager domainManager) throws
			InvalidParameterException, DomainInUseException, InvalidAlphabetException, InvalidCheckDigitClassException,
			InvalidGeneratorException, InvalidParentDomainException, UnknownDomainException
	{
		try
		{
			domainManager.getDomain(domainName);
			logger.info("domain " + domainName + " already exists");
		}
		catch (UnknownDomainException maybe)
		{
			DomainInDTO domainDTO = new DomainInDTO(domainName, domainName + " label",
					"org.emau.icmvc.ganimed.ttp.psn.generator.Verhoeff", Numbers.class.getName(),
					new DomainConfig(), "eine Testdomain", null);
			domainManager.addDomain(domainDTO);
			logger.info("domain " + domainName + " created");
		}
	}

	public long countPSNs()
	{
		try
		{
			return domainManager.countPSNs(domainName, new PaginationConfig());
		}
		catch (InvalidParameterException | UnknownDomainException e)
		{
			throw new IllegalStateException(e);
		}
	}

	private void fixHttpClientTimeouts(Object port)
	{
		HTTPConduit httpConduit = (HTTPConduit) ClientProxy.getClient(port).getConduit();
		HTTPClientPolicy httpClientPolicy = httpConduit.getClient();
		if (httpClientPolicy == null)
		{
			httpClientPolicy = new HTTPClientPolicy();
			httpConduit.setClient(httpClientPolicy);
		}
		httpClientPolicy.setConnectionTimeout(cxfClientConnectTimeout); // default 30000L
		httpClientPolicy.setReceiveTimeout(cxfClientReceiveTimeout); // default 60000L
	}

	public void requestPSNBatches(int startValue, int batchSize, int batchCount)
	{
		for (int i = 0; i < batchCount; i++)
		{
			requestPSNs(startValue + i * batchSize, batchSize);
			logger.debug("added " + batchSize + " PSNs up to " + toOriginalValue(startValue + (i + 1) * batchSize - 1));
		}
	}

	public String requestPSN(String value)
	{
		try
		{
			String psn = psnManager.getOrCreatePseudonymFor(value, domainName);
			logger.debug("psn for " + value + " is " + psn);
			return psn;
		}
		catch (InvalidParameterException e)
		{
			throw new IllegalArgumentException(e);
		}
		catch (UnknownDomainException | DBException | DomainIsFullException e)
		{
			throw new IllegalStateException(e);
		}
	}


	public Map<String, String> requestPSNs(Set<String> values)
	{
		try
		{
			return psnManager.getOrCreatePseudonymForList(values, domainName);
		}
		catch (InvalidParameterException e)
		{
			throw new IllegalArgumentException(e);
		}
		catch (UnknownDomainException | DBException | DomainIsFullException e)
		{
			throw new IllegalStateException(e);
		}
	}

	public void requestPSNs(int start, int count)
	{
		Set<String> values = new HashSet<>();
		for (int i = 0; i < count; i++)
		{
			values.add(toOriginalValue(i + start));
		}
		requestPSNs(values);
	}

	public List<PSNDTO> listPSNsPaginated(int pageSize)
	{
		try
		{
			return domainManager.listPSNsPaginated(domainName, new PaginationConfig(1, pageSize));
		}
		catch (InvalidParameterException | UnknownDomainException e)
		{
			throw new IllegalStateException(e);
		}
	}

	public List<PSNDTO> listAllPSNs()
	{
		try
		{
			return domainManager.listPSNs(domainName);
		}
		catch (InvalidParameterException | UnknownDomainException e)
		{
			throw new IllegalStateException(e);
		}
	}

	private static String toOriginalValue(int i)
	{
		return "wert_" + String.format("%09d", i);
	}
}
