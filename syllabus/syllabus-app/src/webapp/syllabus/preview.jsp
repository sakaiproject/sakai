<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
<%@ taglib uri="http://sakaiproject.org/jsf/syllabus" prefix="syllabus" %>
<% response.setContentType("text/html; charset=UTF-8"); %>
<f:view>

<jsp:useBean id="msgs" class="org.sakaiproject.util.ResourceLoader" scope="session">
   <jsp:setProperty name="msgs" property="baseName" value="org.sakaiproject.tool.syllabus.bundle.Messages"/>
</jsp:useBean>

	<sakai:view_container>
		<sakai:view_content>
			<h:outputText value="#{SyllabusTool.alertMessage}" styleClass="alertMessage" rendered="#{SyllabusTool.alertMessage != null}" />
			<h:form>
		  	<sakai:tool_bar_message value="#{msgs.previewNotice}" /> 
				<h4><h:outputText value="#{SyllabusTool.syllabusDataTitle}"/></h4>
				<div class="indnt1">
					<syllabus:syllabus_htmlShowArea value="#{SyllabusTool.syllabusDataAsset}" />
				</div>	
					<h:dataTable value="#{SyllabusTool.attachments}" var="eachAttach">
					  <h:column>
							<f:facet name="header">
								<h:outputText value="" />
							</f:facet>
							<sakai:contentTypeMap fileType="#{eachAttach.type}" mapType="image" var="imagePath" pathPrefix="/library/image/"/>									
							<h:graphicImage id="exampleFileIcon" value="#{imagePath}" />	
							<h:outputLink value="#{eachAttach.url}" target="_new_window">
								<h:outputText value="#{eachAttach.name}" />
							</h:outputLink>
						</h:column>
					</h:dataTable>
				<sakai:button_bar>
					<h:commandButton
						action="#{SyllabusTool.processEditPreviewBack}"
						value="#{msgs.revise}" 
						accesskey="x" />
				</sakai:button_bar>

			</h:form>
		</sakai:view_content>
	</sakai:view_container>
</f:view>
