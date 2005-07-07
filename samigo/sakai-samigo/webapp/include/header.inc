<%
	// get the HTML fragment for the header from the Sakai portal,
	// and then add Samigo's own tool-specific CSS file to it.
	String head = request.getAttribute("sakai.html.head");
	head += "<link href=\"/samigo/css/samigo.css\" type=\"text/css\" rel=\"stylesheet\" media=\"all\" />\n";
	
    request.setAttribute("html.head", head);
    request.setAttribute("html.body.onload", request.getAttribute("sakai.html.body.onload"));   
    request.setAttribute("html.head.sakai", request.getAttribute("sakai.html.head")); 
	
%>
