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

<jsp:useBean id="msgs" class="org.sakaiproject.util.ResourceLoader" scope="session">
   <jsp:setProperty name="msgs" property="baseName" value="org.sakaiproject.api.app.messagecenter.bundle.Messages"/>
</jsp:useBean>

<f:view>
  <sakai:view title="#{msgs.cdfm_message_forums}">
<!--jsp/main.jsp-->

       <h:form id="msgForum">

       		<script type="text/javascript" src="/library/js/jquery.js"></script>
       		<sakai:script contextBase="/sakai-messageforums-tool" path="/js/sak-10625.js"/>
				<sakai:script contextBase="/sakai-messageforums-tool" path="/js/forum.js"/>
				<%--//designNote:  if layout="table" is needed,  need rendered attr here so that no empty tables  are put in the response - leaving undefined here so that it reverts to layout="list" --%> 
				<h:messages globalOnly="true" infoClass="success" errorClass="alertMessage" />  		
  
  		  <%-- include hide division here so that pvtArea can be used w/o div in isolated view --%>	
  		  <mf:forumHideDivision title="#{msgs.pvtarea_name}" id="_test_div" 
					rendered="#{PrivateMessagesTool.pvtAreaEnabled || PrivateMessagesTool.instructor}">

<%
  String thisId = request.getParameter("panel");
  if (thisId == null) 
  {
    thisId = "Main" + org.sakaiproject.tool.cover.ToolManager.getCurrentPlacement().getId();
  }
%>
			<script type="text/javascript">
  				setPanelId('<%= org.sakaiproject.util.Web.escapeJavascript(thisId)%>');
			</script> 
		
			<h:inputHidden id="mainOrHp" value="main" />
        
   	    	<%@include file="privateMsg/pvtArea.jsp"%>
        </mf:forumHideDivision>


		<%-- include hide division here so that dfArea can be used w/o div within Forums tool --%>        
		<mf:forumHideDivision title="#{msgs.cdfm_discussion_forums}" id="_test_div" >
	        <%@include file="discussionForum/area/dfArea.jsp"%>
        	<h:inputHidden id="mainOrForumOrTopic" value="main" />

    	</mf:forumHideDivision>
    	    
      </h:form>
  </sakai:view>
</f:view> 
