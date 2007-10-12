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
 * Button press activities:
 *
 * 	o Launch the search window
 *  o Save the editor and plugin instance
 *  o Set up event handlers to track the current editor window
 *
 * Note:  The "helper" name (see "url =" below) corresponds to the name
 *        specified in the project build procedure
 */
ResourceSearchCommand.Execute = function()
{
	var editorApi = window.FCK;
	/*
	 * Global structures.  From the popup window, these are accessed as:
	 *
	 *   window.opener.top.document.__editorcommmon.property
	 *   window.opener.top.document.__editorareas[index]
	 */
	if (typeof top.document.__editorareas == "undefined")
	{
		top.document.__editorareas  = new Array();
		top.document.__editorcommon = new Object();
	}
  /*
   * Do we need to create the popup window?
   */
	if ((typeof top.document.__editorcommon.popupwindow == "undefined") ||
    	(top.document.__editorcommon.popupwindow.closed))
	{
  	var attributes 	= "height=420,width=750,location=1,toolbar=1,status=1,menubar=1,"
  	                + "scrollbars=1,resizable=1";

    var name        = "__Resource_Search_Window__";

    var url         = getBaseUrl()
                    + "/sakai.citation.editor.integration.helper?"
                    + "panel=Main&sakai_action=doIntegrationSearch&searchType=noSearch";
  	/*
  	 * New search window
  	 */
  	top.document.__editorcommon.popupwindow = window.open(url, name, attributes);
  }
	/*
	 * Save editor API instance
	 */
	activeEditorInstance = findEditorIndexByName(editorApi.Name);
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
	top.document.__editorcommon.popupwindow.focus();
}

/*
 * Return the button state
 */
ResourceSearchCommand.GetState = function()
{
	return FCK_TRISTATE_OFF;
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
 * Return the active FCKeditor instance
 */
function findEditorIndexByName(editorName)
{
	var activeEditorInstance = top.document.__editorareas.length;

	for (var i = 0; i < activeEditorInstance; i++)
	{
	  var editorInstance  = top.document.__editorareas[i];
    /*
     * If this page has been loaded before, we need to overwrite the previously
     * saved (and now stale) editor instance
     */
	  if (editorInstance.Name == editorName)
	  {
	    activeEditorInstance = i;
	    break;
	  }
  }
  return activeEditorInstance;
}

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
