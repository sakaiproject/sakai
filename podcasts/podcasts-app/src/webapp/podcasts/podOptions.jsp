

<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
<% response.setContentType("text/html; charset=UTF-8"); %>

<jsp:useBean id="msgs" class="org.sakaiproject.util.ResourceLoader" scope="session">
	<jsp:setProperty name="msgs" property="baseName" value="org.sakaiproject.api.podcasts.bundle.Messages"/>
</jsp:useBean>

<f:view>
    <link href="/library/skin/tool_base.css" type="text/css" rel="stylesheet" media="all" />
    <link href="/library/skin/default/tool.css" type="text/css" rel="stylesheet" media="all" />

    <script type="text/javascript" src="/library/js/headscripts.js"></script>

  <sakai:view toolCssHref="./css/podcaster.css">
  <script type="text/javascript" src="./scripts/popupscripts.js"></script>
  <h:form enctype="multipart/form-data">
    <div>  <!-- Page title and Instructions -->
      <h3><h:outputText value="#{msgs.options_title}" /></h3>
      <div class="indnt1">
          <p class="instruction"> 
            <h:outputText value="#{msgs.options_directions1}" />
 	          <h:outputLink styleClass="active" onclick="showPopupHere(this,'podcatcher'); return false;"
                  onmouseover="this.style.cursor='pointer'; return false;" onmouseout="hidePopup('podcatcher');">
	          	   <h:outputText value="#{msgs.podcatcher}" />
	          </h:outputLink>
	          <h:outputText value="," />

 	        <h:outputText value="#{msgs.options_directions2}" />
 			<h:outputLink styleClass="active" onclick="showPopupHere(this,'podcatcher'); return false;"
                  onmouseover="this.style.cursor='pointer'; return false;" onmouseout="hidePopup('podcatcher');">
	          	   <h:outputText value="#{msgs.podcatcher}" />
	        </h:outputLink>
	        <h:outputText value="," />

 	        <h:outputText value="#{msgs.options_directions3}" />
          </p>
 	  </div>
    </div>
    <br />

  <table class="table" cellpadding="0" cellspacing="0" border="0"
       summary="Table holds radio buttons to select access rights.">

      <tr class="navIntraTool">
        <th align="center"><h:outputText value="#{msgs.access}" /></th>
      </tr>

      <tr>
        <td class="indnt1">
          <h:selectOneRadio value="#{podOptions.podOption}" layout="pageDirection">
            <f:selectItems value="#{podOptions.displayItems}" />
          </h:selectOneRadio>
        </td>
      </tr>
  </table>
  
    <sakai:button_bar>
      <sakai:button_bar_item action="#{podOptions.processOptionChange}" value="#{msgs.change_submit}"
            accesskey="s" title="Save Podcast Display Options" styleClass="active"/>
      <sakai:button_bar_item action="#{podOptions.processOptionCancel}" value="#{msgs.cancel}" 
            accesskey="c" title="Cancel Podcast Display Options" />
    </sakai:button_bar>
  </h:form>

    <!-- This is the div for the popup definition. It is not displayed until the element is moused over -->
    <div id="podcatcher" class="podcatcher_popup" 
        style="position:absolute; top: -1000px; left: -1000px;">
      <h:outputText value="#{msgs.popup_text}" />
    </div>
  
  </sakai:view>
</f:view>  
