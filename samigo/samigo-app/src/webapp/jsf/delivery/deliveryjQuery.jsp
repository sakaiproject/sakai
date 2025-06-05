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
	var portal = portal || { locale: "<h:outputText value="#{delivery.localeString}"/>" };
	portal.locale = portal.locale || "<h:outputText value="#{delivery.localeString}"/>";
	var honorPledgeIsChecked = true;
	var understandPledgeIsChecked = true;
	var button_ok = "<h:outputText value="#{deliveryMessages.button_ok} "/>";
	var please_wait = "<h:outputText value="#{deliveryMessages.please_wait} "/>";
	var beginButtonValue = "<h:outputText value='#{deliveryMessages.begin_assessment_}' />";
	var continueButtonValue = "<h:outputText value='#{deliveryMessages.continue_assessment_}' />";
	var selector = "input[value='" + beginButtonValue + "']";

	function enableDisableSubmitButton() {
		var honourPledgeRequired = $('#takeAssessmentForm\\:honor_pledge').length > 0;
		var understandPledgeRequired = $("#takeAssessmentForm\\:understand_pledge").length > 0;

		if (honourPledgeRequired) {
			honorPledgeIsChecked = $('#takeAssessmentForm\\:honor_pledge').prop('checked');
		}
		if (understandPledgeRequired) {
			understandPledgeIsChecked = $('#takeAssessmentForm\\:understand_pledge').prop('checked');
		}

		var enable = false;
		if ((honourPledgeRequired && understandPledgeRequired && honorPledgeIsChecked && understandPledgeIsChecked) // both are required, both are checked
				|| (honourPledgeRequired && honorPledgeIsChecked && !understandPledgeRequired)              // honour pledge only required and checked
				|| (!honourPledgeRequired && understandPledgeRequired && understandPledgeIsChecked)) {      // understand (resubmit consequences) pledge only required and checked
			enable = true;
		}

		if (enable) {
			$(selector).addClass('active');
			$(selector).removeAttr('disabled');
		} else {
			$(selector).removeClass('active');
			$(selector).attr('disabled','disabled');
		}
	}

	$(document).ready(function(){

		var timerSave = false;

		// If there's no "begin" button, we need to grab the "continue" button
		if ($(selector).length <= 0) {
			selector = "input[value='" + continueButtonValue + "']";
		}
	
		//Turn off browser autocomplete on all forms
		$("form").attr("autocomplete", "off");

		var selector = $('#takeAssessmentForm\\:beginAssessment1');

		// If instructor requires honor pledge, we check for it before allowing assessment to start
		if($('#takeAssessmentForm\\:honor_pledge').length > 0) {
			honorPledgeIsChecked = false;
			$('#takeAssessmentForm\\:honor_pledge').change(enableDisableSubmitButton);
		}

		// If this student requires an understand (resubmit consequences) pledge, we check for it before allowing assessment to start
		if ($("#takeAssessmentForm\\:understand_pledge").length > 0) {
			understandPledgeIsChecked = false;
			$("#takeAssessmentForm\\:understand_pledge").change(enableDisableSubmitButton);
		}

		// Check honor code checkbox, understand (resubmit consequences) pledge checkbox, lock the UI to avoid user double-clicks
		$("input.active[type='submit'][class!='noActionButton']").click(function() {
			var beginButtonIds = ["takeAssessmentForm:beginAssessment1", "takeAssessmentForm:beginAssessment2", "takeAssessmentForm:beginAssessment3", "takeAssessmentForm:continueAssessment1", "takeAssessmentForm:continueAssessment2"];
			if (beginButtonIds.includes($(this).attr('id'))) {
				var invalid = false;
				if (!honorPledgeIsChecked) {
					$('#takeAssessmentForm\\:honorPledgeRequired').show();
					invalid = true;
				} else {
					$('#takeAssessmentForm\\:honorPledgeRequired').hide();
				}
				if (!understandPledgeIsChecked) {
					$('#takeAssessmentForm\\:understandPledgeRequired').show();
					invalid = true;
				} else {
					$('#takeAssessmentForm\\:understandPledgeRequired').hide();
				}

				// If any validation failed, do not continue
				if (invalid) {
					return false;
				}
			}
			if (!timerSave) $.blockUI({ message: '<h3>' + please_wait + ' <img src="/library/image/sakai/spinner.gif" /></h3>', overlayCSS: { backgroundColor: '#ccc', opacity: 0.25} });
		});

		// If there's an honour or understand pledge present, disable the begin/continue button by default
		if($('#takeAssessmentForm\\:honor_pledge').length > 0 || $("#takeAssessmentForm\\:understand_pledge").length > 0) {
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
