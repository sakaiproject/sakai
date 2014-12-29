/*
 * TwinPeaks common scripts
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
//		alert("window=" + window);
// 		alert("window.opener=" + window.opener);

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
//	alert("window.opener.document=" + typeof window.opener.document);
//	try {alert("window.opener.document.htmlareas=" + typeof window.opener.document.htmlareas);} catch (e) {}

	 	if ((!window.opener.document) || (!window.opener.document.htmlareas) || (window.opener.document.htmlareas.length == 0))
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
  if (SRC_findResourceSearchBase().inTextMode()) {
    alert("Unable to add this link (the editor is in text mode)");
    return false;
  }

  if (!SRC_findResourceSearchBase().inEditArea()) {
    alert("Unable to add this link (cursor focus is not in an edit area)");
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
	window.defaultStatus = ""
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
 * Find the base (or first established) ResourceSearch object for this page
 */
function SRC_findResourceSearchBase()
{
 	return window.opener.document.htmlareas[0][1].__ResourceSearch;
}

/*
 * Find the unique ResourceSearch instance for the current edit frame
 */
function SRC_findResourceSearchInstance()
{
	var resourceSearchBase;
	/*
	 * Find the unique editor instance for the selected text area
	 */
 	resourceSearchBase = SRC_findResourceSearchBase();
  return resourceSearchBase.findResourceSearch();
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
 * occasionally necessary - see the Goolge Scholar implementation)
 */
 function __SRC_makeCitationAnchor(server, params, anchorText, openInNewWindow)
 {
  var doubleQuoteRE 	= /&quot;/g

 	var prefix					= "<A href=\"javascript:var a='"
	 										+ server
	 										+	"'+escape('"
	 										+	params
	 										+	"');"

	var suffix					= "\">"
                			+ anchorText.replace(doubleQuoteRE, "\"")
                			+ "</A>";

	/* An aside: if we use an "onClick" handler here, we wind
   * up with a link that is functional *even in the editor*.
 	 */
 	if (openInNewWindow)
 	{
 		/*
 		 * Open the citation in a new window
 		 */
	 	newAnchor  	=	prefix
	 							+ "var w=window.open(a,'__RS_Window__','"
                + "height=420,width=700,toolbar,status,scrollbars,resizable"
                + "');w.focus();"
                + suffix;
  }
  else
  {
  	/*
  	 * Open in the current window
  	 */
	 	newAnchor   = prefix
	 							+ "window.location.href=a;"
    						+ suffix;
	}
  return newAnchor;
}

/*
 * Add citation HTML to the current edit area
 *
 * 	href = anchor URL
 *	anchorText = Anchor text
 *	paramsForEscape = parameters to be escaped via JavaScript at "click time"
 *	alternateText = alternate version of anchor text (this will be persisted)
 *	openInNewWindow = boolean: open page in a new window?
 */
function SRC_addCitation(href, anchorText, paramsForEscape, alternateText, openInNewWindow)
{
	/*
	 * Make sure we can access our parent window (and that we're in an edit area)
	 */
	if (!SRC_verifyWindowAccess() || !SRC_verifyEditArea())
	{
		return false;
	}
  /*
   * SearchResultCommon.js
   * Set up the citation anchor and add it to the edit frame
   */
  try {
  	var	persistentText	= anchorText;
  	var server					= href;
  	var params					= "";
	  var newAnchor;
		/*
		 * Pick up [optional] persistent anchor text and parameters for encoding
		 */
		if (alternateText) {
			persistentText = alternateText;
		}

		if (paramsForEscape) {
			params = paramsForEscape;
		}
		/*
		 * Format the anchor and insert it into the edit area
		 */
	  newAnchor = __SRC_makeCitationAnchor(server, params, persistentText, openInNewWindow);
    SRC_findResourceSearchInstance().insertAnchorHTML(newAnchor);

  } catch (exception)  {
    if (confirm(exception + "\r\n\r\n"
        +       "Failed to add further information to your page\r\n\r\n"
        +       "         Close this search window?"))
    {
      window.close();
    }

  } finally  {
    return false;
  }
}
