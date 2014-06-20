function getSelect(selectBox) {
    if (selectBox && selectBox instanceof HTMLSelectElement) { 
        return selectBox.options[selectBox.selectedIndex].value;
    }
}

function setSelect(selectBox,index) {
    if (selectBox && selectBox instanceof HTMLSelectElement) { 
        selectBox.value = index;
    }
}

// Parses select fields on a form and returns the date object for a specific prefix
function getSelectDate (prefix) {
  var sMonth = parseInt(getSelect(document.getElementById(prefix+"month")));
  var sDay = parseInt(getSelect(document.getElementById(prefix+"day")));
  var sYear = parseInt(getSelect(document.getElementById(prefix+"year")));
  var sHour = parseInt(getSelect(document.getElementById(prefix+"hour")));
  var sMinute = parseInt(getSelect(document.getElementById(prefix+"min")));
  var sAmpm = getSelect(document.getElementById(prefix+"ampm"));
  if (sAmpm == "PM") {
      sHour += 12;
  }
  else if (sHour == 12) {
      sHour = 0;
  }
  return new Date(sYear,sMonth-1,sDay,sHour,sMinute,0,0);
}

// Sets a various fields of a select date with a prefix to the date field
function setSelectDate (prefix,dateval) {
    if (dateval && dateval instanceof Date) {
        setSelect(document.getElementById(prefix+"year"),dateval.getFullYear());
        setSelect(document.getElementById(prefix+"month"),dateval.getMonth()+1);
        setSelect(document.getElementById(prefix+"day"),dateval.getDate());
        var sHour = dateval.getHours();
        var sAmpm = sHour >= 12 ? 'PM' : 'AM';
        sHour = sHour % 12;
        if (sHour == 0) {
           sHour = 12; 
        }
        setSelect(document.getElementById(prefix+"hour"),sHour);
        setSelect(document.getElementById(prefix+"ampm"),sAmpm);
        setSelect(document.getElementById(prefix+"min"),dateval.getMinutes());
    }
}

