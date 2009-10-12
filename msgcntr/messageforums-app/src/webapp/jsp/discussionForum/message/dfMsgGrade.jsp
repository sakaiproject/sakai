<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
<%@ taglib uri="http://sakaiproject.org/jsf/messageforums" prefix="mf" %>
<jsp:useBean id="msgs" class="org.sakaiproject.util.ResourceLoader" scope="session">
   <jsp:setProperty name="msgs" property="baseName" value="org.sakaiproject.api.app.messagecenter.bundle.Messages"/>
</jsp:useBean>

<f:view>
	<sakai:view toolCssHref="/sakai-messageforums-tool/css/msgcntr.css">
       		<script type="text/javascript" src="/library/js/jquery.js"></script>
       		<sakai:script contextBase="/sakai-messageforums-tool" path="/js/sak-10625.js"/>
  <h:form id="msgForum">
<!--jsp\discussionForum\message\dfMsgGrade.jsp-->
      <h3><h:outputText value="#{msgs.cdfm_grade_msg}" /></h3>
			<h4>
				<h:outputText value="#{ForumTool.selectedForum.forum.title}" />
				<h:outputText value=" #{msgs.cdfm_dash} " /> 
				<h:outputText	value="#{ForumTool.selectedTopic.topic.title}" />
			</h4>
			<h:messages globalOnly="true" infoClass="success" errorClass="alertMessage" />
			  
			<div class="singleMessage">
				<h:outputText value="#{ForumTool.selectedMessage.message.title}" styleClass="title"/>
				<h:outputText value="#{ForumTool.selectedMessage.message.author}" />
				<h:outputText value=" #{msgs.cdfm_openb} " />
				<h:outputText value="#{ForumTool.selectedMessage.message.created}" >
					<f:convertDateTime pattern="#{msgs.date_format}" />  
				</h:outputText>
				<h:outputText value=" #{msgs.cdfm_closeb}" />
				<%-- Attachments --%>
				<h:dataTable value="#{ForumTool.selectedMessage.attachList}"	var="eachAttach" rendered="#{!empty ForumTool.selectedMessage.attachList}">
					<h:column	rendered="#{!empty ForumTool.selectedMessage.message.attachments}">
						<sakai:contentTypeMap fileType="#{eachAttach.attachment.attachmentType}" mapType="image" var="imagePath" pathPrefix="/library/image/"/>
  						<h:graphicImage id="exampleFileIcon" value="#{imagePath}" />
						<h:outputLink value="#{eachAttach.url}" target="_new_window">
							<h:outputText value="#{eachAttach.attachment.attachmentName}" />
						</h:outputLink>
					</h:column>
				</h:dataTable>
				<h:outputText escape="false" value="#{ForumTool.selectedMessage.message.body}"  style="display:block;margin-top:2em" styleClass="textPanel"/>
			</div>	


			
			<p class="instruction" style="margin-top:1em">
  	    <h:outputText value="#{msgs.cdfm_required}"rendered="#{ForumTool.allowedToGradeItem}"/> 
  	    <h:outputText value="#{msgs.pvt_star}" styleClass="reqStarInline" rendered="#{ForumTool.allowedToGradeItem}" />
			</p>
   
			<h:panelGrid styleClass="jsfFormTable" columns="1" columnClasses="shorttext" border="0">
				<h:panelGroup>
					<h:outputLabel for="assignment"  style="padding-bottom:.3em;display:block;clear:both;float:none;white-space:nowrap"  rendered="#{ForumTool.allowedToGradeItem}">
						<h:outputText value="#{msgs.cdfm_info_required_sign}" styleClass="reqStarInline" style="padding-right:3px"/>
						<h:outputText  value="#{msgs.cdfm_assignments}"/>
					</h:outputLabel>	
        <h:selectOneMenu id="assignment" value="#{ForumTool.selectedAssign}" valueChangeListener="#{ForumTool.processGradeAssignChange}"
              onchange="document.forms[0].submit();">
           <f:selectItems value="#{ForumTool.assignments}" />
         </h:selectOneMenu>
         <h:outputFormat value=" #{msgs.cdfm_points_possible}" rendered="#{!ForumTool.selGBItemRestricted && ForumTool.gbItemPointsPossible != null}">
						<f:param value="#{ForumTool.gbItemPointsPossible}"/>
					</h:outputFormat>
      </h:panelGroup>
      <h:outputText value="" rendered="#{ForumTool.selGBItemRestricted && !ForumTool.noItemSelected}" />
      <h:outputText value="#{msgs.cdfm_no_gb_perm}" rendered="#{ForumTool.selGBItemRestricted && !ForumTool.noItemSelected}" 
      		styleClass="alertMessage"/>
				<h:panelGroup  rendered="#{!ForumTool.selGBItemRestricted}">
					<h:outputLabel for="dfMsgGradeGradePoint" rendered="#{ForumTool.allowedToGradeItem}"  style="padding-bottom:.3em;display:block;clear:both;float:none;white-space:nowrap">
						<h:outputText value="#{msgs.cdfm_info_required_sign}" styleClass="reqStarInline"  style="padding-right:3px" />
						<h:outputText  value="#{msgs.cdfm_grade_points}" />
					</h:outputLabel>	
					<h:panelGroup >
	    		<h:inputText value="#{ForumTool.gradePoint}" id="dfMsgGradeGradePoint" size="5" rendered="#{ForumTool.allowedToGradeItem}" />
						<h:message for="dfMsgGradeGradePoint" styleClass="alertMessage"  rendered="#{ForumTool.allowedToGradeItem}"/>
	      </h:panelGroup>
	      
	      <h:panelGroup rendered="#{!ForumTool.allowedToGradeItem}" >
	        <h:outputText value="#{ForumTool.gradePoint}" rendered="#{ForumTool.gradePoint != null}" />
	        <h:outputText value="#{msgs.cdfm_null_points}" rendered="#{ForumTool.gradePoint == null}" />
				</h:panelGroup>
      </h:panelGroup>
				<h:panelGroup>
					<h:outputLabel  for="comments" value="#{msgs.cdfm_comments}" 
							  style="padding-bottom:.3em;display:block;clear:both;float:none;white-space:nowrap"  rendered="#{!ForumTool.selGBItemRestricted}"/>
       <h:inputTextarea id="comments" value="#{ForumTool.gradeComment}" rows="5" cols="50"
       		rendered="#{!ForumTool.selGBItemRestricted}" readonly="#{!ForumTool.allowedToGradeItem}"/>
				</h:panelGroup>	
    </h:panelGrid>

    <sakai:button_bar>
      <sakai:button_bar_item action="#{ForumTool.processDfGradeSubmit}" value="#{msgs.cdfm_submit_grade}" 
      		accesskey="s" styleClass="active" disabled="#{!ForumTool.allowedToGradeItem}"/>
      <sakai:button_bar_item action="#{ForumTool.processDfGradeCancel}" value="#{msgs.cdfm_cancel}" accesskey="x" />
    </sakai:button_bar>
  </h:form>
</sakai:view>
</f:view>
