var ASN_INE = ASN_INE || {};

ASN_INE.noMembersInCommonMsg = function(display)
{
	var msg = document.getElementById("msgNoMembersInCommon");
	var msgSuccess = document.getElementById("msgNoMembersInCommonSuccess");
	var msgError = document.getElementById("msgNoMembersInCommonError");
	var groupAssignRadio = document.getElementById("groupAssignment");
	if (groupAssignRadio.checked && display === true)
	{
		ASN_INE.show(msg);
	}
	else
	{
		ASN_INE.hide(msg);
	}

	// hide any previous success/failure messages
	ASN_INE.hide(msgSuccess);
	ASN_INE.hide(msgError);
};

/**
 * Validates the state of the group selection, disabling the submit buttons if the Assign To
 * section indicates groups are in use but no groups have been selected
 */
ASN_INE.validateGroupSelection = function()
{
	var groupsRadio = document.getElementById("groups");
	var groupAssignRadio = document.getElementById("groupAssignment");
	var groupsInUse = groupsRadio.checked || groupAssignRadio.checked;
	var selectedGroupCount = ASN_INE.getSelectedGroupsCount();
	var groupsSelected = ASN_INE.getSelectedGroupsCount() > 0;

	var disableButtons = groupsInUse && !groupsSelected;
	ASN_INE.enablePostPreviewSave(!disableButtons);

	var groupCount = groupsInUse ? selectedGroupCount : 0;
	ASN_INE.enableNoMembersInCommon(groupCount);
};

ASN_INE.enablePostPreviewSave = function(enabled)
{
	// Get the form submission buttons
	var postButtons = document.getElementsByName("post");
	var previewButtons = document.getElementsByName("preview");
	var saveButtons = document.getElementsByName("save");

	// Enable/disable the post, save and preview buttons
	for (i = 0; i < postButtons.length; i++)
	{
		postButtons[i].disabled = !enabled;
	}
	for (i = 0; i < previewButtons.length; i++)
	{
		previewButtons[i].disabled = !enabled;
	}
	for (i = 0; i < saveButtons.length; i++)
	{
		saveButtons[i].disabled = !enabled;
	}
};

ASN_INE.togglePeerAssessment = function(element)
{
	//Disable the peer review area and renable the site property unless this is selected
	var section = document.getElementById("peerAssessmentOptions");
	section.style.display="none";
	ASN.resizeFrame('shrink');
	$("#site").prop("disabled", false);
	$("#site").parent().prop("disabled", false);
	$("#site").parent().prop("class", "");
	$("#site").parent().prop("style", "");

	ASN_INE.setGroupAssignmentRadioEnabled(!element.checked);
	if (element.checked)
	{
		section.style.display="block";
		ASN.resizeFrame('grow');
	}
};

ASN_INE.setGroupAssignmentRadioEnabled = function(enabled)
{
	var groupAssignRadio = document.getElementById("groupAssignment");
	if (groupAssignRadio !== null)
	{
		groupAssignRadio.disabled = !enabled;
		var label = document.getElementById("groupAssignmentCheckboxLabel");
		var peerInUseMsg = document.getElementById("msgNoGroupAssignmentPeerInUse");
		if (enabled)
		{
			label.classList.remove("disabled");
			peerInUseMsg.style.display = "none";
		}
		else
		{
			label.classList.add("disabled");
			peerInUseMsg.style.display = "inline";
		}
	}
};

ASN_INE.isGradeTypePoints = function()
{
	var select = document.getElementsByClassName("gradeScaleSelect")[0];
	return select && select.value === "3";
};

ASN_INE.handleGradeAssignmentClick = function(checkbox, selectId, pointsId)
{
	$("#assignmentGradingPanel").toggle(checkbox.checked);

	var select = document.getElementById(selectId);
	ASN_INE.handleGradeScaleChange(select, pointsId);
};

ASN_INE.handleGradeScaleChange = function(select, textfieldId)
{
	var pointsField = document.getElementById(textfieldId);
	if (select === null || pointsField === null)
	{
		return;
	}
	var pointsPanel = document.getElementById("assignmentGradingPointsPanel");
	var isPoints = ASN_INE.isGradeTypePoints();
	pointsPanel.style.display = isPoints ? "block" : "none";

	if (!isPoints) // we're switching away from points which means peer assessment is not valid, so re-enable the group option
	{
		ASN_INE.setGroupAssignmentRadioEnabled(true);
	}
	else // we're switching to points, peer assessment may already be enabled
	{
		if (pointsField !== null && pointsField.value.length < 1)
		{
			pointsField.focus();
		}
		ASN_INE.evaluateAssignToOptionsForPeerAssessment();
	}
};

// evaluate the state of the assign to options based on the current
// state of peer assessment
ASN_INE.evaluateAssignToOptionsForPeerAssessment = function()
{
	var peerCheck = document.getElementById("usePeerAssessment");
	var gradeAsn = document.getElementById("gradeAssignment");
	if (peerCheck === null || gradeAsn === null)
	{
		return;
	}

	ASN_INE.setGroupAssignmentRadioEnabled(!gradeAsn.checked || !peerCheck.checked);
}

ASN_INE.handleSendToGradebookClick = function(checkbox, addToGbRadioId, assocWithGbRadioId)
{
	if (checkbox.checked)
	{
		var addRadio = document.getElementById(addToGbRadioId);
		var assocRadio = document.getElementById(assocWithGbRadioId);
		if (addRadio !== null && !addRadio.checked && (assocRadio === null || !assocRadio.checked))
		{
			addRadio.click();
		}
	}
	var panel = document.getElementById("assignmentGradingGradebookOptionsPanel");
	panel.style.display = checkbox.checked ? "block" : "none";
}

