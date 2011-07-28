/*
 * Created on Dec 2, 2005
 */
package uk.ac.cam.caret.sakai.rsf.servlet;

import java.util.TimeZone;

import javax.servlet.http.HttpServletRequest;

import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SitePage;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.time.api.TimeService;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.api.Tool;

import uk.ac.cam.caret.sakai.rsf.bridge.SakaiNavConversion;
import uk.ac.cam.caret.sakai.rsf.copies.DefaultPortalMatter;
import uk.ac.cam.caret.sakai.rsf.producers.FrameAdjustingProducer;
import uk.ac.cam.caret.sakai.rsf.template.SakaiBodyTPI;
import uk.ac.cam.caret.sakai.rsf.template.SakaiPortalMatterSCR;
import uk.org.ponder.rsf.renderer.ComponentRenderer;
import uk.org.ponder.rsf.renderer.scr.BasicSCR;
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

  private TimeService timeservice;

  private String urlEntityReference;

  public void setHttpServletRequest(HttpServletRequest request) {
    this.request = request;
  }

  public void setSiteService(SiteService siteservice) {
    this.siteservice = siteservice;
  }

  public void setTimeService(TimeService timeservice) {
    this.timeservice = timeservice;
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

    SakaiPortalMatterSCR matterscr = new SakaiPortalMatterSCR();
    String headMatter = (String) request.getAttribute("sakai.html.head");
    if (headMatter == null) {
      headMatter = DefaultPortalMatter.getDefaultPortalMatter();
    }
    matterscr.setHeadMatter(headMatter);
    
    if (tool != null && placement != null) {
      String toolid = tool.getId();
      String toolinstancepid = placement.getId();

      String frameid = FrameAdjustingProducer.deriveFrameTitle(toolinstancepid);

      // Deliver the rewrite rule to the renderer that will invoke the relevant
      // Javascript magic to resize our frame.

      String sakaionload = (String) request
          .getAttribute("sakai.html.body.onload");
      String hname = "addSakaiRSFDomModifyHook";
      // String fullonload = "if (" + hname + "){" + hname + "(\"" + frameid +
      // "\");};" + sakaionload;
      String fullonload = "if (typeof(" + hname + ") != 'undefined'){ " + hname
          + "('" + frameid + "');}" + sakaionload;
      
      FlatSCR bodyscr = new FlatSCR();
      bodyscr.addNameValue(new NameValue("onload", fullonload));
      bodyscr.tag_type = ComponentRenderer.NESTING_TAG;

      Logger.log.info("Got tool dispatcher id of " + toolid
          + " resourceBaseURL " + bup.getResourceBaseURL() + " baseURL "
          + bup.getBaseURL() + " and Sakai PID " + toolinstancepid);

      // Compute the ConsumerInfo object.
      site = SakaiNavConversion.siteForPID(siteservice, toolinstancepid);
      // tc will be null for Mercury portal
      ToolConfiguration tc = siteservice.findTool(toolinstancepid);
      if (tc != null) {
        sitepage = SakaiNavConversion.pageForToolConfig(siteservice, tc);
      }
      bodyscr.setName(SakaiBodyTPI.SAKAI_BODY);
      
      src.addSCR(bodyscr);
    }
    else {
      NullRewriteSCR bodyscr2 = new NullRewriteSCR();
      bodyscr2.setName(SakaiBodyTPI.SAKAI_BODY);
      src.addSCR(bodyscr2);
    }

    src.addSCR(matterscr);
    
    consumerinfo = new ConsumerInfo();
    consumerinfo.urlbase = bup.getBaseURL();
    consumerinfo.resourceurlbase = bup.getResourceBaseURL();
    consumerinfo.consumertype = "sakai";

    consumerinfo.externalURL = sitepage == null ? consumerinfo.urlbase
        : URLUtil.deSpace(sitepage.getUrl());
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
    return timeservice.getLocalTimeZone();
  }

  public Placement getPlacement() {
    return placement;
  }
  
  // Make sure we always return a valid context reference, since so much depends on it
  public String getContext() {
    return placement == null? "" : placement.getContext();
  }
  
  public String getEntityReference() {
    return urlEntityReference == null? 
        (site == null? "" : site.getReference())
        : urlEntityReference;
  }
}
