var SynMainLite = SynMainLite || {};

SynMainLite.setOptionsVisible = function (visible) {
	const element = document.querySelector(".workspaceTable");
	if (!element || !DataTable.isDataTable(element)) {
		resize();
		return;
	}

	const table = new DataTable(element);
	let displayedRows = 0;
	table.rows().every(function () {
		// DataTables retains the checkbox cell even while the column is detached.
		const cell = table.cell(this.index(), 0).node();
		const checkbox = cell.querySelector("input[type='checkbox']:not(.unchangedValue)");
		const savedValue = cell.querySelector(".unchangedValue").checked;
		if (!visible) checkbox.checked = savedValue;
		this.node().hidden = !visible && savedValue;
		if (!savedValue) displayedRows++;
	});

	element.hidden = !visible && displayedRows === 0;
	table.column(0).visible(visible);

	const form = element.closest("form");
	form.querySelectorAll(".optionsTable, .hideInfo").forEach(control => {
		control.style.display = visible ? "" : "none";
	});
	form.querySelectorAll(".optionLink").forEach(link => {
		link.parentElement.parentElement.style.display = visible ? "none" : "";
	});
	form.querySelectorAll(".noActivity").forEach(message => {
		message.style.display = !visible && displayedRows === 0 ? "" : "none";
	});
	resize();
};

SynMainLite.setupTableCss = function(){
	
	jQuery("tr", $(".workspaceTable")).each(function(){
		 if($(this.cells).size() == 4){		
			 $($(this.cells)[2]).addClass('rightAlignColumn');
			 $($(this.cells)[3]).addClass('rightAlignColumn');
		 }else if($(this.cells).size() == 3){		
			 $($(this.cells)[2]).addClass('rightAlignColumn');
		 }	
	});

};

SynMainLite.setupTableSortImageOffset = function(){
	//this will adjust the sort images so they are right after the text
	//8px added for padding
	jQuery("th", jQuery("tr", $(".workspaceTable"))).each(function(){
		$(this).css("background-position", "" + ($($(this)[0].childNodes[0]).width() + 8) + "px");
	});	
};
	
/*
*
*Put this code in the jsp page since IE8 was having trouble with $(document).ready() function.
*
$(document).ready(function() { 
	SynMainLite.setupTableParsers();
	SynMainLite.setupTableCss();
	SynMainLite.setupTableHeaders();
	SynMainLite.setupTableSortImageOffset();
	//hide all checkboxes that are used to reset original values
	$(".unchangedValue").hide();
}); 


$(window).load(function(){
	SynMainLite.setOptionsVisible(false);
});

*/
