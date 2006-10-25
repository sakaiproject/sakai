<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
<%@ taglib uri="http://www.sakaiproject.org/podcasts" prefix="podcasts" %>
<% response.setContentType("text/html; charset=UTF-8"); %>

<f:loadBundle basename="org.sakaiproject.api.podcasts.bundle.Messages" var="msgs"/>

<f:view>
    <link href="./css/podcaster.css" type="text/css" rel="stylesheet" media="all" />

  <sakai:view>
      <script type="text/javascript" src="jsf/widget/datepicker/datepicker.js" language="JavaScript" ></script> 
      
  <h:form id="podAdd" enctype="multipart/form-data">

    <div>  <!-- Page title and Instructions -->
       <h3><h:outputText value="#{msgs.add_title}" /></h3>
       <h:outputText value="#{msgs.add_directions}" styleClass="indnt1 instruction" /><br />
       <h:outputText value="#{msgs.required_prompt}" styleClass="indnt1 instruction" />
       <span class="reqStarInline indnt1">*</span>
       <h:messages styleClass="alertMessage" id="errorMessages"/>
    </div>
    <br /><br />
    
      <h:outputText value="#{msgs.file_to_upload}" rendered="#{podHomeBean.displayNoFileErrMsg && ! empty podHomeBean.filename }" 
      		styleClass="indnt1" />
 	  <h:outputText value="#{podHomeBean.filename}" rendered="#{podHomeBean.displayNoFileErrMsg}" styleClass="indnt1" />

    <div class="indnt1">  <!-- Choose a file -->
      <span class="reqStarInline">*</span>
 	  <h:outputText value="#{msgs.file_prompt}" styleClass="reqPrompt" />

 	  <sakai:inputFileUpload id="podfile" valueChangeListener="#{podHomeBean.processFileUpload}"
 	     styleClass="indnt6" size="35" />

	  <h:outputText value="#{msgs.nofile_alert}" styleClass="alertMessage" rendered="#{podHomeBean.displayNoFileErrMsg}" />
    </div>
    <br />

    <div class="indnt1">  <!-- Date -->
      <span class="reqStarInline">*</span>
      <h:outputText value="#{msgs.date_prompt}" styleClass="reqPrompt" />

	<span class="indnt1">
      <podcasts:datePicker value="#{podHomeBean.date}" id="addDate" size="28" />
	  <h:outputText value="#{msgs.date_picker_format_string}" />
	</span>

 	  <h:outputText value="#{msgs.nodate_alert}" styleClass="alertMessage" rendered="#{podHomeBean.displayNoDateErrMsg}" /> 
 	  <h:outputText value="#{msgs.invalid_date_alert}" styleClass="alertMessage" rendered="#{podHomeBean.displayInvalidDateErrMsg}" />
 	</div>
    <br />
     
    <div class="indnt1">  <!-- Title -->
      <span class="reqStarInline">*</span>
      <h:outputText value="#{msgs.title_prompt}" styleClass="reqPrompt" />

 	  <h:inputText id="podtitle" value="#{podHomeBean.title}" styleClass="indnt4" size="35" maxlength="255" />

	  <h:outputText value="#{msgs.notitle_alert}" styleClass="alertMessage" rendered="#{podHomeBean.displayNoTitleErrMsg}" />
    </div>
    <br />

    <div class="indnt1"> <!-- Description -->
      <h:outputText value="#{msgs.description_prompt}" styleClass="reqPrompt" />
      <br />
      <h:inputTextarea value="#{podHomeBean.description}" rows="6" cols="80" />
    </div>
    <br />

    <sakai:button_bar>
        <sakai:button_bar_item action="#{podHomeBean.processAdd}" value="#{msgs.add}"
            accesskey="a" title="Add a Podcast" styleClass="active" />
        <sakai:button_bar_item action="#{podHomeBean.processCancelAdd}" value="#{msgs.cancel}" 
            accesskey="c" title="Cancel a Podcast" />
    </sakai:button_bar>
  </h:form>
 </sakai:view>
</f:view>