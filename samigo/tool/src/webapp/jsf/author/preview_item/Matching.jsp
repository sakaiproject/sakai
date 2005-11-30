<%-- $Id$
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
  <h:outputText escape="false" value="#{question.instruction}" />
  <!-- 1. print out the matching choices -->
  <h:dataTable value="#{question.itemData.itemTextArraySorted}" var="itemText">
    <h:column>
      <h:dataTable value="#{itemText.answerArraySorted}" var="answer"
         rendered="#{itemText.sequence==1}">
        <h:column>
            <h:panelGrid columns="2">
              <h:outputText escape="false" value="#{answer.label}. "/>
              <h:outputText escape="false" value="#{answer.text}" />
            </h:panelGrid>
        </h:column>
      </h:dataTable>
    </h:column>
  </h:dataTable>

  <!-- 2. print out the matching text -->
  <h:dataTable value="#{question.itemData.itemTextArraySorted}" var="itemText">
    <h:column>
      <h:panelGrid columns="2">
        <h:selectOneMenu id="label" disabled="true">
          <f:selectItem itemValue="" itemLabel="select"/>
          <f:selectItem itemValue="" itemLabel="A"/>
          <f:selectItem itemValue="" itemLabel="B"/>
          <f:selectItem itemValue="" itemLabel="C"/>
        </h:selectOneMenu>
        <h:outputText escape="false" value="#{itemText.sequence}. #{itemText.text}" />

        <h:outputText value="" />

        <%-- show correct & incorrect answer feedback, only need to show the set that is attached
             to the correct answer. Look at the data in the table and you may understand this part
             better -daisyf --%>
        <h:dataTable value="#{itemText.answerArray}" var="answer">
            <h:column>
              <h:panelGroup rendered="#{answer.isCorrect && answer.correctAnswerFbIsNotEmpty}" styleClass="longtext">
                <h:outputLabel value="#{msg.correct}: " />
                <h:outputText escape="false" value="#{answer.correctAnswerFeedback}" />
              </h:panelGroup>
            </h:column>
        </h:dataTable>

        <h:outputText value="" />

        <h:dataTable value="#{itemText.answerArray}" var="answer">
            <h:column>
              <h:panelGroup rendered="#{answer.isCorrect && answer.incorrectAnswerFbIsNotEmpty}" styleClass="longtext">
                <h:outputLabel value="#{msg.incorrect}: " />
                <h:outputText escape="false" value="#{answer.inCorrectAnswerFeedback}" />
              </h:panelGroup>
            </h:column>
        </h:dataTable>

      </h:panelGrid>
    </h:column>
  </h:dataTable>

      <%-- answer key --%>
 <h:panelGroup>
      <h:outputLabel value="#{msg.answerKey}: "/>
      <h:outputText escape="false" value="#{question.itemData.answerKey}" />
<f:verbatim><br/></f:verbatim>
</h:panelGroup>
<h:panelGroup rendered="#{question.itemData.correctItemFbIsNotEmpty}">
      <h:outputLabel value="#{msg.correct}:"/>
      <h:outputText value="#{question.itemData.correctItemFeedback}" escape="false" />
<f:verbatim><br/></f:verbatim>
</h:panelGroup>
<h:panelGroup rendered="#{question.itemData.incorrectItemFbIsNotEmpty}">
     <h:outputLabel value="#{msg.incorrect}:"/>
      <h:outputText value="#{question.itemData.inCorrectItemFeedback}" escape="false" />
</h:panelGroup>
