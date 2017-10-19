package org.emau.icmvc.ttp.psn.frontend.validators;

/*
 * ###license-information-start###
 * gPAS - a Generic Pseudonym Administration Service
 * __
 * Copyright (C) 2013 - 2017 The MOSAIC Project - Institut fuer Community Medicine der
 * 							Universitaetsmedizin Greifswald - mosaic-projekt@uni-greifswald.de
 * 							concept and implementation
 * 							l. geidel
 * 							web client
 * 							g. weiher
 * 							a. blumentritt
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

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * validator for custom alphabet input checks for duplicate chars and pattern
 * 
 * @author Weiher
 * 
 */
@ManagedBean(name = "alphabetValidator")
@RequestScoped
public class AlphabetValidator implements Validator {
	private final String pattern = "(.,)*.";

	private final Logger logger = LoggerFactory.getLogger(AlphabetValidator.class);

	/**
	 * validation fails when custom is passed as an attribute (alphabet_type) and the value is empty, has duplicates in it or doesn't match 'pattern'
	 */
	@Override
	public void validate(FacesContext arg0, UIComponent component, Object value) throws ValidatorException {

		if (logger.isDebugEnabled()) {
			logger.debug("validating custom Alphabet " + value);
		}
		if (value == null) {
			if (logger.isDebugEnabled()) {
				logger.debug("validation failed: custom Alphabet is null");
			}
			throw new ValidatorException(new FacesMessage(FacesMessage.SEVERITY_ERROR, "validation error: customAlphabet is required",
					"validation error: customAlphabet is required"));
		}
		String customAlphabetString = (String) value;
		// test if the String is in the right format
		if (!(customAlphabetString.matches(pattern))) {
			if (logger.isDebugEnabled()) {
				logger.debug("validation failed: alphabet does not match valid pattern");
			}
			throw new ValidatorException(new FacesMessage(FacesMessage.SEVERITY_ERROR, "validation error: '" + (String) value
					+ "' is no valid Alphabet String", "validation error: '" + (String) value + "' is no valid Alphabet String"));
		}
		// test for duplicate chars
		for (int index = 0, loop = 1; loop < customAlphabetString.length(); loop++) {
			if (customAlphabetString.charAt(index) != ',' && customAlphabetString.charAt(index) == customAlphabetString.charAt(loop))
				throw new ValidatorException(new FacesMessage(FacesMessage.SEVERITY_ERROR, "validation error: duplicate char '"
						+ customAlphabetString.charAt(index) + "'", "validation error: duplicate char '" + customAlphabetString.charAt(index) + "'"));
			if (loop == customAlphabetString.length() - 1)
				loop = ++index;
		}
	}
}
