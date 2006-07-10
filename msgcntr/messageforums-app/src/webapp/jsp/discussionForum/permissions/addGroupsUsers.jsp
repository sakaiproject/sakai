<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
<%@ taglib uri="http://sakaiproject.org/jsf/messageforums" prefix="mf" %>
<f:loadBundle basename="org.sakaiproject.tool.messageforums.bundle.Messages" var="msgs"/>
<link href='/sakai-messageforums-tool/css/msgForums.css' rel='stylesheet' type='text/css' />

<f:view>

  <sakai:view title="#{msgs.cdfm_container_title}">
    <h:form id="addGroupsUsers"> 
                
      <sakai:tool_bar_message value="#{msgs.cdfm_button_bar_add_groups_users}" />
      <sakai:instruction_message value="#{msgs.cdfm_add_users}" />
      
      <h:panelGrid summary="" columns="3">
        <h:panelGroup>
          <h:selectManyListbox id="list1" size="10" styleClass="selectGroupsUsers">
            <f:selectItems value="#{ForumTool.totalGroupsUsersList}"/>
          </h:selectManyListbox>
        </h:panelGroup>
        <h:panelGroup>    
          <h:commandButton value="     #{msgs.cdfm_add_button}    " accesskey="a" />
          <f:verbatim><br /></f:verbatim>
          <h:commandButton value="#{msgs.cdfm_remove_button}" accesskey="r" />
        </h:panelGroup>
        <h:panelGroup>   
           <h:selectManyListbox id="list2" size="10" styleClass="selectGroupsUsers" />        
        </h:panelGroup>
      </h:panelGrid>
     
  			        	    		        
      <sakai:button_bar>
        <sakai:button_bar_item action="#{ForumTool.processAddGroupsUsersSubmit}" accesskey="s" value="Submit" />
        <sakai:button_bar_item action="#{ForumTool.processAddGroupsUsersCancel}" accesskey="c" value="Cancel" />
      </sakai:button_bar>
    </h:form>     
  </sakai:view>
</f:view> 

