

<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
<% response.setContentType("text/html; charset=UTF-8"); %>

<f:loadBundle basename="org.sakaiproject.tool.podcaster.bundle.Messages" var="msgs"/>

<f:view>
  <sakai:view>
    <link href="/library/skin/tool_base.css" type="text/css" rel="stylesheet" media="all" />
    <link href="/library/skin/default/tool.css" type="text/css" rel="stylesheet" media="all" />
    <link href="./css/podcaster.css" type="text/css" rel="stylesheet" media="all" />

    <script type="text/javascript" language="JavaScript" src="/library/js/headscripts.js"></script>
    <script type="text/javascript" language="JavaScript" src="./scripts/popupscripts.js"></script>

  <h:form enctype="multipart/form-data">
    <div>  <!-- Page title and Instructions -->
      <h3><h:outputText value="#{msgs.podcast_options_title}" /></h3>
      <div class="indnt1">
          <p class="instruction"> 
            <h:outputText value="#{msgs.podcast_options_directions1}" />
            <span onmouseover="showPopupHere(this,'podcatcher'); return false;" 
 	              onmouseout="hidePopup('podcatcher');" style="color: #0099cc;" class="active">
 	           <h:outputText value="#{msgs.podcast_home_podcatcher}" />,
 	        </span>

 	        <h:outputText value="#{msgs.podcast_options_directions2}" />
            <span onmouseover="showPopupHere(this,'podcatcher'); return false;" 
 	              onmouseout="hidePopup('podcatcher');" style="color: #0099cc;" class="active">
 	           <h:outputText value="#{msgs.podcast_home_podcatcher}" />,
 	        </span>

 	        <h:outputText value="#{msgs.podcast_options_directions3}" />
          </p>
 	  </div>
    </div>
    <br />

  <table class="listHier lines nolines" cellpadding="0" cellspacing="0" border="0"
       summary="Table holds radio buttons to select access rights.">

      <tr class="navIntraTool">
        <th align="center"><h:outputText value="#{msgs.podcast_access}" /></th>
      </tr>

      <tr>
        <td class="indnt1">
          <h:selectOneRadio value="#{podOptions.podOption}" layout="pageDirection">
            <f:selectItems value="#{podOptions.displayItems}" />
          </h:selectOneRadio>
        </td>
      </tr>
  </table>
  
    <div class="act">
      <h:commandButton type="submit" value="#{msgs.podcast_change_submit}" styleClass="active"/>
      <h:form>
        <h:commandButton type="submit" value="#{msgs.podcast_cancel}" styleClass="reqPrompt" 
             action="cancel" />
      </h:form>
    </div>
  </h:form>
  
  </sakai:view>

    <!-- This is the div for the popup definition. It is not displayed until the element is moused over -->
    <div id="podcatcher" class="podcatcher_popup" 
        style="position:absolute; top: -1000px; left: -1000px; visibility:hidden;">
      A podcatcher is a computer program used to automatically download
      podcasts.  iTunes is a popular podcatcher, but is not the only
      software available for this purpose.
    </div>
</f:view>  