<?xml version="1.0" encoding="UTF-8"?>
<jsp:root version="3.0"
    xmlns:jsp="http://java.sun.com/JSP/Page"
    xmlns:h="http://java.sun.com/jsf/html"
    xmlns:f="http://java.sun.com/jsf/core"
    xmlns:samigo="http://www.sakaiproject.org/samigo">
<jsp:directive.page pageEncoding="UTF-8" contentType="text/html;charset=utf-8" trimDirectiveWhitespaces="false" isELIgnored="false" deferredSyntaxAllowedAsLiteral="false"/>
<jsp:output omit-xml-declaration="yes"/>
  <jsp:directive.include file="/include/header.inc"/>
  <!-- $Id: transferPoolConfirm.jsp 2012-11-10 wang58@iupui.edu  -->
  <jsp:directive.page language="java"/>
  <jsp:text><![CDATA[




<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
]]></jsp:text>
  <f:view>
    <html xmlns="http://www.w3.org/1999/xhtml">
      <head>
        <jsp:expression> request.getAttribute("html.head") </jsp:expression>
        <title>
          <h:outputText value="#{questionPoolMessages.transfer_pool_confirmation}"/>
        </title>
        <script language="javascript">
          <jsp:directive.include file="/js/samigotree.js"/>
        </script>
        <jsp:element name="script">
          <jsp:attribute name="src" trim="false">/library/js/spinner.js</jsp:attribute>
          <jsp:body>
          </jsp:body>
        </jsp:element>
      </head>
      <jsp:element name="body">
        <jsp:attribute name="onload" trim="false"><jsp:expression> request.getAttribute("html.body.onload") </jsp:expression></jsp:attribute>
        <jsp:body>
          <div class="portletBody">
            <h:form id="transferPoolConfirm">
              <h:messages infoClass="validation" warnClass="validation" errorClass="validation" fatalClass="validation"/>
              <h3>
                <h:outputText value="#{questionPoolMessages.transfer_pool_ownership}"/>
              </h3>
              <jsp:element name="p">
                <jsp:body>
                </jsp:body>
              </jsp:element>
              <div>
                <h:outputText value="#{questionpool.confirmMessage}"/>
              </div>
              <jsp:element name="p">
                <jsp:body>
                </jsp:body>
              </jsp:element>
              <h:dataTable id="TreeTable" value="#{questionpool.transferSelectedQpools}" var="pool" cellpadding="0" cellspacing="0" styleClass="listHier">
                <h:column id="col1">
                  <f:facet name="header">
                    <h:panelGroup>
                      <h:outputText value="#{questionPoolMessages.p_name}"/>
                    </h:panelGroup>
                  </f:facet>
                  <h:panelGroup styleClass="tier#{questionpool.tree.currentLevel}" id="firstcolumn">
                    <h:inputHidden id="rowid" value="#{questionpool.tree.currentObjectHTMLId}"/>
                    <h:outputText id="poolnametext" value="#{pool.displayName}" escape="false"/>
                  </h:panelGroup>
                </h:column>
                <h:column id="col2">
                  <f:facet name="header">
                    <h:panelGroup>
                      <h:outputText value="#{questionPoolMessages.creator}"/>
                    </h:panelGroup>
                  </f:facet>
                  <h:panelGroup id="secondcolumn">
                    <h:outputText value="#{pool.ownerDisplayName}"/>
                  </h:panelGroup>
                </h:column>
                <h:column id="col3">
                  <f:facet name="header">
                    <h:panelGroup>
                      <h:outputText value="#{questionPoolMessages.last_mod}"/>
                    </h:panelGroup>
                  </f:facet>
                  <h:panelGroup id="thirdcolumn">
                    <h:outputText value="#{pool.lastModified}">
                      <f:convertDateTime dateStyle="medium" timeStyle="short" timeZone="#{author.userTimeZone}"/>
                    </h:outputText>
                  </h:panelGroup>
                </h:column>
                <h:column id="col4">
                  <f:facet name="header">
                    <h:panelGroup>
                      <h:outputText value="#{questionPoolMessages.qs}"/>
                    </h:panelGroup>
                  </f:facet>
                  <h:panelGroup id="fourthcolumn">
                    <h:outputText value="#{pool.data.questionPoolItemSize}"/>
                  </h:panelGroup>
                </h:column>
                <h:column id="col5">
                  <f:facet name="header">
                    <h:panelGroup>
                      <h:outputText value="#{questionPoolMessages.subps}"/>
                    </h:panelGroup>
                  </f:facet>
                  <h:panelGroup id="fifthcolumn">
                    <h:outputText value="#{pool.subPoolSize}"/>
                  </h:panelGroup>
                </h:column>
              </h:dataTable>
              <p class="act">
                <h:commandButton accesskey="#{questionPoolMessages.a_transfer}" id="transferpoolSubmit" immediate="true" value="#{questionPoolMessages.transfer_pool_ownership}" action="#{questionpool.transferPoolOwnership}" styleClass="active" onclick="SPNR.disableControlsAndSpin(this, null);"/>
                <h:commandButton accesskey="#{questionPoolMessages.a_transfer_back}" id="transferpoolSubmit2" immediate="true" value="#{questionPoolMessages.transfer_pool_back}" action="#{questionpool.transferPoolConfirmBack}" styleClass="active" onclick="SPNR.disableControlsAndSpin(this, null);"/>
                <h:commandButton accesskey="#{questionPoolMessages.a_cancel}" id="transferpoolCancel" value="#{questionPoolMessages.transfer_pool_cancel}" action="#{questionpool.cancelTransferPool}" immediate="true" onclick="SPNR.disableControlsAndSpin(this, null);"/>
              </p>
            </h:form>
          </div>
        </jsp:body>
      </jsp:element>
    </html>
  </f:view>
</jsp:root>
