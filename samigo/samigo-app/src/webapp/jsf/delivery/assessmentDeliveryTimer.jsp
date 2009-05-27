<%-- $Id:
Headings for delivery pages, needs to have msg=DeliveryMessages.properties, etc.
--%>
<!--
<%--
***********************************************************************************
*
* Copyright (c) 2006 The Sakai Foundation.
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
--%>
-->

<!-- BEGIN OF TIMER -->
<h:panelGroup rendered="#{(delivery.actionString=='takeAssessment'
                           || delivery.actionString=='takeAssessmentViaUrl')
                        && delivery.hasTimeLimit}" >
<f:verbatim><span id="remText"></f:verbatim><h:outputText value="#{deliveryMessages.time_remaining} "/><f:verbatim></span></f:verbatim>
<f:verbatim><span id="timer"></f:verbatim><f:verbatim> </span></f:verbatim>

<f:verbatim> <span id="bar"></f:verbatim>
<h:panelGroup rendered="#{delivery.timeElapseAfterFileUpload == null || delivery.timeElapseFloat ge delivery.timeElapseAfterFileUploadFloat}">
<samigo:timerBar height="15" width="300"
    wait="#{delivery.timeLimit}"
    elapsed="#{delivery.timeElapse}"
    expireMessage1="#{deliveryMessages.time_expired1}"
	expireMessage2="#{deliveryMessages.time_expired2}"
	fiveMinutesMessage1="#{deliveryMessages.five_minutes_left1}"
	fiveMinutesMessage2="#{deliveryMessages.five_minutes_left2}"
    expireScript="document.forms[0].elements['takeAssessmentForm:assessmentDeliveryHeading:elapsed'].value=loaded; document.forms[0].elements['takeAssessmentForm:assessmentDeliveryHeading:outoftime'].value='true'; clickSubmitForGrade();" />
</h:panelGroup>

<h:panelGroup rendered="#{delivery.timeElapseAfterFileUpload != null && delivery.timeElapseFloat lt delivery.timeElapseAfterFileUploadFloat}">
<samigo:timerBar height="15" width="300"
    wait="#{delivery.timeLimit}"
    elapsed="#{delivery.timeElapseAfterFileUpload}"
    expireMessage1="#{deliveryMessages.time_expired1}"
	expireMessage2="#{deliveryMessages.time_expired2}"
	fiveMinutesMessage1="#{deliveryMessages.five_minutes_left1}"
	fiveMinutesMessage2="#{deliveryMessages.five_minutes_left2}"
	expireScript="document.forms[0].elements['takeAssessmentForm:assessmentDeliveryHeading:elapsed'].value=loaded; document.forms[0].elements['takeAssessmentForm:assessmentDeliveryHeading:outoftime'].value='true'; clickSubmitForGrade();" />
</h:panelGroup>

<f:verbatim>  </span></f:verbatim>

<!-- HIDE / SHOW TIMER BAR -->
<h:commandButton type="button" onclick="document.getElementById('remText').style.display=document.getElementById('remText').style.display=='none' ? '': 'none';document.getElementById('timer').style.display=document.getElementById('timer').style.display=='none' ? '': 'none';document.getElementById('bar').style.display=document.getElementById('bar').style.display=='none' ? '': 'none'"
onkeypress="document.getElementById('remText').style.display=document.getElementById('remText').style.display=='none' ? '': 'none';document.getElementById('timer').style.display=document.getElementById('timer').style.display=='none' ? '': 'none';document.getElementById('bar').style.display=document.getElementById('bar').style.display=='none' ? '': 'none'"
 value="#{deliveryMessages.hide_show}" />
<!-- END OF TIMER -->

<h:inputHidden id="elapsed" value="#{delivery.timeElapse}" />
<h:inputHidden id="outoftime" value="#{delivery.timeOutSubmission}"/>

<h:commandLink title="#{deliveryMessages.t_submit}" id="submitforgrade" action="#{delivery.submitForGrade}" value="" />

<script language="javascript" type="text/JavaScript">
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

<h:panelGroup rendered="#{delivery.actionString=='previewAssessment' && delivery.hasTimeLimit}" >
	<h:graphicImage height="60" width="300" url="/images/delivery/TimerPreview.png"/>
</h:panelGroup>
</p>
