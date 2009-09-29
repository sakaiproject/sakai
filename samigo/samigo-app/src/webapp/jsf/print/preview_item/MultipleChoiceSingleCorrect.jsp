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

  <h:dataTable value="#{question.itemData.itemTextArraySorted}" var="itemText">
    <h:column>
      <h:dataTable value="#{itemText.answerArraySorted}" var="answer">
        <h:column> 
       <%-- inputBlock --%>
         <h:panelGroup styleClass="inputBlock" rendered="#{answer.text != null && answer.text ne ''}">
          <%-- 
         <h:graphicImage id="image1" rendered="#{answer.isCorrect}"
             alt="#{msg.correct}" url="/images/checked.gif" />--%>         
          <h:graphicImage id="image2" 
             alt="#{msg.not_correct}" url="/images/unchecked.gif"/>      
          <h:outputText escape="false" value="#{answer.label}. #{answer.text}" /> 
       </h:panelGroup>
        </h:column>
      </h:dataTable>
    </h:column>
  </h:dataTable>

  <%-- answerBlock --%>
  <h:outputText escape="false" value="<hr />" rendered="#{printSettings.showKeys}"/>
  <h:panelGroup styleClass="answerBlock" rendered="#{printSettings.showKeys}">
    <h:outputLabel value="#{msg.answerKey}: "/>
    <h:outputText escape="false" value="#{question.key}" />
  </h:panelGroup>