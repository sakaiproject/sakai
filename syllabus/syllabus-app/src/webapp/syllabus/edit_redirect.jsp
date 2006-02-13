<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
<% response.setContentType("text/html; charset=UTF-8"); %>
<f:view>
<f:loadBundle basename="org.sakaiproject.tool.syllabus.bundle.Messages" var="msgs"/>
	<sakai:view_container title="#{msgs.title_edit}">
		<sakai:view_content>
			<h:form>
				<h3>
					<h:outputText value="#{msgs.redirect_sylla}" />
				</h3>
				<p class="shortText">
					<span class="reqStar">*</span>
		<h:outputLabel for="urlValue"><h:outputText value="#{msgs.syllabus_url}"/></h:outputLabel>
<%--					<label for=""><h:outputText value="#{msgs.syllabus_url}"/></label> --%>
					<h:inputText id="urlValue" value="#{SyllabusTool.syllabusItem.redirectURL}" size="65" />
				</p>
				<sakai:button_bar>
					<sakai:button_bar_item
						action="#{SyllabusTool.processEditSaveRedirect}"
						value="#{msgs.save}" />
					<sakai:button_bar_item
						action="#{SyllabusTool.processEditCancelRedirect}"
						value="#{msgs.cancel}" />
				</sakai:button_bar>

			</h:form>
		</sakai:view_content>
	</sakai:view_container>
</f:view>
