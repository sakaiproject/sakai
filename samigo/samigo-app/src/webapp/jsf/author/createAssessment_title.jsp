<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="t"%>
<%@ taglib uri="http://www.sakaiproject.org/samigo" prefix="samigo" %>
<!DOCTYPE html
    PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
    "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<!--
* $Id$
<%--
**********************************************************************************
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
<f:view>
<html xmlns="http://www.w3.org/1999/xhtml" lang="en" xml:lang="en">
<head><%= request.getAttribute("html.head") %>
    <title><h:outputText value="#{authorFrontDoorMessages.auth_front_door}" /></title>
</head>
<body onload="<%= request.getAttribute("html.body.onload") %>">
<div class="portletBody container-fluid">
    <!-- content... -->
    <h:form id="authorIndexForm">
        <!-- HEADINGS -->
        <%@ include file="/jsf/author/editAssessmentHeadings.jsp" %>
        <script>includeWebjarLibrary('datatables');</script>
        <script src="/samigo-app/js/info.js"></script>

<%--        header--%>
        <div class="page-header">
            <h1>
                <h:outputText value="#{questionPoolMessages.add} #{authorFrontDoorMessages.assessments}" />
            </h1>
        </div>

        <div class="samigo-container">
            <h:panelGroup layout="block" styleClass="sak-banner-error" rendered="#{! empty facesContext.maximumSeverity}">
                <h:messages rendered="#{! empty facesContext.maximumSeverity}" layout="table"/>
            </h:panelGroup>

            <div id="samigo-create-new-box" class="col-md-6">
                <h2><h:outputText value="#{authorFrontDoorMessages.assessment_scratch}" rendered="#{authorization.createAssessment}" /></h2>

                <div class="form-group form-inline">
                    <h:outputLabel for="title" value="#{authorFrontDoorMessages.assessment_create}"/>
                    <h:inputText id="title" maxlength="255" value="#{author.assessTitle}" styleClass="form-control" />
                </div>

                <div class="form-group">
                    <t:selectOneRadio id="creationMode" layout="spread" value="#{author.assessCreationMode}" rendered="#{samLiteBean.visible}">
                        <f:selectItem itemValue="1" itemLabel="#{authorFrontDoorMessages.assessmentBuild}" />
                        <f:selectItem itemValue="2" itemLabel="#{authorFrontDoorMessages.markupText}" />
                    </t:selectOneRadio>
                    <!-- SAM-2487 mark them up manually -->
                    <ul class="creation-mode-list no-list">
                        <li>
                            <t:radio renderLogicalId="true" for="creationMode" index="0" />
                        </li>
                        <li>
                            <t:radio renderLogicalId="true" for="creationMode" index="1" />
                        </li>
                    </ul>
                </div>

                <h:panelGroup layout="block" styleClass="form-group" rendered="#{author.showTemplateList}">
                    <h:outputLabel value="#{authorFrontDoorMessages.assessment_choose} " rendered="#{author.showTemplateList}" />
                    <h:selectOneMenu id="assessmentTemplate" value="#{author.assessmentTemplateId}" rendered="#{author.showTemplateList}">
                        <f:selectItem itemValue="" itemLabel="#{generalMessages.select_menu}"/>
                        <f:selectItems value="#{author.assessmentTemplateList}" />
                    </h:selectOneMenu>
                </h:panelGroup>

                <div class="form-group act">
                    <h:commandButton id="createnew" styleClass="active" type="submit" value="#{authorFrontDoorMessages.button_create}" action="#{author.getOutcome}">
                        <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.AuthorAssessmentListener" />
                    </h:commandButton>
                </div>
            </div>

            <div id="samigo-create-or-box" class="col-md-1">
                <h:outputText value="#{authorFrontDoorMessages.label_or}"/>
            </div>

            <div id="samigo-create-import-box" class="col-md-5">
                <h2><h:outputText value="#{authorFrontDoorMessages.assessment_import}" rendered="#{authorization.createAssessment}"/></h2>
                <h:commandButton id="import" value="#{authorFrontDoorMessages.button_import}" immediate="true" type="submit" rendered="#{authorization.createAssessment}" action="importAssessment" />
            </div>
        </div>
    </h:form>
    <!-- end content -->
</div>
</body>
</html>
</f:view>
