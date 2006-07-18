<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
<% response.setContentType("text/html; charset=UTF-8"); %>

<f:loadBundle basename="org.sakaiproject.tool.podcasts.bundle.Messages" var="msgs"/>

<f:view>
  <sakai:view>
    <link href="/library/skin/tool_base.css" type="text/css" rel="stylesheet" media="all" />
    <link href="/library/skin/default/tool.css" type="text/css" rel="stylesheet" media="all" />
    <link href="./css/podcaster.css" type="text/css" rel="stylesheet" media="all" />

    <!--  TODO: figure out why it is not finding this -->
    <script type="text/javascript" src="/library/calendar/js/calendar2.js" language="JavaScript" ></script>

  <h:form id="podRev" enctype="multipart/form-data">

    <div>  <!-- Page title and Instructions -->
      <h3><h:outputText value="#{msgs.revise_title}" /></h3>
      <div class="indnt1">
          <p class="instruction"> 
            <h:outputText value="#{msgs.revise_directions}" />
 	        <br /><br />
            <h:outputText value="#{msgs.required_prompt}" />
            <span class="reqStarInline">*</span>
          </p>
 	  </div>
    </div>
    <br />

    <div class="indnt1">  <!-- Choose a file -->
      <h:outputText value="#{msgs.current_file}" />
      <b><h:outputText value="#{podHomeBean.selectedPodcast.filename}" styleClass="indnt2" /></b>
      <br />
      
 	  <h:outputText value="#{msgs.file_prompt}" styleClass="reqPrompt" />
 	  <sakai:inputFileUpload id="podfile" value="#{podHomeBean.filename}"
 	     valueChangeListener="#{podHomeBean.processFileUpload}" 
 	     styleClass="indnt1" size="35" />

      <h:message for="podfile" styleClass="alertMessage" />
    </div>
    <br />

    <div class="indnt1">  <!-- Date -->
     <h:outputText value="#{msgs.current_date}" />
     <b><h:outputText value="#{podHomeBean.selectedPodcast.displayDate}" styleClass="indnt2" /></b>
     <br />
     
      <h:outputText value="#{msgs.date_prompt}" styleClass="reqPrompt" />

     <sakai:input_date id="poddate" value="#{podHomeBean.date}" showDate="true" rendered="true" />
     
      <h:message for="poddate" styleClass="alertMessage" />
    </div>
    <br />
     
    <div class="indnt1">  <!-- Title -->
      <h:outputText value="#{msgs.title_prompt}" styleClass="reqPrompt" />
 	  <h:inputText id="podtitle" value="#{podHomeBean.selectedPodcast.title}" styleClass="indnt3" size="35" maxlength="255" />

      <h:message for="podtitle" styleClass="alertMessage" />
    </div>
    <br />

    <div class="indnt1"> <!-- Description -->
      <h:outputText value="#{msgs.description_prompt}" styleClass="reqPrompt" />
      <br />
      <h:inputTextarea value="#{podHomeBean.selectedPodcast.description}" rows="6" cols="80" />
    </div>
    <br />

    <div class="indnt1"> <!-- Email Notification -->
      <h:outputText value="#{msgs.email_prompt}" />
      <h:selectOneMenu value="#{podHomeBean.email}">
          <f:selectItems value="#{podHomeBean.emailItems}" />
      </h:selectOneMenu>
      
    </div>
    <br />

    <sakai:button_bar>  <!-- Save Changes and Cancel buttons -->
      <sakai:button_bar_item action="#{podHomeBean.processRevisePodcast}" value="#{msgs.change_submit}" 
          accesskey="s" title="Save changes to Podcasts" styleClass="active" />
      <sakai:button_bar_item action="#{podHomeBean.processCancelRevise}" value="#{msgs.cancel}" 
          accesskey="c" title="Cancel Changes" />
    </sakai:button_bar>
   </h:form>
 </sakai:view>
</f:view>