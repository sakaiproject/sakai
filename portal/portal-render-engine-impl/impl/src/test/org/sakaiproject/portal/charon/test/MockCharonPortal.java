/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006, 2007, 2008 The Sakai Foundation
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

package org.sakaiproject.portal.charon.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServlet;

import lombok.extern.slf4j.Slf4j;
import org.w3c.tidy.Tidy;

import org.sakaiproject.portal.api.PortalRenderContext;
import org.sakaiproject.portal.charon.velocity.VelocityPortalRenderEngine;

/**
 * <p>
 * This class operates as Mock portal to enable testing of a set of templates.
 * It can be used to drive any render engine, but is setup for the default
 * render engine. It is run as a unit test.
 * </p>
 */
@Slf4j
public class MockCharonPortal extends HttpServlet
{
	private VelocityPortalRenderEngine rengine;

	private File outputDir;

	private String outputFile;

	private Object resourceLoader;

	public MockCharonPortal(File outputDir) throws Exception
	{
		this.outputDir = outputDir;
		String renderEngineClass = VelocityPortalRenderEngine.class.getName();

		Class c = Class.forName(renderEngineClass);
		rengine = (VelocityPortalRenderEngine) c.newInstance();
		rengine.setPortalConfig("/testportalvelocity.config");
		rengine.setDebug(true);
		rengine.init();

	}


	public void doError(boolean withSession, boolean withToolSession) throws IOException
	{

		// start the response
		PortalRenderContext rcontext = startPageContext();
		
		
		if ( withSession ) {
			rcontext.put("s", new MockSession());
		}
		if ( withToolSession ) {
			rcontext.put("ts", new MockToolSession());
		}
		
		rcontext.put("req", new MockHttpServletRequest());

		showSession(rcontext);

		showSnoop(rcontext);

		sendResponse(rcontext, "error");
	}

	private void showSnoop(PortalRenderContext rcontext)
	{

		rcontext.put("snoopRequest", "snoopRequest");

		Map m = new HashMap();
		m.put("param1", "configparam1");
		m.put("param1", "configparam1");
		rcontext.put("snoopServletConfigParams", m);
		rcontext.put("snoopRequest", "snoopRequest");
		rcontext.put("snoopRequestHeaders", m);
		rcontext.put("snoopRequestParamsSingle", m);
		rcontext.put("snoopRequestParamsMulti", m);
		rcontext.put("snoopRequestAttr", m);
	}

	public void doGallery() throws IOException
	{
		PortalRenderContext rcontext = startPageContext();

		// the 'little' top area
		includeGalleryNav(rcontext);

		includeWorksite(rcontext);

		includeBottom(rcontext);

		sendResponse(rcontext, "gallery");
	}

	public void doNavLogin() throws IOException
	{
		// start the response
		PortalRenderContext rcontext = startPageContext();

		includeLogo(rcontext);

		sendResponse(rcontext, "login");
	}

	public void doPage() throws IOException
	{
		PortalRenderContext rcontext = startPageContext();

		includePage(rcontext);

		includeBottom(rcontext);

		sendResponse(rcontext, "page");
	}

	private PortalRenderContext startPageContext()
	{
		PortalRenderContext rcontext = rengine.newRenderContext(null);
		
		rcontext.put("pageSkinRepo", "skinRepo");
		rcontext.put("pageSkin", "skin");
		rcontext.put("pageTitle", "Web.escapeHtml(title)");
		rcontext.put("pageScriptPath", "getScriptPath()");
		rcontext.put("pageWebjarsPath", "getWebjarsPath()");
		rcontext.put("pageTop", Boolean.valueOf(true));
		rcontext.put("pageSiteType", "class=\"siteType\" ");
		rcontext.put("toolParamResetState", "PARM_STATE_RESET");
		rcontext.put("rloader", resourceLoader);
		
                String headCssToolBase = "<link href=\""
                        + "/tool_base.css\" type=\"text/css\" rel=\"stylesheet\" media=\"all\" />\n";
                String headCssToolSkin = "<link href=\"" 
                + "/tool.css\" type=\"text/css\" rel=\"stylesheet\" media=\"all\" />\n";
                String headCss = headCssToolBase + headCssToolSkin;
                String headJs = "<script type=\"text/javascript\" src=\"/library/js/headscripts.js\"></script>\n";
                String head = headCss + headJs;

                rcontext.put("sakai_html_head", head);
                rcontext.put("sakai_html_head_css", headCss);
                rcontext.put("sakai_html_head_css_base", headCssToolBase);
                rcontext.put("sakai_html_head_css_skin", headCssToolSkin);
                rcontext.put("sakai_html_head_js", headJs);

		rcontext.put("sitReset", "sitReset");
		//rcontext.put("browser", new BrowserDetector("Mozilla/5.0 (Macintosh; U; Intel Mac OS X; en) AppleWebKit/523.12.2 (KHTML, like Gecko) Version/3.0.4 Safari/523.12.2"));

		return rcontext;
	}

