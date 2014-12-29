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

import uk.ac.cam.caret.sakai.rwiki.tool.api.HttpCommand;

/**
 * The command that simply dispatches to the set servletPath.
 * 
 * @author andrew
 */
public class SimpleCommand implements HttpCommand
{

	private String servletPath;

	public void execute(Dispatcher dispatcher, HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException
	{
		// So far we need do nothing except dispatch
		dispatcher.dispatch(servletPath, request, response);

	}

	/**
	 * @return the path to the jsp file
	 */
	public String getServletPath()
	{
		return servletPath;
	}

	/**
	 * @param servletPath
	 *        the path to the jsp file
	 */
	public void setServletPath(String servletPath)
	{
		this.servletPath = servletPath;
	}

}
