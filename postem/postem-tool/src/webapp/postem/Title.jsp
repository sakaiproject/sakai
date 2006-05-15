<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>

<f:loadBundle basename="org.sakaiproject.tool.postem.bundle.Messages" var="msgs"/>
<f:view>
<sakai:view title="Post'Em Tool">

	<sakai:title_bar value="#{msgs.title}" helpDocId="postem_overview"/>

</sakai:view>
</f:view>