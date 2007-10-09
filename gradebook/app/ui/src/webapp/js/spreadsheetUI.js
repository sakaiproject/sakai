var el1, el2, els = null;
adjustScrolls = function(){
   el1.style.left = -els.scrollLeft + 'px';
   el2.style.top = -els.scrollTop + 'px';
}

function gethandles(){
   ie = $.browser.msie;
   if ( $.browser.msie && $.browser.version < 7){ $("div#q2 div ul").css("position", "absolute"); $("div#q4").css("left", "-3px"); }
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

   $("div#q3 tr").each(function(i){
      if($(this).height() > $("div#q4 tr:eq(" + i + ")").height()){
         ie ? $("div#q4 tr:eq(" + i + ")").height($(this).height() - 12) 
            : $("div#q4 tr:eq(" + i + ")").height($(this).height());
      } else {
         ie ? $(this).css("height", $("div#q4 tr:eq(" + i + ")").height() - 12 + "px") 
            :$(this).css("height", $("div#q4 tr:eq(" + i + ")").height() + "px");
      }
   });
   //check if we need scrollbars - SAK-9969
   maxwidth = $("div#mainwrap").width() - ($("div#q4").width() - $("div#q4 table").width()) + 15 + (ie?2:0);
   if(maxwidth < $("body").width() - 2) $("div#mainwrap").css("max-width", maxwidth);
   if($("div#q4 div table").height() < $("div#q4").height()){
      $("div#q3").height($("div#q3").height() - ($("div#q4").height() - $("div#q4 table").height()) + 15 + (ie?2:0));
      $("div#q4 div").height($("div#q4 div").height() - ($("div#q4").height() - $("div#q4 table").height()) + 15 + (ie?2:0));
   }
   //end check if we need scrollbars - SAK-9969
   el1 = $("div#q2 div ul").get(0);
   el2 = $("div#q3 div table").get(0);
   els = $("div#q4 div").get(0);
   $("div#q4 div").scroll(adjustScrolls);
}
$(document).ready(gethandles);
