/**********************************************************************************
* $URL$
* $Id$
* Formatted text editing widget within Sakai.
*
* This Sakai-specific JavaScript does setup for HTMLArea JavaScript library.
*
* Usage:
*
* This code should be included via a <script> tag in the document.
*
* should be called like this:
* <script type="text/javascript" defer="1">
*   sakaiSetupRichTextarea("MyTextareaID", 640, 480);
* </script>
*
*
*
***********************************************************************************
@license@
**********************************************************************************/
// the inputRichText directory
var inputRichTextDir = "/jsf-resource/inputRichText";

/**
* This is a private utility to set the language code.
* parameter: language a two character language code such as "en", "fr" etc.
*
*  Usage (from Java) something like:
*  writer.write("<script>sakaiSetLanguage(" + locale.language + ");</script>/n");
*/
function sakaiSetLanguage(language)
{
  _editor_lang = language;
}

// a warning message to display to the user if their browser doesn't support
// the formatted text editing widget.
if (document.msgFormattedTextNotSupported)
{
    // the warning message was set outside of this JavaScript file
}
else
{
    document.msgFormattedTextNotSupported =
      "<br>The rich text editor is not supported " +
      "by the browser you are using.<br>" +
      "Please see <a href='/" + inputRichTextDir +
      "/htmlarea/reference.html#supportedbrowsers' target='_blank'>" +
      "supported browsers</a> for more information.<br>";
}


