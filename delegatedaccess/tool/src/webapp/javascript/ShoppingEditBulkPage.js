/* global includeWebjarLibrary, localDatePicker */
/* for eslint and codacy */
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



    // i18n load translations from message bundles
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
            //Keep the dropdown button text constant
            buttonText: function(options, select) {
                return shoppingEditBulkPageTranslations.multiselectButtonText;
            },
            //Click handler for individual select/deselects
            onChange: function(option, checked, select) {
                if ( typeof authTools === 'undefined') {
                    //Rebuild the bootstrap-multiselect because something went wrong
                    showAuthToolsMultiselect();
                } 
                
                let authToolEquivilent = document.querySelector(`#showAuthTools option[value='${option[0].value}']`);
                if (checked && typeof authTools !== 'undefined') {
                    //After selecting a public tool, also
                    //select and disable it in the showAuthTools multiselect
                    selectAndDisableAuthTool(authToolEquivilent);
                    $('#showAuthTools').multiselect('refresh');
                } else if (!checked && typeof authTools !== 'undefined') {
                    //After unselecting a public tool, also
                    //reenable and leave it checked in the showAuthTools multiselect
                    reenableAuthTool(authToolEquivilent);
                    $('#showAuthTools').multiselect('refresh');
                } 
            },
            //Click handler for the select all option
            onSelectAll: function(){
                if ( typeof authTools === 'undefined') {
                    //Rebuild the bootstrap-multiselect because something went wrong
                    showAuthToolsMultiselect();
                } 

                //After selecting all the public tools,
                //also select and disable all of the auth tools                                
                authTools.forEach(function(el){
                    selectAndDisableAuthTool(el);
                });
                $('#showAuthTools').multiselect('refresh');
            },
            //Click handler for deselecting all
            onDeselectAll: function() {
                //After unselecting all the public tools, also
                //reenable and leave checked all the auth tools
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
            //Keep the dropdown button text constant
            buttonText: function(options, select) {
                return shoppingEditBulkPageTranslations.multiselectButtonText;
            },
            onInitialized: function(select, container){
                //Assign authTools for use elsewhere
                authTools = Array.from(document.querySelector('#showAuthTools').children);
            }
        });
    }
    
    /**
     * Select and disable an auth tool 
     * @param tool a Javascript object containing the HTML <option> 
     * from the public tool list to manipulate in the auth tool list
     */
    function selectAndDisableAuthTool(tool) {
        tool.disabled = true;
        tool.selected = true; 
    }

    /**
     * Reenable an auth tool 
     * @param tool a Javascript object containing the HTML <option> 
     * from the public tool list to manipulate in the auth tool list
     */
    function reenableAuthTool(tool){
        tool.disabled = false;  
    }
});