	public void doSite() throws IOException
	{
		PortalRenderContext rcontext = startPageContext();

		// the 'full' top area
		includeSiteNav(rcontext);

		includeWorksite(rcontext);

		includeBottom(rcontext);

		// end the response
		sendResponse(rcontext, "site");
	}

	public void doWorksite() throws IOException
	{

		PortalRenderContext rcontext = startPageContext();

		includeWorksite(rcontext);

		includeBottom(rcontext);

		// end the response
		sendResponse(rcontext, "worksite");
	}

	protected void includeBottom(PortalRenderContext rcontext)
	{

		rcontext.put("pagepopup", false);
		{
			List l = new ArrayList();
			l.add("bottomnav1");
			l.add("bottomnav2");
			rcontext.put("bottomNav", l);
		}

		rcontext.put("bottomNavSitNewWindow", "site_newwindow");
		{

			List l = new ArrayList();
			Map m = new HashMap();
			m.put("poweredByUrl", "poweredByUrl[i]");
			m.put("poweredByImage", "poweredByImage[i]");
			m.put("poweredByAltText", "poweredByAltText[i]");
			l.add(m);
			rcontext.put("bottomNavPoweredBy", l);

		}
		{
			List l = new ArrayList();
			Map m = new HashMap();
			m.put("poweredByUrl", "http://sakaiproject.org");
			m.put("poweredByImage", "/library/image/sakai_powered.gif");
			m.put("poweredByAltText", "Powered by Sakai");
			l.add(m);
			rcontext.put("bottomNavPoweredBy", l);
		}

		rcontext.put("bottomNavService", "service");
		rcontext.put("bottomNavCopyright", "copyright");
		rcontext.put("bottomNavServiceVersion", "serviceVersion");
		rcontext.put("bottomNavSakaiVersion", "sakaiVersion");
		rcontext.put("bottomNavServer", "server");
                rcontext.put("userWarning", Boolean.FALSE);

	}

	protected void includeGalleryLogin(PortalRenderContext rcontext) throws IOException
	{
		includeLogin(rcontext);
	}

	protected void includeGalleryNav(PortalRenderContext rcontext) throws IOException
	{
		rcontext.put("galleryHasAccessibilityURL", Boolean.valueOf(true));

		rcontext.put("galleryAccessibilityURL", "accessibilityURL");
		// rcontext.put("gallarySitAccessibility", "sit_accessibility");
		// rcontext.put("gallarySitJumpcontent", "sit_jumpcontent");
		// rcontext.put("gallarySitJumptools", "sit_jumptools");
		// rcontext.put("gallarySitJumpworksite", "sit_jumpworksite");
		rcontext.put("gallaryLoggedIn", Boolean.valueOf(true));
		includeTabs(rcontext);

	}

	protected void includeLogo(PortalRenderContext rcontext) throws IOException
	{
		rcontext.put("logoSkin", "skin");
		rcontext.put("logoSkinRepo", "skinRepo");
		rcontext.put("logoSiteType", "siteType");
		rcontext.put("logoSiteClass", "cssClass");
		includeLogin(rcontext);
	}

	protected void includeLogin(PortalRenderContext rcontext)
	{

		rcontext.put("loginTopLogin", Boolean.valueOf(true));
		rcontext.put("loginLogInOutUrl", "logInOutUrl");
		rcontext.put("loginMessage", "message");
		rcontext.put("loginImage1", "image1");
		rcontext.put("loginHasImage1", Boolean.valueOf(true));
		rcontext.put("loginLogInOutUrl2", "logInOutUrl2");
		rcontext.put("loginHasLogInOutUrl2", Boolean.valueOf(true));
		rcontext.put("loginMessage2", "message2");
		rcontext.put("loginImage2", "image2");
		rcontext.put("loginHasImage2", Boolean.valueOf(true));

		rcontext.put("loginPortalPath", "portalPath");
		rcontext.put("loginEidWording", "eidWording");
		rcontext.put("loginPwWording", "pwWording");
		rcontext.put("loginWording", "loginWording");
	}

