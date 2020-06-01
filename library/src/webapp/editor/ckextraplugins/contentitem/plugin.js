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
                    this.getDialog().addFocusable(this.getElement(), 0);
                    this.focus();
                    // console.log(ContentItemIFrameWindow);
               },

               {
                    onOk : function()
                    {
                        // Dialog onOk callback.
                        // console.log(ContentItemIFrameWindow.returned_content_item);
                        var editor = this._.editor;
                        var items = ContentItemIFrameWindow.returned_content_item;
			if ( items ) for(var i=0; i < items.length; i++) {
                            var item = items[i];
                            console.log(item['@type']);
/*
                            // Deep Link 1.0
                            { "@type" : "LtiLinkItem", ...
                                "placementAdvice" : {
                                    "displayWidth" : 800,
                                        "presentationDocumentTarget" : "iframe",
                                        "displayHeight" : 600
                                }}
                            }

                            // Deep Link 2.0 - Thanks for being different :)
                            {
                                "type": "ltiResourceLink",
                                "title": "A title",
                                "url": "https://lti.example.com/launchMe",

                                "iframe": {
                                    "width": 400,
                                    "height": 890
                                }

*/

                            try {
                                if ( item['@type'] == 'LtiLinkItem' || item['type'] == 'ltiResourceLink' ) {
                                    var iframeString;
                                    // Deep Link 1.0
                                    if ( item.launch && item.placementAdvice && item.placementAdvice.displayWidth && item.placementAdvice.displayWidth > 10 &&
                                        item.placementAdvice.displayHeight && item.placementAdvice.displayHeight > 10 &&
                                        item.placementAdvice.presentationDocumentTarget &&
                                        item.placementAdvice.presentationDocumentTarget == 'iframe' ) {
				                        iframeString = '<br/><iframe src="' + CKEDITOR.tools.htmlEncode(item.launch) + '" '+
                                                'height="'+CKEDITOR.tools.htmlEncode(item.placementAdvice.displayHeight)+'" '+
                                                'width="'+CKEDITOR.tools.htmlEncode(item.placementAdvice.displayWidth)+'" '+
                                                'title="'+CKEDITOR.tools.htmlEncode(item.title)+'" '+
                                                'allowfullscreen="true" webkitallowfullscreen="true" mozallowfullscreen="true" '+
                                                'allow="camera; microphone" ' +
                                                'class="lti-iframe"></iframe><br/>' ;
                                    // Deep Link 2.0
                                    } else if ( item.launch && item.presentation && item.presentation.documentTarget &&
                                        item.presentation.documentTarget == 'iframe' &&
                                        item.presentation.width && item.presentation.height &&
                                        item.presentation.width > 10 && item.presentation.height > 10 ) {
				                        iframeString = '<br/><iframe src="' + CKEDITOR.tools.htmlEncode(item.launch) + '" '+
                                                'height="'+CKEDITOR.tools.htmlEncode(item.iframe.height)+'" '+
                                                'width="'+CKEDITOR.tools.htmlEncode(item.iframe.width)+'" '+
                                                'title="'+CKEDITOR.tools.htmlEncode(item.title)+'" '+
                                                'allowfullscreen="true" webkitallowfullscreen="true" mozallowfullscreen="true" '+
                                                'allow="camera; microphone" ' +
                                                'class="lti-iframe"></iframe><br/>' ;

                                    // ContentItem 2.0 B
                                    } else if ( item.launch && item.iframe && item.iframe.width && item.iframe.height &&
                                        item.iframe.width > 10 && item.iframe.height > 10 ) {
				                        iframeString = '<br/><iframe src="' + CKEDITOR.tools.htmlEncode(item.launch) + '" '+
                                                'height="'+CKEDITOR.tools.htmlEncode(item.iframe.height)+'" '+
                                                'width="'+CKEDITOR.tools.htmlEncode(item.iframe.width)+'" '+
                                                'title="'+CKEDITOR.tools.htmlEncode(item.title)+'" '+
                                                'allowfullscreen="true" webkitallowfullscreen="true" mozallowfullscreen="true" '+
                                                'allow="camera; microphone" ' +
                                                'class="lti-iframe"></iframe><br/>' ;

                                    } else {
				                        iframeString = '<a href="' + CKEDITOR.tools.htmlEncode(item.launch) + '" target="_blank" class="lti-launch">'+CKEDITOR.tools.htmlEncode(item.title)+'</a><br/>' ;
                                    }
                                    console.log(iframeString);
			                        editor.insertHtml(iframeString, 'unfiltered_html');
                                } else if ( item['@type'] == 'ContentItem') {
				    editor.insertHtml( '<a href="' + CKEDITOR.tools.htmlEncode(item.url) + '" target="_blank" class="lti-contentitem">'+CKEDITOR.tools.htmlEncode(item.title)+'</a><br/>' );
                                } else if ( item['@type'] == 'FileItem' && item['mediaType'].startsWith('image/') ) {
				    editor.insertHtml( '<img src="' + CKEDITOR.tools.htmlEncode(item.url) + '" target="_blank" class="lti-image"><br/>' );
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
