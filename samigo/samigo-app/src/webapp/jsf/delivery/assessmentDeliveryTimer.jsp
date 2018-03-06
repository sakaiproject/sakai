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
<!-- 
<h:outputText value="hasTimeLimit=#{delivery.hasTimeLimit}, turnIntoTimedAssessment=#{delivery.turnIntoTimedAssessment}, time_remaining=#{delivery.timeElapse}"/>
-->
<h:panelGroup rendered="#{(delivery.actionString=='takeAssessment'
                           || delivery.actionString=='takeAssessmentViaUrl')
                        && (delivery.hasTimeLimit || delivery.turnIntoTimedAssessment)}" >
                        
<h:panelGroup rendered="#{delivery.hasTimeLimit}">                        
<f:verbatim><span id="remText"></f:verbatim><h:outputText value="#{deliveryMessages.time_remaining} "/><f:verbatim></span></f:verbatim>
<f:verbatim><span id="timer"></f:verbatim><f:verbatim> </span></f:verbatim>
</h:panelGroup>

<f:verbatim> <span id="bar"></f:verbatim>
<h:panelGroup rendered="#{delivery.timeElapseAfterFileUpload == null || delivery.timeElapseDouble ge delivery.timeElapseAfterFileUploadDouble}">
<samigo:timerBar height="15" width="300"
    wait="#{delivery.timeLimit}"
    elapsed="#{delivery.timeElapse}"
    timeUpMessage="#{deliveryMessages.time_up}"
    expireScript="document.forms[0].elements['takeAssessmentForm:assessmentDeliveryHeading:elapsed'].value=loaded; document.forms[0].elements['takeAssessmentForm:assessmentDeliveryHeading:outoftime'].value='true'; " />
</h:panelGroup>

<h:panelGroup rendered="#{delivery.timeElapseAfterFileUpload != null && delivery.timeElapseDouble lt delivery.timeElapseAfterFileUploadDouble}">
<samigo:timerBar height="15" width="300"
    wait="#{delivery.timeLimit}"
    elapsed="#{delivery.timeElapseAfterFileUpload}"
    timeUpMessage="#{deliveryMessages.time_up}"
    expireScript="document.forms[0].elements['takeAssessmentForm:assessmentDeliveryHeading:elapsed'].value=loaded; document.forms[0].elements['takeAssessmentForm:assessmentDeliveryHeading:outoftime'].value='true'; " />
</h:panelGroup>

<f:verbatim>  </span></f:verbatim>

<!-- HIDE / SHOW TIMER BAR -->
<h:panelGroup rendered="#{delivery.hasTimeLimit}">
<h:commandButton type="button" onclick="document.getElementById('remText').style.display=document.getElementById('remText').style.display=='none' ? '': 'none';document.getElementById('timer').style.display=document.getElementById('timer').style.display=='none' ? '': 'none';document.getElementById('bar').style.display=document.getElementById('bar').style.display=='none' ? '': 'none';return false;"
 value="#{deliveryMessages.hide_show}" styleClass="noActionButton" />
 </h:panelGroup>
<!-- END OF TIMER -->

<h:inputHidden id="elapsed" value="#{delivery.timeElapse}" />
<h:inputHidden id="outoftime" value="#{delivery.timeOutSubmission}"/>

<h:commandLink title="#{deliveryMessages.t_submit}" id="submitforgrade" action="#{delivery.submitForGradeFromTimer}" value="" />
<h:commandLink id="saveNoCheck" action="#{delivery.saveNoCheck}" value="" />
<h:commandLink id="submitNoCheck" action="#{delivery.submitFromTimeoutPopup}" value="" />

<script type="text/JavaScript">
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

function clickSubmit(){
	var newindex = 0;
	for (i=0; i<document.links.length; i++) {
		if(document.links[i].id == "takeAssessmentForm:assessmentDeliveryHeading:submitNoCheck")
		{
			newindex = i;
			break;
		}
	}
	document.links[newindex].onclick();
}

function clickDoNotSubmit(){
	var newindex = 0;
	for (i=0; i<document.links.length; i++) {
		if(document.links[i].id == "takeAssessmentForm:assessmentDeliveryHeading:saveNoCheck")
		{
			newindex = i;
			break;
		}
	}
	document.links[newindex].onclick();
}
</script>
</h:panelGroup>

<h:panelGroup rendered="#{delivery.actionString=='previewAssessment' && delivery.hasTimeLimit}" >
  <f:verbatim><div style="margin:10px 0px 0px 0px;"><span style="background-color:#bab5b5; padding:5px"></f:verbatim>
  <h:outputText value="#{deliveryMessages.timer_preview_not_available}"/>
  <f:verbatim></div></span></f:verbatim>
</h:panelGroup>
</p>
