<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>


<%
	String saved = request.getParameter("saved");
	if(saved != null && !"".equals(saved)){
%>
	<script type="text/javascript">		
		parent.dialogutil.closeDialogAndSubmitForm('dialogDiv', 'dialogFrame', parent.document.gbForm);
	</script>
<%
	}else{
	
%>
<f:view>

<sakai:flowState bean="#{rosterBean}" />
<script src="/library/js/spinner.js" type="text/javascript"></script>
<h:form id="hidShowForm">
	<h2>
		<h:outputText value="#{msgs.hide_show_openLink}"/>
	</h2>
	<div class="information">
		<h:outputText value="#{msgs.hide_show_information}"/>
	</div>
   	<h:dataTable id="hideColumns" value="#{rosterBean.gradableObjectColumns}" var="colVar" styleClass="listHier lines">
   		<h:column rendered="#{colVar.assignmentColumn}">
   			<f:facet name="header">
   				<h:outputText value="#{msgs.hide_show_hide}"/>
   			</f:facet>
   			<h:selectBooleanCheckbox id="hide" value="#{colVar.hideInAllGradesTable}" title="#{msgs.hide_show_hide} #{colVar.name}" />
   		</h:column>
   		<h:column rendered="#{colVar.assignmentColumn}">
   			<f:facet name="header">
   				<h:outputText value="#{msgs.hide_show_assignment}"/>
   			</f:facet>
   			<h:outputText value="#{colVar.name}"/>
   		</h:column>
   	</h:dataTable>
   
  	<div class="instruction" style="float:right;">	        	
       	<h:commandButton action="#{rosterBean.saveHidden}" value="#{msgs.feedback_options_submit}" onclick="SPNR.disableControlsAndSpin( this, null );"/>
        <input type="button" value='<h:outputText value="#{msgs.feedback_options_cancel}"/>' onclick="SPNR.disableControlsAndSpin( this, null );parent.dialogutil.closeDialog('dialogDiv', 'dialogFrame');"/>
  	</div>
  	<f:verbatim>    
    	<input type="text" id="saved" name="saved" value="true" style="display: none;"/>
    </f:verbatim>   
</h:form>
</f:view>
<%
	}
%>
