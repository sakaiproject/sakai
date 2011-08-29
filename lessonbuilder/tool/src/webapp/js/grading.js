$(function() {
	makeButtons();
	
	$.ajaxSetup ({
		cache: false
	});
	
	$(".gradingTable").find(".details-row").hide();
	
	$(".toggleButton").click(function(event) {
		var firstRow = $(this).parents(".first-row").get(0);
		$(".gradingTable").find(".details-row").each(function(index, value) {
			if($(value).is(":visible")) {
				$(value).hide();
			}else if(!$(value).is(":visible") && $(value).attr("id") === $(firstRow).next().attr("id")) {
				if($(value).find(".replaceWithComments").children().length == 0) {
					var oldSrc = $(firstRow).find(".toggleStatus").attr("src");
					oldSrc = oldSrc.replace("loading.gif", "");
					oldSrc = oldSrc.replace("no-status.png", "");
					
					$(firstRow).find(".toggleStatus").attr("src", oldSrc + "loading.gif");
					
					$(value).find(".replaceWithComments").load($(value).find(".commentsLink").attr("href"), function() {
						var current = $(value);
						var next = $(value).next().next();
						
						makeButtons();
						
						$(current).show();
						
						var oldSrc = $(firstRow).find(".toggleStatus").attr("src");
						oldSrc = oldSrc.replace("loading.gif", "");
						oldSrc = oldSrc.replace("no-status.png", "");
						
						$(firstRow).find(".toggleStatus").attr("src", oldSrc + "no-status.png");
						
						setMainFrameHeight(window.name);
						prefetchComments(next);
					});
				}else {
					$(value).show();
					setMainFrameHeight(window.name);
					prefetchComments($(value).next().next());
				}
			}
		});
		
		return false;
	});
	
	$(".pointsBox").each(function(index, value) {
		$(value).parent().children("img").attr("id", "statusImg" + index);
		$(value).val($(value).parent().children(".pointsSpan").text());
	});
	
	$(".pointsBox").live('change', function(){
		var img = $(this).parent().children("img");
		img.attr("src", getStrippedImgSrc(img.attr("id")) + "no-status.png");
		$(this).addClass("unsubmitted");
	});
	
	$(".pointsBox").keyup(function(event){
		if(event.keyCode == 13) {
			var img = $(this).parent().children("img");
			
			$(this).removeClass("unsubmitted");
			img.attr("src", getStrippedImgSrc(img.attr("id")) + "loading.gif");
			
			$(".idField").val($(this).parent().children(".uuidBox").text()).change();
			$(".jsIdField").val(img.attr("id")).change();
			$(".typeField").val("comment");
			
			// This one triggers the update
			$(".pointsField").val($(this).val()).change();
		}
	});
});

function prefetchComments(value) {
	// Prefetch the next one as well, so that it's ready when they need it.
	if($(value).length > 0 && $(value).find(".replaceWithComments").children().length == 0) {
		$(value).find(".replaceWithComments").load($(value).find(".commentsLink").attr("href"), makeButtons);
	}
}

function makeButtons() {
	if (!(navigator.userAgent.indexOf("Firefox/2.") > 0)) {
	    $('.usebutton').button({text:true});
	} else {
	    // fake it; can't seem to get rid of underline though
	    $('.usebutton').css('border', '1px solid black').css('padding', '1px 4px').css('color', 'black');
	}
}