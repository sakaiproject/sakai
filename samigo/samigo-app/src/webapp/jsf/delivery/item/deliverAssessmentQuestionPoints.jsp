<%--
***********************************************************************************
* Copyright (c) 2003-2022 The Apereo Foundation
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
**********************************************************************************/
--%>

<p class="input-group-addon">
  <%-- REVIEW ASSESSMENT --%>
  <h:outputText value="#{question.roundedMaxPointsToDisplay} #{deliveryMessages.pt}" rendered="#{delivery.actionString=='reviewAssessment'}"/>
  <%-- DELIVER ASSESSMENT --%>
  <h:outputText value="#{question.roundedMaxPoints}" rendered="#{delivery.settings.displayScoreDuringAssessments != '2' && question.itemData.scoreDisplayFlag && delivery.actionString!='reviewAssessment'}"  >
    <f:convertNumber maxFractionDigits="2" groupingUsed="false"/>
  </h:outputText>
  <h:outputText value=" #{deliveryMessages.pt}" rendered="#{delivery.settings.displayScoreDuringAssessments != '2' && question.itemData.scoreDisplayFlag && delivery.actionString!='reviewAssessment'}"  />
  <h:outputText value="#{deliveryMessages.discount} #{question.itemData.discount} "  rendered="#{question.itemData.discount!='0.0' && delivery.settings.displayScoreDuringAssessments != '2' && question.itemData.scoreDisplayFlag}"  >
    <f:convertNumber maxFractionDigits="2" groupingUsed="false"/>
  </h:outputText>
  <h:outputText styleClass="extraCreditLabel" rendered="#{question.itemData.isExtraCredit == true}" value="#{deliveryMessages.extra_credit_preview}" />
</p>
