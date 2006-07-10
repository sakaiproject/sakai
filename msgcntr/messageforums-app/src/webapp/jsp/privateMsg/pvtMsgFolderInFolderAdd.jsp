<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
<f:loadBundle basename="org.sakaiproject.tool.messageforums.bundle.Messages" var="msgs"/>
<link href='/sakai-messageforums-tool/css/msgForums.css' rel='stylesheet' type='text/css' />

<f:view>
  <sakai:view title="#{msgs.pvt_msgs_label} #{msgs.pvt_create_folder}">

		<h:form id="pvtMsgFolderAdd">
		
			  <sakai:tool_bar_message value="#{msgs.pvt_msgs_label} #{msgs.pvt_create_folder}" />
			 
				<div class="instruction">
				  <h:outputText value="#{msgs.cdfm_required}"/> <h:outputText value="#{msgs.pvt_star}" styleClass="reqStarInline"/>
				</div>
			   
			  <div class="msgHeadings">
	          <h:outputText value="#{msgs.pvt_folder_title}"/>
	      </div> 
			 
			  <h:outputText value="#{msgs.pvt_star}" styleClass="reqStarInline"/><h:outputLabel for="title" value="#{msgs.pvt_folder_title}"/>
			  <h:inputText id="title" value="#{PrivateMessagesTool.addFolder}" />
			  <br />
			     
				<sakai:button_bar>
			  	  <sakai:button_bar_item action="#{PrivateMessagesTool.processPvtMsgFldInFldCreate}" value="#{msgs.pvt_add}" accesskey="a" />
			    <sakai:button_bar_item action="#{PrivateMessagesTool.processPvtMsgFldAddCancel}" value="#{msgs.pvt_cancel}" accesskey="c" />
			  </sakai:button_bar>   
           
		 </h:form>

	</sakai:view>
</f:view>



