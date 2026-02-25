<%-- $Id$
include file for displaying file upload questions
should be included in file importing DeliveryMessages
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

<h:outputText value="#{question.text}"  escape="false"/>
<f:verbatim><br /></f:verbatim>

      <!-- ATTACHMENTS -->
      <%@ include file="/jsf/delivery/item/attachment.jsp" %>

      <%-- media list, note that question is ItemContentsBean --%>
      <h:dataTable value="#{question.mediaArray}" var="media">
        <h:column>
          <f:verbatim>&nbsp;&nbsp;&nbsp;&nbsp;</f:verbatim>
          <h:outputLink title="#{evaluationMessages.t_fileUpload}" value="/samigo-app/servlet/ShowMedia?mediaId=#{media.mediaId}&setMimeType=false" target="new_window">
             <h:outputText value="#{media.filename}" />
          </h:outputLink>
        </h:column>
        <h:column>
         <h:outputText value="#{evaluationMessages.open_bracket}"/>
         	<h:outputText value="#{media.fileSizeKBFormat} #{generalMessages.kb}"/>
         <h:outputText value="#{evaluationMessages.close_bracket}"/>
        </h:column>
      </h:dataTable>

      <h:panelGrid rendered="#{delivery.feedbackComponent.showItemLevel && question.feedbackIsNotEmpty}">
        <h:panelGroup>
         <h:outputLabel for="feedSC" styleClass="answerkeyFeedbackCommentLabel" value="#{commonMessages.feedback}: " />
         <h:outputText id="feedSC" value="#{question.feedback}" escape="false" />
        </h:panelGroup>
        <h:outputText value=" " />
      </h:panelGrid>
