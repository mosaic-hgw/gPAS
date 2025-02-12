package org.emau.icmvc.ttp.psn.frontend.controller.common;

/*-
 * ###license-information-start###
 * gICS - a Generic Informed Consent Service
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

import java.io.Serial;
import java.io.Serializable;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.emau.icmvc.ganimed.ttp.psn.DomainManager;
import org.emau.icmvc.ganimed.ttp.psn.PSNManager;
import org.emau.icmvc.ganimed.ttp.psn.PSNManagerWithNotification;
import org.emau.icmvc.ganimed.ttp.psn.StatisticManager;
import org.emau.icmvc.ttp.auth.TTPNames;
import org.emau.icmvc.ttp.util.ProxyBuilder;
import org.icmvc.ttp.web.util.WebAuthContext;

/**
 * Common helper for using the (proxied) services.
 *
 * @author moser
 */
@Named
@SessionScoped
public class ServiceHelper implements Serializable
{
	@Serial
	private static final long serialVersionUID = 893963112948317725L;

	protected static final String TOOL = "gPAS";
	protected static final String NOTIFICATION_CLIENT_ID = TOOL + "_Web";

	@Inject
	private WebAuthContext webAuthContext;

	@EJB(lookup = "java:global/gpas/gpas-ejb/PSNManagerBean!org.emau.icmvc.ganimed.ttp.psn.PSNManager")
	protected transient PSNManager serviceTarget;
	protected transient PSNManager service;
	protected transient PSNManager serviceWithAutomaticNotification;

	@EJB(lookup = "java:global/gpas/gpas-ejb/PSNManagerWithNotificationBean!org.emau.icmvc.ganimed.ttp.psn.PSNManagerWithNotification")
	protected transient PSNManagerWithNotification serviceWithNotificationTarget;
	protected transient PSNManagerWithNotification serviceWithNotification;

	@EJB(lookup = "java:global/gpas/gpas-ejb/DomainManagerBean!org.emau.icmvc.ganimed.ttp.psn.DomainManager")
	protected transient DomainManager managerTarget;
	protected transient DomainManager manager;

	@EJB(lookup = "java:global/gpas/gpas-ejb/StatisticManagerBean!org.emau.icmvc.ganimed.ttp.psn.StatisticManager")
	private transient StatisticManager statisticServiceTarget;
	private transient StatisticManager statisticService;

	@PostConstruct
	private void init() {
		if (getWebAuthContext().isUsingDomainBasedRolesDisabled(TTPNames.Tool.epix))
		{
			service = serviceTarget;
			manager = managerTarget;
			serviceWithNotification = serviceWithNotificationTarget;
			statisticService = statisticServiceTarget;
		}
		else
		{
			service = getWebAuthContext().createUpdateAuthContextProxy(serviceTarget, PSNManager.class);
			manager = getWebAuthContext().createUpdateAuthContextProxy(managerTarget, DomainManager.class);
			serviceWithNotification = getWebAuthContext().createUpdateAuthContextProxy(serviceWithNotificationTarget, PSNManagerWithNotification.class);
			statisticService = getWebAuthContext().createUpdateAuthContextProxy(statisticServiceTarget, StatisticManager.class);
		}
	}

	public PSNManager getService()
	{
		if (service == null)
		{
			init();
		}
		return service;
	}

	public DomainManager getManager()
	{
		if (manager == null)
		{
			init();
		}
		return manager;
	}

	public PSNManagerWithNotification getServiceWithNotification()
	{
		if (serviceWithNotification == null)
		{
			init();
		}
		return serviceWithNotification;
	}

	public PSNManager getServiceWithAutomaticNotification(boolean notify)
	{
		if (notify && serviceWithAutomaticNotification == null)
		{
			serviceWithAutomaticNotification = ProxyBuilder.wrap(getService(), PSNManager.class).withMatchingMethodsDelegatingInvocationHandler(
					getServiceWithNotification(), PSNManagerWithNotification.class, new ProxyBuilder.ArgumentsPrepender(NOTIFICATION_CLIENT_ID)).build();
		}
		return notify ? serviceWithAutomaticNotification : getService();
	}

	public StatisticManager getStatisticService()
	{
		if (statisticService == null)
		{
			init();
		}
		return statisticService;
	}

	public WebAuthContext getWebAuthContext()
	{
		return webAuthContext;
	}
}
