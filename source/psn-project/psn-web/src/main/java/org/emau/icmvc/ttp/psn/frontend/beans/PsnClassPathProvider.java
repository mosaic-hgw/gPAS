package org.emau.icmvc.ttp.psn.frontend.beans;

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



import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ResourceBundle;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import org.emau.icmvc.ganimed.ttp.psn.generator.Alphabet;
import org.emau.icmvc.ganimed.ttp.psn.generator.CheckDigits;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * retrieves class paths and names for generator and alphabet classes from class_paths bundle
 *
 * @author Weiher
 */
@ManagedBean(name = "ClassPathProvider")
@ViewScoped
public class PsnClassPathProvider
{
	private ResourceBundle alphabetBundle;
	private ResourceBundle generatorBundle;

	private final Logger logger = LoggerFactory.getLogger(PsnClassPathProvider.class);

	/**
	 * retrieves data from Recource Bundles
	 */
	@PostConstruct
	public void init()
	{
		FacesContext context = FacesContext.getCurrentInstance();
		alphabetBundle = context.getApplication().getResourceBundle(context, "alphabet_reference");
		generatorBundle = context.getApplication().getResourceBundle(context, "generator_reference");
		if (logger.isDebugEnabled())
		{
			logger.debug("initialising");
		}
	}

	/**
	 * retrieves generator/alphabet class name from full class path
	 *
	 * @param fullPath
	 * @return the class name if the path was found in the recource bundles, the full path if no
	 *         matching key was found
	 */
	public String convertClassPath(String fullPath)
	{
		String returnValue = fullPath;
		if (alphabetBundle.containsKey(fullPath))
		{
			returnValue = alphabetBundle.getString(fullPath);
		}
		if (generatorBundle.containsKey(fullPath))
		{
			returnValue = generatorBundle.getString(fullPath);
		}
		if (logger.isTraceEnabled())
		{
			logger.trace("Class name for " + fullPath + " is: " + returnValue);
		}
		return returnValue;
	}

	/**
	 * creates a map of all {@link Alphabet} classes -> their full class paths
	 *
	 * @return the alphabet map
	 */
	public Map<String, Object> getAlphabetMap()
	{
		Map<String, Object> map = new LinkedHashMap<>();
		for (String key : alphabetBundle.keySet())
		{
			map.put(alphabetBundle.getString(key), key);
		}
		return map;

	}

	/**
	 * creates a map of all {@link CheckDigits} classes -> their full class paths
	 *
	 * @return the checkDigits map
	 */
	public Map<String, Object> getGeneratorMap()
	{
		Map<String, Object> map = new LinkedHashMap<>();
		for (String key : generatorBundle.keySet())
		{
			map.put(generatorBundle.getString(key), key);
		}
		return map;

	}
}
