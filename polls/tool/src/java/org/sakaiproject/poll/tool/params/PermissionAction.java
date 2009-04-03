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
 *       http://www.osedu.org/licenses/ECL-2.0
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
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.cover.AuthzGroupService;
import org.sakaiproject.authz.api.Role;
import org.sakaiproject.authz.api.AuthzPermissionException;
import org.sakaiproject.authz.api.GroupNotDefinedException;
import org.sakaiproject.tool.cover.ToolManager;

import org.sakaiproject.poll.logic.PollListManager;

public class PermissionAction {

	private static Log m_log = LogFactory.getLog(PermissionAction.class);
	public Map perms = null;
	public String submissionStatus;
	
	public void setRoleperms(Map perms)
	{
		this.perms = perms;
	} 
	
	
	public String setPermissions()
	{
		
		  if ("cancel".equals(submissionStatus))
			  return "cancel";
		  
		  m_log.info("Seting permissions");
			if (perms == null)
				m_log.error("My perms Map is null");
			else
			{
				AuthzGroup authz = null;
				try {
					 authz = AuthzGroupService.getAuthzGroup("/site/" + ToolManager.getCurrentPlacement().getContext());
				}
				catch (GroupNotDefinedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return "error";
				}
				for (Iterator i = perms.keySet().iterator(); i.hasNext();)
				{	
					String key = (String) i.next();
					Role role = authz.getRole(key);
					try {
					  PollRolePerms rp = (PollRolePerms) perms.get(key);
					  if (rp.add != null )
						  setFunc(role,PollListManager.PERMISSION_ADD,rp.add);
					  if (rp.deleteAny != null )
						  setFunc(role,PollListManager.PERMISSION_DELETE_ANY, rp.deleteAny);
					  if (rp.deleteOwn != null )
						  setFunc(role,PollListManager.PERMISSION_DELETE_OWN,rp.deleteOwn);
					  if (rp.editAny != null )
						  setFunc(role,PollListManager.PERMISSION_EDIT_ANY,rp.editAny);
					  if (rp.editOwn != null )
						  setFunc(role,PollListManager.PERMISSION_EDIT_OWN,rp.editOwn);
					  if (rp.vote != null )
						  setFunc(role,PollListManager.PERMISSION_VOTE,rp.vote);
					  
					  m_log.info(" Key: " + key + " Vote: " + rp.vote + " New: " + rp.add );
					}
					  catch(Exception e)
					{
						m_log.error(" ClassCast Ex PermKey: " + key);
						e.printStackTrace();
						return "error";
					}
				}
				try {
					AuthzGroupService.save(authz);
				}
				catch (GroupNotDefinedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return "error";
				}
				catch (AuthzPermissionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return "error";
				}
				
	  
			}
			return "Success";
	}
	
	private void setFunc(Role role, String function, Boolean allow)
	{
		
			//m_log.debug("Setting " + function + " to " + allow.toString() + " for " + rolename + " in /site/" + ToolManager.getCurrentPlacement().getContext());
			if (allow.booleanValue())
				role.allowFunction(function);
			else
				role.disallowFunction(function);
			
	} 

			
	  public String cancel() {
		  return "cancel";
	  }
	  		 
		
	
	
}