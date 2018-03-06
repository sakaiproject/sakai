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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.component.api.ComponentManager;
import org.sakaiproject.tool.cover.SessionManager;

import uk.ac.cam.caret.sakai.rwiki.service.message.api.PreferenceService;
import uk.ac.cam.caret.sakai.rwiki.tool.RWikiServlet;
import uk.ac.cam.caret.sakai.rwiki.tool.RequestScopeSuperBean;
import uk.ac.cam.caret.sakai.rwiki.tool.api.HttpCommand;
import uk.ac.cam.caret.sakai.rwiki.tool.bean.EditBean;
import uk.ac.cam.caret.sakai.rwiki.tool.bean.PreferencesBean;
import uk.ac.cam.caret.sakai.rwiki.tool.bean.ViewBean;

/**
 * @author andrew
 */
@Slf4j
public class UpdatePreferencesCommand implements HttpCommand
{

	private PreferenceService preferenceService;

	private String successfulPath;
	
	public void init()
	{
		ComponentManager cm = org.sakaiproject.component.cover.ComponentManager
				.getInstance();

		preferenceService = (PreferenceService) load(cm,
				PreferenceService.class.getName());
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
	 * @see uk.ac.cam.caret.sakai.rwiki.tool.api.HttpCommand#execute(javax.servlet.http.HttpServletRequest,
	 *      javax.servlet.http.HttpServletResponse)
	 */
	public void execute(Dispatcher dispatcher,HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException
	{

		RequestScopeSuperBean rssb = RequestScopeSuperBean
				.getFromRequest(request);

		ViewBean vb = rssb.getViewBean();
		EditBean eb = rssb.getEditBean();

		String userId = rssb.getCurrentUserId();
		String localSpace = vb.getLocalSpace();

		String notificationLevel = request
				.getParameter(PreferencesBean.NOTIFICATION_PREFERENCE_PARAM);

		if (!PreferenceService.SEPARATE_PREFERENCE.equals(notificationLevel)
				&& !PreferenceService.DIGEST_PREFERENCE
						.equals(notificationLevel)
				&& !PreferenceService.NONE_PREFERENCE.equals(notificationLevel))
		{
			notificationLevel = PreferencesBean.NO_PREFERENCE;
		}

		if (EditBean.SAVE_VALUE.equals(eb.getSaveType()))
		{
			if (PreferencesBean.NO_PREFERENCE.equals(notificationLevel))
			{
				preferenceService.deletePreference(userId, localSpace,
						PreferenceService.MAIL_NOTIFCIATION);
			}
			else
			{
				preferenceService.updatePreference(userId, localSpace,
						PreferenceService.MAIL_NOTIFCIATION, notificationLevel);
			}

		}
		this.successfulUpdateDispatch(dispatcher,request, response);

		String requestURL = request.getRequestURL().toString();
		SessionManager.getCurrentToolSession().setAttribute(
				RWikiServlet.SAVED_REQUEST_URL, requestURL + vb.getInfoUrl());
	}

	private void successfulUpdateDispatch(Dispatcher dispatcher, HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException
	{
		dispatcher.dispatch(successfulPath, request, response);
	}


	public String getSuccessfulPath()
	{
		return successfulPath;
	}

	public void setSuccessfulPath(String successfulPath)
	{
		this.successfulPath = successfulPath;
	}

}
