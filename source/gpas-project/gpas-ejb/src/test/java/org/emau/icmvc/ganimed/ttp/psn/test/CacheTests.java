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
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.emau.icmvc.ganimed.ttp.psn.alphabets.Numbers;
import org.emau.icmvc.ganimed.ttp.psn.config.DomainConfig;
import org.emau.icmvc.ganimed.ttp.psn.dto.DomainInDTO;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.DomainInUseException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.DomainIsFullException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.InvalidAlphabetException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.InvalidCheckDigitClassException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.InvalidGeneratorException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.InvalidParameterException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.InvalidParentDomainException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.PSNErrorStrings;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.UnknownDomainException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.UnknownValueException;
import org.emau.icmvc.ganimed.ttp.psn.generator.Verhoeff;
import org.emau.icmvc.ganimed.ttp.psn.internal.Cache;
import org.emau.icmvc.ganimed.ttp.psn.internal.DAO;
import org.emau.icmvc.ganimed.ttp.psn.internal.PSNCacheObject;
import org.emau.icmvc.ganimed.ttp.psn.model.Domain;
import org.emau.icmvc.ganimed.ttp.psn.model.PSN;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

public class CacheTests
{
	private AutoCloseable closeable;

	@Mock
	private DAO dao;

	@InjectMocks
	private Cache cache;

	private List<PSN> daoPsns;

	private static final Logger logger = LogManager.getLogger(CacheTests.class);
	private static final int size = 1_000_000;

	@BeforeEach
	protected void setUp() throws UnknownValueException
	{
		closeable = openMocks(this);
		daoPsns = new ArrayList<>();

		// getPSNObjects
		doAnswer(invocation -> {
			Object[] args = invocation.getArguments();
			String value = (String) args[0];
			Domain domain = (Domain) args[1];
			List<PSN> result = daoPsns.stream().filter(p -> p.getOriginalValue().equals(value) && p.getDomain().equals(domain)).toList();
			if (result.isEmpty())
			{
				String message = "value " + value + " for domain " + domain.getName() + " not found";
				throw new UnknownValueException(message);
			}
			return result;
		}).when(dao).getPSNObjects(any(), any(Domain.class));

		// addPSN
		doAnswer(invocation -> {
			Object[] args = invocation.getArguments();
			PSN psn = (PSN) args[0];
			daoPsns.add(psn);
			return null;
		}).when(dao).addPSN(any(PSN.class));
	}

	@AfterEach
	void teardown() throws Exception
	{
		closeable.close();
	}

	@Nested
	@DisplayName("getOrCreatePseudonymsFor")
	class GetOrCreatePseudonymsForTests
	{
		@Test
		void onCreatePseudonym()
				throws InvalidParameterException, UnknownDomainException, DomainInUseException, InvalidGeneratorException, InvalidAlphabetException,
				InvalidParentDomainException, InvalidCheckDigitClassException, DomainIsFullException
		{
			// Arrange
			addDomain(createDomain());
			String value = "value1";

			// Act
			cache.getOrCreatePseudonymsFor(value, "domain", 1);

			// Assert
			assertEquals(1, daoPsns.size());
			assertEquals(value, daoPsns.getFirst().getOriginalValue());
		}
		
		@Test
		void onCreatePseudonymForErrorString()
				throws InvalidParameterException, UnknownDomainException, DomainInUseException, InvalidGeneratorException, InvalidAlphabetException,
				InvalidParentDomainException, InvalidCheckDigitClassException
		{
			// Arrange
			addDomain(createDomain());

			// Act
			assertThrows(InvalidParameterException.class, () -> cache.getOrCreatePseudonymsFor(PSNErrorStrings.INVALID_VALUE, "domain", 1));

			// Assert
			assertEquals(0, daoPsns.size());
		}

		@Test
		void onCreatePseudonymThrowsRuntimeException()
				throws InvalidParameterException, UnknownDomainException, DomainInUseException, InvalidGeneratorException, InvalidAlphabetException,
				InvalidParentDomainException, InvalidCheckDigitClassException, UnknownValueException
		{
			// Arrange
			addDomain(createDomain());
			when(dao.getPSNObjects(any(), any(Domain.class))).thenThrow(RuntimeException.class);

			// Act
			assertThrows(RuntimeException.class, () -> cache.getOrCreatePseudonymsFor("value", "domain", 1));

			// Assert
			assertEquals(0, daoPsns.size());
		}

		@Test
		void onCreatePseudonymInNewThreadAfterException()
				throws InvalidParameterException, UnknownDomainException, DomainInUseException, InvalidGeneratorException, InvalidAlphabetException,
				InvalidParentDomainException, InvalidCheckDigitClassException, InterruptedException
		{
			// Arrange
			addDomain(createDomain());

			// Act
			assertThrows(InvalidParameterException.class, () -> cache.getOrCreatePseudonymsFor(PSNErrorStrings.INVALID_VALUE, "domain", 1));

			Thread t1 = new Thread(() -> {
				try
				{
					cache.getOrCreatePseudonymsFor("value1", "domain", 1);
				}
				catch (DomainIsFullException | InvalidParameterException | UnknownDomainException e)
				{
					throw new RuntimeException(e);
				}
			});
			t1.start();
			t1.join(1000);

			// Assert
			assertEquals(1, daoPsns.size());
		}
	}

	private DomainInDTO createDomain()
	{
		DomainConfig domainConfig = new DomainConfig();
		return new DomainInDTO("domain", Verhoeff.class, Numbers.class, null, domainConfig, null, null);
	}

	private void addDomain(DomainInDTO domain)
			throws InvalidParameterException, DomainInUseException, InvalidGeneratorException, InvalidAlphabetException, UnknownDomainException, InvalidParentDomainException,
			InvalidCheckDigitClassException
	{
		if (cache.listDomains().stream().filter(d -> d.getName().equals(domain.getName())).findAny().isEmpty())
		{
			cache.addDomain(domain);
		}
	}

	@Nested
	@DisplayName("cache basic")
	class BasicCacheTests
	{
		// old stuff
		@Test
		void testPos()
		{
			logger.info("start cache test");
			PSNCacheObject cacheObject = new PSNCacheObject(size);
			for (int i = 0; i < size; i++)
			{
				int pos = i;
				Assertions.assertFalse(cacheObject.isPosSet(pos), "empty cache has value set, pos=" + pos);
				cacheObject.setPos(pos);
				Assertions.assertTrue(cacheObject.isPosSet(pos), "cache hasn't set value, pos=" + pos);
			}

			for (int i = size - 1; i >= 0; i--)
			{
				int pos = i;
				Assertions.assertTrue(cacheObject.isPosSet(pos), "full cache has value not set, pos=" + pos);
				cacheObject.unsetPos(pos);
				Assertions.assertFalse(cacheObject.isPosSet(pos), "cache hasn't removed value, pos=" + pos);
			}
			logger.info("cache test ended");
		}
	}
}
