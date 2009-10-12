var panelId;
function setPanelId(thisid)
{
  panelId = thisid;
}


function showHideDivBlock(hideDivisionNo, context)
{
  var tmpdiv = hideDivisionNo + "__hide_division_";
  var tmpimg = hideDivisionNo + "__img_hide_division_";
  var divisionNo = getTheElement(tmpdiv);
  var imgNo = getTheElement(tmpimg);
  if(divisionNo)
 
  {
    if(divisionNo.style.display =="block")
    {
      divisionNo.style.display="none";
      if (imgNo)
      {
        imgNo.src = context + "/images/right_arrow.gif";
       }
    }
    else
    {
      divisionNo.style.display="block";
      if(imgNo)
      {
        imgNo.src = context + "/images/down_arrow.gif";
      }
    }
    if(panelId != null)
    {
      resizeFrame('grow');
    }
  }
}


//this function needs jquery 1.1.2 or later - it resizes the parent iframe without bringing the scroll to the top
	function resizeFrame(updown)
	{
		if (top.location != self.location) 	 {
			var frame = parent.document.getElementById(window.name);
		}
			if( frame )
		{
			if(updown=='shrink')
			{
				var clientH = document.body.clientHeight - 30;
			}
			else
			{
				var clientH = document.body.clientHeight + 30;
			}
			$( frame ).height( clientH );
		}
		else
		{
			throw( "resizeFrame did not get the frame (using name=" + window.name + ")" );
		}
	}


/*

function showHideDivBlock(hideDivisionNo, context){

  var tmpdiv = hideDivisionNo + "__hide_division_";
  var tmpimg = hideDivisionNo + "__img_hide_division_";
  var divisionNo = getTheElement(tmpdiv);
  var imgNo = getTheElement(tmpimg);


	if (divisionNo.style.display == "block") {
		imgNo.src = context + "/images/right_arrow.gif";
	}
	else {
		imgNo.src = context + "/images/down_arrow.gif";
	}
}

*/
function showHideDiv(hideDivisionNo, context)
{
  var tmpdiv = hideDivisionNo + "__hide_division_";
  var tmpimg = hideDivisionNo + "__img_hide_division_";
  var divisionNo = getTheElement(tmpdiv);
  var imgNo = getTheElement(tmpimg);

  if(divisionNo)
  {
    if(divisionNo.style.display =="block" || divisionNo.style.display =="table-row")
    {
      divisionNo.style.display="none";
      if (imgNo)
      {
        imgNo.src = context + "/images/collapse.gif";
      }
    }
    else
    {
      if(navigator.product == "Gecko")
      {
        divisionNo.style.display="table-row";
      }
      else
      {
        divisionNo.style.display="block";
      }
      if(imgNo)
      {
        imgNo.src = context + "/images/expand.gif";
      }
    }
  }
}

function getTheElement(thisid)
{

  var thiselm = null;

  if (document.getElementById)
  {
    thiselm = document.getElementById(thisid);
  }
  else if (document.all)
  {
    thiselm = document.all[thisid];
  }
  else if (document.layers)
  {
    thiselm = document.layers[thisid];
  }

  if(thiselm)   
  {
    if(thiselm == null)
    {
      return;
    }
    else
    {
      return thiselm;
    }
  }
}

function check(field)
 {
    for (i = 0; i < field.length; i++) 
    {
        field[i].checked = true;
    }
 }
function unCheck(field)
{
    for (i = 0; i < field.length; i++) 
    {
        field[i].checked = false; 
    }
}

function toggleDisplay(obj) {
	resize();
	$("#" + obj).slideToggle("normal", resize);
	return;    
}


jQuery.fn.fadeToggle = function(speed, easing, callback) {
   return this.animate({opacity: 'toggle'}, speed, easing, callback);

}; 
function toggleDisplayInline(obj) {
//	resize();
//		$("#" + obj).slideToggle("normal", resize);
		$("#" + obj).fadeToggle();

	return;
}

function toggleHide(obj){
	if(obj.innerHTML.match(/hide/i)){
		obj.innerHTML = obj.innerHTML.replace('Hide ', '');
	} else {
		obj.innerHTML = obj.innerHTML.replace(/(<.+>)([^<>]+)/i, "$1 Hide $2");
	}
}

