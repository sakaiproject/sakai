<%@ page contentType="text/html;charset=utf-8" pageEncoding="utf-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://www.sakaiproject.org/samigo" prefix="samigo" %>
<!-- $Id:  -->
  <f:view>
    <f:verbatim><!DOCTYPE html
     PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
     "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
    </f:verbatim>
    <html xmlns="http://www.w3.org/1999/xhtml">
      <head>
      <title>test</title>
      <samigo:stylesheet path="/css/main.css"/>
      <samigo:stylesheet path="/jsp/aam/stylesheets/nav.css"/>
      <script language="javascript" style="text/JavaScript">
<!--
/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2000-2004 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" must
 *    not be used to endorse or promote products derived from this
 *    software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    nor may "Apache" appear in their name, without prior written
 *    permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 * Portions of this software are based upon public domain software
 * originally written at the National Center for Supercomputing Applications,
 * University of Illinois, Urbana-Champaign.
 */




var checkflag = "false";

function checkAll(field) {
if (field != null) {
  if (field.length >0){
// for more than one checkbox
    if (checkflag == "false") {
       for (i = 0; i < field.length; i++) {
           field[i].checked = true;}
       checkflag = "true";
       return "Uncheck all"; }
    else {
       for (i = 0; i < field.length; i++) {
           field[i].checked = false; }
       checkflag = "false";
       return "Check all"; }
  }
  else {
// for only one checkbox
    if (checkflag == "false") {
  field.checked = true;
  checkflag = "true";
  return "Uncheck all"; }
else {
  field.checked = false;
  checkflag = "false";
  return "Check all"; }

   }
}
}


function uncheck(field){
      field.checked = false;
  checkflag = "false";
    return "uncheck";
}

function flagRows(){
//return;
 var divs = document.getElementsByTagName("A");
  for (var i = 0; i < divs.length; i++) {
     var d = divs[i];
     if (d.className == "folder"){
        d.style.backgroundImage = "url(../../../images/tree/closed.gif)";
     }
   }
  }

function toggleRows(elm) {
 var rows = document.getElementsByTagName("TR");
 elm.style.backgroundImage = "url(../../../images/tree/closed.gif)";
 var newDisplay = "none";
 var thisID = elm.parentNode.parentNode.parentNode.id + "-";
  alert(thisID);
 // Are we expanding or contracting? If the first child is hidden, we expand
  for (var i = 0; i < rows.length; i++) {
   var r = rows[i];
   if (matchStart(r.id, thisID, true)) {
    if (r.style.display == "none") {
     if (document.all) newDisplay = "block"; //IE4+ specific code
     else newDisplay = "table-row"; //Netscape and Mozilla
     elm.style.backgroundImage = "url(../../../images/tree/open.gif)";
    }
    break;
   }
 }

 // When expanding, only expand one level.  Collapse all desendants.
 var matchDirectChildrenOnly = (newDisplay != "none");

 for (var j = 0; j < rows.length; j++) {
   var s = rows[j];
   if (matchStart(s.id, thisID, matchDirectChildrenOnly)) {
     s.style.display = newDisplay;
     var cell = s.getElementsByTagName("TD")[0];
     var tier = cell.getElementsByTagName("DIV")[0];
     var folder = tier.getElementsByTagName("A")[0];
     if (folder.getAttribute("onclick") != null) {
      folder.style.backgroundImage = "url(../../../images/tree/closed.gif)";
     }
   }
 }
}

function matchStart(target, pattern, matchDirectChildrenOnly) {
 var pos = target.indexOf(pattern);
 if (pos != 0) return false;
 if (!matchDirectChildrenOnly) return true;
 if (target.slice(pos + pattern.length, target.length).indexOf("-") >= 0) return
 false;
 return true;
}

function collapseAllRows() {
 var rows = document.getElementsByTagName("TR");
 for (var j = 0; j < rows.length; j++) {
   var r = rows[j];
   if (r.id.indexOf("-") >= 0) {
     r.style.display = "none";
   }
 }
}

function collapseRowsByLevel(i) {
 var rows = document.getElementsByTagName("TR");
 for (var j = 0; j < rows.length; j++) {
   var r = rows[j];
   var rtokens =r.id.split("-");

   if (r.id.indexOf("-") >= 0) {
     if (rtokens.length > i) {
       r.style.display = "none";
     }
   }

 }
}


// below is for simple tree
function toggleBullet(elm) {
 var newDisplay = "none";
 var e = elm.nextSibling;
 while (e != null) {
  if (e.tagName == "OL" || e.tagName == "ol") {
   if (e.style.display == "none") newDisplay = "block";
   break;
  }
  e = e.nextSibling;
 }
 while (e != null) {
  if (e.tagName == "OL" || e.tagName == "ol") e.style.display = newDisplay;
  e = e.nextSibling;
 }
}

