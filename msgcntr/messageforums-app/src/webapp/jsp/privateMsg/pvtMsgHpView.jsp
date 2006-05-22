<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
<%@ taglib uri="http://sakaiproject.org/jsf/messageforums" prefix="mf" %>
<f:loadBundle basename="org.sakaiproject.tool.messageforums.bundle.Messages" var="msgs"/>

<f:view>
  <sakai:view>
    <link href='/sakai-messageforums-tool/css/msgForums.css' rel='stylesheet' type='text/css' />  
    
      <h:form id="msgForum">
        <h:commandLink action="#{PrivateMessagesTool.processActionHome}" value="#{msgs.cdfm_message_forums}" /> /
        <h:outputText value="#{msgs.pvt_message_nav}"/>
        <%@include file="pvtArea.jsp"%>
      </h:form>
  </sakai:view>
</f:view> 
