<!-- $Id: gradeStudentResultAttachment.jsp 11254 2006-06-28 03:38:28Z daisyf@stanford.edu $
<%--
***********************************************************************************
*
* Copyright (c) 2006 The Sakai Foundation.
*
* Licensed under the Educational Community License, Version 1.0 (the"License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.opensource.org/licenses/ecl1.php
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

<script type="text/JavaScript">
function addAttachments(field){
var insertlinkid= field.id.replace("addAttachments", "hiddenlink");
var newindex = 0;
for (i=0; i<document.links.length; i++) {
  if(document.links[i].id == insertlinkid)
  {
    newindex = i;
    break;
  }
}

document.links[newindex].onclick();
}

function addRemoveAttachments(field){
var insertlinkid= field.id.replace("addRemoveAttachments", "hiddenlink");
var newindex = 0;
for (i=0; i<document.links.length; i++) {
  if(document.links[i].id == insertlinkid)
  {
    newindex = i;
    break;
  }
}

document.links[newindex].onclick();
}

</script>

<!-- ASSESSMENT ATTACHMENTS -->
 <h:panelGrid border="0">
 <f:verbatim><div class="longtext"></f:verbatim><h:outputLabel value="#{assessmentSettingsMessages.attachments}" />
  <h:panelGroup rendered="#{question.hasItemGradingAttachment}">
    <h:dataTable value="#{question.itemGradingAttachmentList}" var="attach">
      <h:column>
        <%@ include file="/jsf/shared/mimeicon.jsp" %>
      </h:column>
      <h:column>
        <f:verbatim>&nbsp;&nbsp;&nbsp;&nbsp;</f:verbatim>
        <h:outputLink value="#{attach.location}" target="new_window">
          <h:outputText escape="false" value="#{attach.filename}" />
        </h:outputLink>
      </h:column>
      <h:column>
        <f:verbatim>&nbsp;&nbsp;&nbsp;&nbsp;</f:verbatim>
        <h:outputText escape="false" value="(#{attach.fileSize} #{generalMessages.kb})" rendered="#{!attach.isLink}"/>
      </h:column>
    </h:dataTable>
  </h:panelGroup>
  <h:panelGroup rendered="#{!question.hasItemGradingAttachment}">
    <h:outputText escape="false" value="#{assessmentSettingsMessages.no_attachments}" />
  </h:panelGroup>

  <h:commandButton tabindex="-1" value="#{assessmentSettingsMessages.add_attachments}" type="button" id="addAttachments" rendered="#{!question.hasItemGradingAttachment}"
       style="act" onclick="addAttachments(this);" onkeypress="addAttachments(this);" />

  <h:commandButton tabindex="-1" value="#{assessmentSettingsMessages.add_remove_attachments}" type="button" id="addRemoveAttachments" rendered="#{question.hasItemGradingAttachment}"
       style="act" onclick="addRemoveAttachments(this);" onkeypress="addRemoveAttachments(this);" />

    <h:commandLink tabindex="-1" id="hiddenlink" action="#{studentScores.addAttachmentsRedirect}">
      <f:param name="itemGradingId" value="#{question.itemGradingIdForFilePicker}" />
      <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.evaluation.StudentScoreAttachmentListener"/>
    </h:commandLink>

  </h:panelGrid>
<f:verbatim></div></f:verbatim>

