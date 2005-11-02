<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>

<f:view>
<sakai:view title="alphaIndex tag - Sakai 2.0 JSF example">


<hr />
<h2>Private message- Received</h2>
<hr />

<h:commandButton value="Cancel" action="#{PrivateMessagesTool.processPvtMsgCancel}"/> 
 
    
</sakai:view>
</f:view>
