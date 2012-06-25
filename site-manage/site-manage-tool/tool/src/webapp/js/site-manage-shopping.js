// Shopping Period JS
$(document).ready(function(){

	// Decorate the date inputs
	$("#shoppingVisibilityStart").datepicker();
	$("#shoppingVisibilityEnd").datepicker();
	
	// Get the shopping info for this site from a WS
	$.getJSON("/direct/delegated_access/" + $('#siteId').html() + ".json",
		function(response){
			var data = response.data;
		
			//add auth options
			$.getJSON("/direct/delegated_access/shoppingOptions/authorization.json", 
				function(response){
					for (var i=0; i<response.delegated_access_collection.length; i++) {
						if(response.delegated_access_collection[i].key === data.shoppingAuth){
							$('#shoppingVisibility')
								.append($("<option selected></option>")
								.attr("value",response.delegated_access_collection[i].key)
								.text(response.delegated_access_collection[i].value));
						}else{
							$('#shoppingVisibility')
								.append($("<option></option>")
								.attr("value",response.delegated_access_collection[i].key)
								.text(response.delegated_access_collection[i].value));
						}
					}
				}
			);
			
			//add role options
			$.getJSON("/direct/delegated_access/shoppingOptions/roles.json", 
				function(response){
					for (var i=0; i<response.delegated_access_collection.length; i++) {
						if(response.delegated_access_collection[i].key === data.shoppingRealm + ":" + data.shoppingRole){
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
				}
			);
			//add showTools options
			$.getJSON("/direct/delegated_access/shoppingOptions/tools.json", 
				function(response){
					for (var i=0; i<response.delegated_access_collection.length; i++) {
						if($.inArray(response.delegated_access_collection[i].key, data.shoppingShowTools) >= 0){
							$('#showTools')
								.append($("<option selected></option>")
								.attr("value",response.delegated_access_collection[i].key)
								.text(response.delegated_access_collection[i].value));
						}else{
							$('#showTools')
								.append($("<option></option>")
								.attr("value",response.delegated_access_collection[i].key)
								.text(response.delegated_access_collection[i].value));
						}
					}
				}
			);
		
            
            $('#shoppingVisibilityDiv').show();
            $('#shoppingVisibility').val(data.shoppingAuth);
            
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
            if(data.revokeInstructorEditable){
            	setShoppingSettingsDisabled(true);
				$("#shoppingPeriodOverride").hide();
				$("#optOutOfShoppingPeriod").hide();
				$("#editShoppingInstructions").hide();
				$("#shoppingPeriodOverrideLabel").hide();
				$("#viewShoppingInstructions").show();
				$('#shoppingPeriodOverride').attr('checked', false);
			}
	    }
	);
	
	
	
	// Gather the shopping variables and save them to a WS
	$("#updateButton").click(function(){
        try {
	     
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
            var data = {
                'shoppingAuth'      : $('#shoppingVisibility option:selected').val(),
                'shoppingStartDate' : start,
                'shoppingEndDate'   : end,
                'shoppingRealm'     : shoppingRealm,
                'shoppingRole'      : shoppingRole,
                'shoppingShowTools' : $('#showTools').val(),
                'directAccess': document.getElementById('shoppingPeriodOverride').checked
            };
            var form = $('form[name=editParticipantForm]');

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
        catch (error){
            console.log(error);
        }
	});
});

function setShoppingSettingsDisabled(disabled){
	$(".shoppingSetting").attr("disabled", disabled);
}

function optOutOfShoppingPeriod(){
	$('#shoppingPeriodOverride').attr('checked', true);
	$('.shoppingSetting').attr('value', '');
	setShoppingSettingsDisabled(false)
}

