//fix for double click stack traces in IE - SAK-10625
var click=0;
sak10625_disabler = function(){
  var existing_event = this.onclick;
  $("a, input[type=button], input[type=submit]").attr("onclick", "");
  $(this).addClass("disable_a_href"); 
  this.onclick = null;
  if(existing_event) { 
    $(this).click(existing_event);
  }
}
if (typeof window.jQuery != "undefined") {


$(document).ready(function(){
	$("a").filter(function(){regexp=/submit\(\)/; return regexp.test($(this).attr("onclick"));}).bind("click", sak10625_disabler);
	 $("input[type=button], input[type=submit]").bind("click", sak10625_disabler);
});
    }
