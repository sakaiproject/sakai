var MAXITEMS = 10; // Max amount of extended time items allowed. We're capping this at 10 for now

var activeExtTimeEntries = 0;

// This is the method called from authorSettings.jsp to set up all the extended time stuff
function extendedTimeInitialize() {
	addAllExtendedTimeItems();
	allExtendedTimeEntries = document.getElementById("assessmentSettingsAction\:xt1").value.split("^");

	for (var itemNum = 1; itemNum <= MAXITEMS; itemNum++) { 
		copyListValuesForExtTime(itemNum);
	}

	var itemNum = 1;
	do {
		activeExtTimeEntries++; 
		fullExtendedTimeString = allExtendedTimeEntries[itemNum-1].split("|");
		initializeExtTimeValues(fullExtendedTimeString,itemNum);
		showActiveExtTimeEntities(fullExtendedTimeString,itemNum);
		itemNum++;
			
	} while(itemNum < allExtendedTimeEntries.length);
}

// We secretly store the extended time info in a hidden JSF input field. Whenever a user edits an
// extended time value this method gets called on save and updates the hidden field with everything
// that has been entered. When the form is submitted Java sorts it all out.
function extendedTimeCombine() {
	document.getElementById("assessmentSettingsAction\:xt1").value = "";
	for (var itemNum = 1; itemNum <= MAXITEMS; itemNum++) { 
		var target = document.getElementById("xt_id"+itemNum).value;
		if(target != "1"){ // don't add empties
			var minutes = (parseInt(document.getElementById("xt_hours"+itemNum).value) * 3600) + parseInt(document.getElementById("xt_minutes"+itemNum).value) * 60;
			var code = target+"|" + minutes +"|"
			+ document.getElementById("xt_open"+itemNum).value+"|" + document.getElementById("xt_due"+itemNum).value+"|" + document.getElementById("xt_retract"+itemNum).value
			+ "^";
			document.getElementById("assessmentSettingsAction\:xt1").value = document.getElementById("assessmentSettingsAction\:xt1").value.concat(code);
		} // end if(target != "0")
	} //end for
	
}

// Each Extended time item needs to either bring in existing values or
// sync with values already on the page.
function initializeExtTimeValues(fullExtendedTimeString,itemNum) {
	//document.getElementById("xt_id"+itemNum).value = fullExtendedTimeString[0];
	var targetId = fullExtendedTimeString[0]; 
	var seconds = fullExtendedTimeString[1];
	var hours = Math.floor(seconds / (60 * 60)); 
	var divisor_for_minutes = seconds % (60 * 60);
	var minutes = Math.floor(divisor_for_minutes / 60);

	initializeSelectList("xt_id"+itemNum, targetId); 
	initializeSelectList("xt_hours"+itemNum, hours);
	initializeSelectList("xt_minutes"+itemNum, minutes);

	document.getElementById("xt_open"+itemNum).value = evaluateDate(fullExtendedTimeString[2]);
	document.getElementById("xt_due"+itemNum).value = evaluateDate(fullExtendedTimeString[3]);
	document.getElementById("xt_retract"+itemNum).value = evaluateDate(fullExtendedTimeString[4]);
}

// Avoid undefined date values
function evaluateDate(dateVal) {
	if(dateVal == null) return "";
	else return dateVal;
}

// If there are already values for extended time, go ahead and show them; otherwise
// hide them until the user activates them.
function showActiveExtTimeEntities(fullExtendedTimeString,itemNum) {
	if(document.getElementById("xt_id"+itemNum).value.length > 1) { 
		document.getElementById("extendedTimeEntries").style.display = 'block';
		document.getElementById("xt"+itemNum).style.display = 'block';
		document.getElementById("xt_show").style.display = 'none';

		if(document.getElementById("xt_open"+itemNum).value.length > 1) {
			document.getElementById("xt_dates"+itemNum).style.display = 'block';
			document.getElementById("xt_datesToggle"+itemNum).checked=true;
		}
	} else if(itemNum == 1){ // if nothing is set, allow the first one to show so the user can add input
		document.getElementById("xt"+itemNum).style.display = 'block';
	}
}

// Default a select list to a particular value
function initializeSelectList(listName, val) {
	var sel = document.getElementById(listName);
	for(var i = 0, j = sel.options.length; i < j; ++i) {
		if(sel.options[i].value == val) {
		   sel.selectedIndex = i;
		   break;
		}
	 }
}

// Rather than building lists independently. We'll guarantee consistency by copying them from the page.
function copyListValuesForExtTime(itemNum) {

	var srcTargetList = document.getElementById("assessmentSettingsAction\:extendedTimeTargets");
	var options = srcTargetList.innerHTML;
	document.getElementById("xt_id"+itemNum).innerHTML = options;

	var srcHoursList = document.getElementById("assessmentSettingsAction\:timedHours");
	options = srcHoursList.innerHTML;
	document.getElementById("xt_hours"+itemNum).innerHTML = options;

	var srcMinutesList = document.getElementById("assessmentSettingsAction\:timedMinutes");

	options = srcMinutesList.innerHTML;
	document.getElementById("xt_minutes"+itemNum).innerHTML = options;

}

// Control to allow checkboxes to toggle whether a div displays or not.
function toggleExtendedTimeEntity(it, box) { 
	var vis = (box.checked) ? "block" : "none";
	document.getElementById(it).style.display = vis;

	// They are clearing out the list
	if(vis == "none" && it == "extendedTimeEntries") {
		deleteAllExtTimeEntries();
	}
}

// Action whent he first add link is clicked
function showExtendedTime() {
	document.getElementById('xt_show').style.display = "none";
	document.getElementById('extendedTimeEntries').style.display = "block";
}

