/*
 * Twin Peaks Library Resource Search for HTMLArea-3.0
 *
 * DOM Node types
 */
__RS_DOM_ELEMENT_NODE	= 1;										// Node.ELEMENT_NODE
__RS_DOM_TEXT_NODE		= 3;										// Node.TEXT_NODE
__RS_DOM_FRAGMENT			= 11;										// Node.FRAGMENT_NODE
/*
 * Offsets into the document.htmlareas array entries
 */
__RS_HTMLAREA_ID			= 0;
__RS_HTMLAREA_EDITOR	= 1;
__RS_HTMLAREA_HIDDEN	= 2;

function ResourceSearch(editor) {
	var config					= editor.config;
	var toolTip					= ResourceSearch.I18N;
	var buttonList			= ResourceSearch.btnList;
	var self 						= this;
	var toolbar 				= [];

	/*
	 * Save the HTMLArea instance and let the editor know about this plugin
	 */
	this.editor = editor;
	editor.__ResourceSearch	= this;

	ResourceSearch._pluginInfo.name = "ResourceSearch" + document.htmlareas.length;
  /*
   * Mark this editor instance as the active edit area
   */
	this.active = false;
	/*
	 * Register the toolbar buttons provided by this plugin
	 */
	for (var i in buttonList)
	{
		var button = buttonList[i];
		var id;

		if (!button) {
			toolbar.push("separator");
			continue;
		}

		id = "RS-" + button[0];
		config.registerButton(id,
													toolTip[id],
											 	 	editor.imgURL(button[0] + ".gif", "ResourceSearch"),
											 		false,
				   						 		function(editor, id) { self.buttonPress(editor, id); },
				   						 		button[1]);	// undefined
		toolbar.push(id);
	}

	for (var i in toolbar)
	{
		config.toolbar[1].push(toolbar[i]);
	}
};

/*
 * Plugin details
 *
 * "name" is givien a unique value at editor generate() time.  See
 * onGenerateOnce() for details.
 */
ResourceSearch._pluginInfo =
{
	name          : "",
	version       : "1.0",
	developer     : "Indiana University Library Information Technology",
	developer_url : "http://www.libraries.iub.edu/",
	c_owner       : "",
	sponsor       : "",
	sponsor_url   : "",
	license       : "htmlArea"
};

/*
 * Our custom buttons (use null to insert a vertical bar separator)
 */
ResourceSearch.btnList =
[
	null,
	["resource-search"]
];

/*
 * Event handlers
 *
 * Called on intial HTMLArea editor generation only.
 */
ResourceSearch.prototype.onGenerateOnce = function() {
	var func = this.editorEvent;
	/*
	 * Establish a local handler for "mouseup" events.
	 */
	HTMLArea._addEvents(this.editor._doc, ["mouseup"], func);
}
/*
 * Called whenever the HTMLArea editor is generated.  We make sure an edit
 * area is "active" (this to permit additions from an already opened search
 * window).  An alternative approach is to simply prohibit additions until
 * our user manually selects a window - at present, this seems like the less
 * attractive approach.
 */
ResourceSearch.prototype.onGenerate = function() {

  for (var i = 0; i < document.htmlareas.length; i++)
  {
  	var editor = document.htmlareas[i][__RS_HTMLAREA_EDITOR];

  	if (editor.__ResourceSearch.isActive()) {
 			return;
 		}
	}
	this.setActive(true);
}
/*
 * Handle HTMLArea key press activity
 */
ResourceSearch.prototype.onKeyPress = function(event) {
}
/*
 * Handle tool bar refresh
 */
ResourceSearch.prototype.onUpdateToolbar = function() {
}
/*
 * Handle "mouseup" event
 */
ResourceSearch.prototype.editorEvent = function(event) {
	ResourceSearch.markActive(this);
}

/*
 * Set the "active instance" flag (boolean)
 */
ResourceSearch.prototype.setActive = function(state)
{
	this.active = state;
}
/*
 * Is this the "active" ResourceSearch instance?
 */
ResourceSearch.prototype.isActive = function()
{
	return this.active;
}
/*
 * Fetch the unique search window title for this ResourceSearch instance
 */
ResourceSearch.prototype.getWindowTitle = function()
{
	return "__ResourceSearch__";
}
/*
 * Mark as "active" the ResourceSearch instance with cursor focus
 */
