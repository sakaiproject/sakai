includeWebjarLibrary('bootstrap-multiselect');
				
				$(document).ready(function() {
					$( "#tabs" ).tabs();
					$( "#shoppingtabs" ).tabs();

					localDatePicker({
						input: '#shoppingVisibilityStart',
						useTime: 0,
						parseFormat: 'YYYY-MM-DD',
						allowEmptyDate: true,
						ashidden: {
							iso8601: 'shoppingVisibilityStartISO8601'
						}
					});

					localDatePicker({
						input: '#shoppingVisibilityEnd',
						useTime: 0,
						parseFormat: 'YYYY-MM-DD',
						allowEmptyDate: true,
						ashidden: {
							iso8601: 'shoppingVisibilityEndISO8601'
						}
					});



                    // i18n
                    let shoppingEditBulkPageTranslations = {};
                    jQuery.i18n.properties({
                        name: 'Messages', 
                        path: '/delegatedaccess-tool/bundle/',
                        namespace: 'delegatedaccess-tool',
                        mode: 'both',
                        async: true,
                        cache: true,
                        callback: function(){
                             shoppingEditBulkPageTranslations.multiselectButtonText = jQuery.i18n.prop('selectTools');
                             shoppingEditBulkPageTranslations.searchPlaceholder = jQuery.i18n.prop('searchPlaceholder');
                             //initialize the bootstrap-multiselect lists after translations are ready
                             showPublicToolsMultiselect();
                             showAuthToolsMultiselect();
                        }
                    });
                    

                    function showPublicToolsMultiselect(){
                        $('#showPublicTools').multiselect({
                            filterPlaceholder: shoppingEditBulkPageTranslations.searchPlaceholder,
                            enableCaseInsensitiveFiltering: true,
                            includeSelectAllOption: true,
                            maxHeight:200,
                            buttonText: function(options, select) {
                                return shoppingEditBulkPageTranslations.multiselectButtonText;
                            },
                            
                            onChange: function(option, checked, select) {
                                //console.log(option, option[0].parentElement,checked,select);
                                //let authToolEquivilent = document.querySelector(`#shoppingtabs-2 .multiselect-container input[value='${option[0].value}']`).closest('li');
                                let authToolEquivilent = document.querySelector(`#showAuthTools option[value='${option[0].value}']`);
                                if (checked && typeof authTools !== 'undefined') {
                                    //Add it to the showAuthTools form data
                                    // console.log(`#showAuthTools option[value='${option[0].value}']`);
                                    // document.querySelector(`#showAuthTools option[value='${option[0].value}']`).selected = true;
                                    
                                    //Check it in the showAuthTools visual
//                                     document.querySelector(`#shoppingtabs-2 .multiselect-container input[value='${option[0].value}']`).checked = true;
                                    
                                    // These actually add it to the showAuthTools form data
                                    // $('#showAuthTools').multiselect('select', option[0].value);
                                    // $('#showAuthTools').multiselect('refresh');
                                    
                                    //Disable it in the showAuthTools visual
//                                     let authToolEquivilent = document.querySelector(`#shoppingtabs-2 .multiselect-container input[value='${option[0].value}']`);
//                                     authToolEquivilent.disabled = true;
//                                     let authToolEquivilentParent = authToolEquivilent.closest('li');
//                                     authToolEquivilentParent.className = "disabled";
                                    selectAndDisableAuthTool(authToolEquivilent);
                                    $('#showAuthTools').multiselect('refresh');
                                } else if (!checked && typeof authTools !== 'undefined') {
                                	//logic for unselecting
                                	reenableAuthTool(authToolEquivilent);
                                	$('#showAuthTools').multiselect('refresh');
                                } else {
                                	//not sure how this would happen
                                }
                            },
                            onSelectAll: function(){
                                if ( typeof authTools === 'undefined') {
                                    //Rebuild the bootstrap-multiselect because something went wrong
                                    showAuthToolsMultiselect();
                                } else {
// 									$('#showAuthTools').multiselect('selectAll');

									//Remove the search box from the array
// 									authTools.shift();
                                       
									//Process the rest of the auth tools                                
									authTools.forEach(function(el){
										selectAndDisableAuthTool(el);
									});
                                    $('#showAuthTools').multiselect('refresh');
									//Uncheck the authTools select all
// 									authTools[0].querySelector('input').checked = false;
                                }
                               
                            },
                            onDeselectAll: function() {

                            	authTools.forEach(function(el){
									reenableAuthTool(el);
								});
								$('#showAuthTools').multiselect('refresh');
                            }
                        });
                    }

                    let authTools;
                    function showAuthToolsMultiselect(){
                        $('#showAuthTools').multiselect({
                            filterPlaceholder: shoppingEditBulkPageTranslations.searchPlaceholder,
                            enableCaseInsensitiveFiltering: true,
                            includeSelectAllOption: true,
                            maxHeight:200,
                            buttonText: function(options, select) {
                                return shoppingEditBulkPageTranslations.multiselectButtonText;
                            },
                            onInitialized: function(select, container){
                               //Assign authTools for use elsewhere
                               //authTools = Array.from(document.querySelector('#shoppingtabs-2 .multiselect-container').children);
                               authTools = Array.from(document.querySelector('#showAuthTools').children);
                            }
                        });
                    }
					
//                     function selectAndDisableAuthToolBootstrap (tool){
//                     	//directly affect boostrap-multiselect
//                     	tool.querySelector('input').checked = true;
// 						tool.querySelector('input').disabled = true;
// 						tool.className = "disabled";
                        
//                     }
                    function selectAndDisableAuthTool(tool) {
                    	tool.disabled = true;
                    	tool.selected = true;
                    	
                    }
// 					function reenableAuthToolBootstrap(tool){
// 						tool.querySelector('input').disabled = false;
// 						tool.className = "active";
// 						$('#showAuthTools').multiselect('refresh');
// 					}
					function reenableAuthTool(tool){
						tool.disabled = false;
						
					}

                    //make the select box pretty:
					// $("#showAuthTools").asmSelect({selectClass: "shoppingSetting showAuthTools", removeClass: "asmListItemRemove shoppingSetting"})
					// .each(function(index){
					// 	if(index < 1){
					// 		$("#showPublicTools").asmSelect({selectClass: "shoppingSetting showPublicTools", removeClass: "asmListItemRemove shoppingSetting"})
					// 		.each(function(index){
					// 			//only need to run this once :)
					// 			if(index < 1){
					// 				setShoppingToolOptionsEnabled(true, function(){setShoppingToolOptionsEnabled(false);});
					// 			}
					// 		});
					// 	}
					// });
				// 	$("#showPublicToolsSelectAll").click(function() {
				// 		$("#showPublicTools").children().attr("selected", "selected").end().change();
				// 		setShoppingToolOptionsEnabled(true, function(){setShoppingToolOptionsEnabled(false);});
				// 		return false;
				// 	}); 
				// 	$("#showAuthToolsSelectAll").click(function() {
				// 		$(".showPublicTools option:not(.asmOptionDisabled)").each(
				// 			function(i, elem){
				// 				$("#showAuthTools").children("[value='" + $(elem).val() + "']").attr("selected", "selected").end().change();
				// 			}
				// 		);
				// 		setShoppingToolOptionsEnabled(true, function(){setShoppingToolOptionsEnabled(false);});
				// 		return false;
				// 	});
				// });
				
				// function setShoppingSettingsDisabled(disabled){
				// 	$(".shoppingSetting").attr("disabled", disabled).end().change();
				// 	if(disabled){
				// 		$("a.shoppingSetting").hide();
				// 	}else{
				// 		$("a.shoppingSetting").show();
				// 	}
				// 	//always set these to disabled
				// 	$(".shoppingSettingDisabled").attr("disabled", true).end().change();
				// 	$("a.shoppingSettingDisabled").hide();
				// 	if(!disabled){
				// 		setShoppingToolOptionsEnabled(true, function(){setShoppingToolOptionsEnabled(false);});
				// 	}
				// }
				
				// function setShoppingToolOptionsEnabled(first, callback){
				// 	if(first){
				// 		//remove all the public tool options which were added to the auth list
				// 		//these will be readded below:
				// 		$("#showAuthTools").children(".publicTool").attr("selected", "").attr('disabled', true).end().change();
				// 		$("#showAuthTools").children(".publicTool").removeClass("publicTool");
				// 	}
				// 	$(".showPublicTools option").each(function (i, elem) {
				// 		if("" !== $(elem).val()){
				// 			$(".showAuthTools option").each(function (j, elem2) {
				// 				if($(elem).val() === $(elem2).val()){
				// 					if($(elem).hasClass("asmOptionDisabled")){
				// 						if(first){
				// 							//add the selected public option to the auth options list to
				// 							//show to the user that public tools will show up for auth users as well
				// 							$("#showAuthTools").children("[value='" + $(elem).val() + "']").attr("selected", "selected").attr('disabled', true).end().change();
				// 						}else{
				// 							//hide the "remove" link for auth tools that are in the public tools list
				// 							$("li.asmListItem[rel='" + $(elem2).attr("rel") +"'] a.asmListItemRemove").hide();
				// 							$("#showAuthTools").children("[value='" + $(elem).val() + "']").addClass("publicTool");
				// 						}
				// 					}
				// 				}
				// 			});
				// 		}
					});
				// 	if(callback != null){
				// 		//call back to ourselves and manipulate the <select> tag.  This is because
				// 		//we have to wait for change() to complete first before modifying.
				// 		callback();
				// 	}
				// }