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

package uk.ac.cam.caret.sakai.rwiki.tool.command;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.AuthzPermissionException;
import org.sakaiproject.authz.api.GroupNotDefinedException;
import org.sakaiproject.authz.api.Role;
import org.sakaiproject.component.api.ComponentManager;
import org.sakaiproject.tool.cover.SessionManager;

import uk.ac.cam.caret.sakai.rwiki.service.api.RWikiSecurityService;
import uk.ac.cam.caret.sakai.rwiki.tool.RWikiServlet;
import uk.ac.cam.caret.sakai.rwiki.tool.RequestScopeSuperBean;
import uk.ac.cam.caret.sakai.rwiki.tool.api.HttpCommand;
import uk.ac.cam.caret.sakai.rwiki.tool.bean.AuthZGroupEditBean;
import uk.ac.cam.caret.sakai.rwiki.tool.bean.ErrorBean;
import uk.ac.cam.caret.sakai.rwiki.tool.bean.ResourceLoaderBean;
import uk.ac.cam.caret.sakai.rwiki.tool.bean.ViewBean;
import uk.ac.cam.caret.sakai.rwiki.tool.bean.helper.ViewParamsHelperBean;

/**
 * @author andrew
 */
@Slf4j
public class EditAuthZGroupCommand implements HttpCommand
{
	private String editRealmPath;

	private String cancelEditPath;

	private String successfulPath;

	private String permissionPath;

	private String unknownRealmPath;

	private String idInUsePath;

	private AuthzGroupService realmService;

	public void init()
	{
		ComponentManager cm = org.sakaiproject.component.cover.ComponentManager
				.getInstance();
		realmService = (AuthzGroupService) load(cm, AuthzGroupService.class
				.getName());
	}

