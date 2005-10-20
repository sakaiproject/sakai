<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %> 

<% response.setContentType("text/html; charset=UTF-8"); %>

<f:loadBundle basename="org.sakaiproject.tool.messageforums.bundle.Messages" var="msgs"/>

<f:view>

<sakai:view_container title="Messge Forums">
  <sakai:view_content>
    <sakai:hideDivision title="testing">
      hi! 	
    </sakai:hideDivision>
  </sakai:view_content>	
</sakai:view_container>

</f:view> 
