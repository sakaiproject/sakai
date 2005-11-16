<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
<%@ taglib uri="http://sakaiproject.org/jsf/messageforums" prefix="mf" %>
<f:loadBundle basename="org.sakaiproject.tool.messageforums.bundle.Messages" var="msgs"/>
<link href='/sakai-messageforums-tool/css/msgForums.css' rel='stylesheet' type='text/css' />
<f:view>
  <sakai:view_container title="#{msgs.cdfm_container_title}">
    <sakai:view_content>
      <h:form id="msgForum">
      	<sakai:script contextBase="/sakai-jsf-resource" path="/hideDivision/hideDivision.js"/>
        <%@include file="privateMsg/pvtArea.jsp"%>
        <%@include file="discussionForum/dfArea.jsp"%>
      </h:form>
    </sakai:view_content>
  </sakai:view_container>
</f:view> 