function collapseAll() {
  var lists = document.getElementsByTagName('OL');
  for (var j = 0; j < lists.length; j++)
   lists[j].style.display = "none";
  lists = document.getElementsByTagName('ol');
  for (var j = 0; j < lists.length; j++)
   lists[j].style.display = "none";
  var e = document.getElementById("root");
  e.style.display = "block";
}

function PopupWin(url)
{
   window.open(url,"ha_fullscreen","toolbar=no,location=no,directories=no,status=no,menubar=yes,"+"scrollbars=yes,resizable=yes,width=640,height=480");

}

//-->
</script>

      </head>
      <body>
  <!-- content... -->
<h:form>


<div style="border: 1px #000;">
  <h:dataTable headerClass="altBackground">
    <h:column >
      <f:facet name="header">
        <h:checkbox  onclick="this.value=checkAll(this.form.selectedPools)"  property="allPoolsSelected"/>
        <h:outputText value="Pool Name" />
      </f:facet>
    </h:column>
    <h:column >
      <f:facet name="header">
        <h:outputText value="Creator" />
      </f:facet>
    </h:column>
    <h:column >
      <f:facet name="header">
        <h:outputText value="Last Modified" />
      </f:facet>
    </h:column>
    <h:column >
      <f:facet name="header">
        <h:outputText value="# of Questions" />
      </f:facet>
    </h:column>
    <h:column >
      <f:facet name="header">
        <h:outputText value="# of Subpools" />
      </f:facet>
    </h:column>
    <h:column>
      <f:facet name="header">
        <h:outputText value="" />
      </f:facet>
    </h:column>
  </h:dataTable>
  <table border="0" width="100%">
    <logic:iterate name="subpoolTree" property="sortedObjects" id="pool"
      type="org.navigoproject.business.entity.questionpool.model.QuestionPool" indexId="ctr">
    <% subpoolTree.setCurrentId(pool.getId()); %>
    <logic:empty name="subpoolTree" property="parent">
    <tr id="<%= subpoolTree.getCurrentObjectHTMLId() %>" >
    </logic:empty> <logic:notEmpty name="subpoolTree" property="parent">
    <tr id="<%= subpoolTree.getCurrentObjectHTMLId() %>" >
    </logic:notEmpty>
    <td id="p<%= (ctr.intValue() * 3) + 1 %>">
      <div id="p<%= (ctr.intValue() * 3) + 2 %>"
         class="tier<%= subpoolTree.getCurrentLevel() %>" > <h:multibox property="selectedPools">
        <bean:write name="pool" property="id"/> </h:multibox> <logic:empty name="subpoolTree" property="childList">
        <a id="p<%= (ctr.intValue() * 3) + 3 %>" class="doc" >
<!-- need this following line for Mozilla -->
<img border="0" width="17" src="../images/spacer.gif">
</a>
        </logic:empty> <logic:notEmpty name="subpoolTree" property="childList">
        <a name="p<%= (ctr.intValue() * 3) + 3 %>" id="p<%= (ctr.intValue() * 3) + 3 %>"
             href="#<%=pool.getId()%>" onclick="toggleRows(this)" class="folder">
<!-- need this following line for Mozilla -->
<img border="0" width="17" src="../images/spacer.gif">
</a> </logic:notEmpty>
        <h:link page="/startCreatePool.do?use=edit" paramName="pool" paramProperty="id" paramId="id">
        <bean:write name="pool" property="displayName" /> </h:link> </div></td>
    <logic:iterate name="subpoolTree" property="currentObjectProperties"
       id="props" indexId="propctr">
    <td> <logic:equal name="propctr" value="1"> <bean:write name="props" format="MM/dd/yyyy" />
      </logic:equal> <logic:equal name="propctr" value="2"> <logic:equal name="props" value="0">
      -- </logic:equal> <logic:notEqual name="props" value="0"> <bean:write name="props" />
      </logic:notEqual> </logic:equal> <logic:equal name="propctr" value="3">
      <logic:equal name="props" value="0"> -- </logic:equal> <logic:notEqual name="props" value="0">
      <bean:write name="props" /> </logic:notEqual> </logic:equal> <logic:equal name="propctr" value="0">
      <bean:write name="props" /> </logic:equal> </td>
    </logic:iterate>
    <td> <input type="button" value="Add Subpool"  onclick="document.location='<%=request.getContextPath()%>/startCreatePool.do?id=<%=subpoolTree.getCurrentId().toString()%>&use=createsub&pid=<%=subpoolTree.getCurrentId().toString()%>'">
    </td>
    </tr>
    </logic:iterate>
  </table>
</div>

</h:form>
  <!-- end content -->
      </body>
    </html>
  </f:view>