function dueDateChange(field) {
  var dueprefix = "new_assignment_due";
  var acceptprefix = "new_assignment_close";

  var dueDate = getSelectDate(dueprefix);
  var acceptDate = getSelectDate(acceptprefix);

  if (dueDate.getTime() > acceptDate.getTime()) {
    //Due date > accept date, update acceptDate field to match
    setSelectDate(acceptprefix,dueDate); 
  }

}
function setupAssignNew(){
    // show the previously opened field
    $('.extraNode').hide();
    showNode = $('#attachments_for').val();
    $('#' + showNode + '-node').fadeIn('slow');
    $('.validationError').hide();
    
    if ($('#value_allPurpose').val() == 'true') {
        $('#noAllPurpose').hide('');
        $('#hasAllPurpose').show('');
    }
    resizeFrame('grow');
    
    $(".toggleRoleLink").click(function(){
        var actionType;
        var roleType;
        actionType = this.id.substring(0, this.id.indexOf('_'));
        roleType = this.id.substring(this.id.indexOf('_') + 1);
        if (actionType == "expand") {

            $('#roleDiv_' + roleType).show('');
            $('#collapse_' + roleType).show('');
            $('#expand_' + roleType).hide('');
            resizeFrame('grow');
        }
        else {
            $('#roleDiv_' + roleType).hide('');
            $('#expand_' + roleType).show('');
            $('#collapse_' + roleType).hide('');
            resizeFrame('grow');
        }
    });
    
    $(".extraNodeLink").click(function(){
        $('.validationError').hide();
        nodeType = this.id.substring(0, this.id.indexOf('_'));
        linkType = this.id.substring((this.id.indexOf('_') + 1));
        if (linkType == "delete") {
            if (nodeType == "allPurpose") {
                $('#' + nodeType + '_title').val('');
                $('#' + nodeType + '_title_holder').val('');
                // uncheck all checkboxes 
                $('#allPurposeGroupLists input[type=checkbox]').prop('checked', '');
                $('#allPurposeGroupLists label').removeClass('selectedItem');
                $('#allPurposeAttachShowWhen input[type=checkbox]').prop('checked', '');
                $('#allPurposeAttachShowWhen #allPurposeHide1').prop('checked', 'checked');
                $('#allPurposeAttachShowWhen #allPurposeHide2').prop('checked', '');
                $('#allPurposeAttachShowWhen select').val('1');
                $('.countDisplay').text('0');
            }
            $('#' + nodeType + '_to_delete').val('true');
            $('#' + nodeType + '_text').val('');
            $('#' + nodeType + '_text_holder').val('');
            $('#' + nodeType + '_to').val('0');
            $('#' + nodeType + '_to_holder').val('0');
            $('#' + 'no' + nodeType).show('');
            $('#' + 'has' + nodeType).hide('');
            $('.extraNode').hide();
		        $('#extraNodeLinkList').show();
            resizeFrame('grow');
        }
        if (linkType == "add" || linkType == "edit") {
            $('.extraNode').hide();
            $('#' + this.id.substring(0, this.id.indexOf('_')) + '-node').fadeIn('slow');
		        $('#extraNodeLinkList').hide();
            resizeFrame('grow');
        }
        location.href = '#extraNodesTop'
    });
    $(".extraNodeInput").click(function(){
        $('.validationError').hide();
        nodeType = this.id.substring(0, this.id.indexOf('_'));
        inputType = this.id.substring((this.id.indexOf('_') + 1));
        Text = $('#' + nodeType + '_text').val();
        Title = $('#' + nodeType + '_title').val();
        To = $('#' + nodeType + '_to').val();
        defText = $('#' + nodeType + '_text_holder').val();
        defTitle = $('#' + nodeType + '_title_holder').val();
        defTo = $('#' + nodeType + '_to_holder').val();
        
        if (inputType == "cancel") {
		        $('#extraNodeLinkList').show();		
            if (nodeType == "allPurpose") {
                $('#' + nodeType + '_title').val(defTitle);
            }
            $('#' + nodeType + '_text').val(defText);
            $('#' + nodeType + '_to').val(defTo);
            $('.extraNode').hide();
            location.href = '#extraNodesTop'
        }
        if (inputType == "save") {
            validation = 'ok';
            var titleOK
            var textOK
            var toOK
            var message = '';
            if (nodeType == "allPurpose") {
                if ($('#' + nodeType + '_title').val() == '') {
                    $('#' + nodeType + '_title_message').show();
                    validation = 'failed';
                }
                var selector_checked = $("#allPurposeGroupLists input:checked").length;
                if (selector_checked == 0) {
                    $('#' + nodeType + '_userselect').show();
                    validation = 'failed';
                }
                
                // need to check the date sequence
                // release date
                var allPurposeReleaseHour = $('#' + nodeType + '_releaseHour').val();
                if ($('#' + nodeType + '_releaseAMPM').val()=="PM")
                {
                	allPurposeReleaseHour=parseInt(allPurposeReleaseHour) + 12;
                }
                var allPurposeReleaseDate = new Date($('#' + nodeType + '_releaseYear').val(), 
                								$('#' + nodeType + '_releaseMonth').val()-1,// month in Javascript date starts with 0
                								$('#' + nodeType + '_releaseDay').val(),
                								allPurposeReleaseHour,
                								$('#' + nodeType + '_releaseMin').val(),
                								0, // seconds
                								0); // milliseconds
                // retract date
                var allPurposeRetractHour = $('#' + nodeType + '_retractHour').val();
                if ($('#' + nodeType + '_retractAMPM').val()=="PM")
                {
                	allPurposeRetractHour= parseInt(allPurposeRetractHour) + 12;
                }
                var allPurposeRetractDate = new Date($('#' + nodeType + '_retractYear').val(), 
                								$('#' + nodeType + '_retractMonth').val()-1,// month in Javascript date starts with 0
                								$('#' + nodeType + '_retractDay').val(),
                								allPurposeRetractHour,
                								$('#' + nodeType + '_retractMin').val(),
                								0, // seconds
                								0); // milliseconds
                if (allPurposeReleaseDate >= allPurposeRetractDate)
                {
                	// show alert if release date is after retract date
                	validation = 'failed';
                	$('#' + nodeType + '_release_after_retract_message').show();
                }
            }
            else {
                titleOK = true;
            }
            
            if ($('#' + nodeType + '_text').val() == '') {
                $('#' + nodeType + '_text_message').show();
                validation = 'failed';
            }
            else {
                textOK = true;
            }
            if (To == 0) {
                $('#' + nodeType + '_to_message').show();
                validation = 'failed';
            }
            else {
                toOK = true;
            }
            
            if (validation == 'ok') {
                if (titleOK) {
                    $('#' + nodeType + '_title_holder').val(Title);
                }
                if (textOK) {
                    $('#' + nodeType + '_text_holder').val(Text);
                }
                if (toOK) {
                    $('#' + nodeType + '_to_holder').val(To);
                }
                $('#' + 'no' + nodeType).hide('');
                $('#' + 'has' + nodeType).show('');
                $('#value_' + nodeType).val('true');
                $('.extraNode').hide();
                $('.validationError').hide();
                location.href = '#extraNodesTop'
			        $('#extraNodeLinkList').show();			
            }
            else {
                resizeFrame('grow');
                location.href = '#extraNodesBoxTop'
            }
        }
    });
    $(".userList input").click(function(){
        var thisCount = Number($(this).parents('.groupCell').children('.countDisplay').text());
        $(this).parents('.groupCell').find('.selectAllMembers').prop('checked', '');
        if (this.checked) {
            $(this).parent('label').addClass('selectedItem');
            ($(this).parents('.groupCell').children('.countDisplay').text(thisCount + 1));
        }
        else {
            ($(this).parents('.groupCell').children('.countDisplay').text(thisCount - 1));
            $(this).parent('label').removeClass('selectedItem');
            
        }
    });
    $(".selectAllMembers").click(function(){
        if (this.checked) {
        	// need to minus the "select all" input box itself when counting the total user selected
            $(this).parents('.groupCell').children('.countDisplay').text($(this).parents('.groupCell').find('input').prop('checked', 'checked').length-1);
            $(this).parents('.groupCell').find('input').prop('checked', 'checked');
            
            $(this).parents('.groupCell').find('li label').addClass('selectedItem');
        }
        else {
            $(this).parents('.groupCell').children('.countDisplay').text('0');
            $(this).parents('.groupCell').find('input').prop('checked', '');
            $(this).parents('.groupCell').find('li label').removeClass('selectedItem');
        }
    });
    $(".groupCell").each(function(){
        if ($(this).find('input.selectAllMembers:checked').length) {
            $(this).children('.countDisplay').text($(this).find('input').prop('checked', 'checked').length);
        }
        else {
            $(this).children('.countDisplay').text($(this).find('.countHolder').text());
        }
    });
    
}

	function resizeFrame(updown) 
	{
		if (top.location != self.location) 	 {
			var frame = parent.document.getElementById(window.name);
		}	
			if( frame ) 
		{
			if(updown=='shrink')
			{
				var clientH = document.body.clientHeight;
			}
			else
			{
				var clientH = document.body.clientHeight + 30;
			}
			$( frame ).height( clientH );
		} 
		else 
		{
//			throw( "resizeFrame did not get the frame (using name=" + window.name + ")" );
		}
	}
	
	 	// toggle a fade
 	jQuery.fn.fadeToggle = function(speed, easing, callback) {
   return this.animate({opacity: 'toggle'}, speed, easing, callback);
}; 


