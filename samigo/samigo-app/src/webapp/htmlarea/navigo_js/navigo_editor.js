// $Id: navigo_editor.js,v 1.1.1.1 2004/07/28 21:32:08 rgollub.stanford.edu Exp $

// for Mozilla, this keeps the correct editor value
var currentEditorForPopup;

function setConfigurations(root, reqRows)
{
   var editor = null;
   var config = new HTMLArea.Config();

   //Configurations by Rashmi
   //***************************************************************************
   config.statusBar = false;
   config.imgURL = root + "images/";
   config.popupURL = root +"popups/";
  //****************************************************************************

   function cut_copy_paste(e, cmd, obj)
   {
      try
      {
         e.execCommand(cmd);
      }
      catch(e)
      {
         if(Area.is_gecko)
         {
            alert("Some revisions of Mozilla/Gecko do not support programatic " + "access to cut/copy/paste functions, for security reasons.  " + "Your browser is one of them.  Please use the standard key combinations:\n" + "CTRL-X for cut, CTRL-C for copy, CTRL-V for paste.");
            obj.element.style.display = "none";
         }
      }
   };

 //*****************************************************************************

 // configuration for all the button images
  config.btnList = {
    bold: [ "Bold", root+"images/ed_format_bold.gif", false, function(e) {e.execCommand("bold");} ],
    italic: [ "Italic", root+"images/ed_format_italic.gif", false, function(e) {e.execCommand("italic");} ],
    underline: [ "Underline", root+"images/ed_format_underline.gif", false, function(e) {e.execCommand("underline");} ],
    strikethrough: [ "Strikethrough", root+"images/ed_format_strike.gif", false, function(e) {e.execCommand("strikethrough");} ],
    subscript: [ "Subscript", root+"images/ed_format_sub.gif", false, function(e) {e.execCommand("subscript");} ],
    superscript: [ "Superscript", root+"images/ed_format_sup.gif", false, function(e) {e.execCommand("superscript");} ],
    justifyleft: [ "Justify Left", root+"images/ed_align_left.gif", false, function(e) {e.execCommand("justifyleft");} ],
    justifycenter: [ "Justify Center",root+ "images/ed_align_center.gif", false, function(e) {e.execCommand("justifycenter");} ],
    justifyright: [ "Justify Right", root+"images/ed_align_right.gif", false, function(e) {e.execCommand("justifyright");} ],
    justifyfull: [ "Justify Full", root+"images/ed_align_justify.gif", false, function(e) {e.execCommand("justifyfull");} ],
    insertorderedlist: [ "Ordered List", root+"images/ed_list_num.gif", false, function(e) {e.execCommand("insertorderedlist");} ],
    insertunorderedlist: [ "Bulleted List", root+"images/ed_list_bullet.gif", false, function(e) {e.execCommand("insertunorderedlist");} ],
    outdent: [ "Decrease Indent", root+"images/ed_indent_less.gif", false, function(e) {e.execCommand("outdent");} ],
    indent: [ "Increase Indent", root+"images/ed_indent_more.gif", false, function(e) {e.execCommand("indent");} ],
    forecolor: [ "Font Color", root+"images/ed_color_fg.gif", false, function(e) {e.execCommand("forecolor");} ],
    hilitecolor: [ "Background Color", root+"images/ed_color_bg.gif", false, function(e) {e.execCommand("hilitecolor");} ],
    inserthorizontalrule: [ "Horizontal Rule", root+"images/ed_hr.gif", false, function(e) {e.execCommand("inserthorizontalrule");} ],
    createlink: [ "Insert Web Link", root+"images/ed_link.gif", false, function(e) {e.execCommand("createlink", true);} ],
    insertimage: [ "Insert Image", root+"images/ed_image.gif", false, function(e) {e.execCommand("insertimage");} ],
    inserttable: [ "Insert Table", root+"images/insert_table.gif", false, function(e) {e.execCommand("inserttable");} ],
    htmlmode: [ "Toggle HTML Source", root+"images/ed_html.gif", true, function(e) {e.execCommand("htmlmode");} ],
    popupeditor: [ "Enlarge Editor", root+ "images/fullscreen_maximize.gif", true,
     function(editor, id) {
        if (HTMLArea.is_ie) {
          window.open(root +"popups/fullscreen.html", "ha_fullscreen",
              "toolbar=no,location=no,directories=no,status=no,menubar=no," +
              "scrollbars=no,resizable=yes,width=640,height=480");
        } else {
          window.open(root +"popups/fullscreen.html" , "ha_fullscreen",
              "toolbar=no,menubar=no,personalbar=no,width=640,height=480," +
              "scrollbars=no,resizable=yes");
    }} ],
    about: [ "About this editor", root+"images/ed_about.gif", true, function(e) {e.execCommand("about");} ],
    showhelp: [ "Help using editor", root+"images/ed_help.gif", true, function(e) {e.execCommand("showhelp");} ],
    undo: [ "Undoes your last action", root+"images/ed_undo.gif", false, function(e) {e.execCommand("undo");} ],
    redo: [ "Redoes your last action", root+"images/ed_redo.gif", false, function(e) {e.execCommand("redo");} ],
    cut: [ "Cut selection",root+ "images/ed_cut.gif", false, cut_copy_paste ],
    copy: [ "Copy selection", root+"images/ed_copy.gif", false, cut_copy_paste ],
    paste: [ "Paste from clipboard", root+"images/ed_paste.gif", false, cut_copy_paste ]
    };

//******************************************************************************

   // register custom button for smiling face
    config.registerButton("smile", //button ID,
   "Insert Smiling Face", //tooltip,
   root + "images/regular_smile.gif", // image
   false, // textMode,

   function(editor, id)
   {
     editor._insertSmile( root + "images/regular_smile.gif","Insert Smiling Face");
   }
   );

//******************************************************************************
  // register custom button for sad face
    config.registerButton("sad", //button ID,
   "Insert Sad Face", //tooltip,
   root + "images/sad_smile.gif", // image
   false, // textMode,

   function(editor, id)
   {
     editor._insertSmile( root + "images/sad_smile.gif","Insert Sad Face");
   }
   );
//******************************************************************************

   // register custom button for audio recording
   config.registerButton("recordresponse", //button ID,
   "Record audio response", //tooltip,
   root + "images/recordresponse.gif", // image
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
        imageUrl = root + "../jsp/aam/images/";
      }

      // gets called when the button is clicked
      window.open(
      root + "../jsp/aam/applet/soundRecorder.jsp" +
        "?" + "filename=" + filename +
        "&" + "seconds=" + seconds +
        "&" + "limit=" + limit +
        "&" + "app=" + app +
        "&" + "dir=" + dir +
        "&" + "imageUrl=" + imageUrl,
      "__ha_dialog",
      "toolbar=no,menubar=no,personalbar=no,width=430,height=330," +
      "scrollbars=no,resizable=no");
   }
   );
   //***************************************************************************
   // register custom button for image insertion, adds functionality over the basic
   // dialog code in popups/insert_image.html
   config.registerButton("navigo_insertimage", //button ID,
   "Insert Image", //tooltip,
   root + "images/ed_image.gif", // image
   true, // textMode,

   function(editor, id)
   {
      currentEditorForPopup = editor;
      // gets called when the button is clicked
//
//		this is a pure HTML version, switched to JSP to make dynamic
//    var results = window.open( root +"navigo_popups/insert_image.html",
//
      var results = window.open( root +"navigo_popups/insert_image.jsp",
      "__ha_dialog", "toolbar=no,menubar=no,personalbar=no,width=650,height=500," + //	   	"scrollbars=no,resizable=no"  // having trouble in Mozilla sizing, so leave resize in for now
      "scrollbars=no,resizable=yes");
   }
   );

  //****************************************************************************

   // register custom button for link insertion, adds functionality over the basic
   // dialog code in popups/insert_link.html

   config.registerButton("navigo_createlink", //button ID,
   "File Upload and Link", //tooltip,
   root + "images/ed_link.gif", // image
   true, // textMode,

   function(editor, id)
   {
     currentEditorForPopup = editor;
// gets called when the button is clicked
//
//		this is a pure HTML version, switched to JSP to make dynamic
//    var results = window.open( root +"navigo_popups/insert_link.html",
//
      var results = window.open( root +"navigo_popups/insert_link.jsp",
      "__ha_dialog", "toolbar=no,menubar=no,personalbar=no,width=650,height=380," + //	   	"scrollbars=no,resizable=no"  // having trouble in Mozilla sizing, so leave resize in for now
      "scrollbars=no,resizable=yes");
   }
   );
  //****************************************************************************

   // custom help
   // dialog code in navigo_popups/editor_help.html
   config.registerButton("navigo_showhelp", //button ID,
   "Editor Help", //tooltip,
   root + "images/ed_help.gif", // image
   true, // textMode,

   function(editor, id)
   {
      // gets called when the button is clicked
      var results = window.open( root +"navigo_popups/editor_help.html",
      "__ha_dialog", "toolbar=no,menubar=no,personalbar=no,width=740,height=500," + "scrollbars=yes,resizable=yes");
   }
   );
  //****************************************************************************

   // custom info about
   // dialog code in navigo_popups/about.html
   config.registerButton("navigo_about", //button ID,
   "About Sakai and HTMLArea", //tooltip,
 root +"images/ed_about.gif", // image
   true, // textMode,

   function(editor, id)
   {
      // gets called when the button is clicked
      var results = window.open( root +"navigo_popups/about.html", "__ha_dialog",
      "toolbar=no,menubar=no,personalbar=no,width=600,height=550," + "scrollbars=no,resizable=yes");
   }
   );
 //*****************************************************************************
 // set the configuration for the toolbar
 // alert(reqRows);
   if(reqRows != null && reqRows == "three")
    {
      // three lines
      config.toolbar = [
        [ "fontname", "space", "fontsize", "space", "bold", "italic", "underline",
        "strikethrough","forecolor","hilitecolor"],

        ["subscript", "superscript",  "separator",
        "justifyleft", "justifycenter", "justifyright", "justifyfull",
        "insertorderedlist", "insertunorderedlist", "outdent", "indent",
        "inserthorizontalrule"],

        ["smile", "sad", "navigo_createlink", "navigo_insertimage",
        "recordresponse", "separator",
        "htmlmode",  "separator", "navigo_showhelp", "navigo_about"],
        ];

      // FULL OPTIONS -- this documents how to use all the supported options
      /*
       config.toolbar = [["fontname", "space", "fontsize", "space",
       "formatblock", "space", "bold", "italic", "underline"],
       ["separator", "strikethrough", "subscript", "superscript", "separator",
       "copy", "cut", "paste", "space", "undo", "redo", "justifyleft",
       "justifycenter", "justifyright", "justifyfull", "separator",
       "insertorderedlist", "insertunorderedlist", "outdent", "indent",
       "separator"],
       ["separator", "navigo_createlink", "navigo_insertimage", "recordresponse",
       "separator","forecolor", "hilitecolor", "textindicator", "separator",
       "inserthorizontalrule", "createlink", "insertimage", "inserttable",
       "htmlmode", "separator", "navigo_showhelp", "navigo_about", "separator",
       "smile", "sad"]];
       */
     }
    else
    { if(reqRows != null && reqRows == "two")
    {
      // two lines
         config.toolbar = [
        [ "fontname", "space", "fontsize", "space", "bold", "italic", "underline",
        "strikethrough" ,"subscript", "superscript", "forecolor","hilitecolor",  "separator",
        "justifyleft", "justifycenter", "justifyright", "justifyfull"
        ],

        ["insertorderedlist", "insertunorderedlist", "outdent", "indent",
        "inserthorizontalrule","smile", "sad", "navigo_createlink", "navigo_insertimage",
        "recordresponse", "separator",
        "htmlmode",  "separator", "navigo_showhelp", "navigo_about"],
        ];
    }
    else
    {
      // single line toolbar
      config.toolbar = [
        [ "fontname", "space", "fontsize", "space", "bold", "italic", "underline",
        "strikethrough", "subscript", "superscript", "forecolor","hilitecolor",  "separator",
        "justifyleft", "justifycenter", "justifyright", "justifyfull", "separator",
        "insertorderedlist", "insertunorderedlist", "outdent", "indent",
        "inserthorizontalrule", "smile", "sad", "separator",
        "navigo_createlink", "navigo_insertimage",  "recordresponse", "separator",
        "htmlmode", "separator", "navigo_showhelp", "navigo_about"],
        ];

      // FULL OPTIONS -- this documents how to use all the supported options
      /*
       config.toolbar = [["fontname", "space", "fontsize", "space",
       "formatblock", "space", "bold", "italic", "underline", "separator",
       "strikethrough", "subscript", "superscript", "separator", "copy", "cut",
       "paste", "space", "undo", "redo"],["separator", "navigo_createlink",
       "navigo_insertimage", "recordresponse","separator","justifyleft",
       "justifycenter", "justifyright", "justifyfull", "separator",
       "insertorderedlist", "insertunorderedlist", "outdent", "indent",
       "separator", "forecolor", "hilitecolor", "textindicator", "separator",
       "inserthorizontalrule", "createlink", "insertimage", "inserttable",
       "htmlmode", "separator", "navigo_showhelp", "navigo_about", "separator",
       "smile", "sad"]];
       */
    }
    }

      return config;
   };


 //*****************************************************************************

   // Addtional code by Rashmi
   // Create the specified plugin and register it with this HTMLArea
  HTMLArea.prototype.registerPlugin = function(pluginName, root)
  {
          this.plugins[pluginName] = eval("new " + pluginName + "(this , root);");
  };

  // static function that loads the required plugin and lang file, based on the
  // language loaded already for HTMLArea.  You better make sure that the plugin
  // _has_ that language, otherwise shit might happen ;-)
  HTMLArea.loadPlugin = function(pluginName,root) {
  var editorurl = root;
  if (typeof _editor_url != "undefined") {
          editorurl = _editor_url + "/";
  }
  var dir = editorurl + "plugins/" + pluginName;
  var plugin = pluginName.replace(/([a-z])([A-Z])([a-z])/g,
                function (str, l1, l2, l3) {
                        return l1 + "-" + l2.toLowerCase() + l3;
                }).toLowerCase() + ".js";

  document.write("<script type='text/javascript' src='"   + root + "navigo_js/" + plugin + "'></script>");
  document.write("<script type='text/javascript' src='" + dir + "/lang/" + HTMLArea.I18N.lang + ".js'></script>");
};

 // create an editor for a specific textarea
 //**********************************************************************************************************

   function initEditorById(textboxId, root, rows, shouldToggle)
   {
      var config = setConfigurations(root, rows);
      editor = new HTMLArea(textboxId, config);
      // register the SpellChecker plugin
      //editor.registerPlugin("SpellChecker", root);
      editor.generate();
      if(shouldToggle != null && shouldToggle == true)
        {
          var ta = getelm(textboxId);
          hidden[ta.id] = false;
          toggle_display_toolbar(ta, editor);

        }
      return editor;
   }

  //**********************************************************************************************************
 //specific to xsl using questions only, loads editor on each and every textarea
 // that has an id
   function loadEditor(root, startArrayAt, formNo, rows, questionXpath)
   {

      if(startArrayAt == 2)
      {
         ta_editor[1] = initEditorById(questionXpath, root, "two",true);
      }
      var editorNo = startArrayAt;
      formNo = formNo;
      var totalelements = document.forms[formNo].elements.length;
      var allElements = [];
      for(var i = 0; i <(totalelements); i++)
      {
         if(document.forms[formNo].elements[i].type == "textarea" && document.forms[formNo].elements[i].name != "stxx/item/presentation/flow/material/mattext" && document.forms[formNo].elements[i].name != "stxx/form/itemActionForm/fibAnswer")
         {
            if((document.forms[formNo].elements[i].name == document.forms[formNo].elements[i].id) && ( document.forms[formNo].elements[i].id !="")) // for gecko id of a textarea is must.
            {
               textAreaNames[editorNo] = document.forms[formNo].elements[i].name;
               editorNo = editorNo + 1;
            }
         }
      }
      for(var j = startArrayAt; j <(editorNo); j++)
      {
         ta_editor[j] = initEditorById(textAreaNames[j], root, rows,true);
      }
      // Return the focus back to first field on the form
      for(var i = 0; i <(totalelements); i++)
      {
         if(document.forms[formNo].elements[i].type != "hidden" &&
          !document.forms[formNo].elements[i].disabled &&
          !document.forms[formNo].elements[i].readOnly)
         {
            document.forms[formNo].elements[i].focus();
            break;
         }
      }
   };
 //*****************************************************************************
