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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.sakaiproject.util.ResourceLoader;

import lombok.extern.slf4j.Slf4j;

/**
 * @author zqian
 *
 */
@Slf4j
public class SiteTypeProviderImpl implements org.sakaiproject.sitemanage.api.SiteTypeProvider {

	private static ResourceLoader rb = new ResourceLoader("SiteTypeProvider");
	
	@Override
	public List<String> getTypes()
	{
		List<String> rv = new ArrayList<String>();
		return rv;
	}
	
	@Override
	public List<String> getTypesForSiteList()
	{
		return new ArrayList<String>();
	}
	
	@Override
	public List<String> getTypesForSiteCreation()
	{
		return new ArrayList<String>();
	}

	@Override
	public HashMap<String, String> getTemplateForSiteTypes()
	{
		return new HashMap<String, String>();	
	}
	
	@Override
	public String getSiteTitle(String type, List<String> params)
	{
		return "";
	}
	
	@Override
	public String getSiteDescription(String type, List<String> params)
	{
		return "";
	}
	
	@Override
	public String getSiteShortDescription(String type, List<String> params)
	{
		return "";
	}
	
	@Override
	public String getSiteAlias(String type, List<String> params)
	{
		return "";
	}
	
	public void init() {
	}

	public void destroy() {
	}
}
