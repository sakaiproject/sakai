<%@ page import="java.util.*, javax.faces.context.*, javax.faces.application.*,
                 javax.faces.el.*, org.sakaiproject.user.tool.*"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="t"%>
<%-- Core JSTL tag library --%>
<%@ taglib uri = "http://java.sun.com/jsp/jstl/core" prefix = "c" %>
<% response.setContentType("text/html; charset=UTF-8"); %>

<f:view> 
	<sakai:view_container title="#{msgs.privacy_title}">
    <sakai:stylesheet path="/css/prefs.css"/>
	<sakai:view_content>
    <h:form>
      <!--SAK-18566 -->
<h:outputText value="#{Portal.latestJQuery}" escape="false"/>
		<script type="text/javascript" src="/sakai-user-tool-prefs/js/prefs.js">// </script>
		<script type="text/javascript">
			$(document).ready(function(){
				setupPrefsGen();
			})
	 </script>   


	<!-- *********** Tool rendering top of page if on MyWorkspace home page *********** --> 
   	<h:panelGroup rendered="#{privacyBean.myWorkspace}" >
		<%-- Set current value for template --%> 
		<c:set var="cTemplate" value = "privacy" scope = "session" />
		<%@ include file="toolbar.jspf"%>

		<!--SAK-18566 -->
		<t:div rendered="#{privacyBean.updateMessage}">
			<jsp:include page="prefUpdatedMsg.jsp"/>
		</t:div>
		<h:panelGroup rendered="#{privacyBean.myWorkspace}" >
			<h3>
				<h:outputText value="#{msgs.privacy_title}" />
			</h3>
		</h:panelGroup>
	 	<%--  Message if Show All or Hide All has been clicked --%>
	 	<f:verbatim><div></f:verbatim>
	 	<h:outputText value="#{privacyBean.changeAllMsg}" styleClass="success" rendered="#{privacyBean.allChanged}" />
		<f:verbatim></div></f:verbatim>
			  
	<f:verbatim><br /><h4></f:verbatim>
	<h:outputText value="#{msgs.privacy_default}" />
	<f:verbatim></h4><br /></f:verbatim>
	<h:selectOneRadio value="#{privacyBean.defaultPrivacyStatus}" layout="pageDirection">
		<f:selectItem itemLabel="#{msgs.privacy_item_visible}" itemValue="#{privacyBean.visibleValue}" />
		<f:selectItem itemLabel="#{msgs.privacy_item_hidden}" itemValue="#{privacyBean.hiddenValue}" />
	</h:selectOneRadio>
	<f:verbatim><br /></f:verbatim>

        <h:outputText value="#{msgs.privacy_no_choices}" rendered="#{privacyBean.sitesEmpty}" />
        <f:verbatim><h4></f:verbatim>
		<h:outputText value="#{msgs.privacy_specific_site}" />
		<f:verbatim></h4></f:verbatim>
		<f:verbatim><br /></f:verbatim>
	 	<h:outputText value="#{msgs.privacy_choose}" rendered="#{not privacyBean.sitesEmpty}" />

   		<h:selectOneMenu value="#{privacyBean.selectedSite}" immediate="true" onchange="this.form.submit( );"
      									valueChangeListener="#{privacyBean.processSiteSelected}" 
                                        id="siteSelect" rendered="#{not privacyBean.sitesEmpty}">
        	<f:selectItems value="#{privacyBean.sites}" />
		</h:selectOneMenu>
	  
	  	<h:outputText value="#{msgs.privacy_site_not_selected}" styleClass="alertMessage" rendered="#{privacyBean.noSiteProcessErr}" />
	</h:panelGroup>

	<%-- *********** common Tool rendering *********** --%>
	<h:panelGroup rendered="#{!privacyBean.myWorkspace || privacyBean.siteSelected}" >
      <f:verbatim><div class="instruction"></f:verbatim>
      <h:outputText value="#{msgs.privacy_stmt1}" />
 
	  <f:verbatim><strong></f:verbatim>
	  <h:outputText value="#{privacyBean.displayStatus}" />
	  <f:verbatim></strong></f:verbatim>
 	        
 	  <h:outputText value="#{msgs.privacy_stmt_show} " rendered="#{privacyBean.show}" />
 	  <h:outputText value="#{msgs.privacy_stmt_hide} " rendered="#{! privacyBean.show}" />    
	  <%--
 	  <h:outputText value="#{msgs.privacy_change_directions}" />
 	  <h:outputText value=" #{privacyBean.checkboxText} " />
 	  <h:outputText value="#{msgs.privacy_change_directions2}" />
 	  --%>
      <f:verbatim></div></f:verbatim>
     
     
      <f:verbatim><p></f:verbatim>
      <h:outputText value="#{msgs.privacy_choice_heading}" />
      <f:verbatim></p></f:verbatim>
      <h:selectOneRadio value="#{privacyBean.privacyStatus}" layout="pageDirection">
      	<f:selectItem itemLabel="#{msgs.privacy_choice_hidden}" itemValue="#{privacyBean.hiddenValue}" />
      	<f:selectItem itemLabel="#{msgs.privacy_choice_visible}" itemValue="#{privacyBean.visibleValue}" />
      	<f:selectItem itemLabel="#{msgs.privacy_choice_later}" itemValue="" />
      </h:selectOneRadio>
     
      <h:commandButton action="#{privacyBean.processUpdate}" value="#{msgs.privacy_update}"
      	   title="#{msgs.privacy_update_title}" styleClass="active" rendered="#{!privacyBean.myWorkspace}" 
      	   onclick="parent.privacy_hide_popup();" />
      	   
      <f:verbatim><p></f:verbatim>
      <h:outputText value="#{msgs.privacy_change_later_instructions}" escape="false" styleClass="instruction" />
	  <f:verbatim></p></f:verbatim>
     
     	<%--
  	  <f:verbatim> <table> <tr> <td class="shorttext"> </f:verbatim>
  	  
	  <h:selectBooleanCheckbox value="#{privacyBean.changeStatus}" id="statusChange" />
      <h:outputText value="#{privacyBean.checkboxText}" />

	  <f:verbatim> </td> </f:verbatim>
 	  
 	  <h:panelGroup rendered="#{! privacyBean.myWorkspace}" >
 	  	<f:verbatim>  <td>&nbsp;&nbsp;&nbsp;</td>  <td>  </f:verbatim>
 	  	
	 	<sakai:button_bar_item action="#{privacyBean.processUpdate}" value="#{msgs.privacy_update}"
        	accesskey="u" title="#{msgs.privacy_update_title}" styleClass="active" rendered="#{! privacyBean.myWorkspace}" />
        
        <f:verbatim> </td> </f:verbatim>
      </h:panelGroup>

      <f:verbatim></tr></table></f:verbatim>
	--%>
	  <h:outputText escape="false" rendered="#{privacyBean.displayPopup}"
	  	value="<script type=\"text/javascript\">parent.privacy_show_popup();</script>" />

  	</h:panelGroup>
   		<h:panelGroup rendered="#{privacyBean.myWorkspace && not privacyBean.sitesEmpty}" >
 	  	  <sakai:button_bar>
        	<sakai:button_bar_item action="#{privacyBean.processUpdate}" value="#{msgs.privacy_update}"
                accesskey="u" title="#{msgs.privacy_update_title}" styleClass="active" />
	  	  </sakai:button_bar>
	  	  <f:verbatim><h4></f:verbatim>
		  <h:outputText value="#{msgs.privacy_all_sites}" />
		  <f:verbatim></h4></f:verbatim>
		<sakai:button_bar>
   			<sakai:button_bar_item action="#{privacyBean.processShowAll}" value="#{msgs.privacy_show_all}"
   				accesskey="s" title="#{msgs.privacy_show_title}" styleClass="active" />
   			<sakai:button_bar_item action="#{privacyBean.processHideAll}" value="#{msgs.privacy_hide_all}"
   				accesskey="s" title="#{msgs.privacy_hide_title}" styleClass="active" />
	  	  </sakai:button_bar>
	   	</h:panelGroup>

    </h:form>
    </sakai:view_content>
    </sakai:view_container>
</f:view>  

<%
	FacesContext context = FacesContext.getCurrentInstance();
	Application app = context.getApplication();
	ValueBinding binding = app.createValueBinding("#{privacyBean}");
	PrivacyBean pb = (PrivacyBean) binding.getValue(context);
	pb.setAllChanged(false);
%> 
