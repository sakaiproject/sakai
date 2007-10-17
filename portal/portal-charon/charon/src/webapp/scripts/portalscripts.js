/* Script for pop-up dhtml more tabs implementation 
 * uses jQuery library
 */

var dhtml_more_tabs = function() {
	//initialize the more tabs area then reset the function to just the show/hide behavior
	$('div#selectNav').appendTo('#linkNav').addClass('dhtml_more_tabs');
	$('div#selectNav').css('top',$('#linkNav').height() - 3); // adjust the vertical position
	$('div#selectNav').width($('#linkNav').width()*0.75); // fixes an IE6 bug
	dhtml_more_tabs = function() {
		if ($('#selectNav').css('display') == 'none' ) {
			$('div#selectNav').show();
			// highlight the more tab
			$('.more-tab').addClass('more-active');
			// dim the current tab
			$('.selectedTab').addClass('tab-dim');
			// mask the rest of the page
			createDHTMLMask() ;
			$('.more-tab').css('z-index',9800);
			$('#selectNav').css('z-index',9900);
			$('.selectedTab').bind('click',function(){dhtml_more_tabs();return false;});
		} else {
			// unhighlight the more tab
			$('.more-tab').removeClass('more-active');
			// hide the dropdown
			$('div#selectNav').hide(); // hide the box
			//undim the currently selected tab
			$('.selectedTab').removeClass('tab-dim');
			removeDHTMLMask()
			$('.selectedTab').unbind('click');
		}
	}
	dhtml_more_tabs();
}

function createDHTMLMask() {
	$('body').append('<div id="portalMask">&nbsp;</div>');
	$('#portalMask').css('height',browserSafeDocHeight()).css('width','100%').css('z-index',1000).bind("click",function(event){
		dhtml_more_tabs();
		return false;
	});
}

function removeDHTMLMask() {
	$('#portalMask').remove();
}
