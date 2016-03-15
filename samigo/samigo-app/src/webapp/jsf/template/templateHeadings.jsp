<!-- $Id: templateHeadings.jsp 243 2005-06-24 02:43:50Z esmiley@stanford.edu $
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
  <h:panelGroup rendered="#{authorization.adminQuestionPool or authorization.adminAssessment}">

<f:verbatim><ul class="navIntraTool actionToolbar" role="menu">
<li role="menuitem" class="firstToolBarItem"><span></f:verbatim>

   <h:commandLink title="#{generalMessages.t_assessment}" action="author" id="authorlink" immediate="true" rendered="#{authorization.adminAssessment}">
      <h:outputText value="#{generalMessages.assessment}" />
       <f:actionListener
         type="org.sakaiproject.tool.assessment.ui.listener.author.AuthorActionListener" />
   </h:commandLink>

<f:verbatim></span></li>
<li role="menuitem" ><span class="current"></f:verbatim>

      <h:outputText value="#{generalMessages.template}" />
      
<f:verbatim></span></li>
<li role="menuitem" ><span></f:verbatim>

    <h:commandLink id="questionPoolsLink" title="#{generalMessages.t_questionPool}" action="poolList" immediate="true" rendered="#{authorization.adminQuestionPool}">
      <h:outputText value="#{generalMessages.questionPool}" />
      <f:actionListener
         type="org.sakaiproject.tool.assessment.ui.listener.questionpool.QuestionPoolListener" />
    </h:commandLink>

<f:verbatim></span></li>
<li role="menuitem" ><span></f:verbatim>
    <h:commandLink id="evnetLogLink" accesskey="#{generalMessages.a_log}" title="#{generalMessages.t_eventLog}" action="eventLog" immediate="true" rendered="#{authorization.adminQuestionPool}">
      <h:outputText value="#{generalMessages.eventLog}" />
      <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.EventLogListener" />
    </h:commandLink>
<f:verbatim></span></li>
<li role="menuitem" ><span></f:verbatim>
	<h:commandLink id="sectionActivity" accesskey="#{generalMessages.a_section_activity}" title="#{generalMessages.section_activity}" action="sectionActivity" immediate="true" rendered="#{authorization.adminQuestionPool}">
		<h:outputText value="#{generalMessages.section_activity}" />
		<f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.SectionActivityListener" />
	</h:commandLink>
<f:verbatim></span></li> 

</ul></f:verbatim>

  </h:panelGroup>
