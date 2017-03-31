<%-- $Id:
Headings for delivery pages, needs to have msg=DeliveryMessages.properties, etc.
--%>
<!--
<%--
***********************************************************************************
*
* Copyright (c) 2004, 2005, 2006 The Sakai Foundation.
*
* Licensed under the Educational Community License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.osedu.org/licenses/ECL-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License. 
*
**********************************************************************************/
--%>
-->

<%-- TITLE --%>
<h1>
   <h:outputText value="#{delivery.assessmentTitle}" escape="false"/>
</h1>
<%-- NAV BAR --%>
  <ul class="navIntraTool actionToolbar" role="menu">
  <h:panelGroup rendered="#{(delivery.feedbackComponent.showImmediate || delivery.feedbackOnDate) 
                         && (delivery.actionString=='previewAssessment'
                             || delivery.actionString=='takeAssessment'
                             || delivery.actionString=='takeAssessmentViaUrl')}">
    <li role="menuitem" class="firstToolBarItem"><span>

<!-- SHOW FEEDBACK LINK FOR TAKE ASSESSMENT AND TAKE ASSESSMENT VIA URL -->
    <h:commandLink title="#{commonMessages.feedback}" action="#{delivery.getOutcome}" 
       id="showFeedback" onmouseup="saveTime(); serializeImagePoints();" 
       rendered="#{(delivery.actionString=='takeAssessment'
                || delivery.actionString=='takeAssessmentViaUrl') && !(delivery.pageContents.isNoParts && delivery.navigation eq '1')}" >
     <h:outputText value="#{deliveryMessages.show_feedback}" />
     <f:param name="showfeedbacknow" value="true" />
     <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.delivery.ShowFeedbackActionListener" />
    </h:commandLink>

<!-- SHOW FEEDBACK LINK FOR PREVIEW ASSESSMENT -->
    <h:outputText value="#{deliveryMessages.show_feedback_preview}" 
         rendered="#{delivery.actionString=='previewAssessment' && !(delivery.pageContents.isNoParts && delivery.navigation eq '1')}" />

  </span></li>
  </h:panelGroup >


<!-- TABLE OF CONTENT LINK FOR TAKE ASSESSMENT AND TAKE ASSESSMENT VIA URL -->
  <li role="menuitem"><span>
  <h:commandLink title="#{deliveryMessages.t_tableOfContents}" action="#{delivery.getOutcome}" 
     id="showTOC" onmouseup="saveTime(); serializeImagePoints();"
     rendered="#{(delivery.actionString=='takeAssessment'
                   || delivery.actionString=='takeAssessmentViaUrl')
               && delivery.navigation ne '1'}">
    <h:outputText value="#{deliveryMessages.table_of_contents}" />
    <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.delivery.TableOfContentsActionListener" />
  </h:commandLink>


<!-- TABLE OF CONTENT LINK FOR PREVIEW ASSESSMENT -->
 <h:commandLink title="#{deliveryMessages.t_tableOfContents}" action="tableOfContents" onmouseup="saveTime();"
    rendered="#{delivery.actionString=='previewAssessment'
             && delivery.navigation ne '1'}">
    <h:outputText value="#{deliveryMessages.table_of_contents}" />
    <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.delivery.DeliveryActionListener" />
  </h:commandLink>


<!-- RETURN TO ASSESSMENT PAGE LINK FOR REVIEW ASSESSMENT -->
  <h:commandLink action="select" title="#{deliveryMessages.t_returnAssessmentList}"
     rendered="#{delivery.actionString=='reviewAssessment'&&
!delivery.anonymousLogin}">
    <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.select.SelectActionListener" />
    <h:outputText value="#{deliveryMessages.button_return_select}" />
  </h:commandLink>
  </span></li>
</ul>

<!-- GRADER COMMENT FOR REVIEW ASSESSMENT -->
<f:verbatim><br /></f:verbatim> 
<h:panelGrid rendered="#{delivery.feedbackComponent.showGraderComment && delivery.actionString=='reviewAssessment' 
					&& (delivery.graderComment ne '' || delivery.hasAssessmentGradingAttachment)}" columns="1" border="0">
    <h:panelGroup>  
      <h:outputLabel for="commentSC" styleClass="answerkeyFeedbackCommentLabel" value="#{deliveryMessages.comment}#{deliveryMessages.column} " />
	  <h:outputText id="commentSC" value="#{delivery.graderComment}" escape="false" rendered="#{delivery.graderComment ne ''}"/>
    </h:panelGroup>
    
	<h:panelGroup rendered="#{delivery.hasAssessmentGradingAttachment}">
      <h:dataTable value="#{delivery.assessmentGradingAttachmentList}" var="attach">
        <h:column>
          <%@ include file="/jsf/shared/mimeicon.jsp" %>
        </h:column>
        <h:column>
          <f:verbatim>&nbsp;&nbsp;&nbsp;&nbsp;</f:verbatim>
          <h:outputLink value="#{attach.location}" target="new_window">
            <h:outputText value="#{attach.filename}" />
          </h:outputLink>
        </h:column>
        <h:column>
          <f:verbatim>&nbsp;&nbsp;&nbsp;&nbsp;</f:verbatim>
          <h:outputText escape="false" value="(#{attach.fileSize} #{generalMessages.kb})" rendered="#{!attach.isLink}"/>
        </h:column>
      </h:dataTable>
    </h:panelGroup>
  </h:panelGrid>

<%@ include file="/jsf/delivery/assessmentDeliveryTimer.jsp" %>

