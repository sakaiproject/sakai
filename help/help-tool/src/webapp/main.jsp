<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core" %>

<%@ taglib prefix="fmt" uri="http://java.sun.com/jstl/fmt" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
<%-- Custom tag library just for this tool --%>
<%@ taglib uri="http://sakaiproject.org/jsf/help" prefix="help" %>

<f:view>
	<help:helpFrameSet 
    	helpWindowTitle="#{msgs.help}"
	    searchToolUrl="#{requestScope.searchURL}" 
    	tocToolUrl="#{requestScope.tocURL}" 
    	helpUrl="#{requestScope.helpURL}"
	/>
</f:view>
