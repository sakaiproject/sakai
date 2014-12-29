<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
<% response.setContentType("text/html; charset=UTF-8"); %>
<f:view>

<jsp:useBean id="msgs" class="org.sakaiproject.util.ResourceLoader" scope="session">
   <jsp:setProperty name="msgs" property="baseName" value="org.sakaiproject.tool.syllabus.bundle.Messages"/>
</jsp:useBean>

	<sakai:view_container title="#{msgs.title_list}">
		<sakai:view_content>
			<h:form>
<h:outputText value="multi_delete_confirm.jsp" />
				<table width="100%" align="center">
					<tr>
					  <td width="100%" align="center">
						  <table width="100%" align="center">
							  <tr>
								  <td align="center" style="font-size: 12pt; color: #8B0000" width="100%">
								    <h:outputText value="#{msgs.delConfAlert}" />
								  </td>
								  <td/>
							  </tr>
						  </table>
						</td>
					</tr>
					<tr>
					  <td width="100%">
						  <table width="100%" align="center">
							  <tr>
							    <td width="50%" align="right">
		 							  <sakai:tool_bar_item
										  action="#{SyllabusTool.processConfirmMuliDelete}"
										  value="#{msgs.bar_ok}" />
								  </td>
								  <td width="50%" align="left">
									  <sakai:tool_bar_item
										  action="#{SyllabusTool.processCancelMultiDelete}"
										  value="#{msgs.bar_cancel}" />
								  </td>
							  </tr>
						  </table>
						</td>
					</tr>
				</table>

			</h:form>
		</sakai:view_content>
	</sakai:view_container>
</f:view>
