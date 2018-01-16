<!-- $Id: transferPoolInputUser.jsp 2012-11-10 wang58@iupui.edu -->

<%@ page contentType="text/html;charset=utf-8" pageEncoding="utf-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://www.sakaiproject.org/samigo" prefix="samigo" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<f:view>
    <html xmlns="http://www.w3.org/1999/xhtml">
        <head>
            <%= request.getAttribute("html.head") %>
            <title><h:outputText value="#{questionPoolMessages.transfer_pool_user}"/></title>
            
            <script language="javascript" type="text/JavaScript">
                <%@ include file="/js/samigotree.js" %>
            </script>
			<samigo:script path="/../library/js/spinner.js"/>
        </head>
        <body onload="<%= request.getAttribute("html.body.onload") %>">
            <div class="portletBody">
                <h:form id="transferPoolInputUser">
                    <h:messages infoClass="validation" warnClass="validation" errorClass="validation" fatalClass="validation"/>

                    <h3>
                        <h:outputText value="#{questionPoolMessages.transfer_pool_ownership}" />
                    </h3>
                    <p></p>
                    <div class="tier2">
                        <h:outputText value="#{questionPoolMessages.transfer_pool_input_user}" />
                    </div>
                    <p></p>
                    <div class="tier3">
                        <h:inputText id="owneruserId" value="#{questionpool.ownerId}" />
                    </div>

                    <p class="act">
                        <h:commandButton accesskey="#{questionPoolMessages.a_transfer}" id="transferpoolSubmit" immediate="true"
                            value="#{questionPoolMessages.tranfer_pool_continue}" action="#{questionpool.transferPoolInputUserContinue}" styleClass="active"
                            onclick="SPNR.disableControlsAndSpin(this, null);"/>
                        <h:commandButton accesskey="#{questionPoolMessages.a_transfer_back}" id="transferpoolSubmit2" immediate="true"
                            value="#{questionPoolMessages.transfer_pool_back}" action="#{questionpool.transferPoolInputUserBack}" styleClass="active"
                            onclick="SPNR.disableControlsAndSpin(this, null);"/>
                        <h:commandButton accesskey="#{questionPoolMessages.a_cancel}" id="transferpoolCancel" value="#{questionPoolMessages.transfer_pool_cancel}" 
                            action="#{questionpool.cancelTransferPool}" immediate="true" onclick="SPNR.disableControlsAndSpin(this, null);"/>
                    </p>
                </h:form>
            </div>
        </body>
    </html>
</f:view>
