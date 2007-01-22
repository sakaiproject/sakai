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
	//					Put here so can clear on unload.
	var intervalid, index = 0;

	/*
	 * Makes the '....' on the page expand
	 */	
	function progress()
	{
		var content = document.getElementById("progress");
		if (index++) {
			text = content.innerHTML;
			height = content.style.height;
			width = content.style.width;
			content.innerHTML = "";
			content.style.width = width;
			content.style.height = height;			
		}
		else {
			document.getElementById("progress").innerHTML = text;
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
		setTimeout( function() {
			var urlEl = document.getElementById("longPageLoad");
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
			location.href = url;
		}, 0);
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
<table width="99%" height="99%">
<tr>
	<td align="center" valign="middle">
		<table cellpadding="0" cellspacing="0">
		<tr>
		  <td width="40">
			<span id="progress" class="alertMessage" style="text-decoration: blink;">
				<h3><h:outputText value="#{msgs.loading_wait}" /></h3>
			</span>
		  </td>
		</tr>
		</table>
	</td>
</tr>
</table>
  </sakai:view>
</f:view>

</body>

</html>