// Action when a user clicks to add a new extended time entry
function addExtTimeEntry() {
	activeExtTimeEntries++;
	document.getElementById("xt"+activeExtTimeEntries).style.display = "block";
	if(activeExtTimeEntries == MAXITEMS) { // prevents them from adding more than max
		document.getElementById("addExtTimeControl").style.display = "none";
	}
}

// Delete Extended Time entry when a user clicks the button
function deleteExtTimeEntry(itemNum) {
	document.getElementById("xt_id"+itemNum).value = "1";
	document.getElementById("xt_hours"+itemNum).value = "0";
	document.getElementById("xt_minutes"+itemNum).value = "0";
	deleteExtTimeDates(itemNum);
	extendedTimeCombine(); // updates the form input
	document.getElementById("xt"+itemNum).style.display = "none";
}

function deleteExtTimeDates(itemNum) {
	document.getElementById("xt_open"+itemNum).value = "";
	document.getElementById("xt_due"+itemNum).value = "";
	document.getElementById("xt_retract"+itemNum).value = "";
}

function deleteAllExtTimeEntries() {
	for (var itemNum = 1; itemNum <= MAXITEMS; itemNum++) { 
		deleteExtTimeEntry(itemNum);
	}

	// we need to keep one around for re-adding potientially
	activeExtTimeEntries = 1;
	document.getElementById("xt1").style.display = "block";
}

// Dynamically create a div for each potential extended time item. Most will be hidden.
function addAllExtendedTimeItems() {
	var xtItem = document.getElementById("extendedTimeEntries");
	xtItem.innerHTML = "";

	for (var itemNum = 1; itemNum <= MAXITEMS; itemNum++) { 

		var code = "<div id=\"xt"+itemNum+"\" style=\"display:none;\">"+ // none display by default
			"<br />"+
			"<select id=\"xt_id"+itemNum+"\"></select>&nbsp;&nbsp;"+
			"<select id=\"xt_hours"+itemNum+"\"></select>hrs.&nbsp;"+
			"<select id=\"xt_minutes"+itemNum+"\"></select>min.&nbsp;"+
			"&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<input type=\"button\" value=\"Delete this entry\" onclick=\"deleteExtTimeEntry("+itemNum+")\">"+ // delete button
			"<br />"+
			"<input id=\"xt_datesToggle"+itemNum+"\" type=\"checkbox\" class=\"tier1\" onclick=\"deleteExtTimeDates("+itemNum+");toggleExtendedTimeEntity('xt_dates"+itemNum+"', this)\">Change Delivery Dates for this group/student."+
			"<div id=\"xt_dates"+itemNum+"\" class=\"tier3\" style=\"display:none;\">"+ // dates don't display by default
			"<table><tr><td>"+
			"Available Date</td><td>"+
			"<input type=\"text\" size=\"25\"  id=\"xt_open"+itemNum+"\">&nbsp;"+
			"<img id=\"_datePickerPop_xt_open"+itemNum+"\" height=\"16\" border=\"0\" width=\"16\" alt=\"Click Here to Pick Date\" src=\"/library/calendar/images/calendar/cal.gif\" style=\"cursor:pointer;\" onclick=\"javascript:var cal06670970441558088 = new calendar2(document.getElementById('xt_open"+itemNum+"'));cal06670970441558088.year_scroll = true;cal06670970441558088.time_comp = true;cal06670970441558088.popup('','/library/calendar/html/');\">"+
			"</td></tr><tr><td>"+
			"Due Date</td><td>"+
			"<input type=\"text\" size=\"25\"  id=\"xt_due"+itemNum+"\">&nbsp;"+
			"<img id=\"_datePickerPop_xt_due"+itemNum+"\" height=\"16\" border=\"0\" width=\"16\" alt=\"Click Here to Pick Date\" src=\"/library/calendar/images/calendar/cal.gif\" style=\"cursor:pointer;\" onclick=\"javascript:var cal06670970441558088 = new calendar2(document.getElementById('xt_due"+itemNum+"'));cal06670970441558088.year_scroll = true;cal06670970441558088.time_comp = true;cal06670970441558088.popup('','/library/calendar/html/');\">"+
			"</td></tr><tr><td>"+
			"Retract Date</td><td>"+
			"<input type=\"text\" size=\"25\"  id=\"xt_retract"+itemNum+"\">&nbsp;"+
			"<img id=\"_datePickerPop_xt_retract"+itemNum+"\" height=\"16\" border=\"0\" width=\"16\" alt=\"Click Here to Pick Date\" src=\"/library/calendar/images/calendar/cal.gif\" style=\"cursor:pointer;\" onclick=\"javascript:var cal06670970441558088 = new calendar2(document.getElementById('xt_retract"+itemNum+"'));cal06670970441558088.year_scroll = true;cal06670970441558088.time_comp = true;cal06670970441558088.popup('','/library/calendar/html/');\">"+
			"</td></tr></table>"+
			"</div> <!--end dates -->"+
			"<hr width=\"450\"  align=\"left\">"+
			"</div> <!--end xt"+itemNum+" -->";

			xtItem.innerHTML = xtItem.innerHTML.concat(code);
	} // end for

	// Add the link for allowing the user to add additional extended time entries
	var addLinkCode = "<div id=\"addExtTimeControl\">"+
			"<a href=\"#\" onclick=\"addExtTimeEntry()\" style=\"color:#0080C0\">Add another Time Limit/Delivery Date Exception.</a>"+
			"</div>";
	xtItem.innerHTML = xtItem.innerHTML.concat(addLinkCode);

}