<%@ page contentType="text/html;charset=utf-8" pageEncoding="utf-8" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://www.sakaiproject.org/samigo" prefix="samigo" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>

<!DOCTYPE html
     PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
     "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

  <f:view>
    <html xmlns="http://www.w3.org/1999/xhtml">
      <head><%= request.getAttribute("html.head") %>
      <title><h:outputText
        value="#{evaluationMessages.title_total}" /></title>
    <samigo:script path="/jsf/widget/hideDivision/hideDivision.js" />

      </head>
  <body onload="hideUnhideAllDivsExceptFirst('none');;<%= request.getAttribute("html.body.onload") %>">
<!-- $Id:  -->
<!-- content... -->
<script>
function toPoint(id)
{
  var x=document.getElementById(id).value
  document.getElementById(id).value=x.replace(',','.')
}

function clickEmailLink(field){
var emaillinkid= field.id.replace("createEmail", "hiddenlink");

var newindex = 0;
for (i=0; i<document.links.length; i++) {
  if(document.links[i].id == emaillinkid)
  {
    newindex = i;
    break;
  }
}

document.links[newindex].onclick();
window.open('../evaluation/createNewEmail.faces','createEmail','width=600,height=600,scrollbars=yes, resizable=yes');
}

function clickEmailLink(field, fromName, fromEmailAddress, toName, toEmailAddress, assessmentName){
var emaillinkid= field.id.replace("createEmail", "hiddenlink");
var newindex = 0;
for (i=0; i<document.links.length; i++) {
  if(document.links[i].id == emaillinkid)
  {
    newindex = i;
    break;
  }
}

document.links[newindex].onclick();
window.open("../evaluation/createNewEmail.faces?fromEmailLinkClick=true&fromName=" + fromName + "&fromEmailAddress=" + fromEmailAddress + "&toName=" + toName + "&toEmailAddress=" + toEmailAddress +  "&assessmentName=" + assessmentName,'createEmail','width=600,height=600,scrollbars=yes, resizable=yes');

document.location='../evaluation/gradeStudentResult';
}

</script>

 <div class="portletBody">
<h:form id="editStudentResults">
  <h:inputHidden id="publishedIdd" value="#{studentScores.publishedId}" />
  <h:inputHidden id="publishedId" value="#{studentScores.publishedId}" />
  <h:inputHidden id="studentid" value="#{studentScores.studentId}" />
  <h:inputHidden id="studentName" value="#{studentScores.studentName}" />
  <h:inputHidden id="gradingData" value="#{studentScores.assessmentGradingId}" />
  <h:inputHidden id="itemId" value="#{studentScores.itemId}" />

  <!-- HEADINGS -->
  <%@ include file="/jsf/evaluation/evaluationHeadings.jsp" %>

  <h3>
    <h:outputText value="#{studentScores.studentName}" rendered="#{totalScores.anonymous eq 'false'}"/>
    <h:outputText value="#{evaluationMessages.submission_id}#{deliveryMessages.column} #{studentScores.assessmentGradingId}" rendered="#{totalScores.anonymous eq 'true'}"/>
  </h3>
  <p class="navViewAction">
    <h:commandLink title="#{evaluationMessages.t_submissionStatus}" action="submissionStatus" immediate="true">
      <h:outputText value="#{evaluationMessages.sub_status}" />
      <f:param name="allSubmissions" value="true"/>
      <f:actionListener
        type="org.sakaiproject.tool.assessment.ui.listener.evaluation.SubmissionStatusListener" />
    </h:commandLink>
    <h:outputText value=" #{evaluationMessages.separator} " />
    <h:commandLink title="#{evaluationMessages.t_totalScores}" action="totalScores" immediate="true">
      <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.evaluation.ResetTotalScoreListener" />
      <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.evaluation.TotalScoreListener" />
      <h:outputText value="#{evaluationMessages.title_total}" />
    </h:commandLink>
    
    <h:outputText value=" #{evaluationMessages.separator} " rendered="#{totalScores.firstItem ne ''}"  />
    <h:commandLink title="#{evaluationMessages.t_histogram}" action="histogramScores" immediate="true"
      rendered="#{totalScores.firstItem ne ''}" >
      <h:outputText value="#{evaluationMessages.stat_view}" />
      <f:actionListener
        type="org.sakaiproject.tool.assessment.ui.listener.evaluation.HistogramListener" />
    </h:commandLink>
  </p>

  <h:messages infoClass="validation" warnClass="validation" errorClass="validation" fatalClass="validation"/>

