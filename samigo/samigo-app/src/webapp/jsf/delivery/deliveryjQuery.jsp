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

<script type="text/javascript" src="/library/webjars/jquery-blockui/2.65/jquery.blockUI.js"></script>

<script type="text/javascript">
     var honorPledgeIsChecked = true;
     var button_ok = "<h:outputText value="#{deliveryMessages.button_ok} "/>";
     var please_wait = "<h:outputText value="#{deliveryMessages.please_wait} "/>";

     var time_30_warning = "<h:outputText value="#{deliveryMessages.time_30_warning} "/><h:outputText value="#{deliveryMessages.time_30_warning_2} " />";
     var time_due_warning = "<h:outputText value="#{deliveryMessages.time_due_warning_1} "/><h:outputText value="#{deliveryMessages.time_due_warning_2} " />";     
     
     $(document).ready(function(){

                var timerSave = false;
	
		//Turn off browser autocomplete on all forms
		$("form").attr("autocomplete", "off");

		// If instructor requires honor pledge, we check for it before allowing assessment to start
		if($('#takeAssessmentForm\\:honor_pledge').length > 0) {
			honorPledgeIsChecked = false;

			$('#takeAssessmentForm\\:honor_pledge').change(
				function() { honorPledgeIsChecked = $('#takeAssessmentForm\\:honor_pledge').prop('checked'); }
			);
		}

		// Block the UI to avoid user double-clicks
		$("input[type='submit'][class!='noActionButton']").click(function() { 
			if (!honorPledgeIsChecked) return false;
                        if (!timerSave) $.blockUI({ message: '<h3>' + please_wait + ' <img src="/library/image/sakai/spinner.gif" /></h3>', overlayCSS: { backgroundColor: '#ccc', opacity: 0.25} });
		}); 
		//Disable the back button
		disableBackButton("<h:outputText value="#{deliveryMessages.use_form_navigation}"/>");

		if($('#submittedForm\\:renderTimeoutMessage').length > 0){
			showTimerExpiredWarning(function() { ($('#timer-expired-warning').parent()).css('display', 'none');});
		}
	});

        function checkIfHonorPledgeIsChecked() {
          if(!honorPledgeIsChecked){
            alert("<h:outputText value='#{deliveryMessages.honor_pledge_select}'/>");
            $('#takeAssessmentForm\\:honorPledgeRequired').show();
            return false;
          }
          return true;
        }

</script>
