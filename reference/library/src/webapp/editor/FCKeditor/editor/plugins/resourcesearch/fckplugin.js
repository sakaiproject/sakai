/*
 * ResourceSearchCommand
 *
 * Add library search functionality to the FCK editor
 */
var ResourceSearchCommand = function() { }

ResourceSearchCommand.prototype.Execute = function() { }

/*
 * Search command registration
*/
FCKCommands.RegisterCommand('ResourceSearch', ResourceSearchCommand);
/*
 * Set up our Search button (name and hover text)
 */
var RSC_searchButton = new FCKToolbarButton('ResourceSearch',
                                            FCKLang['DlgSearchTitle']);
/*
 * Add an icon for the button, and register the new command
 */
RSC_searchButton.IconPath = FCKConfig.PluginsPath + 'resourcesearch/book.gif';
FCKToolbarItems.RegisterItem('ResourceSearch', RSC_searchButton);

/*
 * Date
 */
var RSC_milliseconds = null;

/*
 * Button press activities:
 *
 * 	o Launch the search window
 *  o Save the editor and plugin instance
 *  o Set up event handlers to track the current editor window
 *
 * Note:  The base URL (see "url =" below) corresponds to the name specified
 *        in the project build procedure
 */
ResourceSearchCommand.Execute = function()
{
	var	activeEditorInstance;
	var editorApi;
	var popupWindow;
	var attributes;
	var url;

	editorApi	= window.FCK;

	attributes 	= "height=420, width=750, location=1, toolbar=1, status=1, menubar=1, scrollbars=1, resizable=1";

  	url         = getBaseUrl() + "/sakai.citation.editor.integration.helper?panel=Main&sakai_action=doIntegrationSearch&searchType=noSearch";

	/*
	 * New search window
	 */
	popupWindow = window.open(url,
	                          ResourceSearchCommand.getWindowTitle(),
														attributes);
	/*
	 * Save editor API instance
	 *
	 * This is accessed as window.opener.top.document.__editorareas[index] in the popup
	 */
	if (typeof top.document.__editorareas == "undefined")
	{
		top.document.__editorareas = new Array();
	}

	activeEditorInstance = top.document.__editorareas.length;

	for (var i = 0; i < activeEditorInstance; i++)
	{
	  var editorInstance  = top.document.__editorareas[i];

    /*
     * If this page has been loaded before, we need to overwrite the previously
     * saved (and now stale) editor instance
     */
	  if (editorInstance.Name == editorApi.Name)
	  {
	    activeEditorInstance = i;
	    break;
	  }
  }
  /*
   * Save this editor API instance (and this plugin)
   */
  editorApi.__resourceSearch = this;
	top.document.__editorareas[activeEditorInstance] = editorApi;
	/*
	 * Mark this API/plugin combination as "active"
   */
	flagActiveEditor(top.document.__editorareas[activeEditorInstance]);
  /*
   * Attach a focus event handler to track the active editor instance
   */
  editorApi.Events.AttachEvent('OnFocus', flagActiveEditor);
  /*
	 * Finally, give the popup window focus
   */
	popupWindow.focus();
}

/*
 * Return the button state
 */
ResourceSearchCommand.GetState = function()
{
	return FCK_TRISTATE_OFF;
}

/*
 * Return the current time as milliseconds
 */
ResourceSearchCommand.getDateAsMilliseconds = function()
{
  if (RSC_milliseconds == null)
  {
    RSC_milliseconds = Date.parse(new Date());
  }
	return RSC_milliseconds;
}

/*
 * Fetch the window title for the ResourceSearch search window
 */
ResourceSearchCommand.getWindowTitle = function()
{
  var title = "__ResourceSearchCommandWindow__"
            + ResourceSearchCommand.getDateAsMilliseconds()
            + "__";

  return title;
}

/*
 * Return the active FCKeditor instance
 */
ResourceSearchCommand.findEditorInstance = function()
{
  var editorCount = top.document.__editorareas.length;

	for (var i = 0; i < editorCount; i++)
	{
	  if (top.document.__editorareas[i].__active)
	  {
	    return top.document.__editorareas[i];
	  }
  }
  alert("WARNING: No active editor found, using base instance");
  return top.document.__editorareas[0];
}

/*
 * Helpers
 */

/*
 * Note the active FCKeditor instance
 */
function flagActiveEditor(editorInstance)
{
  var editorCount = top.document.__editorareas.length;

	for (var i = 0; i < editorCount; i++)
	{
	  top.document.__editorareas[i].__active = false;
  }
  editorInstance.__active = true;
}

/*
 * Lookup the current Tool id
 */
function getBaseUrl()
{
	var placementId = document.__pid;
	var baseUrl = document.__baseUrl;

	if((typeof baseUrl == "undefined") || (baseUrl.length == 0))
	{
		baseUrl = parent.document.__baseUrl;
		if((typeof baseUrl == "undefined") || (baseUrl.length == 0))
		{
			baseUrl = top.document.__baseUrl;
			if((typeof baseUrl == "undefined") || (baseUrl.length == 0))
			{
				if ((typeof placementId == "undefined") || (placementId.length == 0))
				{
					placementId = parent.document.__pid;
					if ((typeof placementId == "undefined") || (placementId.length == 0))
					{
						placementId = top.document.__pid;
					}
				}
				if ((typeof placementId == "undefined") || (placementId.length == 0))
				{
					return "__cannot_locate_sakai_placement_id__";
				}
				baseUrl = "/portal/tool/" + placementId;
			}
		}
	}
	var i = baseUrl.indexOf("?");
	if (i != -1)
	{
	  baseUrl = baseUrl.substring(0, i);
	}
	return baseUrl;
}
