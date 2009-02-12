var serializationChanged = new Boolean(false);

function serialize(s)
{
	//kill the unsaved changes message
	if (navigator.userAgent.toLowerCase().indexOf("safari") != -1 && window != top) {
		top.onbeforeunload = function() { };
	}
	else {
		window.onbeforeunload = function() { };
	}
	
	serial = $.SortSerialize(s);
	
	//TODO: replace regexp stuff with a new hidden id item
	var pageOrder = serial.hash;
	pageOrder = pageOrder.replace(/:&sort1\[\]=content::page-row:/g, ' ');
	pageOrder = pageOrder.replace('sort1[]=content::page-row:', '');
	pageOrder = pageOrder.substring(0, pageOrder.length - 1);

	document.getElementById('content::state-init').value = pageOrder;
}

function doRemovePage(clickedLink) {
	var name = $(clickedLink).parent().parent().find(".item_label_box").text();
	var conf = confirm($("#del-message").text() + " " + name + "?");

	if (conf == true) {
		$("#call-results").fadeOut('400');
		$("#call-results").load(clickedLink, function() {
			var status = $(this).find("div[@id='value']").text();
			if (status == "pass") {
		    	var target = $(clickedLink).parent().parent();
				$(this).fadeIn('400');		
				$(target).slideUp('fast', $(target).remove());
			}
			else if (status == "fail") {
				$(this).fadeIn('400');
			}
			//TODO: refresh available pages, but don't mess up display message
			resetFrame();
	  	});
	}
	return conf;
}

function doShowPage(clickedLink) {
		$(clickedLink).parent().parent().find(".item_control.show_link").hide();
		$(clickedLink).parent().parent().find(".indicator").show();
		$("#call-results").fadeOut('10');
		$("#call-results").load(clickedLink, function() {
			var status = $("#call-results").find("#value").text();
			$(clickedLink).parent().parent().find(".indicator").hide();				
			if (status == "pass") {
				$(clickedLink).parent().parent().find(".item_control.hide_link").show();
				$("#call-results").fadeIn('400');
			}
			else if (status == "fail") {
				$(clickedLink).parent().parent().find(".item_control.show_link").show();
				$("#call-results").fadeIn('400');
			}
	  	});
}

function doHidePage(clickedLink) {
		$(clickedLink).parent().parent().find(".item_control.hide_link").hide();
		$(clickedLink).parent().parent().find(".indicator").show();
		$("#call-results").fadeOut('10');
		$("#call-results").load(clickedLink, function() {
			var status = $("#call-results").find("#value").text();
			$(clickedLink).parent().parent().find(".indicator").hide();				
			if (status == "pass") {
				$(clickedLink).parent().parent().find(".item_control.show_link").show();
				$("#call-results").fadeIn('400');
			}
			else if (status == "fail") {
				$(clickedLink).parent().parent().find(".item_control.hide_link").show();
				$("#call-results").fadeIn('400');
			}
	  	});
}

function doEditPage(clickedLink) {
	$("#call-results").load(clickedLink, function() {
		var status = $("#call-results").find("#value").text();
		if (status == "pass") {
	    	var target = document.getElementById('content::page-row:' + $("#call-results").find("#pageId").text() + ':');
			$("#call-results").fadeIn('500');
					
		}
		else if (status == "fail") {
			$("#call-results").fadeIn('500');
		}
  	});
}

function showAddPage(clickedLink, init) {
	if (init) {
		$("#add-control").hide();
		$("#list-label").show();
		$(".tool_list").css("border", "1px solid #ccc");
	}
	$("#add-panel").fadeOut(1, $("#add-panel").load(clickedLink, function() {
		$("#call-results").fadeOut(200, function() {
			$("#call-results").html($("#add-panel").find("#message").html());
			$("#add-panel").fadeIn(200, $("#call-results").fadeIn(200, resetFrame()));
		});
	}));
}

