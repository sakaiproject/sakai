<?xml version="1.0" encoding="UTF-8"?>
<jsp:root version="3.0"
    xmlns:jsp="http://java.sun.com/JSP/Page"
    xmlns:h="http://java.sun.com/jsf/html"
    xmlns:f="http://java.sun.com/jsf/core"
    xmlns:samigo="http://www.sakaiproject.org/samigo">
<jsp:directive.page pageEncoding="UTF-8" contentType="text/html;charset=utf-8" trimDirectiveWhitespaces="false" isELIgnored="false" deferredSyntaxAllowedAsLiteral="false"/>
<jsp:output omit-xml-declaration="yes"/>
  <jsp:directive.include file="/include/header.inc"/>
  <!-- $Id: transferPool.jsp 2012-11-10 wang58@iupui.edu  -->
  <jsp:directive.page language="java"/>
  <jsp:text><![CDATA[




<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
]]></jsp:text>
  <f:view>
    <html xmlns="http://www.w3.org/1999/xhtml">
      <head>
        <jsp:expression> request.getAttribute("html.head") </jsp:expression>
        <title>
          <h:outputText value="#{questionPoolMessages.transfer_pool}"/>
        </title>
        <!-- stylesheet and script widgets  -->
        <script language="javascript">
          <jsp:directive.include file="/js/samigotree.js"/>
          <jsp:text>
                function initPage()
                {
                    updateButtonStatusOnCheck(document.getElementById('transferPool:transferpoolSubmit'), document.getElementById('transferPool'));
                }
              window.onload = initPage;
            </jsp:text>
        </script>
        <jsp:element name="script">
          <jsp:attribute name="src" trim="false">/library/js/spinner.js</jsp:attribute>
          <jsp:body>
          </jsp:body>
        </jsp:element>
      </head>
      <jsp:element name="body">
        <jsp:attribute name="onload" trim="false">disableCheckboxes();<jsp:expression> request.getAttribute("html.body.onload") </jsp:expression></jsp:attribute>
        <jsp:body>
          <!-- content...  -->
          <div class="portletBody">
            <h:form id="transferPool">
              <h:messages infoClass="validation" warnClass="validation" errorClass="validation" fatalClass="validation"/>
              <h3>
                <h:outputText value="#{questionPoolMessages.transfer_pool_ownership}"/>
              </h3>
              <h:inputHidden id="checkAll" value=""/>
              <h:inputHidden id="disabledCheckboxes" value=""/>
              <br/>
              <div class="tier2">
                <h:selectBooleanCheckbox id="checkAllCheckbox" onclick="checkAllCheckboxes(this);updateButtonStatusOnCheck(document.getElementById('transferPool:transferpoolSubmit'), document.getElementById('transferPool')); " value="#{questionpool.checkAll}"/>
                <h:outputText value="#{questionPoolMessages.transfer_pool_select_all}"/>
              </div>
              <div class="longtext tier2">
                <jsp:directive.include file="/jsf/questionpool/transferPoolTree.jsp"/>
              </div>
              <p class="act">
                <h:commandButton accesskey="#{questionPoolMessages.a_transfer}" id="transferpoolSubmit" immediate="true" value="#{questionPoolMessages.tranfer_pool_continue}" action="#{questionpool.transferPoolContinue}" styleClass="active" onclick="passSelectedPoolIds();SPNR.disableControlsAndSpin(this, null);"/>
                <h:commandButton accesskey="#{questionPoolMessages.a_cancel}" id="transferpoolCancel" value="#{questionPoolMessages.transfer_pool_cancel}" action="#{questionpool.cancelTransferPool}" immediate="true" onclick="SPNR.disableControlsAndSpin(this, null);"/>
              </p>
            </h:form>
          </div>
        </jsp:body>
      </jsp:element>
    </html>
  </f:view>
</jsp:root>
