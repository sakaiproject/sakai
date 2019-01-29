<%--
***********************************************************************************
*
* Copyright (c) 2003-2018 The Apereo Foundation
*
* Licensed under the Educational Community License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*             http://opensource.org/licenses/ecl2
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*
**********************************************************************************/
--%>
<div class="portletBody">
    <%-- begin assessmentHasBeenSubmittedContent.jsp --%>
    <h3><h:outputText value="#{deliveryMessages.assessment_has_been_submitted_title}"/></h3>
    <h:outputText value="#{deliveryMessages.assessment_has_been_submitted}" rendered="#{delivery.actionString!='takeAssessmentViaUrl'}"/>
    <h:outputText value="#{deliveryMessages.assessment_has_been_submitted_url}" rendered="#{delivery.actionString=='takeAssessmentViaUrl'}"/>

    <h:form id="assessment_has_been_submitted">
        <p class="act">
            <h:commandButton value="#{deliveryMessages.button_return}" type="submit" styleClass="active" action="select" rendered="#{delivery.actionString!='takeAssessmentViaUrl'}">
                <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.select.SelectActionListener" />
            </h:commandButton>
        </p>
    </h:form>
    <%-- end assessmentHasBeenSubmittedContent.jsp --%>
</div>
