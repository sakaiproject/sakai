

<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
<% response.setContentType("text/html; charset=UTF-8"); %>

<f:loadBundle basename="org.sakaiproject.api.podcasts.bundle.Messages" var="msgs"/>

<f:view>
    <link href="/library/skin/tool_base.css" type="text/css" rel="stylesheet" media="all" />
    <link href="/library/skin/default/tool.css" type="text/css" rel="stylesheet" media="all" />
    <link href="./css/podcaster.css" type="text/css" rel="stylesheet" media="all" />

    <script type="text/javascript" language="JavaScript" src="/library/js/headscripts.js"></script>
    <script type="text/javascript" language="JavaScript" src="./scripts/popupscripts.js"></script>

  <sakai:view>
  <h:form enctype="multipart/form-data">
    <div>  <!-- Page title and Instructions -->
      <h3><h:outputText value="#{msgs.options_title}" /></h3>
      <div class="indnt1">
          <p class="instruction"> 
            <h:outputText value="#{msgs.options_directions1}" />
            <span onClick="showPopupHere(this,'podcatcher'); return false;" 
            	     onMouseOver="this.style.cursor='pointer'; return false;"
 	              onMouseOut="hidePopup('podcatcher');">
 	        <h:outputLink styleClass="active" onclick="return false">
 	           <h:outputText value="#{msgs.podcatcher}" />
 	        </h:outputLink>,
 	        </span>

 	        <h:outputText value="#{msgs.options_directions2}" />
            <span onClick="showPopupHere(this,'podcatcher'); return false;" 
            	     onMouseOver="this.style.cursor='pointer'; return false;"
 	              onMouseOut="hidePopup('podcatcher');">
	        <h:outputLink styleClass="active" onclick="return false">
 	           <h:outputText value="#{msgs.podcatcher}" />
 	        </h:outputLink>,
 	        </span>

 	        <h:outputText value="#{msgs.options_directions3}" />
          </p>
 	  </div>
    </div>
    <br />

  <table class="listHier lines nolines nocolor" cellpadding="0" cellspacing="0" border="0"
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
  
  </sakai:view>

    <!-- This is the div for the popup definition. It is not displayed until the element is moused over -->
    <div id="podcatcher" class="podcatcher_popup" 
        style="position:absolute; top: -1000px; left: -1000px;">
      <h:outputText value="#{msgs.popup_text}" />
    </div>
</f:view>  