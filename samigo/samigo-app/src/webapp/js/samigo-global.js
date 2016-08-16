// SAM-1817: This was originally in RichTextEditor.java
function show_editor(client_id, frame_id) {
	var status =  document.getElementById(client_id + '_textinput_current_status');
	status.value = "expanded";
	chef_setupformattedtextarea(client_id, true, frame_id);
	if (typeof setBlockDivs == "function" && typeof retainHideUnhideStatus == "function") {
		setBlockDivs();
		retainHideUnhideStatus('none');
	}
	var toggle=document.getElementById(client_id + "_toggle");
	if(toggle!==null){
		toggle.style.display = "none";
	}
}

function encodeHTML(text) {
	text = text.replace(
		/&/g, '&amp;').replace(
		/"/g, '&quot;').replace(
		/</g, '&lt;').replace(
		/>/g, '&gt;');
	return text;
}

function chef_setupformattedtextarea(client_id, shouldToggle, frame_id) {
	$("body").height($("body").outerHeight() + 600);

	var textarea_id = client_id + "_textinput";

	if (shouldToggle == true) {
		var input_text = document.getElementById(textarea_id);
		var input_text_value = input_text.value;
		var input_text_encoded = encodeHTML(input_text_value);
		input_text.value = input_text_encoded;
	}

	sakai.editor.launch(textarea_id,'','450','240');
	setMainFrameHeight(frame_id);
}

$( document ).ready(function() {
  if ( $("#selectIndexForm\\:selectTable").length ) {
    $("#selectIndexForm\\:selectTable").tablesorter({ 
      sortList: [[2,0]],
      textExtraction: {
        0: function(node, table, cellIndex) { return $(node).find("a").text(); }
      }
    });
  }
  if ( $("#editform\\:questionpool-questions").length ) {
    $("#editform\\:questionpool-questions").tablesorter({
      headers: {
        0: {
          sorter: false
        }
      }
    });
  }
});

function whichradio(el) {
	var parentTable = $(el).closest('table');
	var forcedRanking;
	var allowChange = true;
	// resolve the property for this instance
	$(parentTable).siblings('input[type=hidden]').each(
			function() {
				if($(this).prop('id').indexOf('forceRanking') !== -1 ) {
					forcedRanking = $(this).val() === "true";
					return false;
				}
			});
	if(forcedRanking) {
		var parts = $(el).prop('id').split(':');
		var curCol = -1;
		var colId = '';
		// determine current column
		for(var i = 0; i < parts.length; ++i) {
			if(parts[i] === 'matrixSurveyRadioTable') {
				var dynId = parts[i+2];
				curCol = dynId.substring(dynId.lastIndexOf('_')+1, dynId.length);
				colId = curCol + ':myRadioId';
				break;
			}
		}
		// check for conflicts
		$('input[type=radio]',parentTable).not(el).each(function(){
			var id = $(this).prop('id');
			if(id.indexOf(colId) !== -1 && $(this).is(':checked')) {
				el.checked = false;
				alert("You are only allowed one selection per column, please try again.");
				allowChange = false;
			}
		});
	}

	return allowChange;
}