	protected void includePage(PortalRenderContext rcontext) throws IOException
	{
		// divs to wrap the tools
		rcontext.put("pageWrapperClass", "wrapperClass");
		rcontext.put("pageColumnLayout", "col1of2");
		{
			List toolList = new ArrayList();
			toolList.add(includeTool());
			toolList.add(includeTool());
			rcontext.put("pageColumn0Tools", toolList);
		}

		rcontext.put("pageTwoColumn", Boolean.valueOf(true));
		{
			List toolList = new ArrayList();
			toolList.add(includeTool());
			rcontext.put("pageColumn1Tools", toolList);
		}
	}

	protected void includePageNav(Map rcontext) throws IOException
	{
		rcontext.put("pageNavPublished", Boolean.valueOf(true));
		rcontext.put("pageNavType", "type");
		rcontext.put("pageNavIconUrl", "iconUrl");
		rcontext.put("helpMenuClass", "HelpMenuClass");
		// rcontext.put("pageNavSitToolsHead", "sit_toolshead");

		List l = new ArrayList();
		Map m = new HashMap();
		m.put("current", Boolean.valueOf(true));
		m.put("ispopup", Boolean.valueOf(false));
		m.put("pagePopupUrl", "pagePopupUrl");
		m.put("pageIdWeb", "pageId");
		m.put("jsPageTitle", "pageTitleJS");
		m.put("htmlPageTitle", "pageTitleHTML");
		m.put("pagerefUrl", "pagerefUrl");
		l.add(m);
		m.put("current", Boolean.valueOf(false));
		m.put("ispopup", Boolean.valueOf(false));
		m.put("pagePopupUrl", "pagePopupUrl");
		m.put("pageIdWeb", "pageId");
		m.put("jsPageTitle", "pageTitleJS");
		m.put("htmlPageTitle", "pageTitleHTML");
		m.put("pagerefUrl", "pagerefUrl");
		m.put("menuClass", "MenuClass");
		m.put("pageRefUrl", "pageRefURL");
		m.put("pageSiteRefURL", "siteRefURL");
		m.put("pageTitle","pageTitle");

		l.add(m);
		rcontext.put("pageNavTools", l);

		rcontext.put("pageNavShowHelp", Boolean.valueOf(true));
		rcontext.put("pageNavHelpUrl", "helpUrl");
		// rcontext.put("pageNavSitHelp", "sit_help");

		// rcontext.put("pageNavSitPresenceTitle", "sit_presencetitle");
		// rcontext.put("pageNavSitPresenceFrameTitle",
		// "sit_presenceiframetit");
		rcontext.put("pageNavToolsCount", l.size());
		rcontext.put("pageNavShowPresenceLoggedIn", Boolean.valueOf(true));
		rcontext.put("pageNavPresenceUrl", "presenceUrl");
		rcontext.put("pageNavPresenceIframe", Boolean.valueOf(false));
                rcontext.put("sakaiPresenceTimeDelay", Integer.valueOf(3000));
		// rcontext.put("pageNavSitContentshead", "sit_contentshead");

	}

	protected void includeSiteNav(PortalRenderContext rcontext) throws IOException
	{
		rcontext.put("siteNavHasAccessibilityURL", Boolean.valueOf((true)));
		rcontext.put("siteNavAccessibilityURL", "accessibilityURL");
		// rcontext.put("siteNavSitAccessability", "sit_accessibility");
		// rcontext.put("siteNavSitJumpContent", "sit_jumpcontent");
		// rcontext.put("siteNavSitJumpTools", "sit_jumptools");
		// rcontext.put("siteNavSitJumpWorksite", "sit_jumpworksite");

		rcontext.put("siteNavLoggedIn", Boolean.valueOf(true));

		includeLogo(rcontext);
		includeTabs(rcontext);
	}

	protected void includeTabs(PortalRenderContext rcontext) throws IOException
	{

		rcontext.put("tabsCssClass", "cssClass");
		// rcontext.put("tabsSitWorksiteHead", "sit_worksiteshead");
		rcontext.put("tabsCurMyWorkspace", Boolean.valueOf(true));
		// rcontext.put("tabsSitMyWorkspace", "sit_mywor");
		rcontext.put("tabsSiteUrl", "mySiteUrl");
		// rcontext.put("tabsSitWorksite", "sit_worksite");

		List l = new ArrayList();
		{
			Map m = new HashMap();
			m.put("isCurrentSite", Boolean.valueOf(false));
			m.put("siteTitle", "siteTitle");
			m.put("siteUrl", "Web.escapeHtml(s.getTitle())");
			l.add(m);
			l.add(m);

		}
		rcontext.put("tabsSites", l);

		rcontext.put("tabsHasExtraTitle", Boolean.valueOf(true));

		rcontext.put("tabsExtraTitle", "Web.escapeHtml(extraTitle)");
		rcontext.put("tabsMoreSitesShow", Boolean.valueOf(true));
		// rcontext.put("tabsSitSelectMessage", "sit_selectmessage");
		// rcontext.put("tabsSitMode", "sit_more");
		{
			Map m = new HashMap();

			m.put("siteTitle", "Web.escapeHtml(s.getTitle())");
			m.put("siteUrl", "siteUrl");
			l.add(m);
			l.add(m);
		}
		rcontext.put("tabsMoreSites", l);

		rcontext.put("tabsAddLogout", Boolean.valueOf(true));
		rcontext.put("tabsLogoutUrl", "logoutUrl");
		// rcontext.put("tabsSitLog", "sit_log");
	}

