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
    $('#randomOption').click(function(){
        if (this.checked) {
            $("#randomGroupCreateDiv").fadeIn();
        }
        else {
            $("#randomGroupCreateDiv").fadeOut();
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
    $('#userSplit').click(function(){
        if (this.checked) {
            $('p').removeClass('optGroupSelectSelected');
            $(this).parent('p').addClass('optGroupSelectSelected');
            $('#userFields').slideDown();
            $('#groupFields').slideUp();
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
};

setupValidation = function(){


    $('#save').click(function(e){
        var validForm = true;
        if ($("#randomOption").prop('checked') == true) {
            if ($("#groupSplit").prop('checked') == true) {
                if ($('#groupTitle-group').val() === '') {
                    $('#groupTitle-group').parent('p').addClass('validationFail');
                    validForm = false;
                }
                else {
                    $('#groupTitle-group').parent('p').removeClass('validationFail');
                }
                if ($('#numToSplit-group').val() === '' || isNaN($('#numToSplit-group').val())) {
                    $('#numToSplit-group').parent('p').addClass('validationFail');
                    validForm = false;
                }
                else {
                    $('#numToSplit-group').parent('p').removeClass('validationFail');
                }
                
            }
            if ($("#userSplit").prop('checked') == true) {
                if ($('#groupTitle-user').val() === '') {
                    $('#groupTitle-user').parent('p').addClass('validationFail');
                    validForm = false;
                }
                else {
                    $('#groupTitle-user').parent('p').removeClass('validationFail');
                }
                
                if ($('#numToSplit-user').val() === '' || isNaN($('#numToSplit-user').val())) {
                    $('#numToSplit-user').parent('p').addClass('validationFail');
                    validForm = false;
                }
                else {
                    $('#numToSplit-user').parent('p').removeClass('validationFail');
                }
                
                
            }
        }
        if (validForm === false) {
            return false;
        }
        else{
            return true;
        }
    });
    
};

//this function needs jquery 1.1.2 or later - it resizes the parent iframe without bringing the scroll to the top
function resizeFrame(updown){
    var clientH;
    if (top.location != self.location) {
        var frame = parent.document.getElementById(window.name);
    }
    if (frame) {
        if (updown == 'shrink') {
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

function disableButtonsActivateSpinner( primaryActionID, secondaryActionID, spinnerID ) 
{
    // Clone & disable the buttons (disable the clone, hide the original)
    var btnPrimary = document.getElementById( primaryActionID );
    var btnSecondary = document.getElementById( secondaryActionID );
    var btnDelete = document.getElementById( "delete" );
    var btnGenerate = document.getElementById( "generate-row::generate" );
    var list = [btnPrimary, btnSecondary];
    
    if( btnDelete !== null )
    {
        list.push(btnDelete);
    }
    if( btnGenerate !== null )
    {
        list.push(btnGenerate);
    }
    
    for( i = 0; i < list.length; i++ )
    {
        
        var original = list[i];
        var clone = document.createElement( "input" );
        var parent = original.parentNode;
        clone.setAttribute( "type", "button" );
        clone.setAttribute( "id", original.getAttribute( "id" ) + "Disabled" );
        clone.setAttribute( "name", original.getAttribute( "name" ) + "Disabled" );
        clone.setAttribute( "className", original.getAttribute( "className" ) );
        clone.setAttribute( "disabled", "true" );
        clone.value = original.value;
        original.style.display = "none";
        parent.insertBefore( clone, original );
    }
    
    // Show the spinner
    document.getElementById( spinnerID ).style.visibility = "visible";
}