/*
* Configure custom command buttons for configuration.
*
* Registration parameters:
*
* - editor button id
* - tooltip text
* - image icon
* - supported in HTMLArea textmode true/false
* - command handler function
*/
function sakaiRegisterButtons(config)
{

  // register custom button for smiling face
  config.registerButton("smile", //button ID,
   "Insert Smiling Face", //tooltip,
   inputRichTextDir + "/images/regular_smile.gif", // image
   false, // textMode,

   function(editor, id)
   {
     editor._insertSmile( inputRichTextDir + "/images/regular_smile.gif","Insert Smiling Face");
   }
  );

  // register custom button for sad face
  config.registerButton("sad", //button ID,
   "Insert Sad Face", //tooltip,
   inputRichTextDir + "/images/sad_smile.gif", // image
   false, // textMode,

   function(editor, id)
   {
     editor._insertSmile( inputRichTextDir + "/images/sad_smile.gif","Insert Sad Face");
   }
  );

  // register custom button for audio recording
  config.registerButton("recordresponse", //button ID,
   "Record audio response", //tooltip,
   inputRichTextDir + "/images/recordresponse.gif", // image
   true, // textMode,

   // this function is a little long because we are looking for global varibles
   // to be set in the calling page, if they are not there we provide defaults
   //before the rest of the work can be done.
   function(editor, id)
   {
      currentEditorForPopup = editor;
      var filename, limit, seconds, app, dir, imageUrl;
      // first we need to look for global settings and if they don't exist
      // we use defaults, mostly OK, so we can comment out the debug alerts
      // SHOULD have unique filename, dir-- should leave in those 2 alerts

      // first, we look for file name
      if (typeof audioFileName != "undefined" &&
          typeof audioFileExtension != "undefined")
      {
        // we make each call to this function use a different random offset
        random_offset = Math.floor(Math.random()*1000000001);
        filename = audioFileName + "_" + random_offset + "." + audioFileExtension;
      }
      else
      {
        alert ("filename is undefined, defaulting to test.au!");
        filename = "test.au";
      }


      // limit on retries (0 is unlimited)
      if (typeof audioLimit != "undefined")
      {
        limit = audioLimit;
      }
      else
      {
        alert ("limit is undefined, defaulting to 0");
        limit = 0;
      }

      // maximum seconds to record
      if (typeof audioSeconds != "undefined")
      {
        seconds = audioSeconds;
      }
      else
      {
        alert ("seconds is undefined, defaulting to 30");
        seconds = 30;
      }

      // user facing application name
      if (typeof audioAppName != "undefined")
      {
        app = audioAppName;
      }
      else
      {
        alert ("app is undefined, defaulting to 'Audio Recording'");
        app = "Audio Recording";
      }

      // next, we look for directory for uploads, or use tmp as a default
      // this may work OK, but we shouldn't assume that this is a UNIX server
      if (typeof audioDir != "undefined")
      {
        dir = audioDir;
      }
      else
      {
        alert ("dir is undefined, defaulting to temporary directory!");
        dir = "/tmp";
        //dir = "c:\\tmp";
      }

      if (typeof audioImageURL != "undefined")
      {
        imageUrl = audioImageURL;
      }
      else
      {
        alert ("imageUrl is undefined, setting a default location");
        imageUrl = inputRichTextDir + "/images/";
      }

      // gets called when the button is clicked
      window.open(
      inputRichTextDir + "/sakai-popups/recordSound.faces" +
        "?" + "filename=" + filename +
        "&" + "seconds=" + seconds +
        "&" + "limit=" + limit +
        "&" + "app=" + app +
        "&" + "dir=" + dir +
        "&" + "imageUrl=" + imageUrl,
      "_sakai_wysiwyg_popup",
      "toolbar=no,menubar=no,personalbar=no,width=430,height=330," +
      "scrollbars=no,resizable=no");
   }
  );

  // register custom button for image insertion, adds functionality over the basic
  // dialog code in popups/insert_image.html
  config.registerButton("sakai_insertimage", //button ID,
   "Insert Image", //tooltip,
   inputRichTextDir + "/images/ed_image.gif", // image
   true, // textMode,

   function(editor, id)
   {
      currentEditorForPopup = editor;
      // gets called when the button is clicked
      var results = window.open( root +"sakai_popups/insertImage.faces",
      "_sakai_wysiwyg_popup", "toolbar=no,menubar=no,personalbar=no,width=650,height=500," +
      "scrollbars=no,resizable=yes");
   }
  );

  // register custom button for link insertion, adds functionality over the basic
  // dialog code in popups/insert_link.html

  config.registerButton(
   "sakai_createlink", //button ID,
   "File Upload and Link", //tooltip,
   inputRichTextDir + "/images/ed_link.gif", // image
   true, // textMode,

   function(editor, id)
   {
     currentEditorForPopup = editor;
     // gets called when the button is clicked
     var results = window.open( root + "/sakai_popups/insertLink.faces",
      "_sakai_wysiwyg_popup", "toolbar=no,menubar=no,personalbar=no,width=650,height=380," +
      "scrollbars=no,resizable=yes");
   }
  );

}

/* Check whether the user's browser supports the widget.
*
* The only browsers that work are:
*  1. Internet Explorer 5.5 or greater on Windows
*  2. a recent version of gecko-based browser (Mozilla, Netscape, Firebird, Firefox)
*
*   returns true if the browser is supported, false if the browser is not supported
*/
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
  document.write(document.msgFormattedTextNotSupported);
}
else
{
  // browser supports the editor, start setting it up
  if (document.htmlareas == undefined)
  {
    document.htmlareas = new Array();

    // This section will only be included ONCE per HTML document that contains formatted text editing widget(s)
    _editor_url = inputRichTextDir + "/htmlarea/";
    _editor_lang = "en";

    // funky way to include the HTMLArea JavaScript library, from within JavaScript
    document.write('<script type="text/javascript" src="' + inputRichTextDir + '/htmlarea/htmlarea.js"></script>');
  }
}


/*
 * Set up fancy formatted text editing widget.
 * parameter:  textareaId The HTML id of the HTML textarea turned into widget
 * parameter:  config the HTMLArea.Config
 */
