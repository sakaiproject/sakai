<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
<% response.setContentType("text/html; charset=UTF-8"); %>

<f:loadBundle basename="org.sakaiproject.tool.podcasts.bundle.Messages" var="msgs"/>

<f:view>
  <sakai:view>
    <link href="./css/podcaster.css" type="text/css" rel="stylesheet" media="all" />

      <!--  TODO: figure out why it is not finding this -->
      <script type="text/javascript" src="/library/calendar/js/calendar2.js" language="JavaScript" ></script>
      
  <h:form id="podAdd" enctype="multipart/form-data">

    <div>  <!-- Page title and Instructions -->
      <h3><h:outputText value="#{msgs.add_title}" /></h3>
      <div class="indnt1">
          <p class="instruction"> 
            <h:outputText value="#{msgs.add_directions}" />
 	        <br /><br />
            <h:outputText value="#{msgs.required_prompt}" />
            <span class="reqStarInline">*</span>
          </p>
 	  </div>
    </div>
    <br />

    <div class="indnt1">  <!-- Choose a file -->
      <span class="reqStarInline">*</span>
 	  <h:outputText value="#{msgs.file_prompt}" styleClass="reqPrompt" />
 	  <sakai:inputFileUpload id="podfile" valueChangeListener="#{podHomeBean.processFileUpload}"
 	     styleClass="indnt1" size="35" />

<!--      h:message for="podfile" styleClass="alertMessage" -->
	  <h:outputText value="#{msgs.nofile_alert}" styleClass="alertMessage" rendered="#{podHomeBean.displayNoFileErrMsg}" />
    </div>
    <br />

    <div class="indnt1">  <!-- Date -->
      <span class="reqStarInline">*</span>
      <h:outputText value="#{msgs.date_prompt}" styleClass="reqPrompt" />

      <sakai:input_date id="poddate" value="#{podHomeBean.date}" showDate="true" rendered="true" />

<!--        h:message for="poddate" styleClass="alertMessage" /> -->
 	  <h:outputText value="#{msgs.nodate_alert}" styleClass="alertMessage" rendered="#{podHomeBean.displayNoDateErrMsg}" /> 
 	</div>
    <br />
     
    <div class="indnt1">  <!-- Title -->
      <span class="reqStarInline">*</span>
      <h:outputText value="#{msgs.title_prompt}" styleClass="reqPrompt" />
 	  <h:inputText id="podtitle" value="#{podHomeBean.title}" styleClass="indnt3" size="35" maxlength="255" />

<!--       h:message for="podtitle" styleClass="alertMessage" /> -->
	  <h:outputText value="#{msgs.notitle_alert}" styleClass="alertMessage" rendered="#{podHomeBean.displayNoTitleErrMsg}" />
    </div>
    <br />

    <div class="indnt1"> <!-- Description -->
      <h:outputText value="#{msgs.description_prompt}" styleClass="reqPrompt" />
      <br />
      <h:inputTextarea value="#{podHomeBean.description}" rows="6" cols="80" />
    </div>
    <br />

    <div class="indnt1"> <!-- Email Notification -->
      <h:outputText value="#{msgs.email_prompt}" />
      <h:selectOneMenu value="#{podHomeBean.email}">
          <f:selectItems value="#{podHomeBean.emailItems}" />
      </h:selectOneMenu>
      
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