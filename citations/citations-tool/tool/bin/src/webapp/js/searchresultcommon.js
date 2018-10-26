/*
 * Editor search servlet - common scripts
 *
 * Verify access to our parent window and the HTML editor
 */
function SRC_verifyWindowAccess()
 {
 	try
 	{
	  /*
	   * Done if the edit window is closed
	   */
	  if (window.opener.closed)
	  {
	    if (confirm("The editor window has been closed\r\n\r\n"
	        +       "    Close this search window?"))
	    {
	      window.close();
	    }
	    return false;
	  }
	  /*
	   * Done if there isn't an HTML editor associated with this window (how?)
	   */
	 	if ((!window.opener.top.document) ||
	 			(!window.opener.top.document.__editorareas) ||
	 			(window.opener.top.document.__editorareas.length == 0))
		{
	    if (confirm("There is no HTML editor associated with this window\r\n\r\n"
	        +       "          Close this search window?"))
	    {
	      window.close();
	    }
	    return false;
	  }
	}
	catch (exception)
	{
    if (confirm("Unable to access an HTML editor for this window\r\n\r\n"
        +       "            Close this search window?"))
    {
      window.close();
    }
    return false;
	}
	return true;
}

/*
 * Verify our parent window is still open and accessible
 */
function SRC_verifyWindowOpener()
{
 	SRC_clearTimedWindowVerification();

  if ((!window.opener) 				||
  		(window.opener.closed) 	||
  		(typeof window.opener.document == "unknown"))
  {
    SRC_closedWindowOpenerAlert();
    return;
	}
	SRC_timedWindowVerification();
}

/*
 * We can't access our parent - flag window "disconnected", disable buttons
 */
function SRC_closedWindowOpenerAlert()
{
  var element;

  top.document.__disabled = "true";

  element = document.getElementById("editor-integration-disconnect");
  if (element != null)
  {
    element.style.display = "inline";
    SRC_disableAddButtons();
    return
  }

	if (confirm("This Library Resource Search window can no longer access the HTML editor\r\n\r\n"
			+       "                   Close the search window?"))
  {
    window.close();
  }
}

/*
 * Verify that the editor is ready
 */
function SRC_verifyEditArea()
{
	var FCK_EDITMODE_WYSIWYG	= 0;
	var editor 								= SRC_findEditorInstance();

	if (!(editor.EditMode == FCK_EDITMODE_WYSIWYG))
	{
    alert("Unable to add this link (the editor is in text mode)");
    return false;
  }

  return true;
}

/*
 * Inactive page handling
 *
 * Save the form and button names - these will be disabled
 */
var __SRC_resultsForm = null;
var __SRC_buttonLabel = null;

function SRC_initializePageInfo(name, label)
{
  __SRC_resultsForm = name;
  __SRC_buttonLabel = label;
}

/*
 * Disable any "add citation" buttons on the results page
 */
function SRC_disableAddButtons()
{
  var formElement, inputList;

  if ((__SRC_resultsForm == null) || (__SRC_buttonLabel == null)) return;

  formElement = document.getElementById(__SRC_resultsForm);
  if (formElement == null)
  {
    return;
  }

  inputList = formElement.getElementsByTagName("input");
  for (var i = 0; i < inputList.length; i++)
  {
    var input = inputList[i];

    if ((input.type == "button") && (input.value == __SRC_buttonLabel))
    {
      input.disabled = true;
    }
  }
}

/*
 * Verify window access in a timed fashion
 */
function SRC_timedWindowVerification()
{
	SRC_clearTimedWindowVerification();
	document.__verificationTimerId = setTimeout(function()
																							{
																								SRC_verifyWindowOpener();
																							}, 1000);
}

/*
 * Cancel the window verification timer event
 */
function SRC_clearTimedWindowVerification()
{
	if (document.__verificationTimerId)
	{
		clearTimeout(document.__verificationTimerId);
		document.__verificationTimerId = null;
	}
}

/*
 * Null (or empty) string?
 */
 function SRC_isNull(text)
 {
 	var notSpaceRE = /\S/

 	if ((text == null) || (text == ""))
 	{
 		return true;
 	}
 	return text.search(notSpaceRE) == -1;
}

