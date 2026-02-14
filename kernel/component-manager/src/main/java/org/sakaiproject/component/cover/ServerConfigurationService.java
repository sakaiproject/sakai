/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 Sakai Foundation
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

package org.sakaiproject.component.cover;

import org.sakaiproject.component.cover.ComponentManager;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * ServerConfigurationService is a static Cover for the {@link org.sakaiproject.component.api.ServerConfigurationService ServerConfigurationService}; see that interface for usage details.
 * </p>
 * 
 * @version $Revision$
 */
public class ServerConfigurationService
{
	public final static String CURRENT_SERVER_URL = org.sakaiproject.component.api.ServerConfigurationService.CURRENT_SERVER_URL;

	public final static String CURRENT_PORTAL_PATH = org.sakaiproject.component.api.ServerConfigurationService.CURRENT_PORTAL_PATH;

   /**
	 * Access the component instance: special cover only method.
	 * 
	 * @return the component instance.
	 */
	public static org.sakaiproject.component.api.ServerConfigurationService getInstance()
	{
		if (ComponentManager.CACHE_COMPONENTS)
		{
			if (m_instance == null)
				m_instance = (org.sakaiproject.component.api.ServerConfigurationService) ComponentManager
						.get(org.sakaiproject.component.api.ServerConfigurationService.class);
			return m_instance;
		}
		else
		{
			return (org.sakaiproject.component.api.ServerConfigurationService) ComponentManager
					.get(org.sakaiproject.component.api.ServerConfigurationService.class);
		}
	}

	private static org.sakaiproject.component.api.ServerConfigurationService m_instance = null;

	public static java.lang.String SERVICE_NAME = org.sakaiproject.component.api.ServerConfigurationService.SERVICE_NAME;

	public static java.lang.String getServerId()
	{
		org.sakaiproject.component.api.ServerConfigurationService service = getInstance();
		if (service == null) return null;

		return service.getServerId();
	}

	public static java.lang.String getServerInstance()
	{
		org.sakaiproject.component.api.ServerConfigurationService service = getInstance();
		if (service == null) return null;

		return service.getServerInstance();
	}

	public static java.lang.String getServerIdInstance()
	{
		org.sakaiproject.component.api.ServerConfigurationService service = getInstance();
		if (service == null) return null;

		return service.getServerIdInstance();
	}

	public static java.lang.String getServerName()
	{
		org.sakaiproject.component.api.ServerConfigurationService service = getInstance();
		if (service == null) return null;

		return service.getServerName();
	}

	public static java.lang.String getServerUrl()
	{
		org.sakaiproject.component.api.ServerConfigurationService service = getInstance();
		if (service == null) return null;

		return service.getServerUrl();
	}

	public static java.lang.String getAccessUrl()
	{
		org.sakaiproject.component.api.ServerConfigurationService service = getInstance();
		if (service == null) return null;

		return service.getAccessUrl();
	}

	public static java.lang.String getAccessPath()
	{
		org.sakaiproject.component.api.ServerConfigurationService service = getInstance();
		if (service == null) return null;

		return service.getAccessPath();
	}

	public static java.lang.String getHelpUrl(java.lang.String param0)
	{
		org.sakaiproject.component.api.ServerConfigurationService service = getInstance();
		if (service == null) return null;

		return service.getHelpUrl(param0);
	}

	public static java.lang.String getPortalUrl()
	{
		org.sakaiproject.component.api.ServerConfigurationService service = getInstance();
		if (service == null) return null;

		return service.getPortalUrl();
	}

	public static java.lang.String getToolUrl()
	{
		org.sakaiproject.component.api.ServerConfigurationService service = getInstance();
		if (service == null) return null;

		return service.getToolUrl();
	}

	public static java.lang.String getGatewaySiteId()
	{
		org.sakaiproject.component.api.ServerConfigurationService service = getInstance();
		if (service == null) return null;

		return service.getGatewaySiteId();
	}

	public static java.lang.String getLoggedOutUrl()
	{
		org.sakaiproject.component.api.ServerConfigurationService service = getInstance();
		if (service == null) return null;

		return service.getLoggedOutUrl();
	}

	public static java.lang.String getUserHomeUrl()
	{
		org.sakaiproject.component.api.ServerConfigurationService service = getInstance();
		if (service == null) return null;

		return service.getUserHomeUrl();
	}

	public static java.lang.String getSakaiHomePath()
	{
		org.sakaiproject.component.api.ServerConfigurationService service = getInstance();
		if (service == null) return null;

		return service.getSakaiHomePath();
	}

	public static boolean getBoolean(java.lang.String param0, boolean param1)
	{
		org.sakaiproject.component.api.ServerConfigurationService service = getInstance();
		if (service == null) return false;

		return service.getBoolean(param0, param1);
	}

	public static java.lang.String getString(java.lang.String param0, java.lang.String param1)
	{
		org.sakaiproject.component.api.ServerConfigurationService service = getInstance();
		if (service == null) return null;

		return service.getString(param0, param1);
	}

