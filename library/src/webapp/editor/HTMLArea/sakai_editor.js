/**********************************************************************************
*
* $Header: /cvs/sakai2/reference/library/src/webapp/htmlarea/sakai-htmlarea.js,v 1.3 2005/05/28 20:16:19 ggolden.umich.edu Exp $
*
***********************************************************************************
@license@
**********************************************************************************/

//formatted text editing widget within Sakai
//
// This Sakai-specific JavaScript does setup for using the HTMLArea JavaScript library.
//
// Usage: 
//
// This code should be included via a <script> tag in the document.
// Then either chef_setupformattedtextarea() or chef_setuphtmldocumenttextarea() should
// be called like:
// <script type="text/javascript" defer="1">chef_setupformattedtextarea("MyTextareaID", 640, 480);</script>

// a warning message to display to the user if their browser doesn't support
// the formatted text editing widget.
if (document.msg_formattedtextnotsupported)
{
	// the warning message was set outside of this JavaScript file
}
else
{
	document.msg_formattedtextnotsupported = "<br>The rich text editor is not supported by the browser you are using.<br>" 
		+ "Please see <a href='/library/htmlarea/reference.html#supportedbrowsers' target='_blank'>"
		+ "supported browsers</a> for more information.<br>";
}

// check whether the users browser supports the widget
// returns true if the browser is supported, false if the browser is not supported
// The only browsers that work are:
// 1. Internet Explorer 5.5 or greater on Windows
// 2. a recent version of gecko-based browser (Mozilla, Netscape, Firebird, Firefox)
function isHTMLAreaSupported()
{
	var agent = navigator.userAgent.toLowerCase();

	// Mac IE doesn't work		
	if ( /msie 5/i.test(navigator.userAgent) && /mac_/i.test(navigator.userAgent) && !/opera/i.test(navigator.userAgent) )
	{
		return false;
	}
	
	var is_ie = ((agent.indexOf("msie") != -1) && (agent.indexOf("opera") == -1));
	var is_gecko = (navigator.product == "Gecko");

	// Camino doesn't work
	if (/Camino/i.test(navigator.userAgent))
	{
		return false;
	}
	
	if (is_gecko) 
	{
		if (navigator.productSub < 20021201) 
		{
			//alert("You need at least Mozilla-1.3 Alpha.\n" +
			//      "Sorry, your Gecko is not supported.");
			return false;
		}
		if (navigator.productSub < 20030210) {
			//alert("Mozilla < 1.3 Beta is not supported!\n" +
			return false;
		}

	}
	
	return is_gecko || is_ie;
	
}; // function isHTMLAreaSupported()


if (!isHTMLAreaSupported())
{
	// output a warning message to tell the user that their browser doesn't support the widget
	document.write(document.msg_formattedtextnotsupported);
}
else
{
	// browser supports the editor, start setting it up
	if (document.htmlareas == undefined)
	{
		document.htmlareas = new Array();
	
		// This section will only be included ONCE per HTML document that contains formatted text editing widget(s)
		_editor_url = "/library/htmlarea/";
		_editor_lang = "en";
		
		// funky way to include the HTMLArea JavaScript library, from within JavaScript
		if(typeof HTMLArea == "undefined")
		{
			document.write('<script type="text/javascript" src="/library/htmlarea/htmlarea.js"></script>');
		}
			
		//if (fulldocumentediting)
		//{
		//	  document.write('<script type="text/javascript">HTMLArea.loadPlugin("TableOperations");</script>');
		//}
	}
}

// textarea_id - The HTML id of the HTML textarea 
// that should be turned into a fancy formatted text editing widget
function chef_setupformattedtextarea(textarea_id)
{
	chef_setupformattedtext(textarea_id, 600, 400, 2, false);
}

// textarea_id - The HTML id of the HTML textarea 
// that should be turned into a fancy formatted text editing widget
function chef_setupformattedtextarea(textarea_id, width, height, toolbar_num_button_rows)
{
	chef_setupformattedtext(textarea_id, width, height, toolbar_num_button_rows, false);
}

// textarea_id - The HTML id of the HTML textarea 
// that should be turned into a full-featured HTML document editing widget
function chef_setuphtmldocumenttextarea(textarea_id, width, height, toolbar_num_button_rows)
{
	chef_setupformattedtext(textarea_id, width, height, toolbar_num_button_rows, true);
}

