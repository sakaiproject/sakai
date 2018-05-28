// ONC-4633
// Handles the hover event for the small "i" on the event log
$(document).ready(
  function()
  {
    $("span.info").hover(
    function()
    {
      $(this).find("span.makeLogInfo").show();
    },
    function()
    {
      $(this).find("span.makeLogInfo").hide();
    });
    $("span.deleted").each(function(){
    	var tr = this.closest('tr');
    	$(tr).css('color','#AAA');
    });
    $("#eventLogId\\:assessmentTitle option").each(function(){
    	if(typeof this.innerHTML.contains !== "undefined" && this.innerHTML.contains('-deleted')){
    		this.style="color:#AAA";
    	}
    });
  }
);

// Initialize the search box with some text and style
function initHelpValue(helpText, id)
{
    var element=document.getElementById(id);
    if(element.value=="" || element.value==helpText)
    {
        element.value=helpText;
        element.className="prePopulateText";
    }
}

// Clear the help text from the search box
function resetHelpValue(helpText, id)
{
    var element=document.getElementById(id);
    if(element.value==helpText)
    {
        element.value="";
        element.className="";
    }
}
