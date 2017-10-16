<%@ page import="java.util.*, javax.faces.context.*, javax.faces.application.*,
                 javax.faces.el.*, org.sakaiproject.tool.messageforums.*,
                 org.sakaiproject.tool.messageforums.ui.*,javax.servlet.http.HttpUtils,java.util.Enumeration"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
<%@ taglib uri="http://sakaiproject.org/jsf/messageforums" prefix="mf" %>


<jsp:useBean id="msgs" class="org.sakaiproject.util.ResourceLoader" scope="session">
   <jsp:setProperty name="msgs" property="baseName" value="org.sakaiproject.api.app.messagecenter.bundle.Messages"/>
</jsp:useBean>

<f:view>
	<sakai:view title="#{msgs.pvtarea_name}">
		<h:form id="prefs_pvt_form">
<%
    FacesContext context = FacesContext.getCurrentInstance();
	ExternalContext exContext = context.getExternalContext();
	Map paramMap = exContext.getRequestParameterMap();
    Application app = context.getApplication();
    ValueBinding binding = app.createValueBinding("#{PrivateMessagesTool}");
    PrivateMessagesTool pmt = (PrivateMessagesTool) binding.getValue(context);
    request.setAttribute("currentTopic",pmt.getReceivedTopicForMessage((String) paramMap.get("current_msg_detail")));
    request.setAttribute("currentMessage",(String) paramMap.get("current_msg_detail"));
     
    if(pmt.getUserId() != null){
  	//show entire page, otherwise, don't allow anon user to use this tool:
%>

		    <script type="text/javascript" src="/library/webjars/jquery/1.12.4/jquery.min.js?version="></script>
       		<sakai:script contextBase="/messageforums-tool" path="/js/sak-10625.js"/>
			<sakai:script contextBase="/messageforums-tool" path="/js/forum.js"/>
			<script type="text/javascript">
				$(document).ready(function() {
					if ($('ul[class="alertMessage"]').length==0) {
						eval('function c(){' + $('a[title="shortaccess"]').attr('onclick') + '};c();');
					} else {
						$('a[title="shortaccess"]').toggle();
					}
				});
			</script>		
			
 			<h:messages styleClass="alertMessage" id="errorMessages" rendered="#{! empty facesContext.maximumSeverity}"/> 

			<h:commandLink action="#{PrivateMessagesTool.processPvtMsgTopicAndDetail}"
				             immediate="true" title="shortaccess">
				<h:outputText value="#{msgs.loading_direct_access}" />
				<f:param value="#{currentTopic}" name="pvtMsgTopicId" />
				<f:param value="#{currentMessage}" name="current_msg_detail" />
			</h:commandLink>
<%
	}else{
	//user is an anon user, just show a message saying they can't use this tool:
%>
			<h:outputText value="#{msgs.pvt_anon_warning}" styleClass="information"/>
<%
	}
%>

		 </h:form>
	</sakai:view>
</f:view>