	public static java.lang.String getString(java.lang.String param0)
	{
		org.sakaiproject.component.api.ServerConfigurationService service = getInstance();
		if (service == null) return null;

		return service.getString(param0);
	}

	public static java.lang.String[] getStrings(java.lang.String param0)
	{
		org.sakaiproject.component.api.ServerConfigurationService service = getInstance();
		if (service == null) return null;

		return service.getStrings(param0);
	}

	public static java.lang.String getBrowserFeatureAllowString()
	{
		org.sakaiproject.component.api.ServerConfigurationService service = getInstance();
		if (service == null) return "camera; fullscreen; microphone; local-network-access *";

		return service.getBrowserFeatureAllowString();
	}

	public static java.util.List getToolOrder(java.lang.String param0)
	{
		org.sakaiproject.component.api.ServerConfigurationService service = getInstance();
		if (service == null) return null;

		return service.getToolOrder(param0);
	}

	/** 
	 * Access the list of tools by group
	 * 
	 * @param category
	 *			 The tool category
	 * @return An unordered list of tool ids (String) in selected group, or an empty list if there are none for this category.
	 */	
	public static java.util.List getToolsRequired(java.lang.String param0)
	{
		org.sakaiproject.component.api.ServerConfigurationService service = getInstance();
		if (service == null) return null;

		return service.getToolsRequired(param0);
	}
	
	/**
	 * Access the list of groups by category (site type)
	 * 
	 * @param category
	 *			 The tool category
	 * @return An ordered list of tool ids (String) indicating the desired tool display order, or an empty list if there are none for this category.
	 */
	public static java.util.List getCategoryGroups(java.lang.String param0)
	{
		org.sakaiproject.component.api.ServerConfigurationService service = getInstance();
		if (service == null) return null;

		return service.getCategoryGroups(param0);
	}	


	/*
	 * Returns true if selected tool is contained in pre-initialized list of selected items
	 * @parms toolId id of the selected tool
	 */
	public static boolean toolGroupIsSelected(String param0,String param1) {
		org.sakaiproject.component.api.ServerConfigurationService service = getInstance();
		if (service == null) return false;

		return service.toolGroupIsSelected(param0,param1);			
	}

	 /*
	  * Returns true if selected tool is contained in pre-initialized list of required items
	  * @parms toolId id of the selected tool
	  */
	public static boolean toolGroupIsRequired(String param0, String param1) {
		org.sakaiproject.component.api.ServerConfigurationService service = getInstance();
		if (service == null) return false;
		return service.toolGroupIsRequired(param0,param1);			
	}
	
	public static java.util.List getToolGroup(java.lang.String param0)
	{
		org.sakaiproject.component.api.ServerConfigurationService service = getInstance();
		if (service == null) return null;

		return service.getToolGroup(param0);
	}

	public static java.util.List getDefaultTools(java.lang.String param0)
	{
		org.sakaiproject.component.api.ServerConfigurationService service = getInstance();
		if (service == null) return null;

		return service.getDefaultTools(param0);
	}

	public static int getInt(java.lang.String param0, int param1)
	{
		org.sakaiproject.component.api.ServerConfigurationService service = getInstance();
		if (service == null) return 0;

		return service.getInt(param0, param1);
	}


   /**
    * access the list of tool categories for the given site type
    *
    * @param category the site type
    * @return a list of tool category ids in order
    */
   public static List<String> getToolCategories(String category)
   {
      org.sakaiproject.component.api.ServerConfigurationService service = getInstance();
      if (service == null) return null;

      return service.getToolCategories(category);
   }

   /**
    * access the map of tool categories to tool ids for this site type
    * @param category the site type
    * @return a map of tool category ids to tool ids
    */
   public static Map<String, List<String>> getToolCategoriesAsMap(String category)
   {
      org.sakaiproject.component.api.ServerConfigurationService service = getInstance();
      if (service == null) return null;

      return service.getToolCategoriesAsMap(category);
   }

   /**
    * access a map of tool id to tool category id for this site type
    * @param category the site type
    * @return map with tool id as key and category id as value
    */
   public static Map<String, String> getToolToCategoryMap(String category)
   {
      org.sakaiproject.component.api.ServerConfigurationService service = getInstance();
      if (service == null) return null;

      return service.getToolToCategoryMap(category);
   }

   public static String getSmtpFrom() {
      org.sakaiproject.component.api.ServerConfigurationService service = getInstance();
      if (service == null) return null;

      return service.getSmtpFrom();
   }

   public static String getSmtpPort() {
      org.sakaiproject.component.api.ServerConfigurationService service = getInstance();
      if (service == null) return null;

      return service.getSmtpPort();
   }

   public static String getSmtpServer() {
      org.sakaiproject.component.api.ServerConfigurationService service = getInstance();
      if (service == null) return null;

      return service.getSmtpServer();
   }

}
