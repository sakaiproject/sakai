<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>

<%
    response.setContentType("text/html; charset=UTF-8");
    response.addDateHeader("Expires", System.currentTimeMillis() - (1000L * 60L * 60L * 24L * 365L));
    response.addDateHeader("Last-Modified", System.currentTimeMillis());
    response.addHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0, post-check=0, pre-check=0");
    response.addHeader("Pragma", "no-cache");
%>

<jsp:useBean id="msgs" class="org.sakaiproject.util.ResourceLoader" scope="session"> 
<jsp:setProperty name="msgs" property="baseName" value="org.sakaiproject.tool.postem.bundle.Messages"/> 
</jsp:useBean>
<f:view>
	<sakai:view title="#{msgs.title_view}">
		<sakai:view_content>
			<h:form>
				
		  	<sakai:tool_bar_message value="#{msgs.view_student}" />
		  	<sakai:panel_edit>
		  		<h:outputLabel styleClass="shorttext" for="participant"><h:outputText value="#{msgs.choose_username}"/></h:outputLabel>
		  		<h:selectOneMenu value="#{PostemTool.selectedStudent}" id="participant" onchange="submit()">
		  			<f:selectItems value="#{PostemTool.studentMap}"/>
		  		</h:selectOneMenu>
		  	</sakai:panel_edit>
		  	
		  	<br />
		  	
	    	<h:outputText value="#{PostemTool.selectedStudentGrades}" escape="false" rendered="#{PostemTool.editable}"/>
		  	
				<sakai:button_bar>					
					<sakai:button_bar_item
						action="#{PostemTool.processCancelView}"
						value="#{msgs.back}" />
				</sakai:button_bar>		  	

			</h:form>
		</sakai:view_content>
	</sakai:view>
</f:view>