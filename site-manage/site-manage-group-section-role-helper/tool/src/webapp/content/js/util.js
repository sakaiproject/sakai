function setStateValue(){
    alert('instate');
    // concatenate the group member option values
    var stateValue = "";
    var values = document.getElementById('newRight');
    alert(values.value);
    alert("sate=" + stateValue);
    document.getElementById('content::state-init').value = stateValue;
}

setupAutoCreate = function(){
    $('#save').prop('disabled', true);
    // on page open: paint the row if the checkbox is checked
    $('tbody :checkbox').each(function(){
        if (this.checked) {
            $(this).parents("tr").addClass('selectedSelected');
        }
    });
    
    //checkbox click toggles selected class, enables save if there is
    // at least one tbody checkbox selected, disables otherwise
    $('tbody :checkbox').click(function(){
        $(this).parents("tr").toggleClass('selectedSelected');
        checkEnableSave();
    });
    // selecting check on thead selects or deselects all tbody check in same table
    $('thead th :checkbox').click(function(){
        var myList = $(this).parents('table').find('tbody');
        if (this.checked) {
            $(myList).find(':checkbox').prop('checked', true);
            $(myList).find('tr').addClass('selectedSelected');
        }
        else {
            $(myList).find(':checkbox').prop('checked', false);
            $(myList).find('tr').removeClass('selectedSelected');
        }
        checkEnableSave();
    });
    $('#roleOption').click(function(){
        if (this.checked) {
            $("#randomGroupCreateDiv").fadeOut();
        }
        else {
            $("#randomGroupCreateDiv").fadeIn();
        }
        resetFrame();
    });
     $("#rosterOption").click(function(){
        if (this.checked) {
            $("#rosterRandomGroupCreateDiv").fadeOut();
        }
        else {
            $("#rosterRandomGroupCreateDiv").fadeIn();
        }
        resetFrame();
    });
    $('#randomOption').click(function(){
        if (this.checked) {
            $("#randomGroupCreateDiv").fadeIn();
        }
        else {
            $("#randomGroupCreateDiv").fadeOut();
        }
        resetFrame();
    });
    $("#rosterRandomOption").click(function(){
        if (this.checked) {
            $("#rosterRandomGroupCreateDiv").fadeIn();
        }
        else {
            $("#rosterRandomGroupCreateDiv").fadeOut();
        }
        resetFrame();
    });
    $('#groupSplit').click(function(){
        if (this.checked) {
            $('p').removeClass('optGroupSelectSelected');
            $(this).parent('p').addClass('optGroupSelectSelected');
            $('#userFields').slideUp();
            $('#groupFields').slideDown();
        }
    });
    $("#rosterGroupSplit").click(function(){
        if (this.checked) {
            $("p").removeClass("optGroupSelectSelected");
            $(this).parent("p").addClass("optGroupSelectSelected");
            $("#rosterUserFields").slideUp();
            $("#rosterGroupFields").slideDown();
        }
    });
    $('#userSplit').click(function(){
        if (this.checked) {
            $('p').removeClass('optGroupSelectSelected');
            $(this).parent('p').addClass('optGroupSelectSelected');
            $('#userFields').slideDown();
            $('#groupFields').slideUp();
        }
    });
    $("#rosterUserSplit").click(function(){
        if (this.checked) {
            $("p").removeClass("optGroupSelectSelected");
            $(this).parent("p").addClass("optGroupSelectSelected");
            $("#rosterUserFields").slideDown();
            $("#rosterGroupFields").slideUp();
        }
    });
};
checkEnableSave = function(){
    if ($('tbody :checked').length === 0) {
        $('#save').prop('disabled', true).addClass('disabled');
    }
    else {
        $('#save').prop('disabled', false).removeClass('disabled');
    }
    if ($('#roleList tbody :checked').length === 0) {
        $('#roleOptionsFieldset').fadeOut('fast');
    }
    else
    {
        if ($('#roleList tbody :checked').length === 1) {
            $('#roleOptionsFieldset').fadeIn('slow');
            resizeFrame('grow');
        }
        else {
            $('#roleOption').prop('checked', true);
            $('#roleOptionsFieldset').fadeOut('slow');
            $('#randomGroupCreateDiv').fadeOut('slow');
            resizeFrame('grow');
        }
    }

     if ($("#rosterList tbody :checked").length === 0) {
        $("#rosterOptionsFieldset").fadeOut("fast");
    }
    else {
        if ($("#rosterList tbody :checked").length === 1) {
            $("#rosterOptionsFieldset").fadeIn("slow");
            resizeFrame("grow");
        }
        else {
            $("#rosterOption").prop("checked", true);
            $("#rosterOptionsFieldset").fadeOut("slow");
            $("#rosterRandomGroupCreateDiv").fadeOut("slow");
            resizeFrame("grow");
        }
    }
};

