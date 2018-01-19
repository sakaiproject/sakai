<!-- $Id: transferPoolConfirm.jsp 2012-11-10 wang58@iupui.edu -->

<%@ page contentType="text/html;charset=utf-8" pageEncoding="utf-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://www.sakaiproject.org/samigo" prefix="samigo" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<f:view>
    <html xmlns="http://www.w3.org/1999/xhtml">
        <head>
            <%= request.getAttribute("html.head") %>
            <title><h:outputText value="#{questionPoolMessages.transfer_pool_confirmation}" /></title>
            
            <script language="javascript" type="text/JavaScript">
                <%@ include file="/js/samigotree.js" %>
            </script>
            <samigo:script path="/../library/js/spinner.js"/>

            <samigo:stylesheet path="/css/tool_sam.css" />
        </head>
        <body onload="<%= request.getAttribute("html.body.onload") %>">
            <div class="portletBody">
                <h:form id="transferPoolConfirm">
                    <h:messages infoClass="validation" warnClass="validation" errorClass="validation" fatalClass="validation" />

                    <h3>
                        <h:outputText value="#{questionPoolMessages.transfer_pool_ownership}"/>
                    </h3>
                    <p></p>
                    <div>
                        <h:outputText value="#{questionpool.confirmMessage}" />
                    </div>
                    <p></p>

                    <h:dataTable id="TreeTable" value="#{questionpool.transferSelectedQpools}"
                        var="pool" cellpadding="0" cellspacing="0" styleClass="listHier" >

                        <h:column id="col1">
                            <f:facet name="header">
                                <h:panelGroup>      
                                    <h:outputText  value="#{questionPoolMessages.p_name}" />
                                </h:panelGroup>
                            </f:facet>
                            <h:panelGroup styleClass="tier#{questionpool.tree.currentLevel}"  id="firstcolumn">
                                <h:inputHidden id="rowid" value="#{questionpool.tree.currentObjectHTMLId}" />
                                <h:outputText id="poolnametext" value="#{pool.displayName}" escape="false" />
                            </h:panelGroup>
                        </h:column>

                        <h:column id="col2">
                            <f:facet name="header">
                                <h:panelGroup>      
                                    <h:outputText  value="#{questionPoolMessages.creator}" />
                                </h:panelGroup>
                            </f:facet>
                            <h:panelGroup id="secondcolumn">
                                <h:outputText value="#{pool.ownerDisplayName}" />
                            </h:panelGroup>
                        </h:column>

                        <h:column id="col3">
                            <f:facet name="header">
                                <h:panelGroup>
                                    <h:outputText value="#{questionPoolMessages.last_mod}" />
                                </h:panelGroup>
                            </f:facet>
                            <h:panelGroup id="thirdcolumn">
                                <h:outputText value="#{pool.lastModified}">
                                    <f:convertDateTime pattern="#{generalMessages.output_date_picker}" />
                                </h:outputText>
                            </h:panelGroup>
                        </h:column>

                        <h:column id="col4">
                            <f:facet name="header">
                                <h:panelGroup>
                                    <h:outputText value="#{questionPoolMessages.qs}" />
                                </h:panelGroup>
                            </f:facet>
                            <h:panelGroup id="fourthcolumn" >
                                <h:outputText value="#{pool.data.questionPoolItemSize}" />
                            </h:panelGroup>
                        </h:column>

                        <h:column id="col5">
                            <f:facet name="header">
                                <h:panelGroup>
                                    <h:outputText value="#{questionPoolMessages.subps}" />
                                </h:panelGroup>
                            </f:facet>
                            <h:panelGroup id="fifthcolumn">
                                <h:outputText value="#{pool.subPoolSize}" />
                            </h:panelGroup>
                        </h:column>
                    </h:dataTable>

                    <p class="act">
                        <h:commandButton accesskey="#{questionPoolMessages.a_transfer}" id="transferpoolSubmit" immediate="true"
                            value="#{questionPoolMessages.transfer_pool_ownership}" action="#{questionpool.transferPoolOwnership}" styleClass="active"
                            onclick="SPNR.disableControlsAndSpin(this, null);"/>
                        <h:commandButton accesskey="#{questionPoolMessages.a_transfer_back}" id="transferpoolSubmit2" immediate="true"
                            value="#{questionPoolMessages.transfer_pool_back}" action="#{questionpool.transferPoolConfirmBack}" styleClass="active"
                            onclick="SPNR.disableControlsAndSpin(this, null);"/>
                        <h:commandButton accesskey="#{questionPoolMessages.a_cancel}" id="transferpoolCancel" value="#{questionPoolMessages.transfer_pool_cancel}" 
                            action="#{questionpool.cancelTransferPool}" immediate="true" onclick="SPNR.disableControlsAndSpin(this, null);"/>
                    </p>
                </h:form>
            </div>
        </body>
    </html>
</f:view>
