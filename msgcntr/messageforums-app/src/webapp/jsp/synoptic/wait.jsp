<%@ page import="java.util.*, javax.faces.context.*, javax.faces.application.*,
                 javax.faces.el.*, org.sakaiproject.tool.messageforums.ui.*"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>

<jsp:useBean id="msgs" class="org.sakaiproject.util.ResourceLoader" scope="session">
   <jsp:setProperty name="msgs" property="baseName" value="org.sakaiproject.api.app.messagecenter.bundle.Messages"/>
</jsp:useBean>

<html>

<head>
<script src="js/frameAdjust.js" type="text/javascript"></script>
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
	var images = new Array(12);
	var imagesURI = new Array(12);
	
	function getEl(id) {
		if (document.getElementById) {
			return  document.getElementById(id);
		}
		else if (document.all) {
			return document.all[id];
		}
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
			notloading = false;
		
			// checking if ff browser since animated gif
			// does not spin when href changed, so use AJAX
			var agt=navigator.userAgent.toLowerCase();

			if (agt.indexOf("firefox") != -1) {
				getActualFile();
			}
			else {

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

						// GET parameter added so other page will know it
						// was called by this wait page (and not from iframe)
						location.href = url + "?time=1";
					}
				}, 300);
			}
		}
	}

	/*
	 * Turns off the refeshing of the wait screen
	 * when actual page being loaded.
	 */
	function unload() {
		// turn off animation when page finished
		window.clearInterval(intervalid);
	}

	/*
	 * For FF browsers, animated gif freezes when href changed,
	 * so use AJAX to get redirected page.
	 *
	 */	
	function getActualFile()
	{
		var http;
		http = new XMLHttpRequest();

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

		// GET parameter added so other page will know it
		// was called by this wait page (and not from iframe)
		http.open("GET", url + "?time=1", true);

		http.onreadystatechange = function()
		{
			if (http.readyState == 4) {
    			var response = http.responseText; 
    			document.getElementById('result').innerHTML = response;
    			adjustMainFrameHeight(self.name);
 	       }	 
		}
		
		http.send(null);
	}

// --></script>
</head>

<f:view>
  <sakai:view>
  	  <sakai:script contextBase="/sakai-messageforums-tool" path="/js/popupscripts.js"/>
  
  	<%-- Used to store where to redirect to so javascript can grab it. --%>
    <h:inputHidden id="longPageLoad" value="#{msgs.longPageLoad}" />

	<%-- Firefox browser needs to use AJAX to get actual page. Retrieved page then
		 stuffed into this div --%>
	<f:verbatim><div id="result"></f:verbatim>

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
		  	<h:graphicImage value="#{msgs.wait_icon}" />
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

	<f:verbatim></div></f:verbatim>

  </sakai:view>
</f:view>

<script language="JavaScript"> 
	// Call javascript function to grab actual long loading page
	load();
</script>

</html>
