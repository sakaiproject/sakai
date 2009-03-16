/*
 calling template has dom placeholder for dialog,
 args:class of trigger el,  message strings for no short description, no long description
 */
function setupGetSiteDesc(trigger, nosd, nold){
    $(document).keydown(function(e){
        if (e.which == 27) {
            $(".jqmWindow").jqmHide();
        }
    });
    $('#dialog').jqm({
        closeClass: 'jqModalCloseClass',
        onShow: function(h){
            h.w.fadeIn("fast", function(){
                if (h.o) {
                    resizeFrame('grow')
                };
                            });
        },
        onHide: function(h){
            h.w.fadeOut("fast", function(){
                if (h.o) {
                    h.o.remove();
                    resizeFrame('shrink')
                };
                            });
        }
    });
    
    $('.' + trigger).click(function(e){
        var topVal = (e.pageY - 50) + "px";
        var siteURL = '/direct/site/' + $(this).attr('id') + '.json';
        jQuery.getJSON(siteURL, function(data){
            var desc = '', shortdesc = '', title = '', owner = '', email = '', personInfo = '';
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
					owner = "(" + data.props['contact-name'] + ")"
				}
				
				if (data.props['contact-email']) {
					email = " <a href=\"mailto:" + data.props['contact-email'] + "\">" + data.props['contact-email'] + "</a>"
				}
			}
            title = unescape(data.title);
            $('#dialog #dialogChild').html("<h4>" + title + " <span class=\'textPanelFooter\'>" + owner + email + "</span></h4>" + "<p class=\'textPanelFooter\'>" + shortdesc + "</p>" + "<div class=\'textPanel  jqmWindowMain\'>" + desc + "</div>");
            
            $('#dialog').jqmShow();
            if ($("#dialogChild .jqmWindowMain").height() > 360) {
                $("#dialogChild .jqmWindowMain").addClass("jqmWindowMainOverFlow")
            }
            $('#dialog').css({
                'position': 'absolute',
                'top': topVal
            });
            e.preventDefault();
        });
        
    });
}


/*
 calling template has dom placeholder for dialog,
 args:class of trigger el,  message strings 
 */
function setupGetGroupInfo(trigger, memberstring, printstring){
    $(document).keydown(function(e){
        if (e.which == 27) {
            $(".jqmWindow").jqmHide();
        }
    });
    $('#dialog').jqm({
        closeClass: 'jqModalCloseClass',
        onShow: function(h){
            h.w.fadeIn("fast", function(){
                if (h.o) {
                    resizeFrame('grow')
                };
                            });
        },
        onHide: function(h){
            h.w.fadeOut("fast", function(){
                if (h.o) {
                    h.o.remove();
                    resizeFrame('shrink')
                };
                            });
        }
    });
    
    $('.' + trigger).click(function(e){
        var topVal = (e.pageY - 100) + "px";
        var id = $(this).attr('id');
        var groupURL = '/direct/membership/group/' + id + '.json';
        var list = "";
        var count = 1
        
        jQuery.getJSON(groupURL, function(data){
        
            $.each(data.membership_collection, function(i, item){
                list = list + "<tr><td>" + count + ") " + item.userSortName + "</td><td>" + item.memberRole + "</td><td><a href=\'mailto:" + item.userEmail + "\'>" + item.userEmail + "</a></td></tr>"
                count++
            });
            
            $('#dialog #dialogChild').html("<h4>" + memberstring + " (<a  href=\"#\" id='\printme\' class=\'print-window\' onclick=\'printPreview(\"/direct/membership/group/" + id + ".json\")\'>" + printstring + "</a>)</h4>" + "<p class=\'textPanelFooter\'></p>" + "<div class=\'textPanel  jqmWindowMain\'><div id=\'groupListContent\'><table class=\'listHier lines nolines\' border=\'0\'><tr><th>Member</th><th>Role</th><th>Email</th>" + list + "</table></div>");
            $('#dialog').jqmShow();
            if ($("#dialogChild .jqmWindowMain").height() > 360) {
                $("#dialogChild .jqmWindowMain").addClass("jqmWindowMainOverFlow")
            }
            $('#dialog').css({
                'position': 'absolute',
                'top': topVal
            });
            e.preventDefault();
        });
        
    });
}

/*
 if message exists fade it in, apply the class, then hide
 args: message box id, class to apply
 */
function setupMessageListener(messageHolder, messageMode){
    //test to see if there is an actual message (trim whitespace first)
    var str = $("#" + messageHolder).text();
    str = jQuery.trim(str);
    // show if message is there, then hide it
    if (str != '') {
        $("#" + messageHolder).fadeIn('slow')
        $("#" + messageHolder).addClass(messageMode);
        $("#" + messageHolder).animate({
            opacity: 1.0
        }, 5000)
        $("#" + messageHolder).fadeOut('slow', function(){
            $("#" + messageHolder).remove();
        });
    }
}

function resizeFrame(updown){
    if (top.location != self.location) {
        var frame = parent.document.getElementById(window.name);
    }
    if (frame) {
        if (updown == 'shrink') {
            var clientH = document.body.clientHeight;
        }
        else {
            var clientH = document.body.clientHeight + 200;
        }
        $(frame).height(clientH);
    }
    else {
        //			throw( "resizeFrame did not get the frame (using name=" + window.name + ")" );
    }
}

/*
 a list with checkboxes, selecting/unselecting checkbox applies/removes class from row,
 selecting top checkbox selelects/unselects all, top checkbox is hidden if there are no
 selectable items, onload, rows with selected checkboxes are highlighted with class
 args: id of table, id of select all checkbox, highlight class
 */
function setupSelectList(list, allcontrol, highlightClass){
    $('#' + list + ' :checked').parent("td").parent("tr").addClass(highlightClass);
    
    if ($('#' + list + ' td :checkbox').length == 0) {
        $('#' + allcontrol).hide()
    };
    $('#' + allcontrol).click(function(){
        if (this.checked) {
            $('#' + list + ' td :checkbox').attr('checked', 'checked')
            $('#' + list + ' td :checkbox').parent('td').parent('tr').addClass(highlightClass);
        }
        else {
            $('#' + list + ' td :checkbox').attr('checked', '')
            $('#' + list + ' tbody tr').removeClass(highlightClass);
        }
    });
    
    $('#' + list + ' td :checkbox').click(function(){
        someChecked = false
        if (this.checked) {
            $(this).parents('tr').addClass(highlightClass);
        }
        else {
            $(this).parents('tr').removeClass(highlightClass);
        }
        $('#' + list + ' td :checkbox').each(function(){
            if (this.checked) {
                someChecked = true
            }
        });
        if (!someChecked) {
            $('#' + allcontrol).attr('checked', '');
        }
        if ($('#' + list + ' td :checked').length !== $('#' + list + ' td :checkbox').length) {
            $('#' + allcontrol).attr('checked', '');
        }
        
        if ($('#' + list + ' td :checked').length == $('#' + list + ' td :checkbox').length) {
            $('#' + allcontrol).attr('checked', 'checked');
        }
    });
}


// toggle a fade
jQuery.fn.fadeToggle = function(speed, easing, callback){
    return this.animate({
        opacity: 'toggle'
    }, speed, easing, callback);
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
