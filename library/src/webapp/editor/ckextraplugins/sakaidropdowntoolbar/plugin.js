/**
 * @fileOverview The "dropdown-toolbar" plugin that makes it possible to add
 *               ckeditor toolbar buttons, plugins in a dropdown menu.
 *               Overrides the out of the box ckeditor behavior for
 *               toolbarCanCollapse and toolbarStartupExpanded config options
 *
 *               Designed to be used with this ckeditor config
 *               sakaiDropdownToolbar: true
 *               toolbarCanCollapse: true
 *               toolbarStartupExpanded: false
 */
( function() {
    CKEDITOR.plugins.add( 'sakaidropdowntoolbar', {
        requires: ['button', 'toolbar'],
        init: function( editor ){
            //Exit if config not set
            if (!editor.config.sakaiDropdownToolbar) { return; }

            editor.on('loaded', function(event){
                const top = document.getElementById(`${editor.id}_top`);
                const toolbox = document.getElementById(`${editor.id}_toolbox`);
                const toolboxMain = document.querySelector(`#${editor.id}_toolbox .cke_toolbox_main`);
                const toolboxCollapser = document.querySelector(`#${editor.id}_toolbox .cke_toolbox_collapser`);

                //Behavior for Full toolbar
                if (editor.config.toolbar === "Full") {

                    //Show the first row on startup
                    if (!editor.config.toolbarStartupExpanded && editor.config.toolbarCanCollapse){
                        top.classList.add('min');
                        toolboxMain.style.display = 'block';
                    }

                    if (editor.config.toolbarCanCollapse){
                        //Styles to allow collapsing
                        toolbox.style.display = 'flex';
                        toolboxCollapser.style.display = 'block';

                        //Watch for toggling of the collapser 
                        const config = {
                            attributes: true,
                            childList: false,
                            subtree: false
                        };
                        const observer = new MutationObserver(function(mutations) {
                            mutations.forEach(function(mutation) {

                                if (mutation.type === "attributes" && mutation.attributeName === "style") {
                                    if (toolboxMain.style.display === "none") {
                                        toolboxMain.style.display = "block";
                                    } else {
                                        top.classList.toggle('min');
                                    }
                                }
                            });
                        })

                        observer.observe(toolboxMain, config);
                    }
                }

                //Behavior for Basic Toolbar
                else if (editor.config.toolbar === "Basic") {
                    toolboxMain.style.display = 'block';
                    toolboxCollapser.style.display = 'none';
                }
                //If other toolbars are defined, add additional else if statements here
            });
        }
    });
})();