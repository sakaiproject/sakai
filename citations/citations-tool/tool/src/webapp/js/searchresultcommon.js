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
	 			(!window.opener.top.document.__htmlareas) ||
	 			(window.opener.top.document.__htmlareas.length == 0))
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
    setTimeout(function() { SRC_closeWindowOpener(); }, 4000);
    return;
	}
	SRC_timedWindowVerification();
}

/*
 * We can't access our parent - close on request
 */
function SRC_closeWindowOpener()
{
	if (confirm("This Resource Search window can no longer access the HTML editor\r\n\r\n"
			+       "                   Close the search window?"))
  {
    window.close();
  }
}

/*
 * Verify that:
 *  -- the editor is ready
 *  -- the cursor is positioned within an editor window
 */
function SRC_verifyEditArea()
{
	var FCK_EDITMODE_WYSIWYG	= 0;
	var editor 								= SRC_findResourceSearchInstance();

	if (!(editor.EditMode == FCK_EDITMODE_WYSIWYG))
	{
    alert("Unable to add this link (the editor is in text mode)");
    return false;
  }

  return true;
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


function SRC_timedStatus()
{
	SRC_clearTimedStatus();
	document.__cursorTimerId = setTimeout(function()
																				{
																					SRC_statusDefaults();
																				}, 10000);
}

function SRC_clearTimedStatus()
{
	if (document.__cursorTimerId)
	{
		clearTimeout(document.__cursorTimerId);
		document.__cursorTimerId = null;
	}
}

function SRC_statusSearching()
{
	window.defaultStatus = "Searching ...";
	SRC_timedStatus();
};

function SRC_statusDefaults()
{
	SRC_clearTimedStatus();
	window.defaultStatus = "";
};

/*
 * Do any required setup work for query form submission
 */
function SRC_formSetup(form)
{
	/*
	 * Set up the "searching" display and approve the submit
	 */
	SRC_statusSearching();
	return true;
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
 	return window.opener.top.document.__htmlareas[0];
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
function SRC_findResourceSearchInstance()
{
	var resourceSearchBase;
	/*
	 * Find the unique editor instance for the selected text area
	 */
 	resourceSearchBase = SRC_findBaseResourceSearchCommand();

  return resourceSearchBase.findEditorInstance();
 }

/*
 * Generate an anchor for the citation page.  This is of the general form:
 *
 * Constant prefix:
 *		<A href="javascript:var a='URL?parameters'+escape('parameters-to-escape');
 *
 * Open new page (inline or new window):
 *		... open URL described by variable A ...
 *
 * Constant suffix:
 *		">Anchor text with &quot; entities converted to \"</A>
 *
 * The text passed to the JavaScript escape() function allows us to "re-escape"
 * parameter values that were unescaped by the HTML editor (this is
 * occasionally necessary - see the IUCAT implementation)
 */
function __SRC_makeCitationAnchor(href, anchorText, creatorText, sourceText)
{
  var doubleQuoteRE 	= /&quot;/g

	var newAnchor   		= "<span><a href=\""
	 										+ href
	 										+	"\">"
										 	+ anchorText.replace(doubleQuoteRE, "\"")
   										+ "</a><br/>"
   										+ creatorText.replace(doubleQuoteRE, "\"")
   										+ " "
										 	+ sourceText.replace(doubleQuoteRE, "\"")
   										+ "</span>";

  return newAnchor;
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
	  var newAnchor;

		/*
		 * Format the anchor and insert it into the edit area
		 */
	  newAnchor = __SRC_makeCitationAnchor(href, anchorText, creatorText, sourceText);

    SRC_findResourceSearchInstance().InsertHtml(newAnchor);
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
