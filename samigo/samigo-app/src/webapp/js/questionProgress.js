<!-- Javascript for the Question Progress Panel-->
(function (questionProgress, $, undefined) {
	var origWrapWidth = 0;
	var CLICK_PANEL_WIDTH = 299;
	var QP_ENABLED = true;
	$('#questionProgressClick').click(function () {
		toggle(400);
		setQPToggleOn(!getQPToggleOn());
	});

	function toggle(animateTimer) {
		var QPToggleOn = getQPToggleOn();
		var wrapWidth = document.getElementById("delivAssessmentWrapper").offsetWidth;
		var clickWidth = document.getElementById("questionProgressClick").offsetWidth;
		var panelWidth = CLICK_PANEL_WIDTH;
		var newWrapWidth = (wrapWidth - clickWidth) - panelWidth;
		var clickHeight = $("#questionProgressClick").height();
		var panelHeight = $("#questionProgressPanel").height();

		if (!QPToggleOn) {
			origWrapWidth = wrapWidth;
		}
		else {
			origWrapWidth = wrapWidth + clickWidth + panelWidth;
		}

		$("#qpOpen").toggle();
		$("#qpClose").toggle();
		$("#questionProgressPanel").toggle("slide", {direction: "right"});
		var panelHeight = $("#questionProgressPanel").height();

		// Position the clickTab
		if (!QPToggleOn) {
			$("#questionProgressClick").animate({marginTop: 0, right: panelWidth}, animateTimer);
			$("#delivAssessmentWrapper").animate({width: newWrapWidth}, animateTimer);
		}
		else {
			$("#questionProgressClick").animate({marginTop: 0, right: 0}, animateTimer);
			$("#delivAssessmentWrapper").animate({width: origWrapWidth}, animateTimer);
		}
	}

	function getQPToggleOn() {
		var value = localStorage.getItem('QPToggleOn');
		return value == '1';
	}

	function setQPToggleOn(QPToggleOn) {
		localStorage.setItem('QPToggleOn', (QPToggleOn ? '1' : '0'));
	}

	// This method transposes the table of contents table from displaying everything in one column to
	// rather display in a more space efficient table with 10 columns per row.
	questionProgress.transposeTOCTables = function() {
		var MAXCOLUMNS = 5;
		var tableList = document.getElementsByTagName("table");
		for (var i = 0; i < tableList.length; i++) { // loop thru all tables to find the ones we want
			if (tableList[i].id.indexOf("tocquestions") > 0) {
				var oldTable = document.getElementById(tableList[i].id);
				var newTable = document.createElement("table");

				var colCount = 0;
				var newRowCount = 0;
				var colCount = 0;

				newTable.insertRow(newRowCount); // create row
				for (var oldRowCount = 0; oldRowCount < oldTable.rows.length; oldRowCount++) {
					newTable.rows[newRowCount].insertCell(colCount); // create cell

					// Set new cell equal to what was in the old row
					var cellText = oldTable.rows[oldRowCount].cells[0].innerHTML; // always just one column in the original rows

					newTable.rows[newRowCount].cells[colCount].innerHTML = cellText; // drop the dot.
					colCount++;

					if (colCount > (MAXCOLUMNS - 1)) { // reset & go to next row in new table
						newRowCount++;
						colCount = 0;
						newTable.insertRow(newRowCount); //create new row
					}
				}
				var temp = oldTable.ownerDocument.createElement('div');
				temp.innerHTML = '<table>' + newTable.innerHTML + '</table>';
				temp.firstChild.id = oldTable.id;
				temp.firstChild.className += oldTable.className;
				oldTable.parentNode.replaceChild(temp.firstChild, oldTable);
			}// end if tab.id
		} //end tableList for
	};

	// Make sure the main content area isn't initially too wide for the Question Progress
	// Panel to display.
	questionProgress.setUp = function() {
		if (QP_ENABLED) {
			var wrapWidth = document.getElementById("delivAssessmentWrapper").offsetWidth;
			var clickWidth = document.getElementById("questionProgressClick").offsetWidth;
			var newWrapWidth = (wrapWidth - clickWidth) - CLICK_PANEL_WIDTH;
			var clickPos = 0;

			if (!getQPToggleOn()) {
				document.getElementById('qpOpen').style.display = "none";
				document.getElementById('qpClose').style.display = "block";
				document.getElementById('questionProgressPanel').style.display = "none";
				document.getElementById('questionProgressClick').style.right = 0;
				newWrapWidth = newWrapWidth + CLICK_PANEL_WIDTH;
			} else {
				document.getElementById('qpOpen').style.display = "block";
				document.getElementById('qpClose').style.display = "none";
				document.getElementById('questionProgressClick').style.display = "block";
				document.getElementById('questionProgressPanel').style.display = "block";
			}

			document.getElementById('delivAssessmentWrapper').style.width = newWrapWidth + "px";
			document.getElementById('questionProgressClick').style.marginTop = clickPos + "px";
		}

	};

	// Hide Question Progress panel if strict linear nav. Also hide if there's more than one question on the page
	questionProgress.access = function(navigation, layout) {
		if (navigation == '2' && layout == '1') {
			document.getElementById('questionProgressClick').style.display = "block";
		} else {
			$("#questionProgressClick").hide();
			$("#questionProgressPanel").hide();
			QP_ENABLED = false;
		}
	};

	questionProgress.disableLink = function(link) {
		link.style.display = 'none';
		link.parentNode.firstChild.style.display = 'block';
		return true;
	};
}( window.questionProgress = window.questionProgress || {}, jQuery )) ;