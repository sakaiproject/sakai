/*
Javascript for the UC Berkeley"s implementation of the Sakai Messages Tool

Copyright 2009 University of California, Berkeley

Licensed under the Educational Community License (ECL), Version 2.0 or the New
BSD license. You may not use this file except in compliance with one these
Licenses.
*/

/*global jQuery, recipients */

(function ($) {
	var jsonData = [];
	var forumFilterSelect = {};
    var container = {};
    var sourceList = {};

    var templates = {
        sourceItem: '<div class="%forumid"><input style="margin-right:.7em" type="radio" id = "topicradios" value="%topicvalue" name="topicradios"/>%topiclabel</div>',
        sourceItemDisabled: '<div class="%forumid"><input style="margin-right:.7em" type="radio" disabled id="topicradios" value="%topicvalue" name="topicradios"/>%topiclabel</div>'
    };

    var escapeHtml = function (value) {
        return String(value ?? "").replace(/[&<>"']/g, function (match) {
            return {
                "&": "&amp;",
                "<": "&lt;",
                ">": "&gt;",
                "\"": "&quot;",
                "'": "&#39;"
            }[match];
        });
    };

    var formatTemplate = function (template, values) {
        return template.replace(/%([A-Za-z0-9_]+)/g, function (match, key) {
            return Object.prototype.hasOwnProperty.call(values, key) ? escapeHtml(values[key]) : match;
        });
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
        var itemHTML = formatTemplate(templates.sourceItem, {
	    forumid: topics.forumid,
            topicvalue: topics.topicid,
            topiclabel: topics.topictitle
        });
        return itemHTML;
    };

    var makeSourceListItemDisabled = function (topics) {
        var itemHTML = formatTemplate(templates.sourceItemDisabled, {
            forumid: topics.forumid,
            topicvalue: topics.topicid,
            topiclabel: topics.topictitle
        });
        return itemHTML;
    };

    var buildThreadList = function() {
        var itemHTML = "";

        $("#checkbox input[type=checkbox]:checked").each(function() {
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
            var sourcetopicid = $("[id^='sourcetopicid-']")[0].id.split("-")[1];
            var currtopicid = topics[j].topicid;
            if (sourcetopicid == currtopicid) {
                // if current topic, greyed out disable radio selection
                itemHTML += makeSourceListItemDisabled(topics[j]);
            }
            else {
                itemHTML += makeSourceListItem(topics[j]);
            }
        }
        $(sourceList).html(itemHTML);
        $(".topic-source-total, .topic-source-counter", container).text(totalTopics);
    };

    var moveThreadEnabled = function (){
        var moveCheckbox = document.forms['msgForum'].moveCheckbox;
        if (typeof moveCheckbox === 'undefined') {
            return false;
        }

        return Array.from(moveCheckbox.length === undefined ? [moveCheckbox] : moveCheckbox)
            .some(function (checkbox) {
                return checkbox.checked;
            });
    };

    var bindDOMelements = function () {
        $(".topic-btn-save").click(function () {
            var linkid = "msgForum:hidden_move_message_commandLink";
            var hiddenmovelink = document.getElementById(linkid);
            hiddenmovelink.onclick();
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
            $(".moveReminder").val($("#checkbox-reminder:checked").val() !== undefined);
        });

        $(".topic-btn-cancel").click(function () {
            var linkid = "msgForum:hidden_close_move_thread";
            var hiddenmovelink = document.getElementById(linkid);
            hiddenmovelink.onclick();
        });

        $('input:radio',sourceList).click(function () {
            var radiovalue = $(this).val();
            $(".topic-btn-save").removeAttr("disabled");
            $(".selectedTopicid").val(radiovalue);
        });

        $(".forumDropdown").change(function () {
            filterList();
        });

        $(".display-topic-picker").click(function () {
            if (moveThreadEnabled()) {
                buildThreadList();
                $(".topic-picker").dialog("open");
                $(".topic-picker").css({ "overflow-y": "auto", height: "calc(100% - 50px)" });
                $(".topic-source-picker").css({ "overflow-y": "auto", height: "calc(100% - 50px)" });
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
    };
    
    var initDialog = function () {
        $(".topic-picker").dialog({ autoOpen: false, width: 680, modal: true, position: "top", draggable: false, resizable: false});
    };
 
    $(function () {
        window.parent.scrollTo(0,0);
        parseData();
        collectElements();
        initDialog();
        $(".topic-source-scroller-inner").css({ maxHeight: 270, overflowY: "auto" });
        buildSourceListScroller();
        buildForumSelect();
        $(".forumDropdown").val("select-forum");
        filterList();
        bindDOMelements();
    });
        
})(jQuery);
