<%@ page import="java.util.*, javax.faces.context.*, javax.faces.application.*,
                 javax.faces.el.*, org.sakaiproject.tool.messageforums.*"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
<%@ taglib uri="http://sakaiproject.org/jsf/messageforums" prefix="mf" %>
                 
<%  
  /** initialize user's private message area per request **/
  FacesContext context = FacesContext.getCurrentInstance();
  Application app = context.getApplication();
  ValueBinding binding = app.createValueBinding("#{PrivateMessagesTool}");
  PrivateMessagesTool pmt = (PrivateMessagesTool) binding.getValue(context);
  pmt.initializePrivateMessageArea();
%>

<f:loadBundle basename="org.sakaiproject.tool.messageforums.bundle.Messages" var="msgs"/>

<f:view>
<link href='/sakai-messageforums-tool/css/msgForums.css' rel='stylesheet' type='text/css' />
  <sakai:view>
       <h:form id="msgForum">
				<sakai:script contextBase="/sakai-messageforums-tool" path="/js/forum.js"/>
  			<h:messages styleClass="alertMessage" id="errorMessages" layout="table" />  		
  		  
  		  <%-- include hide division here so that pvtArea can be used w/o div in isolated view --%>
  		  <mf:forumHideDivision title="#{msgs.pvtarea_name}" id="_test_div" 
                        rendered="#{PrivateMessagesTool.pvtAreaEnabled || PrivateMessagesTool.instructor}">
<%
  String thisId = request.getParameter("panel");
  if (thisId == null) 
  {
    thisId = "Main" + org.sakaiproject.api.kernel.tool.cover.ToolManager.getCurrentPlacement().getId();
  }

%>
<script language="javascript">
  setPanelId('<%= org.sakaiproject.util.web.Web.escapeJavascript(thisId)%>');
</script> 
          <%@include file="privateMsg/pvtArea.jsp"%>
        </mf:forumHideDivision>
        <%@include file="discussionForum/area/dfArea.jsp"%>
      </h:form>
  </sakai:view>
</f:view> 