function showEditPage(clickedLink) {
	li = $(clickedLink).parent().parent();
	$(li).find(".item_label_box").hide();
	$(li).find(".item_control_box").hide();
	$(li).find(".item_edit_box").fadeIn('normal');
	//$(li).removeClass("sortable_item");
	$(li).addClass("editable_item");
	$(li).unbind();
	resetFrame();
}

function doSaveEdit(clickedLink) {
	li = $(clickedLink).parent().parent();
	newTitle = $(li).find(".new_title");
	newConfig = $(li).find(".new_config");
	$("#call-results").load(clickedLink + "&newTitle=" + encodeURIComponent(newTitle.val()) + "&newConfig=" + encodeURIComponent(newConfig.val()), function() {

		var status = $("#call-results").find("#value").text();
		if (status == 'pass') {
			$(li).find(".item_edit_box").hide();
			newTitle = $("#call-results").find("#pageId strong").html();	
			$(li).find(".item_label_box").empty();
			$(li).find(".item_label_box").append(newTitle);
			$(li).find(".item_label_box").show();
			$(li).find(".item_label_box").attr("style", "display: inline");
			$(li).find(".item_control_box").show();
			$(li).find(".item_control_box").attr("style", "display: inline");
			$(li).addClass("sortable_item");
			$(li).removeClass("editable_item");
			makeSortable($(li).parent());
		}
  	});
}

function doCancelEdit(clickedLink) {
	li = $(clickedLink).parent().parent();
	$(li).find(".item_edit_box").hide();
	$(li).find(".item_label_box").show();
	$(li).find(".item_label_box").attr("style", "display: inline");
	$(li).find(".item_control_box").show();
	$(li).find(".item_control_box").attr("style", "display: inline");
	$(li).addClass("sortable_item");
	$(li).removeClass("editable_item");
	$(li).find(".new_title").val($(li).find(".item_label_box").text());
	makeSortable($(li).parent());
}

function checkReset() {
	var reset = confirm($("#reset-message").text());
	if (reset)
		return true;
	else
		return false;
}
				
function makeSortable(path) {
	$(path).Sortable( {
		accept :        'sortable_item',
		activeclass :   'sortable_active',
		hoverclass :    'sortable_hover',
		helperclass :   'sort_helper',
		opacity:        0.8,
		revert:	        true,
		tolerance:      'intersect',
		axis:           'vertically',
		domNode:        $(path).get(0),
		onStop:	        function () {
			if (serializationChanged == false) {
				serializationChanged = true;
				
				//Makes the assumption that it is ok to over write the onbeforeexit event on top
				//which is a safe assumption *most* of the time and it's only needed for Safari
				if (navigator.userAgent.toLowerCase().indexOf("safari") != -1 && window != top) {
					top.pageOrderExitMessage = $("#exit-message").text();
					top.onbeforeunload = function() { return top.pageOrderExitMessage };
				}
				else {
					window.onbeforeunload = function() { return $("#exit-message").text(); };
				}
			}
		}
	});
}

function makeDraggable(path) {
	$(path).Draggable( {
		revert : true,
		onStop: function () {
			if ($(this).parent().attr('id') == 'sort1') {
				addTool($(this), false);
			}
		}
	});
}

function addTool(draggable, manual) {
	if (manual == true) {
		// we got fired via the add link not a drag and drop..
		//  so we need to manually add to the list
		$('#sort1').append(draggable);
	}
	$(draggable).attr("style", "");
	//force possitioning so IE displays this right
	$(draggable).position("static");
	$("#call-results").fadeOut('200');
	url = $(draggable).find(".tool_add_url").attr("href");
	oldId = $(draggable).id();
	$(draggable).empty();
	li = $(draggable);
	$("#call-results").load(url, function() {
		$(li).DraggableDestroy();
		$(li).id("content::" + $("#call-results").find("li").id());
		$(li).html($("#call-results").find("li").html());
		$(this).find("li").remove();
		makeSortable($(li).parent());
		$("#call-results").fadeIn('200', resetFrame());
	});
	return false;
}

