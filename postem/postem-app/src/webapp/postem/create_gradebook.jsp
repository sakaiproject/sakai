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
      <script src="/library/js/spinner.js" type="text/javascript"></script>
      <h:form enctype="multipart/form-data">
      
      	<div class="page-header">
          <h1><h:outputText value="#{msgs.create_update}"/></h1>
        </div>
      				
				<h:panelGroup styleClass="alertMessage" rendered="#{PostemTool.displayErrors}">
				  <h:messages globalOnly="true" layout="table" />
				</h:panelGroup>

				<div class="instruction">
					<strong><h:outputText value="#{msgs.feedback_instructions}"/></strong>
					<f:verbatim><br /></f:verbatim>
					<h:outputText value="#{msgs.feedback_first}"/>
					<f:verbatim><br /></f:verbatim>
					<h:outputText value="#{msgs.feedback_second}"/>
					<f:verbatim><br /></f:verbatim>
					<h:outputText value="#{msgs.feedback_third}"/>
				</div>
								
				<sakai:panel_titled>
					<div class="form-group row">
						<h:outputLabel for="title" value="#{msgs.gradebook_title}" styleClass="form-group-label col-xs-2" />
						<div class="col-xs-4">
						  <h:inputText id="title" value="#{PostemTool.currentGradebook.title}"/>
						</div>
					</div>											

          <div class="form-group row">
            <h:outputLabel for="choosefile" value="#{msgs.feedback_title}" styleClass="form-group-label col-xs-2" />
            <div class="col-xs-4">
                    <sakai:button_bar>
                    <%-- (gsilver) cannot pass a needed title atribute to these next items --%>
                        <sakai:button_bar_item
                            id="choosefile"
                            action="#{PostemTool.processAddAttachRedirect}" 
                            value="#{msgs.gradebook_choosefile}"/>
                        <h:outputText value="#{PostemTool.attachmentTitle}"/>
                    </sakai:button_bar>
            </div>
          </div>

					<div class="form-group row">
						<h:outputLabel value="#{msgs.gradebook_feedbackavail}" styleClass="form-group-label col-xs-2" />
						<div class="col-xs-4">
						  <h:selectBooleanCheckbox id="release" value="#{PostemTool.currentGradebook.release}" />
						  <h:outputLabel for="release"><h:outputText value="#{msgs.release}"/></h:outputLabel>
						</div>

					</div>											
				</sakai:panel_titled>

				<br />

				<sakai:button_bar>
          <sakai:button_bar_item
			    	action="#{PostemTool.processCreate}"
					value="#{msgs.bar_post}"
					onclick="SPNR.disableControlsAndSpin(this, null);"
					rendered="#{PostemTool.editable}"/>
			  	<sakai:button_bar_item
			    	action="#{PostemTool.processCancelNew}"
					value="#{msgs.cancel}"
					onclick="SPNR.disableControlsAndSpin(this, null);"
					rendered="#{PostemTool.editable}"/>
   	    </sakai:button_bar>
				
    </h:form>

  </sakai:view>
</f:view> 
