/**
 * Basic sample plugin inserting current date and time into CKEditor editing area.
 */

// Register the plugin with the editor.
// http://docs.cksource.com/ckeditor_api/symbols/CKEDITOR.plugins.html
var ContentItemIFrameWindow = null;
CKEDITOR.plugins.add( 'contentitem',
{ requires : [ 'iframedialog' ], lang: ['en'],
    // The plugin initialization logic goes inside this method.
    // http://docs.cksource.com/ckeditor_api/symbols/CKEDITOR.pluginDefinition.html#init
    init: function( editor )
    {
        // http://ckeditor.com/forums/CKEditor-3.x/iframe-dialog-how-get-ok-button-pressed-event
        // https://gist.github.com/garryyao/1170303
        var height = 480, width = 750;
        CKEDITOR.dialog.addIframe(
               'ContentItemDialog',
               'Select Content Item',
               sakai.editor.contentItemUrl, width, height,
               function()
               {
                    // Iframe loaded callback.
                    var iframe = document.getElementById( this._.frameId );
                    ContentItemIFrameWindow = iframe.contentWindow;
                    // console.log(ContentItemIFrameWindow);
               },

               {
                    onOk : function()
                    {
                        // Dialog onOk callback.
                        // console.log(ContentItemIFrameWindow.returned_content_item);
                        var items = ContentItemIFrameWindow.returned_content_item;
			if ( items ) for(var i=0; i < items.length; i++) {
                            var item = items[i];
                            console.log(item['@type']);
                            try {
                                if ( item['@type'] == 'LtiLinkItem') {
				    editor.insertHtml( '<a href="' + item.launch + '" target="_blank" class="lti-launch">'+item.title+'</a><br/>' );
                                } else if ( item['@type'] == 'ContentItem') {
				    editor.insertHtml( '<a href="' + item.url + '" target="_blank" class="lti-contentitem">'+item.title+'</a><br/>' );
                                } else if ( item['@type'] == 'FileItem' && item['mediaType'].startsWith('image/') ) {
				    editor.insertHtml( '<img src="' + item.url + '" target="_blank" class="lti-image"><br/>' );
                                } else {
                                    console.log('Not handled: '+item['@type']);
                                }
                            } catch(err) {
                                console.log(err);
                            }
                        }
                    }
               }
        );
        editor.addCommand( 'ContentItemDialog', new CKEDITOR.dialogCommand( 'ContentItemDialog' ) );

        // Create a toolbar button that executes the plugin command.
        // http://docs.cksource.com/ckeditor_api/symbols/CKEDITOR.ui.html#addButton
	editor.ui.addButton( 'ContentItem',
        {
            // Toolbar button tooltip.
            label: 'Insert ContentItem',
            // Reference to the plugin command name.
            command: 'ContentItemDialog',
            // Button's icon file path.
            icon: this.path + 'images/contentitem.png'
        } );
    }
} );
