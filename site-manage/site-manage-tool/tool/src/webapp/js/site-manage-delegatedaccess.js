var shoppingOptOut = false;
var canEdit = false;
var revokeInstructorEditable = false;
// Shopping Period JS
$(document).ready(function(){
	shoppingOptOut = false;
	
	//activate the Jquery tabs
	$( "#shoppingtabs" ).tabs();
	
	//if view DA access is enabled, set up the fields:
	if($('#viewDelegatedAccessUsers')){
		$.getJSON("/direct/delegated_access/access/site/" + $('#siteId').html() + ".json",
			function(response){
				var anyAccess = false;
				for (var i=0; i<response.delegated_access_collection.length; i++) {
					anyAccess = true;
					$('#viewDAAccessUsersTable tr:last').after('<tr>' +
												'<td>' + response.delegated_access_collection[i].data.userDisplayName + '</td>' +
												'<td>' + response.delegated_access_collection[i].data.userEid + '</td>' +
												'<td>' + response.delegated_access_collection[i].data.realm + '</td>' +
												'<td>' + response.delegated_access_collection[i].data.role + '</td>' +
												'<td>' + response.delegated_access_collection[i].data.deniedToolsNames + '</td></tr>');
				}
				if(anyAccess){
					$('#viewDelegatedAccessUsersDiv').show();
					resizeFrame('grow');
				}
			}
		);
	}
	
	//if shopping period is enabled, set up the fields:
	if($('#shoppingPeriodInstructorEditable')){
		// Decorate the date inputs
		$("#shoppingVisibilityStart").datepicker();
		$("#shoppingVisibilityEnd").datepicker();
		
		// Get the shopping info for this site from a WS
		$.getJSON("/direct/delegated_access/canEditShopping/site/" + $('#siteId').html() + ".json",
				function(canEditShopping){
		$.getJSON("/direct/delegated_access/" + $('#siteId').html() + ".json",
			function(response){
				var data = response.data;
				canEdit = canEditShopping.delegated_access_collection[0].data;
				//add role options
				$.getJSON("/direct/delegated_access/shoppingOptions/roles.json", 
					function(response){
						for (var i=0; i<response.delegated_access_collection.length; i++) {
							if(response.delegated_access_collection[i].key === data.shoppingRealm + ":" + data.shoppingRole
									|| response.delegated_access_collection.length == 1){
								//if this is the selected option or the only option, then select it
								$('#shoppingRole')
									.append($("<option selected></option>")
									.attr("value",response.delegated_access_collection[i].key)
									.text(response.delegated_access_collection[i].value));
							}else{
								$('#shoppingRole')
									.append($("<option></option>")
									.attr("value",response.delegated_access_collection[i].key)
									.text(response.delegated_access_collection[i].value));
							}
						}
						if(response.delegated_access_collection.length == 1){
							//hide the Role option since there is only one option to select and its pre-selected:
							$('#shoppingRole').hide();
							$("label[for='shoppingRole']").hide();
						}
					}
				);
				//add showTools options
				if(canEdit === true){
					//if the user can edit, this means they won't get a 404 error when calling the pages function
					$.getJSON("/direct/site/" + $('#siteId').html() + "/pages.json", 
						function(pageResponse){
							//get a list of all tools in this site:
							var tools = new Array();
							var toolI = 0;
							for(var i =0; i<pageResponse.length; i++){
								for(var j=0; j<pageResponse[i].tools.length; j++){
									tools[toolI] = pageResponse[i].tools[j].toolId;
									toolI++;
								}
							}
							setupTools(data, tools);
						}
					);
				}else{
					setupTools(data, null);
				}
			
	            
	            $('#shoppingVisibilityDiv').show();
	            
	            if (data.shoppingStartDate != undefined && data.shoppingStartDate != ""){
	                var shoppingStartDate = new Date(parseInt(data.shoppingStartDate));
	                $('#shoppingVisibilityStart').val($.datepicker.formatDate("mm/dd/yy", shoppingStartDate));
	            }
	            if (data.shoppingEndDate != undefined && data.shoppingEndDate != ""){
	                var shoppingEndDate = new Date(parseInt(data.shoppingEndDate));
	                $('#shoppingVisibilityEnd').val($.datepicker.formatDate("mm/dd/yy", shoppingEndDate));
	            }
	            
	            //disable options if this shopping site isn't directAccess (option for overriding inheritted settings)
	            if(!data.directAccess){
	            	setShoppingSettingsDisabled(true);
	            }else{
	            	$('#shoppingPeriodOverride').attr("checked", true);
	            }
	            
	            //disable everything if the instructor isn't allowed to edit (controlled by DA)
	            revokeInstructorEditable = data.revokeInstructorEditable;
	            if(revokeInstructorEditable || canEdit !== true){
	            	setShoppingSettingsDisabled(true);
					$("#shoppingPeriodOverride").hide();
					$("#optOutOfShoppingPeriod").hide();
					$("#editShoppingInstructions").hide();
					$("#shoppingPeriodOverrideLabel").hide();
					$("#viewShoppingInstructions").show();
					$('#shoppingPeriodOverride').attr('checked', false);
				}
	            
	            //set any disabled classes to disabled
	            $(".shoppingSettingDisabled").attr("disabled", true);
	            $("a.shoppingSettingDisabled").hide();
	            
	            resizeFrame('grow');
		    }
		);
		});
	
	
	
		// Gather the shopping variables and save them to a WS
		$("#updateButton").click(function(){
	        try {
	        	if(canEdit === true && revokeInstructorEditable !== true){
		            var start = "";
				    try{
					start  = $.datepicker.parseDate("mm/dd/yy", $('#shoppingVisibilityStart').val()).getTime();
				    }catch(err){}
				    var end = "";
				    try{
					end   = $.datepicker.parseDate("mm/dd/yy", $('#shoppingVisibilityEnd').val()).getTime();
				    }catch(err){}
				    var shoppingRole = "";
				    var shoppingRealm = "";
				    try{
				    	var split = $("#shoppingRole").val().split(":");
				    	shoppingRealm = split[0];
				    	shoppingRole = split[1];
				    }catch(err){
				    	shoppingRealm = "";
				    	shoppingRole = "";
				    }
				    var pubTools = $('#showPublicTools').val();
				    var authTools = $('#showAuthTools').val();
				    if(pubTools != null && pubTools.length > 0){
				    	//filter all the public options out of the auth array list
				    	authTools = $.grep($('#showAuthTools').val(), function(n,i){
										return $.inArray(n, $('#showPublicTools').val()) < 0;
									})
				    }
		            var data = {
		                'shoppingStartDate' : start,
		                'shoppingEndDate'   : end,
		                'shoppingRealm'     : shoppingRealm,
		                'shoppingRole'      : shoppingRole,
		                'shoppingShowAuthTools' : authTools,
		                'shoppingShowPublicTools' : pubTools,
		                'directAccess': document.getElementById('shoppingPeriodOverride').checked
		            };
		            var form = $('form[name=editParticipantForm]');
		
		            //check show tools is either selected or that the user has already been warned, if not, return false
		            if(!shoppingOptOut
		            		&& data.directAccess
		            		&& (data.shoppingShowAuthTools === null || data.shoppingShowAuthTools[0] === "")
		            		&& (data.shoppingShowPublicTools === null || data.shoppingShowPublicTools[0] === "")
		            		&& document.getElementById("showToolsWarning").style.display === "none"){
		            	document.getElementById("showToolsWarning").style.display = '';
		            	return false;
		            }
		            
		            $.ajax({
		                type: 'POST',
		                url: "/direct/delegated_access/" + $('#siteId').html() + ".json",
		                data: data,
		                async:false,
		                failure: function failure(data){
		                    // TODO: internationalize
		                    alert("There was an error saving the shopping period info.");
		                    },
		                });
	        	}
	        }
	        catch (error){
	            console.log(error);
	        }
		});
	}
});