function getScrollDist(obj){
	var curtop = 0;
	if (obj.offsetParent) {
		curtop = obj.offsetTop
		while (obj = obj.offsetParent) {
			curtop += obj.offsetTop
		}
	}
	return curtop;
}
function selectDeselectCheckboxes(mainCheckboxId, myForm) {   
	var el = getTheElement(mainCheckboxId);
	var isChecked = el.checked;           
	for ( i = 0; i < myForm.elements.length; i++ ) {
		if (myForm.elements[i].type == 'checkbox' ) {
			myForm.elements[i].checked  = isChecked;                                               
		}
	}
}
function resetMainCheckbox(checkboxId) {
  mainCheckboxEl = getTheElement(checkboxId);
  if (mainCheckboxEl.checked = true) {
  	mainCheckboxEl.checked = false;
  }                                                  
}
// if the containing frame is small, then offsetHeight is pretty good for all but ie/xp.
// ie/xp reports clientHeight == offsetHeight, but has a good scrollHeight
function mySetMainFrameHeight(id)
{
	// run the script only if this window's name matches the id parameter
	// this tells us that the iframe in parent by the name of 'id' is the one who spawned us
	if (typeof window.name != "undefined" && id != window.name) return;

	var frame = parent.document.getElementById(id);
	if (frame)
	{

		var objToResize = (frame.style) ? frame.style : frame;

		var height; 
		
		var scrollH = document.body.scrollHeight;
		var offsetH = document.body.offsetHeight;
		var clientH = document.body.clientHeight;
		var innerDocScrollH = null;

		if (typeof(frame.contentDocument) != 'undefined' || typeof(frame.contentWindow) != 'undefined')
		{
			// very special way to get the height from IE on Windows!
			// note that the above special way of testing for undefined variables is necessary for older browsers
			// (IE 5.5 Mac) to not choke on the undefined variables.
 			var innerDoc = (frame.contentDocument) ? frame.contentDocument : frame.contentWindow.document;
			innerDocScrollH = (innerDoc != null) ? innerDoc.body.scrollHeight : null;
		}

		if (document.all && innerDocScrollH != null)
		{
			// IE on Windows only
			height = innerDocScrollH;
		}
		else
		{
			// every other browser!
			height = offsetH;
		}

		// here we fudge to get a little bigger
		//gsilver: changing this from 50 to 10, and adding extra bottom padding to the portletBody		
		var newHeight = height + 150;
		//contributed patch from hedrick@rutgers.edu (for very long documents)
		if (newHeight > 32760)
		newHeight = 32760;

		// no need to be smaller than...
		//if (height < 200) height = 200;
		objToResize.height=newHeight + "px";
	
		var s = " scrollH: " + scrollH + " offsetH: " + offsetH + " clientH: " + clientH + " innerDocScrollH: " + innerDocScrollH + " Read height: " + height + " Set height to: " + newHeight;

	}
}


function setupMessageNav(messageType){
  	if ($("." + messageType).size() >= 1) {
		if (messageType =="messageNew"){
			tofirst=$("#firstNewItemTitleHolder").text();
			tonext=$("#nextNewItemTitleHolder").text();
			last=$("#lastNewItemTitleHolder").text();
		}
		else{
			tofirst=$("#firstPendingItemTitleHolder").text();
			tonext=$("#nextPendingItemTitleHolder").text();
			last=$("#lastPendingItemTitleHolder").text();
		}
		$('#messNavHolder').append("<span class='jumpToNew specialLink'><a href='#" + messageType + "newMess0'>" + tofirst + "</a></span>");
  		$("." + messageType).each(function(intIndex){
  			$(this).after("<a name='" + messageType + "newMess" + intIndex + "'></a>");
  			if (intIndex !== ($("." + messageType).size() - 1)) {
  				$(this).css({
  					cursor: "pointer"
  				});
					$(this).attr("title",tonext);
  					$(this).click(function(){
  					document.location = "#" + messageType + "newMess" + (intIndex + 1);
  				});
  			}
				else{
					$(this).attr("title",last);
				}
  		});
  	}
  	if ($(".messageNew").size() < 1 && $(".messagePending").size() < 1) {
		$('#messNavHolder').remove()
	}
  }


function doAjax(messageId, topicId, self){
 	$(self).attr('src', '/library/image/sakai/spinner.gif');
	$.ajax({ type: "GET", url: document.forms[0].action , data: "ajax=true&action=markMessageAsRead&messageId=" + messageId + "&topicId=" + topicId,
      success: function(msg){
         if(msg.match(/SUCCESS/)){
     		setTimeout(function(){
							$(self).parents("tr").children("td").children("span").children("span.messageNew").hide();
							$(self).parents("div").parents("div").children("span.messageNew").hide();
              $(self).remove();
               $("#" + messageId).parents("tr:first").children("td").each(function(){this.innerHTML = this.innerHTML.replace(/unreadMsg/g, 'bogus'); });
            }, 500);
         } else {
            $(self).remove();
            $("#" + messageId).parents("tr:first").css("backgroundColor", "#ffD0DC");         
         }
      },
      error: function(){
         $(self).remove();
         $("#" + messageId).parents("tr:first").css("backgroundColor", "#ffD0DC");
      }
   });
	//$.ajax({type: "GET", url: location.href, data: ""});
	return false;
}

// This will display/hide extended description for an element
// Need to be fancy due to a bug (SAK-11933) where copy/pasting
// content causes addition tags with class 'toggle' to be in
// markup, causing errors on display.
// Found multiple versions of this fix, so centralizing it here
function toggleExtendedDescription(hideShowEl, parent, element) {
    resize();
    hideShowEl.toggle();
    parent.slideToggle(resize);
    element.toggle();
}

//fix for double click stack traces in IE - SAK-10625
//add Jquery if necessary
/*if(typeof($) == "undefined"){
   js = document.createElement('script');
   js.setAttribute('language', 'javascript');
   js.setAttribute('type', 'text/javascript');
   js.setAttribute('src','/library/js/jquery.js');
   document.getElementsByTagName('head').item(0).appendChild(js);
//document.write('<script type="text/javascript" src="/library/js/jquery.js"></script>');}
}
js = document.createElement('script');
js.setAttribute('language', 'javascript');
js.setAttribute('type', 'text/javascript');
js.setAttribute('src','/sakai-messageforums-tool/js/sak-10625.js');
document.getElementsByTagName('head').item(0).appendChild(js);*/
