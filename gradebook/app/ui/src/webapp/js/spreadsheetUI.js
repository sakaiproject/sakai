var el1, el2, els = null;
adjustScrolls = function(){
   el1.style.left = -els.scrollLeft + 'px';
   el2.style.top = -els.scrollTop + 'px';
}

function gethandles(){
   ie = $.browser.msie;
   q2_div_ul = $("#q2 div ul");
   q4s = $("#q4");
   q3_div = $("#q3 div");
   q3_div_table = $("#q3 div table");
   q1_width = $("#q1").width();
   q2_ul_li = $("#q2 div ul li");
   q3_top_row = $("#q3 tr:first td");
   q4_top_row = $("#q4 tr:first td");
   if ( ie && $.browser.version < 7){ q2_div_ul.css("position", "absolute"); $(q4s).css("left", "-3px"); }
   $(q3_div).width(10000);
   $("#q1 div").width(10000);

   paddingRight = parseInt($("#q1 li:first").css("padding-right"));
   add = 0;
   $("#q1 li").each(function(i){
      this_width = $(this).width() + paddingRight *2;
      q3_tr_td = $(q3_top_row).get(i);
      match_width = $(q3_tr_td).width() + 10;
      new_width = (match_width < this_width ? this_width : match_width);
     $(q3_tr_td).width(new_width - paddingRight * 2);
     $(this).width(new_width - paddingRight * 2 - 2);
   });
   q1_width = $("#q1").width();
   $(q3_div).width(q1_width);
   
   total = 0; count = 0;
   
   $(q2_ul_li).each(function(c){
      var q2_heading_width = $(this).width();
      if(q2_heading_width < 50) {
        $(this).css("width", "45px"); 
        q2_heading_width = 45;
      }

      total += q2_heading_width + paddingRight * 2; count=c+1;
      
      q4_tr_td = $(q4_top_row).get(c);
      $(q4_tr_td).width(q2_heading_width);
   });
   
   total += count * 2;
   q4_table = $("#q4 table")
   var q4_table_width = $(q4_table).width();
   if(q4_table_width > total){
      $(q2_div_ul).width(q4_table_width);
   }else{
      q4_table_width = total;
      $(q4_table).width(q4_table_width);
   }   
   
   // this takes a lot of processing and doesn't seem to change anything
   /*$("#q3 tr").each(function(i){
      thisHeight = $(this).height();
      thatHeight = $("#q4 tr:eq(" + i + ")").height();
      if(thisHeight > thatHeight){
         ie ? $("#q4 tr:eq(" + i + ")").height(thisHeight - 12) 
            : $("#q4 tr:eq(" + i + ")").height(thisHeight);
      } else {
         ie ? $(this).css("height", thatHeight - 12 + "px") 
            :$(this).css("height",thatHeight + "px");
      }
   });*/
   
   //check if we need scrollbars - SAK-9969
   
   q4_div = $("#q4 div");

   var q4s_width = $(q4s).width();
   if (q4s_width > q4_table_width) {
      var mainwrap = $("#mainwrap");
      maxwidth = $(mainwrap).width() - (q4s_width - q4_table_width) + 15 + (ie?2:0);
        if(maxwidth < $("body").width() - 2) 
      $(mainwrap).css("max-width", maxwidth);
   }
   
   var q4s_height = $(q4s).height();
   var q4_table_height = $(q4_table).height();
   if(q4_table_height < q4s_height){  
      q3s = $("#q3");   
      $(q3s).height($(q3s).height() - (q4s_height - q4_table_height) + 15 + (ie?2:0));
      $(q4_div).height($(q4_div).height() - (q4s_height - q4_table_height) + 15 + (ie?2:0));
   }
   //end check if we need scrollbars - SAK-9969
   el1 = $(q2_div_ul).get(0);
   el2 = $("#q3 div table").get(0);
   els = $(q4_div).get(0);
   $(q4_div).scroll(adjustScrolls);
}
$(document).ready(gethandles);
