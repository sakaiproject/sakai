<!-- $Id: transferPool.jsp 2012-11-10 wang58@iupui.edu -->

<%@ page contentType="text/html;charset=utf-8" pageEncoding="utf-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://www.sakaiproject.org/samigo" prefix="samigo" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<f:view>
    <html xmlns="http://www.w3.org/1999/xhtml">
        <head><%= request.getAttribute("html.head") %>
            <title>
                <h:outputText value="#{questionPoolMessages.transfer_pool}" />
            </title>

            <!-- stylesheet and script widgets -->
            <script language="javascript" type="text/JavaScript">
                <%@ include file="/js/samigotree.js" %>
                function initPage()
                {
                    updateButtonStatusOnCheck(document.getElementById('transferPool:transferpoolSubmit'), document.getElementById('transferPool'));
                }
              window.onload = initPage;
            </script>
            <script src="/library/js/spinner.js" type="text/javascript"></script>
        </head>
    <body onload="disableCheckboxes();<%= request.getAttribute("html.body.onload") %>">

            <!-- content... -->
            <div class="portletBody">
                <h:form id="transferPool">
                    <h:messages infoClass="validation" warnClass="validation" errorClass="validation" fatalClass="validation"/>

                    <h3>
                        <h:outputText value="#{questionPoolMessages.transfer_pool_ownership}"/>
                    </h3>
                    <h:inputHidden id="checkAll" value="" />
                    <h:inputHidden id="disabledCheckboxes" value="" />

                    <br/>
                    <div class="tier2">
                        <h:selectBooleanCheckbox id="checkAllCheckbox" onclick="checkAllCheckboxes(this);updateButtonStatusOnCheck(document.getElementById('transferPool:transferpoolSubmit'), document.getElementById('transferPool')); " value="#{questionpool.checkAll}" />
                        <h:outputText value="#{questionPoolMessages.transfer_pool_select_all}" />
                    </div>

                    <div class="longtext tier2">
                            <%@ include file="/jsf/questionpool/transferPoolTree.jsp" %>
                    </div>

                    <p class="act">
                        <h:commandButton accesskey="#{questionPoolMessages.a_transfer}" id="transferpoolSubmit" immediate="true" 
                            value="#{questionPoolMessages.tranfer_pool_continue}" action="#{questionpool.transferPoolContinue}" styleClass="active" 
                            onclick="passSelectedPoolIds();SPNR.disableControlsAndSpin(this, null);" />
                        <h:commandButton accesskey="#{questionPoolMessages.a_cancel}" id="transferpoolCancel" value="#{questionPoolMessages.transfer_pool_cancel}" 
                            action="#{questionpool.cancelTransferPool}" immediate="true" onclick="SPNR.disableControlsAndSpin(this, null);"/>
                    </p>
                </h:form>
            </div>
        </body>
    </html>
</f:view>
