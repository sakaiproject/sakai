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
    CKEDITOR.plugins.wordcount = {
    };

		//From elementpath
		var spaceId; 
		var spaceElement;
		var getSpaceElement = function()
		{
				if ( !spaceElement )
						spaceElement = CKEDITOR.document.getById( spaceId );
				return spaceElement;
		}

		var emptyHtml = '<span class="cke_empty">&nbsp;</span>';
    
    var plugin = CKEDITOR.plugins.wordcount;

    /**
    * Shows word count
    * 
    */
    function ShowWordCount(evt) {
        var editor = evt.editor;
				space = getSpaceElement();
				space.setHtml( editor.lang.WordCountTxt + " : " + getWordCount(editor.getData()) );
    }
    
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
						spaceId = 'cke_wordcount_' + editor.name;
						editor.on('instanceReady', function(){
										//Word count div needs to exist?? themeSpace can wait for it now
						});
            editor.on('key', ShowWordCount);
            editor.on('paste', ShowWordCount);

						editor.on( 'themeSpace', function( event )
								{
										//Creating bottom
										if ( event.data.space == 'bottom' )
										{
												event.data.html +=
														'<span id="' + spaceId + '_label" class="cke_voice_label">' + editor.lang.elementsPath.eleLabel + '</span>' +
														'<div id="' + spaceId + '" style="float:right" class="cke_wordcount" role="group" aria-labelledby="' + spaceId + '_label">' + emptyHtml + '</div>';
										}
								})
        }
    });
})();
