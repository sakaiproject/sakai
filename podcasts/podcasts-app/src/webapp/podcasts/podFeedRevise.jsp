  <%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
  <%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
  <%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
  <% response.setContentType("text/html; charset=UTF-8"); %>

<jsp:useBean id="msgs" class="org.sakaiproject.util.ResourceLoader" scope="session">
	<jsp:setProperty name="msgs" property="baseName" value="org.sakaiproject.api.podcasts.bundle.Messages"/>
</jsp:useBean>

  <f:view>
    <sakai:view toolCssHref="css/podcaster.css">
    <h:form id="podFeedRev" >

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

    <table class="indnt1 nolines">
      <tr> <!--  ****** Feed Address (non-editable) ****** -->
        <td colspan=2><h:outputText value="#{msgs.podfeed_revise_url_caption}" /></td>
        <td><b><h:outputText id="feedURL" styleClass="feedUrl" value="#{podHomeBean.URL}" /></b></td>
      </tr>
      <tr>  <!-- ****** Feed Title ****** -->
        <td class="reqStarInline">*</td>
        <td><h:outputText value="#{msgs.title_prompt}" /></td>
 	    <td><h:inputText id="podtitle" styleClass="podTitle" value="#{podfeedBean.podfeedTitle}" size="35" maxlength="255" /></td>
 	  </tr>
	  <tr>
  	    <td colspan="3"><h:messages styleClass="alertMessage" id="errorMessages" rendered="#{! empty facesContext.maximumSeverity}" /> </td>
      </tr>
 <%--     <tr>
        <td colspan="2"><h:outputText value="Copyright" /></td>
        <td><h:inputText id="podcopyright" value="#{podfeedBean.feedCopyright}" size="35" maxlength="255" /></td>
      </tr>
      <tr>
        <td colspan="2"><h:outputText value="Generator" /></td>
        <td><h:inputText id="podGenerator" value="#{podfeedBean.feedGenerator}" size="35" maxlength="255" /></td>
      </tr> --%>
      <tr>
        <td colspan="2"><h:outputText value="#{msgs.description_prompt}" /></td>
      </tr>
      <tr>
        <td colspan="3"><h:inputTextarea id="desc" value="#{podfeedBean.podfeedDescription}" rows="6" cols="80" /></td>
      </tr>
    </table>
    <br />

    <sakai:button_bar>  <!-- Save Changes and Cancel buttons -->
      <sakai:button_bar_item action="#{podfeedBean.processRevisePodcast}" value="#{msgs.change_submit}" 
          accesskey="s" title="#{msgs.change_submit}" styleClass="active" />
      <sakai:button_bar_item action="#{podfeedBean.processCancelPodfeedRevise}" value="#{msgs.cancel}" 
          accesskey="c" title="#{msgs.cancel}" />
    </sakai:button_bar>

    </h:form>
    </sakai:view>
	</f:view>
</body>
</html>
