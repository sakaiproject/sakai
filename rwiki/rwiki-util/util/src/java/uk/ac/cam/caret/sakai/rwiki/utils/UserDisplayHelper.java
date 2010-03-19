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

package uk.ac.cam.caret.sakai.rwiki.utils;

import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.user.api.ContextualUserDisplayService;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.user.cover.UserDirectoryService;

public class UserDisplayHelper
{

	private static boolean displayID = ServerConfigurationService.getBoolean("wiki.display.user.id", false);
	
	public static String formatDisplayName(String name, String context)
	{
		return formatDisplayName(name, Messages.getString("UserDisplayHelper.0"), context); //$NON-NLS-1$
	}

	public static String formatDisplayName(String name, String defaultName, String context)
	{
		if ( name == null ) {
			return defaultName;
		}
		User user;
		try
		{
			user = UserDirectoryService.getUser(name);
		}
		catch (UserNotDefinedException e)
		{
			return defaultName + " (" + XmlEscaper.xmlEscape(name) + ")"; //$NON-NLS-1$ //$NON-NLS-2$
		}
		
		ContextualUserDisplayService contextualUserDisplayService = (ContextualUserDisplayService) ComponentManager.get("org.sakaiproject.user.api.ContextualUserDisplayService");
		
		if ( displayID ) {
			if (context != null && contextualUserDisplayService != null)
			{
				String userDisplayName =  contextualUserDisplayService.getUserDisplayName(user, context);
				String userDisplayId =  contextualUserDisplayService.getUserDisplayId(user, context);
				
				if (userDisplayName == null)
					userDisplayName = user.getDisplayName();
				
				if (userDisplayId == null)
					userDisplayId = user.getDisplayId();
				
				return XmlEscaper.xmlEscape(userDisplayName + " (" + userDisplayId + ")"); //$NON-NLS-1$ //$NON-NLS-2$
			}
			else
			{
				return XmlEscaper.xmlEscape(user.getDisplayName() + " (" + user.getDisplayId() + ")"); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
		else
		{
			if (context != null && contextualUserDisplayService != null && !(contextualUserDisplayService.getUserDisplayName(user, context) == null))
				return XmlEscaper.xmlEscape(contextualUserDisplayService.getUserDisplayName(user, context)); //$NON-NLS-1$ //$NON-NLS-2$
			else 
				return XmlEscaper.xmlEscape(user.getDisplayName()); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}
	
	

}
