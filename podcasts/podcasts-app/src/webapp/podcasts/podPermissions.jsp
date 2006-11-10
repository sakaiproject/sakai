<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
<% response.setContentType("text/html; charset=UTF-8"); %>

<f:loadBundle basename="org.sakaiproject.api.podcasts.bundle.Messages" var="msgs"/>

<f:view>
    <link href="/library/skin/tool_base.css" type="text/css" rel="stylesheet" media="all" />
    <link href="/library/skin/default/tool.css" type="text/css" rel="stylesheet" media="all" />
    <link href="./css/podcaster.css" type="text/css" rel="stylesheet" media="all" />

    <script type="text/javascript" language="JavaScript" src="/library/js/headscripts.js"></script>
    <script type="text/javascript" language="JavaScript" src="scripts/popupscripts.js"></script>

  <sakai:view>
    <h:form>
	  <div>
    		<h3><h:outputText value="#{msgs.perm_title}" /></h3>
    		<div class="instruction" >
        	  <h:outputText value="#{msgs.perm_directions} #{podPerms.siteName} (#{podPerms.siteId})" styleClass="indnt1"/>

        <!-- TODO: pull down site name (id) and add to message above-->
		</div>
  	  </div>
       <br />

	<%-- Dymanic table --%>
	<h:dataTable value="#{podPerms.permTableDataList}" var="cellItem" binding="#{podPerms.permDataTable}" />


<br><br><br>
	<%-- Using selectManyCheckbox tag --%>
	<h:dataTable value="#{podPerms.checkboxTableValues}" var="permItem" >
	  <h:column>
	    <h:outputText value="#{permItem.rowName}" />
	  </h:column>
	  <h:column>
	    <h:selectManyCheckbox value="" >
	      <f:selectItems value="#{permItem.checkboxSelectValues}" />
	    </h:selectManyCheckbox>
	  </h:column>
	</h:dataTable>
	
     <sakai:button_bar>
      <sakai:button_bar_item action="#{podPerms.processPermChange}" value="#{msgs.change_submit}" 
          accesskey="s" title="Save Podcast Permissions" styleClass="active" />
      <sakai:button_bar_item action="#{podPerms.processPermCancel}" value="#{msgs.cancel}"
          accesskey="c" title="Cancel Podcast Permission Change" />
    </sakai:button_bar>
  </h:form>
 </sakai:view>
</f:view>