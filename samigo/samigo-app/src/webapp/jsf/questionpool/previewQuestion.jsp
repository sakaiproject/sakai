<%@ page contentType="text/html;charset=utf-8" pageEncoding="utf-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://www.sakaiproject.org/samigo" prefix="samigo" %>
<!DOCTYPE html
     PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
     "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">


  <f:view>
    <html xmlns="http://www.w3.org/1999/xhtml">
      <head><%= request.getAttribute("html.head") %>
      <title><h:outputText value="#{questionPoolMessages.t_previewQuestion}"/></title>
                        <!-- stylesheet and script widgets -->
<script language="javascript" type="text/JavaScript">
<!--
<%@ include file="/js/samigotree.js" %>
//-->
</script>
      </head>
<body onload="<%= request.getAttribute("html.body.onload") %>">
<!-- content... -->
 <div class="portletBody">
<h:form id="previewQuestion">
<h:messages infoClass="validation" warnClass="validation" errorClass="validation" fatalClass="validation"/>
<h3><h:outputText value="#{questionPoolMessages.t_previewQuestion}"/></h3>


<div class="longtext tier2">
<h:dataTable id="questions" value="#{questionpool.itemBean}" var="question" >
    <h:column>
    <h:panelGrid columns="2" border="1" width="100%">
        <h:panelGroup>
            <h:outputText styleClass="tier1" value="#{authorMessages.q}" />
            <h:outputText styleClass="tier1" rendered="#{question.itemData.typeId== 1}" value="#{authorMessages.multiple_choice_type}"/>
            <h:outputText styleClass="tier1" rendered="#{question.itemData.typeId== 2}" value="#{authorMessages.multiple_choice_type}"/>
            <h:outputText styleClass="tier1" rendered="#{question.itemData.typeId== 3}" value="#{authorMessages.multiple_choice_surv}"/>
            <h:outputText styleClass="tier1" rendered="#{question.itemData.typeId== 4}" value="#{authorMessages.true_false}"/>
            <h:outputText styleClass="tier1" rendered="#{question.itemData.typeId== 5}" value="#{authorMessages.short_answer_essay}"/>
            <h:outputText styleClass="tier1" rendered="#{question.itemData.typeId== 6}" value="#{authorMessages.file_upload}"/>
            <h:outputText styleClass="tier1" rendered="#{question.itemData.typeId== 7}" value="#{authorMessages.audio_recording}"/>
            <h:outputText styleClass="tier1" rendered="#{question.itemData.typeId== 8}" value="#{authorMessages.fill_in_the_blank}"/>
            <h:outputText styleClass="tier1" rendered="#{question.itemData.typeId== 9}" value="#{authorMessages.matching}"/>
            <h:outputText styleClass="tier1" rendered="#{question.itemData.typeId== 11}" value="#{authorMessages.fill_in_numeric}"/>
            <h:outputText styleClass="tier1" rendered="#{question.itemData.typeId== 12}" value="#{authorMessages.multiple_choice_type}"/>
            <h:outputText styleClass="tier1" value="#{question.itemData.score}" />
            <h:outputText styleClass="tier1" value="#{authorMessages.points_lower_case}" />
        </h:panelGroup>

    </h:panelGrid>

    <h:panelGrid>
        <h:panelGroup rendered="#{question.itemData.typeId == 9}">
            <%@ include file="/jsf/author/preview_item/Matching.jsp" %>
        </h:panelGroup>
        <h:panelGroup rendered="#{question.itemData.typeId == 11}">
            <%@ include file="/jsf/author/preview_item/FillInNumeric.jsp" %>
        </h:panelGroup>

        <h:panelGroup rendered="#{question.itemData.typeId == 8}">
            <%@ include file="/jsf/author/preview_item/FillInTheBlank.jsp" %>
        </h:panelGroup>

        <h:panelGroup rendered="#{question.itemData.typeId == 7}">
            <%@ include file="/jsf/author/preview_item/AudioRecording.jsp" %>
        </h:panelGroup>

        <h:panelGroup rendered="#{question.itemData.typeId == 6}">
            <%@ include file="/jsf/author/preview_item/FileUpload.jsp" %>
        </h:panelGroup>

        <h:panelGroup rendered="#{question.itemData.typeId == 5}">
            <%@ include file="/jsf/author/preview_item/ShortAnswer.jsp" %>
        </h:panelGroup>

        <h:panelGroup rendered="#{question.itemData.typeId == 4}">
            <%@ include file="/jsf/author/preview_item/TrueFalse.jsp" %>
        </h:panelGroup>

        <!-- same as multiple choice single -->
        <h:panelGroup rendered="#{question.itemData.typeId == 3}">
            <%@ include file="/jsf/author/preview_item/MultipleChoiceSurvey.jsp" %>
        </h:panelGroup>

        <h:panelGroup rendered="#{question.itemData.typeId == 2}">
            <%@ include file="/jsf/author/preview_item/MultipleChoiceMultipleCorrect.jsp" %>
        </h:panelGroup>

        <h:panelGroup rendered="#{question.itemData.typeId == 1}">
            <%@ include file="/jsf/author/preview_item/MultipleChoiceSingleCorrect.jsp" %>
        </h:panelGroup>

        <h:panelGroup rendered="#{question.itemData.typeId == 12}">
            <%@ include file="/jsf/author/preview_item/MultipleChoiceMultipleCorrect.jsp" %>
        </h:panelGroup>

    </h:panelGrid>

    </h:column>

</h:dataTable>

<h:panelGroup styleClass="h3text">
    <h:outputText value="#{questionPoolMessages.t_historical}" />
</h:panelGroup>

<h:dataTable id="historical" value="#{questionpool.itemBean.get(0).itemData.itemHistorical}" var="historical" styleClass="table table-striped table-bordered">
    <h:column>
        <h:outputText value="#{historical.modifiedBy}" >
            <f:converter converterId="org.sakaiproject.tool.assessment.jsf.convert.UsernameConverter" />
        </h:outputText>
    </h:column>
    <h:column>
        <h:outputText value="#{historical.modifiedDate}" >
            <f:convertDateTime dateStyle="medium" timeStyle="short" timeZone="#{author.userTimeZone}" />
        </h:outputText>
    </h:column>
</h:dataTable>

</div>

<p class="act">

  <h:commandButton id="cancel" value="#{questionPoolMessages.return_action}" action="#{questionpool.editPool}">
    <f:param name="qpid" value="#{questionpool.currentPool.id}"/>
  </h:commandButton>

</p>

</h:form>
</div>
</body>
</html>
</f:view>
