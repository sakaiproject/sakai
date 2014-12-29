<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jstl/fmt" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
<%-- Custom tag library just for this tool --%>
<%@ taglib uri="http://sakaiproject.org/jsf/help" prefix="help" %>

<f:view>
<h:outputText value="#{msgs.table_of_contents}" />
<help:tocTree value="#{TableOfContentsTool.tableOfContents.categories}"/>
</f:view>
<%--
<f:view>
	<h:outputText value="Table of Contents" />
	
	<sakai:flat_list value="#{TableOfContentsTool.tableOfContents.categories}" var="cat">
	           <h:column>
		    	   <h:graphicImage value="../image/toc_open.gif"/>
	           </h:column>
         <h:column>
            <h:outputText value="#{cat.name}" />
            <sakai:flat_list value="#{cat.resources}" var="resource">
            	   <h:column>
	    	   <h:graphicImage value="../image/topic.gif"/>
	           </h:column>
                   <h:column>
			  <h:outputLink value="#{resource.location}" target="content">
			    <h:outputText value="#{resource.name}" />
			  </h:outputLink>
	           </h:column>
	    </sakai:flat_list>
          </h:column>
   </sakai:flat_list>
</f:view>
--%>



