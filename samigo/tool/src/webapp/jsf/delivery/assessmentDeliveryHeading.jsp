<%-- $Id:
Headings for delivery pages, needs to have msg=DeliveryMessages.properties, etc.
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

<%-- TITLE --%>
<p>
<h3 style="insColor insBak">
   <h:outputText value="#{delivery.assessmentTitle}" />
</h3>
</p>
<%-- NAV BAR --%>
<p class="navIntraTool">
  <h:panelGroup rendered="#{(delivery.feedbackComponent.showImmediate || delivery.feedbackOnDate) 
                         && (delivery.actionString=='previewAssessment'
                             || delivery.actionString=='takeAssessment'
                             || delivery.actionString=='takeAssessmentViaUrl')}">

<!-- SHOW FEEDBACK LINK FOR TAKE ASSESSMENT AND TAKE ASSESSMENT VIA URL -->
    <h:commandLink title="#{msg.t_feedback}" action="#{delivery.getOutcome}" onmouseup="saveTime();" 
       rendered="#{delivery.actionString=='takeAssessment'
                || delivery.actionString=='takeAssessmentViaUrl'}" >
     <h:outputText value="#{msg.show_feedback}" />
     <f:param name="showfeedbacknow" value="true" />
     <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.delivery.ShowFeedbackActionListener" />
    </h:commandLink>

<!-- SHOW FEEDBACK LINK FOR PREVIEW ASSESSMENT -->
    <h:commandLink title="#{msg.t_feedback}" action="takeAssessment" onmouseup="saveTime();" 
       rendered="#{delivery.actionString=='previewAssessment'}" >
     <h:outputText value="#{msg.show_feedback}" />
     <f:param name="showfeedbacknow" value="true" />
     <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.delivery.DeliveryActionListener" />
    </h:commandLink>

    <h:outputText value=" #{msg.separator} "
      rendered="#{(delivery.actionString=='previewAssessment'
                   || delivery.actionString=='takeAssessment'
                   || delivery.actionString=='takeAssessmentViaUrl')
                && delivery.navigation ne '1'}"/>
  </h:panelGroup >


<!-- TABLE OF CONTENT LINK FOR TAKE ASSESSMENT AND TAKE ASSESSMENT VIA URL -->
  <h:commandLink title="#{msg.t_tableOfContents}" action="#{delivery.getOutcome}" onmouseup="saveTime();"
     rendered="#{(delivery.actionString=='takeAssessment'
                   || delivery.actionString=='takeAssessmentViaUrl')
               && delivery.navigation ne '1'}">
    <h:outputText value="#{msg.table_of_contents}" />
    <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.delivery.TableOfContentsActionListener" />
  </h:commandLink>


<!-- TABLE OF CONTENT LINK FOR PREVIEW ASSESSMENT -->
 <h:commandLink title="#{msg.t_tableOfContents}" action="tableOfContents" onmouseup="saveTime();"
    rendered="#{delivery.actionString=='previewAssessment'
             && delivery.navigation ne '1'}">
    <h:outputText value="#{msg.table_of_contents}" />
    <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.delivery.DeliveryActionListener" />
  </h:commandLink>


<!-- RETURN TO ASSESSMENT PAGE LINK FOR REVIEW ASSESSMENT -->
  <h:commandLink action="select" title="#{msg.t_returnAssessmentList}"
     rendered="#{delivery.actionString=='reviewAssessment'}">
    <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.select.SelectActionListener" />
    <h:outputText value="#{msg.button_return_select}" />
  </h:commandLink>
</p>

<p>


<!-- GRADER COMMENT FOR REVIEW ASSESSMENT -->
<h:outputText rendered="#{delivery.feedbackComponent.showGraderComment 
                       && delivery.actionString=='reviewAssessment'
                       && delivery.graderComment ne ''}" 
   value="<b>#{msg.comments}</b> #{delivery.graderComment}" escape="false" />



<!-- BEGIN OF TIMER -->
<h:panelGroup rendered="#{(delivery.actionString=='previewAssessment'
                           || delivery.actionString=='takeAssessment'
                           || delivery.actionString=='takeAssessmentViaUrl')
                        && delivery.hasTimeLimit}" >
<f:verbatim><span id="remText"></f:verbatim><h:outputText value="#{msg.time_remaining} "/><f:verbatim></span></f:verbatim>
<f:verbatim><span id="timer"></f:verbatim><f:verbatim> </span></f:verbatim>

<f:verbatim> <span id="bar"></f:verbatim>
<h:panelGroup rendered="#{delivery.timeElapseAfterFileUpload == null || delivery.timeElapseFloat ge delivery.timeElapseAfterFileUploadFloat}">
<samigo:timerBar height="15" width="300"
 
    wait="#{delivery.timeLimit}"
    elapsed="#{delivery.timeElapse}"
    expireMessage="Your session has expired."
    expireScript="document.forms[0].elements['takeAssessmentForm:assessmentDeliveryHeading:elapsed'].value=loaded; document.forms[0].elements['takeAssessmentForm:assessmentDeliveryHeading:outoftime'].value='true'; clickSubmitForGrade();" />
</h:panelGroup>

<h:panelGroup rendered="#{delivery.timeElapseAfterFileUpload != null && delivery.timeElapseFloat lt delivery.timeElapseAfterFileUploadFloat}">
<samigo:timerBar height="15" width="300"
    wait="#{delivery.timeLimit}"
    elapsed="#{delivery.timeElapseAfterFileUpload}"
    expireMessage="Your session has expired."
    expireScript="document.forms[0].elements['takeAssessmentForm:assessmentDeliveryHeading:elapsed'].value=loaded; document.forms[0].elements['takeAssessmentForm:assessmentDeliveryHeading:outoftime'].value='true'; clickSubmitForGrade();" />
</h:panelGroup>

<f:verbatim>  </span></f:verbatim>

<!-- HIDE / SHOW TIMER BAR -->
<h:commandButton type="button" onclick="document.getElementById('remText').style.display=document.getElementById('remText').style.display=='none' ? '': 'none';document.getElementById('timer').style.display=document.getElementById('timer').style.display=='none' ? '': 'none';document.getElementById('bar').style.display=document.getElementById('bar').style.display=='none' ? '': 'none'" value="Hide/Show Time Remaining" />
<!-- END OF TIMER -->

<h:inputHidden id="elapsed" value="#{delivery.timeElapse}" />
<h:inputHidden id="outoftime" value="#{delivery.timeOutSubmission}"/>

<h:commandLink title="#{msg.t_submit}" id="submitforgrade" action="#{delivery.submitForGrade}" value="" />

<script language="javascript" style="text/JavaScript">
<!--
function clickSubmitForGrade(){
  var newindex = 0;
  for (i=0; i<document.links.length; i++) {
    if(document.links[i].id == "takeAssessmentForm:assessmentDeliveryHeading:submitforgrade")
    {
      newindex = i;
      break;
    }
  }
  document.links[newindex].onclick();
}
//-->
</script>
</h:panelGroup>
</p>
