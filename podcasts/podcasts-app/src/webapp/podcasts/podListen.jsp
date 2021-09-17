<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf2/sakai" prefix="sakai" %>
<% response.setContentType("text/html; charset=UTF-8"); %>

<jsp:useBean id="msgs" class="org.sakaiproject.util.ResourceLoader" scope="session">
	<jsp:setProperty name="msgs" property="baseName" value="org.sakaiproject.api.podcasts.bundle.Messages"/>
</jsp:useBean>

<f:view>
    <link href="/library/skin/tool_base.css" type="text/css" rel="stylesheet" media="all" />
    <link href="/library/skin/default/tool.css" type="text/css" rel="stylesheet" media="all" />
  <sakai:view toolCssHref="./css/podcaster.css">
    <script>includeWebjarLibrary('wavesurfer.js');</script>
  <h:form id="podListen" enctype="multipart/form-data">
    <%@ include file="/podcasts/podcastMenu.jsp" %>
    <div>  <!-- Page title and Instructions -->
      <div class="page-header">
        <h1><h:outputText value="#{msgs.listen_title}" /></h1>
      </div>
      <div class="indnt1">
          <p class="instruction"> 
            <h:outputText value="#{msgs.listen_instructions}" />
          </p>
      </div>
    </div>

    <div id="podcast-audio-player"></div>
    <div class="controls">
      <button class="btn btn-primary" onclick="playPause(event);">
        <i class="glyphicon glyphicon-play"></i><h:outputText value="#{msgs.play}" />/<i class="glyphicon glyphicon-pause"></i><h:outputText value="#{msgs.pause}" />
      </button>
    </div>

    <sakai:button_bar>  <!-- Back -->
      <h:commandButton action="#{podHomeBean.processBackListen}" value="#{msgs.back}" accesskey="x" title="#{msgs.back}" />
    </sakai:button_bar>
   </h:form>
     <script>
       (function() {

         const wavesurfer = WaveSurfer.create({
           container: '#podcast-audio-player'
         });

         wavesurfer.load('<h:outputText value="#{podHomeBean.selectedPodcast.fileURL}" />');

         playPause = (e) => {
           e.preventDefault();
           wavesurfer.playPause();
         }

       })();
</script>
 </sakai:view>
</f:view>
