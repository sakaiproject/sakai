var sakai = sakai || {};
var utils = utils || {};
var selTools = new Array();
var ltiPrefix = "lti_";
 
$.ajaxSetup({
  cache: false
});

/*
 calling template has dom placeholder for dialog,
 args:class of trigger, id of dialog, message strings
 */
sakai.getSiteInfo = function(trigger, dialogTarget, nosd, nold){
    utils.startDialog(dialogTarget);
    $("." + trigger).click(function(e){
        var siteURL = '/direct/site/' + $(this).attr('id') + '.json';
        jQuery.getJSON(siteURL, function(data){
            var desc = '', shortdesc = '', title = '', owner = '', email = '';
            if (data.description) {
                desc = unescape(data.description);
            }
            else {
                desc = nold;
            }
            if (data.shortDescription) {
                shortdesc = data.shortDescription;
            }
            else {
                shortdesc = nosd;
            }
            
            if (data.props) {
                if (data.props['contact-name']) {
                    owner = data.props['contact-name'];
                }
                
                if (data.props['contact-email']) {
                    email = " (<a href=\"mailto:" + data.props['contact-email'].escapeHTML() + "\" id=\"email\">" + data.props['contact-email'].escapeHTML() + "</a>)";
                }
            }
            sitetitle = data.title.escapeHTML();
            content = ("<h4><span id=\'owner\'></span>" + email + "</h4>" + "<br /><p class=\'textPanelFooter\' id=\'shortdesc\'>" + shortdesc.escapeHTML() + "</p><br />" + "<div class=\"textPanel\">" + desc + "</div>");
            $("#" + dialogTarget).html(content);
            $("#" + dialogTarget + ' #shortdesc').text(shortdesc);
            $("#" + dialogTarget + ' #owner').text(owner);
            $("#" + dialogTarget).dialog('option', 'title', sitetitle);
            utils.endDialog(e, dialogTarget);
            return false;
        });
        
        
    });
};


/*
 calling template has dom placeholder for dialog,
 args:class of trigger, id of dialog, message strings
 */
sakai.getGroupInfo = function(trigger, dialogTarget, memberstr, printstr, tablestr1,tablestr2,tablestr3){
    utils.startDialog(dialogTarget);
	$('.' + trigger).click(function(e){
		
        var id = $(this).attr('id');
        var title = $('#group' + id).html();
        var groupURL = '/direct/membership/group/' + id + '.json';
        var list = "";
        var count = 1;
        
        jQuery.getJSON(groupURL, function(data){
            $.each(data.membership_collection, function(i, item){
                list = list + "<tr><td>" + count + ")&nbsp;" + item.userSortName + "</td><td>" + item.memberRole + "</td><td><a href=\'mailto:" + item.userEmail + "\'>" + item.userEmail + "</a></td></tr>";
                count = count + 1;
            });
            content = ("<h4>(<a  href=\"#\" id=\'printme\' class=\'print-window\' onclick=\'printPreview(\"/direct/membership/group/" + id + ".json\")\'>" + printstr + "</a>)</h4>" + "<p class=\'textPanelFooter\'></p>" + "<div class=\'textPanel\'><div id=\'groupListContent\'><table class=\'listHier lines nolines\' border=\'0\'><tr><th>" + tablestr1 + "</th><th>" + tablestr2 + "</th><th>" + tablestr3 + "</th>" + list + "</table></div>");
            $("#" + dialogTarget).html(content);
            $("#" + dialogTarget).dialog('option', 'title', memberstr + ': ' + title);
            utils.endDialog(e, dialogTarget);
            return false;
        });
    });
};

/*
 if message exists fade it in, apply the class, then hide
 args: message box id, class to apply
 */
sakai.setupMessageListener = function(messageHolder, messageMode){
    //test to see if there is an actual message (trim whitespace first)
    var str = $("#" + messageHolder).text();
    str = jQuery.trim(str);
    // show if message is there, then hide it
    if (str !== '') {
        $("#" + messageHolder).fadeIn('slow');
        $("#" + messageHolder).addClass(messageMode);
        $("#" + messageHolder).animate({
            opacity: 1.0
        }, 5000);
        $("#" + messageHolder).fadeOut('slow', function(){
            $("#" + messageHolder).remove();
        });
    }
};

