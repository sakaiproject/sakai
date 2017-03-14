
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


<h:outputLabel escape="false" value="#{authorMessages.matrix_choice_surv}" /></br>
<h:dataTable value="#{searchQuestionBean.getData(param.idString)}" var="item" width="100%">
    <h:column>


  <h:outputText escape="false" value="#{item.text}" />
 <f:verbatim><br /></f:verbatim>
  <!-- ATTACHMENTS -->
  <%@ include file="/jsf/author/search_preview_item/attachment.jsp" %>
  <f:verbatim><br /></f:verbatim>
   <t:dataTable id="table1"
                         value="#{item.rowChoices}"
                         var="row"
                         rowIndexVar="rowIndex"
                         columnClasses="Tablecolumn"
                         rowClasses=""
                         frame="border"
                         rules="rows"
                         styleClass="TableClass">
                <t:column headerstyle="#{item.relativeWidthStyle}" style="text-align:right;padding-left:0.3em" >
                    <f:facet name="header">
                        <t:outputText value="                "/>
                    </f:facet>
                    <h:outputText value="#{row}"/>
                </t:column>
                
                <t:columns value="#{item.columnIndexList}" var="colIndex" styleClass="center" headerstyleClass="center matrixSurvey" >
                    <f:facet name="header">
                            <t:outputText value="#{item.columnChoices[colIndex]}" />
                    </f:facet>
                     <div style="text-align: center;">
                 <h:graphicImage id="image2" alt="#{authorMessages.not_correct}" url="/images/radiounchecked.gif"/>
                 </div>
                </t:columns>
            </t:dataTable>
          <f:verbatim><br /></f:verbatim> 
<h:panelGrid rendered="#{item.addCommentFlag}">
 	<h:outputText value="#{item.commentField}" />
 	
</h:panelGrid>

<h:panelGroup rendered="#{!author.isEditPendingAssessmentFlow && publishedSettings.feedbackAuthoring ne '2' }">
  <h:outputLabel value="#{commonMessages.feedback}: " />
  <h:outputText value="#{item.generalItemFeedback}" escape="false" />
</h:panelGroup>

<%@ include file="/jsf/author/search_preview_item/tags.jsp" %>

    </h:column>
</h:dataTable>