// To toggle  hide unhide functionality
    function hideUnhide(field, rows)
   {
    if(rows == null || rows.length < 2 )
    rows="three";

      for(var i = 1; i <= ta_editor.length; i++)
      {
         if(textAreaNames[i] == field)
         {
            toggle_display_toolbar(field, ta_editor[i], rows);
         }
      }
   };
//******************************************************************************
// main code for hiding unhiding the toolbar
   function toggle_display_toolbar(textbox, editor, rows)
   {
      // var delta=parseInt(editor._toolbar.offsetheight); //todo -  would be nice to determine height programmatically

                   if(rows == "two")
      {
         var delta = 49;
      }
      else
      {
         var delta = 0;
         //(this is variable too // 49 is there are two rows   )
      }
      var height = parseInt(editor._iframe.style.height);
      // alert(hidden[textbox.id]);
      if(hidden[textbox.id] == undefined)
      {
         editor._toolbar.style.display = "block";
        // editor._iframe.style.height = height - delta + "px";
         hidden[textbox.id] = false;
      }
      else
      {
         if(!hidden[textbox.id])
         {
            editor._toolbar.style.display = "none";
            editor._iframe.style.height = height + delta + "px";
            hidden[textbox.id] = true;
         }
         else
         {
            editor._toolbar.style.display = "block";
            editor._iframe.style.height = height - delta + "px";
            hidden[textbox.id] = false;
         }
      }
          editor.focusEditor() ;

   };
  //**********************************************************************************************************
  //Takes focus to first field
   function focusToFirstField(formno)
   {
      var formNo = formno;
     // alert("setting focus" + formNo);
      var totalelements = document.forms[formno].elements.length;
      //alert(totalelements);
      for(var i = 0; i < totalelements; i++)
      {
         if(document.forms[formNo].elements[i].type != "hidden" && !document.forms[formNo].elements[i].disabled && !document.forms[formNo].elements[i].readOnly)
         {
           // alert(document.forms[formNo].elements[i].name + document.forms[formNo].elements[i].type);
            document.forms[formNo].elements[i].focus();
            break;
         }
      }
   };
   //**************GET ELEMENT ***************************************************************
   function getelm(thisid){

              var thiselm = null;
               if (document.getElementById)
               {
                       // browser implements part of W3C DOM HTML ( Gecko, Internet Explorer 5+, Opera 5+
                     thiselm = document.getElementById(thisid);

            }

                else if (document.all){
         // Internet Explorer 4 or Opera with IE user agent
                 thiselm = document.all[thisid];
              }

                else if (document.layers){ // Navigator 4
                    thiselm = document.layers[thisid];
                }

  if(thiselm)	{

        if(thiselm == null) { return; }
        else {return thiselm;}
      }
  }
 //*****************************************************************************
 // functionality for insertSmile
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
//**************************************************
        function onSubmitFn()
    {
       for (var i = 1; i < ta_editor.length; i++)
      {
         var editor = ta_editor[i];
        editor._textArea.value = editor.getHTML();
      }
  }
//*****************************************************
HTMLArea.prototype._createStatusBar = function() {
  var div = document.createElement("div");
  div.className = "statusBar";
  this._htmlArea.appendChild(div);
  this._statusBar = div;
  //div.appendChild(document.createTextNode(HTMLArea.I18N.msg["Path"] + ": "));
  // creates a holder for the path view
//div = document.createElement("span");
//div.className = "statusBarTree";
  //this._statusBarTree = div;
  //this._statusBar.appendChild(div);
  if (!this.config.statusBar) {
    // disable it...
    div.style.display = "none";
  }
};