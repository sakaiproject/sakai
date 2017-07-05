/**********************************************************************************
 * $URL: $
 * $Id: $
 ***********************************************************************************
 *
 * Copyright (c) 2010 The Sakai Foundation
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

package org.sakaiproject.editor;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.portal.api.Editor;
import org.sakaiproject.portal.api.EditorRegistry;
import org.sakaiproject.portal.api.PortalService;
import org.sakaiproject.portal.util.ErrorReporter;
import org.sakaiproject.portal.util.URLUtils;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.util.EditorConfiguration;
import org.sakaiproject.util.StringUtil;
import org.sakaiproject.portal.util.CSSUtils;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EditorServlet extends HttpServlet
{

	public static final String EDITOR_BOOTSTRAP_JS = "editor-bootstrap.js";
	public static final String EDITOR_JS = "editor.js";
	public static final String EDITOR_LAUNCH_JS = "editor-launch.js";
	
	public static final PortalService portalService = (PortalService) ComponentManager.get(PortalService.class);
	public static final EditorRegistry editorRegistry = (EditorRegistry) ComponentManager.get(EditorRegistry.class);

	/**
	 * Access the Servlet's information display.
	 * 
	 * @return servlet information.
	 */
	@Override
	public String getServletInfo()
	{
		return "Sakai Rich-text Editor Support";
	}

	/**
	 * Initialize the servlet.
	 * 
	 * @param config
	 *        The servlet config.
	 * @throws ServletException
	 */
	@Override
	public void init(ServletConfig config) throws ServletException
	{
		super.init(config);

		log.info("init()");
	}

	/**
	 * Shutdown the servlet.
	 */
	@Override
	public void destroy()
	{
		log.info("destroy()");

		super.destroy();
	}

	/**
	 * Respond to navigation / access requests.
	 * 
	 * @param req
	 *        The servlet request.
	 * @param res
	 *        The servlet response.
	 * @throws ServletException.
	 * @throws IOException.
	 */
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse res)
			throws ServletException, IOException
	{
		try
		{
			// this is either going to be editor.js or editor-launch.js
			String path = URLUtils.getSafePathInfo(req);
			if ((path == null) || (path.length() <= 1)) throw new Exception("no path");

			// get the requested file, ignoring the first "/"
			String[] parts = StringUtil.splitFirst(path.substring(1), "/");
			String name = parts[0];
			
			String placementId = req.getParameter("placement");
			ToolConfiguration tool = SiteService.findTool(placementId);
			String skin = (tool == null) ? null : tool.getSkin();
			
			Editor editor = portalService.getActiveEditor(tool);

			if (EDITOR_JS.equals(name)) {
				res.sendRedirect(editor.getEditorUrl());
			}
			else if (EDITOR_LAUNCH_JS.equals(name)) {
				res.sendRedirect(editor.getLaunchUrl());
			}
			else if (EDITOR_BOOTSTRAP_JS.equals(name)) {
				res.addHeader("Pragma", "no-cache");
				res.addHeader("Cache-Control", "no-cache");
				res.addHeader("Content-Type", "text/javascript");
				
				//Note that this is the same stuff as in SkinnableCharonPortal. We should probably do a bit of refactoring.
				PrintWriter out = res.getWriter();
				out.print("<!-- EditorServlet -->\n");
				out.print("var sakai = sakai || {}; sakai.editor = sakai.editor || {}; " +
						"sakai.editor.editors = sakai.editor.editors || {}; " +
						"sakai.editor.editors.ckeditor = sakai.editor.editors.ckeditor || {}; " +
						"\n");
				Site site = null;
				if ( tool != null ) {
					String siteId = tool.getSiteId();
					try {
						site = SiteService.getSiteVisit(siteId);
					}
					catch (IdUnusedException e) {
						site = null;
					}
					catch (PermissionException e) {
						site = null;
					}
				}

				String contentItemUrl = portalService.getContentItemUrl(site);
				if ( contentItemUrl != null ) {
					out.print("sakai.editor.contentItemUrl = '" + contentItemUrl + "';\n");
				} else {
					out.print("sakai.editor.contentItemUrl = false;\n");
				}
				out.print("sakai.editor.collectionId = '" + portalService.getBrowserCollectionId(tool) + "';\n");
				out.print("sakai.editor.enableResourceSearch = '" + EditorConfiguration.enableResourceSearch() + "';\n");
				out.print("sakai.editor.editors.ckeditor.browser = '" + EditorConfiguration.getCKEditorFileBrowser() + "';\n");
				out.print("sakai.editor.siteToolSkin = '" + CSSUtils.getCssToolSkin(skin) + "';\n");
				out.print("sakai.editor.sitePrintSkin = '" + CSSUtils.getCssPrintSkin(skin) + "';\n");

				out.print(editor.getPreloadScript());
			}
			else {
				throw new Exception("unrecognized request");
			}

		}
		catch (Throwable t)
		{
			doError(req, res, t);
		}
	}

	protected void doError(HttpServletRequest req, HttpServletResponse res, Throwable t)
	{
		ErrorReporter err = new ErrorReporter();
		err.report(req, res, t);
	}

	/**
	 * Respond to data posting requests.
	 * 
	 * @param req
	 *        The servlet request.
	 * @param res
	 *        The servlet response.
	 * @throws ServletException.
	 * @throws IOException.
	 */
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse res)
			throws ServletException, IOException
	{
		doGet(req, res);
	}
}
