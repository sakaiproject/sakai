<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf2/sakai" prefix="sakai" %>
<jsp:useBean id="msgs" class="org.sakaiproject.util.ResourceLoader" scope="session">
   <jsp:setProperty name="msgs" property="baseName" value="org.sakaiproject.api.app.messageforums.bundle.Messages"/>
</jsp:useBean>


<f:view>
	<sakai:view title="#{msgs.pvt_msgs_label} #{msgs.pvt_foldersettings}">
<!--jsp/privateMsg/pvtMsgFolderRevise.jsp-->
	  <h:form id="pvtMsgFolderRevise">
			<script type="text/javascript">includeLatestJQuery("msgcntr");</script>
			<script type="text/javascript" src="/messageforums-tool/js/sak-10625.js"></script>
			<script type="text/javascript" src="/messageforums-tool/js/messages.js"></script>
    <sakai:tool_bar_message value="#{msgs.pvt_rename_folder_label}" />
    <h:messages styleClass="alertMessage" id="errorMessages" rendered="#{! empty facesContext.maximumSeverity}" /> 
 
    <div class="instruction">
	    <h:outputText value="#{msgs.cdfm_required}"/> <h:outputText value="#{msgs.pvt_star}" styleClass="reqStarInline"/>
	</div>
	
		  <h:panelGrid styleClass="jsfFormTable" columns="2">
			  <h:panelGroup styleClass="shorttext required">
				  <h:outputLabel for="revised_title">
				  <h:outputText value="#{msgs.pvt_star}" styleClass="reqStar"/>
				  <h:outputText value="#{msgs.pvt_folder_title} #{msgs.pvt_colon}"/>
				  </h:outputLabel>
			  </h:panelGroup>
			  <h:panelGroup styleClass="shorttext">	
			  <h:inputText value="#{PrivateMessagesTool.selectedNewTopicTitle}" id="revised_title" />
			 </h:panelGroup>
			 </h:panelGrid>
	
		<sakai:button_bar>
	  	  <h:commandButton action="#{PrivateMessagesTool.processPvtMsgFldRevise}" value="#{msgs.pvt_saveset}" accesskey="s"  styleClass="active" />
	  	  <h:commandButton action="#{PrivateMessagesTool.processPvtMsgFolderSettingDelete}" value="#{msgs.pvt_delete}" />
	    <h:commandButton action="#{PrivateMessagesTool.processPvtMsgReturnToFolderView}" value="#{msgs.pvt_cancel}" accesskey="x" />
	  </sakai:button_bar>   
           
	</h:form>

	</sakai:view>
</f:view>

