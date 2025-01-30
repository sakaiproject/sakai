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
package org.sakaiproject.scorm.ui.player;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.cover.ComponentManager;

import org.springframework.web.filter.GenericFilterBean;

public class ScormSecurityFilter extends GenericFilterBean {

	private static final SecurityService securityService = (SecurityService) ComponentManager.get(SecurityService.class);

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException
	{
		ScormSecurityAdvisor securityAdvisor = new ScormSecurityAdvisor();
		securityService.pushAdvisor(securityAdvisor);
		try
		{
			chain.doFilter(request, response);
		}
		finally
		{
			securityService.popAdvisor(securityAdvisor);
		}
	}
}