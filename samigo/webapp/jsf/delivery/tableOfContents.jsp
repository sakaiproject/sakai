<%@ page contentType="text/html;charset=utf-8" pageEncoding="utf-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://www.sakaiproject.org/samigo" prefix="samigo" %>
<!-- $Id: tableOfContents.jsp,v 1.36 2005/06/10 23:59:01 zqingru.stanford.edu Exp $ -->
<f:view>
  <f:verbatim><!DOCTYPE html
   PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
   "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
  </f:verbatim>
  <f:loadBundle
   basename="org.sakaiproject.tool.assessment.bundle.DeliveryMessages"
   var="msg"/>
  <html xmlns="http://www.w3.org/1999/xhtml">
    <head><%= request.getAttribute("html.head") %>
    <title><h:outputText value="#{msg.table_of_contents}" /></title>
    <samigo:stylesheet path="/css/samigo.css"/>
    <samigo:stylesheet path="/css/sam.css"/>
    <samigo:script path="/jsf/widget/hideDivision/hideDivision.js" />
    </head>
    <body onload="hideUnhideAllDivs('none');;<%= request.getAttribute("html.body.onload") %>">
<!-- content... -->
<script language="javascript">
function checkRadio()
{
  for (i=0; i<document.forms[0].elements.length; i++)
  {
    if (document.forms[0].elements[i].type == "radio")
    {
      if (document.forms[0].elements[i].defaultChecked == true)
      {
        document.forms[0].elements[i].click();
      }
    }
  }

}

function noenter(){
return!(window.event && window.event.keyCode == 13);
}

function saveTime()
{
  if((typeof (document.forms[0].elements['takeAssessmentForm:assessmentDeliveryHeading:elapsed'])!=undefined) && ((document.forms[0].elements['takeAssessmentForm:assessmentDeliveryHeading:elapsed'])!=null) ){
  pauseTiming = 'true';
  document.forms[0].elements['takeAssessmentForm:assessmentDeliveryHeading:elapsed'].value=loaded;
 }
}

</script>

<%-- <h:form onsubmit="saveTime()"> --%>

<h:panelGroup rendered="#{delivery.previewAssessment eq 'true' && delivery.notPublished ne 'true'}">
 <f:verbatim><div class="validation"></f:verbatim>
     <h:outputText value="#{msg.ass_preview}" />
     <h:commandButton value="#{msg.done}" action="editAssessment" type="submit"/>
 <f:verbatim></div></f:verbatim>
</h:panelGroup>

<h:panelGroup rendered="#{delivery.previewAssessment eq 'true' && delivery.notPublished eq 'true'}">
 <f:verbatim><div class="validation"></f:verbatim>
     <h:outputText value="#{msg.ass_preview}" />
     <h:commandButton value="#{msg.done}" action="editAssessment">
       <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.RemovePublishedAssessmentListener" />
     </h:commandButton>
 <f:verbatim></div></f:verbatim>
</h:panelGroup>
<%-- </h:form>  --%>

<h3><h:outputText value="#{delivery.assessmentTitle} " /></h3>

<h:panelGroup rendered="#{delivery.previewMode eq 'false' && delivery.hasTimeLimit}" >
<f:verbatim><span id="remText"></f:verbatim><h:outputText value="Time Remaining: "/><f:verbatim></span></f:verbatim>
<f:verbatim><span id="timer"></f:verbatim><f:verbatim> </span></f:verbatim>

<f:verbatim> <span id="bar"></f:verbatim>
<samigo:timerBar height="15" width="300"
    wait="#{delivery.timeLimit}"
    elapsed="#{delivery.timeElapse}"
    expireMessage="Your session has expired."
    expireScript="document.forms[0].elements['takeAssessmentForm:assessmentDeliveryHeading:elapsed'].value=loaded; document.forms[0].elements['takeAssessmentForm:assessmentDeliveryHeading:outoftime'].value='true'; document.forms[0].elements['takeAssessmentForm:saveAndExit'].click();" />

<f:verbatim>  </span></f:verbatim>
<h:commandButton type="button" onclick="document.getElementById('remText').style.display=document.getElementById('remText').style.display=='none' ? '': 'none';document.getElementById('timer').style.display=document.getElementById('timer').style.display=='none' ? '': 'none';document.getElementById('bar').style.display=document.getElementById('bar').style.display=='none' ? '': 'none'" value="Hide/Show Time Remaining" />
<h:inputHidden id="elapsed" value="#{delivery.timeElapse}" />
<h:inputHidden id="outoftime" value="#{delivery.timeOutSubmission}"/>
</h:panelGroup>
<h:inputHidden id="assessmentID" value="#{delivery.assessmentId}"/>
<h:inputHidden id="assessTitle" value="#{delivery.assessmentTitle}" />

