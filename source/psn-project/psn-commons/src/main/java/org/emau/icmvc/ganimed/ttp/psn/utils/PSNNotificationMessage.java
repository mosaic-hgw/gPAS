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

import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.emau.icmvc.ganimed.ttp.psn.dto.InsertPairExceptionDTO;
import org.emau.icmvc.ganimed.ttp.psn.enums.AnonymisationResult;
import org.emau.icmvc.ganimed.ttp.psn.enums.DeletionResult;
import org.emau.icmvc.ttp.notification.NotificationMessage;

public class PSNNotificationMessage extends NotificationMessage
{
	private String domain;
	private Set<String> request;
	private Map<String, String> pseudonymisationResult;
	private Map<String, AnonymisationResult> anonymisationResult;
	private Map<String, DeletionResult> deletionResult;
	private Map<String, String> insertionRequest;
	private List<InsertPairExceptionDTO> insertionResult;

	public PSNNotificationMessage()
	{
		// needed for de-/serialization
	}

	public PSNNotificationMessage(String json) throws IOException
	{
		super(json);
	}

	public PSNNotificationMessage(String type, String clientId, String domain, String comment)
	{
		this(type, clientId, domain, comment, null);
	}

	public PSNNotificationMessage(String type, String clientId, String domain, String comment, Map<String, Serializable> context)
	{
		super(type, clientId, comment, context);
		this.domain = domain;
	}

	public PSNNotificationMessage(NotificationMessage msg)
	{
		capture(msg);
	}

	@Override
	public void capture(NotificationMessage msg)
	{
		super.capture(msg);

		if (msg instanceof PSNNotificationMessage pMsg)
		{
			domain = pMsg.domain;
			request = pMsg.request;
			pseudonymisationResult = pMsg.pseudonymisationResult;
			anonymisationResult = pMsg.anonymisationResult;
			deletionResult = pMsg.deletionResult;
			insertionRequest = pMsg.insertionRequest;
			insertionResult = pMsg.insertionResult;
		}
	}

	public String getDomain()
	{
		return domain;
	}

	public void setDomain(String domain)
	{
		this.domain = domain;
	}

	public Set<String> getRequest()
	{
		return request;
	}

	public void setRequest(Set<String> request)
	{
		this.request = request;
	}

	public Map<String, String> getPseudonymisationResult()
	{
		return pseudonymisationResult;
	}

	public void setPseudonymisationResult(Map<String, String> pseudonymisationResult)
	{
		this.pseudonymisationResult = pseudonymisationResult;
	}

	public Map<String, AnonymisationResult> getAnonymisationResult()
	{
		return anonymisationResult;
	}

	public void setAnonymisationResult(Map<String, AnonymisationResult> anonymisationResult)
	{
		this.anonymisationResult = anonymisationResult;
	}

	public Map<String, DeletionResult> getDeletionResult()
	{
		return deletionResult;
	}

	public void setDeletionResult(Map<String, DeletionResult> deletionResult)
	{
		this.deletionResult = deletionResult;
	}

	public Map<String, String> getInsertionRequest()
	{
		return insertionRequest;
	}

	public void setInsertionRequest(Map<String, String> insertionRequest)
	{
		this.insertionRequest = insertionRequest;
	}

	public List<InsertPairExceptionDTO> getInsertionResult()
	{
		return insertionResult;
	}

	public void setInsertionResult(List<InsertPairExceptionDTO> insertionResult)
	{
		this.insertionResult = insertionResult;
	}

	@Override public boolean equals(Object o)
	{
		if (this == o)
			return true;

		if (o == null || getClass() != o.getClass())
			return false;

		PSNNotificationMessage that = (PSNNotificationMessage) o;

		return new EqualsBuilder().appendSuper(super.equals(o))
				.append(domain, that.domain)
				.append(request, that.request)
				.append(pseudonymisationResult, that.pseudonymisationResult)
				.append(anonymisationResult, that.anonymisationResult)
				.append(deletionResult, that.deletionResult)
				.append(insertionRequest, that.insertionRequest)
				.append(insertionResult, that.insertionResult)
				.isEquals();
	}

	@Override public int hashCode()
	{
		return new HashCodeBuilder(17, 37).appendSuper(super.hashCode())
				.append(domain)
				.append(request)
				.append(pseudonymisationResult)
				.append(anonymisationResult)
				.append(deletionResult)
				.append(insertionRequest)
				.append(insertionResult)
				.toHashCode();
	}

	@Override public String toString()
	{
		return new ToStringBuilder(this)
				.append("domain", domain)
				.append("request", request)
				.append("pseudonymisationResult", pseudonymisationResult)
				.append("anonymisationResult", anonymisationResult)
				.append("deletionResult", deletionResult)
				.append("insertionRequest", insertionRequest)
				.append("insertionResult", insertionResult)
				.append("type", type)
				.append("clientId", clientId)
				.append("comment", comment)
				.append("context", context)
				.toString();
	}
}
