<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>

<f:loadBundle basename="org.sakaiproject.tool.postem.bundle.Messages" var="msgs"/>

<f:view>
	<sakai:view title="#{msgs.title_list}">
			<h:form>
			  <h:outputText styleClass="alertMessage" value="#{msgs.delete_confirm}" />
			  
			  <br />
			  
			  <table styleClass="itemSummary">
			    <tr>
			      <th scope="row"><h:outputText value="#{msgs.title_label}" /></th>
			      <td><h:outputText value="#{PostemTool.currentGradebook.title}"/></td>
			    </tr>
			  </table>

				<sakai:button_bar>
          <sakai:button_bar_item action="#{PostemTool.processDelete}"
					                       value="#{msgs.bar_delete}" />
					<sakai:button_bar_item action="#{PostemTool.processCancelView}"
										             value="#{msgs.cancel}" />
         </sakai:button_bar>

			</h:form>
	</sakai:view>
</f:view>
				