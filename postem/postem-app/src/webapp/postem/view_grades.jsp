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
				
		  	<sakai:tool_bar_message value="#{msgs.grade_view}" />
		  	
		  	<%-- <sakai:panel_edit>
		  		<h:outputText value="#{msgs.choose_stats_column}" rendered="#{PostemTool.currentGradebook.releaseStats}"/>
		  		<h:selectOneMenu value="#{PostemTool.column}" onchange="submit()" rendered="#{PostemTool.currentGradebook.releaseStats}">
		  			<f:selectItems value="#{PostemTool.statsColumns}"/>
		  		</h:selectOneMenu>
		  	</sakai:panel_edit> --%>
		  	
            <h:outputText value="#{PostemTool.currentStudentGrades}" escape="false" rendered="#{!PostemTool.editable}"/>
		  	<h:outputText value="#{PostemTool.firstStudentGrades}" escape="false" rendered="#{PostemTool.editable}"/>
		  	<br />
		  	<%-- <h:dataTable value="#{PostemTool.currentColumn.summary}" var="stat"
		  		rendered="#{PostemTool.currentGradebook.releaseStats}">
		  		<h:column>
		  			<f:facet name="header">
		  				<h:outputText value="#{msgs.stats}"/>
		  			</f:facet>
		  			<h:outputText value="#{stat.first}"/>
		  		</h:column>
		  		<h:column>
		  			<f:facet name="header">
		  				<h:outputText value="#{PostemTool.currentColumn.name}" rendered="#{PostemTool.currentColumn.hasName}"/>
		  			</f:facet>
		  			<h:outputText value="#{stat.second}"/>
		  		</h:column>
		  	</h:dataTable> --%>
				<sakai:button_bar>					
					<sakai:button_bar_item
						action="#{PostemTool.processCancelView}"
						value="#{msgs.back}" />
				</sakai:button_bar>		  	

			</h:form>
		</sakai:view_content>
	</sakai:view>
</f:view>