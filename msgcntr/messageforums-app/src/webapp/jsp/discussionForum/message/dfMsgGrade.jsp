<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
<%@ taglib uri="http://sakaiproject.org/jsf/messageforums" prefix="mf" %>
<f:loadBundle basename="org.sakaiproject.tool.messageforums.bundle.Messages" var="msgs"/>

<f:view>
<sakai:view>
<link href='/sakai-messageforums-tool/css/msgForums.css' rel='stylesheet' type='text/css' />
  <h:form id="DF-1">
    <sakai:panel_titled title="">
      <h:panelGrid columns="2" summary="" width="100%">
		    <h:panelGroup styleClass="breadCrumb">
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
			 </h:panelGroup>
			 <h:panelGroup styleClass="msgNav">
			   <h:outputText   value="#{msgs.cdfm_previous_topic}   "  rendered="#{!ForumTool.selectedTopic.hasPreviousTopic}" />
          <h:commandLink action="#{ForumTool.processActionDisplayPreviousTopic}" value="#{msgs.cdfm_previous_topic}   "  
                         rendered="#{ForumTool.selectedTopic.hasPreviousTopic}" title=" #{msgs.cdfm_previous_topic}">
            <f:param value="#{ForumTool.selectedTopic.previousTopicId}" name="previousTopicId"/>
            <f:param value="#{ForumTool.selectedForum.forum.id}" name="forumId"/>
          </h:commandLink>
          <f:verbatim><h:outputText value=" " /> </f:verbatim>
          <h:outputText  value="#{msgs.cdfm_next_topic}   " rendered="#{!ForumTool.selectedTopic.hasNextTopic}" />
          <h:commandLink action="#{ForumTool.processActionDisplayNextTopic}" value="#{msgs.cdfm_next_topic}   " 
                         rendered="#{ForumTool.selectedTopic.hasNextTopic}" title=" #{msgs.cdfm_next_topic}">
            <f:param value="#{ForumTool.selectedTopic.nextTopicId}" name="nextTopicId"/>
            <f:param value="#{ForumTool.selectedForum.forum.id}" name="forumId"/>
          </h:commandLink>
			 </h:panelGroup>
		  </h:panelGrid>
      	
        <sakai:instruction_message value="#{ForumTool.selectedTopic.topic.shortDescription}" />
        <h:commandLink immediate="true" 
          action="#{ForumTool.processDfComposeToggle}" 
          onmousedown="document.forms[0].onsubmit();"
          rendered="#{ForumTool.selectedTopic.hasExtendedDesciption}" 
          value="#{msgs.cdfm_read_full_description}"
          title="#{msgs.cdfm_read_full_description}">
          <f:param value="dfViewMessage" name="redirectToProcessAction"/>
          <f:param value="true" name="composeExpand"/>
        </h:commandLink> 
         <sakai:inputRichText rows="5" cols="110" buttonSet="none" 
         readonly="true" showXPath="false" id="topic_extended_description" 
         value="#{ForumTool.selectedTopic.topic.extendedDescription}" 
         rendered="#{ForumTool.selectedTopic.readFullDesciption}"/>
      
        <f:verbatim><br /></f:verbatim>
        <h:commandLink immediate="true" 
          action="#{ForumTool.processDfComposeToggle}" 
          onmousedown="document.forms[0].onsubmit();"
          value="#{msgs.cdfm_hide_full_description}" 
          rendered="#{ForumTool.selectedTopic.readFullDesciption}"
          title="#{msgs.cdfm_hide_full_description}">
          <f:param value="dfViewMessage" name="redirectToProcessAction"/>
        </h:commandLink>          



    </sakai:panel_titled>  

    <br />
    <h:panelGrid styleClass="msgDetails" columns="2" summary="" width="100%">
      <h:panelGroup styleClass="msgDetailsCol">
        <h:outputText value="#{msgs.cdfm_subject}"/>
      </h:panelGroup>
      <h:panelGroup>
        <h:outputText value="#{ForumTool.selectedMessage.message.title}" /> 
      </h:panelGroup>
      
      <h:panelGroup styleClass="msgDetailsCol">
        <h:outputText value="#{msgs.cdfm_authoredby}"/>
      </h:panelGroup>
      <h:panelGroup>
        <h:outputText value="#{ForumTool.selectedMessage.message.author} #{msgs.cdfm_openb} #{ForumTool.selectedMessage.message.created} #{msgs.cdfm_closeb}" /> 
      </h:panelGroup>
       
      <h:panelGroup styleClass="msgDetailsCol"  >
        <h:outputText value="#{msgs.cdfm_att}"/>
      </h:panelGroup>
      <h:panelGroup>
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
   

    <h:panelGrid styleClass="jsfFormTable" columns="1">
      <h:panelGroup styleClass="shorttext">
          <h:outputText value="#{msgs.cdfm_curr_score}" rendered="#{ ForumTool.gradebookScore != null && ForumTool.gradebookScore != ''}"/>

            <h:outputText value="#{ForumTool.gradebookScore}" rendered="#{ ForumTool.gradebookScore != null && ForumTool.gradebookScore != ''}"/>
      </h:panelGroup>
      <h:panelGroup styleClass="shorttext">  
        <h:outputLabel for="dfMsgGradeGradePoint" value="#{msgs.cdfm_grade_points}"/>
        <h:inputText value="#{ForumTool.gradePoint}" id="dfMsgGradeGradePoint" size="5">
<%--              <f:validateDoubleRange minimum="0"/>--%>
        </h:inputText>
        <h:message for="dfMsgGradeGradePoint" styleClass="alertMessage" />
      </h:panelGroup>
      <h:panelGroup styleClass="shorttext"> 
        <h:outputLabel for="assignment" value="#{msgs.cdfm_assignments}"/>
        <h:selectOneMenu id="assignment" value="#{ForumTool.selectedAssign}" valueChangeListener="#{ForumTool.processGradeAssignChange}"
              onchange="document.forms[0].submit();">
           <f:selectItems value="#{ForumTool.assignments}" />
         </h:selectOneMenu>
      </h:panelGroup>
      <h:panelGroup styleClass="shorttext">
        <h:outputLabel for="comments" value="#{msgs.cdfm_comments}"/>
        <h:inputTextarea id="comments" value="#{ForumTool.gradeComment}" rows="5" cols="50"/>
      </h:panelGroup>
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
      <sakai:button_bar_item action="#{ForumTool.processDfGradeSubmit}" value="#{msgs.cdfm_submit_grade}" accesskey="s" />
      <sakai:button_bar_item action="#{ForumTool.processDfGradeCancel}" value="#{msgs.cdfm_cancel}" accesskey="c" />
    </sakai:button_bar>

    <mf:htmlShowArea value="#{ForumTool.selectedMessage.message.body}" />
  </h:form>
</sakai:view>
</f:view>