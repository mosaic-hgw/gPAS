package org.emau.icmvc.ttp.psn.frontend.controller.testtools;

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



import org.emau.icmvc.ganimed.ttp.psn.DomainManager;
import org.emau.icmvc.ganimed.ttp.psn.PSNManager;
import org.emau.icmvc.ganimed.ttp.psn.PSNManagerWithNotification;
import org.emau.icmvc.ganimed.ttp.psn.StatisticManager;
import org.emau.icmvc.ttp.psn.frontend.controller.common.ServiceHelper;
import org.icmvc.ttp.web.controller.Time;
import org.icmvc.ttp.web.testtools.JsfTest;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;

import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public abstract class gPASWebTest extends JsfTest
{
	@Mock
	protected ServiceHelper serviceHelper;
	@Mock
	protected PSNManager service;
	@Mock
	protected PSNManagerWithNotification serviceWithNotification;
	@Mock
	protected PSNManager serviceWithAutomaticNotification;
	@Mock
	protected DomainManager domainService;
	@Mock
	protected StatisticManager statisticManager;
	@Mock
	protected Time time;

	@BeforeEach
	protected void setUpGpasWebTest()
	{
		initMocks(this);
		when(serviceHelper.getService()).thenReturn(service);
		when(serviceHelper.getManager()).thenReturn(domainService);
		when(serviceHelper.getServiceWithNotification()).thenReturn(serviceWithNotification);
		when(serviceHelper.getServiceWithAutomaticNotification(true)).thenReturn(serviceWithAutomaticNotification);
		when(serviceHelper.getServiceWithAutomaticNotification(false)).thenReturn(service);
		when(serviceHelper.getStatisticService()).thenReturn(statisticManager);
	}
}
