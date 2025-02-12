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

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import javax.xml.namespace.QName;

import jakarta.xml.ws.Service;
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
import org.emau.icmvc.ganimed.ttp.psn.dto.DomainOutDTO;
import org.emau.icmvc.ganimed.ttp.psn.dto.PSNDTO;
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
	private static final String PSN_SERVICE = "http://localhost:8080/gpas/gpasService?wsdl";
	private static final String DOMAIN_SERVICE = "http://localhost:8080/gpas/DomainService?wsdl";
	private static final String QNAME = "http://psn.ttp.ganimed.icmvc.emau.org/";

	private static final int CXF_STAX_MAX_CHILD_ELEMENTS = 11000000; // default 50000
	private static final int CXF_CLIENT_CONNECT_TIMEOUT = 30000; // default 30000L
	private static final int CXF_CLIENT_RECEIVE_TIMEOUT = 180000; // default 60000L

	public static final Logger LOGGER = LogManager.getLogger(GPASSoapClientDemo.class);

	public static final String DEFAULT_DOMAIN_NAME = "TestDomain";
	public static final int DEFAULT_START_VALUE = 100000000;
	public static final int DEFAULT_BATCH_COUNT = 1;
	public static final int DEFAULT_BATCH_SIZE = 49999;

	public static void main(String[] args)
	{
		final Map<String, String> params = new HashMap<>();
		Arrays.stream(args).map(arg -> arg.split("=")).forEach(param -> {
			if (param.length == 2)
			{
				params.put(param[0].trim().replace('_', '-'), param[1].trim());
			}
			else
			{
				throw new RuntimeException("Invalid argument: expecting parameters as 'name=value'");
			}
		});

		final String domain = params.getOrDefault("domain", DEFAULT_DOMAIN_NAME);
		final boolean empty = Boolean.parseBoolean(params.getOrDefault("empty-domain", Boolean.FALSE.toString()));
		final int startValue = Integer.parseInt(params.getOrDefault("start-value", "" + DEFAULT_START_VALUE));
		final boolean incrementStartValue = Boolean.parseBoolean(params.getOrDefault("increment-start-value",  Boolean.FALSE.toString()));
		final int batchCount = Integer.parseInt(params.getOrDefault("batch-count", "" + DEFAULT_BATCH_COUNT));
		final int batchSize = Integer.parseInt(params.getOrDefault("batch-size", "" + DEFAULT_BATCH_SIZE));
		final boolean multiPsnDomain = Boolean.parseBoolean(params.getOrDefault("multi-psn-domain", Boolean.FALSE.toString()));

		try
		{
			timing(domain, empty, batchCount, batchSize, startValue, incrementStartValue, multiPsnDomain);
		}
		catch (InvalidParameterException | DomainInUseException e)
		{
			throw new RuntimeException(e);
		}
	}

	public static void timing(String domain, boolean empty, int batchCount, int batchSize, int startValue, boolean incrementStartValuePerBatch, boolean multiPsnDomain)
			throws InvalidParameterException, DomainInUseException
	{
		LOGGER.info("Start gPAS timing (domain={}, empty={}, batchCount={}, batchSize={}, startValue={}, incrementStartValuePerBatch={}, mpsn={})...",
				domain, empty, batchCount, batchSize, startValue, incrementStartValuePerBatch, multiPsnDomain);

		final GPASSoapClientDemo demo = new GPASSoapClientDemo(domain, empty, multiPsnDomain,
				CXF_STAX_MAX_CHILD_ELEMENTS, CXF_CLIENT_CONNECT_TIMEOUT, CXF_CLIENT_RECEIVE_TIMEOUT);

		long timeMillis = System.currentTimeMillis();

		timedCall("counted",
				() -> (int) demo.countPSNs());
		timedRun("requested single",
				() -> demo.requestPSN("wert_xyz"));
		timedRun("requested set of 2",
				() -> demo.requestPSNs(Set.of("wert_123", "wert_456")));
		timedRun("requested " + batchCount + " batche(s) of " + batchSize,
				() -> demo.requestPSNBatches(startValue, batchSize, batchCount, incrementStartValuePerBatch));
		timedCall("counted",
				() -> (int) demo.countPSNs());
		timedCall("listed a page of",
				() -> demo.listPSNsPaginated(50000).size());
		timedCall("listed a page of",
				() -> demo.listPSNsPaginated(1000000).size());
		timedCall("listed all",
				() -> demo.listAllPSNs().size());

		LOGGER.info("End of gPAS timing - took {} ms.", System.currentTimeMillis() - timeMillis);
	}

	private static void timedRun(String msg, Runnable c)
	{
		long timeMillis = System.currentTimeMillis();
		c.run();
		LOGGER.info("{} pseudonym(s) in {} ms.", msg, System.currentTimeMillis() - timeMillis);
	}

	private static void timedCall(String msg, Callable<Integer> c)
	{
		try
		{
			long timeMillis = System.currentTimeMillis();
			LOGGER.info("{} {} pseudonym(s) in {} ms.", msg, c.call(), System.currentTimeMillis() - timeMillis);
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}

	private final DomainManager domainManager;
	private final PSNManager psnManager;
	private final String domainName;
	private final int cxfClientConnectTimeout; // default 30000L
	private final int cxfClientReceiveTimeout; // default 60000L

	public GPASSoapClientDemo(String domainName, boolean emptyDomain, boolean multiPsnDomain,
			int cxfStaxMaxChildElements, int cxfClientConnectTimeout, int cxfClientReceiveTimeout)
			throws InvalidParameterException, DomainInUseException
	{
		System.setProperty("org.apache.cxf.stax.maxChildElements", "" + cxfStaxMaxChildElements);
		this.domainName = domainName;
		this.cxfClientConnectTimeout = cxfClientConnectTimeout;
		this.cxfClientReceiveTimeout = cxfClientReceiveTimeout;
		psnManager = createService("PSNManagerBeanService", PSN_SERVICE, PSNManager.class);
		domainManager = createService("DomainManagerBeanService", DOMAIN_SERVICE, DomainManager.class);
		ensureDomainExists(domainManager, emptyDomain, multiPsnDomain);
	}

	private <S> S createService(String serviceName, String wsdl, Class<S> serviceClass)
	{
		QName serviceQName = new QName(QNAME, serviceName);
		try
		{
			URL wsdlURL = new URI(wsdl).toURL();
			Service service = Service.create(wsdlURL, serviceQName);
			S webService = service.getPort(serviceClass);
			fixHttpClientTimeouts(webService);
			LOGGER.info("Created {}", serviceName);
			return webService;
		}
		catch (MalformedURLException | URISyntaxException e)
		{
			throw new RuntimeException(e);
		}
	}

	private void ensureDomainExists(DomainManager domainManager, boolean emptyDomain, boolean multiPsnDomain) throws InvalidParameterException, DomainInUseException
	{
		try
		{
			DomainOutDTO domain = domainManager.getDomain(domainName);
			LOGGER.info("domain {} already exists", domainName);

			if (emptyDomain)
			{
				domainManager.deleteDomainWithPSNs(domainName);
				LOGGER.info("domain {} deleted", domainName);
				domainManager.getDomain(domainName); // will throw an UnknownDomainException
			}
			// domain exist
			if (multiPsnDomain && !domain.getConfig().isMultiPsnDomain())
			{
				throw new InvalidParameterException("Existing test domain is not (as requested) a multi-psn-domain");
			}
		}
		catch (UnknownDomainException maybe)
		{
			DomainConfig domainConfig = new DomainConfig();
			domainConfig.setMultiPsnDomain(multiPsnDomain);
			DomainInDTO domainDTO = new DomainInDTO(domainName, domainName + " label",
					"org.emau.icmvc.ganimed.ttp.psn.generator.Verhoeff", Numbers.class.getName(),
					domainConfig, "eine Testdomain", null);
			try
			{
				domainManager.addDomain(domainDTO);
				LOGGER.info("domain {} created", domainName);
			}
			catch (InvalidCheckDigitClassException | InvalidAlphabetException | InvalidGeneratorException | InvalidParentDomainException | UnknownDomainException e)
			{
				throw new RuntimeException(e); // should never happen
			}
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

	public void requestPSNBatches(int startValue, int batchSize, int batchCount, boolean incrementStartValuePerBatch)
	{
		for (int i = 0; i < batchCount; i++)
		{
			requestPSNs(startValue, batchSize);
			LOGGER.info("added {} PSNs up to {}", batchSize, toOriginalValue(startValue + batchSize - 1));

			if (incrementStartValuePerBatch)
			{
				startValue += batchSize;
			}
		}
	}

	public String requestPSN(String value)
	{
		try
		{
			String psn = psnManager.getOrCreatePseudonymFor(value, domainName);
			LOGGER.debug("psn for {} is {}", value, psn);
			return psn;
		}
		catch (InvalidParameterException e)
		{
			throw new IllegalArgumentException(e);
		}
		catch (UnknownDomainException | DomainIsFullException e)
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
		catch (UnknownDomainException | DomainIsFullException e)
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
