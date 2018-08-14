// Javascript for the "Sort by..." feature of the CardTable
var RHTB = RHTB || {}; // RHTB = Responsive Headers Tool Bar

RHTB.toggleSort = function(table, showButton, hideButton, on)
{
	showButton.style.display = on ? "none" : "initial";
	hideButton.style.display = on ? "initial" : "none";

	Array.prototype.forEach.call(table.getElementsByClassName("wicket_orderNone"), function(element)
	{
		if (on)
		{
			element.classList.add("sakai-showHeader");
		}
		else
		{
			element.classList.remove("sakai-showHeader");
		}
	});
};

RHTB.init = function(tableId, sortableCols)
{
	var table = document.getElementById(tableId);
	var sortToggle = table.getElementsByClassName("sakai-sortToggle")[0];
	var showButton = table.getElementsByClassName("sakai-showSortButton")[0];
	var hideButton = table.getElementsByClassName("sakai-hideSortButton")[0];
	if (sortToggle && showButton && hideButton && sortableCols > 1)
	{
		showButton.style.display = "inline";
		showButton.addEventListener("click", function(){RHTB.toggleSort(table, showButton, hideButton, true);}, false);
		hideButton.addEventListener("click", function(){RHTB.toggleSort(table, showButton, hideButton, false);}, false);
	}
};
