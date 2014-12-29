// This file is used by the access display of resources in the kernel.
$(document).ready(function(){
	resizeFrame(window);
	function resizeFrame(currentWindow) {
		if (currentWindow && currentWindow.parent && currentWindow.name != "") {
			var frame = currentWindow.parent.document.getElementById(currentWindow.name);
			if (frame) {
				var clientH = currentWindow.document.body.clientHeight + 10;
				$(frame).height(clientH);
				// This is because the web content tool puts an iframe inside an iframe
				// So we recurse up through them.
				resizeFrame(currentWindow.parent.window);
			}
		}
	}
	jQuery.fn.fadeToggle = function(speed, easing, callback){
		return this.animate({opacity: 'toggle'}, speed, easing, callback);
	};
	if ($('.textPanel').size() < 1){
		$('a#toggler').hide();
	}
	$('a#toggler').click(function(){
		$('.textPanel').fadeToggle('1000', '', 'resizeFrame');
	});
	$('.file a').each(function (i){
		$(this).addClass(getFileExtension($(this).attr('href')));
	});
	function getFileExtension(filename) {
		var ext = /^.+\.([^.]+)$/.exec(filename);
		return ext == null ? "" : ext[1].toLowerCase();
	}
});