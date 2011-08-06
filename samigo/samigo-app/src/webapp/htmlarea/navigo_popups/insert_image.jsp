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
  <title>Insert Image</title>

<script type="text/javascript" src="../popups/popup.js"></script>
<style type="text/css">
@import url(htmlarea.css);

</style>

<script type="text/javascript"><!--

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
//  if f_url were not a hidden value but a text input, uncomment this line
//  document.getElementById("f_url").focus();
};

// This is if the Submit button is pressed, the default dialog used OK, hence the name
function onOK() {

  // data to pass back to the calling window
  var fields = ["f_url", "f_alt", "f_align", "f_border", "f_horiz", "f_vert",
    "f_newfile", "f_name", "f_author",   	"f_source_0",  "f_description" ];

  // create an associative array of parameters
  var param = new Object();
  for (var i in fields) {
    var id = fields[i];
    var el = document.getElementById(id);
    param[id] = el.value;
  }

  form[0].submit();

  return false;
};

// This is if the cancel is pressed
function onCancel() {
  __dlg_close(null);
  window.close();//mozilla
};

// this supplies a default for the alt tag
function defaultAlt(){
  if ( document.getElementById("f_alt").value=="" &&
    document.getElementById("f_url").value!=""
  ){
    document.getElementById("f_alt").value=document.getElementById("f_url").value;
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

<div class="title">Insert Image</div>

<html:form action="htmlInlineUpload.do" method="post" onsubmit="onOK();"
  enctype="multipart/form-data">
<table border="0" width="100%" >
  <tbody>
    <tr  bgcolor=#E1E1E1 >
      <td class="tdSideRedText">Source</td>
      <td>
        <html:radio property="source" styleId="f_source_0" value="0"/>
        Inline image uploaded from your computer:
        <html:file property="newfile" styleId="f_newfile" />
        <br>
        <html:radio property="source" styleId="f_source_1" value="1"/>
        Inline image from an external URL:
        <html:text property="link" onchange="fixMyURL(this);defaultAlt();"
          size="50" style="width:100%"/>
        <!-- for internal use in WYSIWYG editor -->
        <input type="hidden" name="url" id="f_url"  />
        <!-- for internal use in WYSIWYG editor -->
        <input type="hidden" name="isHtmlInline" id="isHtmlInline"
          value="true" />
        <!-- for internal use in WYSIWYG editor -->
        <input type="hidden" name="isHtmlImage" id="isHtmlImage"
          value="true" />
        <br>
      </td>
    </tr>
    <tr  bgcolor=#FFFFFF >
      <td class="tdSideRedText">Title</td>
      <td>
          <html:text property="name" styleId="f_name" value="New image"/>
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
    <!--
    FROM THIS POINT ON, ALL FIELDS CONTROL THE HTMLAREA IMAGE SETTINGS
    AND ARE NOT PUT INTO THE ACTION FORM
    -->
    <tr bgcolor=#E1E1E1 >
      <td style="width: 7em; text-align: right">Alternate text:</td>
      <td>
          <html:text property="imageAlt" styleId="f_alt" style="width:100%"
            title="For browsers that don't support images"/>
      </td>
    </tr>
  </tbody>
</table>

<p />

<fieldset style="float: left; margin-left: 5px;">
<legend>Layout</legend>

<div class="space"></div>

<div class="fl">Alignment:</div>
<html:select size="1" property="imageAlign" styleId="f_align"
  title="Positioning of this image">
  <html:option value=""                >Not set</html:option>
  <html:option value="left"            >Left</html:option>
  <html:option value="right"           >Right</html:option>
  <html:option value="texttop"         >Texttop</html:option>
  <html:option value="absmiddle"       >Absmiddle</html:option>
  <html:option value="baseline"        >Baseline</html:option>
  <html:option value="absbottom"       >Absbottom</html:option>
  <html:option value="bottom"          >Bottom</html:option>
  <html:option value="middle"          >Middle</html:option>
  <html:option value="top"             >Top</html:option>
</html:select>

<p />

<div class="fl">Border thickness:</div>
  <html:text property="imageBorder" styleId="f_border" size="5" value="0"
    title="Leave zero for no border" />
<div class="space"></div>

</fieldset>

<fieldset style="float:right; margin-right: 5px;">
<legend>Spacing</legend>

<div class="space"></div>

<div class="fr">Horizontal:</div>
  <html:text property="imageHspace" styleId="f_horiz" size="5" value="0"
    title="Horizontal padding"/>
<p />

<div class="fr">Vertical:</div>
  <html:text property="imageVspace" styleId="f_vert" size="5" value="0"
    title="Vertical padding" />
<div class="space"></div>

</fieldset>

<div style="margin-top: 85px; text-align: left;color: FF0000">
<!-- BUTTONS -->
<hr />
  <html:reset onclick="return onCancel();" value="Cancel"/>
  <html:submit value="Submit" property="Submit" />
</div>
</html:form>

</body>
</html:html>
