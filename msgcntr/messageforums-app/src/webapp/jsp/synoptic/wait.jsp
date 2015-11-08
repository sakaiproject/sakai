<%@ page import="java.util.*, javax.faces.context.*, javax.faces.application.*,
                 javax.faces.el.*, org.sakaiproject.tool.messageforums.ui.*"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>

<jsp:useBean id="msgs" class="org.sakaiproject.util.ResourceLoader" scope="session">
   <jsp:setProperty name="msgs" property="baseName" value="org.sakaiproject.api.app.messagecenter.bundle.Messages"/>
</jsp:useBean>

<%
  	String thisId = request.getParameter("panel");
  	if (thisId == null) 
  	{
    	thisId = "Main" + org.sakaiproject.tool.cover.ToolManager.getCurrentPlacement().getId();
 	}
%>

<html>

<head>
<script type="text/javascript" src="/library/js/headscripts.js"></script>
<script src="js/frameAdjust.js" type="text/javascript"></script>
<script type="text/javascript">includeLatestJQuery("msgcntr");</script>



<script type="text/javascript"><!--
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


			SynMainLite.setupTableParsers();
			SynMainLite.setupTableHeaders();
			SynMainLite.setupTableSortImageOffset();
			//hide all checkboxes that are used to reset original values
			$(".unchangedValue").hide();
			SynMainLite.toggleHiddenRows();

		 	
		}
	}

	/*
	 * Turns off the refeshing of the wait screen
	 * when actual page being loaded.
	 */
	function unload() {
		// turn off animation when page finished
		window.clearInterval(intervalid);
		SynMainLite.setupTableParsers();
		SynMainLite.setupTableHeaders();
		SynMainLite.setupTableSortImageOffset();
		//hide all checkboxes that are used to reset original values
		$(".unchangedValue").hide();
		SynMainLite.toggleHiddenRows();
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
  	  <script type="text/javascript" src="/library/js/jquery/tablesorter/2.0.3/jquery.tablesorter-2.0.3.min.js"></script>
	  <sakai:script contextBase="/messageforums-tool" path="/js/synopticLite.js"/>
  	  <sakai:script contextBase="/messageforums-tool" path="/js/popupscripts.js"/>
  	  <script type="text/javascript">

//this function (setupTableParsers) setting has to be in the jsp page b/c of the msgs.syn_no_messages string.
var SynMainLite = SynMainLite || {};

SynMainLite.setupTableHeaders = function (){
	//since f:facet only allows one tag (no nested tags either) this will set up the hyperlink (only used to
	//make the user realize they can click the headers for sorting) to have the correct text from the msgs variable
	try{
	$("#hideHeader")[0].innerHTML = "<h:outputText value="#{msgs.syn_hide}"/>";
	$("#siteHeader")[0].innerHTML = '<h:outputText value="#{msgs.syn_site_heading}"/>';
	$("#messagesHeader")[0].innerHTML = '<h:outputText value="#{msgs.syn_private_heading}"/>';
	$("#forumsHeader")[0].innerHTML = '<h:outputText value="#{msgs.syn_discussion_heading}"/>';
	$("#showOptions")[0].innerHTML = '<h:outputText value="#{msgs.syn_options}"/>';
	}catch(e){
	}
};


SynMainLite.setupTableParsers = function (){

	 //add message count orderer
	 $.tablesorter.addParser({
	        id: 'newMessageCount',
	        is: function(s) {
	            return false;
	        },
	        format: function(s) {
	            //this is used to parse out the number of messages from the html, or 
	            //convert 'none' to the number 0, so we can order numberically
	            return s.toLowerCase().replace('<h:outputText value="#{msgs.syn_no_messages}"/>',0).replace(new RegExp('</a>$'), '').replace(new RegExp('<a.*>'),'').replace(new RegExp('<img.*>'),'');           
	        },
	        type: "numeric"
	    });  
	 //add title sorter
	    $.tablesorter.addParser({
	        id: 'title',
	        is: function(s) {
	            return false;
	        },
	        format: function(s) {
	            //this is used to parse out the number of messages from the html, or 
	            //convert 'none' to the number 0, so we can order numberically
	            return s.toLowerCase().replace(new RegExp('</a>$'), '').replace(new RegExp('<a.*>'),'');           
	        },
	        type: "text"
	    });
	    
	    //add checkbox sorter
	    $.tablesorter.addParser({
	        id: 'checkbox',
	        is: function(s) {
	            return false;
	        },
	        format: function(s) {
	            var integer = 0;
	            if (s.toLowerCase().match(/<input[^>]*checked*/i)) {
	                integer = 1;
	            }
	            return integer;
	        },
	        type: "numeric"
	    }); 
	    
	    //apply orderers to workspaceTable
	    $(".workspaceTable").tablesorter({ 
		    
	        headers: {
	    	0: { 
	    	    sorter:'checkbox' 
	    	},
	    	1: { 
	 	       sorter:'title' 
	    	}, 
	    	2: { 
		        sorter:'newMessageCount' 
		    }, 
	        3: { 
	            sorter:'newMessageCount' 
	        } 
	        } 
	    });

	};







	function resize(){
		mySetMainFrameHeightViewCell('<%= org.sakaiproject.util.Web.escapeJavascript(thisId)%>');
	}
	
	
function mySetMainFrameHeightViewCell(id)
{
	// run the script only if this window's name matches the id parameter
	// this tells us that the iframe in parent by the name of 'id' is the one who spawned us
	if (typeof window.name != "undefined" && id != window.name) return;

	var frame = parent.document.getElementById(id);
	if (frame)
	{

		var objToResize = (frame.style) ? frame.style : frame;
  
    // SAK-11014 revert           if ( false ) {

		var height; 		
		var offsetH = document.body.offsetHeight;
		var innerDocScrollH = null;

		if (typeof(frame.contentDocument) != 'undefined' || typeof(frame.contentWindow) != 'undefined')
		{
			// very special way to get the height from IE on Windows!
			// note that the above special way of testing for undefined variables is necessary for older browsers
			// (IE 5.5 Mac) to not choke on the undefined variables.
 			var innerDoc = (frame.contentDocument) ? frame.contentDocument : frame.contentWindow.document;
			innerDocScrollH = (innerDoc != null) ? innerDoc.body.scrollHeight : null;
		}
	
		if (document.all && innerDocScrollH != null)
		{
			// IE on Windows only
			height = innerDocScrollH;
		}
		else
		{
			// every other browser!
			height = offsetH;
		}
   // SAK-11014 revert		} 

   // SAK-11014 revert             var height = getFrameHeight(frame);

		// here we fudge to get a little bigger
		var newHeight = height + 40;

		// but not too big!
		if (newHeight > 32760) newHeight = 32760;

		// capture my current scroll position
		var scroll = findScroll();

		// resize parent frame (this resets the scroll as well)
		objToResize.height=newHeight + "px";

		// reset the scroll, unless it was y=0)
		if (scroll[1] > 0)
		{
			var position = findPosition(frame);
			parent.window.scrollTo(position[0]+scroll[0], position[1]+scroll[1]);
		}
	}
}
</script> 
  
  	<%-- Used to store where to redirect to so javascript can grab it. --%>
    <h:inputHidden id="longPageLoad" value="synMainLite" />

	<%-- Firefox browser needs to use AJAX to get actual page. Retrieved page then
		 stuffed into this div --%>
	<f:verbatim><div id="result"></f:verbatim>

<%  
  /* if MyWorkspace, display wait gif and message. if not, just redirect to synMain. */
  FacesContext context = FacesContext.getCurrentInstance();
  Application app = context.getApplication();
  ValueBinding binding = app.createValueBinding("#{mfSynopticBeanLite}");
  MessageForumSynopticBeanLite mfsb = (MessageForumSynopticBeanLite) binding.getValue(context);
  
  if (mfsb.isMyWorkspace() && mfsb.isDisableMyWorkspace().booleanValue()){
	  if(mfsb.getDisableMyWorkspaceDisabledMessage() != null && !"".equals(mfsb.getDisableMyWorkspaceDisabledMessage())){
%>	  
		<h:outputText value="#{mfSynopticBeanLite.disableMyWorkspaceDisabledMessage}"/>
<%	  
	  }else{
%>
		<h:outputText value="#{msgs.synopticToolDisabled}"/>		  
<%
	  }
  }else if (mfsb.isMyWorkspace() && mfsb.isUserRequestSynoptic().booleanValue()){
%>
<div>
<br>
<h:graphicImage url="#{mfSynopticBeanLite.serverUrl}/library/image/silk/email.png"/>
&nbsp;<a href="#" onclick="load();"><h:outputText value="#{msgs.viewSynopticInfo}"/></a>
</div>
<% 
	}else{
%>
	
	<script type="text/javascript"> 
	// Call javascript function to grab actual long loading page
	load();
	</script>
<%

   }
%>
  <f:verbatim></div></f:verbatim>

  </sakai:view>
</f:view>



</html>