function sakaiSetupRichTextarea(textareaId, config)
{
  if (!isHTMLAreaSupported()) return;

  var counter = document.htmlareas.length;

  // instantiate the widget
  var ta = HTMLArea.getElementById("textarea", textareaId);
  var editor = new HTMLArea(ta, config);

//  // display the editing widget a bit later
//  setTimeout(
//    function()
//    {
      editor.generate();
      editor._iframe.style.border = "1px solid #000000";
//    }, 500);
//
  document.htmlareas[counter] = new Array(textareaId, editor, false);
//  _sakaiReturnFocusBack()
}



/*
 * Toggle: show or hide the docuemnt editor toolbar
 * parameter:  textareaId the HTMLArea id of the textarea
 */
function toggleToolbarDisplay(textareaId)
{
  // var delta=parseInt(editor._toolbar.offsetheight); //todo -  would be nice to determine height programmatically
      var delta = 49;

  var i;
  // find the editor object for the given textareaId
  for (i=0; i<document.htmlareas.length; i++)
  {
    if (document.htmlareas[i][0] == textareaId)
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


/**
* Functionality for insertSmile.
*/
HTMLArea.prototype._insertSmile = function(url,f_alt )
{
  var editor = this;
  var fields = ["f_url", "f_alt", "f_align", "f_border", "f_horiz", "f_vert"];
  var values = [url, f_alt, "baseline", 0, 0, 0];
  var param = new Object();
  for(var i in fields)
  {
     var id = fields[i];
     var val = values[i];
     param[id] = val;
  }
  var sel = editor._getSelection();
  var range = editor._createRange(sel);
  editor._doc.execCommand("insertimage", false, param["f_url"]);
  var img = null;
  if(HTMLArea.is_ie)
  {
     img = range.parentElement();
     // wonder if this works...
     if(img.tagName.toLowerCase() != "img")
     {
        img = img.previousSibling;
     }
  }
  else
  {
     img = range.startContainer.previousSibling;
  }
  for(field in param)
  {
     var value = param[field];
     if(!value)
     {
        continue;
     }
     switch(field)
     {
        case "f_alt" : img.alt = value;
           break;
        case "f_border" : img.border = parseInt(value);
           break;
        case "f_align" : img.align = value;
           break;
        case "f_vert" : img.vspace = parseInt(value);
           break;
        case "f_horiz" : img.hspace = parseInt(value);
           break;
     }
  }
};

/**
* Moved code out of renderer.
*
* Jira bug#:SAK-126 returning focus back to first editable field on the web page.
*/
function _sakaiReturnFocusBack()
{
  setTimeout(
   function(){
    if (document.forms.length > 0)
    {
      var allElements = document.forms[0];
      for (i = 0; i < allElements.length; i++)
      {
        if((allElements.elements[i].getAttribute("type") !=null) &&((allElements.elements[i].type == "text") || (allElements.elements[i].type == "textarea")))
        {
          document.forms[0].elements[i].focus();
          break;
        }
      }
     }
   }, 600);

}

function sakaiRegisterResourceList(config, image, resources) {
  var filedropdown = {
     id                 : "filedropdown",
     tooltip            : "tooltip",
     options            : resources,
     action             :
        function(editor) {updateAttachmentsValue(editor, this)},
     refresh            : function(editor){;}
  };

  config.registerDropdown(filedropdown);

  config.registerButton(
     "insertfile",
     "Insert file",
     image,
     false,
     function(editor) {
        editor.insertHTML(editor.filedropdownValue);
     }
  );

}

function updateAttachmentsValue(editor, obj) {
   var value = editor._toolbarObjects[obj.id].element.value;
   editor.filedropdownValue = value;
}

function resetRichTextEditor(clientId, config) {

   for (i=0;i<document.htmlareas.length;i++){
      if (document.htmlareas[i][0] == clientId) {
         var editor = document.htmlareas[i][1];
         editor.setMode();
         editor.setMode();
      }
   }

}


/**********************************************************************************
* $URL$
* $Id$
**********************************************************************************/
