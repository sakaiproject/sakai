<%@ page contentType="text/html;charset=utf-8" pageEncoding="utf-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://www.sakaiproject.org/samigo" prefix="samigo" %>
<!--
* $Id$
<%--
***********************************************************************************
*
* Copyright (c) 2005 The Regents of the University of Michigan, Trustees of Indiana University,
*                  Board of Trustees of the Leland Stanford, Jr., University, and The MIT Corporation
*
* Licensed under the Educational Community License Version 1.0 (the "License");
* By obtaining, using and/or copying this Original Work, you agree that you have read,
* understand, and will comply with the terms and conditions of the Educational Community License.
* You may obtain a copy of the License at:
*
*      http://cvs.sakaiproject.org/licenses/license_1_0.html
*
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
* INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
* AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
* DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
* FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*
**********************************************************************************/
--%>
-->
  <f:view>
    <f:verbatim><!DOCTYPE html
     PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
     "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
    </f:verbatim>
    <f:loadBundle
     basename="org.sakaiproject.tool.assessment.bundle.AuthorMessages"
     var="msg"/>
    <html xmlns="http://www.w3.org/1999/xhtml">
      <head><%= request.getAttribute("html.head") %>
      <title><h:outputText value="#{msg.item_display_author}"/></title>
      </head>
      <body onload="<%= request.getAttribute("html.body.onload") %>">
<!-- content... -->
<!-- FORM -->
<h:form id="itemauthor">

<!-- CHANGE TYPE -->
<h:outputText value="Test Create an Item.  (all work, except matching ) " />
<f:verbatim><br/></f:verbatim>
<h:outputText value="#{msg.change_q_type}"/>


<%-- pulldown with button
<h:selectOneMenu  value="#{itemauthor.itemType}">
  <f:selectItem itemLabel="#{msg.select_qtype}" itemValue=""/>
  <f:selectItem itemLabel="#{msg.multiple_choice_type}" itemValue="1"/>
  <f:selectItem itemLabel="#{msg.multiple_choice_surv}" itemValue="3"/>
  <f:selectItem itemLabel="#{msg.short_answer_essay}" itemValue="5"/>
  <f:selectItem itemLabel="#{msg.fill_in_the_blank}" itemValue="8"/>
  <f:selectItem itemLabel="#{msg.matching}" itemValue="9"/>
  <f:selectItem itemLabel="#{msg.true_false}" itemValue="4"/>
  <f:selectItem itemLabel="#{msg.audio_recording}" itemValue="7"/>
  <f:selectItem itemLabel="#{msg.file_upload}" itemValue="6"/>
  <f:selectItem itemLabel="#{msg.import_from_q}" itemValue="10"/>
</h:selectOneMenu>

<h:commandButton type="submit"  action="#{itemauthor.doit}" value="#{msg.change}">
        <f:actionListener
           type="org.sakaiproject.tool.assessment.ui.listener.author.StartCreateItemListener" />

</h:commandButton>

<h:selectOneMenu  id="pulldown" onchange="document.forms['itemauthor']['itemauthor:_idcl'].value='itemauthor:hiddenlink'; document.forms['itemauthor'].submit(); return true;" value="#{itemauthor.itemType}" >
      <f:valueChangeListener
           type="org.sakaiproject.tool.assessment.ui.listener.author.StartCreateItemListener" />
--%>






<h:selectOneMenu  id="pulldown" onchange="document.links[0].onclick();"  value="#{itemauthor.itemType}" >
      <f:valueChangeListener
           type="org.sakaiproject.tool.assessment.ui.listener.author.StartCreateItemListener" />
  <f:selectItem itemLabel="#{msg.select_qtype}" itemValue=""/>
  <f:selectItem itemLabel="#{msg.multiple_choice_type}" itemValue="1"/>
  <f:selectItem itemLabel="#{msg.multiple_choice_surv}" itemValue="3"/>
  <f:selectItem itemLabel="#{msg.short_answer_essay}" itemValue="5"/>
  <f:selectItem itemLabel="#{msg.fill_in_the_blank}" itemValue="8"/>
  <f:selectItem itemLabel="#{msg.matching}" itemValue="9"/>
  <f:selectItem itemLabel="#{msg.true_false}" itemValue="4"/>
  <f:selectItem itemLabel="#{msg.audio_recording}" itemValue="7"/>
  <f:selectItem itemLabel="#{msg.file_upload}" itemValue="6"/>
  <f:selectItem itemLabel="#{msg.import_from_q}" itemValue="10"/>
</h:selectOneMenu>

<h:commandLink id="hiddenlink" action="#{itemauthor.doit}" value="">
</h:commandLink>

<f:verbatim><br/></f:verbatim>
<f:verbatim><br/></f:verbatim>
<f:verbatim><br/></f:verbatim>
<!-- testing modify -->
<h:outputText value="Test Modify (all work except for matching and multiplechoice) " />
<f:verbatim><br/></f:verbatim>
   <h:commandLink id="modify" action="#{itemauthor.doit}">
          <h:outputText value="modify" />
        <f:actionListener
           type="org.sakaiproject.tool.assessment.ui.listener.author.ItemModifyListener" />
<f:param name="itemid" value="109"/>
        </h:commandLink>



<f:verbatim><br/></f:verbatim>
<f:verbatim><br/></f:verbatim>
<f:verbatim><br/></f:verbatim>
<!-- testing modify -->
   <h:commandLink id="delete" action="#{itemauthor.deleteItem}">
          <h:outputText value="test delete" />
   <f:param name="itemid" value="57"/>
        </h:commandLink>



<h:selectOneRadio layout="lineDirection" onclick="submit()" value="1" >
      <f:selectItem itemValue="1"
        itemLabel="#{msg.single}" />
      <f:selectItem itemValue="2"
        itemLabel="#{msg.multipl_mc}" />
    </h:selectOneRadio>




</h:form>


<!-- end content -->
    </body>
  </html>
</f:view>
