<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
<%@ taglib uri="http://sakaiproject.org/jsf/messageforums" prefix="mf" %>
<jsp:useBean id="msgs" class="org.sakaiproject.util.ResourceLoader" scope="session">
   <jsp:setProperty name="msgs" property="baseName" value="org.sakaiproject.api.app.messagecenter.bundle.Messages"/>
</jsp:useBean>

<f:view>
<sakai:view>
	<h:form id="msgForum">
<!--jsp/discussionForum/message/printFriendlyThread.jsp-->
		
			<div>
				<a id="printIcon" href="" onClick="javascript:window.print();">
					<h:graphicImage url="/../../library/image/silk/printer.png" alt="#{msgs.print_friendly}" title="#{msgs.print_friendly}" />
					<h:outputText value="#{msgs.send_to_printer}" />
				</a>
				<h:outputText value=" " /><h:outputText value="|" /><h:outputText value=" " />
				<a value="" href="" onClick="window.close();" >
					<h:outputText value="#{msgs.close_window}" />
				</a>
			</div>
		
			    <h:panelGroup>
					<f:verbatim><div class="breadCrumb specialLink"><h3></f:verbatim>
			      <h:outputText value="#{msgs.cdfm_discussion_forums}" />
      			  <f:verbatim><h:outputText value=" " /><h:outputText value=" / " /><h:outputText value=" " /></f:verbatim>
				  <h:outputText value="#{ForumTool.selectedForum.forum.title}" />
				  <f:verbatim><h:outputText value=" " /><h:outputText value=" / " /><h:outputText value=" " /></f:verbatim>
				  	  <h:outputText value="#{ForumTool.selectedTopic.topic.title}" />
			  	  <f:verbatim><h:outputText value=" " /><h:outputText value=" / " /><h:outputText value=" " /></f:verbatim>
				  	  <h:outputText value="#{ForumTool.selectedThreadHead.message.title}" />
				  <f:verbatim></h3></div></f:verbatim>
				 </h:panelGroup>
		
	
		<%--rjlowe: Expanded View to show the message bodies, but not threaded --%>
		<h:dataTable id="expandedMessages" value="#{ForumTool.PFSelectedThread}" var="message" rendered="#{!ForumTool.threaded}"
   	 		styleClass="listHier" cellpadding="0" cellspacing="0" width="100%" columnClasses="bogus">
			<h:column>
				<f:verbatim><div class="hierItemBlock"></f:verbatim>

					<f:verbatim><h4 class="textPanelHeader specialLink" style="width:100%"></f:verbatim>
                                                  
                        <h:outputText value="#{message.message.title}" />		          	
			          	<h:outputText value=" - #{message.message.author}"/>
   
                        <h:outputText value="#{message.message.created}">
  				   	         <f:convertDateTime pattern="#{msgs.date_format_paren}" />
            			</h:outputText>

                     <f:verbatim></h4></f:verbatim>

		  			<mf:htmlShowArea value="#{message.message.body}" hideBorder="true" />
				<f:verbatim></div></f:verbatim>
			</h:column>
		</h:dataTable>
		
		<%--rjlowe: Expanded View to show the message bodies, threaded --%>
		<mf:hierDataTable id="expandedThreadedMessages" value="#{ForumTool.PFSelectedThread}" var="message" rendered="#{ForumTool.threaded}"
   	 		noarrows="true" styleClass="listHier" cellpadding="0" cellspacing="0" width="100%" columnClasses="bogus">
			<h:column id="_msg_subject">
				<f:verbatim><div class="hierItemBlock"></f:verbatim>

					<f:verbatim><h4 class="textPanelHeader specialLink" style="width:100%"></f:verbatim>
                                                  
                        <h:outputText value="#{message.message.title}" />		          	
			          	<h:outputText value=" - #{message.message.author}"/>
   
                        <h:outputText value="#{message.message.created}">
  				   	         <f:convertDateTime pattern="#{msgs.date_format_paren}" />
            			</h:outputText>

                     <f:verbatim></h4></f:verbatim>

		  			<mf:htmlShowArea value="#{message.message.body}" hideBorder="true" />
				<f:verbatim></div></f:verbatim>
			</h:column>
		</mf:hierDataTable>
	
	</h:form>
</sakai:view>
</f:view>