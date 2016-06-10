<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="t" %>






<jsp:useBean id="msgs" class="org.sakaiproject.util.ResourceLoader" scope="session">
   <jsp:setProperty name="msgs" property="baseName" value="org.sakaiproject.api.app.messagecenter.bundle.Messages"/>
</jsp:useBean>

<f:view>
  <sakai:view id="synopticView">
<script type="text/javascript">includeLatestJQuery("msgcntr");</script>
<script type="text/javascript" src="/library/js/jquery/tablesorter/2.0.3/jquery.tablesorter-2.0.3.min.js"></script>
<f:verbatim>
 <%
  	String thisId = request.getParameter("panel");
  	if (thisId == null) 
  	{
    	thisId = "Main" + org.sakaiproject.tool.cover.ToolManager.getCurrentPlacement().getId();
 	}
%>
<script type="text/javascript">

//this function (setupTableParsers) setting has to be in the jsp page b/c of the msgs.syn_no_messages string.
var SynMainLite = SynMainLite || {};
var messagesDisabled = <h:outputText value="#{mfSynopticBeanLite.disableMessages}"/>;
var forumsDisabled = <h:outputText value="#{mfSynopticBeanLite.disableForums}"/>;

SynMainLite.setupTableHeaders = function (){
	try{
	//since f:facet only allows one tag (no nested tags either) this will set up the hyperlink (only used to
	//make the user realize they can click the headers for sorting) to have the correct text from the msgs variable
	$("#hideHeader")[0].innerHTML = "<h:outputText value="#{msgs.syn_hide}"/>";
	$("#siteHeader")[0].innerHTML = '<h:outputText value="#{msgs.syn_site_heading}"/>';
	if(!messagesDisabled){
		$("#messagesHeader")[0].innerHTML = '<h:outputText value="#{msgs.syn_private_heading}"/>';
	}
	if(!forumsDisabled){
		$("#forumsHeader")[0].innerHTML = '<h:outputText value="#{msgs.syn_discussion_heading}"/>';
	}
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
	    
	    if(!messagesDisabled && !forumsDisabled){
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
	    }else if(messagesDisabled && forumsDisabled){
	    	 $(".workspaceTable").tablesorter({ 
				    
			        headers: {
			    	0: { 
			    	    sorter:'checkbox' 
			    	},
			    	1: { 
			 	       sorter:'title' 
			    	} 
			        } 
			    });
	    }else{
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
				    }
			        } 
			    });
	    }
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
 
 
 </f:verbatim>
