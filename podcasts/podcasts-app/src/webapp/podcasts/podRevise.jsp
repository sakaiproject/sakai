<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf2/sakai" prefix="sakai" %>
<% response.setContentType("text/html; charset=UTF-8"); %>

<jsp:useBean id="msgs" class="org.sakaiproject.util.ResourceLoader" scope="session">
	<jsp:setProperty name="msgs" property="baseName" value="org.sakaiproject.api.podcasts.bundle.Messages"/>
</jsp:useBean>

<f:view>
    <link href="/library/skin/tool_base.css" type="text/css" rel="stylesheet" media="all" />
    <link href="/library/skin/default-skin/tool.css" type="text/css" rel="stylesheet" media="all" />

  <sakai:view toolCssHref="./css/podcaster.css">
      <script>includeLatestJQuery("podRevise");</script>
      <script src="/library/js/lang-datepicker/lang-datepicker.js"></script>

      <script>
        $(document).ready(function() {
           localDatePicker({
              input: '#podRev\\:poddate',
              useTime: 1,
              parseFormat: 'YYYY-MM-DD HH:mm:ss',
              allowEmptyDate: false,
              val: '<h:outputText value="#{podHomeBean.selectedPodcast.displayDateRevise}"><f:convertDateTime pattern="yyyy-MM-dd HH:mm:ss"/></h:outputText>',
              ashidden: { iso8601: 'podReviseISO8601' }
          });

          var menuLink = $('#podcastMainMenuLink');
          var menuLinkSpan = menuLink.closest('span');
          menuLinkSpan.addClass('current');
          menuLinkSpan.html(menuLink.text());

        });
      </script>

  <h:form id="podRev" enctype="multipart/form-data">
    <%@ include file="/podcasts/podcastMenu.jsp" %>
    <div>  <!-- Page title and Instructions -->
      <div class="page-header">
        <h1><h:outputText value="#{msgs.revise_title}" /></h1>
      </div>
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
        <td colspan=3><h:message for="podfile" styleClass="sak-banner-error" /></td>
      </tr>
      <tr>  <!-- ****** Date ****** --> 
        <td><h:outputText value="#{msgs.date_prompt}" />&nbsp;&nbsp;&nbsp;</td>
        <td>
	      <h:inputText value="#{podHomeBean.selectedPodcast.displayDateRevise}" size="28" id="poddate" />
		</td>
     </tr>
     <tr>
       <td colspan="3"><h:outputText value="#{msgs.invalid_date_alert}" styleClass="sak-banner-error" rendered="#{podHomeBean.displayInvalidDateErrMsg}" /></td>
     </tr>
     <tr>
       <td colspan="3"><h:message for="poddate" styleClass="sak-banner-error" /></td>
     </tr>
    <tr>  <!-- ****** Title ****** -->
      <td><h:outputText value="#{msgs.title_prompt}" /></td>
 	  <td><h:inputText id="podtitle" value="#{podHomeBean.selectedPodcast.title}" size="35" maxlength="255" /></td>
 	</tr>
    <tr>
	  <td colspan="3"><h:outputText value="#{msgs.notitle_alert}" styleClass="sak-banner-error" rendered="#{podHomeBean.displayNoTitleErrMsg}" /></td>
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
      <h:commandButton action="#{podHomeBean.processRevisePodcast}" value="#{msgs.change_submit}"
          accesskey="s" title="#{msgs.change_submit}" styleClass="active" />
      <h:commandButton action="#{podHomeBean.processCancelRevise}" value="#{msgs.cancel}"
          accesskey="x" title="#{msgs.cancel}" />
    </sakai:button_bar>
   </h:form>
 </sakai:view>
</f:view>
