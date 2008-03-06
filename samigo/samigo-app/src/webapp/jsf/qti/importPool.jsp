<%@ page contentType="text/html;charset=utf-8" pageEncoding="utf-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://www.sakaiproject.org/samigo" prefix="samigo" %>
<%@ taglib uri="http://java.sun.com/upload" prefix="corejsf" %>
<!DOCTYPE html
     PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
     "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<!-- $Id$
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
    <html xmlns="http://www.w3.org/1999/xhtml">
      <head><%= request.getAttribute("html.head") %>
      <title><h:outputText value="#{authorImportExport.import_qp}" /></title>
      </head>
      <body onload="<%= request.getAttribute("html.body.onload") %>">
 <div class="portletBody">
<!-- content... -->
 <h:form id="importPoolForm" enctype="multipart/form-data">

   <h:inputHidden value="#{xmlImport.importType}" />
   <h3><h:outputText  value="#{authorImportExport.import_qp}" /></h3>
    <div class="tier1">
     <div class="form_label">
      <h:messages infoClass="validation" warnClass="validation" errorClass="validation" fatalClass="validation"/>
      <%-- currently import pool mirrors import assessment --%>
      <h:outputText value="#{authorImportExport.import_instructions}"/>
    </div>
    <br />
   <div class="tier2">
   <h:outputLabel  styleClass="form_label" value="#{authorImportExport.choose_file}"/>
    <%-- target represents location where import will be temporarily stored
        check valueChangeListener for final destination --%>
    <corejsf:upload target="jsf/upload_tmp/qti_imports/#{person.id}"
      valueChangeListener="#{xmlImport.importPoolFromQti}"/>
   </div>
    <br/>
    <br/>
     <%-- activates the valueChangeListener --%>
     <h:commandButton id="questionPoolsLink" value="#{authorImportExport.import_action}" type="submit"
       style="act" action="poolList" >
       <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.questionpool.QuestionPoolListener" />
	 </h:commandButton>
     <%-- immediate=true bypasses the valueChangeListener --%>
     <h:commandButton value="#{authorImportExport.import_cancel_action}" type="submit"
       style="act" action="poolList" immediate="true"/>
  </div>

 </h:form>
</div>

 <!-- end content -->
      </body>
    </html>
  </f:view>
