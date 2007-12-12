<%-- Included on main page if Resources tool not included in site --%>

<%-- Instructors (maintain) get error message --%>
<h:panelGroup rendered="#{podHomeBean.canUpdateSite}" >
	<h:messages styleClass="alertMessage" id="errorMessagesNR" /> 
</h:panelGroup>

<%-- Students (access) get no podcasts exist --%>
<h:panelGroup rendered="#{! podHomeBean.canUpdateSite}" >
	<f:verbatim><div><h3></f:verbatim>
 	        	
	<h:outputText value="#{msgs.podcast_home_title}" />
 	      	  
 	<f:verbatim></h3>
	<div class="indnt1">
    <br /></f:verbatim>
        
    <h:outputText  styleClass="instruction" value="#{msgs.no_podcasts}"  />
    <f:verbatim></div></f:verbatim>
</h:panelGroup>
