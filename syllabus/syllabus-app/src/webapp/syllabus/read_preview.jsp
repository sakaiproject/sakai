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
		<script>includeLatestJQuery('read_preview.jsp');</script>
		<script>
			jQuery(document).ready(function() {
				var menuLink = $('#syllabusMenuBulkEditLink');
				menuLink.addClass('current');
				menuLink.find('a').removeAttr('href');
			});
		 </script>
			<h:form>
			<%@ include file="mainMenu.jsp" %>
			<h:outputText value="#{SyllabusTool.alertMessage}" styleClass="sak-banner-error" rendered="#{SyllabusTool.alertMessage != null}" />
		  	<sakai:tool_bar_message value="#{msgs.previewNotice}" />
			<h4>
				<h:outputText value="#{SyllabusTool.syllabusDataTitle}" />
			</h4>
			<div class="indnt1">
				<syllabus:syllabus_htmlShowArea value="#{SyllabusTool.syllabusDataAsset}" />
			</div>	

			<h:dataTable value="#{SyllabusTool.allAttachments}" var="eachAttach" styleClass="indnt1" summary="">
			 	<h:column>
					<f:facet name="header">
						<h:outputText value="" />
						</f:facet>
					<sakai:contentTypeMap fileType="#{eachAttach.type}" mapType="image" var="imagePath" pathPrefix="/library/image/"/>									
					<h:graphicImage id="exampleFileIcon" value="#{imagePath}" />
					<h:outputLink value="#{eachAttach.url}" target="_blank" title="#{msgs.openLinkNewWindow}">
						<h:outputText value=" " /><h:outputText value="#{eachAttach.name}" />
					</h:outputLink>
				</h:column>
			</h:dataTable>
				
				<sakai:button_bar>
				<%-- (gsilver) cannot pass a needed title atribute to this next item --%>
					<h:commandButton
						action="#{SyllabusTool.processReadPreviewBack}"
						value="#{msgs.revise}" 
						accesskey="x" />
				</sakai:button_bar>

			</h:form>
		</sakai:view_content>
	</sakai:view_container>
</f:view>
