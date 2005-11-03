<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
<%@ taglib uri="http://sakaiproject.org/jsf/messageforums" prefix="mf" %> 
<f:loadBundle basename="org.sakaiproject.tool.messageforums.bundle.Messages" var="msgs"/>
<sakai:script contextBase="/sakai-jsf-resource" path="/hideDivision/hideDivision.js"/>
  
<f:view>
	<sakai:view title="Messge Forums">
		<h:form id="msgForum"> 
			<@include file="/privateMsg/pvtArea.jsp"/>
			<@include file="/discussionForum/dfArea.jsp"/>
		</h:form>
	</sakai:view>
</f:view>  
   