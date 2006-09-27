<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
<%@ taglib uri="http://sakaiproject.org/jsf/messageforums" prefix="mf" %>
<f:loadBundle basename="org.sakaiproject.tool.messageforums.bundle.Messages" var="msgs"/>

<f:view>
<sakai:view>
  <h:form id="DF-1">
<!--jsp\discussionForum\message\dfMsgGrade.jsp-->

      <h:panelGrid columns="2" summary="layout" width="100%"  styleClass="navPanel">
		    <h:panelGroup>
			<f:verbatim><div class="breadCrumb specialLink"><h3></f:verbatim>
		      <h:commandLink action="#{ForumTool.processActionHome}" value="#{msgs.cdfm_message_forums}" title=" #{msgs.cdfm_message_forums}" /> 
          <f:verbatim><h:outputText value=" " /><h:outputText value=" / " /><h:outputText value=" " /></f:verbatim>
          <h:commandLink action="#{ForumTool.processActionDisplayForum}" value="#{ForumTool.selectedForum.forum.title}" title=" #{ForumTool.selectedForum.forum.title}">
            <f:param value="#{ForumTool.selectedForum.forum.id}" name="forumId"/>
          </h:commandLink> 
          <f:verbatim><h:outputText value=" " /><h:outputText value=" / " /><h:outputText value=" " /></f:verbatim>
          <h:commandLink action="#{ForumTool.processActionDisplayTopic}" value="#{ForumTool.selectedTopic.topic.title}"  title=" #{ForumTool.selectedTopic.topic.title}">
            <f:param value="#{ForumTool.selectedForum.forum.id}" name="forumId"/>
            <f:param value="#{ForumTool.selectedTopic.topic.id}" name="topicId"/>
          </h:commandLink>
		  <f:verbatim></h3></div></f:verbatim>
			 </h:panelGroup>
			 <h:panelGroup styleClass="itemNav">
			   <h:outputText   value="#{msgs.cdfm_previous_topic}   "  rendered="#{!ForumTool.selectedTopic.hasPreviousTopic}" />
          <h:commandLink action="#{ForumTool.processActionDisplayPreviousTopic}" value="#{msgs.cdfm_previous_topic}   "  
                         rendered="#{ForumTool.selectedTopic.hasPreviousTopic}" title=" #{msgs.cdfm_previous_topic}">
            <f:param value="#{ForumTool.selectedTopic.previousTopicId}" name="previousTopicId"/>
            <f:param value="#{ForumTool.selectedForum.forum.id}" name="forumId"/>
          </h:commandLink>
          <f:verbatim><h:outputText value=" | " /> </f:verbatim>
          <h:outputText  value="#{msgs.cdfm_next_topic}   " rendered="#{!ForumTool.selectedTopic.hasNextTopic}" />
          <h:commandLink action="#{ForumTool.processActionDisplayNextTopic}" value="#{msgs.cdfm_next_topic}   " 
                         rendered="#{ForumTool.selectedTopic.hasNextTopic}" title=" #{msgs.cdfm_next_topic}">
            <f:param value="#{ForumTool.selectedTopic.nextTopicId}" name="nextTopicId"/>
            <f:param value="#{ForumTool.selectedForum.forum.id}" name="forumId"/>
          </h:commandLink>
			 </h:panelGroup>
		  </h:panelGrid> 
      
			<p class="textPanel">
				<h:outputText value="#{ForumTool.selectedTopic.topic.shortDescription}" />
		  </p>
			<p class="textPanelFooter specialLink">
				<h:commandLink immediate="true" action="#{ForumTool.processActionToggleDisplayExtendedDescription}" rendered="#{ForumTool.selectedTopic.hasExtendedDesciption}"
						id="topic_extended_show" value="#{msgs.cdfm_read_full_description}" title="#{msgs.cdfm_read_full_description}">
						<f:param value="#{topic.topic.id}" name="topicId"/>
						<f:param value="processActionGradeMessage" name="redirectToProcessAction"/>
					</h:commandLink>
					<h:commandLink immediate="true" action="#{ForumTool.processActionToggleDisplayExtendedDescription}" id="topic_extended_hide"
						 value="#{msgs.cdfm_hide_full_description}" rendered="#{ForumTool.selectedTopic.readFullDesciption}" title="#{msgs.cdfm_hide_full_description}">
						<f:param value="#{ForumTool.selectedTopic.topic.id}" name="topicId"/>
						<f:param value="processActionGradeMessage" name="redirectToProcessAction"/>
					</h:commandLink>
				</p>

     <mf:htmlShowArea  id="topic_extended_description" hideBorder="false"	value="#{ForumTool.selectedTopic.topic.extendedDescription}" rendered="#{ForumTool.selectedTopic.readFullDesciption}"/> 
				 
		<br />
      
    <h:panelGrid styleClass="itemSummary" columns="2" summary="layout">
      <h:outputText value="#{msgs.cdfm_subject}"/>
      <h:outputText value="#{ForumTool.selectedMessage.message.title}" /> 
      
      <h:outputText value="#{msgs.cdfm_authoredby}"/>
      <h:outputText value="#{ForumTool.selectedMessage.message.author} #{msgs.cdfm_openb} #{ForumTool.selectedMessage.message.created} #{msgs.cdfm_closeb}" /> 

       <%-- gsilver:moving rendered attribute from column in data table to 2 h:panelGroups - if there are no attachments - do not create an empty row with the attachments label--%>
      <h:panelGroup styleClass="header"   rendered="#{!empty ForumTool.selectedMessage.message.attachments}">
        <h:outputText value="#{msgs.cdfm_att}"/>
      </h:panelGroup>
      <h:panelGroup  rendered="#{!empty ForumTool.selectedMessage.message.attachments}">
        <%-- Attachments --%>
          <h:dataTable value="#{ForumTool.selectedMessage.message.attachments}" var="eachAttach" >
            <h:column rendered="#{!empty ForumTool.selectedMessage.message.attachments}">
              <h:graphicImage url="/images/excel.gif" rendered="#{eachAttach.attachmentType == 'application/vnd.ms-excel'}" alt="" />
              <h:graphicImage url="/images/html.gif" rendered="#{eachAttach.attachmentType == 'text/html'}" alt="" />
              <h:graphicImage url="/images/pdf.gif" rendered="#{eachAttach.attachmentType == 'application/pdf'}" alt="" />
              <h:graphicImage url="/sakai-messageforums-tool/images/ppt.gif" rendered="#{eachAttach.attachmentType == 'application/vnd.ms-powerpoint'}" alt="" />
              <h:graphicImage url="/images/text.gif" rendered="#{eachAttach.attachmentType == 'text/plain'}" alt="" />
              <h:graphicImage url="/images/word.gif" rendered="#{eachAttach.attachmentType == 'application/msword'}" alt="" />
                 
              <h:outputLink value="#{eachAttach.attachmentUrl}" target="_new_window">
                <h:outputText value="#{eachAttach.attachmentName}"/>
              </h:outputLink>
              
            </h:column>
          </h:dataTable>
      </h:panelGroup>
    </h:panelGrid>
    
    <hr class="itemSeparator" />
    
    <div class="instruction">
  	  <h:outputText value="#{msgs.cdfm_required}"/> <h:outputText value="#{msgs.pvt_star}" styleClass="reqStarInline" />
		</div>
   
    <h:panelGrid styleClass="jsfFormTable" columns="2">
      <h:panelGroup styleClass="shorttext" rendered="#{ ForumTool.gradebookScore != null && ForumTool.gradebookScore != ''}">
          <h:outputText value="#{msgs.cdfm_curr_score}"/>
      </h:panelGroup>
      <h:panelGroup styleClass="shorttext" rendered="#{ ForumTool.gradebookScore != null && ForumTool.gradebookScore != ''}">
          <h:outputText value="#{ForumTool.gradebookScore}"/>
      </h:panelGroup>
      
      <h:panelGroup styleClass="shorttext required">
				<h:outputText value="#{msgs.cdfm_info_required_sign}" styleClass="reqStar"/>  
        <h:outputLabel for="dfMsgGradeGradePoint" value="#{msgs.cdfm_grade_points}"/>
      </h:panelGroup>
      <h:panelGroup styleClass="shorttext">
        <h:inputText value="#{ForumTool.gradePoint}" id="dfMsgGradeGradePoint" size="5">
