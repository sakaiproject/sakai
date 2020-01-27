<!--
<%--
***********************************************************************************
*
* Copyright (c) 2011 The Sakai Foundation.
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

<script src="/library/webjars/jquery-blockui/2.65/jquery.blockUI.js"></script>

<script>
     var honorPledgeIsChecked = true;
     var scoringType = <h:outputText value="#{delivery.scoringType}"/>;
     var autoSubmit = <h:outputText value="#{delivery.settings.autoSubmit}"/>;
     var totalSubmissions = <h:outputText value="#{delivery.totalSubmissions}"/>;
     var button_ok = "<h:outputText value="#{deliveryMessages.button_ok} "/>";
     var please_wait = "<h:outputText value="#{deliveryMessages.please_wait} "/>";
     var submitButtonValue = "<h:outputText value='#{deliveryMessages.begin_assessment_}' />";
     var selector = "input[value='" + submitButtonValue + "']";
     var newAttemptAutoSubmitWarning = "<h:outputText value="#{deliveryMessages.begin_assessment_msg_attempt_autosubmit_warn_average}" rendered="#{delivery.scoringType == 4}" /><h:outputText value="#{deliveryMessages.begin_assessment_msg_attempt_autosubmit_warn_last} " rendered="#{delivery.scoringType == 2}" />";

     $(document).ready(function(){

                var timerSave = false;
	
		//Turn off browser autocomplete on all forms
		$("form").attr("autocomplete", "off");

		// If instructor requires honor pledge, we check for it before allowing assessment to start
		if($('#takeAssessmentForm\\:honor_pledge').length > 0) {
			honorPledgeIsChecked = false;

			$('#takeAssessmentForm\\:honor_pledge').change(function() {
					honorPledgeIsChecked = $('#takeAssessmentForm\\:honor_pledge').prop('checked');
					if(honorPledgeIsChecked) {
						$(selector).addClass('active');
						$(selector).removeAttr('disabled');
					} else {
						$(selector).removeClass('active');
						$(selector).attr('disabled','disabled');
					}
				}
			);
		}

		// Check honor code checkbox, warn about autosubmitting empty attempts, lock the UI to avoid user double-clicks
		$("input.active[type='submit'][class!='noActionButton']").click(function() {
			var beginButtonIds = ["takeAssessmentForm:beginAssessment1", "takeAssessmentForm:beginAssessment2", "takeAssessmentForm:beginAssessment3"];
			if (beginButtonIds.includes($(this).attr('id'))) {
				if (!honorPledgeIsChecked) {
					alert("<h:outputText value='#{deliveryMessages.honor_pledge_select}'/>");
					$('#takeAssessmentForm\\:honorPledgeRequired').show();
					return false;
				}
				if (totalSubmissions > 0 && autoSubmit && (scoringType === 2 || scoringType === 4)) {
					if (!confirm(newAttemptAutoSubmitWarning)) {
						return false;
					}
				}
			}
			if (!timerSave) $.blockUI({ message: '<h3>' + please_wait + ' <img src="/library/image/sakai/spinner.gif" /></h3>', overlayCSS: { backgroundColor: '#ccc', opacity: 0.25} });
		});

		if($('#takeAssessmentForm\\:honor_pledge').length > 0) {
			$(selector).removeClass('active');
			$(selector).attr('disabled','disabled');
		}

		//Disable the back button
		disableBackButton("<h:outputText value="#{deliveryMessages.use_form_navigation}"/>");

		if($('#submittedForm\\:renderTimeoutMessage').length > 0){
			showTimerExpiredWarning(function() { ($('#timer-expired-warning').parent()).css('display', 'none');});
		}
	});

</script>
