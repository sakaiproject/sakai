<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>

<f:loadBundle basename="org.sakaiproject.tool.messageforums.bundle.Messages" var="msgs"/>

<f:view>
  <sakai:view>
    <h:form>
	  	This options page is under construction. Just testing navigation to/from.
  	
    	<sakai:button_bar>
      		<sakai:button_bar_item action="#{mfSynopticBean.processOptionsChange}" value="#{msgs.syn_change_submit}"
            	accesskey="s" title="Save Synoptic Message Center Options" styleClass="active"/>
      		<sakai:button_bar_item action="#{mfSynopticBean.processOptionsCancel}" value="#{msgs.syn_cancel}" 
            	accesskey="c" title="Cancel Synoptic Message Center Options" />
    	</sakai:button_bar>
    </h:form>
  </sakai:view>
</f:view>