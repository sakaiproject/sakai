/**
 * @fileOverview The sakaiformat plugin extends the ckeditor format plug.
 *
 *
 */

 CKEDITOR.plugins.add( 'sakaiformat', {
    requires: ['format'],
    lang: ['en'],
    init: function( editor ){
        let sakai_format_config = {
            sakai_format_h3: { element: 'h3', attributes: {'title': editor.lang.sakaiformat.h3}},
        }
        
        editor.on('uiSpace', function(event) {
            if (editor.blockless || editor.elementMode == CKEDITOR.ELEMENT_MODE_INLINE)
                return;

            let output = [''];
            

            if ( editor.config.sakaiFormat ) {
                let format = CKEDITOR.plugins.get('format');
                // console.log(editor,format);

            }
        })
        
        

    }

})