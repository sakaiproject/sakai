<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
<% response.setContentType("text/html; charset=UTF-8"); %>

<jsp:useBean id="msgs" class="org.sakaiproject.util.ResourceLoader" scope="session">
	<jsp:setProperty name="msgs" property="baseName" value="org.sakaiproject.api.podcasts.bundle.Messages"/>
</jsp:useBean>

<f:view>
    <link href="./css/podcaster.css" type="text/css" rel="stylesheet" media="all" />

  <sakai:view>
      <script type="text/javascript">includeLatestJQuery("podAdd");</script>
      <script type="text/javascript" src="/library/js/lang-datepicker/lang-datepicker.js"></script>

      <script type="text/javascript">
        $(document).ready(function() {
           localDatePicker({
              input: '#podAdd\\:addDate',
              useTime: 1,
              parseFormat: 'YYYY-MM-DD HH:mm:ss',
              allowEmptyDate: true,
              val: '',
              ashidden: { iso8601: 'podAddISO8601' }
          });
        });
      </script>
      
  <h:form id="podAdd" enctype="multipart/form-data">

    <div>  <!-- Page title and Instructions -->
       <h3><h:outputText value="#{msgs.add_title}" /></h3>
		<%-- SAK-9822: added error message when too large file was attempted to be uploaded  --%>
	    <h:outputText value="#{podHomeBean.maxSizeExceededAlert}" styleClass="alertMessage" rendered="#{podHomeBean.uploadStatus}" />
       <h:outputText value="#{msgs.add_directions}" styleClass="indnt1" /> <br />
       <h:outputText value="#{msgs.required_prompt}" styleClass="indnt1" />
       <span class="reqStarInline indnt1">*</span>
       <h:messages styleClass="alertMessage" id="errorMessages" rendered="#{! empty facesContext.maximumSeverity}" />
    </div>
    <br />
    
    	<table class="indnt1 nolines">
    	  <tr>
    	   <td>
     		  <h:outputText value=" " rendered="#{podHomeBean.errorOnPage && ! empty podHomeBean.filename}" />
      		</td>
     		<td>
     		  <h:outputText value="#{msgs.file_to_upload}" rendered="#{podHomeBean.errorOnPage && ! empty podHomeBean.filename}" />
      		</td>
      		<td><b><h:outputText value="#{podHomeBean.filename}" rendered="#{podHomeBean.errorOnPage && ! empty podHomeBean.filename}" /></b></td>
		  </tr>
    	  <tr> <!-- ****** Choose a file ****** -->
      		<td class="reqStarInline">*</td>
 	  		<td><h:outputText value="#{msgs.file_prompt}" /></td>
			<td>
			  <sakai:inputFileUpload id="podfile" valueChangeListener="#{podHomeBean.processFileUpload}" size="35" />
 	     	</td>
 	      </tr>
 	      <tr>
			<td colspan="3"><h:outputText value="#{msgs.nofile_alert}" styleClass="alertMessage" rendered="#{podHomeBean.displayNoFileErrMsg}" /></td>
	      </tr>
		  <tr>  <!-- ****** Date ****** -->
      		<td class="reqStarInline">*</td>
	      	<td><label for="podAdd:addDate"><h:outputText value="#{msgs.date_prompt}" /></label>&nbsp;&nbsp;&nbsp;</td>
			<td>
				<h:inputText value="#{podHomeBean.date}" size="28" id="addDate" />
	  		</td>
	  	  </tr>
	  	  <tr>
 	  		<td colspan="3">
 	  			<h:outputText value="#{msgs.nodate_alert}" styleClass="alertMessage" rendered="#{podHomeBean.displayNoDateErrMsg}" /> 
 	  			<h:outputText value="#{msgs.invalid_date_alert}" styleClass="alertMessage" rendered="#{podHomeBean.displayInvalidDateErrMsg}" />
 	  		</td>
 	  	  </tr>
		  <tr>  <!-- ****** Title ****** -->
		    <td class="reqStarInline">*</td>
		    <td><label for="podAdd:podtitle"><h:outputText value="#{msgs.title_prompt}" /></label></td>
	 	  	<td><h:inputText id="podtitle" value="#{podHomeBean.title}" size="35" maxlength="255" /></td>
	 	  </tr>
	 	  <tr>
			<td colspan="3"><h:outputText value="#{msgs.notitle_alert}" styleClass="alertMessage" rendered="#{podHomeBean.displayNoTitleErrMsg}" /></td>
	      </tr>
          <tr> <!-- ****** Description ****** -->
      		<td colspan="2"><label for="podAdd:poddescription"><h:outputText value="#{msgs.description_prompt}" /></label></td>
      	  </tr>
      	  <tr>
	        <td colspan="3"><h:inputTextarea id="poddescription" value="#{podHomeBean.description}" rows="6" cols="80" /></td>
    	  </tr>
		</table>

    <sakai:button_bar>
        <sakai:button_bar_item action="#{podHomeBean.processAdd}" value="#{msgs.add}"
            accesskey="a" title="Add a Podcast" styleClass="active" />
        <sakai:button_bar_item action="#{podHomeBean.processCancelAdd}" value="#{msgs.cancel}" 
            accesskey="c" title="Cancel a Podcast" />
    </sakai:button_bar>
  </h:form>
 </sakai:view>
</f:view>
