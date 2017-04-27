<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<%@ page import="org.sakaiproject.lessonbuildertool.tool.beans.SimplePageBean" %><%
%><%@ page import="java.net.URLEncoder" %><%
%><%@ page import="org.sakaiproject.tool.api.Session" %><%
%><%@ page import="org.sakaiproject.authz.cover.SecurityService" %><%
%><%@ page import="org.sakaiproject.tool.cover.SessionManager" %><%
%><%@ page import="org.sakaiproject.site.api.Site" %><%
%><%@ page import="org.sakaiproject.site.api.SitePage" %><%
%><%@ page import="org.sakaiproject.site.cover.SiteService" %><%
%><%@ page import="org.sakaiproject.component.cover.ComponentManager" %><%
%><%@ page import="org.sakaiproject.lessonbuildertool.model.SimplePageToolDao" %><%
%><%@ page import="org.sakaiproject.event.cover.EventTrackingService" %><%
%><%@ page import="org.sakaiproject.lessonbuildertool.api.LessonBuilderEvents" %><%
%><%@ page import="org.sakaiproject.lessonbuildertool.SimplePage" %><%
%><%@ page import="org.apache.commons.lang.StringEscapeUtils" %>


<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<META HTTP-EQUIV="CACHE-CONTROL" CONTENT="NO-CACHE"/>
<script type="text/javascript" language="JavaScript" src="/library/js/headscripts.js"></script>
<link href="/library/skin/tool_base.css" type="text/css" rel="stylesheet" media="all" />
<link href="/library/skin/default/tool.css" type="text/css" rel="stylesheet" media="all" />
<link rel="stylesheet" href="../css/Simplepagetool.css" type="text/css"/>
</head>
<body>

<%

    // called from remove page to do the actual removal. We can't use
    // normal RSF because after removing the tool it will try to return to
    // a non-existent RSF context.
    //
    // called by
    //  https://heidelberg.rutgers.edu/sakai-lessonbuildertool-tool/removePage?page=330&site=2da97547-7031-4bca-8f18-c6f9517016b9
    // for security verify that we own the site and that the page is in the site
    // error messages should be impossible, so we're not internationalizing them

    SimplePageToolDao dao = (SimplePageToolDao)ComponentManager
    .get(SimplePageToolDao.class);

    String siteId = request.getParameter("site");
    Site site = null;
    try {
        site = SiteService.getSite(siteId);
    } catch (Exception e) {
	out.println(StringEscapeUtils.escapeHtml(e.toString()));
	return;
    }

    Session s = SessionManager.getCurrentSession();
    if (s == null) {
	out.println("no session. this must be called from within Sakai");
	return;
    }

    String userId = s.getUserId();
    String siteReference = SiteService.siteReference(siteId);

    if (!(SecurityService.unlock(userId, SiteService.SECURE_UPDATE_SITE, siteReference) ||
	  SecurityService.isSuperUser())) {
	out.println(StringEscapeUtils.escapeHtml("sorry, you aren't allowed to update this site " + userId + " " + siteReference));
	return;
    }

    String removeId = request.getParameter("page");

    SimplePage simplePage = dao.getPage(Long.parseLong(removeId));
		
    if (simplePage == null) {
	out.println("No such page");
	return;
    }

    if (!simplePage.getSiteId().equals(siteId)) {
	out.println("Specified page not in current site");
	return;
    }

    if(simplePage.getOwner()!=null && !simplePage.isOwned()) {
	out.println("Can't remove student pages this way");
	return;
    }
    
    SitePage sitePage = site.getPage(simplePage.getToolId());
    if (sitePage == null) {
	out.println(StringEscapeUtils.escapeHtml("removePage can't find site page for " + simplePage.getPageId()));
	return;
    }
    
    site.removePage(sitePage);
		
    try {
	SiteService.save(site);
    } catch (Exception e) {
	out.println(StringEscapeUtils.escapeHtml("removePage unable to save site " + e));
    }
		
    EventTrackingService.post(EventTrackingService.newEvent(LessonBuilderEvents.PAGE_REMOVE, "/lessonbuilder/page/" + simplePage.getPageId(), true));

   out.println("<script>parent.location.replace(\"/portal/site/" + URLEncoder.encode(site.getId(), "UTF-8") + "\")</script>");

%>

</body>
</html>
