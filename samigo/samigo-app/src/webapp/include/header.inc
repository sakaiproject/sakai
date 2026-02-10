<% // NOTE: DO NOT ADD WHITESPACE LINES TO THIS SCRIPTLET
   String skinFolder = (String) request.getAttribute("sakai.skin.folder");
   if (skinFolder == null || skinFolder.isEmpty()) {
     skinFolder = org.sakaiproject.portal.util.CSSUtils.adjustCssSkinFolder(null);
   }
   if ( (String) request.getAttribute("sakai.html.head") != null)
    {
      // get the HTML fragment for the header from the Sakai portal,
      // and then add Samigo's own tool-specific CSS file to it.
      String head = (String) request.getAttribute("sakai.html.head");
      head += org.sakaiproject.portal.util.PortalUtils.includeLatestJQuery("Samigo");
      head += "<script>includeWebjarLibrary(\"jquery.tablesorter\", { includeCss: true });</script>\n";
      head += "<script>includeWebjarLibrary(\"qtip2\", { includeCss: true });</script>\n";
      head += "<script>includeWebjarLibrary(\"bootstrap\");</script>\n";
      head += "<script src=\"/samigo-app/js/samigo-global.js\"></script>\n";

      request.setAttribute("html.head", head);
      request.setAttribute("html.body.onload", request.getAttribute("sakai.html.body.onload"));
      request.setAttribute("html.head.sakai", request.getAttribute("sakai.html.head"));
    }
    else
    {
      // supply the standalone versions tool styles
      request.setAttribute("html.head",
         org.sakaiproject.portal.util.CSSUtils.getPortalSkinLink(skinFolder) +
         "<link href=\"" + org.sakaiproject.portal.util.CSSUtils.getCssToolBase() + "\" type=\"text/css\" rel=\"stylesheet\" media=\"all\" />\n" +
         "<link href=\"" + org.sakaiproject.portal.util.CSSUtils.getCssToolSkin(skinFolder) + "\" type=\"text/css\" rel=\"stylesheet\" media=\"all\" />\n" +
         "<link href=\"" + org.sakaiproject.portal.util.CSSUtils.getCssPrintSkin(skinFolder) + "\" type=\"text/css\" rel=\"stylesheet\" media=\"print\" />\n" +
         "<link href=\"" + org.sakaiproject.portal.util.CSSUtils.getCssPropertiesSkin(skinFolder) + "\" type=\"text/css\" rel=\"stylesheet\" media=\"all\" />\n" +
         "<script src=\"/library/js/headscripts.js\"></script>\n" +
         "<script>includeLatestJQuery(\"Samigo\");</script>\n" +
         "<script>includeWebjarLibrary(\"jquery.tablesorter\", { includeCss: true });</script>\n" +
         "<script>includeWebjarLibrary(\"qtip2\", { includeCss: true });</script>\n" +
         "<script>includeWebjarLibrary(\"bootstrap\");</script>\n" +
         "<script src=\"/samigo-app/js/samigo-global.js\"></script>\n");
      request.setAttribute("html.body.onload", "");
    } %>
