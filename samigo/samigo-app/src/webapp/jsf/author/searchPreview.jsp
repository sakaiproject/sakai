<%@ page contentType="text/html;charset=utf-8" pageEncoding="utf-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="t" %>
<%@ taglib uri="http://www.sakaiproject.org/samigo" prefix="samigo" %>
<!DOCTYPE html
PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<!--
$Id: fullShortAnswer.jsp 6643 2006-03-13 19:38:07Z hquinn@stanford.edu $
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
<f:view>
    <f:subview id="preview" rendered="#{searchQuestionBean.userOwns(param.idString) == true}">

        <h:dataTable value="#{searchQuestionBean.getItem(param.idString)}" var="itemValue" width="100%">
            <h:column>

                <h:panelGrid>
                    <h:panelGroup rendered="#{param.typeId == '9'}">
                        <%@ include file="/jsf/author/search_preview_item/Matching.jsp" %>
                    </h:panelGroup>

                    <h:panelGroup rendered="#{param.typeId == '11'}">
                        <%@ include file="/jsf/author/search_preview_item/FillInNumeric.jsp" %>
                    </h:panelGroup>


                    <h:panelGroup rendered="#{param.typeId == '8'}">
                        <%@ include file="/jsf/author/search_preview_item/FillInTheBlank.jsp" %>
                    </h:panelGroup>

                    <h:panelGroup rendered="#{param.typeId == '7'}">
                        <%@ include file="/jsf/author/search_preview_item/AudioRecording.jsp" %>
                    </h:panelGroup>

                    <h:panelGroup rendered="#{param.typeId == '6'}">
                        <%@ include file="/jsf/author/search_preview_item/FileUpload.jsp" %>
                    </h:panelGroup>

                    <h:panelGroup rendered="#{param.typeId == '5'}">
                        <%@ include file="/jsf/author/search_preview_item/ShortAnswer.jsp" %>
                    </h:panelGroup>

                    <h:panelGroup rendered="#{param.typeId == '4'}">
                        <%@ include file="/jsf/author/search_preview_item/TrueFalse.jsp" %>
                    </h:panelGroup>

                    <!-- same as multiple choice single -->
                    <h:panelGroup rendered="#{param.typeId == '3'}">
                        <%@ include file="/jsf/author/search_preview_item/MultipleChoiceSurvey.jsp" %>
                    </h:panelGroup>

                    <h:panelGroup rendered="#{param.typeId == '2'}">
                        <%@ include file="/jsf/author/search_preview_item/MultipleChoiceMultipleCorrect.jsp" %>
                    </h:panelGroup>

                    <h:panelGroup rendered="#{param.typeId == '1'}">
                        <%@ include file="/jsf/author/search_preview_item/MultipleChoiceSingleCorrect.jsp" %>
                    </h:panelGroup>

                    <h:panelGroup rendered="#{param.typeId == '12'}">
                        <%@ include file="/jsf/author/search_preview_item/MultipleChoiceMultipleCorrect.jsp" %>
                    </h:panelGroup>

                    <h:panelGroup rendered="#{param.typeId == '14'}">
                        <%@ include file="/jsf/author/search_preview_item/ExtendedMatchingItems.jsp" %>
                    </h:panelGroup>

                    <h:panelGroup rendered="#{param.typeId == '13'}">
                        <%@ include file="/jsf/author/search_preview_item/MatrixChoicesSurvey.jsp" %>
                    </h:panelGroup>

                    <h:panelGroup rendered="#{param.typeId == '15'}"><!-- // CALCULATED_QUESTION -->
                        <%@ include file="/jsf/author/search_preview_item/CalculatedQuestion.jsp" %>
                    </h:panelGroup>
                    <h:panelGroup rendered="#{param.typeId == '16'}"><!-- // IMAGEMAP_QUESTION -->
                        <%@ include file="/jsf/author/search_preview_item/ImageMapQuestion.jsp" %>
                    </h:panelGroup>

                </h:panelGrid>

    <br/><h:outputLabel value="#{authorMessages.origins}" /></br>

                <t:dataList value="#{searchQuestionBean.originFull(itemValue.hash)}" var="origin" layout="unorderedList">
        <f:verbatim><span></f:verbatim>
        <h:outputText value="#{origin}"/>
        <f:verbatim></span></br>  </f:verbatim>
    </t:dataList>

            </h:column>
        </h:dataTable>
    </f:subview>

</f:view>