/*
 * Find the base (or first established) FCK editor API instance
 */
function SRC_findBaseFCKEditorApi()
{
 	return window.opener.top.document.__editorareas[0];
}

/*
 * Find the base (or first established) ResourceSearch plugin instance
 */
function SRC_findBaseResourceSearchCommand()
{
  var editorApi = SRC_findBaseFCKEditorApi();

 	return editorApi.__resourceSearch;
}

/*
 * Find the FCK editor API instance for the current edit frame
 */
function SRC_findEditorInstance()
{
	var resourceSearchBase;
	/*
	 * Find the unique editor instance for the selected text area
	 */
 	resourceSearchBase = SRC_findBaseResourceSearchCommand();

  return resourceSearchBase.findEditorInstance();
 }

/*
 * Find the Resource Search editor plugin for the current editor frame
 */
function SRC_findResourceSearchInstance()
{
  return SRC_findEditorInstance().__resourceSearch;
}

/*
 * Generate an anchor for the citations page.  This is in the general form:
 *
 *    <html wrapper><anchor>[<br/>[authors][ ][sources]]</html wrapper>
 *
 * The "wrapper" encloses the anchor and citation details - this establishes a
 * single HTML component that can be manipulated as one object in the editor.
 *
 * href           -- The OpenURL
 * anchorText     -- The clickable text for this anchor
 * creatorText    -- Authors [optional]
 * sourceText     -- Source [optional]
 */
function __SRC_makeCitationAnchor(href, anchorText, creatorText, sourceText)
{
  var doubleQuoteRE 	= /&quot;/g

  var wrapperPrefix   = "<span><br/>";
  var wrapperSuffix   = "<br/></span>";

	var citationText;
	var newAnchor;
  /*
   * Set up the anchor
   */
  newAnchor	= "<a href=\""
	 					+ href
	 					+	"\">"
						+ anchorText.replace(doubleQuoteRE, "\"")
   					+ "</a>";
  /*
   * Finish now if no citation details are available
   */
  if (SRC_isNull(creatorText) && SRC_isNull(sourceText))
  {
    return wrapperPrefix + newAnchor + wrapperSuffix;
  }
  /*
   * Add the citation: <br/>[authors][source]
   */
  citationText = "<br/>";

  if (!SRC_isNull(creatorText))
  {
    citationText += creatorText.replace(doubleQuoteRE, "\"");

  	if (!SRC_isNull(sourceText))
	  {
      citationText += " ";
    }
	}

	if (!SRC_isNull(sourceText))
	{
	  citationText += sourceText.replace(doubleQuoteRE, "\"");
	}

  return wrapperPrefix + newAnchor + citationText + wrapperSuffix;
}

/*
 * Add citation HTML to the current edit area
 *
 * 	href = anchor URL
 *	anchorText = Anchor text
 *	citationText = additional citation details
 */
function SRC_addCitation(href, anchorText, creatorText, sourceText)
{
	/*
	 * Make sure we can access our parent window (and that we're in an edit area)
	 */
	if (!SRC_verifyWindowAccess() || !SRC_verifyEditArea())
	{
		return false;
	}
  /*
   * Set up the citation anchor and add it to the edit frame
   */
  try
  {
    var editorApi = SRC_findEditorInstance();
	  var newAnchor;

		/*
		 * Format the anchor and insert it into the edit area.
		 *
		 * We use GetHTML() to serialize the InsertHTML() activities (which take place
		 * asynchronously in the edit window) and the window.focus() call (which should
		 * happen only after insertHTML() is completely finished).
		 */
    newAnchor = __SRC_makeCitationAnchor(href, anchorText, creatorText, sourceText);

    editorApi.InsertHtml(newAnchor);
    editorApi.GetHTML();

    window.focus();
  }
  catch (exception)
  {
    if (confirm(exception + "\r\n\r\n"
        +       "Failed to add further information to your page\r\n\r\n"
        +       "         Close this search window?"))
    {
      window.close();
    }
  }
  finally
  {
    return false;
  }
}
