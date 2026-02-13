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
				var clientH = document.body.clientHeight + 90;
			}
			$( frame ).height( clientH );
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

var instrumentThreads = function(target){
    var threadCount = 0;
    $('#' + target).children('tbody').children('tr').each(function(index){
        //remove "New messages" message if this is the only message in the thread
        if (($(this).prev('.hierItemBlock').size() === 1) & ($(this).prop('class') !== undefined)) {
            $(this).prev().find('span.childrenNewThread').remove();
        }
        if (($(this).next('.hierItemBlock').size() === 1) & ($(this).prop('class') !== undefined)) {
            $(this).find('span.childrenNewThread').remove();
        }
        if ($(this).next().size() === 0) {
            $(this).find('span.childrenNewThread').remove();
        }
        
        //add same class to all messg. in a thread so we can remove the "New messages" message in
        //thread seed after all of the messgs in the thread have been marked as read (in doAjax)
        if ($(this).prop('class') === "hierItemBlock") {
            threadCount = threadCount + 1;
        }
        $(this).addClass('thread' + threadCount)
    });
}

function setupMessageNav(messageType){
	$('.messagesThreaded tr').each(function(rowIndex){
		$(this).prop('rowCount',rowIndex)
		});
    if ($("." + messageType).size() >= 1) {
        if (messageType == "messageNew") {
            tofirst = $("#firstNewItemTitleHolder").text();
            tonext = $("#nextNewItemTitleHolder").text();
            last = $("#lastNewItemTitleHolder").text();
        }
        else {
            tofirst = $("#firstPendingItemTitleHolder").text();
            tonext = $("#nextPendingItemTitleHolder").text();
            last = $("#lastPendingItemTitleHolder").text();
        }
		//go to first new or pending message
		if ($('#messNavHolder').find('.jumpToNew[rel="setupMessageNav"]').length === 0) {
			$('#messNavHolder').append("<span class='jumpToNew specialLink' rel='setupMessageNav'><a class='button' href='#" + messageType + "newMess0'>" + tofirst + "</a></span>");
		}
        //instrument link targets (clicking on "New" goes to next one, same with "Pending")
		$("." + messageType).each(function(intIndex){
            var parentRow = $(this).parents('tr');
			var parentTable = $(this).parents('table');
			var totalTableRows =$(parentTable).find('tr').size()
            $(parentRow).addClass(messageType + 'Next');
            $(this).after("<a class=\"messageNewAnchor\" name='" + messageType + "newMess" + intIndex + "'> </a>");
            if (intIndex !== ($("." + messageType).size() - 1)) {
                $(this).css({
                    cursor: "pointer"
                });
                $(this).prop("title", tonext);
                $(this).click(function(){
                    //in message type is "New" find next new by crawling the DOM
                    // (real next one may have been marked as read, so no longer news)
                    if (messageType === 'messageNew') {
                        const thisIndex = parseInt($(parentRow).prop('rowCount')) + 1;
                        const targetElement = $(parentTable).find('tr').slice(thisIndex, totalTableRows).filter('.messageNewNext').eq(0);
                        if (targetElement.length) {
                            targetElement[0].scrollIntoView({ behavior: 'smooth' });$
                        }
                    }
                    // if "Pending" just link directly to next one
                    else {
                        // Scroll the target into view using jquery
                        const targetElement = $("a[name='" +  messageType + "newMess" + (intIndex + 1) + "']");
                        if (targetElement.length) {
                            targetElement[0].scrollIntoView({ behavior: 'smooth' });$
                        }
                    }
                });
                // Scroll the new message into view
                $('#messNavHolder a').click(function(e) {$
                    e.preventDefault();$
                    const targetPosPrep = $(this).attr('href').replace('#','');$
                    const targetElement = $("a[name='" + targetPosPrep + "']");$
                    if (targetElement.length) {$
                        targetElement[0].scrollIntoView({ behavior: 'smooth' });$
                    }$
                });$
            }
            else {
                $(this).prop("title", last);
            }
        });
    }
}

