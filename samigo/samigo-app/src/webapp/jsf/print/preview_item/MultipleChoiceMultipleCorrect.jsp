<%-- 
include file for delivering multiple choice questions
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
  <h:outputText styleClass="questionBlock" escape="false" value="#{question.itemData.text}" />
  
  <h:outputText value="<br />" escape="false" />
  <h:outputText value="<br />" escape="false" />    
  <%@ include file="/jsf/delivery/item/attachment.jsp" %>

  <h:dataTable value="#{question.itemData.itemTextArraySorted}" var="itemText" width="100%">
    <h:column>
      <h:dataTable value="#{itemText.answerArraySorted}" var="answer" width="100%">
        <h:column> 
          <%-- inputBlock --%>
          <h:panelGroup styleClass="inputBlock" rendered="#{answer.text != null && answer.text ne ''}">
                     
            <%-- Show answer text --%>
            <h:graphicImage id="image1" url="/images/unchecked.gif"/>
            <h:outputText escape="false" value="#{answer.label}. " />
            <h:outputText escape="false" value="#{answer.text}" styleClass="mcAnswerText"/>
            
          </h:panelGroup>
        </h:column>
        <h:column>
          <%-- Show feedback answer --%>
          <h:panelGroup styleClass="feedbackBlock" rendered="#{printSettings.showKeysFeedback && answer.text !=null && answer.text!=''}">
  			<h:outputLabel value="#{commonMessages.feedback}: " />
  			<h:outputText escape="false" value="#{answer.generalAnswerFeedback}" 
  				rendered="#{answer.generalAnswerFeedback != null && answer.generalAnswerFeedback != '' && answer.generalAnswerFeedback != '&nbsp;'}"/>
  			<h:outputText escape="false" value="--------" 
  				rendered="#{answer.generalAnswerFeedback == null || answer.generalAnswerFeedback == '' || answer.generalAnswerFeedback == '&nbsp;'}"/>
  	 	  </h:panelGroup>
        </h:column>
      </h:dataTable>
    </h:column>
  </h:dataTable>

  <%-- answerBlock --%>
  <h:panelGroup styleClass="answerBlock" rendered="#{printSettings.showKeys || printSettings.showKeysFeedback}">
  	<h:outputLabel value="#{printMessages.answer_point}: "/>
  	<h:outputText value="#{question.itemData.score}">
        <f:convertNumber maxFractionDigits="2"/>
    </h:outputText>
    <h:outputText escape="false" value=" #{authorMessages.points_lower_case}" />
  	<h:outputText value="<br />" escape="false" />
    <h:outputLabel value="#{printMessages.answer_key}: "/>
    <h:outputText escape="false" value="#{question.key}" />
    <h:outputText value="<br />" escape="false" />
  </h:panelGroup>
  
  <%-- feedbackBlock --%>
  <h:panelGroup styleClass="answerBlock" rendered="#{printSettings.showKeysFeedback}">
	<h:outputLabel value="#{printMessages.correct_feedback}: "/>
	<h:outputText escape="false" value="#{question.itemData.correctItemFeedback}" 
		rendered="#{question.itemData.correctItemFeedback != null && question.itemData.correctItemFeedback != '' && question.itemData.correctItemFeedback != '&nbsp;'}"/>
	<h:outputText escape="false" value="--------" 
		rendered="#{question.itemData.correctItemFeedback == null || question.itemData.correctItemFeedback == '' || question.itemData.correctItemFeedback == '&nbsp;'}"/>
  	<h:outputText value="<br />" escape="false" />
  	<h:outputLabel value="#{printMessages.incorrect_feedback}: "/>
    <h:outputText escape="false" value="#{question.itemData.inCorrectItemFeedback}" 
    	rendered="#{question.itemData.inCorrectItemFeedback != null && question.itemData.inCorrectItemFeedback != '' && question.itemData.inCorrectItemFeedback != '&nbsp;'}"/>
    <h:outputText escape="false" value="--------" 
    	rendered="#{question.itemData.inCorrectItemFeedback == null || question.itemData.inCorrectItemFeedback == '' || question.itemData.inCorrectItemFeedback == '&nbsp;'}"/>
    <h:outputText value="<br />" escape="false" />
  </h:panelGroup>
  
