<%--
include file for delivering matching questions
should be included in file importing DeliveryMessages
--%>
<!--
<%--
***********************************************************************************
*
* Copyright (c) 2005 The Regents of the University of Michigan, Trustees of Indiana University,
*                  Board of Trustees of the Leland Stanford, Jr., University, and The MIT Corporation
*
* Licensed under the Educational Community License Version 1.0 (the "License");
* By obtaining, using and/or copying this Original Work, you agree that you have read,
* understand, and will comply with the terms and conditions of the Educational Community License.
* You may obtain a copy of the License at:
*
*      http://cvs.sakaiproject.org/licenses/license_1_0.html
*
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
* INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
* AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
* DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
* FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*
**********************************************************************************/
--%>
-->
  <%-- questionBlock --%>
  <h:outputText styleClass="questionBlock" escape="false" value="#{question.itemData.instruction}" />
  
  <h:outputText value="<br />" escape="false" />
  <h:outputText value="<br />" escape="false" />    
  <%@ include file="/jsf/delivery/item/attachment.jsp" %>

  <h:panelGrid columns="2" width="99%">
    <%-- items --%>
    <h:panelGroup>
      <h:dataTable id="items" value="#{question.itemData.itemTextArraySorted}" var="itemText">
        <h:column>
          <h:outputText value="#{itemText.sequence}. #{itemText.text}" escape="false"/>
        </h:column>
      </h:dataTable>
    </h:panelGroup>
	
	<h:outputText value="<br />" escape="false" />
    
    <%-- image --%>
    <h:panelGroup>
     	<f:verbatim>  
			<div class='studentImageContainer'>
				<img id='img' src='</f:verbatim><h:outputText value="#{question.imageSrc}" /><f:verbatim>' />
			</div>
		</f:verbatim>
    </h:panelGroup>
  </h:panelGrid>

  <%-- answerBlock --%>
  <h:panelGroup styleClass="answerBlock" rendered="#{printSettings.showKeys || printSettings.showKeysFeedback}">
  	<h:outputLabel value="#{printMessages.answer_point}: "/>
	<h:outputText value="#{question.itemData.score}">
		<f:convertNumber maxFractionDigits="2"/>
    </h:outputText>
  	<h:outputText escape="false" value=" #{authorMessages.points_lower_case}" />
  	<h:outputText value="<br />" escape="false" />
    <h:outputLabel value="#{printMessages.answer_key}: "/>
		<h:dataTable value="#{question.answers}" var="answer" binding="#{table}">
			<h:column>
				<h:outputText escape="false" value="<input type='hidden' id='hiddenSerializedCoords_#{part.number}_#{question.sequence}_#{table.rowIndex}' value='#{answer}' />" /> 
			</h:column>
		</h:dataTable>

		<f:verbatim> 
			<div id="answerImageMapContainer_</f:verbatim><h:outputText value="#{part.number}_#{question.sequence}"/><f:verbatim>" class='authorImageContainer'>
				<img id='img' src='</f:verbatim><h:outputText value="#{question.imageSrc}" /><f:verbatim>' />
			</div>
		</f:verbatim>
    	

    <h:outputText value="<br />" escape="false" />
  </h:panelGroup>


  <%-- feedbackBlock --%>
  <h:panelGroup styleClass="answerBlock" rendered="#{printSettings.showKeysFeedback}">
	<h:outputLabel value="#{printMessages.correct_feedback}: "/>
	<h:outputText escape="false" value="#{question.itemData.correctItemFeedback}" rendered="#{question.itemData.correctItemFeedback != null && question.itemData.correctItemFeedback != ''}"/>
	<h:outputText escape="false" value="--------" rendered="#{question.itemData.correctItemFeedback == null || question.itemData.correctItemFeedback == ''}"/>
  	<h:outputText value="<br />" escape="false" />
  	<h:outputLabel value="#{printMessages.incorrect_feedback}: "/>
    <h:outputText escape="false" value="#{question.itemData.inCorrectItemFeedback}" rendered="#{question.itemData.inCorrectItemFeedback != null && question.itemData.inCorrectItemFeedback != ''}"/>
    <h:outputText escape="false" value="--------" rendered="#{question.itemData.inCorrectItemFeedback == null || question.itemData.inCorrectItemFeedback == ''}"/>
    <h:outputText value="<br />" escape="false" />
  </h:panelGroup>