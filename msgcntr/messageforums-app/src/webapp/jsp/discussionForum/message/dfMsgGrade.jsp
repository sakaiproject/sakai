<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
<%@ taglib uri="http://sakaiproject.org/jsf/messageforums" prefix="mf" %>
<f:loadBundle basename="org.sakaiproject.tool.messageforums.bundle.Messages" var="msgs"/>
<link href='/sakai-messageforums-tool/css/msgForums.css' rel='stylesheet' type='text/css' />
<f:view>
<sakai:view>
  <h:form id="DF-1">
    <sakai:group_box>
      <div class="left-header-section">
      	<h:commandLink action="#{ForumTool.processActionHome}" value="#{msgs.cdfm_message_forums}" /> /
          <h:commandLink action="#{ForumTool.processActionDisplayForum}" value="#{ForumTool.selectedForum.forum.title}" >
            <f:param value="#{ForumTool.selectedForum.forum.id}" name="forumId"/>
          </h:commandLink> /
          <h:commandLink action="#{ForumTool.processActionDisplayTopic}" value="#{ForumTool.selectedTopic.topic.title}" >
            <f:param value="#{ForumTool.selectedForum.forum.id}" name="forumId"/>
            <f:param value="#{ForumTool.selectedTopic.topic.id}" name="topicId"/>
          </h:commandLink>
        <sakai:instruction_message value="#{ForumTool.selectedTopic.topic.shortDescription}" />
        <h:commandLink immediate="true" 
          action="#{ForumTool.processDfComposeToggle}" 
          onmousedown="document.forms[0].onsubmit();"
          rendered="#{ForumTool.selectedTopic.hasExtendedDesciption}" 
          value="#{msgs.cdfm_read_full_description}">
          <f:param value="dfViewMessage" name="redirectToProcessAction"/>
          <f:param value="true" name="composeExpand"/>
        </h:commandLink> 
         <sakai:inputRichText rows="5" cols="110" buttonSet="none" 
         readonly="true" showXPath="false" id="topic_extended_description" 
         value="#{ForumTool.selectedTopic.topic.extendedDescription}" 
         rendered="#{ForumTool.selectedTopic.readFullDesciption}"/>
      
        <f:verbatim><br/></f:verbatim>
        <h:commandLink immediate="true" 
       action="#{ForumTool.processDfComposeToggle}" 
          onmousedown="document.forms[0].onsubmit();"
          value="#{msgs.cdfm_hide_full_description}" 
          rendered="#{ForumTool.selectedTopic.readFullDesciption}">
          <f:param value="dfViewMessage" name="redirectToProcessAction"/>
        </h:commandLink>          
      </div>
      <div class="right-header-section">
        <h:outputText   value="#{msgs.cdfm_previous_topic}   "  rendered="#{!ForumTool.selectedTopic.hasPreviousTopic}" />
        <h:commandLink action="#{ForumTool.processActionDisplayPreviousTopic}" value="#{msgs.cdfm_previous_topic}   "  rendered="#{ForumTool.selectedTopic.hasPreviousTopic}" >
          <f:param value="#{ForumTool.selectedTopic.previousTopicId}" name="previousTopicId"/>
          <f:param value="#{ForumTool.selectedForum.forum.id}" name="forumId"/>
        </h:commandLink>
        <h:outputText   value="#{msgs.cdfm_next_topic}   " rendered="#{!ForumTool.selectedTopic.hasNextTopic}" />
        <h:commandLink action="#{ForumTool.processActionDisplayNextTopic}" value="#{msgs.cdfm_next_topic}   " rendered="#{ForumTool.selectedTopic.hasNextTopic}" >
         <f:param value="#{ForumTool.selectedTopic.nextTopicId}" name="nextTopicId"/>
          <f:param value="#{ForumTool.selectedForum.forum.id}" name="forumId"/>
        </h:commandLink>
      </div>
    </sakai:group_box>  

  <br/>
    <sakai:group_box>
      <sakai:panel_edit>
        <sakai:doc_section>  
        </sakai:doc_section>    
      </sakai:panel_edit>
    </sakai:group_box>
    
    <sakai:group_box>
    <table width="100%" align="left" style="background-color:#DDDFE4;">
      <tr>
        <td align="left">
          <h:outputText style="font-weight:bold"  value="Subject "/>
        </td>
        <td align="left">
          <h:outputText value="#{ForumTool.selectedMessage.message.title}" />  
        </td>  
        <td align="left">
        </td>
      </tr>
      <tr>
        <td align="left">
          <h:outputText style="font-weight:bold"  value="#{msgs.cdfm_authoredby}"/>
        </td>
        <td align="left">
          <h:outputText value="#{ForumTool.selectedMessage.message.author}" />  
          <h:outputText value=" (" />  
          <h:outputText value="#{ForumTool.selectedMessage.message.created}" />
          <h:outputText value=")" />  
        </td>
        <td></td>
      </tr>
      <tr>
        <td align="left">
          <h:outputText style="font-weight:bold"  value="Attachments "/>
        </td>
        <td align="left">
          <%-- Attachments --%>
          <h:dataTable value="#{ForumTool.selectedMessage.message.attachments}" var="eachAttach" >
            <h:column rendered="#{!empty ForumTool.selectedMessage.message.attachments}">
              <h:graphicImage url="/images/excel.gif" rendered="#{eachAttach.attachmentType == 'application/vnd.ms-excel'}"/>
              <h:graphicImage url="/images/html.gif" rendered="#{eachAttach.attachmentType == 'text/html'}"/>
              <h:graphicImage url="/images/pdf.gif" rendered="#{eachAttach.attachmentType == 'application/pdf'}"/>
              <h:graphicImage url="/sakai-messageforums-tool/images/ppt.gif" rendered="#{eachAttach.attachmentType == 'application/vnd.ms-powerpoint'}"/>
              <h:graphicImage url="/images/text.gif" rendered="#{eachAttach.attachmentType == 'text/plain'}"/>
              <h:graphicImage url="/images/word.gif" rendered="#{eachAttach.attachmentType == 'application/msword'}"/>
                 
              <h:outputLink value="#{eachAttach.attachmentUrl}" target="_new_window">
                <h:outputText value="#{eachAttach.attachmentName}"/>
              </h:outputLink>
              
            </h:column>
          </h:dataTable>
        <%-- Attachments --%>
        </td>
        <td></td>
      </tr>
    </table>    
    </sakai:group_box>

    <sakai:group_box>
      <table width="100%" align="left">
        <tr>
          <td align="left" width="15%">
            <h:outputText value="Current score: " rendered="#{ ForumTool.gradebookScore != null && ForumTool.gradebookScore != ''}"/>
          </td>
          <td align="left" width="85%">
            <h:outputText value="#{ForumTool.gradebookScore}" rendered="#{ ForumTool.gradebookScore != null && ForumTool.gradebookScore != ''}"/>
          </td>  
        </tr>
        <tr>
          <td align="left" width="15%">
            <h:outputText value="Grade (Points Only):"/>
          </td>
          <td align="left" width="85%">
            <h:inputText value="#{ForumTool.gradePoint}" id="dfMsgGradeGradePoint" size="5">
