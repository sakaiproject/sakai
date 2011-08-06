<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/struts-template.tld" prefix="template" %>
<jsp:useBean id="fileUpload" scope="session"
  class="org.navigoproject.ui.web.form.edit.FileUploadForm" />
<html:html>

<!--
/**********************************************************************************
 * $URL: $
 * $Id: $
 ***********************************************************************************
 *
 * Copyright (c) 2005 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/
-->

<head>
  <title>Insert Link</title>

<script type="text/javascript" src="../popups/popup.js"></script>
<style type="text/css">
@import url(htmlarea.css);

</style>

<script type="text/javascript"><!--
var openerEditor = opener.editor;

// is this link an HTTP or HTTPS url?
function hasHTTP(s) {
  if (!s) return false;
  if (s.indexOf("http")==0) return true;
  return false;
}

// prepends http:// on URLs that don't have a protocol
function fixMyURL(o){
  s=o.value;
  if (!hasHTTP(s)){
    o.value="http://" + s;
  }

  document.getElementById("f_url").value = s;
}

function Init() {
  __dlg_init();
};


// This is if the Submit button is pressed, the default dialog used OK, hence the name
function onOK() {
  // data to pass back to the calling window
  var fields = ["f_url", "f_newfile", "f_name", "f_author",
  "f_source_0",  "f_description" ];

  // create an associative array for parameters
  var param = new Object();

  for (var i in fields) {
    var id = fields[i];
    var el = document.getElementById(id);
    param[id] = el.value;
  }

  return false;
};

// This is if the cancel is pressed
function onCancel() {
  __dlg_close(null);
  window.close();//mozilla
};

// this supplies a default for the text
function defaultName(){
  if ( document.getElementById("f_name").value=="" &&
    document.getElementById("f_url").value!=""
  )
  {
    document.getElementById("f_name").value=document.getElementById("f_url").value;
  }
}

//--></script>
<style type="text/css">
html, body {
  font: 11px Tahoma,Verdana,sans-serif;
  margin: 0px;
  padding: 0px;
}
body { padding: 5px; }
table {
  font: 11px Tahoma,Verdana,sans-serif;
}
form p {
  margin-top: 5px;
  margin-bottom: 5px;
}
.fl { width: 9em; float: left; padding: 2px 5px; text-align: right; }
.fr { width: 6em; float: left; padding: 2px 5px; text-align: right; }
fieldset { padding: 0px 10px 5px 5px; }
select, input, button { font: 11px Tahoma,Verdana,sans-serif; }
button { width: 70px; }
.space { padding: 2px; }

.title { background: #ddf; color: #000; font-weight: bold; font-size: 120%;
padding: 3px 10px; margin-bottom: 10px;
border-bottom: 1px solid black; letter-spacing: 2px;
}
form { padding: 0px; margin: 0px; }
</style>


</head>

<body onload="Init()">

<div class="title">Insert Link</div>

<html:form action="htmlInlineUpload.do" method="post" onsubmit="onOK();"
  enctype="multipart/form-data">

<table border="0" width="100%" >
  <tbody>
      <tr  bgcolor=#E1E1E1 >
      <td class="tdSideRedText">Source</td>
      <td>
        <html:radio property="source" styleId="f_source_0" value="0" />
        Link to a file uploaded from your computer:
        <html:file property="newfile" styleId="f_newfile" />
        <br>
        <html:radio property="source" styleId="f_source_1" value="1"/>
        Link to an external URL:
        <html:text property="link" onchange="fixMyURL(this);defaultAlt();"
          size="50" style="width:100%"/>
        <!-- for internal use in WYSIWYG editor -->
        <input type="hidden" name="url" id="f_url"  />
        <!-- for internal use in WYSIWYG editor -->
        <input type="hidden" name="isHtmlInline" id="isHtmlInline"
          value="true" />
        <!-- for internal use in WYSIWYG editor -->
        <input type="hidden" name="isHtmlImage" id="isHtmlImage"
          value="false" />
        <br>
      </td>
    </tr>


    <tr  bgcolor=#FFFFFF >
      <td class="tdSideRedText">Title</td>
      <td>
        <html:text  property="name" styleId="f_name" value="New Link"/>
        <br>
      </td>
    </tr>
    <tr  bgcolor=#E1E1E1 >
      <td class="tdSideRedText">Description <br> (Optional)</td>
      <td>
        <html:textarea property="description" styleId="f_description"
          cols="60" rows="4" style="width:100%"  />
        <br>
      </td>
    </tr>
    <tr  bgcolor=#FFFFFF >
      <td class="tdSideRedText">Author/Citation <br> (Optional)</td>
      <td>
        <html:text property="author" styleId="f_author"/>
        <br>
      </td>
    </tr>
    <tr>
      <td colspan="2">
        <!-- BUTTONS -->
        <hr />
        <input type="reset" value="Cancel" onclick="return onCancel();"/>
        <input type="submit" value="Submit"  />
      </td>
    </tr>
  </tbody>
</table>
</html:form>
</body>
</html:html>
