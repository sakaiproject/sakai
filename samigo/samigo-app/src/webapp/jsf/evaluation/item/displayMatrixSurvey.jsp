<%-- $Id: displayMatrix.jsp 59356 2009-03-31 17:23:25Z kimhuang@oit.rutgers.edu$
include file for displaying matrix choices question
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

  <h:outputText escape="false" value="#{question.text}" />
  <f:verbatim><br /></f:verbatim>
  <f:verbatim><br /></f:verbatim>
  <!-- ATTACHMENTS -->
 <t:dataTable id="table1"
                         value="#{question.rowChoices}"
                         var="row"
                         rowIndexVar="rowIndex"
                         columnClasses="center"
                         rowClasses="">
                <t:column headerstyle="#{question.relativeWidthStyle}" style="text-align:right;padding-left:0.3em" >
                    <f:facet name="header">
                        <t:outputText value="                "/>
                    </f:facet>
                    <h:outputText value="#{row}"/>
                </t:column>
                
                <t:columns value="#{question.columnIndexList}" var="colIndex" styleClass="center" headerstyleClass="center" >
                    <f:facet name="header">
                            <t:outputText value="#{question.columnChoices[colIndex]}" />
                    </f:facet>
                   <div style="text-align: center;">
                 <h:graphicImage id="image2" alt="#{authorMessages.not_correct}" url="/images/radiounchecked.gif"/>
                 <f:verbatim><br /></f:verbatim>
           
                 </div>
                </t:columns>
            </t:dataTable>
            <f:verbatim><br /></f:verbatim>
             <h:panelGrid rendered="#{question.addCommentFlag}">  
 			<h:outputText value="#{question.commentField}" />
		</h:panelGrid>
		<f:verbatim><br /></f:verbatim>
<%@ include file="/jsf/evaluation/item/displayTags.jsp" %>
