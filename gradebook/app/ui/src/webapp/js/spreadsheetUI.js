		var el1, el2, els = null;
      function findMyPos(obj) {if(obj){
      	var curleft = curtop = 0;
      	if (obj.offsetParent) {
      		curleft = obj.offsetLeft
      		curtop = obj.offsetTop
      		while (obj = obj.offsetParent) {
      			curleft += obj.offsetLeft
      			curtop += obj.offsetTop
      		}
      	}
         return [curleft,curtop];
      }}

      adjustScrolls = function(){
         el1.style.left = -els.scrollLeft + 'px';
         el2.style.top = -els.scrollTop + 'px';
      }

      var x, y, w, h1, h2;
      initResizerCoords = function(){
         y = parseInt($('div.handle').css('top'));
         h1 = $('div#q3 div').height();
         h2 = $('div#q4 div').height();
      }

      adjustResizer = function(newX, newY){
         newY = parseInt(newY);
         $('div#q3 div').height(h1 - (y - newY))
         $('div#q4 div').height(h2 - (y - newY))
      }

      function gethandles(){
         $("div#q3 div").width($("ul#q1").width());
         $("ul#q1 li").each(function(i){
            this_width = $(this).width() + parseInt($(this).css("padding-right")) *2;
            if($("div#q3 tr:first td:eq(" + i + ")").width() < this_width){
               $("div#q3 tr:first td:eq(" + i + ")").width(this_width - parseInt($("div#q3 tr:first td:eq(" + i + ")").css("padding-right")) * 2);
            }else{
               $(this).width($("div#q3 tr:first td:eq(" + i + ")").width() - parseInt($(this).css("padding-right")) - parseInt($(this).css("padding-left")) - 2);
            }
         });
         $("div#q3 div").width($("ul#q1").width());
         total = 0; count = 0;
         $("div#q2 div ul li").each(function(c){
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
    

		 if($('div.handle').get(0)){
         $('div.handle').Draggable({
            containment: [findMyPos($('div.handle').get(0))[0], 
                           findMyPos($('div.handle').get(0))[1], 
                           0, 
                           $('div#q4 div table').height() - parseInt($('div.handle').css('top')) + 15],
            onStart: initResizerCoords,
            onDrag: adjustResizer,
            axis: 'vertically' });
          }
      }

      
	$(document).ready(gethandles);
      