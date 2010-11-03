/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006 The Sakai Foundation.
 *
 * Licensed under the Educational Community License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.opensource.org/licenses/ecl1.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package uk.ac.cam.caret.sakai.rwiki.tool.bean.helper;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.servlet.ServletRequest;

import org.sakaiproject.thread_local.cover.ThreadLocalManager;

import uk.ac.cam.caret.sakai.rwiki.service.api.RWikiObjectService;
import uk.ac.cam.caret.sakai.rwiki.service.api.RWikiSecurityService;
import uk.ac.cam.caret.sakai.rwiki.tool.bean.EditBean;
import uk.ac.cam.caret.sakai.rwiki.tool.bean.SearchBean;
import uk.ac.cam.caret.sakai.rwiki.tool.bean.ToolConfigBean;
import uk.ac.cam.caret.sakai.rwiki.tool.bean.ViewBean;
import uk.ac.cam.caret.sakai.rwiki.utils.NameHelper;

/**
 * Bean to get common request parameters from the servlet request.
 * 
 * @author andrew
 */
public class ViewParamsHelperBean
{

	private static final String SMALL_CHANGE_PARAM = "smallchange";

	private static final Object SMALL_CHANGE = "smallchange";

	public static final String SAVE_OK = "save-ok";

	public static final String SAVE_VERSION_EXCEPTION = "save-versionexcep";

	public static final String SAVE_CANCEL = "save-cancel";

	public static final String SAVE_PREVIEW = "save-preview";

	private static final String[] AUTOSAVE_REMOVE = { SAVE_OK, SAVE_CANCEL };

	private static final String[] AUTOSAVE_NORECOVER = { SAVE_VERSION_EXCEPTION, SAVE_PREVIEW };

	/**
	 * the requested global page name
	 */
	private String globalName;

	/**
	 * the local space
	 */
	private String localSpace;

	/**
	 * the requested search criteria
	 */
	private String search;

	/**
	 * The search Page
	 */
	private String searchPage = "0";

	/**
	 * the current servlet request
	 */
	private ServletRequest request;

	/**
	 * the RWikiSecurityService
	 */
	private RWikiSecurityService securityService;
	
	/**
	 * the Tool Config Bean
	 */
	private ToolConfigBean toolConfigBean;

	/**
	 * the default realm
	 */
	private String defaultRealm;

	/**
	 * the submitted content
	 */
	private String content;

	/**
	 * the submitted previous content
	 */
	private String submittedContent;

	/**
	 * the submitted version
	 */
	private String submittedVersion;

	/**
	 * the submitted save type
	 */
	private String saveType;

	/**
	 * breadcrumbs
	 */
	private String withBreadcrumbs;

	private String saveState = "";

	/**
	 * Initializes the bean, gets the parameters out of the request
	 */
	public void init()
	{
		String pageName = request.getParameter(ViewBean.PAGE_NAME_PARAM);

		localSpace = request.getParameter(SearchBean.REALM_PARAM);

		defaultRealm = securityService.getSiteReference();

		if (localSpace == null || "".equals(localSpace))
		{
			localSpace = defaultRealm;
		}
		
		if (pageName == null || "".equals(pageName)) {
			pageName = toolConfigBean.getHomePage();
		}

		globalName = NameHelper.globaliseName(pageName, localSpace);

		search = request.getParameter(SearchBean.SEARCH_PARAM);

		searchPage = request.getParameter(SearchBean.PAGE_PARAM);

		content = request.getParameter(EditBean.CONTENT_PARAM);

		submittedContent = request
				.getParameter(EditBean.SUBMITTED_CONTENT_PARAM);

		submittedVersion = request.getParameter(EditBean.VERSION_PARAM);

		saveType = getSaveTypeFromParameters(request.getParameterMap());

		String smallChange = request.getParameter(SMALL_CHANGE_PARAM);
		if (smallChange != null && smallChange.equals(SMALL_CHANGE))
		{
			ThreadLocalManager.set(RWikiObjectService.SMALL_CHANGE_IN_THREAD,
					RWikiObjectService.SMALL_CHANGE_IN_THREAD);
		}

		if (saveType != null)
		{
			saveType = saveType.toLowerCase();
		}

		withBreadcrumbs =  "0".equals(request.getParameter(ViewBean.PARAM_BREADCRUMB_NAME))?"0":"1";

	}
	
	 /**
	  * @param parameterMap
	  * @return
	  */
		public String getSaveTypeFromParameters(Map parameterMap)
		{
			Set<Entry<Object, Object>> entrySet = parameterMap.entrySet();
			Iterator<Entry<Object, Object>> it = entrySet.iterator();
			while (it.hasNext()) {
				Entry entry = it.next();
				Object key = entry.getKey();
				if ( String.valueOf(key).startsWith("command_") ) {
					Object value = entry.getValue();
					if ( value != null && String.valueOf(parameterMap.get(key)).trim().length() > 0 ) {
						return String.valueOf(key).substring("command_".length());
					}
				}
			}
			return null;
		}

	/**
	 * Get the globalised page name
	 * 
	 * @return the requested globalised page name
	 */
	public String getGlobalName()
	{
		return globalName;
	}

	/**
	 * Set the globalised page name
	 * 
	 * @param globalName
	 */
	public void setGlobalName(String globalName)
	{
		this.globalName = globalName;
	}

