<% // NOTE: DO NOT ADD WHITESPACE LINES TO THIS SCRIPTLET
   if ( (String) request.getAttribute("sakai.html.head") != null)
    {
      // get the HTML fragment for the header from the Sakai portal,
      // and then add Samigo's own tool-specific CSS file to it.
      String head = (String) request.getAttribute("sakai.html.head");
      head += "<link href=\"/samigo-app/css/tool_sam.css\" type=\"text/css\" rel=\"stylesheet\" media=\"all\" />\n";
      head += org.sakaiproject.portal.util.PortalUtils.includeLatestJQuery("Samigo");
      head += "<script type=\"text/javascript\" src=\"/library/js/jquery/tablesorter/2.1.17/jquery.tablesorter.min.js\"></script>\n";
      head += "<link href=\"/library/js/jquery/tablesorter/2.1.17/css/theme.default.css\" type=\"text/css\" rel=\"stylesheet\" media=\"all\" />\n";
      head += "<script type=\"text/javascript\" src=\"/samigo-app/js/samigo-global.js\"></script>\n";

      head += "<script type=\"text/javascript\" src=\"/library/js/jquery/qtip/jquery.qtip-latest.min.js\"></script>\n";
      head += "<link rel=\"stylesheet\" type=\"text/css\" href=\"/library/js/jquery/qtip/jquery.qtip-latest.min.css\"/>\n";
      
      head += "<script type=\"text/javascript\" src=\"/library/js/headscripts.js\"></script>\n";

      request.setAttribute("html.head", head);
      request.setAttribute("html.body.onload", request.getAttribute("sakai.html.body.onload"));
      request.setAttribute("html.head.sakai", request.getAttribute("sakai.html.head"));
    }
    else
    {
      // supply the standalone versions tool styles
      request.setAttribute("html.head",
         "<link href=\"/library/skin/tool_base.css\" type=\"text/css\" rel=\"stylesheet\" media=\"all\" />\n" +
         "<link href=\"/library/skin/morpheus-default/tool.css\" type=\"text/css\" rel=\"stylesheet\" media=\"all\" />\n" +
         "<link href=\"/samigo-app/css/tool_sam.css\" type=\"text/css\" rel=\"stylesheet\" media=\"all\" />\n" +
         "<script type=\"text/javascript\" src=\"/library/webjars/jquery/1.12.4/jquery.min.js\"></script>\n" +
         "<script type=\"text/javascript\" src=\"/library/webjars/jquery-ui/1.12.1/jquery-ui.min.js\"></script>\n" +
         "<script type=\"text/javascript\" src=\"/samigo-app/js/samigo-global.js\"></script>\n" +
         "<script type=\"text/javascript\" src=\"/library/js/headscripts.js\"></script>\n");
      request.setAttribute("html.body.onload", "");
    } %>