<%--              <f:validateDoubleRange minimum="0"/>--%>
            </h:inputText>
            <h:message for="dfMsgGradeGradePoint" errorStyle="color: red; "/> 
          </td>  
        </tr>
        <tr>
          <td align="left" width="15%">
            <h:outputText value="Assignments:"/>
          </td>
          <td align="left" width="85%">
            <h:selectOneMenu value="#{ForumTool.selectedAssign}" valueChangeListener="#{ForumTool.processGradeAssignChange}"
              onchange="document.forms[0].submit();">
              <f:selectItems value="#{ForumTool.assignments}" />
            </h:selectOneMenu>
          </td>
        </tr>
        <tr>
          <td align="left" width="15%">
            <h:outputText value="Comments:"/>
          </td>
          <td align="left" width="85%">
            <h:inputTextarea value="#{ForumTool.gradeComment}" rows="5" cols="50"/>
          </td>
        </tr>
        <tr>
          <td align="left" width="15%">
          </td>
          <td align="left" width="85%">
            <h:selectBooleanCheckbox value="#{ForumTool.gradeNotify}"/>
            <h:outputText value="Send Notification"/>
          </td>
        </tr>
      </table>
    </sakai:group_box>
    
    <br/>
    <sakai:group_box>
      <sakai:panel_edit>
        <sakai:doc_section>  
        </sakai:doc_section>    
      </sakai:panel_edit>
    </sakai:group_box>

    <h:panelGroup rendered="#{ForumTool.noAssignWarn}">
      <h:outputText style="background-color:#FFF8DF;border:1px solid #B8B88A;color:#663300;font-size:x-small;margin:5px 0px 5px 0px;padding:5px 5px 5px 25px;" 
        value="! You must choose an assignment before submit grade." />
    </h:panelGroup>

    <h:panelGroup rendered="#{ForumTool.noGradeWarn}">
      <h:outputText style="background-color:#FFF8DF;border:1px solid #B8B88A;color:#663300;font-size:x-small;margin:5px 0px 5px 0px;padding:5px 5px 5px 25px;" 
        value="! You must enter grade points before submit grade." />
    </h:panelGroup>

    <sakai:button_bar>
      <sakai:button_bar_item action="#{ForumTool.processDfGradeSubmit}" value="Submit Grade" />
      <sakai:button_bar_item action="#{ForumTool.processDfGradeCancel}" value="Cancel" />
    </sakai:button_bar>

    <mf:htmlShowArea value="#{ForumTool.selectedMessage.message.body}"/>
  </h:form>
</sakai:view>
</f:view>