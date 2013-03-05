//-------------------------------------------------------------
//	Created by: Ionel Alexandru 
//	Mail: ionel.alexandru@gmail.com
//	Site: www.fmath.info
//---------------------------------------------------------------

(function()
{

	var fmath_instances = 1;
	var fmath_nbFlash = 0;
	var fmath_flashMathML = new Array();
	var fmath_selectedElement = "";
	var fmath_currentElement = "";
	
	var fmath_newDialog = new CKEDITOR.dialogCommand('fmath_formula');
	
	CKEDITOR.plugins.add( 'fmath_formula',
	{
		init : function( editor )
		{

			CKEDITOR.dialog.add('fmath_formula', this.path + 'dialogs/fmath_formula.js');
			editor.addCommand('fmath_formula', fmath_newDialog);
			editor.ui.addButton('fmath_formula', 
				{
					label:'Add MathML Formula',
					command: 'fmath_formula',
					icon: this.path + 'fmath_formula.jpg'
				});

			editor.on( 'selectionChange', function( evt )
			{
				/*
				 * Despite our initial hope, document.queryCommandEnabled() does not work
				 * for this in Firefox. So we must detect the state by element paths.
				 */
				//var command = editor.getCommand( 'fmath_formula' )
				var element = evt.data.path.lastElement.getAscendant( 'img', true );
				fmath_currentElement = "";
				
				if(element!=null){
					var id = element.getAttribute("id");
                    var ml = element.getAttribute("data-mathml");
					if(id!=null && id.indexOf("MathMLEq")>=0){
						fmath_currentElement = id;
                        if (ml!=null) {

                             fmath_flashMathML[fmath_currentElement] = unescape(ml);
                         }
					}
				}
			} );
			
			//search the last Id
		},
		
		addMathML : function(m){
			fmath_nbFlash =fmath_nbFlash + 1;
			var newName = "MathMLEq" + fmath_nbFlash;
			fmath_flashMathML[newName] = m;
			return newName;
		},

		updateMathML : function(id, m){
			fmath_flashMathML[id] = m;
		},

		getSelected : function(){
			return fmath_currentElement;
		},
		
		getCurrentMathML : function(){
			return fmath_flashMathML[fmath_currentElement];
		},

		getMathML : function(name){
			return fmath_flashMathML[name];
		}
		
	});
})();