	/**
	 * Get the local space
	 * 
	 * @return local space
	 */
	public String getLocalSpace()
	{
		return localSpace;
	}

	/**
	 * Set the local space
	 * 
	 * @param localSpace
	 */
	public void setLocalSpace(String localSpace)
	{
		this.localSpace = localSpace;

	}

	/**
	 * Get the current page's space
	 * 
	 * @return space relating to globalName
	 */
	public String getPageSpace()
	{
		return NameHelper.localizeSpace(globalName, localSpace);
	}

	/**
	 * Get the localised name relating to globalName and localSpace
	 * 
	 * @return the localised name
	 */
	public String getLocalName()
	{
		return NameHelper.localizeName(globalName, localSpace);
	}

	/**
	 * Get the submitted search criteria
	 * 
	 * @return search criteria
	 */
	public String getSearch()
	{
		return search;
	}

	/**
	 * Set the search criteria
	 * 
	 * @param search
	 *        criteria to set
	 */
	public void setSearch(String search)
	{
		this.search = search;
	}

	/**
	 * Get the current servlet request
	 * 
	 * @return current servlet request
	 */
	public ServletRequest getServletRequest()
	{
		return request;
	}

	/**
	 * Set the current servlet request
	 * 
	 * @param request
	 */
	public void setServletRequest(ServletRequest request)
	{
		this.request = request;
	}

	/**
	 * Get the RWikiSecurityService
	 * 
	 * @return the rwikiSecurityService
	 */
	public RWikiSecurityService getSecurityService()
	{
		return securityService;
	}

	/**
	 * Set the RWikiSecurityService to be used in init
	 * 
	 * @param securityService
	 */
	public void setSecurityService(RWikiSecurityService securityService)
	{
		this.securityService = securityService;
	}

	/**
	 * Get the default realm
	 * 
	 * @return default realm for the current request
	 */
	public String getDefaultRealm()
	{
		return defaultRealm;
	}

	/**
	 * Set the default realm
	 * 
	 * @param defaultRealm
	 */
	public void setDefaultRealm(String defaultRealm)
	{
		this.defaultRealm = defaultRealm;
	}

	/**
	 * Get the submitted content
	 * 
	 * @return content
	 */
	public String getContent()
	{
		return content;
	}

	/**
	 * Set the content
	 * 
	 * @param content
	 */
	public void setContent(String content)
	{
		this.content = content;
	}

	/**
	 * Get the previously submitted content
	 * 
	 * @return content as a String
	 */
	public String getSubmittedContent()
	{
		return submittedContent;
	}

	/**
	 * Set the previously submitted content
	 * 
	 * @param submittedContent
	 */
	public void setSubmittedContent(String submittedContent)
	{
		this.submittedContent = submittedContent;
	}

	/**
	 * Get the submitted version number
	 * 
	 * @return version as string
	 */
	public String getSubmittedVersion()
	{
		return submittedVersion;
	}

	/**
	 * Set the submitted version number
	 * 
	 * @param submittedVersion
	 */
	public void setSubmittedVersion(String submittedVersion)
	{
		this.submittedVersion = submittedVersion;
	}

	/**
	 * Get the requested save type
	 * 
	 * @return save type as string
	 */
	public String getSaveType()
	{
		return saveType;
	}

	/**
	 * Set the save type
	 * 
	 * @param saveType
	 */
	public void setSaveType(String saveType)
	{
		if (saveType != null)
		{
			this.saveType = saveType.toLowerCase();
		}
		else
		{
			this.saveType = null;
		}
	}

	/**
	 * @return Returns the withBreadcrumbs.
	 */
	public String getWithBreadcrumbs()
	{
		return withBreadcrumbs;
	}

	/**
	 * @param withBreadcrumbs
	 *        The withBreadcrumbs to set.
	 */
	public void setWithBreadcrumbs(String withBreadcrumbs)
	{
		this.withBreadcrumbs = withBreadcrumbs;
	}

	/**
	 * @return Returns the searchPage.
	 */
	public String getSearchPage()
	{
		return searchPage;
	}

	/**
	 * @param searchPage
	 *        The searchPage to set.
	 */
	public void setSearchPage(String searchPage)
	{
		this.searchPage = searchPage;
	}

	public ToolConfigBean getToolConfigBean()
	{
		return toolConfigBean;
	}

	public void setToolConfigBean(ToolConfigBean toolConfigBean)
	{
		this.toolConfigBean = toolConfigBean;
	}

	public boolean isRemoveAutoSave()
	{
		for ( int i = 0; i < AUTOSAVE_REMOVE.length; i++ ) {
			if ( AUTOSAVE_REMOVE[i].equals(saveState) ) {
				return true;
			}
		}
		return false;
	}
	public boolean isLoadAutoSave()
	{
		for ( int i = 0; i < AUTOSAVE_NORECOVER.length; i++ ) {
			if ( AUTOSAVE_NORECOVER[i].equals(saveState) ) {
				return false;
			}
		}
		return true;
	}

	/**
	 * @return the saveState
	 */
	public String getSaveState()
	{
		return saveState;
	}

	/**
	 * @param saveState the saveState to set
	 */
	public void setSaveState(String saveState)
	{
		this.saveState = saveState;
	}


}
