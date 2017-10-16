/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007 The Sakai Foundation.
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

package uk.ac.cam.caret.sakai.rwiki.tool;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import uk.ac.cam.caret.sakai.rwiki.tool.command.Dispatcher;

/**
 * @author ieb
 *
 */
public class MapDispatcher implements Dispatcher
{
	Map<String, String> targets = new HashMap<String,String>();
	Map<String, Dispatcher> dispatchers = new HashMap<String, Dispatcher>();
	private Dispatcher defaultDispatcher = new DefaultRequestDispatcher();
	private VelocityInlineDispatcher velocityDispatcher = new VelocityInlineDispatcher();
	
	public MapDispatcher(ServletContext context) throws ServletException {
		velocityDispatcher.init(context);
		targets.put("/WEB-INF/command-pages/diff.jsp", "/WEB-INF/vm/diff");
		targets.put("/WEB-INF/command-pages/edit.jsp", "/WEB-INF/vm/edit");
		targets.put("/WEB-INF/command-pages/editRealm-many.jsp", "/WEB-INF/vm/editRealm-many");
		targets.put("/WEB-INF/command-pages/editRealm-manyv2.jsp", "/WEB-INF/vm/editRealm-manyv2");
		targets.put("/WEB-INF/command-pages/editRealm.jsp", "/WEB-INF/vm/editRealm");
		targets.put("/WEB-INF/command-pages/fragmentpreview.jsp", "/WEB-INF/vm/fragmentpreview");
		targets.put("/WEB-INF/command-pages/fragmentview.jsp", "/WEB-INF/vm/fragmentview");
		targets.put("/WEB-INF/command-pages/full_search.jsp", "/WEB-INF/vm/full_search");
		targets.put("/WEB-INF/command-pages/history.jsp", "/WEB-INF/vm/history");
		targets.put("/WEB-INF/command-pages/info.jsp", "/WEB-INF/vm/info");
		targets.put("/WEB-INF/command-pages/permission.jsp", "/WEB-INF/vm/permission");
		targets.put("/WEB-INF/command-pages/preferences.jsp", "/WEB-INF/vm/preferences");
		targets.put("/WEB-INF/command-pages/preview.jsp", "/WEB-INF/vm/preview");
		targets.put("/WEB-INF/command-pages/publicview.jsp", "/WEB-INF/vm/publicview");
		targets.put("/WEB-INF/command-pages/review.jsp", "/WEB-INF/vm/review");
		targets.put("/WEB-INF/command-pages/search.jsp", "/WEB-INF/vm/search");
		targets.put("/WEB-INF/command-pages/title.jsp", "/WEB-INF/vm/title");
		targets.put("/WEB-INF/command-pages/view.jsp", "/WEB-INF/vm/view");

		dispatchers.put("/WEB-INF/command-pages/diff.jsp",velocityDispatcher); 
		dispatchers.put("/WEB-INF/command-pages/edit.jsp",velocityDispatcher);
		dispatchers.put("/WEB-INF/command-pages/editRealm-many.jsp", velocityDispatcher);
		dispatchers.put("/WEB-INF/command-pages/editRealm-manyv2.jsp", velocityDispatcher);
		dispatchers.put("/WEB-INF/command-pages/editRealm.jsp", velocityDispatcher);
		dispatchers.put("/WEB-INF/command-pages/fragmentpreview.jsp", velocityDispatcher);
		dispatchers.put("/WEB-INF/command-pages/fragmentview.jsp", velocityDispatcher);
		dispatchers.put("/WEB-INF/command-pages/full_search.jsp", velocityDispatcher);
		dispatchers.put("/WEB-INF/command-pages/history.jsp", velocityDispatcher);
		dispatchers.put("/WEB-INF/command-pages/info.jsp", velocityDispatcher);
		dispatchers.put("/WEB-INF/command-pages/permission.jsp", velocityDispatcher);
		dispatchers.put("/WEB-INF/command-pages/preferences.jsp", velocityDispatcher);
		dispatchers.put("/WEB-INF/command-pages/preview.jsp", velocityDispatcher);
		dispatchers.put("/WEB-INF/command-pages/publicview.jsp", velocityDispatcher);
		dispatchers.put("/WEB-INF/command-pages/review.jsp", velocityDispatcher);
		dispatchers.put("/WEB-INF/command-pages/search.jsp", velocityDispatcher);
		dispatchers.put("/WEB-INF/command-pages/title.jsp", velocityDispatcher);
		dispatchers.put("/WEB-INF/command-pages/view.jsp",  velocityDispatcher);
		
	}

	/* (non-Javadoc)
	 * @see uk.ac.cam.caret.sakai.rwiki.tool.command.Dispatcher#dispatch(java.lang.String, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	public void dispatch(String path, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		String targetpath = targets.get(path);
		if ( targetpath == null ) {
			targetpath = path;
		}
		
		Dispatcher dispatcher = dispatchers.get(path);
		if ( dispatcher == null ) {
			dispatcher = defaultDispatcher;
		}
		dispatcher.dispatch(targetpath, request, response);
	}

}
