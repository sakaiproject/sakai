/**********************************************************************************
 * $URL: $
 * $Id:  $
 ***********************************************************************************
 *
 * Copyright (c) 2006, 2007, 2008 The Sakai Foundation
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

package org.sakaiproject.poll.tool.params;

import java.util.Map;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.poll.logic.ExternalLogic;

@Slf4j
public class PermissionAction {

	public Map perms = null;
	public String submissionStatus;
	
	public void setRoleperms(Map perms)
	{
		this.perms = perms;
	} 
	
	private ExternalLogic externalLogic;    
	public void setExternalLogic(ExternalLogic externalLogic) {
		this.externalLogic = externalLogic;
	}
	
	public String setPermissions()
	{
		
		  if ("cancel".equals(submissionStatus))
			  return "cancel";
		  
		  log.info("Seting permissions");
			if (perms == null)
				log.error("My perms Map is null");
			else
			{
				try {
					externalLogic.setToolPermissions(perms, externalLogic.getCurrentLocationReference());
				}
				catch (SecurityException e) {
					log.error(e.getMessage(), e);
					return "error";
				}
				catch (IllegalArgumentException e) {
					log.error(e.getMessage(), e);
					return "error";
				}
	  
			}
			return "Success";
	}

			
	  public String cancel() {
		  return "cancel";
	  }

}
