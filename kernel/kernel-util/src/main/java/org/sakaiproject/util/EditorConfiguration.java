/**
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2007, 2008 Sakai Foundation
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
 
package org.sakaiproject.util;

import java.lang.reflect.Method;

import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.cover.SessionManager;

/**
 * EditorConfiguration is a utility class that provides methods to access
 * information that is relevant to the configuration of a rich-text editor.
 */
public class EditorConfiguration 
{
	public static final String ATTR_ENABLE_RESOURCE_SEARCH = "org.sakaiproject.util.EditorConfiguration.enableResourceSearch";

	/**
	 * Access the identifier for the editor currently in use.  This value is
	 * supplied by the ServerConfigurationService and uniquely identifies a 
	 * particular editor supported by Sakai. 
	 * @return The unique identifier for the editor as specified in 
	 * "sakai.properties". 
	 */
	public static String getWysiwigEditor()
	{
		return ServerConfigurationService.getString("wysiwyg.editor");
	}

	/**
	 * Get the file browser that should be used when picking a file in CKEditor.
	 * @return The file browser, i.e. fckeditor, elfinder
	 */
	public static String getCKEditorFileBrowser()
	{
		return ServerConfigurationService.getString("wysiwyg.editor.ckeditor.browser", "elfinder");
	}
	
	/**
	 * Determine whether the CitationsService is fully configured to enable 
	 * this user to search library resources and add search results as citations 
	 * in the document in the rich-text editor. 
	 * @return true if this user may use the resource-search plug-in in the editor
	 * to search library resources and add search results as citations in the 
	 * document in the editor, false otherwise. 
	 */
	public static boolean enableResourceSearch()
	{
		Session session = SessionManager.getCurrentSession();
		Boolean showCitationsButton = (Boolean) session.getAttribute(ATTR_ENABLE_RESOURCE_SEARCH);
		
		if(showCitationsButton == null)
		{
			Object component = ComponentManager.get("org.sakaiproject.citation.api.ConfigurationService");
			if(component == null)
			{
				// if the service can't be found, return FALSE
				showCitationsButton = Boolean.FALSE;
			}
			else
			{
				try
				{
					Method method = component.getClass().getMethod("librarySearchEnabled", new Class[]{});
					
					if(method == null)
					{
						// if the method can't be invoked, return FALSE
						showCitationsButton = Boolean.FALSE;
					}
					else
					{
						showCitationsButton = (Boolean) method.invoke(component, null);
						session.setAttribute(ATTR_ENABLE_RESOURCE_SEARCH, showCitationsButton);
					}
				}
				catch(Exception e)
				{
					// if the method can't be invoked, return FALSE
					showCitationsButton = Boolean.FALSE;
				} 
			}
		}
		
		return showCitationsButton.booleanValue();
	}

}