function chef_setupformattedtext(textarea_id, width, height, toolbar_num_button_rows, fulldocumentediting)
{
	if (!isHTMLAreaSupported()) return;

	var counter = document.htmlareas.length;
	
	var config = new HTMLArea.Config();
	
	if (fulldocumentediting)
	{
		config.hideSomeButtons(" popupeditor ");
	}
	
	config.statusBar = false;
	config.sizeIncludesToolbar = true; // so that showing the toolbar doesn't make the document height increase
	config.killWordOnPaste = true;

	// config.pageStyle = "p { margin: 0px 0px 0px 0px; padding: 0px 0px 0px 0px; }"

	if (width) config.width = width+'px';
	if (height) config.height = height+'px';

	if (typeof toolbar_num_button_rows == 'undefined') 
	{
		toolbar_num_button_rows = 2;
	}

	if (toolbar_num_button_rows == 0)
	{ 
		config.toolbar = [];
	}	
	else if (toolbar_num_button_rows == 2)
	{
		config.toolbar = [
		[ "fontname", "space",
		  "fontsize", "space",
		  "forecolor", "space",
		  "bold", "italic", "underline", "strikethrough", "separator",
		  "subscript", "superscript", "separator",
		  "justifyleft", "justifycenter", "justifyright", "justifyfull", "separator"
		],
		[ 
		  "orderedlist", "unorderedlist", "outdent", "indent",
		  "htmlmode", "separator", 
		  "htmlmode", "separator", "createlink", "separator",
		  "popupeditor", "separator","showhelp", "about" ]
		];
	}
	else if (toolbar_num_button_rows == 3)
	{
        config.toolbar = 
        [
            ['fontname', 'space','fontsize', 'space','formatblock', 'space','bold', 'italic', 'underline'],
            ['separator','strikethrough', 'subscript', 'superscript', 'separator', 'copy', 'cut', 'paste', 'space', 'undo', 'redo', 'separator', 'justifyleft', 'justifycenter', 'justifyright', 'justifyfull', 'separator','outdent', 'indent'],
            ['separator','forecolor', 'hilitecolor', 'textindicator', 'separator','inserthorizontalrule', 'createlink', 'insertimage', 'inserttable', 'htmlmode', 'separator','popupeditor', 'separator', 'showhelp', 'about' ],
        ];         
	}

	
	// instantiate the widget
	var ta = HTMLArea.getElementById("textarea", textarea_id);
	var editor = new HTMLArea(ta, config);
		
	if (fulldocumentediting)
	{
		// register the TableOperations plugin
		//editor.registerPlugin(TableOperations);
	}
	
	// display the editing widget a bit later
	setTimeout(
		function() 
		{ 
			editor.generate(); 
			editor._iframe.style.border = "1px solid #000000";
		}, 500);
	
	document.htmlareas[counter] = new Array(textarea_id, editor, false);
}

function toggle_display_toolbar(textarea_id)
{
    // var delta=parseInt(editor._toolbar.offsetheight); //todo -  would be nice to determine height programmatically
	var delta = 49;
    
    var i;
    // find the editor object for the given textarea_id
    for (i=0; i<document.htmlareas.length; i++)
    {
    		if (document.htmlareas[i][0] == textarea_id)
    		{
    			// hide or show the editor
    			var editor = document.htmlareas[i][1];
    			var hidden = document.htmlareas[i][2];
    			var height = parseInt(editor._iframe.style.height);
    			
    			if (!hidden)
    			{
    				// hide it
    				editor._toolbar.style.display = "none";
    				editor._iframe.style.height = height + delta + "px";
    				document.htmlareas[i][2] = true;
    			}
    			else
    			{
    				// show it
      			editor._toolbar.style.display = "block";
    				editor._iframe.style.height = height - delta + "px";
    				document.htmlareas[i][2] = false;  			
    			}
    			
  			editor.focusEditor();
    		}		
    	}
}


/**********************************************************************************
*
* $Header: /cvs/sakai2/reference/library/src/webapp/htmlarea/sakai-htmlarea.js,v 1.3 2005/05/28 20:16:19 ggolden.umich.edu Exp $
*
**********************************************************************************/
