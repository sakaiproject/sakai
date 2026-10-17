<?xml version="1.0" encoding="UTF-8"?>
<jsp:root version="3.0"
    xmlns:jsp="http://java.sun.com/JSP/Page"
    xmlns:h="http://java.sun.com/jsf/html"
    xmlns:f="http://java.sun.com/jsf/core"
    xmlns:samigo="http://www.sakaiproject.org/samigo">
<jsp:directive.page pageEncoding="UTF-8" contentType="text/html;charset=utf-8" trimDirectiveWhitespaces="false" isELIgnored="false" deferredSyntaxAllowedAsLiteral="false"/>
<jsp:output omit-xml-declaration="yes"/>
  <jsp:directive.include file="/include/header.inc"/>
  <jsp:directive.page language="java"/>
  <jsp:text><![CDATA[



<!DOCTYPE html
     PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
     "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

]]></jsp:text>
  <!--
* $Id$

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

 -->
  <f:view>
    <html xmlns="http://www.w3.org/1999/xhtml" lang="en" xml:lang="en">
      <head>
        <jsp:expression> request.getAttribute("html.head") </jsp:expression>
        <title>
          <h:outputText value="#{deliveryMessages.submission}"/>
        </title>
      </head>
      <script>
        <jsp:text><![CDATA[
function reviewAssessment(field){
var insertlinkid= field.id.replace("reviewAssessment", "hiddenlink");
var newindex = 0;
for (i=0; i<document.links.length; i++) {
  if(document.links[i].id == insertlinkid)
  {
    newindex = i;
    break;
  }
}

document.links[newindex].onclick();
}

function closeWindow() {alert("1"); self.opener=this; self.close(); }
]]></jsp:text>
      </script>
      <jsp:element name="body">
        <jsp:attribute name="onload" trim="false"><jsp:expression> request.getAttribute("html.body.onload") </jsp:expression></jsp:attribute>
        <jsp:body>
          <!-- IF A SECURE DELIVERY MODULE HAS BEEN SELECTED, INJECT ITS HTML FRAGMENT (IF ANY) HERE  -->
          <h:outputText value="#{delivery.secureDeliveryHTMLFragment}" escape="false"/>
          <div class="portletBody container-fluid">
            <h:outputText value="&lt;div style='#{delivery.settings.divBgcolor};#{delivery.settings.divBackground}'&gt;" escape="false"/>
            <!-- content...  -->
            <div class="page-header">
              <h1>
                <h:outputText value="#{deliveryMessages.submission}"/>
                <small>
                  <h:outputText value="#{delivery.assessmentTitle} " escape="false"/>
                </small>
              </h1>
            </div>
            <jsp:directive.include file="/jsf/delivery/deliveryjQuery.jsp"/>
            <div id="timer-expired-warning" style="display:none;">
              <h3>
                <h:outputText value="#{deliveryMessages.time_expired1}"/>
              </h3>
              <p>
                <h:outputText value="#{deliveryMessages.time_expired3}"/>
              </p>
              <div id="squaresWaveG">
                <jsp:element name="div">
                  <jsp:attribute name="id" trim="false">squaresWaveG_1</jsp:attribute>
                  <jsp:attribute name="class" trim="false">squaresWaveG</jsp:attribute>
                  <jsp:body>
                  </jsp:body>
                </jsp:element>
                <jsp:element name="div">
                  <jsp:attribute name="id" trim="false">squaresWaveG_2</jsp:attribute>
                  <jsp:attribute name="class" trim="false">squaresWaveG</jsp:attribute>
                  <jsp:body>
                  </jsp:body>
                </jsp:element>
                <jsp:element name="div">
                  <jsp:attribute name="id" trim="false">squaresWaveG_3</jsp:attribute>
                  <jsp:attribute name="class" trim="false">squaresWaveG</jsp:attribute>
                  <jsp:body>
                  </jsp:body>
                </jsp:element>
                <jsp:element name="div">
                  <jsp:attribute name="id" trim="false">squaresWaveG_4</jsp:attribute>
                  <jsp:attribute name="class" trim="false">squaresWaveG</jsp:attribute>
                  <jsp:body>
                  </jsp:body>
                </jsp:element>
                <jsp:element name="div">
                  <jsp:attribute name="id" trim="false">squaresWaveG_5</jsp:attribute>
                  <jsp:attribute name="class" trim="false">squaresWaveG</jsp:attribute>
                  <jsp:body>
                  </jsp:body>
                </jsp:element>
                <jsp:element name="div">
                  <jsp:attribute name="id" trim="false">squaresWaveG_6</jsp:attribute>
                  <jsp:attribute name="class" trim="false">squaresWaveG</jsp:attribute>
                  <jsp:body>
                  </jsp:body>
                </jsp:element>
                <jsp:element name="div">
                  <jsp:attribute name="id" trim="false">squaresWaveG_7</jsp:attribute>
                  <jsp:attribute name="class" trim="false">squaresWaveG</jsp:attribute>
                  <jsp:body>
                  </jsp:body>
                </jsp:element>
                <jsp:element name="div">
                  <jsp:attribute name="id" trim="false">squaresWaveG_8</jsp:attribute>
                  <jsp:attribute name="class" trim="false">squaresWaveG</jsp:attribute>
                  <jsp:body>
                  </jsp:body>
                </jsp:element>
              </div>
            </div>
            <h:form id="submittedForm">
              <div class="table">
                <h:outputText id="renderTimeoutMessage" styleClass="sak-banner-error" value="#{deliveryMessages.timeOutSubmission}" rendered="#{delivery.timeOutSubmission=='true'}"/>
                <h:messages styleClass="sak-banner-error" rendered="#{! empty facesContext.maximumSeverity}" layout="table"/>
                <h:outputText value="#{deliveryMessages.submission_confirmation_message_1}" rendered="#{!delivery.actionString=='takeAssessmentViaUrl'}"/>
                <h:outputText value="#{deliveryMessages.submission_confirmation_message_4}" rendered="#{delivery.actionString=='takeAssessmentViaUrl'}"/>
                <h:outputText escape="false" value="&lt;br /&gt;&lt;br /&gt; #{delivery.submissionMessage}"/>
                <h:panelGrid columns="2" styleClass="table table-striped" columnClasses="submissionInfoCol1, submissionInfoCol2">
                  <h:outputLabel value="#{deliveryMessages.course_name}"/>
                  <h:outputText value="#{delivery.courseName}"/>
                  <h:outputLabel value="#{deliveryMessages.creator}"/>
                  <h:outputText value="#{delivery.creatorName}"/>
                  <h:outputLabel value="#{deliveryMessages.assessment_title}"/>
                  <h:outputText value="#{delivery.assessmentTitle}" escape="false"/>
                  <h:outputLabel value="#{deliveryMessages.number_of_sub_remain}"/>
                  <h:panelGroup>
                    <h:outputText value="#{delivery.submissionsRemaining} #{deliveryMessages.text_out_of} #{delivery.settings.maxAttempts}" rendered="#{!delivery.settings.unlimitedAttempts}"/>
                    <h:outputText value="#{deliveryMessages.unlimited_}" rendered="#{delivery.settings.unlimitedAttempts}"/>
                  </h:panelGroup>
                  <h:outputLabel value="#{deliveryMessages.conf_num}"/>
                  <h:outputText value="#{delivery.confirmation}"/>
                  <h:outputLabel value="#{deliveryMessages.submission_dttm}"/>
                  <h:outputText value="#{delivery.submissionDate}">
                    <f:convertDateTime dateStyle="medium" timeStyle="short" timeZone="#{delivery.userTimeZone}"/>
                  </h:outputText>
                  <h:outputLabel rendered="#{delivery.submissionFiles.values().size() &gt; 0}" value="#{deliveryMessages.submission_files}"/>
                  <h:dataTable rendered="#{delivery.submissionFiles.values().size() &gt; 0}" value="#{delivery.submissionFiles.values()}" var="media">
                    <h:column>
                      <h:outputLink title="#{evaluationMessages.t_fileUpload}" value="/samigo-app/servlet/ShowMedia?mediaId=#{media.mediaId}" target="new_window">
                        <h:outputText value="#{media.filename} "/>
                      </h:outputLink>
                    </h:column>
                    <h:column>
                      <h:outputText value=" #{evaluationMessages.open_bracket} #{media.fileSizeKBFormat} #{generalMessages.kb} #{evaluationMessages.close_bracket}"/>
                    </h:column>
                  </h:dataTable>
                  <h:outputLabel value="#{deliveryMessages.final_page}" rendered="#{delivery.url!=null &amp;&amp; delivery.url!=''}"/>
                  <h:outputLink title="#{deliveryMessages.t_url}" value="#" rendered="#{delivery.url!=null &amp;&amp; delivery.url!=''}" onclick="window.open('#{delivery.url}','new_window');" onkeypress="window.open('#{delivery.url}','new_window');">
                    <h:outputText value="#{delivery.url}" escape="false"/>
                  </h:outputLink>
                  <h:outputLabel value="#{deliveryMessages.anonymousScore}" rendered="#{delivery.actionString=='takeAssessmentViaUrl' &amp;&amp; delivery.anonymousLogin &amp;&amp; (delivery.feedbackComponent.showImmediate || delivery.feedbackComponent.showOnSubmission || delivery.feedbackOnDate) &amp;&amp; delivery.feedbackComponentOption=='1'}"/>
                  <h:outputText value="&lt;b&gt;#{delivery.roundedRawScoreViaURL}&lt;/b&gt;" rendered="#{delivery.actionString=='takeAssessmentViaUrl' &amp;&amp; delivery.anonymousLogin &amp;&amp; (delivery.feedbackComponent.showImmediate || delivery.feedbackComponent.showOnSubmission || delivery.feedbackOnDate) &amp;&amp; delivery.feedbackComponentOption=='1'}" escape="false"/>
                </h:panelGrid>
              </div>
              <p>
                <h:outputText rendered="#{!delivery.anonymousLogin}" value="#{delivery.receiptEmailSetting} #{deliveryMessages.receiptEmail_changeSetting}" escape="false"/>
              </p>
              <div class="tier1">
                <h:panelGrid columns="2" cellpadding="3" cellspacing="3">
                  <h:commandButton type="submit" value="#{deliveryMessages.button_continue}" onclick="return returnToHostUrl(&quot;#{delivery.selectURL}&quot;);" rendered="#{delivery.actionString=='takeAssessment' || delivery.actionString=='takeAssessmentViaUrl'&#10;            &amp;&amp; !delivery.sebActive}"/>
                  <h:commandButton value="#{deliveryMessages.review_results}" type="button" id="reviewAssessment" rendered="#{delivery.actionString=='takeAssessmentViaUrl' &amp;&amp; (delivery.anonymousLogin || delivery.accessByUrlAndAuthorized) &amp;&amp; (delivery.feedbackComponent.showImmediate || delivery.feedbackComponent.showOnSubmission || delivery.feedbackOnDate) &amp;&amp; delivery.feedbackComponentOption=='2' &amp;&amp; !delivery.sebActive}" style="act" onclick="reviewAssessment(this);" onkeypress="reviewAssessment(this);"/>
                  <h:commandLink id="hiddenlink" action="takeAssessment" rendered="#{delivery.actionString=='takeAssessmentViaUrl' &amp;&amp; (delivery.anonymousLogin || delivery.accessByUrlAndAuthorized) &amp;&amp; (delivery.feedbackComponent.showImmediate || delivery.feedbackComponent.showOnSubmission || delivery.feedbackOnDate) &amp;&amp; delivery.feedbackComponentOption=='2' &amp;&amp; !delivery.sebActive}">
                    <f:param name="publishedId" value="#{delivery.assessmentId}"/>
                    <f:param name="nofeedback" value="false"/>
                    <f:param name="actionString" value="reviewAssessment"/>
                    <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.delivery.BeginDeliveryActionListener"/>
                    <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.delivery.DeliveryActionListener"/>
                  </h:commandLink>
                  <h:commandButton type="button" value="#{deliveryMessages.seb_quit_browser}" onclick="quitSeb()" rendered="#{delivery.sebActive}"/>
                </h:panelGrid>
              </div>
            </h:form>
            <!-- end content  -->
          </div>
          <jsp:text><![CDATA[
</div>

      ]]></jsp:text>
        </jsp:body>
      </jsp:element>
    </html>
  </f:view>
</jsp:root>
