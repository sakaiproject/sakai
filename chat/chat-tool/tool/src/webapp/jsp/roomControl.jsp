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
	
function formSubmit(field)
{		
		if(navigator.userAgent.indexOf("Safari")>0){ 
		var actionInjection = document.getElementById("actionInjection");		 
		actionInjection.name = field.name;        
		field.form.submit();	
		return false;
		}
		return false;	
}
	
</script>
<script type="text/javascript" language="JavaScript">
try { parent.updateNow(); } catch (error) {}
</script>
<script type="text/javascript" language="JavaScript" src="/library/js/headscripts.js"></script>

	<sakai:script contextBase="/library" path="/js/jquery.js" />
	<sakai:script contextBase="/sakai-chat-tool" path="/js/chatscript.js"/>

   <h:form id="mainForm">
	  <input type="hidden" id="actionInjection" name="_idOfAction" value="some value" />
      <h:inputHidden id="chatidhidden" value="#{ChatTool.currentChatChannelId}" />
      <h:outputLabel for="message"	value="#{msgs['control.lab']}" />    
      <br>      
      <h:inputTextarea id="message" value="#{ChatTool.newMessageText}" rows="3" cols="60" onkeypress="formSubmitOnEnterJSF(this, event)" />
      <sakai:button_bar>
          <sakai:button_bar_item id="submit"
              action="#{ChatTool.processActionSubmitMessage}"
              onclick="formSubmit(this)"
              value="#{msgs['control.post']}"
              styleClass="active" />
          <sakai:button_bar_item id="reset"
              action="#{ChatTool.processActionResetMessage}"
              onclick="formSubmit(this)"
              value="#{msgs['control.clear']}" />
      </sakai:button_bar>
   </h:form>

	<script type="text/javascript" language="JavaScript">
		setMainFrameHeight('<h:outputText value="#{ChatTool.framePlacementId}" />');
	</script> 
   
</sakai:view>
</f:view>