<div class="indnt1"><h4>
   <h:outputText value="#{msg.table_of_contents} " />
  <h:outputText styleClass="tier10" value="#{msg.tot_score} " />
 
  <h:outputText value="#{delivery.tableOfContents.maxScore} " />
  <h:outputText value="#{msg.pt}" />
</h4>
<!-- h4 class="tier1">
  <h:outputText value="#{msg.table_of_contents} " />
  <h:outputText styleClass="tier10" value="#{msg.tot_score} " />
  <h:outputText value="#{delivery.tableOfContents.maxScore} " />
  <h:outputText value="#{msg.pt}" />
</h4-->

<div class="indnt2">
<h5 class="plain">
  <h:outputLabel value="#{msg.key}"/>
 </h5>

  <h:graphicImage
   alt="#{msg.unans_q}"
   url="/images/tree/blank.gif" />
  <h:outputText value="#{msg.unans_q}" /><br>
  <h:graphicImage
   alt="#{msg.q_marked}"
   url="/images/tree/marked.gif" />
  <h:outputText value="#{msg.q_marked}" />

<h:form id="tableOfContentsForm" onsubmit="saveTime()">

<font color="red"><h:messages/></font>

  <h:dataTable value="#{delivery.tableOfContents.partsContents}" var="part">
  <h:column>  

 <samigo:hideDivision id="hidePartDiv" title = "#{msg.p} #{part.number} - #{part.nonDefaultText}  -
       #{part.questions-part.unansweredQuestions}/#{part.questions} #{msg.ans_q}, #{part.points}/#{part.maxPoints} #{msg.pt}" >
  
      <h:dataTable value="#{part.itemContents}" var="question">
      <h:column>
   <f:verbatim><div class="indnt3"></f:verbatim>
        <h:panelGroup>
          <h:graphicImage
            alt="#{msg.unans_q}"
            url="/images/tree/blank.gif" rendered="#{question.unanswered}"/>
          <h:graphicImage
            alt="#{msg.q_marked}"
            url="/images/tree/marked.gif"  rendered="#{question.review}"/>
        
            <h:commandLink immediate="true" action="takeAssessment" onmouseup="saveTime();">
            <h:outputText value="#{question.sequence}. #{question.strippedText} (#{question.points}/#{question.maxPoints} #{msg.pt}) " escape="false" />
            <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.delivery.DeliveryActionListener" />
            <f:param name="partnumber" value="#{part.number}" />
            <f:param name="questionnumber" value="#{question.number}" />
          </h:commandLink>

        </h:panelGroup>
<f:verbatim></div></f:verbatim>
       </h:column>
      </h:dataTable>
    </samigo:hideDivision>
<f:verbatim></div></f:verbatim>
  </h:column> 
  </h:dataTable>
</div></div>
<p class="act">
  <h:commandButton type="submit" value="#{msg.button_submit_grading}"
    action="#{delivery.submitForGrade}" styleClass="active"  disabled="#{delivery.previewAssessment eq 'true'}">
  </h:commandButton>

  <h:commandButton type="submit" value="#{msg.button_save_x}" 
    action="#{delivery.saveAndExit}" 
    rendered="#{!delivery.accessViaUrl}" disabled="#{delivery.previewAssessment eq 'true'}">
  </h:commandButton>

  <h:commandButton type="submit" value="#{msg.button_quit}"
    action="#{delivery.saveAndExit}" id="quit"
    rendered="#{delivery.accessViaUrl}" disabled="#{delivery.previewAssessment eq 'true'}">
  </h:commandButton>
</p>

<h:panelGroup rendered="#{delivery.previewAssessment eq 'true' && delivery.notPublished ne 'true'}">
 <f:verbatim><div class="validation"></f:verbatim>
     <h:outputText value="#{msg.ass_preview}" />
     <h:commandButton value="#{msg.done}" action="editAssessment" type="submit"/>
 <f:verbatim></div></f:verbatim>
</h:panelGroup>

<h:panelGroup rendered="#{delivery.previewAssessment eq 'true' && delivery.notPublished eq 'true'}">
 <f:verbatim><div class="validation"></f:verbatim>
     <h:outputText value="#{msg.ass_preview}" />
     <h:commandButton value="#{msg.done}" action="editAssessment">
       <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.RemovePublishedAssessmentListener" />
     </h:commandButton>
 <f:verbatim></div></f:verbatim>
</h:panelGroup>

</h:form>
<!-- end content -->
    </body>
  </html>
</f:view>

