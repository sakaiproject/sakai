 <!-- JavaScript and ToolTextTip for when hovering over podcatcher in directions -->
<!--

//-----------------------------------------------------------------------
// called by showPopupHere to determine the absolute position of the element
//
// el - The element whose position needs to be determined
//
// r - The top,left coordinates of this element which is returned
//-----------------------------------------------------------------------
function getAbsolutePos(el) {
  var SL = 0, ST = 0;
  var is_div = /^div$/i.test(el.tagName);
  if (is_div && el.scrollLeft)
    SL = el.scrollLeft;
    if (is_div && el.scrollTop)
      ST = el.scrollTop;
    var r = { x: el.offsetLeft - SL, y: el.offsetTop - ST };
    if (el.offsetParent) {
      var tmp = getAbsolutePos(el.offsetParent);
      r.x += tmp.x;
      r.y += tmp.y;
    }
    return r;
}

//-----------------------------------------------------------------------
// starting point for displaying a tooltip
//   This is done by resetting the top and left values of the div tag
//   that contains the text to be displayed to just below where the
//   element is
//
// el - The element being hovered over/clicked which needs a tooltip
// divid - The id of the div tag that contains the text for the tooltip
//-----------------------------------------------------------------------
function showPopupHere(el,divid) {
  var targetdiv;
//  var podcast_info;
  if ( document.all ) {
    targetdiv = document.all[divid];
//    podcast_info = document.all["podcast_info"];
  } else {
	targetdiv = document.getElementById(divid);
//	podcast_info = document.getElementById("podcast_info");
  }
		
  if ( targetdiv != null ) {
    var pos = getAbsolutePos(el);
    var width =  el.offsetWidth;
    var height =  el.offsetHeight;

    // Need to adjust position given
    if (document.all) { 
    
       // For IE, need to adjust both
       pos.y -= (height * 2.7);
       pos.x -= (width * .3);

    }
    else {
       // For non-IE
       //    just adjust the height by offsetHeight of element */
       pos.y += height;
 
    }
    
    targetdiv.style.top = pos.y+"px ";
    targetdiv.style.left = pos.x+"px ";
    targetdiv.style.bgolor = "#cccccc";		    
//    targetdiv.style.visibility = "visible";
    
//    podcast_info.style.visibility = "visible";
  } else {
    alert('problem: ' + targetdiv.innerHTML);
  }
}

//-----------------------------------------------------------------------
// This removes the popup from the screen
//    This is done simply by setting the elements visibility to hidden
//
// divid - The id of the popup that needs to be removed
//-----------------------------------------------------------------------
function hidePopup(divid) {
  var targetdiv;
  var podcast_info;
  if ( document.all ) {
      targetdiv = document.all[divid];
  } else {
    targetdiv = document.getElementById(divid);
  }    

  if ( targetdiv != null ) {
    targetdiv.style.top = "-1000px ";
    targetdiv.style.left = "-1000px ";
  }
  
}
-->