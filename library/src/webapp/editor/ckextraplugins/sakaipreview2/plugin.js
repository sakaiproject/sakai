( function() {
    function getIframe(html) {
        var $iframe = $PBJQ('<iframe>');
        $iframe.attr('srcdoc', html);
        $iframe.attr('frameborder', '0');
        $iframe.attr('width', '100%');
        $iframe.attr('height', $PBJQ(window).height() - 240 + "px");
        return $iframe;
    }
    function openModal(editor, html){
        if ($PBJQ.fn.modal) {
            var $modal = $PBJQ('<div class="modal fade" id="ckeditorPreview" tabindex="-1" role="dialog">'+
                                 '<div class="modal-dialog" role="document">'+
                                   '<div class="modal-content">'+
                                     '<div class="modal-header">'+
                                       '<button type="button" class="button pull-right sakaipreview-close" data-dismiss="modal"></button>'+
                                       '<button type="button" class="button pull-right sakaipreview-print"></button>'+
                                       '<h4 class="modal-title">'+editor.lang.preview.preview+'</h4>'+
                                     '</div>'+
                                     '<div class="modal-body"></div>'+
                                   '</div>'+
                                 '</div>'+
                               '</div>');
            $modal.find(".modal-body").html(getIframe(html.dataValue));

            // if (previewHighlightedOnly) {
            //     var $message = $PBJQ("<div>").addClass("alert alert-info").
            //                               text(editor.lang.sakaipreview.previewFullPreamble).
            //                               css("margin", "20px 0 0");

            //     var $previewAll =  $PBJQ("<a>").attr("href", "javascript:void(0);").
            //                               addClass("btn btn-xs btn-default").
            //                               text(editor.lang.sakaipreview.previewFullButton);

            //     $previewAll.on("click", function() {
            //         previewHighlightedOnly = false;
            //         $modal.find(".modal-body").html(getIframe(getPreviewContentHTML(getEditorContent())));
            //         $message.remove();
            //     });

            //     $message.append($previewAll);

            //     $modal.find(".modal-header").append($message);
            // }

            $PBJQ(document.body).append($modal);

            $modal.find(".sakaipreview-print").on("click", function() {
              $modal.find(".sakaipreview-print").prop("disabled", true);
              // we need to get the editor HTML again and tack on some
              // javascript to do the printing for us!
            //   var html = getPreviewContentHTML(getEditorContent());
            //   if (typeof MathJax != "undefined") {
            //     // print after MathJax has finished rendering
            //     html = html + "<script type='text/javascript'>"+
            //                     "MathJax.Hub.Queue(function () {"+
            //                       "window.print();"+
            //                     "});"+
            //                   "</script>";
            //     } else {
            //       // just print!
            //       html = html + "<script type='text/javascript'>"+
            //                       "window.print();"+
            //                     "</script>";
            //     }
              $modal.find(".modal-body").html(getIframe(html));

              setTimeout(function() {
                $modal.find(".sakaipreview-print").prop("disabled", false);
              }, 3000);
            });

            $modal.modal();

            $modal.find(".modal-body").append(html);
            $modal.on("hidden.bs.modal", function() {
                $modal.remove();
            });

            return false;
        }
    }
    let pluginName = 'sakaipreview2';
    let previewCmd = {
        modes: {
			wysiwyg: 1,
			source: 1
		},
		canUndo: false,
		readOnly: 1,
		exec: 
        //Calling this works, replicates OOTB functionality
        //CKEDITOR.plugins.preview.createPreview
        function( editor ) {
            let contentPreviewData = {};
            editor.once('contentPreview', function( event ) {
                console.log('event.data',event);
                contentPreviewData = event.data;
                console.log(CKEDITOR.dom.window.getFrame);
                return contentPreviewData;
            });
            // editor.on('contentPreview', function( event ) {
            //     return false;
            // });
            
            let plugin = CKEDITOR.plugins.preview;
            plugin.createPreview(editor);
            console.log('contentPreviewData',contentPreviewData);
            // sHTML = contentPreviewData.dataValue.replace( /<head>/, '$&' + baseTag ).replace( /[^>]*(?=<\/title>)/, '$& &mdash; ' + editor.lang.preview.preview );
                
            openModal(editor, contentPreviewData);
        }
    };
    CKEDITOR.plugins.add( pluginName, {
        requires: ['preview'],
        icons: 'sakaipreview2',
        init: function( editor ){
            editor.addCommand( pluginName, previewCmd );
			editor.ui.addButton( 'SakaiPreview2', {
				label: editor.lang.preview.preview,
				command: pluginName,
				toolbar: 'document,40'
			} );
            
        }
    })
})();