// evaluate the state of the peer assessment option based on the current group
// assignment setting.
ASN_INE.evaluatePeerAssessmentOption = function()
{
	var groupAsn = document.getElementById("groupAssignment");
	var isGroup = groupAsn !== null && groupAsn.checked;
	var peerCheck = document.getElementById("usePeerAssessment");
	if (peerCheck === null)
	{
		return;
	}

	if (isGroup && peerCheck.checked)
	{
		peerCheck.click();
	}
	ASN_INE.disablePeerAssessment(isGroup);
};

ASN_INE.disablePeerAssessment = function(disable)
{
	var peerCheck = document.getElementById("usePeerAssessment");
	if (peerCheck === null)
	{
		return;
	}

	peerCheck.disabled = disable;
	var label = document.getElementById("peerAssessmentCheckboxLabel");
	if (disable)
	{
		label.classList.add("disabled");
		document.getElementById("peerGroupAsnWarn").style.display = "block";
	}
	else
	{
		label.classList.remove("disabled");
		document.getElementById("peerGroupAsnWarn").style.display = "none";
	}
};

ASN_INE.handleGradebookRadioClick = function(radio, addToGbRadioId)
{
	var catSelect = document.getElementById("category");
	var itemSelect = document.getElementById("gradebookItemSelect");
	var isAdd = radio.id === addToGbRadioId;
	if (catSelect !== null)
	{
		catSelect.disabled = !isAdd;
	}
	if (itemSelect !== null)
	{
		itemSelect.disabled = isAdd;
	}
};

ASN_INE.handleAssignToChangeForPeerAssessment = function()
{
	var peerCheck = document.getElementById("usePeerAssessment");
	if (peerCheck !== null)
	{
		ASN_INE.evaluatePeerAssessmentOption();
	}
};

ASN_INE.enableNoMembersInCommon = function(selectedGroupCount)
{
	ASN_INE.noMembersInCommonMsg(selectedGroupCount > 1);
};

ASN_INE.getSelectedGroupsCount = function()
{
	var options = document.getElementById("selectedGroups").options;
	var groupsSelected = 0;
	for (i = 0; i < options.length; i++)
	{
		if (options[i].selected === true)
		{
			groupsSelected++;
		}
    }

	return groupsSelected;
};

ASN_INE.checkGroupsNow = function(button, siteId, asnRef)
{
    ASN_INE_API.checkGroups(button, siteId, asnRef, ASN_INE.handleCheckGroups, ASN_INE.handleCheckGroupsError);
};

ASN_INE.handleCheckGroupsError = function(data)
{
	console.log("Ajax call error when attempting to check group membership uniqueness.");
};

ASN_INE.handleCheckGroups = function(data)
{
	var button = document.getElementById("checkNowButton");
	button.classList.remove("spinButton");
	button.disabled = false;

	var successMsg = document.getElementById("msgNoMembersInCommonSuccess");
	var errorMsg = document.getElementById("msgNoMembersInCommonError");
	var errorMultipleGroups = document.getElementById("multipleGroupError");
	var checkGroupsError = document.getElementById("checkGroupsError");
	var checkGroupsFailed = document.getElementById("checkGroupsFailed");
	var report = document.getElementById("checkGroupsErrorReport");

	if (errorMultipleGroups !== null)
	{
		ASN_INE.hide(errorMultipleGroups); // hide the multiple groups message from velocity
	}

	if ($.isEmptyObject(data)) // no response from server
	{
		ASN_INE.hide(successMsg);
		ASN_INE.show(errorMsg);
		ASN_INE.hide(checkGroupsError);
		ASN_INE.show(checkGroupsFailed);
	}
	else if ($.isEmptyObject(data.assignment_collection) || data.assignment_collection.length === 0)
	{
		ASN_INE.show(successMsg);
		ASN_INE.hide(errorMsg);
	}
	else // users in multiple groups
	{
		ASN_INE.hide(successMsg);
		ASN_INE.show(errorMsg);
		ASN_INE.show(checkGroupsError);
		ASN_INE.hide(checkGroupsFailed);
		ASN_INE.show(report);

		report.innerHTML = "";
		data.assignment_collection.forEach(function(reportRow)
		{
			var groupTitles = [];
			reportRow.groups.forEach(function(group)
			{
				groupTitles.push(group.title);
			});

			var li = report.appendChild(document.createElement("li"));
			var user = $('<div>').text(reportRow.user.displayName).html();
			var row = user + " (" + groupTitles.join(", ") + ")";
			li.innerHTML = row;
		});
	}
};

ASN_INE.hide = function(element)
{
	element.classList.add("is-hidden");
};

ASN_INE.show = function(element)
{
	element.classList.remove("is-hidden");
};

var ASN_INE_API = ASN_INE_API || {};

ASN_INE_API.checkGroups = function(button, siteId, asnRef, onSuccess, onError)
{
	var endpoint = "/direct/assignment/checkForUsersInMultipleGroups.json";
	var params = {};
	var groups = document.getElementById("selectedGroups").options;

	params.selectedGroups = Array.apply(null, groups).filter(el => el.selected).map(el => el.value);
	params.siteId = siteId;
	params.asnRef = asnRef;

	button.classList.add("spinButton");
	button.disabled = true;

	ASN_INE_API._GET(endpoint, params, onSuccess, onError);
};

ASN_INE_API._GET = function(url, data, onSuccess, onError, onComplete)
{
	$.ajax(
	{
		type: "GET",
		url: url,
		data: data,
		cache: false,
		success: onSuccess || $.noop,
		error: onError || $.noop,
		complete: onComplete || $.noop
	});
};