<f:verbatim><h4></f:verbatim>
<h:outputText value="#{totalScores.assessmentName}" escape="false"/>
<f:verbatim></h4></f:verbatim>
<div class="tier3">
<h:panelGrid columns="2">
   <h:outputText value="#{deliveryMessages.comment}#{deliveryMessages.column}"/>
   <h:inputTextarea value="#{studentScores.comments}" rows="3" cols="30"/>
   </h:panelGrid>
</div>
<f:verbatim><h4></f:verbatim>
<h:outputText value="#{deliveryMessages.table_of_contents}" />
<f:verbatim></h4></f:verbatim>

<div class="tier2">
  <h:dataTable value="#{delivery.tableOfContents.partsContents}" var="part">
  <h:column>
    <h:panelGroup>
      <samigo:hideDivision id="part" title = " #{deliveryMessages.p} #{part.number} #{evaluationMessages.dash} #{part.text} #{evaluationMessages.dash}
       #{part.questions-part.unansweredQuestions}#{evaluationMessages.splash}#{part.questions} #{deliveryMessages.ans_q}, #{part.pointsDisplayString} #{part.maxPoints} #{deliveryMessages.pt}" > 
        <h:dataTable value="#{part.itemContents}" var="question">
          <h:column>
            <f:verbatim><h4 class="tier3"></f:verbatim>
              <h:panelGroup>
                <h:outputLink value="##{part.number}#{deliveryMessages.underscore}#{question.number}"> 
                  <h:outputText escape="false" value="#{question.number}#{deliveryMessages.dot} #{question.strippedText} #{question.maxPoints} #{deliveryMessages.pt} ">
                  </h:outputText>
                </h:outputLink>
              </h:panelGroup>
            <f:verbatim></h4></f:verbatim> 
          </h:column>
        </h:dataTable>
      </samigo:hideDivision>
     </h:panelGroup>
   </h:column>
  </h:dataTable>
</div>


<div class="tier2">
  <h:dataTable value="#{delivery.pageContents.partsContents}" var="part">
    <h:column>
      <f:verbatim><h4 class="tier1"></f:verbatim>
      <h:outputText value="#{deliveryMessages.p} #{part.number} #{deliveryMessages.of} #{part.numParts}" />
      <f:verbatim></h4><div class="tier1"></f:verbatim>
      <h:outputText value="#{part.text}" escape="false" rendered="#{part.numParts ne '1'}" />
      <f:verbatim></div></f:verbatim>
      <f:verbatim></h4><div class="tier2"></f:verbatim>
     <h:outputText value="#{evaluationMessages.no_questions}" escape="false" rendered="#{part.noQuestions}"/>
      <f:verbatim></div></f:verbatim>

      <h:dataTable value="#{part.itemContents}" columnClasses="tier2"
          var="question" border="0">
        <h:column>
          <h:outputText value="<a name=\"" escape="false" />
          <h:outputText value="#{part.number}_#{question.number}\" /></a>"
            escape="false" />
          <f:verbatim><h4 class="tier2"></f:verbatim>
            <h:outputText value="#{deliveryMessages.q} #{question.number} #{deliveryMessages.of} " />
            <h:outputText value="#{part.questions}#{deliveryMessages.column}  " />
            <h:inputText id="adjustedScore" value="#{question.pointsForEdit}" onchange="toPoint(this.id);" >
<f:validateDoubleRange/>
<%--SAK-3776    <f:convertNumber maxFractionDigits="2"/> --%>
            </h:inputText>
            <h:outputText value=" #{deliveryMessages.splash} #{question.maxPoints} " />
            <h:outputText value="#{deliveryMessages.pt}"/>
          <f:verbatim><br/></f:verbatim>
