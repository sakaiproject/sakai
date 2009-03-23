var sakai = sakai ||
{};
var utils = utils ||
{};

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
                    email = " (<a href=\"mailto:" + data.props['contact-email'] + "\">" + data.props['contact-email'] + "</a>)";
                }
            }
            sitetitle = unescape(data.title);
            content = ("<h4>" + owner + email + "</h4>" + "<p class=\'textPanelFooter\'>" + shortdesc + "</p>" + "<div class=\"textPanel\">" + desc + "</div>");
            $("#" + dialogTarget).html(content);
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
        var title = $('#group' + id).text();
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