	protected Map includeTool() throws IOException
	{
		Map toolMap = new HashMap();
		toolMap.put("toolUrl", "toolUrl");
		toolMap.put("toolPlacementIDJS", "Main_" + System.currentTimeMillis());
		toolMap.put("toolTitle", "titleString");
		toolMap.put("toolShowResetButton", Boolean.valueOf(true));
		toolMap.put("toolShowHelpButton", Boolean.valueOf(true));
		toolMap.put("toolHelpActionUrl", "helpActionUrl");
		toolMap.put("toolResetActionUrl", "toolResetActionUrl");
		
		return toolMap;
	}

	protected void includeWorksite(PortalRenderContext rcontext) throws IOException
	{
		Map sitePages = new HashMap();
		rcontext.put("sitePages", sitePages);
		// add the page navigation with presence
		includePageNav(sitePages);

		// add the page
		includePage(rcontext);
	}

	/**
	 * Output some session information
	 * 
	 * @param rcontext
	 *        The print writer
	 * @param html
	 *        If true, output in HTML, else in text.
	 */
	protected void showSession(PortalRenderContext rcontext)
	{
		rcontext.put("sessionSession", "s");
		rcontext.put("sessionToolSession", "ts");
	}

	protected void sendResponse(PortalRenderContext rcontext, String template)
			throws IOException
	{
		// get the writer
		if ( outputFile == null ) {
			outputFile = template;
		}
		File htmlOut = new File(outputDir,outputFile+".html");
		File tidyOut = new File(outputDir,outputFile+".html.tidy.txt");
		File errorFile = new File(outputDir,outputFile+".html.tidy.err");
		
		FileWriter f = new FileWriter(htmlOut);


		try
		{
			log.info("Rendering " + rcontext + " to " + htmlOut);
			rengine.render(template, rcontext, f);
		}
		catch (Exception e)
		{
			throw new RuntimeException("Failed to render template ", e);
		}
		f.close();

		Tidy t = new Tidy();

		FileOutputStream fo = new FileOutputStream(tidyOut);
		t.setIndentContent(true);
		t.setXHTML(true);
		PrintWriter errorOut = new PrintWriter(new FileWriter(errorFile));
		t.setErrout(errorOut);
		t.setOnlyErrors(false);
		t.setQuiet(false);
		t.setShowWarnings(true);
		t.parse(new FileInputStream(htmlOut), fo);
		int e = t.getParseErrors();
		int w = t.getParseWarnings();
		errorOut.close();
		// JTidy r938 became more agressive about warnings
		// Morpheus uses HTML5 tags which JTidy does not grok so we need
		// to actually read and parse the error output
		if ( e != 0 ) {
			try {
				BufferedReader br = new BufferedReader(new FileReader(errorFile));
				String thisLine;
				while ((thisLine = br.readLine()) != null) { // while loop begins here
					log.debug(thisLine);
					if ( thisLine.indexOf("Error:") < 0 ) continue;
					if ( thisLine.indexOf("<nav>") > 0 ) continue;
					if ( thisLine.indexOf("<main>") > 0 ) continue;
					if ( thisLine.indexOf("<header>") > 0 ) continue;
					log.info("Context Dump is " + rcontext.dump());
					throw new RuntimeException("Error in HTML see "+errorFile+" "+thisLine);
				} 
			} 
			catch (IOException ex) {
				log.info("File read error " + ex);
				throw new RuntimeException("File read error "+ex);
			}
		}
		log.info("All OK");
		
		

	}

	/**
	 * @return the outputFile
	 */
	public String getOutputFile()
	{
		return outputFile;
	}

	/**
	 * @param outputFile the outputFile to set
	 */
	public void setOutputFile(String outputFile)
	{
		this.outputFile = outputFile;
	}

	/**
	 * @return the resourceLoader
	 */
	public Object getResourceLoader()
	{
		return resourceLoader;
	}

	/**
	 * @param resourceLoader the resourceLoader to set
	 */
	public void setResourceLoader(Object resourceLoader)
	{
		this.resourceLoader = resourceLoader;
	}

}
