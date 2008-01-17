<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
<%@ taglib uri="http://www.sakaiproject.org/podcasts" prefix="podcasts" %>
<% response.setContentType("text/html; charset=UTF-8"); %>

<jsp:useBean id="msgs" class="org.sakaiproject.util.ResourceLoader" scope="session">
	<jsp:setProperty name="msgs" property="baseName" value="org.sakaiproject.api.podcasts.bundle.Messages"/>
</jsp:useBean>

<f:view>
    <link href="/library/skin/tool_base.css" type="text/css" rel="stylesheet" media="all" />
    <link href="/library/skin/default/tool.css" type="text/css" rel="stylesheet" media="all" />
    <link href="./css/podcaster.css" type="text/css" rel="stylesheet" media="all" />

    <script type="text/javascript" src="jsf/widget/datepicker/datepicker.js" language="JavaScript" ></script> 
  <sakai:view>

  <h:form id="podRev" enctype="multipart/form-data">

    <div>  <!-- Page title and Instructions -->
      <h3><h:outputText value="#{msgs.revise_title}" /></h3>
      <div class="indnt1">
          <p class="instruction"> 
            <h:outputText value="#{msgs.revise_directions}" />
          </p>
 	  </div>
    </div>
    <br />

    <table class="indnt1 nolines">
      <tr>  <!-- ****** Choose a file ****** -->
        <td><h:outputText value="#{msgs.current_file}" /></td>
        <td><h:outputText value="#{podHomeBean.selectedPodcast.filename}" /></td>
      </tr>
      <tr>
 	    <td><h:outputText value="#{msgs.file_prompt}" /></td>
 	    <td>
 	      <sakai:inputFileUpload id="podfile" valueChangeListener="#{podHomeBean.processFileUpload}" size="35" />
		</td>
	  </tr>
	  <tr>
        <td colspan=3><h:message for="podfile" styleClass="alertMessage" /></td>
      </tr>
      <tr>  <!-- ****** Date ****** --> 
        <td><h:outputText value="#{msgs.date_prompt}" />&nbsp;&nbsp;&nbsp;</td>
        <td>
	      <podcasts:datePicker value="#{podHomeBean.selectedPodcast.displayDateRevise}" id="poddate" size="28" />
		  <h:outputText value="#{msgs.date_picker_format_string}" />
		</td>
     </tr>
     <tr>
       <td colspan="3"><h:outputText value="#{msgs.invalid_date_alert}" styleClass="alertMessage" rendered="#{podHomeBean.displayInvalidDateErrMsg}" /></td>
     </tr>
     <tr>
       <td colspan="3"><h:message for="poddate" styleClass="alertMessage" /></td>
     </tr>
    <tr>  <!-- ****** Title ****** -->
      <td><h:outputText value="#{msgs.title_prompt}" /></td>
 	  <td><h:inputText id="podtitle" value="#{podHomeBean.selectedPodcast.title}" size="35" maxlength="255" /></td>
 	</tr>
    <tr>
	  <td colspan="3"><h:outputText value="#{msgs.notitle_alert}" styleClass="alertMessage" rendered="#{podHomeBean.displayNoTitleErrMsg}" /></td>
    </tr>
    <tr> <!-- ****** Description ****** -->
      <td colspan="2"><h:outputText value="#{msgs.description_prompt}" />
    </tr>
    <tr>
      <td colspan="3"><h:inputTextarea value="#{podHomeBean.selectedPodcast.description}" rows="6" cols="80" /></td>
    </tr>
  </table>

<%--    <div class="indnt1"> <!-- Email Notification -->
      <h:outputText value="#{msgs.email_prompt}" />
      <h:selectOneMenu value="#{podHomeBean.email}">
          <f:selectItems value="#{podHomeBean.emailItems}" />
      </h:selectOneMenu>
      
    </div>
    <br />
--%>
    <sakai:button_bar>  <!-- Save Changes and Cancel buttons -->
      <sakai:button_bar_item action="#{podHomeBean.processRevisePodcast}" value="#{msgs.change_submit}" 
          accesskey="s" title="#{msgs.change_submit}" styleClass="active" />
      <sakai:button_bar_item action="#{podHomeBean.processCancelRevise}" value="#{msgs.cancel}" 
          accesskey="c" title="#{msgs.cancel}" />
    </sakai:button_bar>
   </h:form>
 </sakai:view>
</f:view>