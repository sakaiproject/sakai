<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
<% response.setContentType("text/html; charset=UTF-8"); %>
<f:view>
<f:loadBundle basename="org.sakaiproject.tool.syllabus.bundle.Messages" var="msgs"/>
	<sakai:view_container title="#{msgs.title_list}">
	<sakai:view_content>
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
	</sakai:view_content>
	</sakai:view_container>
</f:view>
