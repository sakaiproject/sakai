var LSNGRD = LSNGRD || {};

LSNGRD.childClass = ".uuidBox";
LSNGRD.type = "comment";

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
	LSNGRD.initButtonsAndPointBoxes();
	
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
						
						LSNGRD.makeButtons();
						
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
});

function prefetchComments(value) {
	// Prefetch the next one as well, so that it's ready when they need it.
	if($(value).length > 0 && $(value).find(".replaceWithComments").children().length === 0) {
		var href=$(value).find(".commentsLink").attr("href");
		var ci = href.indexOf("Comment");
		href = "/lessonbuilder-tool/faces/" + href.substring(ci);
		$(value).find(".replaceWithComments").load(href, LSNGRD.makeButtons);
	}
}
