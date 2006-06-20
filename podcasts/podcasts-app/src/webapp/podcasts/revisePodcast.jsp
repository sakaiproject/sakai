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
      <h3><h:outputText value="#{msgs.podcast_revise_title}" /></h3>
      <div class="indnt1">
          <p class="instruction"> 
            <h:outputText value="#{msgs.podcast_revise_directions}" />
 	        <br /><br />
            <h:outputText value="#{msgs.podcast_required_prompt}" />
            <span class="reqStarInline">*</span>
          </p>
 	  </div>
    </div>
    <br />

    <div class="indnt1">  <!-- Choose a file -->
      <span class="reqStarInline">*</span>
 	  <h:outputText value="#{msgs.podcast_file_prompt}" styleClass="reqPrompt" />
 	  <sakai:inputFileUpload id="podfile" value="#{podcastBean.filename}"
 	     valueChangeListener="#{podcastBean.processFileUpload}" 
 	     styleClass="indnt1" size="35" />

      <h:message for="podfile" styleClass="alertMessage" />
    </div>
    <br />

    <div class="indnt1">  <!-- Date -->
      <span class="reqStarInline">*</span>
      <h:outputText value="#{msgs.podcast_date_prompt}" styleClass="reqPrompt" />

     <sakai:input_date id="poddate" value="#{podcastBean.date}" showDate="true" rendered="true" />
     
      <h:message for="poddate" styleClass="alertMessage" />
    </div>
    <br />
     
    <div class="indnt1">  <!-- Title -->
      <span class="reqStarInline">*</span>
      <h:outputText value="#{msgs.podcast_title_prompt}" styleClass="reqPrompt" />
 	  <h:inputText id="podtitle" value="#{podcastBean.title}" styleClass="indnt3" size="35" />

      <h:message for="podtitle" styleClass="alertMessage" />
    </div>
    <br />

    <div class="indnt1"> <!-- Description -->
      <h:outputText value="#{msgs.podcast_add_description_prompt}" styleClass="reqPrompt" />
      <br />
      <h:inputTextarea value="#{podcastBean.description}" rows="6" cols="80" />
    </div>
    <br />

    <div class="indnt1"> <!-- Email Notification -->
      <h:outputText value="#{msgs.podcast_add_email_prompt}" />
      <h:selectOneMenu value="#{podcastBean.email}">
          <f:selectItems value="#{podcastBean.emailItems}" />
      </h:selectOneMenu>
      
    </div>
    <br />

    <sakai:button_bar>  <!-- Save Changes and Cancel buttons -->
      <sakai:button_bar_item action="#{podcastBean.processRevisePodcast}" value="#{msgs.podcast_change_submit}" 
          accesskey="s" title="Save changes to Podcasts" styleClass="active" />
      <sakai:button_bar_item action="#{podcastBean.processCancelRevise}" value="#{msgs.podcast_cancel}" 
          accesskey="c" title="Cancel Changes" />
    </sakai:button_bar>
   </h:form>
 </sakai:view>
</f:view>