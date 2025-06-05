<% // NOTE: DO NOT ADD WHITESPACE LINES TO THIS SCRIPTLET
   if ( (String) request.getAttribute("sakai.html.head") != null)
    {
      // get the HTML fragment for the header from the Sakai portal,
      // and then add Samigo's own tool-specific CSS file to it.
      String head = (String) request.getAttribute("sakai.html.head");
      head += org.sakaiproject.portal.util.PortalUtils.includeLatestJQuery("Samigo");
      head += "<script type=\"text/javascript\" src=\"/library/webjars/jquery.tablesorter/2.27.7/dist/js/jquery.tablesorter.min.js\"></script>\n";
      head += "<link href=\"/library/webjars/jquery.tablesorter/2.27.7/dist/css/theme.default.min.css\" type=\"text/css\" rel=\"stylesheet\" media=\"all\" />\n";
      head += "<script type=\"text/javascript\" src=\"/samigo-app/js/samigo-global.js\"></script>\n";
      head += "<script>includeWebjarLibrary(\"qtip2\");</script>";

      request.setAttribute("html.head", head);
      request.setAttribute("html.body.onload", request.getAttribute("sakai.html.body.onload"));
      request.setAttribute("html.head.sakai", request.getAttribute("sakai.html.head"));
    }
    else
    {
      // supply the standalone versions tool styles
      request.setAttribute("html.head",
         "<link href=\"" + org.sakaiproject.portal.util.CSSUtils.getCssToolBase() + "\" type=\"text/css\" rel=\"stylesheet\" media=\"all\" />\n" +
         "<link href=\"" + org.sakaiproject.portal.util.CSSUtils.getCssToolSkin(org.sakaiproject.portal.util.CSSUtils.adjustCssSkinFolder(null)) + "\" type=\"text/css\" rel=\"stylesheet\" media=\"all\" />\n" +
         "<link href=\"/library/webjars/jquery-ui/1.12.1/jquery-ui.min.css\" type=\"text/css\" rel=\"stylesheet\" media=\"all\" />\n" +
         "<script type=\"text/javascript\" src=\"/library/webjars/jquery/1.12.4/jquery.min.js\"></script>\n" +
         "<script type=\"text/javascript\" src=\"/library/webjars/jquery-ui/1.12.1/jquery-ui.min.js\"></script>\n" +
         "<script type=\"text/javascript\" src=\"/samigo-app/js/samigo-global.js\"></script>\n" +
         "<script type=\"text/javascript\" src=\"/library/js/headscripts.js\"></script>\n");
      request.setAttribute("html.body.onload", "");
    } %>
