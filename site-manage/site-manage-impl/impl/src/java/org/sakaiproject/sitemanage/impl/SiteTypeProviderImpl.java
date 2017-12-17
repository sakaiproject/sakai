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
 *       http://www.opensource.org/licenses/ECL-2.0
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

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;

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
@Slf4j
public class SiteTypeProviderImpl implements org.sakaiproject.sitemanage.api.SiteTypeProvider {

	private static ResourceLoader rb = new ResourceLoader("SiteTypeProvider");
	
	/**
	 * {@inheritDoc}
	 */
	public List<String> getTypes()
	{
		List<String> rv = new ArrayList<String>();
		return rv;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public List<String> getTypesForSiteList()
	{
		return new ArrayList<String>();
	}
	
	/**
	 * {@inheritDoc}
	 */
	public List<String> getTypesForSiteCreation()
	{
		return new ArrayList<String>();
	}

	/**
	 * {@inheritDoc}
	 */
	public HashMap<String, String> getTemplateForSiteTypes()
	{
		return new HashMap<String, String>();	
	}
	
	/**
	 * {@inheritDoc}
	 */
	public String getSiteTitle(String type, List<String> params)
	{
		return "";
	}
	
	/**
	 * {@inheritDoc}
	 */
	public String getSiteDescription(String type, List<String> params)
	{
		return "";
	}
	
	/**
	 * {@inheritDoc}
	 */
	public String getSiteShortDescription(String type, List<String> params)
	{
		return "";
	}
	
	/**
	 * {@inheritDoc}
	 */
	public String getSiteAlias(String type, List<String> params)
	{
		return "";
	}
	
	public void init() {
	}

	public void destroy() {
	}
}
