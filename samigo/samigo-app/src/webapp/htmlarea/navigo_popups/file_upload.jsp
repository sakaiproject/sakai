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
  <title>File Upload</title>

<script type="text/javascript" src="../popups/popup.js"></script>
<style type="text/css">
@import url(htmlarea.css);

</style>

<script language="javascript"><!--

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


function writeToParent(f_url){
  var item_ident_ref = document.fileUpload.itemIdentRef.value;
  var i=0;
  while (opener.ASIDeliveryForm.element[i]!=null){
    if (opener.ASIDeliveryForm.element[i].id==item_ident_ref){
	opener.ASIDeliveryForm.element[i].value=f_url;
    }
    i++;
   }
}

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

<div class="title">Upload A New File</div>

<html:form action="htmlInlineAnswer.do" method="post" onsubmit="onOK();"
  enctype="multipart/form-data">
<table border="0" width="100%" >
  <tbody>
    <tr  bgcolor=#E1E1E1 >
      <td class="tdSideRedText">Source</td>
      <td>
        <html:hidden property="source" styleId="f_source_0" value="0"/>
        Upload File from your computer:
        <html:file property="newfile" styleId="f_newfile" />
        <input type="hidden" name="itemIdentRef" id="itemIdentRef"
          value='<%=
            request.getParameter("item_ident_ref")
          %>'
        />
        <br>
      </td>
    </tr>
    <tr  bgcolor=#FFFFFF >
      <td class="tdSideRedText">Title</td>
      <td>
          <html:text property="name" styleId="f_name" value="New File"/>
        <br>
      </td>
    </tr>
    <tr  bgcolor=#E1E1E1 >
      <td class="tdSideRedText">Description <br> (Optional)</td>
      <td>
        <html:textarea property="description" styleId="f_description" value=""
          cols="60" rows="4" style="width:100%"  />
        <br>
      </td>
    </tr>
  </tbody>
</table>

<!-- BUTTONS -->
<hr />
  <html:reset onclick="return onCancel();" value="Cancel"/>
  <html:submit value="Submit" property="Submit" />
</div>
</html:form>

</body>
</html:html>
