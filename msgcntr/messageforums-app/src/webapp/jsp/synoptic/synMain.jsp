<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>

<f:loadBundle basename="org.sakaiproject.tool.messageforums.bundle.Messages" var="msgs"/>

<f:view>
  <sakai:view>
	<h:form>
     <sakai:tool_bar>
          <sakai:tool_bar_item action="synOptions" value="#{msgs.syn_options}" />
      </sakai:tool_bar>
   
		<h:dataTable value="#{mfSynopticBean.contents}" var="eachSite" >
        	<h:column>
				<h:outputText value="#{eachSite.siteName}" />
			</h:column>
			<h:column>
				<h:outputText value="#{eachSite.unreadPrivate}" />
			</h:column>
			<h:column>
				<h:outputText value="#{eachSite.unreadForums}" />
			</h:column>
		</h:dataTable>
    </h:form>
  </sakai:view>
 </f:view>
 
 