ResourceSearch.markActive = function(documentElement)
{
  for (var i = 0; i < document.htmlareas.length; i++)
  {
  	var editor = document.htmlareas[i][__RS_HTMLAREA_EDITOR];

  	editor.__ResourceSearch.setActive(documentElement == editor._doc);
	}
}

/*
 * Dispatch on an HTMLArea toolbar button press event
 */
ResourceSearch.prototype.buttonPress = function(editor, id) {
	switch (id)
	{
	  case "RS-resource-search":
			var newWindow;
			var attributes;
			var url;
	  	/*
	  	 * Launch a new search window
	  	 */
			attributes 	= "height=420, width=700, toolbar, status, scrollbars, resizable";
			url = "/sakai-osid-repo-test/search?initialQuery=true&cssFile="
									+ getStyleSheet();
			/*
			url 				= "http:/resource-search/search?initialQuery=true&cssFile="
									+ getStyleSheet();
			*/
			editor.focusEditor();
			newWindow = window.open(url,
															editor.__ResourceSearch.getWindowTitle(),
															attributes);
			newWindow.focus();

			ResourceSearch.markActive(editor._doc);
			break;

		default:
			alert('WARNING: Unknown button event ID: "' + id + '"');
			break;
	}
};

/*
 * Insert an anchor (the provided HTML text)
 *
 * Essentially, this is a replacement for HTMLArea.insertHTML() - we use it
 * for "non-Internet Explorer" browsers.
 */
ResourceSearch.prototype.insertAnchorHTML = function(html)
{
	var editor = this.editor;
	/*
	 * IE uses the HTMLArea support
	 */
  if (HTMLArea.is_ie) {
	  editor.insertHTML(html);
	  return;
  }
	/*
	 * Construct a new document fragment with the given HTML
   */
  var fragment  = editor._doc.createDocumentFragment();
	var div       = editor._doc.createElement("div");

	div.innerHTML = html;
  while (div.firstChild) {
		fragment.appendChild(div.firstChild);
	}
	/*
	 * Insert the HTML fragment into our document
	 */
	this.insertAnchorBeforeSelection(fragment);
};
/*
 * Insert an HTML anchor element before the the current cursor position.
 * Returns true on success.
 *
 * Avoid inserting "compound" structures:
 *
 *			<span><a href>xxx</a></span>
 */
ResourceSearch.prototype.insertAnchorBeforeSelection = function(fragment) {
	var editor;
	var selection, range;
  var node, offset;
  var insertionPoint;
  var done;

  /*
   * Not used with IE (throw an exception?)
   */
  if (HTMLArea.is_ie) {
    alert("ERROR: insertAnchorBeforeSelection() not supported for Internet Explorer");
    return false;
  }
  /*
   * We can only add document fragments (Node.FRAGMENT_NODE)
   */
  if (fragment.nodeType != __RS_DOM_FRAGMENT) {
    alert("ERROR: Not a fragment!  Node type: " + fragment.nodeType);
    return false;
  }
	/*
	 * Get selected text area
 	 */
	editor 		= this.editor;
	selection	= editor._getSelection();
	range     = editor._createRange(selection);
  /*
   * Remove any selected text
   *
	 * selection.removeAllRanges();
	 * range.deleteContents();
	 *
	 * Set initial insertion point and offset
   */
  node  	= range.startContainer;
	offset 	= range.startOffset;

  done = false;
  while (!done)
	{
		switch (node.nodeType) {
			/*
			 * An HTML tag - determine the location for text insertion
			 */
	    case __RS_DOM_ELEMENT_NODE:
	      if ((insertionPoint = node.childNodes[offset]) == null)
				{
		      if (node.nextSibling != null) {
		        insertionPoint = node.nextSibling;
		        node = node.parentNode;
		      }
		    	else
		    	{ /*
		    		 * We're at the end of the "page" - append the element
		    		 *
		    		 * alert("WARNING: Failed to determine text insertion point, HTML tag = " + node.tagName + ", node type = " + node.nodeType);
		    		 */
						insertionPoint = node;
						node = node.parentNode;
						node.insertBefore(fragment, null);
					  editor.selectNodeContents(node.lastChild);
						done = true;
		    	}
	      } else if (node.tagName == "A") {
					/*
					 * An anchor:
					 *   Insert our next text *before* the existing anchor if we're at
					 *   the beginning of the "page"
					 */
					if (node.prevSibling == null) {
						insertionPoint = node;
						node = node.parentNode;
						node.insertBefore(fragment, insertionPoint);
					  editor.selectNodeContents(node.childNodes[0]);
						done = true;
					} else {
						/*
						 * Insert new anchor text *after* the exisiting anchor
						 */
		        if (node.nextSibling != null) {
		          insertionPoint = node.nextSibling;
		          node = node.parentNode;
							// alert("insertion updated: " + insertionPoint + ", type = " + insertionPoint.nodeType);
		        }
		      }
	      }
	      break;

	    default:
	      break;
	  }

	  switch (node.nodeType) {
	    case __RS_DOM_ELEMENT_NODE:
	    {
	    	/*
	    	 * If we haven't done so already, insert the anchor and update
	    	 * the selected text to reflect the addition
	    	 */
				if (!done)
				{
					node.insertBefore(fragment, insertionPoint);
		    	editor.selectNodeContents(insertionPoint);
					done = true;
				}
	      break;
		  }
	    case __RS_DOM_TEXT_NODE:
	    	/*
	    	 * Is this the text portion of an anchor element?  If so, don't insert
	    	 * new HTML into that anchor text.
	    	 */
	    	if (node.parentNode.tagName == "A")
	    	{
	    		node = node.parentNode;
	    		continue;
	    	}
	    	/*
	    	 * Split text at cursor
	    	 */
				node = node.splitText(offset);

				node.parentNode.insertBefore(fragment, node);
			  editor.selectNodeContents(node);

				done = true;
				break;

	    default:
	      alert("ERROR: Unexpected node type " + node.nodeType);
	      return false;
	  }
	}
  /*
   * Update screen
   */
 	editor._getSelection().collapseToEnd();
  editor.forceRedraw();
  editor.updateToolbar();

  return true;
};

