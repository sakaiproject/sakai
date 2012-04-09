// Author: Charlie Groll <charlieg@rutgers.edu>

// UI Changes
// This is the js manipulation of the Pickers to load
// popups instead of new windows, preserving the user's
// workflow. Should only really be seen by professors.

var toclick;	// Keep track of the links LB item that opens a new window to refresh and resize the popup
var winW, winH;

winH = $(parent.document).height();
winW = $(parent.document).width();

var picker = picker(); // True if the popup should be displayed (if we're not in pda mode on a screen shorter than 600px)

var w = $(window).width() - 10;

$(window).resize()

$('#pickerdialog').dialog({
	autoOpen: false,
	minWidth: 600,
	maxWidth: w,
	draggable: false,
	position: ['left', 'top']
});

$('#ipickerdialog').dialog({
	autoOpen: false,
	minWidth: 600,
	width: w,
	maxWidth: w,
	resizable: true,
	draggable: false,
	position: ['left', 'top']
});

$('.add-forum-link, #change-forum').click(function(event){
	if (!picker) return true;	// default to previous workflow if on pda mode, where a new window opens to pick a new item

	var title = $(this).text();
	openDialog(title, false);

	toclick = '.add-forum-link';
	event.preventDefault();		// don't load the link in the portal iframe
	closeDropdown();	
	var pageToRequest = $(this).attr("href");
	loadpicker(pageToRequest);
	return false;
});

$('.add-assignment-link, #change-assignment').click(function(event){
	if (!picker) return true;

	var title = $(this).text();
	openDialog(title, false);
	
	toclick = '.add-assignment-link';
	event.preventDefault();
	closeDropdown();
	var pageToRequest = $(this).attr("href");
	loadpicker(pageToRequest);
	return false;
});

$('.add-quiz-link, #change-quiz').click(function(event){
	if (!picker) return true;

	var title = $(this).text();
	openDialog(title, false);

	toclick = '.add-quiz-link';
	event.preventDefault();
	closeDropdown();
	var pageToRequest = $(this).attr("href");
	loadpicker(pageToRequest);
	return false;
});


$('#mm-choose').click(function(){
	if (!picker) return true;

//	$('#add-multimedia-dialog').dialog('close');	

	event.preventDefault();

	var title = $(this).text();
	var pageToRequest = $(this).attr("href");
	openDialog(title, true, pageToRequest);
});

$('#subpage-choose, #change-page').click(function(){
	if (!picker) return true;

	event.preventDefault();

	var title = $(this).text();
	var pageToRequest = $(this).attr("href");
	$('#ipickerdialog').dialog('option', 'width', 700);
	openDialog(title, true, pageToRequest);
});

$('.add-text-link, .itemLink').click(function(){
	if (!picker) return true;

	event.preventDefault();

	var title = $(this).text();
	var pageToRequest = $(this).attr("href");
	$('#ipickerdialog').dialog('option', 'width', 850);
	openDialog(title, true, pageToRequest);
});

$('#edit-item-object, #edit-item-settings').click(function(){ // add #change-blti?
	if (!picker) return true;

	event.preventDefault();

	var title = $(this).text();
	var pageToRequest = $(this).attr("href");
	$('#ipickerdialog').dialog('option', 'width', 850);
	openDialog(title, true, pageToRequest);

});

// this will also be called by child pages to update the div
function loadpicker(address) {
	address += "&time=" + new Date().getTime(); // prevent caching in IE

	$('#pickerdiv').load(address);
	setTimeout(divsize, 250);
	setTimeout(divsize, 1000);
	setTimeout(divsize, 2000);
	return false;
}

// checks if we're in pda mode on a small screen to see if the popup should be loaded
// or if the original method of redirecting the frame should be used
function picker() {
        // allow site to choose whether to use this
	if ($('#newui').text() != 'true') return false;
	if (window == window.top) return false;
	return true;
}

// resize the div that most of the pickers use
function divsize() {
		var port = $('#pickerdiv .portletBody');
		
		var h = port.height();
		$('#pickerdialog').dialog('option', 'height', h + 75);
		checksize($('#pickerdialog'));
		return false;
}

// for the resources, an iframe is necessary. this resizes the iframe after something
// has been added to the resource list
// it uses the resize() function from jquery.ba-resize.js
function framesize() {
	var i = $('#pickerframe');
	i.load(function(){
		var contents = i.contents().find('body');

		contents.resize(function(){
			var e = $(this);
			var h = e.outerHeight(true) + 15;
			i.css({ height: h  });

			var h = i.contents().height();
			i.dialog('option', 'height', h + 60);
			checksize($('#ipickerdialog'));

		});

		contents.resize();
	});
	return false;
}


// this hides the picker frame or div (and resets the iframe's source)
// refresh is true if we need to refresh the LB page, otherwise we just hide the picker
function hidepicker(refresh) {
	if (refresh == null || !refresh) {
		$('#pickerdialog, #ipickerdialog').dialog('close');
		checksize();
	} else {
		window.location.reload();
	}
	return false;
}

function hideidialog() {
	$('#ipickerdialog').dialog('close');	
}

var intervalID = 0;
										
// when a window is opened to create a new forum/quiz/etc,
// we check constantly if that window (wind) has been closed
// and then we refresh the picker by clicking the link that loads it
function check() {
	var iopen = $('#ipickerdialog').dialog('isOpen');
	var open = $('#pickerdialog').dialog('isOpen');
	if (!iopen && open) {
		var alink;
		if (toclick == '.add-forum-link') {
			alink = $('#dropDownDiv').find(toclick);
		} else {
			alink = $('#toolbar').find(toclick);
		}
        alink.click();
        window.clearInterval(intervalID);
    }
	return false;
}

function refresh() {
	$(window).reload();
}

function openDialog(title, iframe, src, event) {
	if (!picker) return true;

	if (event && !$.browser.msie) {
		event.preventDefault();
	}

	$('#edit-item-dialog').dialog('close');

	$('div.ui-dialog:visible').dialog('close');
	hideMultimedia();
	w = $(window).width() - 10;
	if (!iframe){
		$('#pickerdialog').dialog('option','title', title);
		$('#pickerdialog').dialog('open');
		$('#pickerdialog').dialog({
			beforeClose: function(event, ui){ $('#pickerdialog').dialog('option', 'width', 600);  }
		 });
	} else {
		$('#ipickerdialog').html('<iframe id="pickerframe" width="100%" height="99%" marginWidth="0" marginHeight="0" frameBorder="0" scrolling="auto" />');
		$('#pickerframe').attr('src',src);
		$('#pickerframe').load();
		$('#ipickerdialog').dialog('option','title', title);
		$('#ipickerdialog').dialog('open');
		$('#ipickerdialog').dialog({
			beforeClose: function(event, ui){ $('#ipickerdialog').dialog('option', 'width', w);  }
		 });
	}

	if (iframe && event) {
		intervalID = setInterval('check()', 2000);
	}

	framesize();
	framesize();
	framesize();
	// The sizing doesn't always work on the last try for the main frame, so we check it after a few seconds and then again just in case their connection is slow
	setTimeout("checksize($('#ipickerdialog'))",5000);
	setTimeout("checksize($('#ipickerdialog'))",10000);

	return false;
}
