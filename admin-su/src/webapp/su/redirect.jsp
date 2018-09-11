<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>

<%
	response.setContentType("text/html; charset=UTF-8");
	response.addHeader("Cache-Control", "no-store, no-cache");
%>

<f:view>
<sakai:view_container title="#{msgs.title}">
  <h2>&nbsp;&nbsp;switching user, forwarding to <h:outputText value="#{SuTool.portalUrl}"/></h2>
  <script type="text/javascript">
    if (parent){
      parent.location.replace('<h:outputText value="#{SuTool.portalUrl}"/>');
    }
  </script>
</sakai:view_container>
</f:view>