function doAjax(messageId, topicId, self){
    $(self).prop('src', '/library/image/sakai/spinner.gif');
    $.ajax({
        type: "GET",
        url: document.forms[0].action,
        data: "ajax=true&action=markMessageAsNotRead&messageId=" + messageId + "&topicId=" + topicId,
        success: function(msg){
            if (msg.match(/SUCCESS/)) {
                setTimeout(function(){
                    var thisRow = $(self).parents('tr');
                    $(thisRow).children("td").find("div.messageMetadata").children("span.textPanelFooter").addClass('unreadMsg');
                    //only do this if in subject only view
                    if ($(self).parent('td').size() === 1) {
                        var thisTheadClassArr = $(thisRow).prop('class').split(' ');
                        var thisThread = thisTheadClassArr[thisTheadClassArr.length - 1];
                        var unread = parseInt($('.hierItemBlock.' + thisThread + ' .childrenNewNumber').text(), 10) || 0;
                        if (unread > 0) {
                            $('.hierItemBlock.' + thisThread + ' .childrenNewNumber').text(unread + 1);
                        } else {
                            //change class
                            $('.hierItemBlock.' + thisThread + ' .childrenNewZero').removeClass('childrenZero childrenNewZero').addClass('childrenNew childrenNewNumber');
                            $('.hierItemBlock.' + thisThread + ' .childrenZero').removeClass('childrenZero').addClass('childrenNew');
                            $('.hierItemBlock.' + thisThread + ' .childrenNewNumber').text(unread + 1);
                        }
                        $('.' + thisThread).find('em').text($('.' + thisThread).find('em').text() + 1);
                        //hide "New Messages" in thread seed if all messages have been marked as "read"
                        if ($('.' + thisThread).find('span.messageNew').size() === 1) {
                            $('.' + thisThread).find('span.childrenNewThread').css('visibility', 'hidden');
                        }
						// replace this "New" flag if this message has been marked as not read
                        var thisTitle = $(thisRow).children("td").find('a.messagetitlelink').first().text();
                        $(thisRow).children("td").children('span.firstChild').prepend('<span class="messageNew">' + newFlag + '</span>');
                        $(thisRow).children("td").find('a.messagetitlelink').first().html('<span class="unreadMsg">' + thisTitle + '</span>');
                    }
                    else {
						//in dfFlatView - add "New" flag, as well as link target for the thread navigator
						$(self).closest("td").find("span.authorImage").after('<span class="messageNew">' + newFlag + '</span>');
						if (setupMessageNav) { setupMessageNav('messageNew'); }
						var thisRowNumReaders = $(self).closest("div").parent("div").children("span.messageNewNumReaders");
						var messageNumReaders = parseInt($(thisRowNumReaders).text(), 10) || 0;
						if (messageNumReaders>0) { $(thisRowNumReaders).text(messageNumReaders - 1); }
                    }

                    //remove at end after references are not needed
                    $(self).remove();
                }, 500);
            }
            else {
                $(self).remove();
                $("#" + messageId).parents("tr:first").css("backgroundColor", "#ffD0DC");
            }
            const parentTable = $(self).parents('table');
            const total = $(parentTable).find('tr').size();
            if (!!window.numRead && window.numRead > 0) {
                window.numRead--;
                updateBar(window.numRead, total);
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

function doAjaxRead(messageId, topicId, self){
    $(self).prop('src', '/library/image/sakai/spinner.gif');
    $.ajax({
        type: "GET",
        url: document.forms[0].action,
        data: "ajax=true&action=markMessageAsRead&messageId=" + messageId + "&topicId=" + topicId,
        success: function(msg){
            if (msg.match(/SUCCESS/)) {
                setTimeout(function(){
					$(self).parents('tr').removeClass('messageNewNext');
					$(self).parents("div").children("span.messageNew").remove();
					$(self).parents("div").children("div.messageMetadata").find("span.unreadMsg").removeClass('unreadMsg');
					$(self).parents("div").parents("div").children('a.messageNewAnchor').remove();
					// remove "Go to first new message" link if all messages have been marked as "read"
					if ($('.messagesThreaded').find('a.messageNewAnchor').size() === 0) {
					    $('.jumpToNew').remove();
					}
					//add button
					if (isMarkAsNotReadValue === "true") {
						$(self).prepend(
						  $('<a>', {
						    href: 'javascript:void(0);',
						    title: markAsNotReadText,
						    class: 'markAsNotReadIcon button',
						    text: markAsNotReadText,
						    click: function() {
						      doAjax(messageId, topicId, this);
						    }
						  })
						);
					}
					if (setupMessageNav) { setupMessageNav('messageNew'); }
					var thisRowNumReaders = $(self).parent("div").parent("div").children("span.messageNewNumReaders");
					var messageNumReaders = parseInt($(thisRowNumReaders).text(), 10) || 0;
					$(thisRowNumReaders).text(messageNumReaders + 1);
                }, 500);
            }
        }
    });
    return false;
}

$(document).ready(function() {
    var toggleFinished = true;
    $('.toggle').click(function(e) {
        if (toggleFinished) {
            toggleFinished = false;
            $(this).parent().parent().find('.toggleParent').toggle();
            $(this).parent().parent().find('[id$=fullTopicDescription]').slideToggle('slow', function() {
                toggleFinished = true;
            });
        }
        e.preventDefault();
        return false;
    });

    // Account for the styling of Discussions displayed in the Lessons ShowItem iframe.
    let body = document.querySelector('body');
    if (! body.classList.contains('Mrphs-portalBody')) {
        body.classList.add('Mrphs-sakai-forums');
    }
});

function toggleDates(hideShowEl, parent, element) {
    resize();
    hideShowEl.toggle();
    parent.slideToggle(resize);
    element.toggle();
}

// open print preview in another browser window so can size approx what actual
// print out will look like
function printFriendly(url) {
	newwindow=window.open(url,'mywindow','width=960,height=1100,scrollbars=yes,resizable=yes');
	if (window.focus) {newwindow.focus()}
}

var sakaiCKEditorName;
$(document).ready(function () {
    if (typeof(CKEDITOR) != 'undefined') {
        for (instance in CKEDITOR.instances) {
            // there should only be one ckeditor per page
            // save the instance name for other functions to use
            sakaiCKEditorName = instance;

            // bind to the keyup and paste to update the word count
            CKEDITOR.instances[instance].on("instanceReady", function () {
                    this.document.on("keyup", ckeditor_word_count);
                    this.document.on("paste", ckeditor_word_count);
            });
        }
    }

});

function ckeditor_word_count() {
     msgcntr_word_count(CKEDITOR.instances[sakaiCKEditorName].getData());
}

function msgcntr_word_count(forumHtml) {
    if (document.getElementById('counttotal')) {
        document.getElementById('counttotal').innerHTML = "<span class='highlight'>(" + getWordCount(forumHtml) + ")</span>";
    }
}

 function getWordCount(msgStr) {
 
     var matches = msgStr.replace(/<[^<|>]+?>|&nbsp;/gi,' ').replace(/[\u0080-\u202e\u2030-\u205f\u2061-\ufefe\uff00-\uffff]/g,'x').match(/\b/g);
    var count = 0;
    if(matches) {
        count = matches.length/2;
    }

    return count;
}

function InsertHTML(header) { 
	// These lines will write to the original textarea and makes the quoting work when ckeditor is not present
	var finalhtml = header + ' <i>' + titletext + '</i><br/><br/><i>' + messagetext + '</i><br/><br/>';

	document.forms['dfCompose'].elements[rteId].value = finalhtml;
	// Get the editor instance that we want to interact with.
	if (typeof(CKEDITOR) != 'undefined') {
		var oEditor = CKEDITOR.instances[sakaiCKEditorName];
		// Check the active editing mode.
		if ( oEditor.mode == 'wysiwyg' ) {
			// Insert the desired HTML.
			oEditor.insertHtml( finalhtml );
		} else alert( 'You must be in WYSIWYG mode!' );
	}
  return false;
}

var clicked = 'false';
function disable() {
    if (clicked == 'false') {
        clicked = 'true'
    }
    else {
        document.forms[0].elements['dfCompose:post'].disabled=true;
    }
}

function checkUpdate() {
    var tables= document.getElementsByTagName("INPUT");
    for (var i = 0; i < tables.length; i++) {
        if (tables[i].name.indexOf("removeCheckbox") >=0) {
            if(tables[i].checked) {
              abledButton();
              break;
            }
            else disabledButton();
        }
    }
}

function disabledButton() {
    var inputs= document.getElementsByTagName("INPUT");
    for (var i = 0; i < inputs.length; i++) {
        if (inputs[i].name.indexOf("delete_submit") >=0) {
          inputs[i].disabled=true;
          inputs[i].className='disabled';
        }
    }
}
function abledButton() {
    var inputs= document.getElementsByTagName("INPUT");
    for (var i = 0; i < inputs.length; i++) {
        if (inputs[i].name.indexOf("delete_submit") >=0) {
          inputs[i].disabled=false;
          inputs[i].className='enabled';
        }
    }
}

function uncheckOthers(field){
    var type1= document.getElementById("addRank:radiobtnType1");
    var type2= document.getElementById("addRank:radiobtnType2");
    var type1div = document.getElementById("type1div");
    var minpost= document.getElementById("addRank:minpost");  // input field for min. post threshold.
    fieldname = field.getAttribute("name");
    var radio1 = type1.getElementsByTagName("INPUT")[0];
    var radio2 = type2.getElementsByTagName("INPUT")[0];
    if (fieldname ==radio1.getAttribute("name")){
            radio2.checked = false;
            type1div.style.display="block";
            minpost.disabled=true;
    } else {
            radio1.checked = false;
            minpost.disabled=false;
            type1div.style.display="none";
    }
  
    var ranktype=  field.getAttribute("value");
    var inputhidden = document.getElementById("addRank:selectedRankType");
    inputhidden.setAttribute("value", ranktype);
}

function validate(form){
    var rankname_missing = false;
    var ranktype_missing= false;
    var rankminPost_missing= false;
    var rankassign_missing= false;
    
    // RANK NAME
    var rankname= document.getElementById("addRank:rankname");
    if (rankname.value.length < 1) {
        rankname_missing  = true ;
    }
    
    // RANK TYPE
    var type1= document.getElementById("addRank:radiobtnType1");
    var type2= document.getElementById("addRank:radiobtnType2");
    var type1div = document.getElementById("type1div");
    var minpost= document.getElementById("addRank:minpost");  // input field for min. post threshold.
    var radio1 = type1.getElementsByTagName("INPUT")[0];
    var radio2 = type2.getElementsByTagName("INPUT")[0];
    var assignToNames = document.getElementById("addRank:aggregate_assign_to_item_ids");
    if (radio1.checked == true ) {
        // check if assignTo is filled
        var peoplecount = $(".sakai-ppkr-to-container").children('div:visible').length;
        if (peoplecount > 0 ) {
            rankassign_missing= false ;
        }
        else {
            rankassign_missing= true;
        }
    }
    else if (radio2.checked == true )  {
        // check min. # of post
        if (minpost.value > 0) {
            rankminPost_missing = false;
        }
        else
            rankminPost_missing = true;
    }
    else {
        ranktype_missing= true ;
    }

    var topAlertDiv= document.getElementById("topAlert");
    var rankTypeAlertDiv = document.getElementById("rankTypeAlert");
    var ranknameDiv= document.getElementById("ranknamebox");
    var minpostDiv= document.getElementById("minpostbox");  // input field for min. post threshold.
    var assigntoDiv = document.getElementById("assigntobox");  // input field for min. post threshold.
    
    // Display red boxes based on errors.   
    if(topAlertDiv) {
        if(rankname_missing || ranktype_missing || rankminPost_missing || rankassign_missing ) {
            topAlertDiv.style.display="block";
        } else {
            topAlertDiv.style.display="none";
        }
    }
    
    if(rankTypeAlertDiv) {
        if(ranktype_missing) {
            rankTypeAlertDiv.style.display="block";
        } else {
            rankTypeAlertDiv.style.display="none";
        }
    }
    
    if(ranknameDiv) {
        if(rankname_missing ) {
            ranknameDiv.style.border="1px solid #FF5555";
        } else {
            ranknameDiv.style.border="";
        }
    }
    
    if(assigntoDiv) {
        if(rankassign_missing) {
            assigntoDiv.style.border="1px solid #FF5555";
        } else {
            assigntoDiv.style.border="";
        }
    }

    if(minpostDiv) {
        if(rankminPost_missing) {
            minpostDiv.style.border="1px solid #FF5555";
        } else {
            minpostDiv.style.border="";
        }
    }

    var imageErrHidden= document.getElementById("addRank:imageSizeErr_hidden");
    var imageSizeAlertDiv= document.getElementById("imageSizeAlert");
    var hiddenattachid= document.getElementsByName("addRank:add_attach.uploadId");  // input field for min. post threshold.
    var filename =  "";
    if (hiddenattachid && hiddenattachid[0]) {
          filename =  hiddenattachid[0].value;
    }
    
    if (rankname_missing || ranktype_missing || rankminPost_missing || rankassign_missing)  {
        rankname_missing = false;
        ranktype_missing= false;
        rankminPost_missing= false;
        rankassign_missing= false;
        // hide image error if other validation fails. forumrankbean.imageSizeErr is from the previous upload.
        if(imageSizeAlertDiv) {
            imageSizeAlertDiv.style.display = "none";
        }
        return false;
    } else {
        rankname_missing = false;
        ranktype_missing= false;
        rankminPost_missing= false;
        rankassign_missing= false;
        if(imageSizeAlertDiv) {
          if ($.trim(filename)  === "") {
                // do not display the imageSizeErr if no file is selected to upload
                imageSizeAlertDiv.style.display = "none";
            }
            else {
                imageSizeAlertDiv.style.display = "block";
            }
        }
    }
    return true;
}

function resizeFrameForDialog()
{
    if (top.location != self.location) {
        var frame = parent.document.getElementById(window.name);
    }
    if( frame ) {
        var clientH = document.body.clientHeight + 400;
        $( frame ).height( clientH );
    }
}

$(document).ready(function(){
    $('.blockMeOnClick').click(function(e){
        var $buttonContainer = $(this).parents('.act');
        var pos = $(this).position();
        var blockerWidth = $(this).width();
        var blockerHeight = $(this).height();

        $(this).blur();
        $buttonContainer.find('#buttonBlocker').remove();
        $buttonContainer.find('input').css({
            'opacity': '1',
            'filter': 'alpha(opacity = 100)'
        });
        $(this).css({
            'opacity': '.5',
            'filter': 'alpha(opacity = 50)',
            'zoom':'1'
        });
        $buttonContainer.append('<div id=\"buttonBlocker\"></div>');
        $('#buttonBlocker').css({
            'width': blockerWidth,
            'height': blockerHeight,
            'top': pos.top,
            'left': pos.left,
            'display': 'block'
        });
        $buttonContainer.find('.messageProgress').fadeIn('slow')
    });

    
    $('.blockAllOnClick').click(function(e){
        $(this).blur();
        var $buttonContainer = $(this).parents('.act');
        $buttonContainer.find('.blockAllOnClick').each(function(i){
            var pos = $(this).position();
            var blockerWidth = $(this).width();
            var blockerHeight = $(this).height();
            $(this).css({
                'opacity': '.5',
                'filter': 'alpha(opacity = 50)',
                'zoom': '1'
            });
            $(this).after('<div class=\"buttonBlocker buttonBlocker' + i +'\"></div>');
            $(this).next('.buttonBlocker' + i).css({
                'width': blockerWidth,
                'height': blockerHeight,
                'top': pos.top,
                'left': pos.left,
                'display': 'block'
            });
        });
        $buttonContainer.find('.messageProgress').fadeIn('slow')
    });

    $('body').on('total-points-updated', function (e) {

        var gradeField = document.getElementById("msgForum:dfMsgGradeGradePoint");
        if (gradeField) {
            gradeField.value = e.detail.value;
        }
    });

});

var MFR = MFR || {};

MFR.saveRubric = function () {

  const rubricGrading = document.getElementsByTagName("sakai-rubric-grading").item(0);
  rubricGrading && rubricGrading.release();
};

MFR.cancelGrading = function () {

  SPNR.disableControlsAndSpin(this, null);
  const rubricGrading = document.getElementsByTagName("sakai-rubric-grading").item(0);
  rubricGrading && rubricGrading.cancel();
  closeDialogBoxIfExists();
}

window.onload = function() {
    document.querySelectorAll("[id*=createEmail1").forEach(item => {
        item.setAttribute('href', item.href.replaceAll('+', ' '));
    });
}
