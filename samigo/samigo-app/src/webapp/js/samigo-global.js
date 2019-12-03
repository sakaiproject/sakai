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

  var addRubricInputs = function (e, type) {

    var gradingId = e.detail.evaluatedItemId.split(".")[0];
    var inputs = document.getElementById(gradingId + "-inputs");
    var inputId = gradingId + "-" + type + "-" + e.detail.criterionId;
    var input = document.getElementById(inputId );
    if (!input) {
      input = document.createElement("input");
      input.setAttribute("type", "hidden");
      input.setAttribute("id", inputId);
      var name = "rbcs-" + e.detail.evaluatedItemId + "-" + e.detail.entityId + "-" + type;
      if ("totalpoints" !== type && "state-details" !== type) name += "-" + e.detail.criterionId;
      input.setAttribute("name", name);
      if (inputs) {
        inputs.appendChild(input);
      }
    }
    input.setAttribute("value", "criterionrating" === type ? e.detail.ratingId : e.detail.value);
  };

  $('body').on('total-points-updated', function (e) {

    e.stopPropagation();
    var itemId = e.target.parentElement.getAttribute("item-id");

    var points = e.detail.value;

    // handles point changes for assignments, updating the grade field if it exists.
    var gradeField = $('.adjustedScore' + itemId);
    if (gradeField) {
      gradeField.val(points);
    }

    addRubricInputs(e, "totalpoints");
  });

  $('body').on('update-comment', e => addRubricInputs(e, "criterion-comment") );

  $('body').on('rubric-rating-tuned', e => addRubricInputs(e, "criterion-override"));

  $('body').on('rubric-rating-changed', e => {
    addRubricInputs(e, "criterion");
    addRubricInputs(e, "criterionrating");
  });

  $('body').on('update-state-details', e => addRubricInputs(e, "state-details"));

  // SAK-38320: add scope to the table. Maybe can add these direct to the JSF table after JSF 2.3 upgrade?
	$('table.matrixTable th.matrixSurvey').attr('scope', 'col');
	$('table.matrixTable td.matrixColumn').attr('scope', 'row');

  $(window.self).unbind("scroll");
  $(window.self).scroll(function () {
    resizeFrame("grow");
  });
});