<%--              <f:validateDoubleRange minimum="0"/>--%>
        </h:inputText>
        <h:message for="dfMsgGradeGradePoint" styleClass="alertMessage" />
      </h:panelGroup>
      
      <h:panelGroup styleClass="shorttext required">
				<h:outputText value="#{msgs.cdfm_info_required_sign}" styleClass="reqStar"/> 
        <h:outputLabel for="assignment" value="#{msgs.cdfm_assignments}"/>
      </h:panelGroup>
      <h:panelGroup styleClass="shorttext">
        <h:selectOneMenu id="assignment" value="#{ForumTool.selectedAssign}" valueChangeListener="#{ForumTool.processGradeAssignChange}"
              onchange="document.forms[0].submit();">
           <f:selectItems value="#{ForumTool.assignments}" />
         </h:selectOneMenu>
      </h:panelGroup>

        <h:outputLabel styleClass="shorttext required" for="comments" value="#{msgs.cdfm_comments}" style="display:block"/>
        <h:inputTextarea id="comments" value="#{ForumTool.gradeComment}" rows="5" cols="50"/>

      <h:panelGroup styleClass="checkbox">
        <h:selectBooleanCheckbox id="notification" value="#{ForumTool.gradeNotify}"/>
        <h:outputLabel for="notification" value="#{msgs.cdfm_notification}"/>
      </h:panelGroup>
    </h:panelGrid>
   
    <br/>

    <h:panelGroup rendered="#{ForumTool.noAssignWarn}">
      <h:outputText styleClass="alertMessage"  
        value="#{msgs.cdfm_no_assign_for_grade}" />
    </h:panelGroup>

    <h:panelGroup rendered="#{ForumTool.noGradeWarn}">
      <h:outputText styleClass="alertMessage" value="#{msgs.cdfm_no_points_for_grade}" />
    </h:panelGroup>

    <sakai:button_bar>
      <sakai:button_bar_item action="#{ForumTool.processDfGradeSubmit}" value="#{msgs.cdfm_submit_grade}" accesskey="s" styleClass="active"/>
      <sakai:button_bar_item action="#{ForumTool.processDfGradeCancel}" value="#{msgs.cdfm_cancel}" accesskey="x" />
    </sakai:button_bar>

    <mf:htmlShowArea value="#{ForumTool.selectedMessage.message.body}" />
  </h:form>
</sakai:view>
</f:view>
