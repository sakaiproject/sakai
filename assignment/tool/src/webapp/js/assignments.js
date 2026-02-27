// 'Namespace'
var ASN = ASN || {};

// http://stackoverflow.com/a/6021027/3708872
ASN.updateQueryStringParameter = function(uri, key, value) {
  var re = new RegExp("([?&])" + key + "=.*?(&|$)", "i");
  var separator = uri.indexOf('?') !== -1 ? "&" : "?";
  if (uri.match(re)) {
    return uri.replace(re, '$1' + key + "=" + value + '$2');
  }
  else {
    return uri + separator + key + "=" + value;
  }
}

ASN.getSelect = function(selectBox) {
    if (selectBox && selectBox instanceof HTMLSelectElement) { 
        return selectBox.options[selectBox.selectedIndex].value;
    }
};

ASN.setSelect = function(selectBox,index) {
    if (selectBox && selectBox instanceof HTMLSelectElement) { 
        selectBox.value = index;
    }
};

// Parses select fields on a form and returns the date object for a specific prefix
ASN.getSelectDate = function(prefix) {
  var sMonth = parseInt(ASN.getSelect(document.getElementById(prefix+"month")));
  var sDay = parseInt(ASN.getSelect(document.getElementById(prefix+"day")));
  var sYear = parseInt(ASN.getSelect(document.getElementById(prefix+"year")));
  var sHour = parseInt(ASN.getSelect(document.getElementById(prefix+"hour")));
  var sMinute = parseInt(ASN.getSelect(document.getElementById(prefix+"min")));
  var sAmpm = ASN.getSelect(document.getElementById(prefix+"ampm"));
  if (sAmpm === "PM") {
      sHour += 12;
  }
  else if (sHour === 12) {
      sHour = 0;
  }
  return new Date(sYear,sMonth-1,sDay,sHour,sMinute,0,0);
};

// Sets a various fields of a select date with a prefix to the date field
ASN.setSelectDate = function(prefix,dateval) {
    if (dateval && dateval instanceof Date) {
        ASN.setSelect(document.getElementById(prefix+"year"),dateval.getFullYear());
        ASN.setSelect(document.getElementById(prefix+"month"),dateval.getMonth()+1);
        ASN.setSelect(document.getElementById(prefix+"day"),dateval.getDate());
        var sHour = dateval.getHours();
        var sAmpm = sHour >= 12 ? 'PM' : 'AM';
        sHour = sHour % 12;
        if (sHour === 0) {
           sHour = 12; 
        }
        ASN.setSelect(document.getElementById(prefix+"hour"),sHour);
        ASN.setSelect(document.getElementById(prefix+"ampm"),sAmpm);
        ASN.setSelect(document.getElementById(prefix+"min"),dateval.getMinutes());
    }
};

