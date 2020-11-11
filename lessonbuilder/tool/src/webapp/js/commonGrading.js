var LSNGRD = LSNGRD || {};

LSNGRD.initButtonsAndPointBoxes = function() {
	LSNGRD.makeButtons();

	$.ajaxSetup ({
		cache: false
	});

	LSNGRD.initPointBoxes();

	// update points button, do all that need it
	$("#clickToSubmit").click(function(event){
		LSNGRD.updateGrades();
	});

	$("#zeroMissing").click(function(event){
		event.preventDefault();
		$("#zero").click();
	});
};

LSNGRD.initPointBoxes = function() {
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
		if(event.keyCode === 13) {
			LSNGRD.updateGrade($(this));
		}
	});
};

LSNGRD.updateGrades = function() {
	var unsubs = $(".unsubmitted");
	if (unsubs.length > 0) {
	// call back when finished submitted this one
	if (unsubs.length > 1)
		setGradingDoneHook(LSNGRD.updateGrades);
	else
		setGradingDoneHook(null);
	LSNGRD.updateGrade(unsubs.first());
	}
};

LSNGRD.updateGrade = function(item) {
	var img = item.parent().children("img");
	item.removeClass("unsubmitted");
	img.attr("src", getStrippedImgSrc(img.attr("id")) + "loading.gif");

	$(".idField").val(item.parent().children(LSNGRD.childClass).text()).change();
	$(".jsIdField").val(img.attr("id")).change();
	$(".typeField").val(LSNGRD.type);

	// This one triggers the update
	$(".pointsField").val(item.val()).change();
};

LSNGRD.makeButtons = function() {
	if (!(navigator.userAgent.indexOf("Firefox/2.") > 0)) {
		$('.usebutton').button({text:true});
	} else {
		// fake it; can't seem to get rid of underline though
		$('.usebutton').css('border', '1px solid black').css('padding', '1px 4px').css('color', 'black');
	}
};


