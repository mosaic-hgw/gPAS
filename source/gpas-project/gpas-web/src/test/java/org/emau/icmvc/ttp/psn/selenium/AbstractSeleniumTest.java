package org.emau.icmvc.ttp.psn.selenium;

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
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;

import static org.junit.jupiter.api.Assertions.assertNotNull;


/**
 * Abstract base class for selenium tests.
 *
 * <p>
 *     The class expects a local standalone selenium server with at least chrome capabilities.
 *     The most simple way to get one up is using docker compose e.g. with the following
 *     <code>docker-compose.yml</code>:
 * </p>
 *
 * <pre>
 *   version: '3'
 *
 *   services:
 *     selenium:
 *       image: selenium/standalone-chrome:latest
 *       privileged: true
 *       shm_size: 2g
 *       ports:
 *         - "4444:4444"
 * </pre>
 * 
 * Tests can be started with
 * <pre>
 *   mvn test -P selenium
 * </pre>
 * or in a more complex environment e.g. with
 * <pre>
 *   mvn test -P selenium -DargLine="-Dtest.selenium.remote.webdriver.host=selenium -Dtest.selenium.base.host=ci-wildfly"
 * </pre>
 *
 * <p>
 *     Check running tests and available capabilities by opening your browser at <a href="http://localhost:4444">localhost:4444</a>
 *     Point your WebDriver tests to <a href="http://localhost:4444/">localhost:4444</a>,
 *     check the <a href="https://www.selenium.dev/documentation/webdriver/drivers/#remote-webdriver">RemoteWebDriver section</a>
 *     to read on how to do so. Monitor the status of the server at <a href="http://localhost:4444/status">localhost:4444/status</a>.
 *     Or read more on how to configure the server and your tests in the
 *     <a href="https://www.selenium.dev/documentation/grid/getting_started">Selenium Grid - Getting Started Guide</a>.
 * </p>
 *
 */
public abstract class AbstractSeleniumTest
{
	private static final String SYSPROP_PREFIX = "test.selenium";
	public static final String SYSPROP_KEY_TEST_BASE_PORT = SYSPROP_PREFIX + ".base.port";
	public static final String SYSPROP_KEY_TEST_BASE_HOST = SYSPROP_PREFIX + ".base.host";
	public static final String SYSPROP_KEY_TEST_BASE_PROTOCOL = SYSPROP_PREFIX + ".base.protocol";
	public static final String SYSPROP_KEY_TEST_BASE_URL = SYSPROP_PREFIX + ".base.url";
	public static final String SYSPROP_KEY_REMOTE_WEBDRIVER_PORT = SYSPROP_PREFIX + ".remote.webdriver.port";
	public static final String SYSPROP_KEY_REMOTE_WEBDRIVER_HOST = SYSPROP_PREFIX + ".remote.webdriver.host";
	public static final String SYSPROP_KEY_REMOTE_WEBDRIVER_PROTOCOL = SYSPROP_PREFIX + ".remote.webdriver.protocol";
	public static final String SYSPROP_KEY_REMOTE_WEBDRIVER_URL = SYSPROP_PREFIX + ".remote.webdriver.url";
	protected WebDriver driver;
	protected Map<String, Object> vars;
	protected JavascriptExecutor js;

	@BeforeEach
	public void setUp() throws MalformedURLException
	{
		driver = new RemoteWebDriver(
				new URL(getRemoteWebDriverUrl()),
				new ChromeOptions());
		js = (JavascriptExecutor) driver;
		vars = new HashMap<>();
	}

	@AfterEach
	public void tearDown() {
		driver.quit();
	}

	public String getRemoteWebDriverUrl()
	{
		return System.getProperty(SYSPROP_KEY_REMOTE_WEBDRIVER_URL,
				System.getProperty(SYSPROP_KEY_REMOTE_WEBDRIVER_PROTOCOL, "http") + "://" +
						System.getProperty(SYSPROP_KEY_REMOTE_WEBDRIVER_HOST, "localhost") + ":" +
						System.getProperty(SYSPROP_KEY_REMOTE_WEBDRIVER_PORT, "4444"));
	}
	public String getTestBaseUrl()
	{
		return System.getProperty(SYSPROP_KEY_TEST_BASE_URL,
				System.getProperty(SYSPROP_KEY_TEST_BASE_PROTOCOL, "http") + "://" +
						System.getProperty(SYSPROP_KEY_TEST_BASE_HOST, "localhost") + ":" +
						System.getProperty(SYSPROP_KEY_TEST_BASE_PORT, "8080"));
	}
	public String getTestUrl(String query)
	{
		return getTestBaseUrl() + "/" + query;
	}

	public WebElement assertElementExists(By spec, boolean click)
	{
		WebElement element = driver.findElement(spec);
		assertNotNull(element);
		if (click)
		{
			element.click();
		}
		return element;
	}
}
