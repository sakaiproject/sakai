/*******************************************************************************
 * $URL:  $
 * $Id:  $
 * **********************************************************************************
 *
 * Copyright (c) 2010 The Sakai Foundation
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
 ******************************************************************************/

//http://www.quirksmode.org/js/findpos.html
function findPos(obj) {
    var curleft = curtop = 0;
    if (obj.offsetParent) {
        do {
            curleft += obj.offsetLeft;
            curtop += obj.offsetTop;
        } while (obj = obj.offsetParent);
        return [curleft,curtop];
    }
}
var sakai = sakai || {};
sakai.editor = sakai.editor || {};
sakai.editor.editors = sakai.editor.editors || {};
// Temporarily disable enableResourceSearch till citations plugin is ported (SAK-22862)
sakai.editor.enableResourceSearch = false;

sakai.editor.editors.ckeditor = {};
// Please note that no more parameters should be added to this signature.
// The config object allows for name-based config options to be passed.
// The w and h parameters should be removed as soon as their uses can be migrated.
sakai.editor.editors.ckeditor.launch = function(targetId, config, w, h) {
    var folder = "";

    var collectionId = "";
    if (config != null && config.collectionId) {
        collectionId=config.collectionId;
    }
    else if (sakai.editor.collectionId) {
        collectionId=sakai.editor.collectionId
    }

    if (collectionId) {
        folder = "CurrentFolder=" + collectionId
    }

    var language = sakai.locale && sakai.locale.userLanguage || '';
    var country = sakai.locale && sakai.locale.userCountry || null;

    var ckconfig = {
	//Some defaults for audio recorder
        audiorecorder : {
            "maxSeconds" : 180,
            "attemptAllowed" : Number.MAX_VALUE,
            "attemptsRemaining": Number.MAX_VALUE
        },
        skin: 'moono',
        defaultLanguage: 'en',
        allowedContent: true, // http://docs.ckeditor.com/#!/guide/dev_advanced_content_filter-section-3
        language: language + (country ? '-' + country.toLowerCase() : ''),
        height: 310,
        fileConnectorUrl : '/sakai-fck-connector/web/editor/filemanager/browser/default/connectors/jsp/connector' + collectionId + '?' + folder,

        filebrowserBrowseUrl :'/library/editor/FCKeditor/editor/filemanager/browser/default/browser.html?Connector=/sakai-fck-connector/web/editor/filemanager/browser/default/connectors/jsp/connector' + collectionId + '&' + folder,
        filebrowserImageBrowseUrl : '/library/editor/FCKeditor/editor/filemanager/browser/default/browser.html?Type=Image&Connector=/sakai-fck-connector/web/editor/filemanager/browser/default/connectors/jsp/connector' + collectionId + '&' + folder,
        filebrowserFlashBrowseUrl :'/library/editor/FCKeditor/editor/filemanager/browser/default/browser.html?Type=Flash&Connector=/sakai-fck-connector/web/editor/filemanager/browser/default/connectors/jsp/connector' + collectionId + '&' + folder,
				extraPlugins: (sakai.editor.enableResourceSearch ? 'resourcesearch,' : '')+'',


        // These two settings enable the browser's native spell checking and context menus.
        // Control-Right-Click (Windows/Linux) or Command-Right-Click (Mac) on highlighted words
        // will cause the CKEditor menu to be suppressed and display the browser's standard context
        // menu. In some cases (Firefox and Safari, at least), this supplies corrections, suggestions, etc.
        disableNativeSpellChecker: false,
        browserContextMenuOnCtrl: true,

        toolbar_Basic:
        [
            ['Source', '-', 'Bold', 'Italic', 'Link', 'Unlink']
        ],
        toolbar_Full:
        [
            ['Source','-','Templates'],
            // Uncomment the next line and comment the following to enable the default spell checker.
            // Note that it uses spellchecker.net, displays ads and sends content to remote servers without additional setup.
            //['Cut','Copy','Paste','PasteText','PasteFromWord','-','Print', 'SpellChecker', 'Scayt'],
            ['Cut','Copy','Paste','PasteText','PasteFromWord','-','Print'],
            ['Undo','Redo','-','Find','Replace','-','SelectAll','RemoveFormat'],
            ['NumberedList','BulletedList','-','Outdent','Indent','Blockquote','CreateDiv'],
            '/',
            ['Bold','Italic','Underline','Strike','-','Subscript','Superscript'],
						['atd-ckeditor'],
            ['JustifyLeft','JustifyCenter','JustifyRight','JustifyBlock'],
            ['BidiLtr', 'BidiRtl' ],
            ['Link','Unlink','Anchor'],
            (sakai.editor.enableResourceSearch
                ? ['AudioRecorder','ResourceSearch', 'Image','Movie','Flash','Table','HorizontalRule','Smiley','SpecialChar','fmath_formula','FontAwesome']
                : ['AudioRecorder','Image','Movie','Flash','Table','HorizontalRule','Smiley','SpecialChar','fmath_formula','FontAwesome']),
            '/',
            ['Styles','Format','Font','FontSize'],
            ['TextColor','BGColor'],
            ['Maximize', 'ShowBlocks']
        ],
        toolbar: 'Full',
        resize_dir: 'both',
        //SAK-23418
        pasteFromWordRemoveFontStyles : false,
        pasteFromWordRemoveStyles : false,
        autosave_saveDetectionSelectors : "input[id*='save'],input[name*='save'],input[name*='cancel'],input[id*='cancel']"
    };

    //NOTE: The height and width properties are handled discretely here.
    //      The ultimate intent is that the caller-supplied config will simply
    //      overlay the default config. The outstanding question is whether
    //      some properties should disallow override (because of specific setup
    //      here that we would not want duplicated throughout calling code) or
    //      if their override would just be discouraged. We also probably want
    //      some symbolic things like editorSize: 'small', where the supplied
    //      values are interpreted and translated into dimensions, toolbar set,
    //      and anything else relevant. This will allow editor indifference
    //      on the part of tool code, requesting whatever editor be launched
    //      with appropriate settings applied, rather than detecting the editor
    //      and supplying specific values for the desired effect. This set of
    //      "logical" configuration options is yet to be determined.
    if (config) {
        if (config.width) {
            ckconfig.width = config.width;
        } else if (w) {
            ckconfig.width = w;
        }

        if (config.height) {
            ckconfig.height = config.height;
        } else if (h) {
            ckconfig.height = h;
        }

        if (config && config.toolbarSet && ckconfig['toolbar_' + config.toolbarSet]) {
            ckconfig.toolbar = config.toolbarSet;
		}

		if (config.fullPage) {
			ckconfig.fullPage = true;
		}

		if (config.audiorecorder) {
			ckconfig.audiorecorder = config.audiorecorder;
		}

		if (config.disableBrowseServer)
		{
			ckconfig.filebrowserBrowseUrl = null;
			ckconfig.filebrowserImageBrowseUrl = null;
			ckconfig.filebrowserFlashBrowseUrl = null;
			ckconfig.filebrowserLinkBrowseUrl = null;
		}
	}

		//get path of directory ckeditor 
		//
		var basePath = CKEDITOR.basePath; 
		basePath = basePath.substr(0, basePath.indexOf("ckeditor/"))+"ckextraplugins/"; 
		//To add extra plugins outside the plugins directory, add them here! (And in the variable)
		(function() { 
		   CKEDITOR.plugins.addExternal('movieplayer',basePath+'movieplayer/', 'plugin.js'); 
		   CKEDITOR.plugins.addExternal('wordcount',basePath+'wordcount/', 'plugin.js'); 
		   CKEDITOR.plugins.addExternal('fmath_formula',basePath+'fmath_formula/', 'plugin.js'); 
		   CKEDITOR.plugins.addExternal('audiorecorder',basePath+'audiorecorder/', 'plugin.js'); 
		   CKEDITOR.plugins.addExternal('image2',basePath+'image2/', 'plugin.js'); 
		   CKEDITOR.plugins.addExternal('autosave',basePath+'autosave/', 'plugin.js'); 
		   CKEDITOR.plugins.addExternal('fontawesome',basePath+'fontawesome/', 'plugin.js'); 
			 /*
			  To enable after the deadline uncomment these two lines and add atd-ckeditor to toolbar
			  and to extraPlugins. This also needs extra stylesheets.
			  See readme for more info http://www.polishmywriting.com/atd-ckeditor/readme.html
			  You have to actually setup a server or get an API key
			  Hopefully this will get easier to configure soon.
			 */
			 CKEDITOR.plugins.addExternal('atd-ckeditor',basePath+'atd-ckeditor/', 'plugin.js'); 
			 /*
			 Replace this with your own server if you download it from http://openatd.wordpress.com/
			 Or you can proxy to the public one, see the page for more information.
			 */
			 //ckconfig.atd_rpc='//localhost/proxy/spellcheck';
			 //ckconfig.extraPlugins+="atd-ckeditor,";
			 //ckconfig.contentsCss = basePath+'/atd-ckeditor/atd.css';
			 
			 ckconfig.extraPlugins+="image2,audiorecorder,movieplayer,wordcount,fmath_formula,autosave,fontawesome";
			 
			 //SAK-29648
			 ckconfig.contentsCss = basePath+'/fontawesome/font-awesome/css/font-awesome.min.css';
			 CKEDITOR.dtd.$removeEmpty.span = false;
			 CKEDITOR.dtd.$removeEmpty['i'] = false;
    })();

	  CKEDITOR.replace(targetId, ckconfig);
      //SAK-22505
      CKEDITOR.on('dialogDefinition', function(e) {
          // Take the dialog name and its definition from the event
          // data.
          var dialogName = e.data.name;
          var dialogDefinition = e.data.definition;

          var onShow = dialogDefinition.onShow;
          dialogDefinition.onShow = function() {
              var pos = findPos(e.editor.container.$);
              this.move(this.getPosition().x, pos[1]);
              if (typeof onShow !== 'undefined' && typeof onShow.call === 'function') {
                  var result = onShow.call(this);
                  return result;
              }
          }

          if ( dialogName == 'link' )
          {
              var targetTab = dialogDefinition.getContents('target');
              var linkTypeItems = targetTab.elements[0].children[0].items;
              var itemsNoPopup = [];
              for (i=0;i<linkTypeItems.length;i++) {
                  if (linkTypeItems[i][1] != "popup") {
                      itemsNoPopup.push(linkTypeItems[i]);
                  }
              }
              targetTab.elements[0].children[0].items = itemsNoPopup;

          }

      });
}

sakai.editor.launch = sakai.editor.editors.ckeditor.launch;

