<% // NOTE: DO NOT ADD WHITESPACE LINES TO THIS SCRIPTLET
   if ( (String) request.getAttribute("sakai.html.head") != null)
    {
      // get the HTML fragment for the header from the Sakai portal,
      // and then add Samigo's own tool-specific CSS file to it.
      String head = (String) request.getAttribute("sakai.html.head");
      head += org.sakaiproject.portal.util.PortalUtils.includeLatestJQuery("Samigo");
      head += "<script>includeWebjarLibrary(\"jquery.tablesorter\", { includeCss: true });</script>\n"
      head += "<script>includeWebjarLibrary(\"qtip2\", { includeCss: true });</script>\n";
      head += "<script>includeWebjarLibrary(\"bootstrap\", { includeCss: true });</script>\n";
      head += "<script src=\"/samigo-app/js/samigo-global.js\"></script>\n";

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
         "<script src=\"/library/js/headscripts.js\"></script>\n" +
         "<script>includeLatestJQuery(\"Samigo\");</script>\n" +
         "<script>includeWebjarLibrary(\"jquery.tablesorter\", { includeCss: true });</script>\n" +
         "<script>includeWebjarLibrary(\"qtip2\", { includeCss: true });</script>\n" +
         "<script>includeWebjarLibrary(\"bootstrap\", { includeCss: true });</script>\n" +
         "<script src=\"/samigo-app/js/samigo-global.js\"></script>\n";
      request.setAttribute("html.body.onload", "");
    } %>
