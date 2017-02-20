// CommentsGradingPane is called by ajax from a URL like
// /lessonbuilder-tool/faces/CommentsGradingPane
// however URLs we generate need to be the usual
// /portal/pda/xxxx/tool/xxxx/ShowPage
// it's hard to get RSF to generate specified URLs, so
// we fix them up in javascript.

function fixurls() {
    
    var pageurl = window.location + "";
    var pi = pageurl.indexOf("/QuestionsGradingPane");

    pageurl = pageurl.substring(0,pi);
    $('a[target="_lbquestions"]').each(function(index, value) {
	    var linkurl = $(this).attr('href');
	    var li = linkurl.indexOf("/faces/") + 6;
	    if (li >= 0) {
		linkurl = pageurl + linkurl.substring(li);
		$(this).attr('href',linkurl);
	    }
	});
}

$(function() {
	makeButtons();
	
	$.ajaxSetup ({
		cache: false
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
		$(".unsubmitted").each(function(index) {
			updateGrade($(this));
		    });
	});

	$("#zeroMissing").click(function(event){
		event.preventDefault();
		$("#zero").click();
	});

});

function updateGrade(item) {
    var img = item.parent().children("img");
    item.removeClass("unsubmitted");
    img.attr("src", getStrippedImgSrc(img.attr("id")) + "loading.gif");
			
    $(".idField").val(item.parent().children(".responseId").text()).change();
    $(".jsIdField").val(img.attr("id")).change();
    $(".typeField").val("question");
			
    // This one triggers the update
    $(".pointsField").val(item.val()).change();
}

function makeButtons() {
	if (!(navigator.userAgent.indexOf("Firefox/2.") > 0)) {
	    $('.usebutton').button({text:true});
	} else {
	    // fake it; can't seem to get rid of underline though
	    $('.usebutton').css('border', '1px solid black').css('padding', '1px 4px').css('color', 'black');
	}
}