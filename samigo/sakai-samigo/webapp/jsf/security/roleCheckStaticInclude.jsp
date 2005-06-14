<%@ page import="org.sakaiproject.service.legacy.site.cover.SiteService,
                 org.sakaiproject.tool.assessment.ui.listener.author.AuthorActionListener,
                 org.sakaiproject.tool.assessment.ui.listener.select.SelectActionListener,
                 org.sakaiproject.service.framework.portal.cover.PortalService"
%>
<%
  if (!SiteService.allowUpdateSite(PortalService.getCurrentSiteId()))
  {
          SelectActionListener listener = new SelectActionListener();
          listener.processAction(null);
	  request.getRequestDispatcher("../select/selectIndex.faces").forward(request, response);
  }    
  else
  {
     AuthorActionListener authorlistener = new AuthorActionListener();
     authorlistener.processAction(null);
     request.getRequestDispatcher("../author/authorIndex.faces").forward(request, response);
  }
%>