	private Object load(ComponentManager cm, String name)
	{
		Object o = cm.get(name);
		if (o == null)
		{
			log.error("Cant find Spring component named " + name);
		}
		return o;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.cam.caret.sakai.rwiki.service.api.HttpCommand#execute(javax.servlet.http.HttpServletRequest,
	 *      javax.servlet.http.HttpServletResponse)
	 */
	public void execute(Dispatcher dispatcher, HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException
	{

		RequestScopeSuperBean rssb = RequestScopeSuperBean
				.getFromRequest(request);

		ViewParamsHelperBean vphb = rssb.getNameHelperBean();
		AuthZGroupEditBean realmEditBean = rssb.getRealmEditBean();
		String requestedRealmId = realmEditBean.getLocalSpace();

		String saveType = vphb.getSaveType();

		AuthzGroup realmEdit;
		try
		{
			if (realmEditBean.getRealmEdit() == null)
			{
				realmEdit = realmService.getAuthzGroup(requestedRealmId);
				realmEditBean.setRealmEdit(realmEdit);

			}
			else
			{
				// Check the current realm edit is the same as the requested
				// one:
				realmEdit = realmEditBean.getRealmEdit();
				if (!requestedRealmId.equals(realmEdit.getId()))
				{
					// TODO: set errorBean and ignore current request i.e. go
					// back to start
					ErrorBean errorBean = rssb.getErrorBean();
					ResourceLoaderBean rlb = rssb.getResourceLoaderBean();
					
					errorBean.addError(rlb.getString("editauthz.currently_editing1", "You are currently editing: ")
							+ realmEdit.getId()
							+ rlb.getString("editauthz.currently_editing2", ". You must finish editing this realm first"));
					realmEditBean.setLocalSpace(realmEdit.getId());
					saveType = null;
				}
			}

			if (saveType == null || "".equals(saveType))
			{
				// Begin a realmEdit...
				editDispatch(dispatcher,request, response);
				return;
			}
			else if (saveType.equals(AuthZGroupEditBean.CANCEL_VALUE))
			{
				// cancel a realmEdit...
				// TODO: CHECK We beleive that this is no longer needed since
				// locking in authz group
				// is optimistic
				// realmService.cancelEdit(realmEdit);
				realmEditBean.setRealmEdit(null);
				cancelDispatch(dispatcher,request, response);

				String pageName = vphb.getGlobalName();
				String realm = vphb.getLocalSpace();
				ViewBean vb = new ViewBean(pageName, realm);
				String requestURL = request.getRequestURL().toString();
				SessionManager.getCurrentToolSession().setAttribute(
						RWikiServlet.SAVED_REQUEST_URL,
						requestURL + vb.getInfoUrl());

				return;
			}
			else if (saveType.equals(AuthZGroupEditBean.SAVE_VALUE))
			{
				// complete a realmEdit...
				Map requestMap = request.getParameterMap();

				for (Iterator it = realmEdit.getRoles().iterator(); it
						.hasNext();)
				{
					Role roleEdit = (Role) it.next();
					updateRoleEdit(roleEdit, requestMap);
				}

				realmService.save(realmEdit);
				realmEditBean.setRealmEdit(null);
				successfulDispatch(dispatcher,request, response);

				String pageName = vphb.getGlobalName();
				String realm = vphb.getLocalSpace();
				ViewBean vb = new ViewBean(pageName, realm);
				String requestURL = request.getRequestURL().toString();
				SessionManager.getCurrentToolSession().setAttribute(
						RWikiServlet.SAVED_REQUEST_URL,
						requestURL + vb.getInfoUrl());

			}
		}
		catch (GroupNotDefinedException e)
		{
			realmEditBean.setRealmEdit(null);
			unknownRealmDispatch(dispatcher,request, response);
			return;
		}
		catch (AuthzPermissionException e)
		{
			// redirect to permission denied page
			realmEditBean.setRealmEdit(null);
			permissionDeniedDispatch(dispatcher,request, response);
			return;
		}

	}

	public String getIdInUsePath()
	{
		return idInUsePath;
	}

	public void setIdInUsePath(String idInUsePath)
	{
		this.idInUsePath = idInUsePath;
	}

	public String getPermissionPath()
	{
		return permissionPath;
	}

	public void setPermissionPath(String permissionPath)
	{
		this.permissionPath = permissionPath;
	}

	public String getUnknownRealmPath()
	{
		return unknownRealmPath;
	}

	public void setUnknownRealmPath(String unknownRealmPath)
	{
		this.unknownRealmPath = unknownRealmPath;
	}

	public String getCancelEditPath()
	{
		return cancelEditPath;
	}

	public void setCancelEditPath(String cancelEditPath)
	{
		this.cancelEditPath = cancelEditPath;
	}

	public String getEditRealmPath()
	{
		return editRealmPath;
	}

	public void setEditRealmPath(String editRealmPath)
	{
		this.editRealmPath = editRealmPath;
	}

	public String getSuccessfulPath()
	{
		return successfulPath;
	}

	public void setSuccessfulPath(String successfulPath)
	{
		this.successfulPath = successfulPath;
	}

	private void successfulDispatch(Dispatcher dispatcher, HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException
	{
		dispatcher.dispatch(successfulPath, request, response );
	}


	private void cancelDispatch(Dispatcher dispatcher, HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException
	{
		dispatcher.dispatch(cancelEditPath, request, response );
	}

	private void editDispatch(Dispatcher dispatcher, HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException
	{
		dispatcher.dispatch(editRealmPath, request, response );
	}

	private void permissionDeniedDispatch(Dispatcher dispatcher, HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException
	{
		dispatcher.dispatch(permissionPath, request, response );
	}

	private void unknownRealmDispatch(Dispatcher dispatcher, HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException
	{
		dispatcher.dispatch(unknownRealmPath,request, response );
	}

	private void updateRoleEdit(Role roleEdit, Map map)
	{
		String id = roleEdit.getId();
		if (map.get("create_" + id) != null)
		{
			if (!roleEdit.isAllowed(RWikiSecurityService.SECURE_CREATE))
			{
				roleEdit.allowFunction(RWikiSecurityService.SECURE_CREATE);
			}
		}
		else
		{
			if (roleEdit.isAllowed(RWikiSecurityService.SECURE_CREATE))
			{
				roleEdit.disallowFunction(RWikiSecurityService.SECURE_CREATE);
			}
		}

		if (map.get("read_" + id) != null)
		{

			if (!roleEdit.isAllowed(RWikiSecurityService.SECURE_READ))
			{
				roleEdit.allowFunction(RWikiSecurityService.SECURE_READ);
			}
		}
		else
		{
			if (roleEdit.isAllowed(RWikiSecurityService.SECURE_READ))
			{
				roleEdit.disallowFunction(RWikiSecurityService.SECURE_READ);
			}
		}

		if (map.get("update_" + id) != null)
		{
			if (!roleEdit.isAllowed(RWikiSecurityService.SECURE_UPDATE))
			{
				roleEdit.allowFunction(RWikiSecurityService.SECURE_UPDATE);
			}
		}
		else
		{
			if (roleEdit.isAllowed(RWikiSecurityService.SECURE_UPDATE))
			{
				roleEdit.disallowFunction(RWikiSecurityService.SECURE_UPDATE);
			}
		}

		// if (requestMap.get("delete_" + id) != null) {
		// if (!roleEdit.contains(RWikiSecurityServiceImpl.SECURE_DELETE)) {
		// roleEdit.add(RWikiSecurityServiceImpl.SECURE_DELETE);
		// }
		// } else {
		// if (roleEdit.contains(RWikiSecurityServiceImpl.SECURE_DELETE)) {
		// roleEdit.remove(RWikiSecurityServiceImpl.SECURE_DELETE);
		// }
		// }

		if (map.get("admin_" + id) != null)
		{
			if (!roleEdit.isAllowed(RWikiSecurityService.SECURE_ADMIN))
			{
				roleEdit.allowFunction(RWikiSecurityService.SECURE_ADMIN);
			}
		}
		else
		{
			if (roleEdit.isAllowed(RWikiSecurityService.SECURE_ADMIN))
			{
				roleEdit.disallowFunction(RWikiSecurityService.SECURE_ADMIN);
			}
		}

		if (map.get("superadmin_" + id) != null)
		{
			if (!roleEdit.isAllowed(RWikiSecurityService.SECURE_SUPER_ADMIN))
			{
				roleEdit.allowFunction(RWikiSecurityService.SECURE_SUPER_ADMIN);
			}
		}
		else
		{
			if (roleEdit.isAllowed(RWikiSecurityService.SECURE_SUPER_ADMIN))
			{
				roleEdit
						.disallowFunction(RWikiSecurityService.SECURE_SUPER_ADMIN);
			}
		}
	}

}
