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
      }
    );
    $("#eventLogId\\:assessmentTitle option").each(function() {
      if ($(this).text().indexOf(deletedText) > -1){
        $(this).addClass('eventLogDeleted');
      }
    });
    var element = document.getElementById("eventLogId:filteredUser");
    if (element) {
      element.placeholder = searchHint;
    }
  }
);
