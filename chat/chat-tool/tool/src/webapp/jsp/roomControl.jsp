<f:view>
<sakai:view title="">

<script type="text/javascript" language="JavaScript">
doubleDeep = true;
</script>
<script type="text/javascript" language="JavaScript">
	focus_path = ["mainForm:message"];
	

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
              value="#{msgs['control.post']}" />
          <sakai:button_bar_item id="reset"
              action="#{ChatTool.processActionResetMessage}"
              value="#{msgs['control.clear']}" />
      </sakai:button_bar>
   </h:form>
</sakai:view>
</f:view>