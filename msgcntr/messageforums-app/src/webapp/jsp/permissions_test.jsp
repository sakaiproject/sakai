<%@ page import="java.util.*, javax.faces.context.*, javax.faces.application.*,
                 javax.faces.el.*, org.sakaiproject.tool.messageforums.*"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
<%@ taglib uri="http://sakaiproject.org/jsf/messageforums" prefix="mf" %>
                 
<f:loadBundle basename="org.sakaiproject.tool.messageforums.bundle.Messages" var="msgs"/>

<f:view>
   <sakai:view>
     <link href='/sakai-messageforums-tool/css/msgForums.css' rel='stylesheet' type='text/css' /> 
    <%  
    /** initialize user's private message area per request **/
    FacesContext context = FacesContext.getCurrentInstance();
    Application app = context.getApplication();
    ValueBinding binding = app.createValueBinding("#{ForumTool}");
    DiscussionForumTool dft = (DiscussionForumTool) binding.getValue(context);
    out.print(dft.generatePermissionScript());
    %>
    
       <h:form id="msgForum">
  	   <h:selectManyListbox id="list1">
  	     <f:selectItem id="id1" itemValue="id1 test" itemLabel="id1 test"/>
   	     <f:selectItem id="id2" itemValue="id2 test" itemLabel="id2 test"/>
  	     <f:selectItem id="id3" itemValue="id3 test" itemLabel="id3 test"/>
  	   </h:selectManyListbox>  		  
  	   
  	   
  	   <div id="myDiv">
  	   <h:selectBooleanCheckbox id="one" value="1" 
  	                            onclick="findLevelForPermissions(this.parentElement);"/>
  	   <h:selectBooleanCheckbox id="two" value="0"
  	                            onclick="findLevelForPermissions(this.parentElement);"/>
  	   <h:selectBooleanCheckbox id="three" value="1"
  	                            onclick="findLevelForPermissions(this.parentElement);"/>
  	   <h:selectBooleanCheckbox id="four" value="0"
  	                            onclick="findLevelForPermissions(this.parentElement);"/>
  	   </div>
  	   
       <sakai:button_bar>
         <sakai:button_bar_item action="#{ForumTool.processPost}" value="Post" />         
      </sakai:button_bar>
  	     		  
      </h:form>
  </sakai:view>
</f:view> 
