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

import java.util.List;

import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

import org.emau.icmvc.ganimed.ttp.psn.DomainManager;
import org.emau.icmvc.ganimed.ttp.psn.dto.DomainDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * validates a domain String for creation of new domains checks if the domain already exists
 * 
 * @author Weiher
 * 
 */
@ManagedBean(name = "domainValidator")
@RequestScoped
public class DomainValidiator implements Validator {
	@EJB(lookup = "java:global/gpas/psn-ejb/DomainManagerBean!org.emau.icmvc.ganimed.ttp.psn.DomainManager")
	private DomainManager manager;

	private final Logger logger = LoggerFactory.getLogger(DomainValidiator.class);

	@Override
	public void validate(FacesContext arg0, UIComponent arg1, Object value) throws ValidatorException {
		if (logger.isDebugEnabled()) {
			logger.debug("validating domain");
		}
		List<DomainDTO> domains = manager.listDomains();
		if (value == null || ((String) value).isEmpty()) {
			if (logger.isDebugEnabled()) {
				logger.debug("validation failed: domain is required");
			}
			throw new ValidatorException(new FacesMessage(FacesMessage.SEVERITY_ERROR, "validation error: domain is a required field",
					"validation error: domain is a required field"));
		}
		for (DomainDTO existingDomain : domains) {
			if (existingDomain.getDomain().equals(value)) {
				if (logger.isDebugEnabled()) {
					logger.debug("validation failed: domain already exists");
				}
				throw new ValidatorException(new FacesMessage(FacesMessage.SEVERITY_ERROR, "validation error: domain already exists",
						"validation error: domain already exists"));
			}
		}
	}

	public DomainManager getManager() {
		return manager;
	}

	public void setManager(DomainManager manager) {
		this.manager = manager;
	}

}