function setupTools(data, tools){
	$.getJSON("/direct/delegated_access/shoppingOptions/tools.json", 
			function(response){
		
			
				for (var i=0; i<response.delegated_access_collection.length; i++) {
					//only show tools that are in the site
					var foundInSite = false;
					if(tools == null || response.delegated_access_collection[i].key == "Home"){
						foundInSite = true;
					}else{
						foundInSite = $.inArray(response.delegated_access_collection[i].key, tools);
					}
					if(foundInSite >= 0){
						//Auth Tools:
						if($.inArray(response.delegated_access_collection[i].key, data.shoppingShowAuthTools) >= 0){
							$('#showAuthTools')
								.append($("<option selected></option>")
								.attr("value",response.delegated_access_collection[i].key)
								.text(response.delegated_access_collection[i].value));
						}else{
							$('#showAuthTools')
								.append($("<option></option>")
								.attr("value",response.delegated_access_collection[i].key)
								.text(response.delegated_access_collection[i].value));
						}
						
						//Public Tools:
						if($.inArray(response.delegated_access_collection[i].key, data.shoppingShowPublicTools) >= 0){
							$('#showPublicTools')
								.append($("<option selected></option>")
								.attr("value",response.delegated_access_collection[i].key)
								.text(response.delegated_access_collection[i].value));
						}else{
							$('#showPublicTools')
								.append($("<option></option>")
								.attr("value",response.delegated_access_collection[i].key)
								.text(response.delegated_access_collection[i].value));
						}
					}
				}
				var revokedPublicOptClass = "";
				if(data.revokeInstructorPublicOpt){
					revokedPublicOptClass = " shoppingSettingDisabled";
					$("#showPublicToolsSelectAll").addClass("shoppingSettingDisabled");
				}
				//make the select box pretty:
				$("#showAuthTools").asmSelect({selectClass: "shoppingSetting showAuthTools", removeClass: "asmListItemRemove shoppingSetting"})
				.each(function(index){
					if(index < 1){
						$("#showPublicTools").asmSelect({selectClass: "shoppingSetting showPublicTools" + revokedPublicOptClass, removeClass: "asmListItemRemove shoppingSetting" + revokedPublicOptClass})
						.each(function(index){
							//only need to run this once :)
							if(index < 1){
								setShoppingToolOptionsEnabled(true, function(){setShoppingToolOptionsEnabled(false);});
								if(!data.directAccess || data.revokeInstructorEditable || canEdit !== true){
									setShoppingSettingsDisabled(true);
								}else{
									setShoppingSettingsDisabled(false);
								}
							}
						});
					}
				});
				
				$("#showPublicToolsSelectAll").click(function() {
					$("#showPublicTools").children().attr("selected", "selected").end().change();
					setShoppingToolOptionsEnabled(true, function(){setShoppingToolOptionsEnabled(false);});
					return false;
				}); 
				$("#showAuthToolsSelectAll").click(function() {
					$(".showPublicTools option:not(.asmOptionDisabled)").each(
						function(i, elem){
							$("#showAuthTools").children("[value='" + $(elem).val() + "']").attr("selected", "selected").end().change();
						}
					);
					setShoppingToolOptionsEnabled(true, function(){setShoppingToolOptionsEnabled(false);});
					return false;
				}); 
			}
		);
}

