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
  <h:outputText styleClass="questionBlock" escape="false" value="#{question.text}" />

  <h:panelGrid columns="2" width="99%">
    <%-- matching items --%>
    <h:panelGroup>
      <h:dataTable value="#{question.matchingArray}" var="matching">
        <h:column>
          <h:outputText value="#{matching.text}" escape="false"/>
        </h:column>
      </h:dataTable>
    </h:panelGroup>

    <%-- matching choices --%>
    <h:panelGroup>
      <h:dataTable styleClass="inputBlock" value="#{question.answers}" var="answer">
        <h:column>
          <h:outputText value="" escape="false" />
          <h:outputText value="#{answer}" escape="false" />
        </h:column>
      </h:dataTable>
    </h:panelGroup>
  </h:panelGrid>

  <h:outputText escape="false" value="<hr />" rendered="#{printSettings.showKeys}" />
  <h:panelGroup styleClass="answerBlock" rendered="#{printSettings.showKeys}">
    <h:outputLabel value="#{msg.answerKey}: "/>

    <%-- answers from items to choices --%>
    <h:outputText escape="false" value="#{question.key}" />
    <h:outputText escape="false" value="<br/>" />

    <%-- answers from choices to items --%>
    <h:outputText escape="false" value="#{question.itemData.answerKey}" />
  </h:panelGroup>
