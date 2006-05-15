<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
<%@ taglib uri="http://java.sun.com/upload" prefix="corejsf" %>

<f:loadBundle basename="org.sakaiproject.tool.postem.bundle.Messages" var="msgs"/>

<f:view>
  <sakai:view_container title="#{msgs.title_new}">
    <sakai:view_content>
      <h:form enctype="multipart/form-data">
      
      	<h3><h:outputText value="#{msgs.create_update}"/></h3>
      				
				<h:messages style="color: red;"/>

				<sakai:group_box>
					<h:outputText style="font-weight: bold;" value="#{msgs.feedback_instructions}"/>
					<f:verbatim><br /></f:verbatim>
					<h:outputText value="#{msgs.feedback_first}"/>
					<f:verbatim><br /></f:verbatim>
					<h:outputText value="#{msgs.feedback_second}"/>
					<f:verbatim><br /></f:verbatim>
					<h:outputText value="#{msgs.feedback_third}"/>
				</sakai:group_box>				
				<sakai:group_box>
					<table width="80%" align="left">
            <tr>
              <td align="left" width="30%">
								<h:outputText style="font-weight: bold; font-size: 12px; margin-right: 12px;" value="#{msgs.gradebook_title}"/>
							</td>
							<td>
								<h:inputText value="#{PostemTool.currentGradebook.title}"/>
							</td>
						</tr>
            <tr>
              <td align="left" width="20%">
								<h:outputText style="font-weight: bold; font-size: 12px; margin-right: 12px;" value="#{msgs.gradebook_choosefile}"/>
							</td>
							<td>
								<corejsf:upload value="#{PostemTool.csv}"/>
							</td>
						</tr>
            <tr>
              <td align="left" width="20%">
								<h:outputText style="font-weight: bold; font-size: 12px; margin-right: 12px;" value="#{msgs.gradebook_feedbackavail}"/>
							</td>
							<td>
								<h:selectBooleanCheckbox value="#{PostemTool.currentGradebook.release}"/>
								<h:outputText style="margin-top: 25px;" value="#{msgs.release}"/>
							</td>
						</tr>	
					</table>											
				</sakai:group_box>
				
				
				
				<%-- <sakai:hideDivision title="#{msgs.notification}">
					<sakai:panel_edit>
					<h:outputText style="font-size: 13px; font-weight:bold;" value="#{msgs.description}"/><h:inputTextarea/>
					<h:outputText style="font-size: 13px; font-weight:bold; margin-right: 50px;" value="#{msgs.email_notification}"/><h:inputText/>
					</sakai:panel_edit>
				</sakai:hideDivision> --%>
				
				<%-- <sakai:script contextBase="/sakai-jsf-resource" path="/hideDivision/hideDivision.js"/> --%>
				
				<%--<sakai:hideDivision title="#{msgs.advanced}">
				<sakai:panel_edit>
					<h:outputText value="#{msgs.with_header}"/>
					<h:selectBooleanCheckbox value="#{PostemTool.withHeader}"/>
					<h:outputText value="#{msgs.release_statistics}"/>
					<h:selectBooleanCheckbox value="#{PostemTool.currentGradebook.releaseStats}"/>
				</sakai:panel_edit>
				<sakai:doc_section>
					<sakai:doc_section_title><h:outputText style="font-weight: bold; font-size: 15px;" value="#{msgs.template_file}"/></sakai:doc_section_title>
					<h:outputText value="#{msgs.template_instructions}"/>
				</sakai:doc_section>
				<corejsf:upload value="#{PostemTool.newTemplate}"/>
				</sakai:hideDivision>
				<script type="text/javascript">showHideDiv(<h:outputText value="#{msgs.divid}"/>, '/sakai-jsf-resource');</script>
				--%>
				<br />
				
				<sakai:button_bar>
            	<sakai:button_bar_item
			    	action="#{PostemTool.processCreate}"
					value="#{msgs.bar_create}" 
					rendered="#{PostemTool.editable}"/>
			  	<sakai:button_bar_item
			    	action="#{PostemTool.processCancelNew}"
					value="#{msgs.bar_cancel}" 
					rendered="#{PostemTool.editable}"/>
   	    </sakai:button_bar>
				
    </h:form>
     
    </sakai:view_content>
  </sakai:view_container>
</f:view> 