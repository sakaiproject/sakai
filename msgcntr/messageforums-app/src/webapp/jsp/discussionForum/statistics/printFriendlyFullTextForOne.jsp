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
					 
			 <f:verbatim><div class="breadCrumb"><h3></f:verbatim>
			 	<h:outputText value="#{msgs.cdfm_discussion_forums}"/>
			      <f:verbatim><h:outputText value=" " /><h:outputText value=" / " /><h:outputText value=" " /></f:verbatim>
			      	<h:outputText value="#{msgs.stat_list}"/>   
			      <f:verbatim><h:outputText value="" /><h:outputText value=" / " /><h:outputText value=" " /></f:verbatim>
			      <h:outputText value="#{mfStatisticsBean.selectedSiteUser}"/> 
			       <f:verbatim><h:outputText value=" " /><h:outputText value=" / " /><h:outputText value=" " /></f:verbatim>
			       <h:outputText value="#{msgs.stat_authored}"/>   
			        <f:verbatim><h:outputText value=" " /><h:outputText value=" / " /><h:outputText value=" " /></f:verbatim>
			       <h:outputText value="#{mfStatisticsBean.selectedMsgSubject}" />
			    <f:verbatim></h3></div></f:verbatim>
  
  		<h:dataTable id="subjectBody" value="#{mfStatisticsBean.userSubjectMsgBody}" var="stat" styleClass="listHier" cellpadding="0" cellspacing="0" width="100%" columnClasses="bogus">	
   			<h:column rendered="#{!stat.msgDeleted}">
   				<f:verbatim><div class="hierItemBlock"></f:verbatim>
   				<f:verbatim><h4 class="textPanelHeader specialLink" style="width:100%"></f:verbatim>
                                  		
					<h:outputText value="#{stat.forumTitle}" />
					 <f:verbatim><h:outputText value="/" /></f:verbatim>
					<h:outputText value="#{stat.topicTitle}" />
					 <f:verbatim><h:outputText value="/" /></f:verbatim>
					<h:outputText  value= "#{stat.forumSubject} " />
			
					<h:outputText value="#{stat.forumDate}">
						<f:convertDateTime pattern="#{msgs.date_format_paren}" />
					</h:outputText>
					<h:panelGroup>
				
				</h:panelGroup>
			 <f:verbatim></h4></f:verbatim>
		
				<mf:htmlShowArea value="#{stat.message}" hideBorder="true" />
				<f:verbatim></div></f:verbatim>
  			</h:column>
  			
  			<h:column rendered="#{stat.msgDeleted}">
   				<f:verbatim><div class="hierItemBlock"></f:verbatim>
   				<f:verbatim><h4 class="textPanelHeader specialLink" style="width:100%"></f:verbatim>
   				
					<h:panelGroup styleClass="inactive">
						<f:verbatim><span></f:verbatim>
						<h:outputText value="#{msgs.cdfm_msg_deleted_label}" />
						<f:verbatim></span></f:verbatim>
 					</h:panelGroup>
			
				<f:verbatim></h4></f:verbatim>
				<mf:htmlShowArea value="" hideBorder="true" />
				<f:verbatim></div></f:verbatim>
  			</h:column>
  			
  		</h:dataTable>

  	</h:form>
  </sakai:view>
 </f:view>
  	