/*
 * Is the cursor in an editable text area?
 *
 * Internet Explorer allows text insertion at any location on the page.  We need to
 * make sure we're positioned in one of our edit areas.
 */
ResourceSearch.prototype.inEditArea = function()
{
	if (!HTMLArea.is_ie)
	{
		return true;
	}

  for (var i = 0; i < document.htmlareas.length; i++)
  {
  	if (inBody(document.htmlareas[i][__RS_HTMLAREA_EDITOR]))
  	{
  		return true;
  	}
  }
 	return false;
};

/*
 * Is the editor in text (not wysiwyg) mode?
 */
ResourceSearch.prototype.inTextMode = function()
{
  return this.editor._editMode == "textmode";
}

/*
 * Look up the unique ResourceSearch plugin instance for the selected textarea
 */
ResourceSearch.prototype.findResourceSearch = function()
{
	if (HTMLArea.is_ie)
	{
		return ieFindRS();
	}

  for (var i = 0; i < document.htmlareas.length; i++)
  {
  	var editor = document.htmlareas[i][__RS_HTMLAREA_EDITOR];

  	if (editor.__ResourceSearch.isActive())
  	{
  		return editor.__ResourceSearch;
  	}
	}
	/*
	 * No frame is active - use the first instance as a default
	 */
	alert("WARNINNG: No ResourceSearch plugin is marked as \"active\"");
	return document.htmlareas[0][__RS_HTMLAREA_EDITOR].__ResourceSearch;
}

/*
 * "local" functions
 */

/*
 * Look up the unique ResourceSearch plugin instance for the selected
 * textarea (IE specific)
 */
function ieFindRS()
{
  for (var i = 0; i < document.htmlareas.length; i++)
  {
  	var editor = document.htmlareas[i][__RS_HTMLAREA_EDITOR];

  	if (inBody(editor))
  	{
 			return editor.__ResourceSearch;
  	}
	}
	/*
	 * This case should have been caught by the inEditArea() test.  Note
	 * the error and use the first available ResourceSearch plugin
	 */
  alert("ERROR: No ResourceSearch object found for this reference");
 	return document.htmlareas[0][__RS_HTMLAREA_EDITOR].__ResearchSearch;
};

/*
 * Determine if the editor focus is within the body of one
 * of our edit documents.  Return true if so.
 */
function inBody(editor)
{
  var editArea  = editor._doc.body;
  var element   = editor.getParentElement();

  while (element) {
    if (element == editArea) {
       return true;
    }
    element = element.parentNode;
  }
  return false;
};

/*
 * Lookup our stylesheet
 */
function getStyleSheet()
{
	var css = document.getElementsByTagName("link");

	if (css.length == 0) {
		css = top.document.getElementsByTagName("link");
	}

	if (css.length == 0) {
		return "__cannot_locate_stylesheet_link__";
	}

	return css[0].href;
}