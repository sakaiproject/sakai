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

import uk.ac.cam.caret.sakai.rwiki.tool.RequestScopeSuperBean;
import uk.ac.cam.caret.sakai.rwiki.tool.bean.helper.ViewParamsHelperBean;
import uk.ac.cam.caret.sakai.rwiki.utils.NameHelper;

/**
 * @author ieb
 */
public class CommentSaveCommand extends SaveCommand
{

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.cam.caret.sakai.rwiki.tool.command.SaveCommand#successfulUpdateDispatch(javax.servlet.http.HttpServletRequest,
	 *      javax.servlet.http.HttpServletResponse)
	 */
	protected void successfulUpdateDispatch(Dispatcher dispatcher,HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException
	{
		RequestScopeSuperBean rssb = RequestScopeSuperBean
				.getFromRequest(request);

		ViewParamsHelperBean vphb = (ViewParamsHelperBean) rssb
				.getNameHelperBean();

		String localName = NameHelper.localizeName(vphb.getGlobalName(), vphb
				.getPageSpace());
		String globalName = vphb.getGlobalName();
		int baseNameI = localName.indexOf(".");
		String baseName = localName;
		if (baseNameI > 0)
		{
			int nextI = localName.indexOf(".",baseNameI+1);
			baseName = null;
			while (nextI > 0 && baseName == null)
			{
				try
				{
					String test = localName.substring(baseNameI + 1, nextI);
					Integer.parseInt(localName.substring(baseNameI + 1, nextI));
					baseName = localName.substring(0, baseNameI);
				}
				catch (NumberFormatException e)
				{
					baseNameI = nextI;
					nextI = localName.indexOf(".", baseNameI + 1);
				}
			}
			if ( baseName == null ) {
				try
				{
					String test = localName.substring(baseNameI + 1);
					Integer.parseInt(localName.substring(baseNameI + 1));
					baseName = localName.substring(0, baseNameI);
				}
				catch (NumberFormatException e)
				{
					baseName = localName;
				}
			}
		}
		globalName = NameHelper.globaliseName(baseName, vphb.getPageSpace());
		vphb.setGlobalName(globalName);
		// force a refresh
		rssb.getCurrentPageName(true);
		rssb.getCurrentRWikiObject(true);
		super.successfulUpdateDispatch(dispatcher,request, response);
	}

}
