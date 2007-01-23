<%@ page import="java.util.*, javax.faces.context.*, javax.faces.application.*,
                 javax.faces.el.*, org.sakaiproject.tool.messageforums.ui.*"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>

<jsp:useBean id="msgs" class="org.sakaiproject.util.ResourceLoader" scope="session">
   <jsp:setProperty name="msgs" property="baseName" value="org.sakaiproject.tool.messageforums.bundle.Messages"/>
</jsp:useBean>

<html>

<head>
<script language="Javascript"><!--
	/*
	 *	To use this, call this page with a GET parameter of "url"
	 *	with the urlencoded destination page that takes a while to load
	 *
	 */
	
	// intervalid - id of call to progress() every 1/2 sec.
	//				Put here so can clear on unload.
	// loading    - switch since load() done on body load
	//				and also within script (for Safari).
	var intervalid, index = 0;
	var notloading = true;
	
	function getEl(id) {
		if (document.getElementById) {
			return  document.getElementById(id);
		}
		else if (document.all) {
			return document.all[id];
		}
		alert('no getElById and no doc.all');
	}

	/*
	 * Makes the '....' on the page expand
	 */	
	function progress()
	{
		var content = getEl("progress");
		if (index++) {
			text = content.innerHTML;
			height = content.style.height;
			width = content.style.width;
			content.innerHTML = "";
			content.style.width = width;
			content.style.height = height;			
		}
		else {
			document.getEl("progress").innerHTML = text;
		}
		index = index % 2;
	}

	/*
	 * Determines if parameter is just a filename.
	 */
	 function isFilename(fn) {
	 	if (fn.substring(0,5) == 'http:' || 
	 		fn.substring(0,6) == 'https:')
			return false;
		else
			return true;
	 }
	 	
	/*
	 * Determines the URL to redirect to. This is determined
	 * the a GET parameter named url.
	 *
	 * NOTES: To use method 3, site name must start with 'www'.
	 *
	 * Two methods:
	 * 1. another page within site (ex: url=main)
	 * 2. an entire url (ex: url=http://www.somesite.com)
	 */
	function load() {
		if (notloading) {
			notloading = true;

		setTimeout( function() {
			var urlEl = getEl("longPageLoad");
			var url = urlEl.value;

			// just page name, construct url from current href
			if (isFilename(url)) {
				var urlCurrent = window.location.href;
				var lastSlash = urlCurrent.lastIndexOf('/');

				if (lastSlash > 0) {
					url = urlCurrent.substring(0, lastSlash) + '/' + url;
				}
				else {
					// what to do? what to do?
				}
			}

			// GET parameter added so hack won't create an infinite call loop
			location.href = url + "?time=1";
		}, 0);
		
		}
	}

	/*
	 * Turns off the refeshing of the wait screen
	 * when actual page being loaded.
	 */
	function unload() {
		window.clearInterval(intervalid);
	}
	
// --></script>
</head>

<body onload="load()" onunload="unload()">
 
<f:view>
  <sakai:view>
    <h:inputHidden id="longPageLoad" value="#{msgs.longPageLoad}" />

<%  
  /* if MyWorkspace, display wait gif and message. if not, just redirect to synMain. */
  FacesContext context = FacesContext.getCurrentInstance();
  Application app = context.getApplication();
  ValueBinding binding = app.createValueBinding("#{mfSynopticBean}");
  MessageForumSynopticBean mfsb = (MessageForumSynopticBean) binding.getValue(context);
  
  if (mfsb.isMyWorkspace()) {
%>

<table width="99%" height="90%">
<tr>
	<td align="center" valign="middle">
		<table cellpadding="0" cellspacing="0" width="16%">
		<tr>
		  <td>
		  	<h:graphicImage value="images/wait_img.gif" />
		  </td>
		  <td>&nbsp;&nbsp;</td>
		  <td align="right" width="50">
			<span id="progress">
				<h:outputText value="#{msgs.loading_wait}" style="font-size: 14pt;" />
			</span>
		  </td>
		</tr>
		</table>
	</td>
</tr>
</table>

<% } %>

  </sakai:view>
</f:view>

</body>

<script language="JavaScript"> 
	// some browsers ignore the first <body> tag, so add load() here
	load();
</script>

</html>
