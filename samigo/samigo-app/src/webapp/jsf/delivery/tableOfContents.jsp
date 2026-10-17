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
*      http://www.opensource.org/licenses/ECL-2.0
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
          <h:outputText value="#{deliveryMessages.table_of_contents}"/>
        </title>
        <jsp:element name="script">
          <jsp:attribute name="src" trim="false">/samigo-app/jsf/widget/hideDivision/hideDivision.js</jsp:attribute>
          <jsp:body>
          </jsp:body>
        </jsp:element>
        <jsp:directive.include file="/jsf/delivery/deliveryjQuery.jsp"/>
        <h:outputText value="#{delivery.mathJaxHeader}" escape="false" rendered="#{(delivery.actionString=='takeAssessmentViaUrl' ||  delivery.actionString=='previewAssessment') and delivery.isMathJaxEnabled}"/>
      </head>
      <jsp:element name="body">
        <jsp:attribute name="onload" trim="false"><jsp:expression> request.getAttribute("html.body.onload") </jsp:expression></jsp:attribute>
        <jsp:body>
          <!--div class="portletBody" -->
          <!-- IF A SECURE DELIVERY MODULE HAS BEEN SELECTED, INJECT ITS HTML FRAGMENT (IF ANY) HERE  -->
          <h:outputText value="#{delivery.secureDeliveryHTMLFragment}" escape="false"/>
          <div class="portletBody">
            <h:outputText value="&lt;div style='#{delivery.settings.divBgcolor};#{delivery.settings.divBackground}'&gt;" escape="false"/>
            <!-- content...  -->
            <script>
              <jsp:text>
