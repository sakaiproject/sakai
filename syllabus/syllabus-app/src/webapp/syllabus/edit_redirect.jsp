<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf2/sakai" prefix="sakai" %>
<% response.setContentType("text/html; charset=UTF-8"); %>
<f:view>
<jsp:useBean id="msgs" class="org.sakaiproject.util.ResourceLoader" scope="session">
   <jsp:setProperty name="msgs" property="baseName" value="org.sakaiproject.tool.syllabus.bundle.Messages"/>
</jsp:useBean>

	<sakai:view_container title="#{msgs.title_edit}">
		<sakai:view_content>
			<script>includeLatestJQuery('edit_redirect.jsp');</script>
			<script>
				$(document).ready( function() {
					const menuLink = $('#syllabusMenuRedirectLink');
					menuLink.addClass('current');
					menuLink.find('a').removeAttr('href');
					// Internationalized title on the button
					document.querySelector('button.clear-input-btn').title = '<h:outputText value="#{msgs.reset}" />';
				});
				function clearRedirectInput() {
					document.getElementById('redirectForm:urlValue').value = '';
					document.querySelector('input[type="submit"].active').focus();
				}
			</script>
			<h:form id="redirectForm">
				<%@ include file="mainMenu.jsp" %>

				<div class="page-header">
					<h1><h:outputText value="#{msgs.redirect_sylla}" /></h1>
				</div>

<div class="container">
  <div class="row">
				<h:messages styleClass="sak-banner-error" rendered="#{!empty facesContext.maximumSeverity}" />
  </div>
  <div class="row">
    <div class="col">
      <p class="alert-info"><h:outputText value="#{msgs.redirect_sylla_delete}" /></p>
      <div class="input-group mb-3">
        <h:outputLabel for="urlValue" styleClass="input-group-text"><h:outputText value="#{msgs.syllabus_url}"/></h:outputLabel>
        <h:inputText id="urlValue" value="#{SyllabusTool.currentRediredUrl}" styleClass="form-control" />
        <button class="btn btn-outline-secondary position-absolute top-0 end-0 clear-input-btn" type="button" onclick="clearRedirectInput()">
          <span class="fa fa-times-circle-o" aria-hidden="true"></span>
        </button>
      </div>
    </div>
  </div>
</div>

				<sakai:button_bar>
					<h:commandButton
						styleClass="active"
						action="#{SyllabusTool.processEditSaveRedirect}"
						value="#{msgs.save}"
						accesskey="s" />
					<h:commandButton
						action="#{SyllabusTool.processEditCancelRedirect}"
						value="#{msgs.cancel}" 
						accesskey="x" />
				</sakai:button_bar>

			</h:form>
		</sakai:view_content>
	</sakai:view_container>
</f:view>
