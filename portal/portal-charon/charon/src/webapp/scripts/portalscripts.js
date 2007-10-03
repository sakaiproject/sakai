/* Script for pop-up dhtml more tabs implementation 
 * uses jQuery library
 */
function dhtml_more_tabs() {
	if ($('#selectNav').css('display') == 'none' ) {
		// if the selectNav element is not a child of siteNav then we need to init the page
		if ($('#selectNav').parent().attr('id') != 'siteNav') {
			// make the selectNav div a child of the linkNav element for better positioning
			$('div#selectNav').appendTo('#linkNav').addClass('dhtml_more_tabs');
		}
		// show the dropdown
		$('div#selectNav').css('top',$('#linkNav').height() - 3); // adjust the vertical position
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
	return false;
}

function createDHTMLMask() {
	$('#container').append('<div id="portalMask">&nbsp;</div>');
	$('#portalMask').css('height',browserSafeDocHeight()).css('width','100%').css('z-index',1000).bind("click",function(event){
		dhtml_more_tabs();
		return false;
	});
}

function removeDHTMLMask() {
	$('#portalMask').remove();
}
