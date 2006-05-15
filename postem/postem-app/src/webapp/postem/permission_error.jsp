<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>

<f:loadBundle basename="org.sakaiproject.tool.postem.bundle.Messages" var="msgs"/>

<f:view>
	<sakai:view title="#{msgs.title_error}">
		<h:form>

   	  <table width="100%">
  			<tr>
		  	  <td width="0%" />
  	  	  <td width="100%" style="color: red">
						You have no permission for this action!
					</td>
				</tr>
			</table>

		</h:form>
	</sakai:view>
</f:view>