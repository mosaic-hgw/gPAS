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
import java.util.List;
import java.util.ResourceBundle;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import org.emau.icmvc.ganimed.ttp.psn.DomainManager;
import org.emau.icmvc.ganimed.ttp.psn.dto.DomainDTO;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.DomainInUseException;
import org.emau.icmvc.ganimed.ttp.psn.exceptions.UnknownDomainException;
import org.primefaces.event.SelectEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Backing Bean for listing and deleting Domains (domain_tableV2.xthml)
 * 
 * @author Weiher
 * 
 */
@ManagedBean(name = "DomainController")
@ViewScoped
public class DomainController {
	/**
	 * backend service
	 */
	@EJB(lookup = "java:global/gpas/psn-ejb/DomainManagerBean!org.emau.icmvc.ganimed.ttp.psn.DomainManager")
	private DomainManager manager;

	/**
	 * bundle of all info/error messages used in the application
	 */
	private ResourceBundle messages;

	private final Logger logger = LoggerFactory.getLogger(DomainController.class);

	/**
	 * List of all domains/psn-projects (output)
	 */
	private List<DomainDTO> domainList;
	private DomainDTO selectedDomain;

	/**
	 * populates domains when bean is created
	 */
	@PostConstruct
	public void init() {
		messages = ResourceBundle.getBundle("messages");
		updateDomainList();
		if (logger.isDebugEnabled()) {
			logger.debug("DomainController initialised");
		}
	}

	/**
	 * updates the DomainList the domain table is based on
	 */
	public void updateDomainList() {
		domainList = manager.listDomains();
		selectedDomain = null;
		if (logger.isDebugEnabled()) {
			logger.debug("Domain List updated: " + domainList.size() + "entries");
		}
	}

	// ------EventListeners-------------------------------
	/**
	 * sets the selected domain in the bean and updates the psn list
	 * 
	 * @param event
	 *            Row Selection Event
	 */
	public void onDomainRowSelect(SelectEvent event) {
		selectedDomain = (DomainDTO) event.getObject();
		if (logger.isDebugEnabled()) {
			logger.debug("Domain Table row '" + selectedDomain.getDomain() + "' selected");
		}

	}

	/**
	 * called on attempt to delete a domain
	 * 
	 * @param event
	 */
	public void onDeleteDomain(ActionEvent event) {
		FacesContext context = FacesContext.getCurrentInstance();
		if (selectedDomain != null) {
			if (logger.isDebugEnabled()) {
				logger.debug("delete for domain '" + selectedDomain.getDomain() + "' called:");
			}
			// argument für messages
			Object[] args = { selectedDomain.getDomain() };
			try {
				manager.deleteDomain(selectedDomain.getDomain());

				if (logger.isDebugEnabled()) {
					logger.debug("delete successful");
				}

				context.addMessage(null,
						new FacesMessage(FacesMessage.SEVERITY_INFO, new MessageFormat(messages.getString("domain.delete.sucess")).format(args), ""));
				selectedDomain = null;
				updateDomainList();
			} catch (DomainInUseException e) {
				context.addMessage(null,
						new FacesMessage(FacesMessage.SEVERITY_ERROR, messages.getString("domain.error.domainInUse"), e.getMessage()));
				if (logger.isErrorEnabled()) {
					logger.error("domain in use:", e);
				}
			} catch (UnknownDomainException e) {
				context.addMessage(null,
						new FacesMessage(FacesMessage.SEVERITY_ERROR,
								new MessageFormat(messages.getString("domain.error.unkownDomain")).format(args), ""));
				if (logger.isErrorEnabled()) {
					logger.error("domain not found", e);
				}
			}
		} else {
			context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, messages.getString("domain.error.noSelectedDomain"), ""));
			if (logger.isDebugEnabled()) {
				logger.debug("delete domain call failed: no selected domain");
			}
		}

	}

	public List<DomainDTO> getDomainList() {
		return domainList;
	}

	public void setDomainList(List<DomainDTO> domainList) {
		this.domainList = domainList;
	}

	public DomainDTO getSelectedDomain() {
		return selectedDomain;
	}

	public void setSelectedDomain(DomainDTO selectedDomain) {
		this.selectedDomain = selectedDomain;
	}

	public DomainManager getManager() {
		return manager;
	}

	public void setManager(DomainManager manager) {
		this.manager = manager;
	}
}
