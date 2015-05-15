// 'Namespace'
var ASN = {};

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
  if (sAmpm === "PM") {
      sHour += 12;
  }
  else if (sHour === 12) {
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
        if (sHour === 0) {
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
    
    if ($('#value_allPurpose').val() === 'true') {
        $('#noAllPurpose').hide('');
        $('#hasAllPurpose').show('');
    }
    resizeFrame('grow');
    
    $(".toggleRoleLink").click(function(){
        var actionType;
        var roleType;
        actionType = this.id.substring(0, this.id.indexOf('_'));
        roleType = this.id.substring(this.id.indexOf('_') + 1);
        if (actionType === "expand") {

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
        if (linkType === "delete") {
            if (nodeType === "allPurpose") {
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
        if (linkType === "add" || linkType === "edit") {
            $('.extraNode').hide();
            $('#' + this.id.substring(0, this.id.indexOf('_')) + '-node').fadeIn('slow');
            $('#extraNodeLinkList').hide();
            resizeFrame('grow');
        }
        location.href = '#extraNodesTop';
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
        
        if (inputType === "cancel") {
                $('#extraNodeLinkList').show();
            if (nodeType === "allPurpose") {
                $('#' + nodeType + '_title').val(defTitle);
            }
            $('#' + nodeType + '_text').val(defText);
            $('#' + nodeType + '_to').val(defTo);
            $('.extraNode').hide();
            location.href = '#extraNodesTop';
        }
        if (inputType === "save") {
            validation = 'ok';
            var titleOK;
            var textOK;
            var toOK;
            var message = '';
            if (nodeType === "allPurpose") {
                if ($('#' + nodeType + '_title').val() === '') {
                    $('#' + nodeType + '_title_message').show();
                    validation = 'failed';
                }
                var selector_checked = $("#allPurposeGroupLists input:checked").length;
                if (selector_checked === 0) {
                    $('#' + nodeType + '_userselect').show();
                    validation = 'failed';
                }
                
                // need to check the date sequence
                // release date
                var allPurposeReleaseHour = $('#' + nodeType + '_releaseHour').val();
                if ($('#' + nodeType + '_releaseAMPM').val()==="PM")
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
                if ($('#' + nodeType + '_retractAMPM').val()==="PM")
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
            
            if ($('#' + nodeType + '_text').val() === '') {
                $('#' + nodeType + '_text_message').show();
                validation = 'failed';
            }
            else {
                textOK = true;
            }
            if (To === 0) {
                $('#' + nodeType + '_to_message').show();
                validation = 'failed';
            }
            else {
                toOK = true;
            }
            
            if (validation === 'ok') {
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
                location.href = '#extraNodesTop';
                $('#extraNodeLinkList').show();			
            }
            else {
                resizeFrame('grow');
                location.href = '#extraNodesBoxTop';
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
    if (top.location !== self.location)
    {
        var frame = parent.document.getElementById(window.name);
    }
    if( frame ) 
    {
        if(updown==='shrink')
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
    if (openInit === true && openInit !== null) {
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

// SAK-29314 - onclick on any of the nav panel buttons...
ASN.setupItemNavigator = function()
{
    $( ".navigator input:button" ).click( function()
    {
        // Disable all buttons
        $( ".navigator input:button" ).attr( "disabled", "disabled" ).addClass( "disabled" );

        // Show the progress indicator and hide the return to list button
        $( ".messageProgress" ).css( "display", "inline" );
        $( ".cancelgradesubmission" ).css( "display", "none" );
        
    });
};

// SAK-26349
ASN.showOrHideAccessMessages = function(groupRadioSelected) {
    
    // Get the elements
    var container = document.getElementById("messages");
    var groupMsg = document.getElementById("msgSelectGroups");
    var children = container.getElementsByTagName("div");
    
    // Show/hide the messages
    ASN.showOrHideSelectGroupsMessage();
    if (groupRadioSelected) {
        for (i = 0; i < children.length; i++) {
            if (children[i].id !== groupMsg.id) {
                children[i].style.display = "none";
            }
        }
    } 
    else {
        for (i = 0; i < children.length; i++) {
            if (children[i].id !== groupMsg.id) {
                children[i].style.display = "block";
            }
        }
    }
};

ASN.showOrHideSelectGroupsMessage = function() {
    
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
    
    // Get the form submission buttons
    var btnPost = document.getElementById("post");
    var btnSave = document.getElementById("save");
    var btnPreview = document.getElementById("preview");
    var buttons = [btnPost, btnPreview];
    if (btnSave !== null) {
        buttons.push(btnSave);
    }
        
    // Show/hide the groups message
    if (groupsRadio.checked && !groupsSelected) {
        groupMsg.style.display = "block";
        
        // Disable the post, save and preview buttons
        for (i = 0; i < buttons.length; i++) {
            buttons[i].disabled = true;
        }
    } 
    else {
        groupMsg.style.display = "none";
        
        // Enable the post, save and preview buttons
        for (i = 0; i < buttons.length; i++) {
            buttons[i].disabled = false;
        }
    }
};

ASN.toggleGroups = function(clickedElement) {

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
};

function highlightSelectedAttachment()
{
    endsWith = function(str, suffix)
    {
        return str.indexOf(suffix, str.length - suffix.length) !== -1;
    };

    var classSuffix = " assignmentAttachmentHighlight";
    var radioButtons = document.getElementsByName("attachmentSelection");
    for (i = 0; i < radioButtons.length; i++)
    {
        var parentDiv = radioButtons[i].parentNode;
        if (radioButtons[i].checked)
        {
            if (!endsWith(parentDiv.className, classSuffix))
            {
                //add the highlight css class
                parentDiv.className+=classSuffix;
            }
        }
        else
        {
            if (endsWith(parentDiv.className, classSuffix))
            {
                //remove the highlight css class
                var cn = parentDiv.className;
                parentDiv.className=cn.substring(0, cn.length - classSuffix.length);
            }
        }
    }
}

ASN.enableLinks = function()
{
    var downloadAll = document.getElementById( "downloadAll" );
    var uploadAll = document.getElementById( "uploadAll" );
    var releaseGrades = document.getElementById( "releaseGrades" );
    var helpItems = document.getElementById( "helpItems" );
    var links = [downloadAll, uploadAll, releaseGrades, helpItems];
    for( i = 0; i < links.length; i++ )
    {
        if( links[i] !== null )
        {
            links[i].className = "";
        }
    }
};

// SAK-29314
ASN.navPanelAction = function( submissionID, action )
{
    document.gradeForm.action += "&submissionId=" + submissionID;
    document.gradeForm.onsubmit();
    document.getElementById( "option" ).value = action;
    document.gradeForm.submit();
    return false;
};

// SAK-29314
ASN.toggleSubNavButtons = function( checkBoxClicked )
{
    // Get the hidden elements
    var prevSubmissionID = document.getElementById( "prevSubmissionId" );
    var nextSubmissionID = document.getElementById( "nextSubmissionId" );
    var prevUngradedSubmissionID = document.getElementById( "prevUngradedSubmissionID" );
    var nextUngradedSubmissionID = document.getElementById( "nextUngradedSubmissionID" );
    var nextWithSubmissionID = document.getElementById( "nextWithSubmissionID" );
    var prevWithSubmissionID = document.getElementById( "prevWithSubmissionID" );
    var nextUngradedWithSubmissionID = document.getElementById( "nextUngradedWithSubmissionID" );
    var prevUngradedWithSubmissionID = document.getElementById( "prevUngradedWithSubmissionID" );

    // Get the buttons
    var nextButtons = document.getElementsByClassName( "nextsubmission" );
    var prevButtons = document.getElementsByClassName( "prevsubmission" );
    var nextUngradedButtons = document.getElementsByClassName( "nextUngraded" );
    var prevUngradedButtons = document.getElementsByClassName( "prevUngraded" );

    // Enable/disabled the buttons conditionally
    var isSubsOnlyChecked = checkBoxClicked.checked;
    if( isSubsOnlyChecked )
    {
        ASN.toggleElements( nextButtons, (nextWithSubmissionID === null) );
        ASN.toggleElements( prevButtons, (prevWithSubmissionID === null) );
        ASN.toggleElements( nextUngradedButtons, (nextUngradedWithSubmissionID === null) );
        ASN.toggleElements( prevUngradedButtons, (prevUngradedWithSubmissionID === null) );
    }
    else
    {
        ASN.toggleElements( nextButtons, (nextSubmissionID === null) );
        ASN.toggleElements( prevButtons, (prevSubmissionID === null) );
        ASN.toggleElements( nextUngradedButtons, (nextUngradedSubmissionID === null) );
        ASN.toggleElements( prevUngradedButtons, (prevUngradedSubmissionID === null) );
    }

    // Synchronize the two checkboxes
    var check1 = document.getElementById( "chkSubsOnly1" );
    var check2 = document.getElementById( "chkSubsOnly2" );
    check1.checked = isSubsOnlyChecked;
    check2.checked = isSubsOnlyChecked;
};

// SAK-29314
ASN.toggleElements = function( buttons, disabled )
{
    for( i = 0; i < buttons.length; i ++ )
    {
        buttons[i].disabled = disabled;
    }
};

// SAK-29314
ASN.disableControls = function( escape )
{
    // Clone and disable all drop downs (disable the clone, hide the original)
    var dropDowns = ASN.nodeListToArray( document.getElementsByTagName( "select" ) );
    for( i = 0; i < dropDowns.length; i++ )
    {
        // Hide the original drop down
        var select = dropDowns[i];
        select.style.display = "none";

        // Create the cloned element and disable it
        var newSelect = document.createElement( "select" );
        newSelect.setAttribute( "id", select.getAttribute( "id" ) + "Disabled" );
        newSelect.setAttribute( "name", select.getAttribute( "name" ) + "Disabled" );
        newSelect.setAttribute( "className", select.getAttribute( "className" ) );
        newSelect.setAttribute( "disabled", "true" );
        newSelect.innerHTML = select.innerHTML;

        // Add the clone to the DOM where the original was
        var parent = select.parentNode;
        parent.insertBefore( newSelect, select );
    }

    // Get all the input elements, separate into lists by type
    var allInputs = ASN.nodeListToArray( document.getElementsByTagName( "input" ) );
    var buttons = [];
    var textFields = [];
    for( i = 0; i < allInputs.length; i++ )
    {
        if( (allInputs[i].type === "submit" || allInputs[i].type === "button") && allInputs[i].id !== escape )
        {
            buttons.push( allInputs[i] );
        }
        else if( allInputs[i].type === "text" && allInputs[i].id !== escape )
        {
            textFields.push( allInputs[i] );
        }
    }

    // Disable all buttons
    ASN.toggleElements( textFields, true );
    for( i = 0; i < buttons.length; i++ )
    {
        ASN.disableButton( "", buttons[i] );
    }

    // Get the download/upload links
    var downAll = document.getElementById( "downloadAll" );
    var upAll = document.getElementById( "uploadAll" );
    var release = document.getElementById( "releaseGrades" );
    var helpItems = document.getElementById( "helpItems" );
    var links = [downAll, upAll, release, helpItems];
    for( i = 0; i < links.length; i++ )
    {
        if( links[i] !== null )
        {
            ASN.disableLink( links[i] );
        }
    }
};

// SAK-29314
ASN.nodeListToArray = function( nodeList )
{
    var array = [];
    for( var i = nodeList.length >>> 0; i--; )
    {
        array[i] = nodeList[i];
    }

    return array;
};

// SAK-29314
ASN.changePageSize = function()
{
    ASN.disableControls();
    ASN.showSpinner( "navSpinner" );
    document.pagesizeForm.submit();
};

// SAK-29314
ASN.changeView = function()
{
    document.getElementById( "option" ).value = "changeView";
    ASN.disableControls();
    ASN.showSpinner( "viewSpinner" );
    document.viewForm.submit();
    return false;
};

// SAK-29314
ASN.applySearchFilter = function( searchOption )
{
    document.getElementById( "option" ).value = searchOption;

    // Disable everything but the search field
    ASN.disableControls( "search" );

    // Clone and disable the search field (disable the clone, hide the original)
    var original = document.getElementById( "search" );
    var clone = document.createElement( "input" );
    var parent = original.parentNode;
    clone.setAttribute( "type", "text" );
    clone.setAttribute( "id", "searchDisabled" );
    clone.setAttribute( "name", "searchDisabled" );
    clone.setAttribute( "className", original.getAttribute( "className" ) );
    clone.value = original.value;
    clone.setAttribute( "disabled", "true" );
    original.style.display = "none";
    parent.insertBefore( clone, original );

    ASN.showSpinner( "userFilterSpinner" );
    document.viewForm.submit();
    return false;
};

// SAK-29314
ASN.doLinkAction = function( action )
{
    document.getElementById( "option" ).value = action;
    ASN.disableControls();
    ASN.showSpinner( "navSpinner" );
};

// SAK-29314
ASN.applyDefaultGrade = function()
{
    // If the default grade field is a text field, we need to do the clone & hide approach
    var defaultGradeTextField = document.getElementById( "defaultGrade_2" );
    if( defaultGradeTextField !== null )
    {
        // Disable everything but the text field
        ASN.disableControls( "defaultGrade_2" );

        // Clone and disable the text field (disable the clone, hide the original)
        var clone = document.createElement( "input" );
        var parent = defaultGradeTextField.parentNode;
        clone.setAttribute( "type", "text" );
        clone.setAttribute( "id", "defaultGrade_2Disabled" );
        clone.setAttribute( "name", "defaultGrade_2Disabled" );
        clone.setAttribute( "size", defaultGradeTextField.getAttribute( "size" ) );
        clone.setAttribute( "className", defaultGradeTextField.getAttribute( "className" ) );
        clone.value = defaultGradeTextField.value;
        clone.setAttribute( "disabled", "true" );
        defaultGradeTextField.style.display = "none";
        parent.insertBefore( clone, defaultGradeTextField );
    }

    // Otherwise, it's a drop down, so we can just take the normal approach
    else
    {
        ASN.disableControls();
    }

    ASN.showSpinner( "applyGradeSpinner" );
    document.defaultGradeForm.submit();
};

// SAK-29314
ASN.showSpinner = function( spinnerID )
{
    document.getElementById( spinnerID ).style.visibility = "visible";
};

// SAK-29314
ASN.disableButton = function( divId, button )
{
    // first set the button to be invisible
    button.style.display = "none";

    // now create a new disabled button with the same attributes as the existing button
    var newButton = document.createElement( "input" );

    newButton.setAttribute( "type", "button" );
    newButton.setAttribute( "id", button.getAttribute( "id" ) + "Disabled" );
    newButton.setAttribute( "name", button.getAttribute( "name" ) + "Disabled" );
    newButton.setAttribute( "value", button.getAttribute( "value" ) );
    newButton.className = button.className + " noPointers";
    newButton.setAttribute( "disabled", "true" );

    if( "" !== divId )
    {
        var div = document.getElementById( divId );
        div.insertBefore( newButton, button );
    }
    else
    {
        var parent = button.parentNode;
        parent.insertBefore( newButton, button );
    }
};

// SAK-29314
ASN.doGradingFormAction = function( reference, option )
{
    // Apply the reference to the form's action handler if necessary
    if( reference !== null )
    {
        document.gradeForm.action = document.gradeForm.action + "&submissionId=" + reference;
    }

    // Call the form's onSubmit function; change the hidden option value
    document.gradeForm.onsubmit();
    document.getElementById( "option" ).value = option;

    // 'Disable' all form controls to prevent click happy users, except the grade text field; show the spinner
    ASN.disableControls( "grade" );
    ASN.showSpinner( "gradeFormSpinner" );

    // Submit the form
    document.gradeForm.submit();
    return false;
};

// SAK-29314
ASN.doGradingPreviewAction = function()
{
    var buttons = ASN.nodeListToArray( document.getElementsByTagName( "input" ) );
    for( i = 0; i < buttons.length; i++ )
    {
        if( buttons[i].type === "submit" || buttons[i].type === "button" )
        {
            ASN.disableButton( "", buttons[i] );
        }
    }

    ASN.showSpinner( "gradeFormPreviewSpinner" );
};

// SAK-29314
ASN.disableLink = function( link )
{
    link.className = "noPointers";
    link.disabled = true;
};
