var sakai = sakai ||
{};
var utils = utils ||
{};

 
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
            content = ("<h4><span id=\'owner\'></span>" + email + "</h4>" + "<br /><p class=\'textPanelFooter\' id=\'shortdesc\'>" + $(shortdesc).text() + "</p><br />" + "<div class=\"textPanel\">" + desc + "</div>");
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
    
    if ($('#' + list + ' td :checkbox').length === 0) {
        $('#' + allcontrol).hide();
    }
    $('#' + allcontrol).click(function(){
        if (this.checked) {
            $('#' + list + ' :checkbox').attr('checked', 'checked');
            $('#' + list + ' :checkbox').parent('td').parent('tr').addClass(highlightClass);
        }
        else {
            $('#' + list + ' :checkbox').attr('checked', '');
            $('#' + list + ' tbody tr').removeClass(highlightClass);
        }
    });
    
    $('#' + list + ' :checkbox').click(function(){
        var someChecked = false;
        if (this.checked) {
            $(this).parents('tr').addClass(highlightClass);
        }
        else {
            $(this).parents('tr').removeClass(highlightClass);
        }
        $('#' + list + ' :checkbox').each(function(){
            if (this.checked) {
                someChecked = true;
            }
        });
        if (!someChecked) {
            $('#' + allcontrol).attr('checked', '');
        }
        if ($('#' + list + ' :checked').length !== $('#' + list + ' :checkbox').length) {
            $('#' + allcontrol).attr('checked', '');
        }
        
        if ($('#' + list + '  :checked').length === $('#' + list + '  :checkbox').length) {
            $('#' + allcontrol).attr('checked', 'checked');
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
     var courseSiteTypes = $('#courseSiteTypes').val().replace('[','').replace(']','').replace(' ','').split(',');
    
    //uncheck site type radio
    $('input[name="itemType"]').attr('checked', '');
    
    // handles clicking in "Build site from template"
    $('#copy').click(function(e){
        //open template picker
        $('#templateSettings').show();
        //uncheck build own option
        $('#buildOwn').attr('checked', '');
        //hide the list of sites availabel when building own
        $('#siteTypeList').hide();
        //hide the term select used when selecting a course when building own
        $('#termList').hide();
        utils.resizeFrame('grow');
        //show submit button used when using templates 
        $('#submitFromTemplate').show();
        //show submit button used when building own, disable it
        $('#submitBuildOwn').hide();
        $('#submitBuildOwn').attr('disabled', 'disabled');
        //TODO: why? commenting out for now
        //$('#copyContent').attr('checked', 'checked');
    });
    
    $('#buildOwn').click(function(e){
        //hide template picker
        $('#templateSettings').hide();
        //uncheck any checked inputs in the template picker
        $('#templateSettings input:checked').attr('checked', '');
        //hide template inner container for title/term selection, "copy users" etc.
        $('#allTemplateSettings').hide();
        //void the template title and reset the term selection
        $('#siteTitleField').attr('value', '');
        $('#templateSettings select').attr('selectedIndex', 0);
        //hide the template containers for both title and term selection
        $('#templateSettingsTitleTerm span.templateTitleTerm').hide();
        //uncheck the "Create site from template" radio
        $('input[id="copy"]').attr('checked', '');
        // show the build own choices
        $('#siteTypeList').show();
        //hide the submit for creating from template and disable it
        $('#submitFromTemplate').hide().attr('disabled', 'disabled');
        // hide the submit for creating a course from template, in case it was showing
        $('#submitFromTemplateCourse').hide();
        //show the submit for build own
        $('#submitBuildOwn').show();
        utils.resizeFrame('grow');
    });
    // check for a value in the create from template non-course title 
    // field and either enable or disable the submit, also check onblur below
    $('#siteTitleField').keyup(function(e){
        if ($(this).attr('value').length >= 1) {
            $('#submitFromTemplate').attr('disabled', '');
        }
        else {
            $('#submitFromTemplate').attr('disabled', 'disabled');
        }
    });
    $('#siteTitleField').blur(function(){
        if ($(this).attr('value').length >= 1) {
            $('#submitFromTemplate').attr('disabled', '');
        }
        else {
            $('#submitFromTemplate').attr('disabled', 'disabled');
        }
    });
    
    // check that user has picked a term in the term selection field
    //to enable or disable submits for create course from template
    $('#selectTermTemplate').change(function(){
        if (this.selectedIndex === 0) {
            $('#submitFromTemplateCourse').attr('disabled', 'disabled');
        }
        else {
            $('#submitFromTemplateCourse').attr('disabled', '');
            
        }
    });
    
    // handler that opens a block explaining what all the options are 
    // in the template selection (copy users, content, publish now)
    // TODO: What explanations get shown should be a factor of what controls are showing 
    $('#fromTemplateSettingsContainer_instruction_control').click(function(){
        var pos = $(this).position();
        $('#fromTemplateSettingsContainer_instruction_body').css({'top': pos.top - 140,'left': pos.left - 290}).toggle();
    });
    // handler to 
    $('#fromTemplateSettingsContainer_instruction_body').click(function(){
        $(this).fadeOut('slow');
    });
    
    // handler for the template picker radio
    $('#templateList input').click(function(e){
        //what is the ID of the template site
        var selectedTemplateId = $('#templateList input[type="radio"]:checked').val();

        if (!selectedTemplateId){  // how likely is this? 
            $('#templateSettingsTitleTerm span').hide(); // hide title for non-course sites
            $('#submitFromTemplateCourse, #submitFromTemplateCourse ').attr('disabled', 'disabled'); //disable submit to create from templates
            $('#siteTitleField').attr('value', ''); // empty title input
            $('#siteTerms select').attr('selectedIndex', 0); // zero out the term select
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
            $('#publishSiteWrapper input').attr('checked', '');

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
                            $('#copyContentWrapper input').attr('checked', 'checked');
                        }
                        else {
                            $('#copyContentWrapper input').attr('checked', '')
                        }
                        if (this.copyContentLocked === true) {
                            $('#copyContentWrapper input').attr('disabled', 'disabled');
                        }
                        else {
                            $('#copyContentWrapper input').attr('disabled', '');
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
                            $('#copyUsersWrapper input').attr('checked', 'checked');
                        }
                        else {
                            $('#copyUsersWrapper input').attr('checked', '')
                        }
                        if (this.copyUsersLocked === true) {
                            $('#copyUsersWrapper input').attr('disabled', 'disabled');
                        }
                        else {
                            $('#copyUsersWrapper input').attr('disabled', '');
                        }
                    }
                });
            }
            else {
                //show all the controls, unchecked, unlocked, since there are no settings
                $('#copyContentWrapper').show().find('input').attr('disabled', '').attr('checked','');
                $('#copyUsersWrapper').show().find('input').attr('disabled', '').attr('checked','');
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
                $('#siteTerms select').attr('selectedIndex', 0); // zero out the term select
                $('#siteTitle input[type="text"]').focus(); // focus the title input
            }
      }
    });
    
    // populate the blurbs about each type from the json
    $.each(templateControls.templateControls, function(key, value){
        $('.' + key).find('.siteTypeRowBlurb').html(this.blurb);
    });

    // handles clicking on a category (course, project, whatever)
    // opens the list in the category, does clean up (closes other categories, resets control UI)
    $('.siteTypeRow a').click(function(e) {
        e.preventDefault();
        // hide the submit for a course creation via template
       $('#submitFromTemplateCourse').hide();
       //disable and show the generic submit for creating from template
       $('#submitFromTemplate').attr('disabled','disabled').show();
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
        $('#submitBuildOwn').attr('disabled', '');
        
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

