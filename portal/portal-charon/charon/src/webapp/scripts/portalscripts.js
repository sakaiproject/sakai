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
	$('#portalMask').bgiframe();
}

function removeDHTMLMask() {
	$('#portalMask').remove();
}

/* Copyright (c) 2006 Brandon Aaron (http://brandonaaron.net)
 * Dual licensed under the MIT (http://www.opensource.org/licenses/mit-license.php) 
 * and GPL (http://www.opensource.org/licenses/gpl-license.php) licenses.
 *
 * $LastChangedDate$
 * $Rev$
 *
 * Version 2.1.1
 */
(function($){$.fn.bgIframe=$.fn.bgiframe=function(s){if($.browser.msie&&/6.0/.test(navigator.userAgent)){s=$.extend({top:'auto',left:'auto',width:'auto',height:'auto',opacity:true,src:'javascript:false;'},s||{});var prop=function(n){return n&&n.constructor==Number?n+'px':n;},html='<iframe class="bgiframe"frameborder="0"tabindex="-1"src="'+s.src+'"'+'style="display:block;position:absolute;z-index:-1;'+(s.opacity!==false?'filter:Alpha(Opacity=\'0\');':'')+'top:'+(s.top=='auto'?'expression(((parseInt(this.parentNode.currentStyle.borderTopWidth)||0)*-1)+\'px\')':prop(s.top))+';'+'left:'+(s.left=='auto'?'expression(((parseInt(this.parentNode.currentStyle.borderLeftWidth)||0)*-1)+\'px\')':prop(s.left))+';'+'width:'+(s.width=='auto'?'expression(this.parentNode.offsetWidth+\'px\')':prop(s.width))+';'+'height:'+(s.height=='auto'?'expression(this.parentNode.offsetHeight+\'px\')':prop(s.height))+';'+'"/>';return this.each(function(){if($('> iframe.bgiframe',this).length==0)this.insertBefore(document.createElement(html),this.firstChild);});}return this;};})(jQuery);