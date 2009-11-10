<% // NOTE: DO NOT ADD WHITESPACE LINES TO THIS SCRIPTLET
   if ( (String) request.getAttribute("sakai.html.head") != null)
    {
      // get the HTML fragment for the header from the Sakai portal,
      // and then add Samigo's own tool-specific CSS file to it.
      String head = (String) request.getAttribute("sakai.html.head");
      head +=
       "<link href=\"/samigo-app/css/tool_sam.css\" type=\"text/css\" rel=\"stylesheet\" media=\"all\" />\n";
      request.setAttribute("html.head", head);
      request.setAttribute("html.body.onload",
         request.getAttribute("sakai.html.body.onload"));
      request.setAttribute("html.head.sakai",
         request.getAttribute("sakai.html.head"));
    }
    else
    {
      // supply the standalone versions tool styles
      request.setAttribute("html.head",
         "<link href=\"/library/skin/tool_base.css\" type=\"text/css\" rel=\"stylesheet\" media=\"all\" />\n" +
         "<link href=\"/library/skin/default/tool.css\" type=\"text/css\" rel=\"stylesheet\" media=\"all\" />\n" +
         "<link href=\"/samigo-app/css/tool_sam.css\" type=\"text/css\" rel=\"stylesheet\" media=\"all\" />\n");
      request.setAttribute("html.body.onload", "");
    } %>