setupValidation = function(){


    $('#save').click(function(e){
        var validForm = true;
        if ($("#randomOption").prop('checked') === true) {
            if ($("#groupSplit").prop('checked') === true) {
                if ($('#groupTitle-group').val() === '') {
                    $('#groupTitle-group').parent('div').parent('div').addClass('validationFail');
                    validForm = false;
                }
                else {
                    $('#groupTitle-group').parent('div').parent('div').removeClass('validationFail');
                }
                if ($('#numToSplit-group').val() === '' || isNaN($('#numToSplit-group').val())) {
                    $('#numToSplit-group').parent('div').parent('div').addClass('validationFail');
                    validForm = false;
                }
                else {
                    $('#numToSplit-group').parent('div').parent('div').removeClass('validationFail');
                }
                
            }
            if ($("#userSplit").prop('checked') === true) {
                if ($('#groupTitle-user').val() === '') {
                    $('#groupTitle-user').parent('div').parent('div').addClass('validationFail');
                    validForm = false;
                }
                else {
                    $('#groupTitle-user').parent('div').parent('div').removeClass('validationFail');
                }
                
                if ($('#numToSplit-user').val() === '' || isNaN($('#numToSplit-user').val())) {
                    $('#numToSplit-user').parent('div').parent('div').addClass('validationFail');
                    validForm = false;
                }
                else {
                    $('#numToSplit-user').parent('div').parent('div').removeClass('validationFail');
                }
                
                
            }
        }
         if ($("#rosterRandomOption").prop("checked") === true) {
            if ($("#rosterGroupSplit").prop("checked") === true) {
                if ($("#roster-groupTitle-group").val() === "") {
                    $("#roster-groupTitle-group").parent("div").parent("div").addClass("validationFail");
                    validForm = false;
                }
                else {
                    $("#roster-groupTitle-group").parent("div").parent("div").removeClass("validationFail");
                }
                if ($("#roster-numToSplit-group").val() === "" || isNaN($("#roster-numToSplit-group").val())) {
                    $("#roster-numToSplit-group").parent("div").parent("div").addClass("validationFail");
                    validForm = false;
                }
                else {
                    $("#roster-numToSplit-group").parent("div").parent("div").removeClass("validationFail");
                }
            }
            if ($("#rosterUserSplit").prop("checked") === true) {
                if ($("#roster-groupTitle-user").val() === "") {
                    $("#roster-groupTitle-user").parent("div").parent("div").addClass("validationFail");
                    validForm = false;
                }
                else {
                    $("#roster-groupTitle-user").parent("div").parent("div").removeClass("validationFail");
                }
                if ($("#roster-numToSplit-user").val() === "" || isNaN($("#roster-numToSplit-user").val())) {
                    $("#roster-numToSplit-user").parent("div").parent("div").addClass("validationFail");
                    validForm = false;
                }
                else {
                    $("#roster-numToSplit-user").parent("div").parent("div").removeClass("validationFail");
                }
            }
        }
        if (validForm === false) {
            return false;
        }
        else{
            SPNR.disableControlsAndSpin( this, null );
            return true;
        }
    });
    
};

//this function needs jquery 1.1.2 or later - it resizes the parent iframe without bringing the scroll to the top
function resizeFrame(updown){
    var clientH;
    if (top.location !== self.location) {
        var frame = parent.document.getElementById(window.name);
    }
    if (frame) {
        if (updown === 'shrink') {
            clientH = document.body.clientHeight - 30;
        }
        else {
            clientH = document.body.clientHeight + 90;
        }
        $(frame).height(clientH);
    }
    else {
        throw ("resizeFrame did not get the frame (using name=" + window.name + ")");
    }
}

function toggleCheckboxes( clickedElement )
{
    var checkboxes = document.getElementsByName( "delete-group-selection" );
    for( i = 0; i < checkboxes.length; i++ )
    {
        checkboxes[i].checked = clickedElement.checked;
        adjustCount( checkboxes[i], "removeCount" );
    }

    checkEnableRemove();
}

function syncSelectAll()
{
    var allSelected = true;
    var checkboxes = document.getElementsByName( "delete-group-selection" );
    for( i = 0; i < checkboxes.length; i++ )
    {
        if( !checkboxes[i].checked )
        {
            allSelected = false;
            break;
        }
    }

    document.getElementById( "selectAll" ).checked = allSelected;
    checkEnableRemove();
}

function checkEnableRemove()
{
    var button = document.getElementById( "delete-groups" );
    if( button )
    {
        var anySelected = false;
        var checkboxes = document.getElementsByName( "delete-group-selection" );
        for( i = 0; i < checkboxes.length; i++ )
        {
            if( checkboxes[i].checked )
            {
                anySelected = true;
                break;
            }
        }

        if( anySelected )
        {
            button.disabled = false;
            button.className='enabled active';
        }
        else
        {
            button.disabled = true;
            button.className='disabled';
        }
    }
}

function togglePanel( clickedElement, isUserPanel )
{
    var div = clickedElement.parentNode;
    if( div.className === "edit collapsed" )
    {
        div.className = "edit expanded";
        if( isUserPanel )
        {
            $( "#userRowsContainer" ).show();
            $( "#userRowsContainer" ).children().show();
        }
        else
        {
            $( div ).siblings().show();
        }
    }
    else
    {
        div.className = "edit collapsed";
        if( isUserPanel )
        {
            $( "#userRowsContainer" ).hide();
            $( "#userRowsContainer" ).children().hide();
        }
        else
        {
            $( div ).siblings().hide();
        }
    }
}

function adjustDivHeights()
{
    var userRowsHeader = document.getElementById( "usersNotInSet-title-row::" );
    var userPanelExpanded = userRowsHeader.classList.contains( "expanded" );
    var groupFieldsHeight = document.getElementById( "groupFields" ).offsetHeight;
    var userRowsHeaderHeight = userRowsHeader.offsetHeight;
    var actualHeight = groupFieldsHeight - userRowsHeaderHeight;
    var userRowsContainer = document.getElementById( "userRowsContainer" );
    var style = "height: " + actualHeight + "px" + (userPanelExpanded ? "; overflow-y: scroll;" : ";" );
    userRowsContainer.setAttribute( "style", style );
    userRowsContainer.style.height = actualHeight + "px";
    if( userPanelExpanded )
    {
        userRowsContainer.style.overflowY = "scroll";
    }

     resizeFrame( "grow" );
}

function adjustCount(caller, countName)
{
    var counter = document.getElementById(countName);
    if(caller && caller.checked && caller.checked === true)
    {
        counter.value = parseInt(counter.value) + 1;
    }
    else
    {
        counter.value = parseInt(counter.value) - 1;
    }
}