/*
 a list with checkboxes, selecting/unselecting checkbox applies/removes class from row,
 selecting top checkbox selelects/unselects all, top checkbox is hidden if there are no
 selectable items, onload, rows with selected checkboxes are highlighted with class
 args: id of table, id of select all checkbox, highlight row class
 */
sakai.setupSelectList = function(list, allcontrol, highlightClass){
    $('#' + list + ' :checked').parent("td").parent("tr").addClass(highlightClass);
    
    if ($('#' + list + ' td input:checkbox').length === 0) {
        $('#' + allcontrol).hide();
    }
    $('#' + allcontrol).click(function(){
        if (this.checked) {
            $('#' + list + ' input:checkbox').prop('checked', true);
            $('#' + list + ' input:checkbox').parent('td').parent('tr').addClass(highlightClass);
        }
        else {
            $('#' + list + ' input:checkbox').prop('checked', false);
            $('#' + list + ' tbody tr').removeClass(highlightClass);
        }
    });
    
    $('#' + list + ' input:checkbox').click(function(){
        var someChecked = false;
        if (this.checked) {
            $(this).parents('tr').addClass(highlightClass);
        }
        else {
            $(this).parents('tr').removeClass(highlightClass);
        }
        $('#' + list + ' input:checkbox').each(function(){
            if (this.checked) {
                someChecked = true;
            }
        });
        if (!someChecked) {
            $('#' + allcontrol).prop('checked', false);
        }
        if ($('#' + list + ' :checked').length !== $('#' + list + ' input:checkbox').length) {
            $('#' + allcontrol).prop('checked', false);
        }
        
        if ($('#' + list + '  :checked').length === $('#' + list + '  input:checkbox').length) {
            $('#' + allcontrol).prop('checked', true);
        }
    });
};

