// jQuery File Tree Plugin
//
// Version 1.01
//
// Cory S.N. LaViska
// A Beautiful Site (http://abeautifulsite.net/)
// 24 March 2008
//
// Visit http://abeautifulsite.net/notebook.php?article=58 for more information
//
// Usage: $('.fileTreeDemo').fileTree( options, callback )
//
// Options:  root           - root folder to display; default = /
//           script         - location of the serverside AJAX file to use; default = jqueryFileTree.php
//           folderEvent    - event to trigger expand/collapse; default = click
//           expandSpeed    - default = 500 (ms); use -1 for no animation
//           collapseSpeed  - default = 500 (ms); use -1 for no animation
//           expandEasing   - easing function to use on expand (optional)
//           collapseEasing - easing function to use on collapse (optional)
//           multiFolder    - whether or not to limit the browser to one subfolder at a time
//           loadMessage    - Message to display while initial tree loads (can be HTML)
//
// History:
//
// 1.01 - updated to work with foreign characters in directory/file names (12 April 2008)
// 1.00 - released (24 March 2008)
//
// TERMS OF USE
// 
// jQuery File Tree is licensed under a Creative Commons License and is copyrighted (C)2008 by Cory S.N. LaViska.
// For details, visit http://creativecommons.org/licenses/by/3.0/us/
//
(function($){
	
	$.extend($.fn, {
		fileTree: function(o, h) {
			// Defaults
			if( !o || o === undefined ) {o = {};}
			if( o.root === undefined ) {o.root = '/';}
			if( o.script === undefined ) {o.script = 'jqueryFileTree.php';}
			if( o.folderEvent === undefined ) {o.folderEvent = 'click';}
			if( o.expandSpeed === undefined ) {o.expandSpeed= 500;}
			if( o.collapseSpeed === undefined ) {o.collapseSpeed= 500;}
			if( o.expandEasing === undefined ) {o.expandEasing = null;}
			if( o.collapseEasing === undefined ) {o.collapseEasing = null;}
			if( o.multiFolder === undefined ) {o.multiFolder = true;}
			if( o.loadMessage === undefined ) {o.loadMessage = 'Loading...';}
			
			$(this).each( function() {
				
				function showTree(c, t) {
					$(c).addClass('wait');
					$(".jqueryFileTree.start").remove();
					$.post(o.script, { dir: t }, function(data) {
						$(c).find('.start').html('');
						$(c).removeClass('wait').append(data);
						if( o.root == t ) {$(c).find('UL:hidden').show();} else {$(c).find('UL:hidden').slideDown({ duration: o.expandSpeed, easing: o.expandEasing });}
						bindTree(c);
					});
				}
				
				function bindTree(t) {
					$(t).find('LI A').bind(o.folderEvent, function() {
						//if( $(this).parent().hasClass('directory') ) {
            if( $(this).parent().is('.directory') ) {
							//if( $(this).parent().hasClass('collapsed') ) {
              if( $(this).parent().is('.collapsed') ) {
								// Expand
								if( !o.multiFolder ) {
									$(this).parent().parent().find('UL:first').slideUp({ duration: o.collapseSpeed, easing: o.collapseEasing });
									$(this).parent().parent().find('LI.directory').removeClass('expanded').addClass('collapsed');
								}
								if($(this).parent().children().size() <= 2) {
					                // load new content
					                $(this).parent().find('UL:first').remove(); // cleanup
					                //showTree( $(this).parent(), escape($(this).attr('rel').match( /.*\// )) );
					                showTree( $(this).parent(), $(this).attr('rel').match( /.*\// )[0] );
								}else{
									// show existing content
					                $(this).parent().find('UL:first').slideDown({ duration: o.expandSpeed, easing: o.expandEasing });
								}
								$(this).parent().removeClass('collapsed').addClass('expanded');
							} else {
								// Collapse
								$(this).parent().find('UL:first').slideUp({ duration: o.collapseSpeed, easing: o.collapseEasing });
								$(this).parent().removeClass('expanded').addClass('collapsed');
							}
						} else {
							h($(this).attr('rel'));
						}
						return false;
					});
					// Prevent A from triggering the # on non-click events
					if( o.folderEvent.toLowerCase != 'click' ) {$(t).find('LI A').bind('click', function() { return false; });}
				}
				// Loading message
				$(this).html('<ul class="jqueryFileTree start"><li class="wait">' + o.loadMessage + '<li></ul>');
				// Get the initial file list
				//showTree( $(this), escape(o.root) );
				showTree( $(this), o.root );
			});
		}
	});
	
})(jQuery);
