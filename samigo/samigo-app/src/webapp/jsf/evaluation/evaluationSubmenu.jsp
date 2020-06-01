<%--
***********************************************************************************
*
* Copyright (c) 2020 Apereo Foundation

* Licensed under the Educational Community License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at

*             http://opensource.org/licenses/ecl2

* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*
**********************************************************************************/
--%>
<h:panelGroup>
  <ul class='navIntraTool actionToolbar' role='menu'>
    <li role='menuitem'>
      <h:panelGroup id="submissionStatusMenuLink">
        <h:commandLink title="#{evaluationMessages.t_submissionStatus}" action="submissionStatus" immediate="true">
          <h:outputText value="#{evaluationMessages.sub_status}" />
          <f:param name="allSubmissions" value="true"/>
          <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.evaluation.SubmissionStatusListener" />
        </h:commandLink>
      </h:panelGroup>
    </li>
    <li role='menuitem'>
      <h:panelGroup id="totalScoresMenuLink">
        <h:commandLink title="#{evaluationMessages.t_totalScores}" action="totalScores" immediate="true">
          <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.evaluation.ResetTotalScoreListener" />
          <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.evaluation.TotalScoreListener" />
          <h:outputText value="#{commonMessages.total_scores}" />
        </h:commandLink>
      </h:panelGroup>
    </li>
    <li role='menuitem'>
      <h:panelGroup id="questionScoresMenuLink">
        <h:commandLink title="#{evaluationMessages.t_questionScores}" action="questionScores" immediate="true">
          <h:outputText value="#{evaluationMessages.q_view}" />
          <f:param name="allSubmissions" value="3"/>
          <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.evaluation.ResetQuestionScoreListener" />
          <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.evaluation.QuestionScorePagerListener" />
          <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.evaluation.QuestionScoreListener" />
        </h:commandLink>
      </h:panelGroup>
    </li>
    <li role='menuitem'>
      <h:panelGroup id="histogramScoresMenuLink">
        <h:commandLink title="#{evaluationMessages.t_histogram}" action="histogramScores" immediate="true">
          <h:outputText value="#{evaluationMessages.stat_view}" />
          <f:param name="hasNav" value="true"/>
          <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.evaluation.HistogramListener" />
        </h:commandLink>
      </h:panelGroup>
    </li>
    <li role='menuitem'>
      <h:panelGroup id="detailedStatisticsMenuLink">
        <h:commandLink title="#{evaluationMessages.t_itemAnalysis}" action="detailedStatistics" immediate="true">
          <h:outputText value="#{evaluationMessages.item_analysis}" />
          <f:param name="hasNav" value="true"/>
          <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.evaluation.HistogramListener" />
        </h:commandLink>
      </h:panelGroup>
    </li>
    <li role='menuitem'>
      <h:panelGroup id="exportResponsesMenuLink">
        <h:commandLink title="#{commonMessages.export_action}" action="exportResponses" immediate="true">
          <h:outputText value="#{commonMessages.export_action}" />
          <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.evaluation.ExportResponsesListener" />
        </h:commandLink>
      </h:panelGroup>
    </li>
    <h:panelGroup rendered="#{totalScores.hasFileUpload}">
      <li role='menuitem'>
        <h:panelGroup id="downloadFileSubmissionsMenuLink">
          <h:commandLink title="#{evaluationMessages.t_title_download_file_submissions}" action="downloadFileSubmissions" immediate="true">
            <h:outputText value="#{evaluationMessages.title_download_file_submissions}" />
            <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.evaluation.ResetQuestionScoreListener" />
            <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.evaluation.DownloadFileSubmissionsListener" />
          </h:commandLink>
        </h:panelGroup>
      </li>
    </h:panelGroup>
  </ul>
</h:panelGroup>
