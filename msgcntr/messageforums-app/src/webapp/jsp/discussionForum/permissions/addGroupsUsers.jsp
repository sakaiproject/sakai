<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
<%@ taglib uri="http://sakaiproject.org/jsf/messageforums" prefix="mf" %>
<jsp:useBean id="msgs" class="org.sakaiproject.util.ResourceLoader" scope="session">
   <jsp:setProperty name="msgs" property="baseName" value="org.sakaiproject.api.app.messagecenter.bundle.Messages"/>
</jsp:useBean>
<f:view>

  <sakai:view title="#{msgs.cdfm_container_title}">
         		<script type="text/javascript">includeLatestJQuery("msgcntr");</script>
       		<sakai:script contextBase="/messageforums-tool" path="/js/sak-10625.js"/>
       		<sakai:script contextBase="/messageforums-tool" path="/js/messages.js"/>
    <h:form id="addGroupsUsers"> 
<!--jsp/discussionForum/permissions/addGroupsUsers.jsp-->                
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

