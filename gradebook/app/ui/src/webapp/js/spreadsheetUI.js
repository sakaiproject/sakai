var el1, el2, els = null;
adjustScrolls = function(){
   el1.style.left = -els.scrollLeft + 'px';
   el2.style.top = -els.scrollTop + 'px';
}

function gethandles(){
   if($("div#q3 div").css("overflow") == "auto"){
      $("div#mainwrap").css("width", "80%");
      $("ul#q1").width($("div#mainwrap").width() - 15);
      $("ul#q1 li").css("width", ($("ul#q1").width() / $("ul#q1 li").size()- parseInt($("ul#q1 li").css("padding-right")) - parseInt($("ul#q1 li").css("padding-left")) - 2) + "px");
   }
   $("div#q3 div").width($("ul#q1").width());
   $("ul#q1 li").each(function(i){
      this_width = $(this).width() + parseInt($(this).css("padding-right")) *2;
      if($("div#q3 tr:first td:eq(" + i + ")").width() < this_width){
         $("div#q3 tr:first td:eq(" + i + ")").width(this_width - parseInt($("div#q3 tr:first td:eq(" + i + ")").css("padding-right")) * 2);
      }else{
         $(this).width($("div#q3 tr:first td:eq(" + i + ")").width() - parseInt($(this).css("padding-right")) - parseInt($(this).css("padding-left")) - 2);
      }
   });
   $("div#q3 div").width($("ul#q1").width() + ($("div#q3 div").css("overflow") == "auto" ? 15 : 0));
   total = 0; count = 0;
   $("div#q2 div ul li").each(function(c){
   	  if($(this).width() < 50) $(this).css("width", "45px"); 
      total += $(this).width() + parseInt($(this).css("padding-right")) * 2; count=c+1;
   });
   total += count * 2;     
   if($("div#q4 table").width() > total){
      $("div#q2 ul").width($("div#q4 table").width());
   }else{
      $("div#q4 table").width(total);
   }

   $("div#q2 ul li").each(function(i){
      $("div#q4 tr:first td:eq(" + i + ")").width($(this).width() - parseInt($("div#q4 tr:first td:eq(" + i + ")").css("padding-right")) * 2
         + parseInt($(this).css("padding-right")) * 2);
   });   

   el1 = $("div#q2 div ul").get(0);
   el2 = $("div#q3 div table").get(0);
   els = $("div#q4 div").get(0);
   $("div#q4 div").scroll(adjustScrolls);
}
$(document).ready(gethandles);