ASN.setupAssignNew = function(){
    // show the previously opened field
    $('.extraNode').hide();
    showNode = $('#attachments_for').val();
    if (showNode && showNode !== '') {
        $('#' + showNode + '-node').fadeIn('slow');
        // Scroll to the opened node to bring it into view
        setTimeout(function() {
            location.href = '#extraNodesTop';
        }, 500); // Wait for fadeIn animation to complete
    }
    $('.validationError').hide();
    
    if ($('#value_allPurpose').val() === 'true') {
        $('#noAllPurpose').hide('');
        $('#hasAllPurpose').show('');
    }
    ASN.resizeFrame('grow');
    
    $(".toggleRoleLink").click(function(){
        var actionType;
        var roleType;
        actionType = this.id.substring(0, this.id.indexOf('_'));
        roleType = this.id.substring(this.id.indexOf('_') + 1);
        if (actionType === "expand") {

            $('#roleDiv_' + roleType).show('');
            $('#collapse_' + roleType).show('');
            $('#expand_' + roleType).hide('');
            ASN.resizeFrame('grow');
        }
        else {
            $('#roleDiv_' + roleType).hide('');
            $('#expand_' + roleType).show('');
            $('#collapse_' + roleType).hide('');
            ASN.resizeFrame('grow');
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
            ASN.resizeFrame('grow');
        }
        if (linkType === "add" || linkType === "edit") {
            $('.extraNode').hide();
            $('#' + this.id.substring(0, this.id.indexOf('_')) + '-node').fadeIn('slow');
            $('#extraNodeLinkList').hide();
            ASN.resizeFrame('grow');
        }
        location.href = '#extraNodesTop';
    });
    $(".extraNodeInput").click(function(){
        $('.validationError').hide();
        nodeType = this.id.substring(0, this.id.indexOf('_'));
        inputType = this.id.substring((this.id.indexOf('_') + 1));
        Text = $('#' + nodeType + '_text').val();
        Title = $('#' + nodeType + '_title').val();
        extraNodeTo = $('#' + nodeType + '_to').val();
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
                // Title is now optional for allPurpose - attachments can be used instead
                titleOK = true;  // Always valid for allPurpose
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
            
            // Text is now optional for modelanswer and allPurpose - attachments can be used instead
            if (nodeType === "modelanswer" || nodeType === "allPurpose") {
                textOK = true;  // Always valid for these types
            } else {
                // For other node types (like note), still require text
                if ($('#' + nodeType + '_text').val() === '') {
                    $('#' + nodeType + '_text_message').show();
                    validation = 'failed';
                }
                else {
                    textOK = true;
                }
            }
            // Only validate "to" field for node types that have it (modelanswer, note)
            if (nodeType === "modelanswer" || nodeType === "note") {
                if (extraNodeTo === '0') {
                    $('#' + nodeType + '_to_message').show();
                    validation = 'failed';
                }
                else {
                    toOK = true;
                }
            } else {
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
                    $('#' + nodeType + '_to_holder').val(extraNodeTo);
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
                ASN.resizeFrame('grow');
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
            var usersCount = Number($(this).parents('.groupCell').find('input.selectedItem').length);
            if (usersCount === thisCount+1) {
                $(this).parents('.groupCell').find('.selectAllMembers').prop('checked', true);
            }
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
            $(this).children('.countDisplay').text($(this).find('input.selectedItem').prop('checked', 'checked').length);
        }
        else {
            $(this).children('.countDisplay').text($(this).find('.countHolder').text());
        }
    });

    const isEstimate = document.getElementById('new_assignment_check_add_estimate');
    if (isEstimate && isEstimate.checked) {
        document.getElementById('checkestimaterequired').classList.remove('hidden');
        document.getElementById('inputtimestimate').classList.remove('hidden');
    }
};

ASN.resizeFrame = function(updown)
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
        //throw( "resizeFrame did not get the frame (using name=" + window.name + ")" );
    }
};

// toggle a fade
jQuery.fn.fadeToggle = function(speed, easing, callback) {
    return this.animate({opacity: 'toggle'}, speed, easing, callback);
};

ASN.setupToggleAreas = function(toggler, togglee, openInit, speed){
    // toggler=class of click target
    // togglee=class of container to expand
    // openInit=true - all togglee open on enter
    // speed=speed of expand/collapse animation
    if (openInit === true && openInit !== null) {
            $('.expand').hide();
    }
    else {
        $('.' + togglee).hide();
        $('.Mrphs-sakai-assignment-grades .collapse').not('#peer-reviews .accordion-collapse').hide();
        ASN.resizeFrame();
    }
    $('.' + toggler).click(function(){
        $(this).next('.' + togglee).fadeToggle(speed);
        $(this).find('.expand').toggle();
        $(this).find('.collapse').toggle();
        ASN.resizeFrame();
    });
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

ASN.highlightSelectedAttachment = function()
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
};

ASN.saveChanges = function(formName, textAreaId) {
    var _textArea = document.getElementById(textAreaId);
};

