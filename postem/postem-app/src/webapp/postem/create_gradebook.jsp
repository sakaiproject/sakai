<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
<%@ taglib uri="http://java.sun.com/upload" prefix="corejsf" %>

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
  <sakai:view title="#{msgs.title_new}">
      <h:form enctype="multipart/form-data">
      
      	<h3><h:outputText value="#{msgs.create_update}"/></h3>
      				
				<h:panelGroup styleClass="alertMessage" rendered="#{PostemTool.displayErrors}">
				  <h:messages globalOnly="true" layout="table" />
				</h:panelGroup>

				<sakai:panel_titled>
					<h:outputText style="font-weight: bold;" value="#{msgs.feedback_instructions}"/>
					<f:verbatim><br /></f:verbatim>
					<h:outputText value="#{msgs.feedback_first}"/>
					<f:verbatim><br /></f:verbatim>
					<h:outputText value="#{msgs.feedback_second}"/>
					<f:verbatim><br /></f:verbatim>
					<h:outputText value="#{msgs.feedback_third}"/>
				</sakai:panel_titled>
								
				<sakai:panel_titled>
					<h:panelGrid styleClass="jsfFormTable" width="80%" columns="2">

						<h:outputLabel for="title" styleClass="shorttext" style="font-weight: bold;">
						  <h:outputText value="#{msgs.gradebook_title}"/>
						</h:outputLabel>
						<h:inputText id="title" value="#{PostemTool.currentGradebook.title}"/>
								
						<h:outputText style="font-weight: bold;" value="#{msgs.gradebook_choosefile}"/>
						<corejsf:upload value="#{PostemTool.csv}" />

						<%--<h:outputText value="#{msgs.delimiter}" style="font-weight: bold;" />
					  <h:selectOneRadio styleClass="checkbox" value="#{PostemTool.delimiter}" layout="pageDirection">
				      <f:selectItem itemValue="comma" itemLabel="#{msgs.comma_delim}"/>
  			      <f:selectItem itemValue="tab" itemLabel="#{msgs.tab_delim}"/>
			      </h:selectOneRadio>--%>

						<h:outputText value="#{msgs.gradebook_feedbackavail}" style="font-weight: bold;"/>
						<h:panelGroup styleClass="checkbox">
						  <h:selectBooleanCheckbox id="release" value="#{PostemTool.currentGradebook.release}" />
						  <h:outputLabel for="release"><h:outputText value="#{msgs.release}"/></h:outputLabel>
						</h:panelGroup>

					</h:panelGrid>											
				</sakai:panel_titled>
	
								
				<%-- <sakai:hideDivision title="#{msgs.notification}">
					<sakai:panel_edit>
					<h:outputText style="font-size: 13px; font-weight:bold;" value="#{msgs.description}"/><h:inputTextarea/>
					<h:outputText style="font-size: 13px; font-weight:bold; margin-right: 50px;" value="#{msgs.email_notification}"/><h:inputText/>
					</sakai:panel_edit>
				</sakai:hideDivision> --%>
				
				<%-- <sakai:script contextBase="/sakai-jsf-resource" path="/hideDivision/hideDivision.js"/> --%>
				
				<%--<sakai:script contextBase="/sakai-jsf-resource" path="/hideDivision/hideDivision.js"/>
				
				<sakai:hideDivision title="#{msgs.advanced}">--%>
				<%--<sakai:panel_edit>
					<h:outputText value="#{msgs.with_header}"/>
					<h:selectBooleanCheckbox value="#{PostemTool.withHeader}"/>
					<h:outputText value="#{msgs.release_statistics}"/>
					<h:selectBooleanCheckbox value="#{PostemTool.currentGradebook.releaseStats}"/>
				</sakai:panel_edit>--%>
				
					<%--<sakai:panel_titled>
					  <h4><h:outputText value="#{msgs.template_file}" /></h4>
						<h:outputText value="#{msgs.feedback_instructions}" style="font-weight: bold;" />
						<f:verbatim><br /></f:verbatim>
						<h:outputText value="#{msgs.template_instructions}"/>
						<f:verbatim><br /></f:verbatim>
					  <h:panelGrid styleClass="jsfFormTable" columns="2" width="80%">
					    <h:outputText style="font-weight: bold;" value="#{msgs.choose_template}" />
					    <corejsf:upload value="#{PostemTool.newTemplate}"/>
				   	</h:panelGrid>
					</sakai:panel_titled>
				
				</sakai:hideDivision>
				<script type="text/javascript">showHideDiv(<h:outputText value="#{msgs.divid}"/>, '/sakai-jsf-resource');</script>--%>
				
				<br />
				
				<sakai:button_bar>
          <sakai:button_bar_item
			    	action="#{PostemTool.processCreate}"
					value="#{msgs.bar_post}" 
					rendered="#{PostemTool.editable}"/>
			  	<sakai:button_bar_item
			    	action="#{PostemTool.processCancelNew}"
					value="#{msgs.cancel}" 
					rendered="#{PostemTool.editable}"/>
   	    </sakai:button_bar>
				
    </h:form>

  </sakai:view>
</f:view> 
