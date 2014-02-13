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

<script type="text/javascript" src="/library/js/jquery/jquery-1.9.1.min.js"></script>
<script type="text/javascript" src="/library/js/jquery/ui/1.10.3/jquery-ui.1.10.3.full.min.js"></script>
<script type="text/javascript" src="/library/js/jquery/blockUI/2.66/jquery.blockUI.js"></script>

<script type="text/javascript">
     $(document).ready(function(){
	
		$("input[type='submit']").click(function() { 
			$.blockUI({ message: '', overlayCSS: { backgroundColor: '#ff0', opacity: 0} }); 
			setTimeout($.unblockUI, 2000); 
		}); 
			
		$('#timer-warning').dialog({
			autoOpen: false,
			width: 400,
			modal: true,
			resizable: false,
			draggable: false
		});
		
		$('#timer-expired-warning').dialog({
			autoOpen: false,
			width: 400,
			modal: true,
			resizable: false,
			draggable: false,
			closeOnEscape: false,
			open: function (event,ui) { 
				$(".ui-dialog-titlebar", $(this).parent()).hide(); 
				$(this).css("background", "#EEEEEE");
			}
		});		
		
		$('#time-due-warning').dialog({
			autoOpen: false,
			width: 330,
			modal: true,
			resizable: false,
			draggable: false,
			closeOnEscape: false,
	        open: function (event,ui) { 
	        	$(".ui-dialog-titlebar", $(this).parent()).hide(); 
				$(this).css("background", "#EEEEEE");
			}
		});		
	});

	function showTimerWarning() {
		$('#timer-warning').dialog('open');
		return false;
	}		
	
	function showTimerExpiredWarning(submitfunction) {
		$('#timer-expired-warning').dialog('open');
		setTimeout(submitfunction,5000);
		return false;
	}
	
	function showTimeDueWarning() {
		$('#time-due-warning').dialog('open');
		return false;
	}
	
	function show30MinWarning() {
		$('#time-30-warning').dialog({
			modal: true,
			resizable: false,
			draggable: false,
			width: 250,
			open: function (event,ui) { 
				$(this).css("background", "#EEEEEE");
			}
		});
	}
	
</script>