sakai.siteTypeSetup = function(){
    var templateControls='';
    //from sakai.properties - json with what controls to display (and in what state) for each site type    
    if ($('#templateControls').val() !== '') {
        templateControls = eval('(' + $('#templateControls').val() + ')');
    }
    else {
        templateControls =='';
    }
     //the #courseSiteTypes input[type=text] contains what site types are associated with the course category
     // if there are none associated in sakai.properties, the value will be just one ('course')
     var courseSiteTypes = $('#courseSiteTypes').val().replace('[','').replace(']','').replace(/ /gi, '').split(',');
     
    //uncheck site type radio
    $('input[name="itemType"]').prop('checked', false);
    
    // handles clicking in "Build site from template"
    $('#copy').click(function(e){
        //open template picker
        $('#templateSettings').show();
        //uncheck build own option
        $('#buildOwn').prop('checked', false);
        //hide the list of sites availabel when building own
        $('#siteTypeList').hide();
        //hide the term select used when selecting a course when building own
        $('#termList').hide();
        utils.resizeFrame('grow');
        //show submit button used when using templates 
        $('#submitFromTemplate').show();
        //show submit button used when building own, disable it
        $('#submitBuildOwn').hide();
        $('#submitBuildOwn').prop('disabled', true);
        //TODO: why? commenting out for now
        //$('#copyContent').prop('checked', true);
    });
    
    $('#buildOwn').click(function(e){
        //hide template picker
        $('#templateSettings').hide();
        //uncheck any checked inputs in the template picker
        $('#templateSettings input:checked').prop('checked', false);
        //hide template inner container for title/term selection, "copy users" etc.
        $('#allTemplateSettings').hide();
        //void the template title and reset the term selection
        $('#siteTitleField').attr('value', '');
        $('#templateSettings select').prop('selectedIndex', 0);
        //hide the template containers for both title and term selection
        $('#templateSettingsTitleTerm span.templateTitleTerm').hide();
        //uncheck the "Create site from template" radio
        $('input[id="copy"]').prop('checked', false);
        // show the build own choices
        $('#siteTypeList').show();
        //hide the submit for creating from template and disable it
        $('#submitFromTemplate').hide().prop('disabled', true);
        // hide the submit for creating a course from template, in case it was showing
        $('#submitFromTemplateCourse').hide();
        //show the submit for build own
        $('#submitBuildOwn').show();
        utils.resizeFrame('grow');
    });
    // check for a value in the create from template non-course title 
    // field and either enable or disable the submit, also check onblur below
    $('#siteTitleField').keyup(function(e){
        if ($(this).val().length >= 1) {
            $('#submitFromTemplate').attr('disabled', false);
        }
        else {
            $('#submitFromTemplate').attr('disabled', true);
        }
    });
    $('#siteTitleField').blur(function(){
        if ($(this).val().length >= 1) {
            $('#submitFromTemplate').prop('disabled', false);
        }
        else {
            $('#submitFromTemplate').prop('disabled', true);
        }
    });
    
    // check that user has picked a term in the term selection field
    //to enable or disable submits for create course from template
    $('#selectTermTemplate').change(function(){
        if (this.selectedIndex === 0) {
            $('#submitFromTemplateCourse').prop('disabled', true);
        }
        else {
            $('#submitFromTemplateCourse').prop('disabled', false);
            
        }
    });
    
    // handler that opens a block explaining what all the options are 
    // in the template selection (copy users, content, publish now)
    $('#fromTemplateSettingsContainer_instruction_control').click(function(){
        var pos = $(this).position();
        varContainerHeight = $('#fromTemplateSettingsContainer_instruction_body').height();
        $('#fromTemplateSettingsContainer_instruction_body').css({'top': pos.top - varContainerHeight - 20,'left': pos.left - 290}).toggle();
    });
    // handler to 
    $('#fromTemplateSettingsContainer_instruction_body').click(function(){
        $(this).fadeOut('slow');
    });
    
    // handler for the template picker radio
    $('#templateList input').click(function(e){
        //what is the ID of the template site
        var selectedTemplateId = $('#templateList input[type="radio"]:checked').attr('id');

        if (!selectedTemplateId){  // how likely is this? 
            $('#templateSettingsTitleTerm span').hide(); // hide title for non-course sites
            $('#submitFromTemplateCourse, #submitFromTemplateCourse ').prop('disabled', true); //disable submit to create from templates
            $('#siteTitleField').attr('value', ''); // empty title input
            $('#siteTerms select').prop('selectedIndex', 0); // zero out the term select
        }
        else {
            // what is the site type of the template site
            var type = $('#templateList input[type="radio"]:checked').attr('class');
            $('#templateSettingsTitleTerm span.templateTitleTerm').hide(); // hide template term selection and title input controls
            $('#templateList li').removeClass('selectedTemplate'); // remove hightlights from all template rows
             $('#templateList #row' + selectedTemplateId).addClass('selectedTemplate'); // add highlight to selected row
             $('#allTemplateSettings').addClass('allTemplateSettingsHighlight');
             // move in the DOM the template settings to this row
            $('#templateList #row' + selectedTemplateId  + ' .templateSettingsPlaceholder').append($('#allTemplateSettings'));
            // hide instructions for settings
            $('#fromTemplateSettingsContainer_instruction_body').hide();
            $('#publishSiteWrapper input').prop('checked', false);

            //templateControls is a json that comes from a sakai.property
            //it identifies for each site type, whether to show a control, and what attrs it has. 
            if (templateControls !== '') {
                $.each(templateControls.templateControls, function(key, value){
                    if (key === type) {
                        if (this.copyContentVis === true) {
                            $('#copyContentWrapper').show();
                            $('#fromTemplateSettingsContainer_instruction_body_copyUsers').show();
                        }
                        else {
                            $('#copyContentWrapper').hide();
                            $('#fromTemplateSettingsContainer_instruction_body_copyUsers').hide();
                        }
                        if (this.copyContentChecked === true) {
                            $('#copyContentWrapper input').prop('checked', true);
                        }
                        else {
                            $('#copyContentWrapper input').prop('checked', false)
                        }
                        //SAK25401
                        if (this.copyContentLocked === true) {
                           if (this.copyContentLocked === true) {
                            // SAK-25401 hide the actual control, display locked 'stand-in'
                            $( "#copyContent" ).hide();
                            $( "#lockedContent").show();
                           }
                        }

                        else {
                            $('#copyContentWrapper input').prop('disabled', false);
                        }
                        if (this.copyUsersVis === true) {
                            $('#copyUsersWrapper').show();
                            $('#fromTemplateSettingsContainer_instruction_body_copyContent').show();
                        }
                        else {
                            $('#copyUsersWrapper').hide();
                            $('#fromTemplateSettingsContainer_instruction_body_copyContent').hide();
                        }
                        if (this.copyUsersChecked === true) {
                            $('#copyUsersWrapper input').prop('checked', true);
                        }
                        else {
                            $('#copyUsersWrapper input').prop('checked', false)
                        }
                        if (this.copyUsersLocked === true) {
                            $('#copyUsersWrapper input').prop('disabled', true);
                        }
                        else {
                            $('#copyUsersWrapper input').prop('disabled', false);
                        }
                    }
                });
            }
            else {
                //show all the controls, unchecked, unlocked, since there are no settings
                $('#copyContentWrapper').show().find('input').prop('disabled', false).prop('checked',false);
                $('#copyUsersWrapper').show().find('input').prop('disabled', false).prop('checked',false);
                $('#fromTemplateSettingsContainer_instruction_body_copyUsers').show();
                $('#fromTemplateSettingsContainer_instruction_body_copyContent').show();
            }
            
            // show settings
            $('#allTemplateSettings').fadeIn('slow');
            //check to see if this template is of a type that maps to a course
            if ($.inArray(type, courseSiteTypes) !==-1) { //either there is a mapping to what types of sites resolve to courses or a fallback to 'course'  
                $('#submitFromTemplate').hide(); // hide the non-course submit button 
                $('#submitFromTemplateCourse').show(); // show the submit button for course
                $('#siteTerms').show(); // show the term selector
                $('#siteTitle').hide(); // hide the title input (Note: can an installation specify that a course can have a user generated title)?
                $('#siteTerms select').focus(); // focus the term select control
                $('#siteTitleField').attr('value', ''); // void the value of the title input
            }
            // the picked template has a type that does not resolve to a course
            else { 
                $('#submitFromTemplate').show(); // show non-course submit button
                $('#submitFromTemplateCourse').hide(); // hide the course submit button
                $('#siteTitle').show(); //show title input
                $('#siteTerms').hide();//hide the container that holds the site terms
                $('#siteTerms select').prop('selectedIndex', 0); // zero out the term select
                $('#siteTitle input[type="text"]').focus(); // focus the title input
            }
      }
    });
    
    // populate the blurbs about each type from the json
    if (templateControls !== '') { 
        $.each(templateControls.templateControls, function(key, value){
          $('.' + key).find('.siteTypeRowBlurb').html(this.blurb);
        });
    }

    // handles clicking on a category (course, project, whatever)
    // opens the list in the category, does clean up (closes other categories, resets control UI)
    $('.siteTypeRow a').click(function(e) {
        e.preventDefault();
        // hide the submit for a course creation via template
       $('#submitFromTemplateCourse').hide();
       //disable and show the generic submit for creating from template
       $('#submitFromTemplate').prop('disabled', true).show();
       // clean up - hide all rows
       $('li[class^=row]').hide();
       // toggle the UI of all the category links
       $('.siteTypeRow a .open').hide();
       $('.siteTypeRow a .closed').show();
       // reset all categories control UI to "closed" 
        $('.siteTypeRow a').removeClass('openDisc');
        $(this).toggleClass('openDisc');
       // display all rows belonging to this category
       $('.row' + $(this).attr('href')).fadeToggle();
       // set new category control UI some more
       $(this).find('.closed').hide();
       $(this).find('.open').show(); 
       utils.resizeFrame('grow');
    });
    
    // this handles selections on the site type list (trad course, project, portfolio, etc.)
    $('#siteTypeList input').click(function(e){
        if ($(this).attr('id') == 'course') {
            $('#termList').show();
        }
        else {
            $('#termList').hide();
        }
        $('#submitBuildOwn').prop('disabled', false);
        
    });
};