<h:message for="adjustedScore" style="color:red"/>
          <f:verbatim></h4></f:verbatim>

          <f:verbatim><div class="tier3"></f:verbatim>
            
            <h:panelGroup rendered="#{question.itemData.typeId == 7}">
              <f:subview id="deliverAudioRecording">
               <%@ include file="/jsf/evaluation/item/displayAudioRecording.jsp" %>
              </f:subview>
            </h:panelGroup>

            <h:panelGroup rendered="#{question.itemData.typeId == 6}">
              <f:subview id="deliverFileUpload">
                <%@ include file="/jsf/evaluation/item/displayFileUpload.jsp" %>
              </f:subview>
            </h:panelGroup>

            <h:panelGroup rendered="#{question.itemData.typeId == 11}">
            <f:subview id="deliverFillInNumeric">
            <%@ include file="/jsf/delivery/item/deliverFillInNumeric.jsp" %>
          </f:subview>
            </h:panelGroup>


            <h:panelGroup rendered="#{question.itemData.typeId == 8}">
              <f:subview id="deliverFillInTheBlank">
                <%@ include file="/jsf/delivery/item/deliverFillInTheBlank.jsp" %>
              </f:subview>
            </h:panelGroup>

            <h:panelGroup rendered="#{question.itemData.typeId == 9}">
              <f:subview id="deliverMatching">
                <%@ include file="/jsf/delivery/item/deliverMatching.jsp" %>
              </f:subview>
            </h:panelGroup>

            <h:panelGroup
              rendered="#{question.itemData.typeId == 1 || question.itemData.typeId == 12 || question.itemData.typeId == 3}">
              <f:subview id="deliverMultipleChoiceSingleCorrect">
                <%@ include file="/jsf/delivery/item/deliverMultipleChoiceSingleCorrect.jsp" %>
              </f:subview>
            </h:panelGroup>

            <h:panelGroup rendered="#{question.itemData.typeId == 2}">
              <f:subview id="deliverMultipleChoiceMultipleCorrect">
                <%@ include file="/jsf/delivery/item/deliverMultipleChoiceMultipleCorrect.jsp" %>
              </f:subview>
            </h:panelGroup>

            <h:panelGroup rendered="#{question.itemData.typeId == 5}">
              <f:subview id="deliverShortAnswer">
                <%@ include file="/jsf/delivery/item/deliverShortAnswerLink.jsp" %>
              </f:subview>
            </h:panelGroup>

            <h:panelGroup rendered="#{question.itemData.typeId == 4}">
              <f:subview id="deliverTrueFalse">
                <%@ include file="/jsf/delivery/item/deliverTrueFalse.jsp" %>
              </f:subview>
            </h:panelGroup>
          <f:verbatim></div></f:verbatim>

          <f:verbatim><div class="tier2"></f:verbatim>
          <h:panelGrid columns="2" border="0" >
            <h:outputText value="#{deliveryMessages.comment}#{deliveryMessages.column}"/>
            <h:inputTextarea value="#{question.gradingComment}" rows="3" cols="30"/>
            <h:outputText value=" "/>
            <%@ include file="/jsf/evaluation/gradeStudentResultAttachment.jsp" %>
          </h:panelGrid>
          <f:verbatim></div></f:verbatim>
        </h:column>
      </h:dataTable>
    </h:column>
  </h:dataTable>
</div>

<h:outputText value="#{author.updateFormTime}" />
<h:inputHidden value="#{author.currentFormTime}" />

<h:outputLink id="createEmail1" onclick="clickEmailLink(this, \"#{totalScores.graderName}\", '#{totalScores.graderEmailInfo}', \"#{studentScores.firstName} #{studentScores.lastName}\", '#{studentScores.email}', '#{totalScores.assessmentName}');" value="#"> 
  <h:outputText value="  #{evaluationMessages.email} #{studentScores.firstName}" rendered="#{totalScores.anonymous eq 'false' && studentScores.email != null && studentScores.email != '' && email.fromEmailAddress != null && email.fromEmailAddress != ''}" />
</h:outputLink>

<h:commandLink id="hiddenlink1" value="" action="studentScores">
  <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.util.EmailListener" />
  <f:param name="toUserId" value="#{studentScores.studentId}" />
</h:commandLink>

<p class="act">
   <h:commandButton accesskey="#{evaluationMessages.a_save}" styleClass="active" value="#{evaluationMessages.save_cont}" action="sectionActivity" type="submit">
      <f:actionListener
         type="org.sakaiproject.tool.assessment.ui.listener.evaluation.StudentScoreUpdateListener" />
      <f:actionListener
         type="org.sakaiproject.tool.assessment.ui.listener.evaluation.StudentScoreListener" />
      <f:actionListener
         type="org.sakaiproject.tool.assessment.ui.listener.evaluation.ResetTotalScoreListener" />
      <f:actionListener
         type="org.sakaiproject.tool.assessment.ui.listener.evaluation.TotalScoreListener" />
      <f:actionListener
         type="org.sakaiproject.tool.assessment.ui.listener.author.SectionActivityListener" />
   </h:commandButton>
   <h:commandButton accesskey="#{evaluationMessages.a_cancel}" value="#{evaluationMessages.cancel}" action="sectionActivity" immediate="true">
      <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.evaluation.ResetTotalScoreListener" />
      <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.evaluation.TotalScoreListener" />
   </h:commandButton>
</p>
</h:form>
</div>
  <!-- end content -->
      </body>
    </html>
  </f:view>