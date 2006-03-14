<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
<%@ taglib uri="http://sakaiproject.org/jsf/messageforums" prefix="mf" %>
<f:loadBundle basename="org.sakaiproject.tool.messageforums.bundle.Messages" var="msgs"/>
<link href='/sakai-messageforums-tool/css/msgForums.css' rel='stylesheet' type='text/css' />

<f:view>
  <sakai:view_container title="#{msgs.cdfm_container_title}">
    <sakai:view_content>
      <h:form id="addGroupsUsers"> 
                
        <sakai:tool_bar_message value="Add Individuals/Groups" />
        <sakai:instruction_message value="To add Individuals or Groups to the permissions list, select one or more users from the list and click Add, then click Save."/>
        
        <div style="margin:1em; display:inline">
          <h:selectManyListbox id="list1" size="10" style="width:150px;">
            <f:selectItems value="#{ForumTool.totalGroupsUsersList}"/>
          </h:selectManyListbox>    
        </div>
                
        <div class="act" style="display:inline; vertical-align:middle">
          <table style="display:inline">
            <tr><td>
            <h:commandButton value="      #{msgs.cdfm_add_button}     "/>
            <tr><td>
            <h:commandButton value="#{msgs.cdfm_remove_button}"/>
          </table>        
        </div>        
        
        <div style="margin:1em; display:inline">
          <h:selectManyListbox id="list2" size="10" style="width:150px;"/>        
        </div>
          			        	    		        
        <sakai:button_bar>
          <sakai:button_bar_item action="#{ForumTool.processAddGroupsUsersSubmit}" value="Submit" />
          <sakai:button_bar_item action="#{ForumTool.processAddGroupsUsersCancel}" value="Cancel" />
        </sakai:button_bar>
      </h:form>     
    </sakai:view_content>
  </sakai:view_container>
</f:view> 

