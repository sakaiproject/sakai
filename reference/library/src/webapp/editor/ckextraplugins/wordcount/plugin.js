/**********************************************************************************
 * $URL: $
 * $Id: $
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

/**
* Adds a word count to the status bar.
* Parts of this based off elementpath plugin (runs in same area)
* @package wordcount
*/


(function() {
		//From elementpath
		var emptyHtml = '<span class="cke_empty">&nbsp;</span>';
    
		function isModifier(key) {
		  //In Chrome CKEditor is returning the non adjusted keycode
			if (key == CKEDITOR.SHIFT || key == CKEDITOR.SHIFT + 16 || key == CKEDITOR.CTRL || key == CKEDITOR.CTRL + 17  || key == CKEDITOR.ALT || key == CKEDITOR.ALT+18 )
				return true
			return false 
		}

		function getCharCount(htmlData) {
			return htmlData.length
		}

    /**
	  * Counts the words, from forum.js 
    * @param string htmlData data from form 
    * @return int word count
    */

		function getWordCount(htmlData) {
				var matches = htmlData
				matches = matches.replace(/<[^<|>]+?>|&nbsp;/gi,' ')
				matches = matches.replace(/[\u0080-\u202e\u2030-\u205f\u2061-\ufefe\uff00-\uffff]/g,'x')
				//Quote should still be matched
				matches = matches.replace(/&quot;/g,'"')
				//Match on word boundary followed by spaces or punctuations
				matches = matches.match(/\b[\s?!,.):"]+/g);
				var count = 0;
				if(matches) {
						count = matches.length;
				}
				return count;
		}
    /**
    * Adds the plugin to CKEditor
    */
    CKEDITOR.plugins.add('wordcount', {
				lang: ['en'], 
        init: function(editor) {
						var spaceId = 'cke_wordcount_' + editor.name;
						var spaceElement;
						var getSpaceElement = function()
						{
								if ( !spaceElement )
										spaceElement = CKEDITOR.document.getById( spaceId );
								return spaceElement;
						}

						/**
						* Shows word count
						*/
						var ShowWordCount = function (evt) {
								if (evt && evt.data) {
								//var key = evt.data.getKeystroke();
								var key = evt.data.keyCode;
									if (!isModifier(key)) {
										var editor = evt.editor;
										space = getSpaceElement(editor);
										//use getSnapshot because WIRIS is listening for getData. It looks like getData(true) actually doesn't return anything! Hmm!
										editordata = editor.getSnapshot();
										space.setHtml( editor.lang.wordcount.CharCountTxt + " : " + getCharCount(editordata) + " " + editor.lang.wordcount.WordCountTxt + " : " + getWordCount(editordata) );
									}
								}
						}

						editor.on('instanceReady', function(evt){
										//Word count div needs to exist?? themeSpace can wait for it now
										//Show initial count
										ShowWordCount(evt)
								});


            editor.on('key', ShowWordCount);
            editor.on('paste', ShowWordCount);

	    var createBottom = function (evt) {
		//Creating bottom
		if ( evt.data.space == 'bottom' )
		{
		    evt.data.html +=
//			'<span id="' + spaceId + '_label" class="cke_voice_label">' + editor.lang.elementsPath.eleLabel + '</span>' +
			'<div id="' + spaceId + '" style="float:right" class="cke_wordcount" role="group" aria-labelledby="' + spaceId + '_label">' + emptyHtml + '</div>';
		}
	    };
	    //v4
	    editor.on('uiSpace', createBottom);
	    //v3
	    editor.on('themeSpace', createBottom);
        }
    });
})();
