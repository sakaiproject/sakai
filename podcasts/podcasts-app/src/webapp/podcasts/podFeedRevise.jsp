  <%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
  <%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
  <%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
  <% response.setContentType("text/html; charset=UTF-8"); %>

<f:loadBundle basename="org.sakaiproject.tool.podcasts.bundle.Messages" var="msgs"/>

  <f:view>
    <sakai:view>
        <link href="./css/podcaster.css" type="text/css" rel="stylesheet" media="all" />

        <script type="text/javascript" language="JavaScript" src="scripts/popupscripts.js"></script>
    <h:form id="podFeedRev" enctype="multipart/form-data">

    <div>  <!-- Page title and Instructions -->
      <h3><h:outputText value="#{msgs.podfeed_revise_title}" /></h3>
      <div class="indnt1">
          <p class="instruction"> 
            <h:outputText value="#{msgs.podfeed_revise_directions}" />
 	        <br /><br />
            <h:outputText value="#{msgs.required_prompt}" />
            <span class="reqStarInline">*</span>
          </p>
 	  </div>
    </div>
    <br />

    <div class="indnt1">  <!-- Choose a file -->
      <h:outputText value="#{msgs.podfeed_revise_url_caption}" />
      <b><h:outputText value="#{podHomeBean.URL}" styleClass="indnt1" /></b>
    </div>
    <br />

    <div class="indnt1">  <!-- Title -->
      <span class="reqStarInline">*</span>
      <h:outputText value="#{msgs.title_prompt}" styleClass="reqPrompt" />
 	  <h:inputText id="podtitle" value="#{podFeedBean.podFeedTitle}" styleClass="indnt3" size="35" maxlength="255" />

      <h:message for="podtitle" styleClass="alertMessage" />
    </div>
    <br />

    <div class="indnt1"> <!-- Description -->
      <span class="reqStarInline">*</span>
      <h:outputText value="#{msgs.description_prompt}" styleClass="reqPrompt" />
      <br />
      <h:inputTextarea value="#{podFeedBean.podFeedDescription}" rows="6" cols="80" />
    </div>
    <br />

    <sakai:button_bar>  <!-- Save Changes and Cancel buttons -->
      <sakai:button_bar_item action="#{podFeedBean.processPodfeedRevise}" value="#{msgs.change_submit}" 
          accesskey="s" title="#{msgs.change_submit}" styleClass="active" />
      <sakai:button_bar_item action="#{podFeedBean.processCancelPodfeedRevise}" value="#{msgs.cancel}" 
          accesskey="c" title="#{msgs.cancel}" />
    </sakai:button_bar>

    </h:form>
    </sakai:view>
	</f:view>
</body>
</html>