ASN.allowClick = function(object)
{
    object.onclick='';
    object.style.setProperty('color', '--link-active-color');
    var rv = linkFlag;
    // set the flag to be false
    linkFlag = false;
    //return the current flag status
    return rv;
};

ASN.params_unserialize = function(p){
    var ret = [],
    seg = p.replace(/^\?/,'').split('&'),
    len = seg.length, i = 0, s;
    for (;i<len;i++) {
        if (!seg[i]) { continue; }
        s = seg[i].split('=');
        ret.push({'name':s[0],'value':s[1]});
    }
    return ret;
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
ASN.toggleElements = function( elements, disabled )
{
    for( i = 0; i < elements.length; i++ )
    {
        elements[i].disabled = disabled;
    }
};

ASN.checkEnableRemove = function()
{
    var selected = false;
    var checkboxes = document.getElementsByName( "selectedAssignments" );
    for( var i = 0; i < checkboxes.length; i++ )
    {
        if( checkboxes[i].checked )
        {
            selected = true;
            break;
        }
    }

    document.getElementById( "btnRemove" ).disabled = !selected;
    document.getElementById( "btnRemove" ).className = (selected ? "active" : "" );
    document.getElementById( "btnPublish" ).disabled = !selected;
    document.getElementById( "btnPublish" ).className = (selected ? "active" : "" );
    document.getElementById( "btnUnpublish" ).disabled = !selected;
    document.getElementById( "btnUnpublish" ).className = (selected ? "active" : "" );
};

ASN.checkEnableRestore = function()
{
    var selected = false;
    var checkboxes = document.getElementsByName( "selectedAssignments" );
    for( var i = 0; i < checkboxes.length; i++ )
    {
        if( checkboxes[i].checked )
        {
            selected = true;
            break;
        }
    }

    document.getElementById( "btnRestore" ).disabled = !selected;
    document.getElementById( "btnRestore" ).className = (selected ? "active" : "" );
    document.getElementById( "btnHardRemove" ).disabled = !selected;
    document.getElementById( "btnHardRemove" ).className = (selected ? "active" : "" );
};

ASN.toggleResubmitTimePanel = function()
{
    if( document.getElementById( "allowResubmitNumber" ).value !== 0 && document.getElementById( "allowResubmitTime" ) !== null )
    {
        document.getElementById( "allowResubmitTime" ).style.display = "block";
    }
    else
    {
        document.getElementById( "allowResubmitTime" ).style.display = "none";
    }
};

ASN.togglePeerAssessmentOptions = function(checked){
    var section = document.getElementById("peerAssessmentOptions");
    if(checked){
        section.style.display="block";
        ASN.resizeFrame('grow');
    }else{
        section.style.display="none";
        ASN.resizeFrame('shrink');
    }
};
    
ASN.toggleReviewServiceOptions = function(checked){
    var section = document.getElementById("reviewServiceOptions");
    if(checked){
        section.style.display="block";
        ASN.resizeFrame('grow');
    }else{
        section.style.display="none";
        ASN.resizeFrame('shrink');
    }
};

ASN.toggleSmallMatchesOptions = function(checked){
    var section = document.getElementById("smallMatchesOptions");
    if(checked){
        section.style.display="inline-block";
        ASN.resizeFrame('grow');
    }else{
        section.style.display="none";
        ASN.resizeFrame('shrink');
    }
};

ASN.toggleSmallMatchesDisabled = function(){
    var radioOption1 = document.getElementById("wordCount");
    var radioOption2 = document.getElementById("percentage");
    var excludeWords = document.getElementById("tiiExcludeValueWords");
    var excludePercentages = document.getElementById("tiiExcludeValuePercentages");

    excludeWords.disabled = true;
    excludeWords.value = "";
    excludePercentages.disabled = true;
    excludePercentages.value = "";
    if(radioOption1.checked){
        excludeWords.disabled = false;
    }else if(radioOption2.checked){
        excludePercentages.disabled = false;
    }
};

ASN.toggleSelectAll = function(caller, elementName)
{
    var newValue = caller.checked;
    var elements = document.getElementsByName(elementName);
    if(elements)
    {
        //SAK-19147 don't toggle last "Save all submissions in one folder"
        for(var i = 0; i < elements.length; i++)
        {
            if( elements[i].id !== "withoutFolders" )
            {
                elements[i].checked = newValue;
            }
        }
    }
};

ASN.deselectSelectAll = function( caller )
{
    var checked = caller.checked;
    if( !checked )
    {
        document.getElementById( "selectall" ).checked = checked;
    }
};

ASN.handleEnterKeyPress = function(ev)
{
    if (!ev)
    {
        ev = window.event;
    }

    if (ev && ev.keyCode === 13)
    {
         return false; 
    }
};

ASN.invokeDownloadUrl = function(accessPointUrl, actionString, alertMessage, param0, param1, param2, param3, clickedElement)
{
    const extraInfoArray = [];
    let selectedViewString = param1;
    let includeNotSubmitted = false;

    if (document.getElementById('downloadViewString'))
    {
        selectedViewString = document.getElementById('downloadViewString').value;
    }

    if (document.getElementById('downloadSubmissionStatus'))
    {
        includeNotSubmitted = document.getElementById('downloadSubmissionStatus').value === "all";
    }

    if (document.getElementById('studentSubmissionText') && document.getElementById('studentSubmissionText').checked)
    {
        extraInfoArray.push("studentSubmissionText=true");
    }

     if (document.getElementById('studentSubmissionAttachment') && document.getElementById('studentSubmissionAttachment').checked)
    {
        extraInfoArray.push("studentSubmissionAttachment=true");
    }
    if (document.getElementById('gradeFile') && document.getElementById('gradeFile').checked)
    {
        const selectedGradeFileFormat = document.getElementById('gradeFileFormat_excel').checked
            ? document.getElementById('gradeFileFormat_excel').value
            : document.getElementById('gradeFileFormat_csv').value;
        extraInfoArray.push("gradeFile=true&gradeFileFormat=" + encodeURIComponent(selectedGradeFileFormat));
    }
    if (document.getElementById('feedbackTexts') && document.getElementById('feedbackTexts').checked)
    {
        extraInfoArray.push("feedbackTexts=true");
    }
    if (document.getElementById('feedbackComments') && document.getElementById('feedbackComments').checked)
    {
        extraInfoArray.push("feedbackComments=true");
    }
    if (document.getElementById('feedbackAttachments') && document.getElementById('feedbackAttachments').checked)
    {
        extraInfoArray.push("feedbackAttachments=true");
    }
    if (document.getElementById('rubrics') && document.getElementById('rubrics').checked)
    {
        extraInfoArray.push("rubrics=true");
    }
    if (extraInfoArray.length === 0)
    {
        alert(alertMessage);
    }
    else
    {
        SPNR.disableControlsAndSpin( clickedElement, null );

        if (document.getElementById('withoutFolders') && document.getElementById('withoutFolders').checked)
        {
            extraInfoArray.push("withoutFolders=true");
        }

        if (includeNotSubmitted)
        {
            extraInfoArray.push("includeNotSubmitted=true");
        }

        const extraInfoQuery = extraInfoArray.join("&");
        accessPointUrl = accessPointUrl + "?" + extraInfoQuery;
        const encodedContextString = encodeURIComponent(param0 != null ? param0 : "");
        const encodedViewString = encodeURIComponent(selectedViewString != null ? selectedViewString : "");
        const encodedSearchString = encodeURIComponent(param2 != null ? param2 : "");
        const encodedSearchFilterOnly = encodeURIComponent(param3 != null ? param3 : "");
        // attach the assignment reference
        accessPointUrl = accessPointUrl + "&contextString=" + encodedContextString + "&viewString=" + encodedViewString + "&searchString=" + encodedSearchString + "&searchFilterOnly=" + encodedSearchFilterOnly;
        window.location.href=accessPointUrl;
        document.getElementById('downloadUrl').value=accessPointUrl; 
        document.getElementById('uploadAllForm').action=actionString; 
        setTimeout("ASN.submitForm( 'uploadAllForm', null, null, null )", 1500);
    }
};

/* Enables the submit/resubmit button. If checkForFile is true, then it disables the submit/resubmit button if the clonableUpload button has no value*/
ASN.enableSubmitUnlessNoFile = function(checkForFile)
{
    var btnPost = document.getElementById('post');
    var doEnable = true;
    if (checkForFile)
    {
        var btnClonableUpload = document.getElementById('clonableUpload');
        if (btnClonableUpload && btnClonableUpload.value === "")
        {
             doEnable = false;
        }
    }

    if (doEnable)
    {
        btnPost.disabled = false;
    }
    else
    {
        btnPost.disabled = true;
    }
};

ASN.submitForm = function( formID, option, submissionID, view, focusId )
{
    // Get the form
    var form = document.getElementById( formID );
    if( form !== null )
    {
        // Apply the submission ID to the form's action if one is supplied
        if( submissionID !== null )
        {
            form.action = ASN.updateQueryStringParameter(form.action,"submissionId",submissionID);
        }

        if(focusId){
            form.action = form.action + '#' + focusId;
        }

        // Do the onsubmit() if the form has one
        if( form && form.onsubmit )
        {
            form.onsubmit();
        }

        // If an option was given, apply it to the element
        if( option !== null )
        {
            var optionElement = document.getElementById( "option" );
            if( optionElement !== null )
            {
                optionElement.value = option;
            }
        }

        // If a view was given, apply it to the element
        if( view !== null )
        {
            var viewElement = document.getElementById( "view" );
            if( viewElement !== null )
            {
                viewElement.value = view;
            }
        }

        // Do the submit() if the form has one
        if( form && form.submit )
        {
            form.submit();
        }
    }
};

ASN.doStudentViewSubmissionAction = function( formID, option, attachmentID, focusId )
{
    document.getElementById( formID ).currentAttachment.value = attachmentID;
    ASN.submitForm( formID, option, null, null, focusId );
};

ASN.doTagsListAction = function( formID, value, providerID )
{
    var form = document.getElementById( formID );
    form.sakai_action.value = value;
    form.providerId.value = providerID;
};

ASN.toggleAllowResubmissionPanel = function()
{
    var panel = document.getElementById("allowResubmissionPanelContent");
    $(panel).slideToggle(200);
    var expandImg = document.getElementById("expandAllowResub");
    var collapseImg = document.getElementById("collapseAllowResub");
    ASN.swapDisplay(expandImg, collapseImg);
    
    var allow = document.getElementById("allowResToggle");
    if (allow.value === "checked")
    {
        allow.value = "";
    }
    else
    {
        allow.value = "checked";
    }
}

ASN.toggleSendFeedbackPanel = function()
{
    var panel = document.getElementById("sendFeedbackPanelContent");
    $(panel).slideToggle(200);
    var expandImg = document.getElementById("expandSendFeedback");
    var collapseImg = document.getElementById("collapseSendFeedback");
    ASN.swapDisplay(expandImg, collapseImg);
}

ASN.toggleAssignamentInstructionPanel = function()
{
    var panel = document.getElementById("assignamentInstructionPanelContent");
    $(panel).slideToggle(200);
    var expandImg = document.getElementById("expandAssignamentInstruction");
    var collapseImg = document.getElementById("collapseAssignamentInstruction");
    ASN.swapDisplay(expandImg, collapseImg);
}

ASN.swapDisplay = function(elem1, elem2)
{
    var tmpDisplay = elem1.style.display;
    elem1.style.display = elem2.style.display;
    elem2.style.display = tmpDisplay;
}

ASN.toggleIsGroupSubmission = function(checked){
    if (checked) {
        $("#site").prop("disabled", true);
        $("#groups").prop("checked", true).trigger("click");
    } else {
        $("#site").prop("disabled", false);
    }
};

ASN.toggleAutoAnnounceOptions = function(checked){
    var section = document.getElementById("selectAutoAnnounceOptions");
    if(checked){
        section.style.display="block";
        ASN.resizeFrame('grow');
    }else{
        section.style.display="none";
        ASN.resizeFrame('shrink');
    }
};

// SAK-43155
ASN.toggleAutoAnnounceEstimate = function(checked){
    const estimateRequired = document.getElementById('checkestimaterequired');
    const timeEstimate = document.getElementById('inputtimestimate');
    const timeEstimateInput = document.getElementById('new_assignment_input_add_time_estimate');
    const estimateRequiredCheck = document.getElementById('new_assignment_check_add_estimate_required');

    const resizeState = checked ? 'grow': 'shrink';

    if (checked) {
        estimateRequired.classList.remove('hidden');
        timeEstimate.classList.remove('hidden');
    } else {
        estimateRequired.classList.add('hidden');
        timeEstimate.classList.add('hidden');
    }

    ASN.resizeFrame(resizeState);
    if (!checked) {
        timeEstimateInput && (timeEstimateInput.value = '');
        estimateRequiredCheck && (estimateRequiredCheck.checked = '');
    }
};

// SAK-30032
ASN.setupPeerReviewAttachment = function(){
    $('#submissionFileCount').val(1);
    $('#addMoreAttachmentControls').click(function(e){
        e.preventDefault();
        if ($('#submissionFileCount').val() < 5) {
            var $input = $('#clonableUpload').clone().removeAttr('id').addClass('cloned').appendTo('#clonedHolder').children('input');
            $input.val('');
            var $count = $('#submissionFileCount').val();
            var $nameCount = "upload"+$count;
            $input.attr("name", $nameCount);
            $('#submissionFileCount').val(parseInt($('#submissionFileCount').val(), 10) + 1);
            if ($('#submissionFileCount').val() == 5) {
                $('#addMoreAttachmentControls').hide();
                $('#addMoreAttachmentControlsInactive').show();
            }
        }
        $('.cloned a').show();
        ASN.resizeFrame('grow');
    });
    var notifyDeleteControl = function(){
        $('#submissionFileCount').val(parseInt($('#submissionFileCount').val(), 10) - 1);
        if ($('#submissionFileCount').val() < 5) {
            $('#addMoreAttachmentControls').show();
            $('#addMoreAttachmentControlsInactive').hide();
        }
    };
};

// SAK-30032
ASN.submitPeerReviewAttachment = function(id, action)
{
    var theForm = document.getElementById(id);
    if(action !== null) {
        theForm.action = action;
    }
    if(theForm && theForm.onsubmit) {
        theForm.onsubmit();
    }
    if(theForm && theForm.submit) {
        theForm.submit();
    }
};

/* Header: element user interacts with
 * Icon: disclosure triangle
 * Content: element that expands / collapses when the header is interacted with
 * expandText: accessibility text on the header for the expand action
 * collapseText: accessibility text on the header for the collapse action */
ASN.handleReportsTriangleDisclosure = function (header, icon, content, expandText, collapseText)
{
    var expand = "/library/image/sakai/expand.gif";
    var collapse = "/library/image/sakai/collapse.gif";

    var expanded = header.getAttribute("aria-expanded");
    if (expanded === "true")
    {
        header.setAttribute("aria-expanded", "false");
        header.setAttribute("aria-label", expandText);

        icon.src = expand;
        content.style.display = "none";
    }
    else
    {
        header.setAttribute("aria-expanded", "true");
        header.setAttribute("aria-label", collapseText);

        icon.src = collapse;
        content.removeAttribute("style");
        ASN.resizeFrame();
    }
}

// rubrics-specific code
ASN.rubricsEventHandlers = () => {

  document.body && document.body.addEventListener("total-points-updated", e => {

    e.stopPropagation();

    const gradeField = document.getElementById("grade");
    gradeField && (gradeField.value = e.detail.value);
  });
};

ASN.changeVisibleDate = function() 
{
	if($("#allowVisibleDateToggle").prop( "checked" ))
	{
		$('#new_assignment_visiblemonth').val($('#new_assignment_openmonth').val());
		$('#new_assignment_visibleday').val($('#new_assignment_openday').val());
		$('#new_assignment_visibleyear').val($('#new_assignment_openyear').val());
		$('#new_assignment_visiblehour').val($('#new_assignment_openhour').val());
		$('#new_assignment_visiblemin').val($('#new_assignment_openmin').val());
		$('.visibleDatePanel').show();
		ASN.resizeFrame();
	}
	else {
		$('#new_assignment_visiblemonth').val('');
		$('#new_assignment_visibleday').val('');
		$('#new_assignment_visibleyear').val('');
		$('#new_assignment_visiblehour').val('');
		$('#new_assignment_visiblemin').val('');
		$('.visibleDatePanel').hide();
	}
}

ASN.changeResubmissionDate = function()
{
  if(document.getElementById("allowResToggle").checked)
  {
    document.getElementById('allowResubmitNumber').style.display = 'block';
    document.getElementById('allowResubmitTime').style.display = 'block';
    document.getElementById('resubmitNotification').style.display = 'block';
    const closeDatePicker = document.getElementById('closedate');
    const resubmitDatePicker = document.getElementById('resubmissiondate');
    resubmitDatePicker.isoDate = closeDatePicker.isoDate;
    ASN.resizeFrame();
  }
  else
  {
    document.getElementById('allowResubmitNumber').style.display = 'none';
    document.getElementById('allowResubmitTime').style.display = 'none';
    document.getElementById('resubmitNotification').style.display = 'none';
  }
}

ASN.enableTimesheetSetupSection = function()
{
    const el = document.getElementById('timesheetsetupsection');
    el && (el.style.display = 'block');
}

ASN.disableTimesheetSetupSection = function()
{
    const el = document.getElementById('timesheetsetupsection');
    el && (el.style.display = 'none');
    
    const el2 = document.getElementById('new_assignment_check_add_estimate');
    el2 && (el2.checked = false);
    
    ASN.toggleAutoAnnounceEstimate(false);
}

$(document).ready(() => {
  //peer-review and self-report
  $('body').on('rubric-association-loaded', e => {
    const dontAssociateCheck = document.getElementById("dont-associate-radio");
    const doAssociateCheck = document.getElementById("do-associate-radio");
    const studentSelfReport = document.getElementById('rbcs-config-studentSelfReport');
    const usePeerAssessment = document.getElementById("usePeerAssessment");

    if(doAssociateCheck.checked) {
      //page-loading
      if (studentSelfReport.checked) {
        // We do a click to hide "peerAssessmentOptions" container
        if (usePeerAssessment.checked) {
          usePeerAssessment.click();
        }
        usePeerAssessment.disabled = true;
      } else {
        usePeerAssessment.disabled = false;
      }

      if (usePeerAssessment.checked) {
        studentSelfReport.disabled = true;
        studentSelfReport.checked = false;
      } else {
        studentSelfReport.disabled = false;
      }
    }

    doAssociateCheck.addEventListener("change", function() {
        studentSelfReport.disabled = false;
        studentSelfReport.checked = false;
    });

    dontAssociateCheck.addEventListener("change", function() {
        usePeerAssessment.disabled = false;
        studentSelfReport.checked = false;
    });
    
    //event manage
    studentSelfReport.addEventListener("change", function() {
      if (studentSelfReport.checked) {
        // We do a click to hide "peerAssessmentOptions" container
        if (usePeerAssessment.checked) {
          usePeerAssessment.click();
        }
        usePeerAssessment.disabled = true;
      } else {
        usePeerAssessment.disabled = false;
      }
    });
    usePeerAssessment.addEventListener("change", function() {
      if (usePeerAssessment.checked) {
        studentSelfReport.disabled = true;
        studentSelfReport.checked = false;
      } else if (doAssociateCheck.checked) {
        studentSelfReport.disabled = false;
      }
    });
  });

  $("#peer-accordion").accordion({
    heightStyle: 'content',
    active: 0,
    collapsible: true,
    change: function (event, ui) {
      ASN.resizeFrame();
    },
    activate: function (event, ui) {
      ASN.resizeFrame();
    }
  });

  const infoLink = document.getElementById("infoImg");
  infoLink && (new bootstrap.Popover(document.getElementById("infoImg")));

  const saveRubric = e => {
    [...document.getElementsByTagName("sakai-rubric-grading")].forEach(r => r. save());
  };
  const saveButton = document.getElementById("save");
  saveButton && saveButton.addEventListener("click", saveRubric);

  const releaseRubric = e => {
    [...document.getElementsByTagName("sakai-rubric-grading")].forEach(r => r. release());
  };
  const returnButton = document.getElementById("save-and-return");
  returnButton && returnButton.addEventListener("click", releaseRubric);

  const releaseGrades = document.getElementById("releaseGrades");
  releaseGrades && releaseGrades.addEventListener("click", e => {

    e.target.classList.add('noPointers');

    let promises = [];
    [...document.getElementsByTagName("sakai-rubric-student-button")].forEach(b => promises.push(b.releaseEvaluation()));
    Promise.all(promises).then(() => ASN.submitForm('viewForm', 'releaseGrades', null, null));
  });

  const releaseCommented = document.getElementById("releaseCommented");
  releaseCommented && releaseCommented.addEventListener("click", ev =>
      ASN.submitForm('viewForm', 'releaseCommented', null, null));

  const confirmButton = document.getElementById("confirm");
  confirmButton && confirmButton.addEventListener("click", saveRubric);
  const postButton = document.getElementById("post");
  postButton && postButton.addEventListener("click", saveRubric);

  // If grade is released, rubric must be released too
  const gradeIsReleasedInput = document.getElementById("grade-is-released");
  if (gradeIsReleasedInput && gradeIsReleasedInput.value === 'true') {
    const buttons = document.querySelectorAll(".prevsubmission, .prevUngraded, .nextsubmission, .nextUngraded");
    buttons && buttons.forEach(button => button.addEventListener("click", releaseRubric));
  }
});

ASN.cancelGradeSubmission = function () {

  [...document.getElementsByTagName("sakai-rubric-grading")].forEach(r => r. cancel());

  SPNR.disableControlsAndSpin( this, null );
  ASN.submitForm( 'gradeForm', 'cancelgrade', null, null );
  return false;
};

// SAK-43911 (grab_cursor for reordering items)
ASN.grabbing = function (selectedItem) {
    li = $(selectedItem);
    $(li).removeClass("grab_cursor");
    $(li).addClass("grabbing_cursor");
}

ASN.grab = function (selectedItem) {
    li = $(selectedItem);
    $(li).removeClass("grabbing_cursor");
    $(li).addClass("grab_cursor");
}

ASN.clearShadowTags = function () {
    const tagSelector = document.getElementById('tag_selector');
    const tagSelectorVue = document.querySelector("sakai-tag-selector").shadowRoot;
    if (tagSelector && tagSelectorVue) {
      tagSelector.value = '';
      tagSelectorVue.querySelectorAll('input[name="tag[]"').forEach((elem) => elem.remove());
    }
}

ASN.checkIframeTags = function () {
    if (inIframe()) {
        document.getElementById('tagSection').style.display = 'none';
    }
}