function isFromLink() {
  if (</jsp:text>
              <jsp:text>${delivery.actionMode}</jsp:text>
              <jsp:text><![CDATA[ == 5) {
    return true;
  } else {
    return false;
  }
}

function noenter(){
return!(window.event && window.event.keyCode == 13);
}

function showElements(theForm) {
  str = "Form Elements of form " + theForm.name + ": \n "
  for (i = 0; i < theForm.length; i++)
    str += theForm.elements[i].name + "\n"
  alert(str)
}

function saveTime()
{
  //showElements(document.forms[0]);
  if((typeof (document.forms[0].elements['tableOfContentsForm:elapsed'])!=undefined) && ((document.forms[0].elements['tableOfContentsForm:elapsed'])!=null) ){
  pauseTiming = 'true';
  var timeElapse = ]]></jsp:text>
              <jsp:text>${delivery.timeElapse}</jsp:text>
              <jsp:text>;
  if (timeElapse) {
	document.forms[0].elements['tableOfContentsForm:elapsed'].value=timeElapse;
  }
 }
}
</jsp:text>
            </script>
            <h:form id="tableOfContentsForm">
              <h:inputHidden id="hasTimeLimit" value="#{delivery.hasTimeLimit}"/>
              <h:inputHidden id="showTimeWarning" value="#{delivery.showTimeWarning}"/>
              <h:inputHidden id="showTimer" value="#{delivery.showTimer}"/>
              <!-- DONE BUTTON FOR PREVIEW  -->
              <h:panelGroup rendered="#{delivery.actionString=='previewAssessment'}">
                <div class="sak-banner-info mb-5">
                  <h:outputText value="#{deliveryMessages.ass_preview}" escape="false"/>
                  <br/>
                  <br/>
                  <h:commandButton value="#{deliveryMessages.exit_preview}" styleClass="exit-preview-button" action="#{person.cleanResourceIdListInPreview}" type="submit" onclick="return returnToHostUrl(&quot;#{delivery.selectURL}&quot;);"/>
                </div>
              </h:panelGroup>
              <h3>
                <h:outputText value="#{delivery.assessmentTitle} " escape="false"/>
              </h3>
              <!-- BEGIN OF TIMER  -->
              <h:panelGroup rendered="#{(delivery.timeElapseAfterFileUpload == null || delivery.timeElapseDouble ge delivery.timeElapseAfterFileUploadDouble) &amp;&amp; delivery.hasTimeLimit == true}">
                <samigo:timerBar height="15" width="300" wait="#{delivery.timeLimit}" elapsed="#{delivery.timeElapse}" expireScript="document.forms[0].elements['takeAssessmentForm:assessmentDeliveryHeading:elapsed'].value=10*'#{delivery.timeElapse}'; document.forms[0].elements['takeAssessmentForm:assessmentDeliveryHeading:outoftime'].value='true'; "/>
              </h:panelGroup>
              <h:panelGroup rendered="#{delivery.timeElapseAfterFileUpload != null &amp;&amp; delivery.timeElapseDouble lt delivery.timeElapseAfterFileUploadDouble &amp;&amp; delivery.hasTimeLimit == true}">
                <samigo:timerBar height="15" width="300" wait="#{delivery.timeLimit}" elapsed="#{delivery.timeElapseAfterFileUpload}" expireScript="document.forms[0].elements['takeAssessmentForm:assessmentDeliveryHeading:elapsed'].value=10*'#{delivery.timeElapse}'; document.forms[0].elements['takeAssessmentForm:assessmentDeliveryHeading:outoftime'].value='true'; "/>
              </h:panelGroup>
              <!-- END OF TIMER  -->
              <h:panelGroup rendered="#{delivery.actionString=='previewAssessment'&amp;&amp; delivery.hasTimeLimit}">
                <f:verbatim>
                  <jsp:text><![CDATA[<div class="sak-banner-info">]]></jsp:text>
                </f:verbatim>
                <h:outputText value="#{deliveryMessages.timer_preview_not_available}"/>
                <f:verbatim>
                  <jsp:text><![CDATA[</div>]]></jsp:text>
                </f:verbatim>
              </h:panelGroup>
              <f:verbatim>
                <br/>
                <jsp:text><![CDATA[</span>]]></jsp:text>
              </f:verbatim>
              <f:verbatim>
                <jsp:text><![CDATA[<div class="tier1">]]></jsp:text>
              </f:verbatim>
              <f:verbatim>
                <jsp:text><![CDATA[<b>]]></jsp:text>
              </f:verbatim>
              <h:outputText value="#{deliveryMessages.warning}#{deliveryMessages.column} "/>
              <f:verbatim>
                <jsp:text><![CDATA[</b>]]></jsp:text>
              </f:verbatim>
              <h:outputText value="#{deliveryMessages.instruction_submitGrading}"/>
              <f:verbatim>
                <jsp:text><![CDATA[</div>]]></jsp:text>
              </f:verbatim>
              <div class="tier1">
                <h4>
                  <h:outputText value="#{deliveryMessages.table_of_contents} "/>
                  <h:outputText styleClass="tier10" value="#{deliveryMessages.tot_score} "/>
                  <h:outputText value="#{delivery.tableOfContents.maxScore}">
                    <f:convertNumber maxFractionDigits="2" groupingUsed="false"/>
                  </h:outputText>
                  <h:outputText value=" #{deliveryMessages.pt}"/>
                </h4>
              </div>
              <div class="tier2">
                <h5>
                  <h:outputLabel value="#{deliveryMessages.key}"/>
                </h5>
                <h:graphicImage alt="#{deliveryMessages.alt_unans_q}" url="/images/whiteBubble15.png"/>
                <h:outputText value="#{deliveryMessages.unans_q}"/>
                <br/>
                <h:graphicImage alt="#{deliveryMessages.alt_ans_q}" url="/images/blackBubble15.png"/>
                <h:outputText value="#{deliveryMessages.ans_q}"/>
                <br/>
                <h:graphicImage alt="#{deliveryMessages.alt_q_marked}" url="/images/questionMarkBubble15.png" rendered="#{delivery.displayMardForReview}"/>
                <h:outputText value="#{deliveryMessages.q_marked}" rendered="#{delivery.displayMardForReview}"/>
                <h:inputHidden id="assessmentID" value="#{delivery.assessmentId}"/>
                <h:inputHidden id="assessTitle" value="#{delivery.assessmentTitle}"/>
                <h:inputHidden id="elapsed" value="#{delivery.timeElapse}"/>
                <h:inputHidden id="outoftime" value="#{delivery.timeOutSubmission}"/>
                <h:messages styleClass="sak-banner-error" rendered="#{! empty facesContext.maximumSeverity}" layout="table"/>
                <h:dataTable value="#{delivery.tableOfContents.partsContents}" var="part" styleClass="tableofcontents">
                  <h:column>
                    <h:panelGroup>
                      <h2>
                        <h:commandLink immediate="true" action="takeAssessment">
                          <jsp:text><![CDATA[
                <label ]]></jsp:text>
                          <h:outputText value="class='inactive'" rendered="#{part.enabled == 0}"/>
                          <jsp:text><![CDATA[>
                    ]]></jsp:text>
                          <h:outputText value="#{deliveryMessages.p} #{part.number} - "/>
                          <h:outputText value="#{part.nonDefaultText} - "/>
                          <h:outputText value="#{part.questions-part.unansweredQuestions}/#{part.questions} #{deliveryMessages.ans_q}, "/>
                          <h:outputText value="#{part.pointsDisplayString}#{deliveryMessages.splash}#{part.roundedMaxPoints} #{deliveryMessages.pt}"/>
                          <h:outputText escape="false" rendered="#{part.timedSection}" value=" &lt;i title='#{authorMessages.timed}' class='fa fa-clock-o'&gt;&lt;/i&gt;"/>
                          <jsp:text><![CDATA[
                </label>
                ]]></jsp:text>
                          <f:param name="partnumber" value="#{part.number}"/>
                          <f:param name="questionnumber" value="1"/>
                          <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.delivery.UpdateTimerFromTOCListener"/>
                          <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.delivery.DeliveryActionListener"/>
                        </h:commandLink>
                      </h2>
                      <h:panelGroup rendered="#{part.enabled &gt;= 0}">
                        <h:dataTable value="#{part.itemContents}" var="question">
                          <h:column>
                            <f:verbatim>
                              <jsp:text><![CDATA[<div class="tier3">]]></jsp:text>
                            </f:verbatim>
                            <h:panelGroup>
                              <h:graphicImage alt="#{deliveryMessages.alt_unans_q}" url="/images/whiteBubble15.png" rendered="#{question.unanswered}"/>
                              <h:graphicImage alt="#{deliveryMessages.alt_unans_q}" url="/images/blackBubble15.png" rendered="#{!question.unanswered}"/>
                              <h:graphicImage alt="#{deliveryMessages.alt_q_marked}" url="/images/questionMarkBubble15.png" rendered="#{question.review}"/>
                              <h:commandLink immediate="true" action="takeAssessment">
                                <jsp:text><![CDATA[<label ]]></jsp:text>
                                <h:outputText value="class='inactive'" rendered="#{question.enabled == 0 || part.enabled == 0}"/>
                                <jsp:text><![CDATA[
                  >]]></jsp:text>
                                <h:outputText escape="false" value="#{question.sequence}#{deliveryMessages.dot}">
                                  <f:convertNumber maxFractionDigits="2"/>
                                </h:outputText>
                                <h:outputText escape="false" value=" #{question.strippedText}" rendered="#{question.enabled &gt;= 0}">
                                </h:outputText>
                                <h:outputText escape="false" value=" #{deliveryMessages.title_not_available}" rendered="#{question.enabled == -1}">
                                </h:outputText>
                                <h:outputText escape="false" value=" (#{question.pointsDisplayString}#{deliveryMessages.splash}#{question.roundedMaxPointsToDisplay} #{deliveryMessages.pt})" rendered="#{(delivery.settings.displayScoreDuringAssessments != '2' &amp;&amp; question.itemData.scoreDisplayFlag) || question.pointsDisplayString!=''}">
                                  <f:convertNumber maxFractionDigits="2" groupingUsed="false"/>
                                </h:outputText>
                                <h:outputText escape="false" rendered="#{question.timedQuestion}" value=" &lt;i title='#{authorMessages.timed}' class='fa fa-clock-o'&gt;&lt;/i&gt;"/>
                                <jsp:text><![CDATA[
                  </label>
                  ]]></jsp:text>
                                <f:param name="partnumber" value="#{part.number}"/>
                                <f:param name="questionnumber" value="#{question.number}"/>
                                <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.delivery.UpdateTimerFromTOCListener"/>
                                <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.delivery.DeliveryActionListener"/>
                              </h:commandLink>
                            </h:panelGroup>
                            <f:verbatim>
                              <jsp:text><![CDATA[</div>]]></jsp:text>
                            </f:verbatim>
                          </h:column>
                        </h:dataTable>
                      </h:panelGroup>
                    </h:panelGroup>
                  </h:column>
                </h:dataTable>
              </div>
              <p class="act">
                <!-- SUBMIT FOR GRADE BUTTON FOR TAKE ASSESSMENT AND PREVIEW ASSESSMENT  -->
                <!-- check permisison to determine if the button should be displayed  -->
                <h:panelGroup rendered="#{delivery.actionString=='previewAssessment'&#10;                         || (delivery.actionString=='takeAssessment' &#10;                             &amp;&amp; authorization!=null &#10;                             &amp;&amp; authorization.takeAssessment &#10;                             &amp;&amp; authorization.submitAssessmentForGrade)}">
                  <h:commandButton id="submitForGradeTOC1" type="submit" value="#{deliveryMessages.button_submit_grading}" action="#{delivery.confirmSubmitTOC}" styleClass="active" onclick="saveTime()" disabled="#{delivery.actionString=='previewAssessment'}"/>
                </h:panelGroup>
                <h:commandButton id="save" type="submit" value="#{commonMessages.action_save}" action="#{delivery.saveWork}" rendered="#{delivery.actionString=='previewAssessment'&#10;      || delivery.actionString=='takeAssessment'&#10;      || delivery.actionString=='takeAssessmentViaUrl'}"/>
                <!-- SUBMIT BUTTON FOR TAKE ASSESSMENT VIA URL ONLY  -->
                <h:commandButton id="submitForGradeTOC2" type="submit" value="#{deliveryMessages.button_submit_grading}" action="#{delivery.confirmSubmitTOC}" styleClass="active" rendered="#{delivery.actionString=='takeAssessmentViaUrl'}"/>
                <!-- SAVE AND EXIT BUTTON FOR TAKE ASSESMENT AND PREVIEW ASSESSMENT -->
                <h:commandButton id="exitTOC1" type="submit" value="#{deliveryMessages.button_exit}" action="#{delivery.saveAndExit}" onclick="saveTime()" rendered="#{(delivery.actionString=='takeAssessment'&#10;             || delivery.actionString=='previewAssessment') &amp;&amp; !delivery.hasTimeLimit}" disabled="#{delivery.actionString=='previewAssessment'}"/>
                <!-- QUIT BUTTON FOR TAKE ASSESSMENT VIA URL  -->
                <h:commandButton id="exitTOC2" type="submit" value="#{deliveryMessages.button_exit}" action="#{delivery.saveAndExit}" onclick="saveTime()" rendered="#{delivery.actionString=='takeAssessmentViaUrl' &amp;&amp; !delivery.hasTimeLimit}">
                </h:commandButton>
              </p>
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
