/**********************************************************************************
 * $URL:  $
 * $Id:  $
 ***********************************************************************************
 *
 * Copyright (c) 2007, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/
package org.sakaiproject.sitemanage.impl;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;
import java.util.HashMap;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.sitemanage.api.AffiliatedSectionProvider;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.util.ResourceLoader;

/**
 * @author zqian
 *
 */
public class SiteTypeProviderImpl implements org.sakaiproject.sitemanage.api.SiteTypeProvider {
	

	private static final Log log = LogFactory.getLog(SiteTypeProviderImpl.class);
	
	private static ResourceLoader rb = new ResourceLoader("SiteTypeProvider");
	
	private static String SITE_TYPE_GRADTOOLS_STUDENT = "GradToolsStudent";
	
	/**
	 * {@inheritDoc}
	 */
	public List<String> getTypes()
	{
		List<String> rv = new ArrayList<String>();
		rv.add(SITE_TYPE_GRADTOOLS_STUDENT);
		rv.add("GradToolsRackham");
		rv.add("GradToolsDepartment");
		return rv;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public List<String> getTypesForSiteList()
	{
		List<String> rv = new ArrayList<String>();
		// if there is a GradToolsStudent choice inside
		boolean remove = false;
		try {
			// the Grad Tools site option is only presented to
			// GradTools Candidates
			String userId = StringUtils.trimToEmpty(SessionManager.getCurrentSessionUserId());

			// am I a grad student?
			if (!isGradToolsCandidate(userId)) {
				// not a gradstudent
				remove = true;
			}
		} catch (Exception e) {
			remove = true;
			log.warn("getTypesForSiteCreation GradToolsStudent sites", e);
		}
		if (!remove) {
			rv.add(SITE_TYPE_GRADTOOLS_STUDENT);
		}
		return rv;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public List<String> getTypesForSiteCreation()
	{
		List<String> rv = new ArrayList<String>();
		try {
			// the Grad Tools site option is only presented to UM grad
			// students
			String userId = StringUtils.trimToEmpty(SessionManager.getCurrentSessionUserId());

			// am I a UM grad student?
			Boolean isGradStudent = Boolean.valueOf(
					isGradToolsCandidate(userId));

			// if I am a UM grad student, do I already have a Grad Tools
			// site?
			boolean noGradToolsSite = true;
			if (hasGradToolsStudentSite(userId))
				noGradToolsSite = false;
			
			if (isGradStudent && noGradToolsSite)
			{
				rv.add(SITE_TYPE_GRADTOOLS_STUDENT);
			}
		} catch (Exception e) {
			log.warn(this + "getTypesForSiteCreation ", e);
		}
		return rv;
	}

	/**
	 * {@inheritDoc}
	 */
	public HashMap<String, String> getTemplateForSiteTypes()
	{
		HashMap<String, String> rv = new HashMap<String, String>();
		// site template used to create a UM Grad Tools student site
		rv.put(SITE_TYPE_GRADTOOLS_STUDENT, "!gtstudent");
		return rv;	
	}
	
	/**
	 * {@inheritDoc}
	 */
	public String getSiteTitle(String type, List<String> params)
	{
		String rv = "";
		
		if (type.equals(SITE_TYPE_GRADTOOLS_STUDENT))
		{
			rv += rb.getString("java.grad");
			if (params != null)
			{
				for(String p : params)
				{
					rv += "-";
					rv += p;
				}
			}
		}
		return rv;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public String getSiteDescription(String type, List<String> params)
	{
		String rv = "";
		
		if (type.equals(SITE_TYPE_GRADTOOLS_STUDENT))
		{
			rv += rb.getString("java.gradsite");
			if (params != null)
			{
				for(String p : params)
				{
					rv += "-";
					rv += p;
				}
			}
		}
		return rv;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public String getSiteShortDescription(String type, List<String> params)
	{
		String rv = "";
		
		if (type.equals(SITE_TYPE_GRADTOOLS_STUDENT))
		{
			rv += rb.getString("java.grad");
			if (params != null)
			{
				for(String p : params)
				{
					rv += "-";
					rv += p;
				}
			}
		}
		
		return rv;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public String getSiteAlias(String type, List<String> params)
	{
		String rv = "";
		
		if (type.equals(SITE_TYPE_GRADTOOLS_STUDENT))
		{
			rv += "gradtools";
			if (params != null)
			{
				for(String p : params)
				{
					rv += "-";
					rv += p;
				}
			}
		}
		
		return rv;
	}
	
	/**
	 * Special check against the Dissertation service, which might not be
	 * here...
	 * 
	 * @return
	 */
	protected boolean isGradToolsCandidate(String userId) {
		// DissertationService.isCandidate(userId) - but the hard way

		Object service = ComponentManager
				.get("org.sakaiproject.api.app.dissertation.DissertationService");
		if (service == null)
			return false;

		// the method signature
		Class[] signature = new Class[1];
		signature[0] = String.class;

		// the method name
		String methodName = "isCandidate";

		// find a method of this class with this name and signature
		try {
			Method method = service.getClass().getMethod(methodName, signature);

			// the parameters
			Object[] args = new Object[1];
			args[0] = userId;

			// make the call
			Boolean rv = (Boolean) method.invoke(service, args);
			return rv.booleanValue();
		} catch (Throwable t) {
		}

		return false;
	}

	/**
	 * User has a Grad Tools student site
	 * 
	 * @return
	 */
	protected boolean hasGradToolsStudentSite(String userId) {
		boolean has = false;
		int n = 0;
		try {
			n = SiteService.countSites(
					org.sakaiproject.site.api.SiteService.SelectionType.UPDATE,
					SITE_TYPE_GRADTOOLS_STUDENT, null, null);
			if (n > 0)
				has = true;
		} catch (Exception e) {
			log.warn(this + ".addGradToolsStudentSite:" + e.getMessage(), e);
		}

		return has;

	}// hasGradToolsStudentSite
	
	public void init() {
	}

	public void destroy() {
	}
}
