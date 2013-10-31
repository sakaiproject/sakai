// if the containing frame is small, then offsetHeight is pretty good for all but ie/xp.
// ie/xp reports clientHeight == offsetHeight, but has a good scrollHeight
function mySetMainFrameHeight(id)
{
	// run the script only if this window's name matches the id parameter
	// this tells us that the iframe in parent by the name of 'id' is the one who spawned us
	if (typeof window.name != "undefined" && id != window.name) return;

	var frame = parent.document.getElementById(id);
	if (frame)
	{

		var objToResize = (frame.style) ? frame.style : frame;

		var height; 
		
		var scrollH = document.body.scrollHeight;
		var offsetH = document.body.offsetHeight;
		var clientH = document.body.clientHeight;
		var innerDocScrollH = null;

		if (typeof(frame.contentDocument) != 'undefined' || typeof(frame.contentWindow) != 'undefined')
		{
			// very special way to get the height from IE on Windows!
			// note that the above special way of testing for undefined variables is necessary for older browsers
			// (IE 5.5 Mac) to not choke on the undefined variables.
 			var innerDoc = (frame.contentDocument) ? frame.contentDocument : frame.contentWindow.document;
			innerDocScrollH = (innerDoc != null) ? innerDoc.body.scrollHeight : null;
		}

		if (document.all && innerDocScrollH != null)
		{
			// IE on Windows only
			height = innerDocScrollH;
		}
		else
		{
			// every other browser!
			height = offsetH;
		}

		// here we fudge to get a little bigger
		//gsilver: changing this from 50 to 10, and adding extra bottom padding to the portletBody		
		var newHeight = height + 150;
		//contributed patch from hedrick@rutgers.edu (for very long documents)
		if (newHeight > 32760)
		newHeight = 32760;

		// no need to be smaller than...
		//if (height < 200) height = 200;
		objToResize.height=newHeight + "px";
	
		var s = " scrollH: " + scrollH + " offsetH: " + offsetH + " clientH: " + clientH + " innerDocScrollH: " + innerDocScrollH + " Read height: " + height + " Set height to: " + newHeight;

	}
}

// MSGCNTR-53
$(document).ready(function(){
	  if ($('#displayWait').length<=0) {
		  $("body").append('<img id="displayWait" src="/messageforums-tool/images/wait_sakai.gif" width="50" height="50" style="display:none" />');
	  }
	  var loadUI = function() {
		  $.getScript("/library/js/jquery/blockUI/2.66/jquery.blockUI.js",
			function(){
			  $('input[type=submit]').click(function() {
		        $.blockUI({ 
		            message: $('#displayWait'), 
		            css: { 
		                top:  ($(window).height() - 50) /2 + 'px', 
		                left: ($(window).width() - 50) /2 + 'px', 
		                width: '50px' 
		            } 
		        }); 
		        setTimeout($.unblockUI, 10000); 			  
			  });
		    });
	  }
	  // I need UI js
	  if (!$.progressbar) {
		$.getScript("/library/js/jquery-ui-latest/js/jquery-ui.min.js",loadUI);
	  } else {
		loadUI();  
	  }
});

//This is the profile display on user's names.
$(document).ready(function() {			
	$('.authorProfile').each(function() {
		$(this).qtip({ 
			content: {text: msgs_js.loading_wait,
				url: $(this).attr('href'), title: {	text: msgs_js.cdfm_profile_information,button: '[ X ]' }
			},
			position: {	corner: {target: 'center', tooltip: 'leftMiddle'} },
			show: { when: 'click', solo: true, effect: {length:0} },
			hide: { when:'unfocus', fixed:true, delay: 300,  effect: {length:0} },
			style: { tip: true, border: {color:'#687E9C'}, name: 'light', width: 570 }
		});
		$(this).attr('href', 'javascript:;');
	});
});	
