/*
Javascript for the UC Berkeley"s implementation of the Sakai Messages Tool

Copyright 2009 University of California, Berkeley

Licensed under the Educational Community License (ECL), Version 2.0 or the New
BSD license. You may not use this file except in compliance with one these
Licenses.
*/

/*global jQuery, fluid, fluid_1_0, recipients */

(function ($, fluid) {
	var jsonData = [];
	var forumFilterSelect = {};
    var container = {};
    var sourceStructure = {};
    var sourceList = {};
    var sourceListScroller = {};

    var templates = {
        sourceItem: '<div class="%forumid"><input style="margin-right:.7em" type="radio" id = "topicradios" value="%topicvalue" name="topicradios"/>%topiclabel</div>',
        sourceItemDisabled: '<div class="%forumid"><input style="margin-right:.7em" type="radio" disabled id="topicradios" value="%topicvalue" name="topicradios"/>%topiclabel</div>'
    };  
    
    var getJSONData = function (key) {
        var dataSet = {};
        for (var i = 0; i < jsonData.length; i++) {
            if (jsonData[i].hasOwnProperty(key)) {
                dataSet = jsonData[i];
            }
        }
        return dataSet;
    };
    
/*
    var stripeList = function (list) {
        $("div.even", list).removeClass("even");
        $("div:nth-child(even)", list).addClass("even");
    };
*/
    
    var clearHighlight = function (rowElm) {
        rowElm = (rowElm) ? $(rowElm) : $("div.highlight");
        rowElm.removeClass("highlight");
    };
    
    var updateSourceCounter = function (count) {
        count = (count !== undefined) ? count : $("div:visible",sourceList).length;
        $(".topic-source-counter", container).text(count);
    };
    
    var filterList = function () {
        var forumFilter = $(".forumDropdown").val();
        var filteredItems = $("div", sourceList);
        filteredItems.removeClass('shown-by-filter');
        
        if (forumFilter === "select-forum")  {
            filteredItems.addClass('shown-by-filter');
            filteredItems.show();
        } else {
            // convert the filter strings to classNames if not empty
            forumFilter = (forumFilter === "select-forum") ? "" :  "." + forumFilter;
            // hide all rows
            filteredItems.hide();
            // show the rows with the right classes
            filteredItems = $("div" + forumFilter , sourceList);
            filteredItems.addClass('shown-by-filter');
            filteredItems.show();
        }
        // restore the text filter if it's not empty
        if ($("#searchTopic").val().replace(/^\s+|\s+$/g, '') !== "") { 
            searchByName();
        } else {
            updateSourceCounter(filteredItems.length);        
        }

/*
	// clear radio buttons 

	// disable Move Threads button
	$(".topic-btn-save").attr("disabled", "disabled");
*/
     };
    
    var saveRestorePoint = function () {
        $("#searchTopic").val("");
	$(".forumDropdown").val("select-forum");
	
	
    };
    
    var searchByName = function () {
        var contents = $("#searchTopic").val().replace(/^\s+|\s+$/g, '').toLowerCase();
        if (!(contents === "")) {
            var regex = new RegExp(contents);
            $(".shown-by-filter", sourceList).each(function () {
                var item = $(this);
                var name = item.text().replace(/^\s+|\s+$/g, '').toLowerCase();
				
				// use toggle here
                if (name.search(regex) > -1) {
                    item.show();
                }
                else {
                    item.hide();
                }
            });
            updateSourceCounter();
        }
        else {
            filterList();
        }
    };
       
    var buildForumSelect = function () {
        var forum;
        var forumOptionTemplate = $('#select-forum');
        var forumOption;
        var forums= getJSONData("forums").forums;
        for (var i = 0; i < forums.length;  i++) {
                forum= forums[i];
                forumOption = forumOptionTemplate.clone(true);
                forumOption.text(forum.forumtitle);
                forumOption.val(forum.forumid);
                forumOption.data('forum', forums[i]);
                forumFilterSelect.append(forumOption);
        }
    };

    var makeSourceListItem = function (topics) {
        var itemHTML = fluid.stringTemplate(templates.sourceItem, {
	    forumid: topics.forumid,
            topicvalue: topics.topicid,
            topiclabel: $('<p></p>').text(topics.topictitle).html() 
        });
        return itemHTML;
    };

    var makeSourceListItemDisabled = function (topics) {
        var itemHTML = fluid.stringTemplate(templates.sourceItemDisabled, {
            forumid: topics.forumid,
            topicvalue: topics.topicid,
            topiclabel: $('<p></p>').text(topics.topictitle).html()
        });
        return itemHTML;
    };

    var buildThreadList = function() {
	var idlist = []; 
        var itemHTML = "";
	//  #checkbox is the <div id="checkbox">  in dfAllMessages.jsp 
	// we want to find out which messages are selected to move,  

	$("#checkbox input[type=checkbox]:checked").each(function() {
               var threadid = ($(this).val());
		idlist.push(threadid);
		var thetitle = $(this).parent().siblings(".messageTitle").find(".messagetitlelink").text();  
        	itemHTML += " - " + thetitle + "<br/>";
        });

	$(".threads-to-move", container).html(itemHTML);
    }

    var buildSourceListScroller = function () {
        var itemHTML = "";
        var topics= getJSONData("topics").topics;
        var totalTopics= topics.length;
        for (var j = 0; j < totalTopics; j++) {
		var sourcetitle = $(".sourcetitle").text();
		var currtopic = topics[j].topictitle ;
		if ( sourcetitle == currtopic) { 
			// if current topic, greyed out disable radio selection
            		itemHTML += makeSourceListItemDisabled(topics[j]);
		}
		else {
            		itemHTML += makeSourceListItem(topics[j]);
		}
        }
        $(sourceList).html(itemHTML);
/*
        if (window.parent.recipientData) {
            restoreSourceListSettings();
        }
*/
        $(".topic-source-total, .topic-source-counter", container).text(totalTopics);
    };	

    /* KEYBOARD NAVIGATION
     */
    
    var moveThreadEnabled = function (){
                        //function to check total number of CheckBoxes that are checked in a form
    			//if there is no elements in form, disable the move link
    	 		if (typeof document.forms['msgForum'].moveCheckbox === 'undefined') { return false; }
                //initialize total count to zero
                var totalChecked = 0;
                                //get total number of CheckBoxes in form
                        if (typeof document.forms['msgForum'].moveCheckbox.length === 'undefined') {
                   /*when there is just one checkbox moveCheckbox is not an array,  document.forms['msgForum'].moveCheckbox.length returns undefined */
                        if (document.forms['msgForum'].moveCheckbox.checked == true )
                        {
                                        totalChecked += 1;
                        }
                        }
                        else {
                // more than one checkbox is checked.
                var chkBoxCount = document.forms['msgForum'].moveCheckbox.length;
                //loop through each CheckBox
                        for (var i = 0; i < chkBoxCount; i++)
                        {
                                //check the state of each CheckBox
                                if (eval("document.forms['msgForum'].moveCheckbox[" + i + "].checked") == true)
                                {
                                        //it's checked so increment the counter
                                        totalChecked += 1;
                                }
                        }
                        }
                        if (totalChecked >0){
                                // enable the move link
                                return true;
                        }
                        else {
                                return false;
                        }

                }

    /* SETUP DOM EVENTS */
   
    var bindDOMelements = function () {
                
        $(".topic-btn-save").click(function () {
            // call processMoveThread 
	var linkid = "msgForum:hidden_move_message_commandLink";
        var hiddenmovelink = document.getElementById(linkid);
	hiddenmovelink.onclick();
//	$(".topic-picker").dialog("close");
        });
        
        $("#searchTopic").keyup(function (event) {
            if (event.keyCode === $.ui.keyCode.ESCAPE || $(this).val() === "") {
                //if esc is pressed we want to clear the value of search box  
                $(this).val("");
                filterList();
            } else {
                searchByName();
            }
        });
        
        $(".checkbox-reminder").click(function () {
	var reminder = $("#checkbox-reminder:checked").val();
	if (reminder != undefined) {
		// reminder checkbox is checked 
		$(".moveReminder").val(true);
	}
	else {
		// reminder checkbox is NOT checked 
		$(".moveReminder").val(false);
	}
	});

        $(".topic-btn-cancel").click(function () {
			var linkid = "msgForum:hidden_close_move_thread";
			var hiddenmovelink = document.getElementById(linkid);
			hiddenmovelink.onclick();
			//$(".topic-picker").dialog("close");
        });
        
	$('input:radio',sourceList).click(function () {
	 	var radiovalue = $(this).val();	
		// enable Move Threads button 
		$(".topic-btn-save").removeAttr("disabled");
		var topichidden = $(".selectedTopicid");
		$(".selectedTopicid").val(radiovalue);
	});

        $(sourceList).click(function (event) { 
            var elm = ($(event.target).is('div')) ? $(event.target) : $(event.target).parent("div");
          //   addSingleRow(elm);
		});
        
        $(".forumDropdown").change(function () {
            filterList();
        });
        
        $(".display-topic-picker").click(function () {
		if (moveThreadEnabled()) {
			buildThreadList();
 			//buildSourceListScroller();
            		$(".topic-picker").dialog("open");
            		$(".topic-picker").css({ "overflow-y": "auto", height: "calc(100% - 50px)" });
            		$(".topic-source-picker").css({ "overflow-y": "auto", height: "calc(100% - 50px)" });
            		//saveRestorePoint();
		}
		else {
			// do nothing
		}
        });
                
    };

    /* Initialization Routine */

    var parseData = function () {
        var txtData = $("#data").text();
        jsonData = JSON.parse(txtData, null);
    };

    var collectElements = function () {
        container = $("#topic-picker");
        forumFilterSelect = $(".forumDropdown", container);
        sourceList = $(".topic-source-list", container);
        sourceStructure = $(".topic-source", container);
    };
    
    var initDialog = function () {
        $(".topic-picker").dialog({ autoOpen: false, width: 680, modal: true, position: "top", draggable: false, resizable: false});
    };
 
    $(function () {
        window.parent.scrollTo(0,0);
    	parseData();
        collectElements();
        initDialog();
        sourceListScroller = fluid.scroller($(".topic-source-scroller-inner"), { maxHeight: 270});
        //buildThreadList();
        buildSourceListScroller();
	buildForumSelect();
        $(".forumDropdown").val("select-forum");
        filterList();
        bindDOMelements(); 
        // stripeList(sourceList);
    });
        
})(jQuery, fluid_1_0);
