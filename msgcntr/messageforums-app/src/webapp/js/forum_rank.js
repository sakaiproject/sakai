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
	var assignToBox = {};
	var assignToChoice = {};
	var aggregateDelimiter = "&";
	var groupFilterSelect = {};
	var roleFilterSelect = {};	
    var currentTab = {};
    var currSourceRow = {};
    var container = {};
    var sourceStructure = {};
    var sourceList = {};
    var sourceListScroller = {};
    var collectionStructure = {};
    var collectionList = {};
    var collectionListScroller = {};
    var toListScroller = {};
    var currCollectionRow = {};
    var restorePoint = "";
    
    var templates = {
        toTokenTemplate: '<div class="sakai-ppkr-token token-ind sakai-ppkr-to-%membershipItemId" title="%eid" id="sakai-ppkr-to-%membershipItemIdAgain">%displayName <span class="ppkr-remove">x</span></div>',
        choiceSeparator: '<span class="role-choice-separator"> &nbsp; </span>',
        sourceItem: '<div id="source-list-%membershipItemId" class="role-%roleId%groupsClasses" title="%titleEid">%displayName</div>',
        collectionItem: '<div id="%membershipItemId" title="%memberEid">%displayName</div>'
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
    
    var saveRecipientDataToParent = function () {
        window.parent.recipientData = {
            collection: collectionList.html(),
            to_field: assignToBox.html()
        }
    };
        
    var restoreSelectionFromParentWindow = function () {
        if (typeof window.parent.recipientData === "undefined" || window.parent.recipientData === "") {
            return false;
        }
        assignToBox.html(window.parent.recipientData.to_field);
        return true;
    };

    var clearRecipientDataFromParentWindow = function () {
        window.parent.recipientData = "";
    };
    
    var srcIDtoMemberID = function (scrID) {
        return scrID.replace(/source-list-/, "");
    };

    var stripeList = function (list) {
        $("div.even", list).removeClass("even");
        $("div:nth-child(even)", list).addClass("even");
    };
    
    var clearHighlight = function (rowElm) {
        rowElm = (rowElm) ? $(rowElm) : $("div.highlight");
        rowElm.removeClass("highlight");
    };
    
    var updateSourceCounter = function (count) {
        count = (count !== undefined) ? count : $("div:visible",sourceList).length;
        $(".sakai-ppkr-source-counter", container).text(count);
    };
    
    var updateCollectionCounter = function () {
        $(".sakai-ppkr-collection-counter", container).text($("div",collectionList).length);
    };
        
    var hilightRow = function (rowElm) {
        rowElm.addClass("selected");
    };
    
    var unhilightRow = function (rowElm) {
        rowElm.removeClass("selected");
    };
    
    var unhilightAllRows = function () {
        $("div", sourceList).removeClass("selected");
    };
    
    var buildCollectionItem = function (rowElm) { 
        return fluid.stringTemplate(templates.collectionItem, {
            membershipItemId: srcIDtoMemberID(rowElm.attr("id")), 
            memberEid: rowElm.attr("title"), 
            displayName: rowElm.text()
        });
    };

    /* REMOVING ROWS */
      
    var removeRow = function (rowElm) {
    	var userId = rowElm.attr("id");
        unhilightRow($("#source-list-" + userId, sourceList));
        rowElm.remove();
        updateCollectionCounter();
    };
    
    var removeSingleRow = function (rowElm) {
        rowElm = $(rowElm); // cast to jQuery obj
        currCollectionRow = "";
		$("div.key-highlight").removeClass("key-highlight");
		removeRow(rowElm);
        stripeList(collectionList);
    };
    
    var removeRowById = function (userID) {
        userID = srcIDtoMemberID(userID);
        $("div#" + userID, collectionList).remove();
    };
    
    var removeAllRows = function () {
        $("div", collectionList).remove();
        unhilightAllRows();
		updateCollectionCounter(); 
		clearHighlight(); // reset the last highlight
    };
    
    /* ADDING ROWS */
   
    var addRow = function (rowElm) {
        hilightRow(rowElm);
        var newRow = $(buildCollectionItem(rowElm));
		$(collectionList).append(newRow);
		return newRow;
	};
    
    var addSingleRow = function (rowElm) {
        if (rowElm.hasClass("selected")) {
            unhilightRow(rowElm);
            removeRowById(rowElm.attr("id"));
        } else {
            // highlight and add row to container
            $(".key-highlight", container).removeClass("key-highlight");
        	clearHighlight();
        	var newRow = addRow(rowElm);
        	//setFocus(rowElm); // used for keyboard navigation which needs reworking
            collectionListScroller.scrollBottom();
            newRow.addClass("highlight");
            $("#source-scroller").focus();
        	currCollectionRow = newRow;
        }
        stripeList(collectionList);
        updateCollectionCounter(); 
    };

    var addAllRows = function () {
        var collectionHTML = "";
        var itemsToAdd = $("div:visible", sourceList).not(".selected");
        itemsToAdd.each(function () {
			collectionHTML += buildCollectionItem($(this));
            hilightRow($(this));
		});
        
        $(collectionList).append(collectionHTML);
                
        updateCollectionCounter(); 
        stripeList(collectionList);
		clearHighlight(); // reset the last highlight
    };
    
   var buildExistingIndividualSelectItems= function (user) {
        return fluid.stringTemplate(templates.toTokenTemplate, {
            membershipItemId: user.membershipItemId,
            membershipItemIdAgain: user.membershipItemId,
            eid: user.eid,
            displayName: user.userDisplayName,
        });
    };


    var buildExistingUsersCollectionItem = function (user) {
        return fluid.stringTemplate(templates.collectionItem, {
            membershipItemId: user.membershipItemId,
            memberEid: user.eid,
            displayName: user.userDisplayName,
        });
    };

    var buildExistingUsers= function () {

        var users = getJSONData("users").users;
        var totalUsers = users.length;
        var memberitemidlist_raw = $('input[id="addRank:selected_indiv_ids"]').val();
	if (!memberitemidlist_raw) {
		return;
	}
	var memberitemidlist = memberitemidlist_raw.substring(0, memberitemidlist_raw.length - 1);
        var existingUsers= "";
        var existingCollection= "";
        $(".token-ind", assignToBox).remove();

        for (var j = 0; j < totalUsers; j++) {
		var user= users[j];
		if (memberitemidlist.indexOf(user.membershipItemId)>=0) {
            		existingUsers+= buildExistingIndividualSelectItems(user);
                        existingCollection+= buildExistingUsersCollectionItem(user);
           //  hilightRow($(this));
                }
        }

        $(collectionList).append(existingCollection);
        updateCollectionCounter();
        stripeList(collectionList);
        clearHighlight(); // reset the last highlight

        assignToBox.append(existingUsers);
};

    var getRoleMembershipItemId = function (pickedItem) {
        var roleMembershipItemId;
        var idStr = $(pickedItem).attr("id");                             
        roleMembershipItemId = idStr.replace(/token-role-|sakai-ppkr-to-/, "");        
        return roleMembershipItemId;
    };

    var aggregateSendToData = function () {
    	var aggregate = "";
    	$(".sakai-ppkr-to-container").children('div:visible').each(function () {
    		aggregate += getRoleMembershipItemId(this);
    		aggregate += aggregateDelimiter; 
    	});
    	aggregate = aggregate.substring(0, aggregate.length - 1);
    	$('input[id="addRank:aggregate_assign_to_item_ids"]').val(aggregate);
    	$('input[id="pvtMsgReply:aggregate_assign_to_item_ids"]').val(aggregate);
    	$('input[id="pvtMsgForward:aggregate_assign_to_item_ids"]').val(aggregate);
    };
    
    var filterList = function () {
        var groupFilter = $(".sectionDropdown").val();
        var roleFilter = $(".roleDropdown").val();
        var filteredItems = $("div", sourceList);
        filteredItems.removeClass('shown-by-filter');
        
        if ((groupFilter === "all-sections") && (roleFilter === "all-roles")) {
            filteredItems.addClass('shown-by-filter');
            filteredItems.show();
        } else {
            // convert the filter strings to classNames if not empty
            groupFilter = (groupFilter === "all-sections") ? "" :  "." + groupFilter;
            roleFilter = (roleFilter === "all-roles") ? "" : "." + roleFilter;
            // hide all rows
            filteredItems.hide();
            // show the rows with the right classes
            filteredItems = $("div" + groupFilter + roleFilter, sourceList);
            filteredItems.addClass('shown-by-filter');
            filteredItems.show();
        }
        // restore the text filter if it's not empty
        if ($("#searchIndividual").val().replace(/^\s+|\s+$/g, '') !== "") { 
            searchByName();
        } else {
            updateSourceCounter(filteredItems.length);        
        }
     };
    
    var saveRestorePoint = function () {
        restorePoint = collectionList.html();
    };
    
    var restoreCollectionList = function (HTMLdata) {
        $(collectionList).html(HTMLdata);
        updateCollectionCounter(); 
    };
    
    var restoreSourceListSettings = function () {
        var sourceItems = $("div", sourceList);
        sourceItems.each(function () {
            var srcRow = $(this);
            var srcRowIsSelected = srcRow.hasClass("selected");
            var memberID = "#" + srcIDtoMemberID(srcRow.attr("id"));
            if ($(memberID, collectionList).length) { // the row is in the collection list
                if (!srcRowIsSelected) {
                    hilightRow(srcRow);
                }
            } else {
                if (srcRowIsSelected) {
                    unhilightRow(srcRow);
                }
            }
        });
    };
    
    var cancelCleanUpRestore = function () {
        if (collectionList.html() === restorePoint) {
            return;
        }
        restoreCollectionList(restorePoint);
        
        if ($("div", collectionList).length) {
            restoreSourceListSettings();
        } else {
            unhilightAllRows();
        }
    };
    
    var searchByName = function () {
        var contents = $("#searchIndividual").val().replace(/^\s+|\s+$/g, '').toLowerCase();
        if (!(contents === "")) {
            var regex = new RegExp("\\b" + contents);
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
       
    var buildMailToBoxIndividual = function (recipientRow) {
    	var userID = recipientRow.attr("id");
        return fluid.stringTemplate(templates.toTokenTemplate, {
            membershipItemId: userID, 
            membershipItemIdAgain: userID, 
            eid: recipientRow.attr("title"), 
            displayName: recipientRow.text()
        });
    };
   
    var buildMailToBox = function () {
    	// restore previous state - handling going to add attachment page and back
        var needsTokens = !restoreSelectionFromParentWindow();
        
	// assignToBox is the box above the 'Add Individual..' button. It contains the selected results.  

	// CW-2780 CW-2779:  build already selected individuals,  in EditRank page
	buildExistingUsers();
	displayDirections();

	
    };
      


String.prototype.hashCode = function() {
  for(var ret = 0, i = 0, len = this.length; i < len; i++) {
    ret = (31 * ret + this.charCodeAt(i)) << 0;
  }
  return ret;
};

    var buildGroupSelect = function () {
// not seem to work with group id with space in it, 
    	var group;
    	var groupOptionTemplate = $('#group-option-all');
    	var groupOption;
    	var groups = getJSONData("groups").groups;
    	for (var i = 0; i < groups.length;  i++) {
    		group = groups[i];
    		groupOption = groupOptionTemplate.clone(true);
    		groupOption.text(group.title);
		groupOption.val("group-" + group.groupId.hashCode());   // use hashCode instead, since groupID might contain special char. 
    		groupOption.attr('id', group.membershipItemId);
    		groupOption.data('group', group);
    		groupFilterSelect.append(groupOption);
    	}
    };
    
    var buildRoleSelect = function () {
// not seem to work with role id with space in it, 
    	var role;
    	var roleOptionTemplate = $('#role-option-all');
    	var roleOption;
    	var roles = getJSONData("roles").roles;
    	for (var i = 0; i < roles.length;  i++) {
    		role = roles[i];
    		roleOption = roleOptionTemplate.clone(true);
    		roleOption.text(role.roleId);
    		roleOption.val('role-' + role.roleId.hashCode());  // use hashCode instead, since roleID might contain special char.
    		roleOption.attr('id', role.membershipItemId);
    		roleOption.data('role', roles[i]);
    		roleFilterSelect.append(roleOption);
    	}
    };
    
    var makeSourceListItem = function (user) {
        var groupClasses = "";
        var groupMemberships = user.groups;
		for (var i = 0; i < groupMemberships.length; i++) {
			groupClasses += " group-" + groupMemberships[i].groupId.hashCode();   // use hashCode instead, since groupID might contain special char.

		}
        var itemHTML = fluid.stringTemplate(templates.sourceItem, {
            membershipItemId: user.membershipItemId, 
            roleId: user.roleId.hashCode(), 
            groupsClasses: groupClasses,
            displayName: user.userDisplayName,
            titleEid: user.eid
        });
        return itemHTML;
    };

    var buildSourceListScroller = function () {
        var itemHTML = "";
        var users = getJSONData("users").users;
        var totalUsers = users.length;
        for (var j = 0; j < totalUsers; j++) {
            itemHTML += makeSourceListItem(users[j]);
        }
        $(sourceList).html(itemHTML);
        if (window.parent.recipientData) {
            restoreCollectionList(window.parent.recipientData.collection);
            restoreSourceListSettings();
        }
        $(".sakai-ppkr-source-total, .sakai-ppkr-source-counter", container).text(totalUsers);
    };	

    var saveCollection = function () {
        $(".sakai-ppkr").dialog("close");
        var newContent = "";
        $(".token-ind", assignToBox).remove();
        $("div", collectionList).each(function () {
            newContent += buildMailToBoxIndividual($(this));
        });
        assignToBox.append(newContent);
        
        toListScroller.refreshView();
        displayDirections();
    }; 
    
    /* KEYBOARD NAVIGATION
     * this needs work
     */
    
    var addKeyboardNavigation = function () {
    /*
        fluid.tabbable(sourceList);
        fluid.tabbable(collectionList);
        
        selectableContext = fluid.selectable(sourceListScroller, {
            selectableSelector: "li",
            onSelect: function (itemToSelect) {
                $(itemToSelect).addClass("key-highlight");
            },
            onUnselect: function (selectedItem) {
                $(selectedItem).removeClass("key-highlight");
            }
        });
        
        selectableContext = fluid.selectable(collectionList, {
            selectableSelector: "li",
            onSelect: function (itemToSelect) {
                $(itemToSelect).addClass("key-highlight");
            },
            onUnselect: function (selectedItem) {
                $(selectedItem).removeClass("key-highlight");
            }
        });
    */
    };
    
    var bindDeleteKey = function (row) {
        var deleteHandler = function () {
            removeSingleRow(row);
        };
       
        fluid.activatable(row, null, {
            additionalBindings: [{
                key: $.ui.keyCode.DELETE, 
                activateHandler: deleteHandler
            }]
        });
    };
    
    var displayDirections = function () {
        var numInd = $(".token-all:visible", assignToBox).length;
        var numShown = $(".token-ind", assignToBox).length;
        $("p",assignToBox).toggle((numInd + numShown) === 0);
    };
    
    /* SETUP DOM EVENTS */
   
    var bindDOMelements = function () {
        $(".sakai-ppkr-btn-add-all").click(function () {
        	addAllRows();
        });
  
        $(".sakai-ppkr-btn-remove-all").click(function () {
            removeAllRows(); 
            $("input", sourceList).removeAttr("checked");
            $("div", sourceList).removeClass("selected");
        });
                
        $(".sakai-ppkr-btn-save").click(function () {
            saveCollection(); 
        });
        
        $("#searchIndividual").keyup(function (event) {
            if (event.keyCode === $.ui.keyCode.ESCAPE || $(this).val() === "") {
                //if esc is pressed we want to clear the value of search box  
                $(this).val("");
                filterList();
            } else {
                searchByName();
            }
        });
        
        $(".sakai-ppkr-btn-cancel").click(function () {
            $(".sakai-ppkr").dialog("close");
            cancelCleanUpRestore();
        });
        
        $(collectionList).click(function (event) {
            removeRow($(event.target));
        });
        
        $(sourceList).click(function (event) { 
            var elm = ($(event.target).is('div')) ? $(event.target) : $(event.target).parent("div");
            addSingleRow(elm);
		});
        
        $(".sectionDropdown, .roleDropdown").change(function () {
            filterList();
        });
        
        $(".display-sakai-ppkr").click(function () {
            $(".sakai-ppkr").dialog("open");
            saveRestorePoint();
        });
                
        $(".sakai-ppkr-select-all").click(function () {
        	var id = $(this).attr("id").replace(/role-choice-/, "");
            var ppkrClass = ".token-role-" + id;
            //$('.token-ind').remove();
            if ($(ppkrClass).is(":hidden")) {
            	$(".token-role-allParticipants").hide();
            	$(ppkrClass).show();
            }
            $(".sakai-ppkr-to-container-scroller")[0].scrollTop = 0; // scroll to the top to display the element
            displayDirections();
        });
        
        $(".ppkr-remove").on('click', function () {
            var itemElm = $(this).parent();
            var userID = itemElm.attr("id").replace(/sakai-ppkr-to-/, "");
            if (itemElm.hasClass("token-all")) {
                itemElm.hide();
            } else {
                itemElm.remove();
                unhilightRow($("#source-list-" + userID, sourceList));
                removeRowById(userID);
                updateCollectionCounter();
            }
            displayDirections();
        });
        
        $(".sakai-ppkr-send-button").click(function () {
                aggregateSendToData();
                // clear recipients after save 
                clearRecipientDataFromParentWindow();
                return true;           
        });
        
        //$("form").submit();
        
        $(".breadCrumb a, .sakai-ppkr-cancel-button").click(function () {
            // clear recipients after cancel for new message
            clearRecipientDataFromParentWindow();
            return true;
        });
        
        $(".sakai-msgcntr-attach-btn").click(function () {
           saveRecipientDataToParent(); 
        });

    };

    /* Initialization Routine */
   
    var parseData = function () {
        var txtData = $("#data").text();
        jsonData = JSON.parse(txtData, null);
    };
    
    var collectElements = function () {
        container = $("#sakai-ppkr-messages");
        assignToBox = $(".sakai-ppkr-to-container");
        assignToChoice = $(".sakai-ppkr-to-choice");
        groupFilterSelect = $(".sectionDropdown", container);
        roleFilterSelect = $(".roleDropdown", container);
        sourceList = $(".sakai-ppkr-source-list", container);
        sourceStructure = $(".sakai-ppkr-source", container);
        collectionList = $(".sakai-ppkr-collection-list", container);
        collectionStructure = $(".sakai-ppkr-collection-picker", container);
    };
    
    var initDialog = function () {
        $(".sakai-ppkr").dialog({ autoOpen: false, width: 680, height: 500, modal: true, position: 'top'});
    };
 
    $(function () {
        window.parent.scrollTo(0,0);
    	parseData();
        collectElements();
        initDialog();
        buildMailToBox();
        buildGroupSelect();
        buildRoleSelect();
        sourceListScroller = fluid.scroller($(".sakai-ppkr-source-scroller-inner"), { maxHeight: 248});
        collectionListScroller = fluid.scroller(collectionList, { maxHeight: 248});
        toListScroller = fluid.scroller($(".sakai-ppkr-to-container"), { maxHeight: 115});
        buildSourceListScroller();
        filterList();
        bindDOMelements(); 
        addKeyboardNavigation();
        stripeList(sourceList);
    });
        
})(jQuery, fluid_1_0);

