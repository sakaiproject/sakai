/**
 * Copyright © 2005, CARET, University of Cambridge
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and documentation are those
 * of the authors and should not be interpreted as representing official policies,
 * either expressed or implied, of the FreeBSD Project.
 */
/*
 * Created on Dec 2, 2005
 */
package org.sakaiproject.rsf.servlet;

import java.util.TimeZone;

import javax.servlet.http.HttpServletRequest;

import org.sakaiproject.rsf.bridge.SakaiNavConversion;
import org.sakaiproject.rsf.copies.DefaultPortalMatter;
import org.sakaiproject.rsf.producers.FrameAdjustingProducer;
import org.sakaiproject.rsf.template.SakaiBodyTPI;
import org.sakaiproject.rsf.template.SakaiPortalMatterSCR;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SitePage;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.time.api.UserTimeService;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.api.Tool;

import uk.org.ponder.rsf.renderer.ComponentRenderer;
import uk.org.ponder.rsf.renderer.scr.FlatSCR;
import uk.org.ponder.rsf.renderer.scr.NullRewriteSCR;
import uk.org.ponder.rsf.renderer.scr.StaticRendererCollection;
import uk.org.ponder.rsf.viewstate.BaseURLProvider;
import uk.org.ponder.stringutil.URLUtil;
import uk.org.ponder.util.Logger;
import uk.org.ponder.webapputil.ConsumerInfo;
import uk.org.ponder.xml.NameValue;

/**
 * Parses the servlet request and general Sakai environment for appropriate
 * Sakai request-scope beans. Do not try to make any use of URL information from
 * the request outside the EarlyRequestParser.
 * 
 * @author Antranig Basman (antranig@caret.cam.ac.uk)
 * 
 */

public class SakaiRequestParser {
	private HttpServletRequest request;

	private Site site;

	private SitePage sitepage;
	private SiteService siteservice;

	private StaticRendererCollection src = new StaticRendererCollection();

	private ConsumerInfo consumerinfo;

	private Placement placement;

	private BaseURLProvider bup;

	private UserTimeService userTimeService;

	private String urlEntityReference;

	public void setHttpServletRequest(HttpServletRequest request) {
		this.request = request;
	}

	public void setSiteService(SiteService siteservice) {
		this.siteservice = siteservice;
	}

	public void setUserTimeService(UserTimeService userTimeservice) {
		this.userTimeService = userTimeservice;
	}

	public void setBaseURLProvider(BaseURLProvider bup) {
		this.bup = bup;
	}

	public void setUrlEntityReference(String urlEntityReference) {
		this.urlEntityReference = urlEntityReference;
	}

	public void init() {
		Tool tool = (Tool) request.getAttribute("sakai.tool");
		placement = (Placement) request.getAttribute("sakai.tool.placement");

		if (tool != null && placement != null) {
			String toolid = tool.getId();
			String toolinstancepid = placement.getId();

			String frameid = FrameAdjustingProducer.deriveFrameTitle(toolinstancepid);

			// Deliver the rewrite rule to the renderer that will invoke the
			// relevant
			// Javascript magic to resize our frame.

			String sakaionload = (String) request.getAttribute("sakai.html.body.onload");
			String hname = "addSakaiRSFDomModifyHook";
			// String fullonload = "if (" + hname + "){" + hname + "(\"" +
			// frameid +
			// "\");};" + sakaionload;
			String fullonload = "if (typeof(" + hname + ") != 'undefined'){ " + hname + "('" + frameid + "');}"
					+ sakaionload;

			FlatSCR bodyscr = new FlatSCR();
			bodyscr.addNameValue(new NameValue("onload", fullonload));
			bodyscr.tag_type = ComponentRenderer.NESTING_TAG;

			Logger.log.info("Got tool dispatcher id of " + toolid + " resourceBaseURL " + bup.getResourceBaseURL()
					+ " baseURL " + bup.getBaseURL() + " and Sakai PID " + toolinstancepid);

			// Compute the ConsumerInfo object.
			site = SakaiNavConversion.siteForPID(siteservice, toolinstancepid);
			// tc will be null for Mercury portal
			ToolConfiguration tc = siteservice.findTool(toolinstancepid);
			if (tc != null) {
				sitepage = SakaiNavConversion.pageForToolConfig(siteservice, tc);
			}
			bodyscr.setName(SakaiBodyTPI.SAKAI_BODY);

			src.addSCR(bodyscr);
		} else {
			NullRewriteSCR bodyscr2 = new NullRewriteSCR();
			bodyscr2.setName(SakaiBodyTPI.SAKAI_BODY);
			src.addSCR(bodyscr2);
		}

		SakaiPortalMatterSCR matterscr = new SakaiPortalMatterSCR();
		String headMatter = (String) request.getAttribute("sakai.html.head");
		if (headMatter == null) {
			headMatter = DefaultPortalMatter.getDefaultPortalMatter(site,request);
		}
		matterscr.setHeadMatter(headMatter);
		src.addSCR(matterscr);

		consumerinfo = new ConsumerInfo();
		consumerinfo.urlbase = bup.getBaseURL();
		consumerinfo.resourceurlbase = bup.getResourceBaseURL();
		consumerinfo.consumertype = "sakai";

		consumerinfo.externalURL = sitepage == null ? consumerinfo.urlbase : URLUtil.deSpace(sitepage.getUrl());
	}

	public Site getSite() {
		return site;
	}

	public SitePage getSitePage() {
		return sitepage;
	}

	public StaticRendererCollection getConsumerStaticRenderers() {
		return src;
	}

	public ConsumerInfo getConsumerInfo() {
		return consumerinfo;
	}

	public TimeZone getTimeZone() {
		return userTimeService.getLocalTimeZone();
	}

	public Placement getPlacement() {
		return placement;
	}

	// Make sure we always return a valid context reference, since so much
	// depends on it
	public String getContext() {
		return placement == null ? "" : placement.getContext();
	}

	public String getEntityReference() {
		return urlEntityReference == null ? (site == null ? "" : site.getReference()) : urlEntityReference;
	}
}
