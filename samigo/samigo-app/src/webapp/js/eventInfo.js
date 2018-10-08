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
    $("#eventLogId\\:assessmentTitle option").each(function() {
      if ($(this).text().indexOf(deletedText) > -1){
        $(this).addClass('eventLogDeleted');
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
