package org.emau.icmvc.ganimed.ttp.psn.utils;

/*-
 * ###license-information-start###
 * gPAS - a Generic Pseudonym Administration Service
 * __
 * Copyright (C) 2013 - 2023 Independent Trusted Third Party of the University Medicine Greifswald
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

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.emau.icmvc.ganimed.ttp.psn.dto.InsertPairExceptionDTO;
import org.emau.icmvc.ganimed.ttp.psn.enums.AnonymisationResult;
import org.emau.icmvc.ganimed.ttp.psn.enums.DeletionResult;
import org.emau.icmvc.ganimed.ttp.psn.enums.InsertPairError;
import org.junit.jupiter.api.BeforeEach;

public class AbstractPSNNotificationMessageTest
{
	protected static String JSON_SUPER_ATTRS = json("'clientId':'gPAS_Web','comment':'c','domain':'d'");
	protected static String JSON_REQUEST = json("'request':['v1','v2']");
	protected static String JSON_P = json("{'type':'GPAS.GetOrCreatePseudonyms'," + JSON_SUPER_ATTRS + "," + JSON_REQUEST
			+ ",'pseudonymisationResult':{'v1':'p1','v2':'p2'}}");
	protected static String JSON_A = json("{'type':'GPAS.AnonymiseEntries'," + JSON_SUPER_ATTRS + "," + JSON_REQUEST
			+ ",'anonymisationResult':{'v1':'SUCCESS','v2':'NOT_FOUND'}}");
	protected static String JSON_D = json("{'type':'GPAS.DeleteEntries'," + JSON_SUPER_ATTRS + "," + JSON_REQUEST
			+ ",'deletionResult':{'v1':'SUCCESS','v2':'NOT_FOUND'}}");
	protected static String JSON_I = json("{'type':'GPAS.InsertValuePseudonymPairs'," + JSON_SUPER_ATTRS
			+ ",'insertionRequest':{'v1':'p1','v2':'p2'}"
			+ ",'insertionResult':[{'message':'failed','value':'v2','pseudonym':'p2','errorType':'VALUE_INVALID'}]}");
	protected PSNNotificationMessage msg;
	protected PSNNotificationMessage msgP;
	protected PSNNotificationMessage msgA;
	protected PSNNotificationMessage msgD;
	PSNNotificationMessage msgI;
	TreeSet<String> request;
	TreeMap<String, String> pseudonymisationResult;
	TreeMap<String, AnonymisationResult> anonymisationResult;
	TreeMap<String, DeletionResult> deletionResult;
	TreeMap<String, String> insertionRequest;
	List<InsertPairExceptionDTO> insertionResult;

	protected static String json(String s)
	{
		return s.replace('\'', '"');
	}

	@BeforeEach
	void setUp()
	{
		request = new TreeSet<>(Set.of("v1", "v2"));
		pseudonymisationResult = new TreeMap<>(Map.of("v1", "p1", "v2", "p2"));
		anonymisationResult = new TreeMap<>(Map.of("v1", AnonymisationResult.SUCCESS, "v2", AnonymisationResult.NOT_FOUND));
		deletionResult = new TreeMap<>(Map.of("v1", DeletionResult.SUCCESS, "v2", DeletionResult.NOT_FOUND));
		insertionRequest = new TreeMap<>(Map.of("v1", "p1", "v2", "p2"));
		insertionResult = List.of(new InsertPairExceptionDTO("failed", "v2", "p2", InsertPairError.VALUE_INVALID));

		msg = new PSNNotificationMessage("type", "gPAS_Web", "d", "c");

		msgP = new PSNNotificationMessage(msg);
		msgP.setType("GPAS.GetOrCreatePseudonyms");
		msgP.setRequest(request);
		msgP.setPseudonymisationResult(pseudonymisationResult);

		msgA = new PSNNotificationMessage(msg);
		msgA.setType("GPAS.AnonymiseEntries");
		msgA.setRequest(request);
		msgA.setAnonymisationResult(anonymisationResult);

		msgD = new PSNNotificationMessage(msg);
		msgD.setType("GPAS.DeleteEntries");
		msgD.setRequest(request);
		msgD.setDeletionResult(deletionResult);

		msgI = new PSNNotificationMessage(msg);
		msgI.setType("GPAS.InsertValuePseudonymPairs");
		msgI.setInsertionRequest(insertionRequest);
		msgI.setInsertionResult(insertionResult);
	}

	protected PSNNotificationMessage captured(PSNNotificationMessage msg)
	{
		PSNNotificationMessage captured = new PSNNotificationMessage();
		captured.capture(msg);
		return captured;
	}
}
