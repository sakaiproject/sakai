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
    
    /**
	  * Counts the words, from forum.js 
    * @param string htmlData data from form 
    * @return int word count
    */

		function getWordCount(htmlData) {
				var matches = htmlData.replace(/<[^<|>]+?>|&nbsp;/gi,' ').replace(/[\u0080-\u202e\u2030-\u205f\u2061-\ufefe\uff00-\uffff]/g,'x').match(/\b/g);
				var count = 0;
				if(matches) {
						count = matches.length/2;
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
								if (evt) {
										var editor = evt.editor;
										space = getSpaceElement(editor);
										space.setHtml( editor.lang.wordcount.WordCountTxt + " : " + getWordCount(editor.getData()) );
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
