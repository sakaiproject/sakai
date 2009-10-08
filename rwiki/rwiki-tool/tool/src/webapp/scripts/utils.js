/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/rwiki/trunk/rwiki-tool/tool/src/webapp/scripts/ajaxpopup.js $
 * $Id: utils.js 51318 2008-08-24 05:28:47Z csev@umich.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/
var sakai = sakai ||
{};
var utils = utils ||
{};

// toggle a fade
jQuery.fn.fadeToggle = function(speed, easing, callback){
    return this.animate({
        opacity: 'toggle'
    }, speed, easing, callback);
};
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


rearrangeBreadCrumb = function(){
	//start off hidden
    $('#wikiCrumb').hide();
	//if any children, show menu and list
    if ($('#wikiCrumb li').length > 0) {
        $('#visitedPages').show();
    }
	//if large, add class with overflow
    if ($("#wikiCrumb").height() > 200) {
        $("#wikiCrumb").addClass("oversizeCrumb");
    }
	//invert list so more recent go at top
	$("#wikiCrumb li").each(function(){
		$("#wikiCrumb").prepend(this);
	});
	//get position of menu open link
    var pos = $('#visitedPages').position();
    $('#visitedPages').click(function(event){
		//set position of menu a bit below menu open link and show
        $('#wikiCrumb').css('top', pos.top + $('#visitedPages').height() + 5);
        $('#wikiCrumb').toggle();
        $('#visitedPages').toggleClass('visitedPagesOff');
        event.preventDefault();
    });
};

// IE8 has a bug with textareas whose widths are set in css  - see SAK-16714
utils.fixIE8TextArea = function(){
    var initIETaWidth;
    var initIETaCols;
    var autoIETaWidth;
    var theseCols;
    var ta = $('#content');
    
    if ($(ta).length) {
        //get init width and cols values
        initIETaWidth = $(ta).width();
        initIETaCols = $(ta).attr('cols');
        
        //set the width to auto to fix IE 8 textarea bugs
        $(ta).css('width', 'auto');
        
        //check the new width
        autoIETaWidth = $(ta).width();
        
        theseCols = initIETaCols;
        /*
         is it smaller than the init? Add a column till it is about the same
         size as the initial size
         */
        while (autoIETaWidth < (initIETaWidth - 20)) {
            $(ta).attr('cols', theseCols + 1);
            theseCols = $(ta).attr('cols');
            autoIETaWidth = $(ta).width();
        }
        
    }
};

utils.setuphelplinks = function(){
    $('.rwiki_help_popup_link').hover(function(){
        showPopupHere(this, this.name);
    }, function(){
        hidePopup(this.name);
    });
    $('.rwiki_help_popup_link').focus(function(){
        showPopupHere(this, this.name);
    });
    $('.rwiki_help_popup_link').blur(function(){
        hidePopup(this.name);
    });
};