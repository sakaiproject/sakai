var el1, el2, els = null;
adjustScrolls = function(){
   el1.style.left = -els.scrollLeft + 'px';
   el2.style.top = -els.scrollTop + 'px';
}

function gethandles(){
   q2_div_ul = $("#q2 div ul");
   q4s = $("#q4");
   q3_div = $("#q3 div");
   q3_div_table = $("#q3 div table");
   q1_width = $("#q1").width();
   q2_ul_li = $("#q2 div ul li");
   q3_top_row = $("#q3 tr:first td");
   q4_top_row = $("#q4 tr:first td");
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
   
   // #q2 contains the "data" headers that contain the assignment titles
   // the structure is #q2 div:div:ul:li.  #q4 is the table that contains the grade info
   
   total = 0; count = 0;
   $(q2_ul_li).each(function(c){
	   var q2_heading_width = $(this).width() + paddingRight *2;
	   // make sure all of the assignment name headers have a minimum width
	   if(q2_heading_width < 50) {
		   q2_heading_width = 45;
	   }

	   q4_tr_td = $("#q4 tr:first td:eq(" + c + ")");
	   q4_data_width = $(q4_tr_td).width() + 20;
	   new_width = (q4_data_width < q2_heading_width ? q2_heading_width : q4_data_width);
	   $(q4_tr_td).width(new_width);
	   $(this).width(new_width);       

	   total += new_width + paddingRight * 2; count=c+1;
   });
   
   // now we need to set the width of the header section and the associated
   // table containing the grade data
   total += count * 2;
   q4_table = $("#q4 table")
   q4_table_width = total;
   $(q4_table).width(total);
   $(q2_div_ul).width(total);
   
   // this makes sure the height of the data cells matches up for all rows
   $("#q3 tr").each(function(i){
      thisHeight = $(this).height();
      thatHeight = $("#q4 tr:eq(" + i + ")").height();
      if(thisHeight > thatHeight){
         $("#q4 tr:eq(" + i + ")").height(thisHeight);
      } else {
         $(this).css("height",thatHeight + "px");
      }
   });
   
   //check if we need scrollbars - SAK-9969
   
   q4_div = $("#q4 div");

   var q4s_width = $(q4s).width();
   if (q4s_width > q4_table_width) {
      var mainwrap = $("#mainwrap");
      maxwidth = $(mainwrap).width() - (q4s_width - q4_table_width);
        if(maxwidth < $("body").width() - 2) 
      $(mainwrap).css("max-width", maxwidth);
   }
   
   //end check if we need scrollbars - SAK-9969
   el1 = $(q2_div_ul).get(0);
   el2 = $("#q3 div table").get(0);
   els = $(q4_div).get(0);
   $(q4_div).scroll(adjustScrolls);
}
$(document).ready(gethandles);
