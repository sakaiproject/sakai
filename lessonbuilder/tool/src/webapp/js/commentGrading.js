// CommentsGradingPane is called by ajax from a URL like
// /lessonbuilder-tool/faces/CommentsGradingPane
// however URLs we generate need to be the usual
// /portal/pda/xxxx/tool/xxxx/ShowPage
// it's hard to get RSF to generate specified URLs, so
// we fix them up in javascript.

function fixurls() {
    
    var pageurl = window.location + "";
    var pi = pageurl.indexOf("/CommentsGradingPane");

    pageurl = pageurl.substring(0,pi);
    $('a[target="_lbcomments"]').each(function(index, value) {
	    var linkurl = $(this).attr('href');
	    var li = linkurl.indexOf("/faces/");
	    if (li >= 0) {
		linkurl = pageurl + linkurl.substring(li+6);
		$(this).attr('href',linkurl);
	    }
	});
}

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
				if($(value).find(".replaceWithComments").children().length === 0) {
					var oldSrc = $(firstRow).find(".toggleStatus").attr("src");
					oldSrc = oldSrc.replace("loading.gif", "");
					oldSrc = oldSrc.replace("no-status.png", "");
					
					$(firstRow).find(".toggleStatus").attr("src", oldSrc + "loading.gif");
					
					var href=$(value).find(".commentsLink").attr("href");
					var ci = href.indexOf("Comment");
					href = "/lessonbuilder-tool/faces/" + href.substring(ci);
					$(value).find(".replaceWithComments").load(href, function() {
						var current = $(value);
						var next = $(value).next().next();
						
						makeButtons();
						
						$(current).show();
						
						var oldSrc = $(firstRow).find(".toggleStatus").attr("src");
						oldSrc = oldSrc.replace("loading.gif", "");
						oldSrc = oldSrc.replace("no-status.png", "");
						
						$(firstRow).find(".toggleStatus").attr("src", oldSrc + "no-status.png");
						
						fixurls();

						setMainFrameHeight(window.name);
						prefetchComments(next);
					});
				}else {
					fixurls();
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
	
	$(".pointsBox").on('change', function(){
		var img = $(this).parent().children("img");
		img.attr("src", getStrippedImgSrc(img.attr("id")) + "no-status.png");
		$(this).addClass("unsubmitted");
	});
	
	// cr on individual box, update that box
	$(".pointsBox").keyup(function(event){
		if(event.keyCode === 13)
		    updateGrade($(this));
        });

	// update points button, do all the need it
	$("#clickToSubmit").click(function(event){
		updateGrades();
	});

	$("#zeroMissing").click(function(event){
		$("#zero").click();
	});


});

function updateGrades() {
    var unsubs = $(".unsubmitted");
    if (unsubs.length > 0) {
	// call back when finished submitted this one
	if (unsubs.length > 1)
	    setGradingDoneHook(updateGrades);
	else
	    setGradingDoneHook(null);	    
	updateGrade(unsubs.first());
    }
}


function updateGrade(item) {
    var img = item.parent().children("img");
    item.removeClass("unsubmitted");
    img.attr("src", getStrippedImgSrc(img.attr("id")) + "loading.gif");
			
    $(".idField").val(item.parent().children(".uuidBox").text()).change();
    $(".jsIdField").val(img.attr("id")).change();
    $(".typeField").val("comment");
			
    // This one triggers the update
    $(".pointsField").val(item.val()).change();
}

function prefetchComments(value) {
	// Prefetch the next one as well, so that it's ready when they need it.
	if($(value).length > 0 && $(value).find(".replaceWithComments").children().length === 0) {
		var href=$(value).find(".commentsLink").attr("href");
		var ci = href.indexOf("Comment");
		href = "/lessonbuilder-tool/faces/" + href.substring(ci);
		$(value).find(".replaceWithComments").load(href, makeButtons);
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