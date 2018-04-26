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
	<sakai:view title="#{msgs.title_list}">
		<h:form>
            <sakai:tool_bar rendered="#{PostemTool.editable}">
			  	<sakai:tool_bar_item
			    	action="#{PostemTool.processCreateNew}"
					value="#{msgs.bar_new}" 
					rendered="#{PostemTool.editable}"/>
   	        </sakai:tool_bar>
			<sakai:view_content>
	
				<sakai:flat_list value="#{PostemTool.gradebooks}" var="gradebook" binding="#{PostemTool.gradebookTable}" styleClass="table table-bordered table-striped">
					<h:column>
						<f:facet name="header">
							<h:commandLink action="#{PostemTool.toggleTitleSort}" title="#{msgs.sort_title}">
						   	<h:outputText value="#{msgs.gradebook_titles}" />
								<h:graphicImage value="postem/images/sortascending.gif" rendered="#{PostemTool.titleSort && PostemTool.ascending}" alt="#{msgs.sort_title_asc}"/>
								<h:graphicImage value="postem/images/sortdescending.gif" rendered="#{PostemTool.titleSort && !PostemTool.ascending}" alt="#{msgs.sort_title_desc}"/>
							</h:commandLink>
						</f:facet>
						<h:outputText value="#{gradebook.title}" />
					</h:column>
					<h:column rendered="#{PostemTool.editable}">
						<f:facet name="header">
						  <h:commandLink action="#{PostemTool.toggleCreatorSort}" title="#{msgs.sort_creator}">
						   	<h:outputText value="#{msgs.gradebook_creators}" />
								<h:graphicImage value="postem/images/sortascending.gif"  rendered="#{PostemTool.creatorSort && PostemTool.ascending}" alt="#{msgs.sort_creator_asc}"/>
								<h:graphicImage value="postem/images/sortdescending.gif"  rendered="#{PostemTool.creatorSort && !PostemTool.ascending}" alt="#{msgs.sort_creator_desc}"/>
							</h:commandLink>
						</f:facet>
						<h:outputText value="#{gradebook.creatorEid}"/>
					</h:column>
					<h:column rendered="#{PostemTool.editable}">
						<f:facet name="header">
							<h:commandLink action="#{PostemTool.toggleModBySort}" title="#{msgs.sort_mod_by}">
						   	<h:outputText value="#{msgs.gradebook_lastmodifiedby}" />
								<h:graphicImage value="postem/images/sortascending.gif"  rendered="#{PostemTool.modBySort && PostemTool.ascending}" alt="#{msgs.sort_mod_by_asc}"/>
								<h:graphicImage value="postem/images/sortdescending.gif"  rendered="#{PostemTool.modBySort && !PostemTool.ascending}" alt="#{msgs.sort_mod_by_desc}"/>
							</h:commandLink>
						</f:facet>
						<h:outputText value="#{gradebook.lastUpdaterEid}"/>
					</h:column>
					<h:column>
						<f:facet name="header">
							<h:commandLink action="#{PostemTool.toggleModDateSort}" title="#{msgs.sort_mod_date}">
						   	<h:outputText value="#{msgs.gradebook_lastmodified}" />
								<h:graphicImage value="postem/images/sortascending.gif"  rendered="#{PostemTool.modDateSort && PostemTool.ascending}" alt="#{msgs.sort_mod_date_asc}"/>
								<h:graphicImage value="postem/images/sortdescending.gif"  rendered="#{PostemTool.modDateSort && !PostemTool.ascending}" alt="#{msgs.sort_mod_date_desc}"/>
							</h:commandLink>
						</f:facet>
						<h:outputText value="#{gradebook.updatedDateTime}"/>
					</h:column>
					<h:column rendered="#{PostemTool.editable}">
						<f:facet name="header">
							<h:commandLink action="#{PostemTool.toggleReleasedSort}" title="#{msgs.sort_released}">
						   	<h:outputText value="#{msgs.released}" />
								<h:graphicImage value="postem/images/sortascending.gif"  rendered="#{PostemTool.releasedSort && PostemTool.ascending}" alt="#{msgs.sort_released_asc}"/>
								<h:graphicImage value="postem/images/sortdescending.gif"  rendered="#{PostemTool.releasedSort && !PostemTool.ascending}" alt="#{msgs.sort_released_desc}"/>
							</h:commandLink>
						</f:facet>
						<h:outputText rendered="#{gradebook.release}" value="#{msgs.yes}"/>
						<h:outputText rendered="#{!gradebook.release}" value="#{msgs.no}"/>
					</h:column>
					<h:column>
						<h:commandLink action="#{PostemTool.processGradebookView}" rendered="#{!PostemTool.editable}">
							<h:outputText value="#{msgs.gradebook_view}" />
						</h:commandLink>
						<h:commandLink action="#{PostemTool.processInstructorView}" rendered="#{PostemTool.editable}">
							<h:outputText value="#{msgs.gradebook_view}" />
						</h:commandLink>
					</h:column>
					<h:column rendered="#{PostemTool.editable}">
						<h:commandLink action="#{PostemTool.processGradebookView}">
							<h:outputText value="#{msgs.gradebook_preview}" />
						</h:commandLink>
					</h:column>
					<h:column rendered="#{PostemTool.editable}">
						<h:commandLink action="#{PostemTool.processGradebookUpdate}">
							<h:outputText value="#{msgs.gradebook_update}" />
						</h:commandLink>
					</h:column>
					<h:column rendered="#{PostemTool.editable}">
						<h:commandLink action="#{PostemTool.processGradebookDelete}">
							<h:outputText value="#{msgs.gradebook_delete}" />
						</h:commandLink>
					</h:column>
					<h:column rendered="#{PostemTool.editable}">
						<h:commandLink action="#{PostemTool.processCsvDownload}" >
							<h:outputText value="#{msgs.csv_download}"/>
						</h:commandLink>
					</h:column>
					<h:column rendered="#{PostemTool.editable && gradebook.template != null}">
						<h:commandLink action="#{PostemTool.processTemplateDownload}">
							<h:outputText value="#{msgs.template}"/>
						</h:commandLink>
					</h:column>
				</sakai:flat_list>
				
				  <h:outputText styleClass="instruction" value="#{msgs.no_gradebooks}" rendered="#{!PostemTool.gradebooksExist}" />				
			</sakai:view_content>

		</h:form>
	</sakai:view>
</f:view>
