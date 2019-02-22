/**
 * Copyright (c) 2007 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.scorm.ui;

import javax.servlet.http.HttpServletRequest;

import org.apache.wicket.protocol.http.servlet.ServletWebRequest;
import org.apache.wicket.request.Request;

public class AbsoluteUrl
{
	private Request request;
	private String relativeUrl;
	private boolean includeServer, includeContext;

	public AbsoluteUrl(Request request, String relativeUrl, boolean includeServer, boolean includeContext)
	{
		this.request = request;
		this.relativeUrl = relativeUrl;
		this.includeServer = includeServer;
		this.includeContext = includeContext;
	}

	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
	
		ServletWebRequest webRequest = (ServletWebRequest) request;
		HttpServletRequest servletRequest = webRequest.getContainerRequest();

		if (includeServer)
		{
			builder.append(servletRequest.getScheme()).append("://")
				.append(servletRequest.getServerName())
				.append(":")
				.append(servletRequest.getServerPort());
		}

		if (includeContext)
		{
			builder.append(servletRequest.getContextPath()).append("/");
		}

		builder.append(relativeUrl);
		return builder.toString();
	}
}
