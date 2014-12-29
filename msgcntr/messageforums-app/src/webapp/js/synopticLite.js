var SynMainLite = SynMainLite || {};


	
var count = 0;




SynMainLite.toggleHiddenRows = function(){
	jQuery("tr", $(".workspaceTable")).each(function(){
		 if($(this.cells).size() >= 1){
			$($(this.cells)[0]).addClass('optionsTable');
			$($(this.cells)[0]).hide();
			count++
			if($(this.cells)[0].childNodes[0].checked){
				$(this).addClass('optionsTable');
				$(this).hide();
				count--;
			}
		 }	
		});

	if(count == 1){
		$(".workspaceTable").hide();
	}
	resize();
};


SynMainLite.getCount = function(){
	//if count returns 1, then that means there were no
	//rows in the table besides the header
	return count;
};


SynMainLite.resetCheckboxes = function(){
	//this is called when a user cancels their action
	//this function resets the checkboxes back to their orignal
	//value
	jQuery("tr", $(".workspaceTable")).each(function(){
		 if($(this.cells).size() >= 1){				
			if($(this.cells)[0].childNodes.length == 2){
				//checkbox exists, so reset to original setting
				$(this.cells)[0].childNodes[0].checked = $(this.cells)[0].childNodes[1].checked;
			}
		 }	
		});
	
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
	SynMainLite.toggleHiddenRows();
});

*/
