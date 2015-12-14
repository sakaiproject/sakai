<!-- Javascript for the Question Progress Panel-->
(function (questionProgress, $, undefined) {
	var origWrapWidth = 0;
	var CLICK_PANEL_WIDTH = 299;
	var QP_ENABLED = true;
	questionProgress.nonLinear;
	var arrowImgPrefix = "img[id*='";
	var arrowSuffix = "']";
	var rightArrowSuffix = ":rightArrow";
	var downArrowSuffix = ":downArrow";

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

		if (!QPToggleOn) {
			origWrapWidth = wrapWidth;
		}
		else {
			origWrapWidth = wrapWidth + clickWidth + panelWidth;
		}

		$("#qpOpen").toggle();
		$("#qpClose").toggle();
		$("#questionProgressPanel").toggle("slide", {direction: "right"});

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

	function partLinkClickEvent(event) {
		$(event.data.rightArrow).slideToggle();
		$(event.data.downArrow).slideToggle();
		$(event.data.item).slideToggle();
		event.preventDefault();
	}

	function selectAllPartTableDivs() {
		return $("div[id*='partTable']");
	}

	function setUpClickHandlersForParts() {
		var allParts = selectAllPartTableDivs();
		var temp;
		var rightArrow;
		var downArrow;
		var part;
		for(var i = 0; i < allParts.size(); i++) {
			temp = allParts[i].id;
			temp = temp.replace("Table", "Link");
			part = parseInt(temp.substr(9)) - 1;
			rightArrow = arrowImgPrefix + part + rightArrowSuffix + arrowSuffix;
			downArrow = arrowImgPrefix + part + downArrowSuffix + arrowSuffix;
			$("#" + temp).on("click", {
				item: allParts[i],
				rightArrow: rightArrow,
				downArrow: downArrow
			}, partLinkClickEvent);
		}
	}

	function transposeHelper(tableListId) {
		var MAXCOLUMNS = 5;
		var oldTable = document.getElementById(tableListId);
		var newTable = document.createElement("table");

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
	}

	// This method transposes the table of contents table from displaying everything in one column to
	// rather display in a more space efficient table with 10 columns per row.
	questionProgress.transposeTOCTables = function() {
		var tableList = document.getElementsByTagName("table");

		for (var i = 0; i < tableList.length; i++) { // loop thru all tables to find the ones we want
			if (tableList[i].id.indexOf("tocquestions") > 0) {
				transposeHelper(tableList[i].id);
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

			setUpClickHandlersForParts();
			var tables = selectAllPartTableDivs();
			var currentPart = $(":input[id*=partIndex]").val();
			$(tables[currentPart]).show();
		}

	};

	// Hide if there's more than one question on the page
	questionProgress.access = function(navigation, layout) {
		if(layout === 1) { // one question per page
			document.getElementById('questionProgressClick').style.display = "block";
			questionProgress.nonLinear = true;
			if (navigation === 1) { // Linear assessment
				questionProgress.nonLinear = false;
			}
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