sakai.setupToggleAreas = function(toggler, togglee, openInit, speed){
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
        utils.resizeFrame();
    }
    $('.' + toggler).click(function(){
        $(this).next('.' + togglee).fadeToggle(speed);
        $(this).find('.expand').toggle();
        $(this).find('.collapse').toggle();
        utils.resizeFrame();
    });
};

/*
 utilities
 */
/*
 initialize a jQuery-UI dialog
 */

utils.setupUtils= function(){
    $('.revealInstructions').click(function(e){
		e.preventDefault();
        $(this).hide().next().fadeIn('fast');
    });
}; 
utils.startDialog = function(dialogTarget){
    $("#" + dialogTarget).dialog({
        close: function(event, ui){
            utils.resizeFrame('shrink');
        },
        autoOpen: false,
        modal: true,
        height: 330,
		maxHeight:350,
        width: 500,
        draggable: true,
        closeOnEscape: true
    });
    
};
/*
 position, open a jQuery-UI dialog, adjust the parent iframe size if any
 */
utils.endDialog = function(ev, dialogTarget){
    var frame;
    if (top.location !== self.location) {
        frame = parent.document.getElementById(window.name);
    }
    if (frame) {
        var clientH = document.body.clientHeight + 360;
        $(frame).height(clientH);
    }

    $("#" + dialogTarget).dialog('option', 'position', [100, ev.pageY + 10]);
    $("#" + dialogTarget).dialog("open");

};


