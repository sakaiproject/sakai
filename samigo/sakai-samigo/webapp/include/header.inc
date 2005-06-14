<%
	request.setAttribute("html.head", request.getAttribute("sakai.html.head.js"));
	request.setAttribute("html.body.onload", request.getAttribute("sakai.html.body.onload"));	
	request.setAttribute("html.head.sakai", request.getAttribute("sakai.html.head"));

	// TODO: Take this out once Samigo is completely portal-bound 
	// and never goes outside the portal!
	if (request.getAttribute("sakai.html.head.js") == null || ((String) request.getAttribute("sakai.html.head.js")).length() == 0)
	{
		String htmlHead;
		String htmlBodyOnload;
	
		StringBuffer bodyonload = new StringBuffer();
		htmlHead = "<script type=\"text/javascript\" language=\"JavaScript\" src=\"/library/js/headscripts.js\"></script>\n";
		String pid = (String) request.getAttribute("sakai.tool.placement.id");
		if (pid == null) pid = (String) request.getParameter("sakai.tool.placement.id");
		if (pid != null)
		{
			String element = org.sakaiproject.util.web.Web.escapeJavascript("Main" + pid);
			bodyonload.append("setMainFrameHeight('" + element + "');");
		}
		bodyonload.append("setFocus(focus_path);");

		htmlBodyOnload = bodyonload.toString();
		request.setAttribute("html.head", htmlHead);
		request.setAttribute("html.body.onload", htmlBodyOnload);
	}	
%>