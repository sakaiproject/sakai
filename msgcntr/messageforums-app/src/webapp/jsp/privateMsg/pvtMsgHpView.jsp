<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
<%@ taglib uri="http://sakaiproject.org/jsf/messageforums" prefix="mf" %>
<jsp:useBean id="msgs" class="org.sakaiproject.util.ResourceLoader" scope="session">
   <jsp:setProperty name="msgs" property="baseName" value="org.sakaiproject.tool.messageforums.bundle.Messages"/>
</jsp:useBean>

<f:view>
  <sakai:view title="#{msgs.cdfm_message_pvtarea}">
<!--jsp/privateMsg/pvtMsgHpView.jsp-->    
      <h:form id="msgForum">
        <div class="breadCrumb specialLinks">
			<h3>
			<h:commandLink action="#{PrivateMessagesTool.processActionHome}" value="#{msgs.cdfm_message_forums}" title=" #{msgs.cdfm_message_forums}"/> /
			<h:outputText value="#{msgs.pvt_message_nav}"/>
		  </h3>
        </div>
				<h:inputHidden id="mainOrHp" value="pvtMsgHpView" />
        <%@include file="pvtArea.jsp"%>
      </h:form>
  </sakai:view>
</f:view> 
