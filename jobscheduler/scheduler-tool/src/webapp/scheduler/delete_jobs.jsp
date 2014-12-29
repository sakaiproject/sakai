<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
<%@ include file="security_static_include.jsp"%>


<f:view>
	<sakai:view_container title="#{msgs.title_job}">		
	  <h:form>
  	  <h:graphicImage value="/images/quartz.jpg" alt="#{msgs.powered_by} Quartz"/>      
   	  <sakai:view_content>
    	  <h:outputText value="#{msgs.select_jobs_first}" styleClass="alertMessage" rendered="#{empty schedulerTool.filteredJobDetailWrapperList}"/>    	        
  	    <h:dataTable rendered="#{!empty schedulerTool.filteredJobDetailWrapperList}" value="#{schedulerTool.filteredJobDetailWrapperList}" var="job" styleClass="chefFlatListViewTable" >
  	      <h:column>
    	      <f:facet name="header">    	      
    	        <h:outputText value="#{msgs.delete_jobs_confirm}" styleClass="alertMessage"/>    	        
    	      </f:facet>
   	        <h:outputText value="#{job.jobDetail.name}"/>
    	    </h:column>  	      
        </h:dataTable>        
   		  <sakai:button_bar>
  				<sakai:button_bar_item
						action="#{schedulerTool.processDeleteJobs}"
						value="#{msgs.ok}" />					
					<sakai:button_bar_item
					  rendered="#{!empty schedulerTool.filteredJobDetailWrapperList}"
						action="jobs"
						value="#{msgs.cancel}" />
				</sakai:button_bar>
		  </sakai:view_content>
  	</h:form>
	</sakai:view_container>
</f:view>
