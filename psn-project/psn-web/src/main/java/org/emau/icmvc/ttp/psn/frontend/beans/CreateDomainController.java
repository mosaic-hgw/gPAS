package org.emau.icmvc.ttp.psn.frontend.beans;

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

import java.text.MessageFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import org.emau.icmvc.ganimed.ttp.psn.DomainManager;
import org.emau.icmvc.ganimed.ttp.psn.dto.DomainDTO;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.DomainInUseException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.InvalidAlphabetException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.InvalidCheckDigitClassException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.InvalidDomainNameException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.InvalidGeneratorException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.UnknownDomainException;
import org.emau.icmvc.ttp.psn.frontend.datamodel.Property;
import org.primefaces.component.selectonemenu.SelectOneMenu;
import org.primefaces.context.RequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * backingbean for creating new domains/psn_projects; handles validation (new_domain_panel.xhtml)
 * 
 * @author Weiher
 * 
 */
@ManagedBean(name = "CreateDomainController")
@ViewScoped
public class CreateDomainController {

	@ManagedProperty(value = "#{ClassPathProvider}")
	private PsnClassPathProvider provider;

	private ResourceBundle messages;

	private final Logger logger = LoggerFactory.getLogger(CreateDomainController.class);

	@EJB(lookup = "java:global/gpas/psn-ejb/DomainManagerBean!org.emau.icmvc.ganimed.ttp.psn.DomainManager")
	private DomainManager manager;

	@ManagedProperty(value = "#{DomainController}")
	private DomainController domainController;

	/**
	 * list of all domain properties and their assigned values *
	 */
	private List<Property> properties;

	private String domain;

	private String checkDigitClass;

	private String alphabet;

	private String comment;

	private String customAlphabet;

	private String parentDomain;

	@PostConstruct
	public void init() {
		messages = ResourceBundle.getBundle("messages");
	}

	// -------------dataObject generation-------------------------------
	/**
	 * prepares the alphabetmap provided by {@link PsnClassPathProvider} for dropdown menu
	 * 
	 * @return data for dropdown menu ({@link SelectOneMenu}) of all alphabets + custom alphabet entry
	 */
	public Map<String, Object> generateAlphabetMap() {
		Map<String, Object> map = provider.getAlphabetMap();
		return map;
	}

	/**
	 * prepares the generatortmap provided by {@link PsnClassPathProvider} for dropdown menu
	 * 
	 * @return data for dropdown menu ({@link SelectOneMenu}) of all generators
	 */
	public Map<String, Object> generateGeneratorMap() {
		return provider.getGeneratorMap();

	}

	/**
	 * generates a String of all properties specified in the new_domain_dialog
	 * 
	 * @return all properties formated as single string for persisting
	 */
	public String generatePropertiesString() {
		if (logger.isDebugEnabled())
			logger.debug("generating property String from properties ");
		StringBuilder sb = new StringBuilder();
		for (Property p : properties) {
			if (!p.getValue().isEmpty()) {
				sb.append(p.getLabel());
				sb.append('=');
				sb.append(p.getValue());
				sb.append(';');
			}
		}
		logger.debug("generated property String " + sb.toString());
		return sb.toString();
	}

	// --------Listener--------------------
	/**
	 * handles submits of the form
	 * 
	 * @param event
	 *            fired button event
	 */
	public void onNewDomainButtonClicked(ActionEvent event) {

		// FacesContext context = FacesContext.getCurrentInstance();
		// the value of customAlphabet is used if 'custom alphabet' was selected
		// in the form
		String tmpAlphabet;
		if (alphabet == null || alphabet.isEmpty()) {
			tmpAlphabet = customAlphabet;
		} else {
			tmpAlphabet = alphabet;
		}
		if (logger.isDebugEnabled()) {
			logger.debug("attempting to create domain with domain:" + domain + " alphabet:" + tmpAlphabet + " checkDigits:" + checkDigitClass
					+ " comment:" + comment + " properties:" + generatePropertiesString());
		}
		createDomain(domain, checkDigitClass, tmpAlphabet, generatePropertiesString(), comment, parentDomain);
	}

	/**
	 * used to reset all values in the form
	 */
	public void resetForm() {
		properties = null;
		domain = null;
		parentDomain = null;
		checkDigitClass = null;
		alphabet = null;
		customAlphabet = null;
		comment = null;
	}

