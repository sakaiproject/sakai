<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
<%
    response.setContentType("text/html; charset=UTF-8");
    response.addDateHeader("Expires", System.currentTimeMillis() - (1000L * 60L * 60L * 24L * 365L));
    response.addDateHeader("Last-Modified", System.currentTimeMillis());
    response.addHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0, post-check=0, pre-check=0");
    response.addHeader("Pragma", "no-cache");
%>
<f:view>


<script type="text/javascript" language="JavaScript">
doubleDeep = true;
</script>
<script type="text/javascript" language="JavaScript">
	focus_path = ["${form-message}"];
	

function formSubmitOnEnterJSF(field, event)
{
	var keycode;
	if (window.event)
	{
		keycode = window.event.keyCode;
	}
	else
	{
		keycode = event.which ? event.which : event.keyCode
	}

	if (keycode == 13)
	{
		var actionInjection = document.getElementById("actionInjection");
		actionInjection.name = "mainForm:submit";
		field.form.submit();
		return false;
	}
	
	return true;
}
	
</script>
<script type="text/javascript" language="JavaScript">
try { parent.updateNow(); } catch (error) {}
</script>
<script type="text/javascript" language="JavaScript" src="/library/js/headscripts.js"></script>
   <h:form id="mainForm">
	  <input type="hidden" id="actionInjection" name="_idOfAction" value="some value" />
      <label for="message"></label>
      <h:inputTextarea id="message" value="#{ChatTool.newMessageText}" rows="3" cols="60" onkeypress="formSubmitOnEnterJSF(this, event)" />
      <sakai:button_bar>
          <sakai:button_bar_item id="submit"
              action="#{ChatTool.processActionSubmitMessage}"
              value="#{msgs.control_post}" />
          <sakai:button_bar_item id="reset"
              action="#{ChatTool.processActionResetMessage}"
              value="#{msgs.cancel}" />
      </sakai:button_bar>
   </h:form>

</f:view>