function setupToggleAreas(toggler, togglee, openInit, speed){
		// toggler=class of click target
		// togglee=class of container to expand
		// openInit=true - all togglee open on enter
		// speed=speed of expand/collapse animation
	  if (openInit == true && openInit != null) {
				$('.expand').hide();
    }
    else {
        $('.' + togglee).hide();
        $('.collapse').hide();
        resizeFrame();
    }
    $('.' + toggler).click(function(){
        $(this).next('.' + togglee).fadeToggle(speed);
        $(this).find('.expand').toggle();
        $(this).find('.collapse').toggle();
        resizeFrame();
    });
}

function setupItemNavigator(){
    $('.itemNav input').click(function(){
        var what = $(this).attr('class');
        
        $('.' + what).prop('disabled','disabled').addClass('disabled');
        $('.messageProgress').css('visibility','visible');
        
    });
}

// SAK-26349
function showOrHideAccessMessages(groupRadioSelected) {
    
    // Get the elements
    var container = document.getElementById("messages");
    var groupMsg = document.getElementById("msgSelectGroups");
    var children = container.getElementsByTagName("div");
    
    // Show/hide the messages
    if (groupRadioSelected) {
        showOrHideSelectGroupsMessage();
        for (i = 0; i < children.length; i++) {
            if (children[i].id !== groupMsg.id) {
                children[i].style.display = "none";
            }
        }
    } 
    else {
        showOrHideSelectGroupsMessage();
        for (i = 0; i < children.length; i++) {
            if (children[i].id !== groupMsg.id) {
                children[i].style.display = "block";
            }
        }
    }
}

function showOrHideSelectGroupsMessage() {
    
    // Get the elements
    var groupMsg = document.getElementById("msgSelectGroups");
    var groupsRadio = document.getElementById("groups");
    var checkboxes = document.getElementsByName("selectedGroups");
    
    // Determine if groups are selected
    var groupsSelected = false;
    for (i = 0; i < checkboxes.length; i++) {
        if (checkboxes[i].checked === true) {
            groupsSelected = true;
        }
    }
        
    // Show/hide the groups message
    if (groupsRadio.checked && !groupsSelected) {
        groupMsg.style.display = "block";
    } 
    else {
        groupMsg.style.display = "none";
    }
}

function toggleGroups(clickedElement) {

    // Get the elements
    var parent = clickedElement.parentNode;
    var childDivs = parent.getElementsByTagName("div");
    
    // Show/hide the groups panel
    for (i = 0; i < childDivs.length; i++) {
        if(childDivs[i].id === "groupsPanel") {
            if (childDivs[i].style.display === "none") {
                childDivs[i].style.display = "block";
            } 
            else {
                childDivs[i].style.display = "none";
            }
            
            // Change the triangle disclosure icon as appropriate
            if (clickedElement.className === "collapse") {
                clickedElement.className = "expand";
            } 
            else {
                clickedElement.className = "collapse";
            }
        }
    }
}
