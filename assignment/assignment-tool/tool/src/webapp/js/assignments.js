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
                $('#allPurposeGroupLists input[@type=checkbox]').attr('checked', '');
                $('#allPurposeGroupLists label').removeClass('selectedItem');
                $('#allPurposeAttachShowWhen input[@type=checkbox]').attr('checked', '');
                $('#allPurposeAttachShowWhen #allPurposeHide1').attr('checked', 'checked');
                $('#allPurposeAttachShowWhen #allPurposeHide2').attr('checked', '');
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
        $(this).parents('.groupCell').find('.selectAllMembers').attr('checked', '');
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
            $(this).parents('.groupCell').children('.countDisplay').text($(this).parents('.groupCell').find('input').attr('checked', 'checked').length-1);
            $(this).parents('.groupCell').find('input').attr('checked', 'checked');
            
            $(this).parents('.groupCell').find('li label').addClass('selectedItem');
        }
        else {
            $(this).parents('.groupCell').children('.countDisplay').text('0');
            $(this).parents('.groupCell').find('input').attr('checked', '');
            $(this).parents('.groupCell').find('li label').removeClass('selectedItem');
        }
    });
    $(".groupCell").each(function(){
        if ($(this).find('input.selectAllMembers:checked').length) {
            $(this).children('.countDisplay').text($(this).find('input').attr('checked', 'checked').length);
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