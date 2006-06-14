<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
<% response.setContentType("text/html; charset=UTF-8"); %>

<f:loadBundle basename="org.sakaiproject.tool.podcasts.bundle.Messages" var="msgs"/>

<f:view>
  <sakai:view>
    <link href="/library/skin/tool_base.css" type="text/css" rel="stylesheet" media="all" />
    <link href="/library/skin/default/tool.css" type="text/css" rel="stylesheet" media="all" />
    <link href="./css/podcaster.css" type="text/css" rel="stylesheet" media="all" />

    <script type="text/javascript" language="JavaScript" src="/library/js/headscripts.js"></script>
    <script type="text/javascript" language="JavaScript" src="scripts/popupscripts.js"></script>

  <div>
    <h3><h:outputText value="#{msgs.podcast_perm_title}" /></h3>
    <div styleclass="instruction" >
        <h:outputText value="#{msgs.podcast_perm_directions}" styleClass="indnt1"/>

        <!-- TODO: pull down site name (id) and add to message above-->

  </div>
  <br />

  <h:form>  
    <table class="listHier lines" cellpadding="0" cellspacing="0" border="0"
       summary="Table holds permissions based on role. Column 1 is role, column 2 is 
       New permission, column3 is Read permission, column 4 is Revise permission,
       column 5 is Delete permission">

      <tr class="navIntraTool">
        <th align="center"><h:outputText value="#{msgs.podcast_perm_role}" /></th>
        <th align="center"><h:outputText value="#{msgs.podcast_perm_new}" /></th>
        <th align="center"><h:outputText value="#{msgs.podcast_perm_read}" /></th>
        <th align="center"><h:outputText value="#{msgs.podcast_perm_revise}" /></th>
        <th align="center"><h:outputText value="#{msgs.podcast_perm_delete}" /></th>
      </tr>        

      <tr>
        <td><h:outputText value="#{msgs.podcast_perm_maint}" /></td>
        <td><h:selectBooleanCheckbox value="#{podPerms.mNew}" /></td>
        <td><h:selectBooleanCheckbox value="#{podPerms.mRead}" disabled="true" /></td>
        <td><h:selectBooleanCheckbox value="#{podPerms.mRevise}" /></td>
        <td><h:selectBooleanCheckbox value="#{podPerms.mDelete}" /></td>
      </tr>

      <tr>
        <td><h:outputText value="#{msgs.podcast_access}" /></td>
        <td><h:selectBooleanCheckbox value="#{podPerms.aNew}" /></td>
        <td><h:selectBooleanCheckbox value="#{podPerms.aRead}" disabled="true" /></td>
        <td><h:selectBooleanCheckbox value="#{podPerms.aRevise}" /></td>
        <td><h:selectBooleanCheckbox value="#{podPerms.aDelete}" /></td>
      </tr>
    </table>
    <br />

    <sakai:button_bar>
      <sakai:button_bar_item action="#{podPerms.processPermChange}" value="#{msgs.podcast_change_submit}" 
          accesskey="s" title="Save Podcast Permissions" styleClass="active" />
      <sakai:button_bar_item action="#{podPerms.processPermCancel}" value="#{msgs.podcast_cancel}"
          accesskey="c" title="Cancel Podcast Permission Change" />
    </sakai:button_bar>
  </h:form>
 </sakai:view>
</f:view>