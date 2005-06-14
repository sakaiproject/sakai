<%-- $Id: FileUpload.jsp,v 1.8 2005/06/10 20:52:46 daisyf.stanford.edu Exp $
include file for delivering file upload questions
should be included in file importing DeliveryMessages
--%>
  <%@ taglib uri="http://java.sun.com/upload" prefix="corejsf" %>

  <h:outputText escape="false" value="#{question.itemData.text}" />
  <h:panelGrid columns="1" width="100%">
    <h:outputText escape="false" value="#{msg.upload_instruction}" />
    <h:panelGroup>
      <h:outputText value="File:" />
<%--
      <corejsf:upload target="test_fileupload/"/>
--%>
      <h:inputText size="50" /> 
      <h:outputText value="  " />
      <h:commandButton value="Browse" type="button"/>
      <h:outputText value="  " />
      <h:commandButton value="Upload" type="button"/>
    </h:panelGroup>
  </h:panelGrid>
  <h:dataTable value="#{question.itemData.itemTextArraySorted}" var="itemText">
    <h:column>
      <h:dataTable value="#{itemText.answerArray}" var="answer">
        <h:column>
          <h:outputText escape="false" value="#{msg.preview_model_short_answer}" />
          <h:outputText escape="false" value="#{answer.text}" />
        </h:column>
      </h:dataTable>
 </h:column>
  </h:dataTable>
<f:verbatim> <div class="longtext"></f:verbatim>

 <h:outputLabel rendered="#{question.itemData.generalItemFeedback != null && question.itemData.generalItemFeedback ne ''}" value="#{msg.general_fb}: " />
  <h:outputText escape="false" value="#{question.itemData.generalItemFeedback}" />
<f:verbatim> </div></f:verbatim>

