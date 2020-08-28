// SAM-1817: This was originally in RichTextEditor.java
function show_editor(client_id, frame_id, max_chars) {
	var status =  document.getElementById(client_id + '_textinput_current_status');
	status.value = "expanded";
	chef_setupformattedtextarea(client_id, true, frame_id, max_chars);
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

function chef_setupformattedtextarea(client_id, shouldToggle, frame_id, max_chars) {
	$("body").height($("body").outerHeight() + 600);

	var textarea_id = client_id + "_textinput";

	if (shouldToggle == true) {
		var input_text = document.getElementById(textarea_id);
		var input_text_value = input_text.value;
		var input_text_encoded = encodeHTML(input_text_value);
		input_text.value = input_text_encoded;
	}

	config = ''
	if (max_chars) {
		config = {wordcount: {'maxCharCount' : 32000}}
	}
	sakai.editor.launch(textarea_id, config,'450','240');
	//setMainFrameHeight(frame_id);
}

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
				var dynId = parts[i+3];
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
				alert(matrixChoicesAlert);
				allowChange = false;
			}
		});
	}

	return allowChange;
}

function resizeFrame(updown) {
    var frame;
    if (top.location !== self.location) {
        frame = parent.document.getElementById(window.name);
    }
    if (frame) {
        var clientH;
        if (updown === "shrink") {
            clientH = frame.scrollHeight;
        } else {
            clientH = frame.scrollHeight + 30;
        }
        $(frame).height(clientH);
    }
}

function returnToHostUrl(url) {

  if (url) {
    parent.location.href = url;
    return false;
  }
}

function initRubricDialog(gradingId, saveText, cancelText, titleText) {

  var modalId = "modal" + gradingId;
  var previousScore =  $('.adjustedScore' + gradingId).val();
  $("#" + modalId).dialog({
    modal: true,
    buttons: [
      {
        text: saveText,
        click: function () { $(this).dialog("close"); }
      },
      {
        text: cancelText,
        click: function () {

          $(this).dialog("close");
          $('.adjustedScore' + gradingId).val(previousScore);
        }
      }
    ],
    height: "auto",
    margin: 100,
    width: 1100,
    title: titleText
  });
}

$(function () {

  $('body').on('total-points-updated', function (e) {

    e.stopPropagation();

    // handles point changes for assignments, updating the grade field if it exists.
    var gradeField = $('.adjustedScore' + e.detail.evaluatedItemId.replace("\.", "\\."));
    if (gradeField) {
      gradeField.val(e.detail.value);
    }
  });

  // SAK-38320: add scope to the table. Maybe can add these direct to the JSF table after JSF 2.3 upgrade?
	$('table.matrixTable th.matrixSurvey').attr('scope', 'col');
	$('table.matrixTable td.matrixColumn').attr('scope', 'row');

  $(window.self).unbind("scroll");
  $(window.self).scroll(function () {
    resizeFrame("grow");
  });

  const save = e => {
    [...document.getElementsByTagName("sakai-rubric-grading")].forEach(srb => srb.release());
  };

  let saveButton = document.getElementById("editStudentResults:save");
  saveButton && saveButton.addEventListener("click", save);

  saveButton = document.getElementById("editTotalResults:save");
  saveButton && saveButton.addEventListener("click", save);

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