	// ----------persisting--------------
	/**
	 * called on attempt to create a domain
	 * 
	 * @param domain
	 * @param checkDigitClass
	 * @param alphabet
	 * @param properties
	 * @param comment
	 */
	private void createDomain(String domain, String checkDigitClass, String alphabet, String properties, String comment, String parentDomain) {
		// initialize Faces Context for Messages
		FacesContext context = FacesContext.getCurrentInstance();
		// a new domain Object for persisting is created
		DomainDTO newDomain = new DomainDTO(domain, checkDigitClass, alphabet, properties, comment, 0, parentDomain);
		if (logger.isDebugEnabled()) {
			logger.debug("creating new domain: " + newDomain.toString());
		}
		Object[] args = { domain };
		try {
			// try to persist the domain
			manager.addDomain(newDomain);
			if (logger.isInfoEnabled()) {
				logger.info("new domain: " + newDomain.toString() + " persisted");
			}
			context.addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_INFO, new MessageFormat(messages.getString("domain.create.success")).format(args), ""));
			// update Domain cache
			domainController.updateDomainList();
			// reset Form Values in Bean
			resetForm();
			// update the input Panel
			RequestContext.getCurrentInstance().update("new_domain_form:input_panel");
		} catch (InvalidAlphabetException e) {
			context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, e.getLocalizedMessage(), e.getMessage()));
			if (logger.isErrorEnabled()) {
				logger.error("invalid alphabet", e);
			}
		} catch (InvalidCheckDigitClassException e) {
			context.addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, messages.getString("domain.error.invalidCheckDigitClass"), e.getMessage()));
			if (logger.isErrorEnabled()) {
				logger.error("invalid check digit class", e);
			}
		} catch (InvalidGeneratorException e) {
			// check, if the Exception was caused by an
			// InvalidAlphabetException, and show a different message in that
			// case
			boolean invalidAlphabet = false;
			Throwable tmpException = e;
			while (tmpException != null) {
				if (tmpException instanceof InvalidAlphabetException) {
					invalidAlphabet = true;
					break;
				}
				tmpException = tmpException.getCause();
			}
			if (invalidAlphabet) {
				context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, tmpException.getLocalizedMessage(), e.getMessage()));
			} else {
				context.addMessage(null,
						new FacesMessage(FacesMessage.SEVERITY_ERROR, messages.getString("domain.error.invalidGenerator"), e.getMessage()));
			}
			if (logger.isErrorEnabled()) {
				logger.error("generator can't be initialised:", e);
			}
		} catch (DomainInUseException e) {
			context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, messages.getString("domain.error.domainInUse"), e.getMessage()));
			if (logger.isErrorEnabled()) {
				logger.error("Domain is in use", e);
			}
		} catch (UnknownDomainException e) {
			context.addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, messages.getString("domain.error.unkownParentDomain"), e.getMessage()));
			if (logger.isErrorEnabled()) {
				logger.error("given parent domain '" + parentDomain + "' doesn't exist", e);
			}
		} catch (InvalidDomainNameException e) {
			context.addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, messages.getString("domain.error.invalidDomainName"), e.getMessage()));
			if (logger.isErrorEnabled()) {
				logger.error("given domain name '" + domain + "' is invalid", e);
			}
		}

	}

	// ----------getter/setter-------------------
	/**
	 * creates a List of Properties
	 * 
	 * @return a List of {@link Property} generated from the possible property list provided by {@link DomainManager}
	 */
	public List<Property> getProperties() {
		// if the properties have not been initialized yet:
		if (properties == null) {
			properties = new LinkedList<Property>();
			for (String possibleProperty : manager.listPossibleProperties()) {
				properties.add(new Property(possibleProperty));
			}
			if (logger.isDebugEnabled()) {
				logger.debug("getting possible properties from psnmanager");
			}
		}
		return properties;
	}

	public void setProperties(List<Property> properties) {
		this.properties = properties;
	}

	public String getDomain() {
		return domain;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}

	public String getCheckDigitClass() {
		return checkDigitClass;
	}

	public void setCheckDigitClass(String checkDigitClass) {
		this.checkDigitClass = checkDigitClass;
	}

	public String getAlphabet() {
		return alphabet;
	}

	public void setAlphabet(String alphabet) {
		this.alphabet = alphabet;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public String getCustomAlphabet() {
		return customAlphabet;
	}

	public void setCustomAlphabet(String customAlphabet) {
		if (logger.isDebugEnabled())
			logger.debug("custom ALphabet set with " + customAlphabet);
		this.customAlphabet = customAlphabet;
	}

	public PsnClassPathProvider getProvider() {
		return provider;
	}

	public void setProvider(PsnClassPathProvider provider) {
		this.provider = provider;
	}

	public DomainController getDomainController() {
		return domainController;
	}

	public void setDomainController(DomainController domainController) {
		this.domainController = domainController;
	}

	public String getParentDomain() {
		return parentDomain;
	}

	public void setParentDomain(String parentDomain) {
		this.parentDomain = parentDomain;
	}

}