// toggle a fade
jQuery.fn.fadeToggle = function(speed, easing, callback){
    return this.animate({
        opacity: 'toggle'
    }, speed, easing, callback);
};
//escape markup
String.prototype.escapeHTML = function(){
    return (this.replace(/&/g, '&amp;').replace(/>/g, '&gt;').replace(/</g, '&lt;').replace(/"/g, '&quot;'));
};

/*
 resize the iframe based on the contained document height.
 used after DOM operations that add or substract to the doc height
 */
utils.resizeFrame = function(updown){
    var clientH;
    if (top.location !== self.location) {
        var frame = parent.document.getElementById(window.name);
    }
    if (frame) {
        if (updown === 'shrink') {
            clientH = document.body.clientHeight;
        }
        else {
            clientH = document.body.clientHeight + 50;
        }
        $(frame).height(clientH);
    }
    else {
        // throw( "resizeFrame did not get the frame (using name=" + window.name + ")" );
    }
};

// SAK-23905
var setLTIPrefix = function(ltipref) {
        ltiPrefix = ltipref;
};



var setupCategTools = function(){

   var sorttoolSelectionList = function(){
        var mylist = $('#toolSelectionList ul');
        var listitems = mylist.children('li').get();
        listitems.sort(function(a, b){
            return $(a).text().toUpperCase().localeCompare($(b).text().toUpperCase());
        });
        $.each(listitems, function(idx, itm){
            mylist.append(itm);
        });
        if ($('#toolSelectionList ul li').length > 1) {
            if ($('#toolSelectionList ul').find('li#selected_sakai_home').length) {
             $('#toolSelectionList ul').find('li#selected_sakai_home').insertBefore($('#toolSelectionList ul li:first-child'));
            }
        }
   };
    

   var noTools = function() {
        
        if ($('#toolSelectionList  ul li').length - 1 === 0)  {
            $('#toolSelectionList #toolSelectionListMessage').show();
        }
        else {
            $('#toolSelectionList #toolSelectionListMessage').hide();
        }
};
   var showAlert = function(e){
        var pos = $(e.target).position();
        var msg = $('#removalAlertMessage').val();
        var yesMsg = $('#removalAlertYesMessage').val();
        var noMsg = $('#removalAlertNoMessage').val();
        $(e.target).parent('li').append('<div id=\"alertBox\">' + msg + '<a href=\"#\" id=\"alertBoxYes\">' + yesMsg + '</a>&nbsp;|&nbsp;<a href=\"#\" id=\"alertBoxNo\">' + noMsg + '</a></div>');
        $(e.target).find('#alertBox').css({
            'top': pos.top - 14,
            'left': pos.left - 150
        });
        $('#alertBox a#alertBoxYes').on('click', function(){
            $(this).parent('div').prev('a').removeClass('toolInstance').click();
            $('#alertBox').remove();
        });
        $('#alertBox a#alertBoxNo').on('click', function(){
            $(this).closest('li').removeClass('highlightTool');
            $('#alertBox').remove();
        });
    };
    
    // replace characters in tool id received from backend to allow proper DOM selection by id of target tool
    // e.g. all '.''s are replaced by _
    function normalizedId(myId) {
    	var normId = myId.replace(/\./g, '_');
    	return normId;
    } 
   
    // SAK23905 adjust for different id's between toolHolder and toolSelection (except for LTI tools) 
    function denormalizeId(myId) {
	if (myId.lastIndexOf(ltiPrefix, 0) < 0) {
		myId = myId.replace(/\_/g, '.');
	}
	return myId;
    }

    // SAK-16normalizedIde additional tool multiple identification characters (i.e. those beyond the base tool's name) with 
    // underscores to allow proper selection of the tool's associated icon 
    function iconizedId(myId) {
        if (myId.length > 36){
            myId = myId.substring(36);            
        }
    	var iconId = myId.replace(/\./g, '-') ;
    	return iconId;
    }

      function hideToolSelection(myId,delaySeconds){
        var selectionTarget = $('#toolSelectionList').find('#' + normalizedId(myId));
        selectionTarget.addClass('highlightTool').fadeOut(delaySeconds, function(){
                $(this).hide();
                $(this).removeClass('highlightTool');
          });
        $('#toolHolder').find('input[type="checkbox"][id="' + denormalizeId(myId) + '"]').attr("checked",false);
      }

      function showToolSelection(myId,delaySeconds) {
        var selectionTarget  = $('#toolSelectionList').find('#'+normalizedId(myId));
        selectionTarget.addClass('highlightTool').fadeIn(delaySeconds, function(){
          $(this).show();
            $(this).removeClass('highlightTool');
        });
        $('#toolHolder').find('input[type="checkbox"][id="' + denormalizeId(myId) + '"]').attr("checked",true);
      }

      // SAK-16600
      // hide or display an item.  ignore if item is disabled
      function setChecked(myId,checkVal){
       var item = $('#toolHolder').find('input[id="' + denormalizeId(myId) + '"]');
        // ignore if item is disabled
        myId = myId.replace(/\./g,"\\.");
        if (!item.is(':disabled')) {
                if (checkVal== true){
                        sorttoolSelectionList();
                       showToolSelection(myId,200);
                       // make toolholder text bold
                       $('#toolHolder').find('input#' + denormalizeId(myId)).prop('checked', 'checked').next('label').css('font-weight', 'bold');
                       
                } else {
                        hideToolSelection(myId,100);
                        // make toolholder text normal
                        $('#toolHolder').find('input#' + denormalizeId(myId)).next('label').css('font-weight', 'normal');
                }
                // toggle checked
                $('#toolHolder').find('input#' + myId).attr("checked",checkVal);
        }

      }

    // loop through list of tools; creating entry for each unique instance and hiding unselected
    var sourceList = $('input[name="selectedTools"][type="checkbox"]');
    $.each(sourceList, function(){
        var removeLink = '';
        var currentLink = '';
        var thisToolCat = '';
        var thisIdClass = '';
        var toolInstance = '';
        var thisToolCatEsc = '';
        var thisToolId = normalizedId($(this).attr('id'));
	
        if (thisToolId.length > 37) {
            thisToolCat = thisToolId.substring(36) + '';
            thisIdClass = thisToolId.substring(36) + '';
            toolInstance = ' toolInstance';
        }
        else {
            thisToolCat = thisToolId + '';
            thisIdClass = thisToolId + '';
        }
        thisToolCatEsc = thisToolCat.replace(' ', '_');

        // ignore duplicates already found in array
        var idx=selTools.indexOf(thisToolId);
        if (idx < 0) {
        	selTools.push(thisToolId);

        	// selectedTools with disable checkboxes don't have the red [X] remove link
        	if ($(this).prop('disabled') !== true) {
        		removeLink = '<a href="#" class=\"removeTool ' + toolInstance + '\">x</a>';
        	}

    		var selId = normalizedId($(this).attr('id'));
                var iconId = iconizedId($(this).attr('id'));
                $('#toolSelectionList ul').append('<li class=\"icon-' + iconId + '\" id=\"' + thisToolId + '\">' + $(this).next('label').text() + removeLink + '</li>');
        	// append to selected tool list
        	if ($(this).prop('checked')) {
        		// make the selectiion visible
        		//var selId = normalizedId($(this).attr('id'));
        		var iconId = iconizedId($(this).attr('id'));
        		$(this).next('label').css('font-weight', 'bold');
        		//$('#toolHolder').find('#' + thisToolCatEsc).find('ul').show();
        		//$('#toolHolder').find('#' + thisToolCatEsc).find('h4').find('a').addClass('open');
                       showToolSelection(selId,0);
        	}
        	else {
                       hideToolSelection(selId,0);
        		$(this).next('label').css('font-weight', 'normal');
        	}
        	var parentRow = $(this).closest('li');
        	$('#toolHolder').find('#' + thisToolCatEsc).find('ul').append(parentRow);
        }
    });
    
    // set checked/unchecked for each in list
    $('.toolGroup').each(function(){
        var countChecked = $(this).find(':checked').length;
        var countTotal = $(this).find('input[type="checkbox"]').length;
        if (countChecked === 0) {
            $(this).parent('li').find('#selectAll').show();
            $(this).parent('li').find('#unSelectAll').hide();
        }
        if (countChecked === countTotal) {
            $(this).parent('li').find('#selectAll').hide();
            $(this).parent('li').find('#unSelectAll').show();
        }
        if (countChecked !==  0 && countChecked !== countTotal) {
            $(this).parent('li').find('#selectAll').hide();
            $(this).parent('li').find('#unSelectAll').show();
        }
        $(this).parent('li').find('span.checkedCount').text(countChecked).show(); //$(this).parent('li').find('span.checkedCount').hide();
    });
    
    $('#toolHolder a').click(function(e){
        e.preventDefault();
        if ($(this).attr('href')) {
            $(this).closest('li').find('ul').fadeToggle('fast', function(){
                utils.resizeFrame('grow');
            });
            $(this).toggleClass('open');
            return false;
        }
    });
    
    // 
    $('input[name="selectedTools"][type="checkbox"]').click(function(){
        if(($(this).closest('ul').find(':checked').length === $(this).closest('ul').find('input[type="checkbox"]').length) && $(this).closest('ul').find(':checked').length > 0) {
            $('#selectAll').hide();
            $('#unSelectAll').show();
        }
        else {
            $('#selectAll').show();
            $('#unSelectAll').hide();
            
        }
        var count = $(this).closest('ul').find(':checked').length;
        $(this).closest('ul').parent('li').find('span.checkedCount').text(count).show();
        if ($(this).attr('id').length > 37) {
            thisIdClass = $(this).attr('id').substring(36) + '';
        }
        else {
            thisIdClass = $(this).attr('id') + '';
        }
        var chkVal = $(this).prop('checked');
        var myId = $(this).attr('id');
        setChecked(myId, chkVal);
        utils.resizeFrame('grow');
        noTools();
    });

    $('#collExpContainer a#expandAll').click(function(e){
        $('#toolHolder .toolGroup').not(':eq(0)').show();
        $('#toolHolder h4 a').addClass('open');
        $('#collExpContainer a').toggle();
        utils.resizeFrame('grow');
        return false;
    });
    $('#collExpContainer a#contractAll').click(function(e){
        $('#toolHolder .toolGroup').not(':eq(0)').hide();
        $('#toolHolder h4 a').removeClass('open');
        $('#collExpContainer a').toggle();
        utils.resizeFrame('grow');
        return false;
    });    
    $('.selectAll').click(function(){
        if ($(this).attr('id') === "selectAll") {
            $('.sel_unsel_core em').hide();
            $('.sel_unsel_core em#unSelectAll').show();
            $.each($(this).closest('li').find('input[type="checkbox"]'), function(){
        		setChecked($(this).attr('id'),true);
            });
            utils.resizeFrame('grow');
            //setupCategTools();
        }
        else {
            $('.sel_unsel_core em').hide();
            $('.sel_unsel_core em#selectAll').show();
            $.each($(this).closest('li').find(':checked'), function(){
                setChecked($(this).attr('id'),false);
            });
            utils.resizeFrame('grow');
        }
        $(this).closest('li').find('span.checkedCount').text($(this).closest('li').find(':checked').length).show(); 
    });
    
     // Clicked red X on toolSelectionList
      $('.removeTool').click(function(e){
          e.preventDefault();
         var selectedId = $(this).closest('li').attr('id').replace('selected.','');
                // if toolMultple; confirm delete
         if ($(this).hasClass('toolInstance')) {
                $(this).closest('li').addClass('highlightTool');
                showAlert(e);
                return false;
                // remove the checkbox? put in an alert
         } else {
                // for each tool with this id, set check to false and fade in/out selectedTool display
                setChecked(selectedId,false);
         }
         var countSelected = $('#toolHolder').find('input[type="checkbox"][value="' + selectedId + '"]').closest('ul').find(':checked').length;
         $('#toolHolder').find('input[type="checkbox"][id="' + selectedId + '"]').closest('ul').closest('li').find('.checkedCount').text(countSelected);

          noTools();
      });    
 
    $('.moreInfoTool').click(function(e){
        e.preventDefault();
        var moreInfoTitle = this.getAttribute("title");
        var moreInfoSrc = this.getAttribute("href");
        $('#moreInfoHolder').load(moreInfoSrc);
        $("#moreInfoHolder").dialog({
            autoOpen: false,
            height: 500,
            maxHeight: 500,
            maxWidth: 700,
            width: 700,
            title: moreInfoTitle,
            modal: true
        });
        $("span.ui-dialog-title").text(moreInfoTitle);
        $('#moreInfoHolder').dialog('open');
    });

    if ($('.toolGroup').length <= 2) {
        $('.sel_unsel_core, #collExpContainer').hide();
    }
};

var setupRecentSite = function(){

    var target = $('#newSiteAlert').attr('class');

    if (sessionStorage.getItem(target)) {
        $('#newSiteAlert').hide();
    }
  
    $('.newSiteAlertClose').click(function(e){
        e.preventDefault();
        $(this).closest('div').fadeOut('slow');
        sessionStorage.setItem(target, true);
    })

    $('#newSiteAlertPublish').click(function(e){
        e.preventDefault();
        var reqUrl = '/direct/site/' + target + '/edit';
        jQuery.ajax({
            type: 'POST',
            data: 'published=true',
            url: reqUrl,
            contentType: 'application/x-www-form-urlencoded; charset=UTF-8',
            success: function(){
                $('#newSiteAlertPublish').fadeOut('slow', function(){
                    $('#' + target).closest('tr').addClass('selectedSelected')
                    $('#newSiteAlertPublishMess').fadeIn('5000');
                    $('#' + target).closest('tr').fadeOut('slow', function(){
                        var publishedString = $('#newSiteAlertPublishMess').text();
                        $(this).find('td[headers="published"]').text(publishedString).css('font-weight', 'bold');
                        $(this).fadeIn('1000');
                    });
                });
                
            }
        });
        
        
    })
}
if (!Array.prototype.indexOf) { 
    Array.prototype.indexOf = function(obj, start) {
         for (var i = (start || 0), j = this.length; i < j; i++) {
             if (this[i] === obj) { return i; }
         }
         return -1;
    }
}
