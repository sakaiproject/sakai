var reorderlist = null;

// aftermove is used to enable or disable delete button
// depending upon where the row ends up

var aftermoveprocess = function (el,o) {
    aftermover(el);
}

function aftermover(el,pos) {
    // position after move
    var newpos = el.prevAll().length;
    var list = $('#listx');
    // position of the marker line
    var markerpos = list.find('.marker').parent().prevAll().length;
    // enable delete for items above the marker
   if (newpos > markerpos)
	el.find('img').hide();
    else
	el.find('img').show();
}

jQuery(document).ready(function () {
	var opts = {
    	selectors: {
			movables: ".movable"
    	},
	listeners: {
		afterMove: aftermoveprocess
	}
	};

	reorderlist = fluid.reorderList("#listx", opts);
	return reorderlist;
});

// delete moves the item to the end, i.e. below the line
function deleteitem(el) {
    var row = el.parent().parent();
    $('#listx').append(row);    
    reorderlist.refresh();
    aftermover(row);
}

function computeorder() {
	var rows = document.getElementById("itemTable").rows;
   	var order = "";
    var i = 0;
    var max = rows.length;

    for (i = 0; i < max; i++) {
		order = order + " " + rows[i].cells[0].innerHTML;
    }
    
	$("#order").val(order);
    return true;
}