<sakai:script contextBase="/messageforums-tool" path="/js/synopticLite.js"/>
<link rel="stylesheet" type="text/css" href="css/TableSorter.css" />
<link rel="stylesheet" type="text/css" href="css/msgcntr.css" />

	<h:form id="synopticForm">
		
		<%-- ===== Display when in MyWorkspace ===== --%>
		
		<f:subview id="myWorkspaceUnread" rendered="#{mfSynopticBeanLite.myWorkspace}">
			
			<f:subview id="optionsView" rendered="#{(mfSynopticBeanLite.myContentsSize > 0)}">
				<sakai:tool_bar>			
					<f:verbatim><a id="showOptions" name="showOptions" class="optionLink" href="#" onclick="$('.optionsTable').fadeIn(resize);$('.optionLink').parent().parent().toggle();$('.workspaceTable').fadeIn();$('.noActivity').fadeOut();$('.hideInfo').fadeIn();"></a></f:verbatim>			  	
		 		</sakai:tool_bar>
	
				
		  	</f:subview>
		  	
		  	
			
		  	<f:verbatim>
	 			<div class="hideInfo" style="display: none;"> 			
	 		</f:verbatim> 
				<h:outputText styleClass="information" value="#{msgs.syn_hide_info}"/><h:outputText styleClass="highlight" value="#{msgs.syn_hide_info_hidden_msg}"/>
		  	<f:verbatim>
		  		</div>
		  	</f:verbatim>
			
			<t:div styleClass="table-responsive" rendered="#{(mfSynopticBeanLite.myContentsSize > 0)}" style="margin-top:1em;">
				<t:dataTable id="myWorkspaceTable" value="#{mfSynopticBeanLite.contents}" var="eachSite" 
						styleClass="table table-striped table-bordered table-hover tablesorter workspaceTable">

					<t:column headerstyleClass="hideHeader">
						<f:facet name="header">
							<%-- Consult SynMainLite.setupTableHeaders for header text  --%>
							<f:verbatim><a href="" id="hideHeader" onclick="return false;"></a></f:verbatim>
						</f:facet>
						<h:selectBooleanCheckbox value="#{eachSite.synopticMsgcntrItem.hideItem}"/>
						<f:verbatim>
							<input type="checkbox" class="unchangedValue" 
						</f:verbatim>
						<f:subview id="checkbox" rendered="#{eachSite.synopticMsgcntrItem.hideItem}">
							<h:outputText value="Checked"/>
						</f:subview>
						<f:verbatim>
							>
						</f:verbatim>
					</t:column>


					<t:column rendered="#{mfSynopticBeanLite.performance != '2' && !mfSynopticBeanLite.disableMessages}" headerstyleClass="messagesHeader">
						<f:facet name="header">
							<%-- Consult SynMainLite.setupTableHeaders for header text  --%>
							<f:verbatim><a href="" id="messagesHeader" onclick="return false;"></a></f:verbatim>
						</f:facet>

						<h:panelGroup>
							<%-- === To create a link to Messages (& Forums) home page === --%>
							<h:outputText value="#{msgs.syn_no_messages}" rendered="#{eachSite.newMessagesCount == 0 && !eachSite.doesMessagesExist}" />
							<h:outputText value="#{msgs.syn_tool_link_begin}#{eachSite.messagesUrl}';\">#{msgs.syn_no_messages}</a>" 
								escape="false" title="#{msgs.syn_goto_messages}" rendered="#{eachSite.newMessagesCount == 0 && eachSite.doesMessagesExist}"/>						
							
							<h:outputText value="#{msgs.syn_tool_link_begin}#{eachSite.messagesUrl}';\">#{eachSite.newMessagesCount}</a>" 
								escape="false" title="#{msgs.syn_goto_messages}" rendered="#{eachSite.newMessagesCount > 0}"/>
							
							<h:outputText value="  " rendered="#{eachSite.newMessagesCount > 0}" />
		
				 			<h:graphicImage url="#{mfSynopticBeanLite.serverUrl}/library/image/silk/email.png" rendered="#{eachSite.newMessagesCount > 0}" />
							<%--<h:outputText value="  "/>	
									
							<h:graphicImage url="#{mfSynopticBeanLite.serverUrl}/library/image/sakai/information_sm.png" title="#{msgs.syn_date_heading}: #{eachSite.messagesFormattedDate}"/> --%>
						</h:panelGroup>
					</t:column>

					<t:column rendered="#{mfSynopticBeanLite.performance == '2' && !mfSynopticBeanLite.disableMessages}" headerstyleClass="messagesHeader">
						<f:facet name="header">
							<%-- Consult SynMainLite.setupTableHeaders for header text  --%>
							<f:verbatim><a href="" id="messagesHeader" onclick="return false;"></a></f:verbatim>
	 					</f:facet>
						<h:panelGroup>	
							<h:outputText value="#{msgs.syn_no_messages}" rendered="#{eachSite.newMessagesCount == 0}"/>
							<h:outputText value="#{eachSite.newMessagesCount}" rendered="#{eachSite.newMessagesCount > 0}"/>
							<h:outputText value="  " rendered="#{eachSite.newMessagesCount > 0}" />
				 			<h:graphicImage url="#{mfSynopticBeanLite.serverUrl}/library/image/silk/email.png" rendered="#{eachSite.newMessagesCount > 0}" />
						</h:panelGroup>
					</t:column>
					<t:column rendered="#{mfSynopticBeanLite.performance != '2' && !mfSynopticBeanLite.disableForums}" headerstyleClass="forumsHeader">
						<f:facet name="header">
							<%-- Consult SynMainLite.setupTableHeaders for header text  --%>
							<f:verbatim><a href="" id="forumsHeader" onclick="return false;"></a></f:verbatim>
	 					</f:facet>

						<h:panelGroup>
							<%-- === To create a link to (Messages &) Forums home page === --%>
						
							<h:outputText value="#{msgs.syn_no_messages}" rendered="#{eachSite.newForumCount == 0 && !eachSite.doesForumsExist}" />
							<h:outputText value="#{msgs.syn_tool_link_begin}#{eachSite.forumUrl}';\">#{msgs.syn_no_messages}</a>" 
								escape="false" title="#{msgs.syn_goto_forums}" rendered="#{eachSite.newForumCount == 0 && eachSite.doesForumsExist}"/>

							<h:outputText value="#{msgs.syn_tool_link_begin}#{eachSite.forumUrl}';\">#{eachSite.newForumCount}</a>" 
								escape="false" title="#{msgs.syn_goto_forums}" rendered="#{eachSite.newForumCount > 0}"/>

							<h:outputText value="  " rendered="#{eachSite.newForumCount > 0}" />

							<h:graphicImage url="#{mfSynopticBeanLite.serverUrl}/library/image/silk/email.png" rendered="#{eachSite.newForumCount > 0}" />
							<%--<h:outputText value="  "/>
							<h:graphicImage url="#{mfSynopticBeanLite.serverUrl}/library/image/sakai/information_sm.png" title="#{msgs.syn_date_heading}: #{eachSite.forumFormattedDate}"/> --%>
						</h:panelGroup>
					</t:column>
					<t:column rendered="#{mfSynopticBeanLite.performance == '2' && !mfSynopticBeanLite.disableForums}" headerstyleClass="forumsHeader">
						<f:facet name="header">
							<%-- Consult SynMainLite.setupTableHeaders for header text  --%>
							<f:verbatim><a href="" id="forumsHeader" onclick="return false;"></a></f:verbatim>
	 					</f:facet>
						<h:panelGroup>
							<h:outputText value="#{msgs.syn_no_messages}" rendered="#{eachSite.newForumCount == 0}"/>
							<h:outputText value="#{eachSite.newForumCount}" rendered="#{eachSite.newForumCount > 0}"/>
							<h:outputText value="  " rendered="#{eachSite.newForumCount > 0}" />
			 				<h:graphicImage url="#{mfSynopticBeanLite.serverUrl}/library/image/silk/email.png" rendered="#{eachSite.newForumCount > 0}" />
						</h:panelGroup>
					</t:column>
					<t:column headerstyleClass="siteHeader">
						<f:facet name="header">
							<%-- Consult SynMainLite.setupTableHeaders for header text  --%>
							<f:verbatim><a href="" id="siteHeader" onclick="return false;"></a></f:verbatim>
						</f:facet>				
						<h:outputLink value="#{eachSite.siteUrl}" target="_parent" title="#{msgs.syn_goto_site}"><h:outputText value="#{eachSite.synopticMsgcntrItem.siteTitle}"/></h:outputLink>
					</t:column>					
		 		</t:dataTable>
		 	</t:div>
		 	
	 		<f:verbatim>
	 			<div class="optionsTable" style="display: none;"> 			
	 			<br>
	 			<br>
	 		</f:verbatim>
	 		<h:commandButton id="update" value="#{msgs.syn_update}" action="#{mfSynopticBeanLite.proccessActionSaveChanges}"/>	 		
	 		<f:verbatim>
	 			<input type="button" id="cancel" value='</f:verbatim><h:outputText value="#{msgs.syn_cancel}"/><f:verbatim>' onclick="$('.optionsTable').fadeOut(resize);$('.optionLink').parent().parent().toggle();SynMainLite.resetCheckboxes();$('.hideInfo').fadeOut();if(SynMainLite.getCount() == 1){$('.noActivity').fadeIn();$('.workspaceTable').fadeOut();};">
	 		</f:verbatim> 		
	 		<f:verbatim> 		
	 			</div> 			
	 		</f:verbatim>
		
			<f:subview id="noActivityView"  rendered="#{mfSynopticBeanLite.myDisplayedSites < 1}">
				<f:verbatim>	 			
		 			<br>
		 		</f:verbatim>
				<h:outputText value="#{msgs.synoptic_no_activity}" styleClass="noActivity"/>  
			</f:subview>
		</f:subview>


		<!-- Site Home Page -->
		<t:div styleClass="table-responsive" rendered="#{!mfSynopticBeanLite.myWorkspace && mfSynopticBeanLite.anyMFToolInSite}">
			<h:panelGrid columns="2" styleClass="table table-bordered table-hover table-striped">
		
				<h:panelGroup rendered="#{mfSynopticBeanLite.messageForumsPageInSite || mfSynopticBeanLite.messagesPageInSite}">
					<h:outputText value="#{msgs.syn_tool_link_begin}#{mfSynopticBeanLite.siteHomepageContent.messagesUrl}';\">#{msgs.syn_private_heading}</a>" 
							escape="false" title="#{msgs.syn_goto_messages}"/>
				</h:panelGroup>
				
				<h:panelGroup rendered="#{mfSynopticBeanLite.messageForumsPageInSite || mfSynopticBeanLite.messagesPageInSite}">
					<h:outputText value="#{msgs.syn_no_messages}" rendered="#{mfSynopticBeanLite.siteHomepageContent.newMessagesCount == 0}" />
					<h:panelGroup rendered="#{mfSynopticBeanLite.siteHomepageContent.newMessagesCount > 0}" >
				
						<h:outputText value="#{msgs.syn_tool_link_begin}#{mfSynopticBeanLite.siteHomepageContent.messagesUrl}';\">#{mfSynopticBeanLite.siteHomepageContent.newMessagesCount}</a>"
							escape="false" title="#{msgs.syn_goto_messages}" rendered="#{mfSynopticBeanLite.siteHomepageContent.newMessagesCount > 0}" />
					<h:outputText value="  " rendered="true" />
					<h:outputText value="  " rendered="#{mfSynopticBeanLite.siteHomepageContent.newMessagesCount > 0}" />
		 			<h:graphicImage url="#{mfSynopticBeanLite.serverUrl}/library/image/silk/email.png" rendered="#{mfSynopticBeanLite.siteHomepageContent.newMessagesCount > 0}" />
					</h:panelGroup>

					<%--<h:outputText value="  "/>
					<h:graphicImage url="#{mfSynopticBeanLite.serverUrl}/library/image/sakai/information_sm.png" title="#{msgs.syn_date_heading}: #{mfSynopticBeanLite.siteHomepageContent.messagesFormattedDate}"/>  --%>
				</h:panelGroup>
		
				<h:panelGroup  rendered="#{mfSynopticBeanLite.messageForumsPageInSite || mfSynopticBeanLite.forumsPageInSite}">
					<h:outputText value="#{msgs.syn_tool_link_begin}#{mfSynopticBeanLite.siteHomepageContent.forumUrl}';\">#{msgs.syn_discussion_heading}</a>"
						escape="false" title="#{msgs.syn_goto_forums}" />
				</h:panelGroup>

				<h:panelGroup  rendered="#{mfSynopticBeanLite.messageForumsPageInSite || mfSynopticBeanLite.forumsPageInSite}">
					<h:outputText value="#{msgs.syn_no_messages}" rendered="#{mfSynopticBeanLite.siteHomepageContent.newForumCount == 0}" />
			
					<h:outputText value="#{msgs.syn_tool_link_begin}#{mfSynopticBeanLite.siteHomepageContent.forumUrl}';\">#{mfSynopticBeanLite.siteHomepageContent.newForumCount}</a>" 
								escape="false" title="#{msgs.syn_goto_forums}" rendered="#{mfSynopticBeanLite.siteHomepageContent.newForumCount > 0}"/>
			
					<h:outputText value="  " rendered="#{mfSynopticBeanLite.siteHomepageContent.newForumCount > 0}" />

					<h:graphicImage url="#{mfSynopticBeanLite.serverUrl}/library/image/silk/email.png" rendered="#{mfSynopticBeanLite.siteHomepageContent.newForumCount > 0}" />
					
					<%--			<h:outputText value="  "/>
					
					<h:graphicImage url="#{mfSynopticBeanLite.serverUrl}/library/image/sakai/information_sm.png" title="#{msgs.syn_date_heading}: #{mfSynopticBeanLite.siteHomepageContent.forumFormattedDate}"/>  --%>
				</h:panelGroup>
			</h:panelGrid>
		</t:div>
		
	<h:panelGrid columns="1" styleClass="listHier lines nolines"
		rendered="#{!mfSynopticBeanLite.myWorkspace && !mfSynopticBeanLite.anyMFToolInSite}" >
	  	<h:outputText value="#{msgs.synoptic_no_tools}"/>
	</h:panelGrid>

    </h:form> 
    
	<script type="text/javascript">
		//Put this code in the jsp page since IE8 was having trouble with $(document).ready() function.
		SynMainLite.setupTableParsers();
		SynMainLite.setupTableHeaders();
		SynMainLite.setupTableSortImageOffset();
		//hide all checkboxes that are used to reset original values
		$(".unchangedValue").hide();
		SynMainLite.toggleHiddenRows();
	</script>
  </sakai:view>
 </f:view> 
 
 
