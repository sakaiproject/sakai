jQuery(document).ready(function () {
	var opts = {
    	selectors: {
			movables: ".movable"
    	}
	};

	return fluid.reorderList("#listx", opts);
});

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