function setShoppingSettingsDisabled(disabled){
	$(".shoppingSetting").attr("disabled", disabled).end().change();
	if(disabled){
		$("a.shoppingSetting").hide();
	}else{
		$("a.shoppingSetting").show();
	}
	//always set these to disabled
	$(".shoppingSettingDisabled").attr("disabled", true).end().change();
	$("a.shoppingSettingDisabled").hide();
	if(!disabled){
		setShoppingToolOptionsEnabled(true, function(){setShoppingToolOptionsEnabled(false);});
	}
}

function optOutOfShoppingPeriod(){
	$('#shoppingPeriodOverride').attr('checked', true).end().change();
	$('.shoppingSetting').attr('value', '').end().change();
	$("#showPublicTools").children().attr("selected", "").end().change();
	$("#showAuthTools").children().attr("selected", "").end().change();
	setShoppingSettingsDisabled(false)
	shoppingOptOut = true;
	setShoppingToolOptionsEnabled(true, function(){setShoppingToolOptionsEnabled(false);});
}


function resizeFrame(updown){
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
}

/**
 * first: flag that says it's the first time calling this function
 * callback: since we have to wait for the select to refresh (.end().change()), call yourself again
 * with the flag to false so it can manipulate the select tag.
 */
function setShoppingToolOptionsEnabled(first, callback){
	if(first){
		//remove all the public tool options which were added to the auth list
		//these will be readded below:
		$("#showAuthTools").children(".publicTool").attr("selected", "").attr('disabled', true).end().change();
		$("#showAuthTools").children(".publicTool").removeClass("publicTool");
	}
	$(".showPublicTools option").each(function (i, elem) {
		if("" !== $(elem).val()){
			$(".showAuthTools option").each(function (j, elem2) {
				if($(elem).val() === $(elem2).val()){
					if($(elem).hasClass("asmOptionDisabled")){
						if(first){
							//add the selected public option to the auth options list to
							//show to the user that public tools will show up for auth users as well
							$("#showAuthTools").children("[value='" + $(elem).val() + "']").attr("selected", "selected").attr('disabled', true).end().change();
						}else{
							//hide the "remove" link for auth tools that are in the public tools list
							$("li.asmListItem[rel='" + $(elem2).attr("rel") +"'] a.asmListItemRemove").hide();
							$("#showAuthTools").children("[value='" + $(elem).val() + "']").addClass("publicTool");
						}
					}
				}
			});
		}
	});
	if(callback != null){
		//call back to ourselves and manipulate the <select> tag.  This is because
		//we have to wait for change() to complete first before modifying.
		callback();
	}
}
