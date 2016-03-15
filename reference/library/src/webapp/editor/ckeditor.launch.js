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
var sakai = sakai || {};
sakai.editor = sakai.editor || {};
sakai.editor.editors = sakai.editor.editors || {};
// Temporarily disable enableResourceSearch till citations plugin is ported (SAK-22862)
sakai.editor.enableResourceSearch = false;

sakai.editor.editors.ckeditor = sakai.editor.editors.ckeditor || {} ;

//get path of directory ckeditor 
var basePath = "/library/editor/ckextraplugins/"; 

// Please note that no more parameters should be added to this signature.
// The config object allows for name-based config options to be passed.
// The w and h parameters should be removed as soon as their uses can be migrated.
sakai.editor.editors.ckeditor.launch = function(targetId, config, w, h) {
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

    //http://stackoverflow.com/a/1038781/3708872
    function getWidth() {
      if (self.innerHeight) {
        return self.innerWidth;
      }

      if (document.documentElement && document.documentElement.clientWidth) {
        return document.documentElement.clientWidth;
      }

      if (document.body) {
        return document.body.clientWidth;
      }
    }

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

    if (sakai.editor.editors.ckeditor.browser === "elfinder") {
        // Flag for setting elfinder to build or source version
        // Must be either 'src' or 'build'
        var elfinderBuild = 'build';
        var elfinderUrl = '/library/editor/elfinder/sakai/elfinder.' + elfinderBuild +
            '.html?connector=elfinder-connector/elfinder-servlet/connector';

        var filebrowser = {
            browseUrl :      elfinderUrl + '&startdir=' + collectionId,
            imageBrowseUrl : elfinderUrl + '&startdir=' + collectionId + '&type=images',
            flashBrowseUrl : elfinderUrl + '&startdir=' + collectionId + '&type=flash'
        };

    } else {
        var fckeditorUrl = '/library/editor/FCKeditor/editor/filemanager/browser/default/browser.html' +
            '?Connector=/sakai-fck-connector/web/editor/filemanager/browser/default/connectors/jsp/connector';

        var filebrowser = {
            browseUrl : fckeditorUrl + collectionId + '&' + folder,
            imageBrowseUrl : fckeditorUrl + collectionId + '&' + folder + "&Type=Image",
            flashBrowseUrl : fckeditorUrl + collectionId + '&' + folder + "&Type=Flash"
        };
    }

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
        // This is used for uploading by the autorecorder and fmath_formula plugins.
        // TODO Get this to work with elfinder.
        fileConnectorUrl : '/sakai-fck-connector/web/editor/filemanager/browser/default/connectors/jsp/connector' + collectionId + '?' + folder,

        // These are the general URLs for browsing generally and specifically for images/flash object.
        filebrowserBrowseUrl :      filebrowser.browseUrl,
        filebrowserImageBrowseUrl : filebrowser.imageBrowseUrl,
        filebrowserFlashBrowseUrl : filebrowser.flashBrowseUrl,

        extraPlugins: (sakai.editor.enableResourceSearch ? 'resourcesearch,' : '')+'',


        // These two settings enable the browser's native spell checking and context menus.
        // Control-Right-Click (Windows/Linux) or Command-Right-Click (Mac) on highlighted words
        // will cause the CKEditor menu to be suppressed and display the browser's standard context
        // menu. In some cases (Firefox and Safari, at least), this supplies corrections, suggestions, etc.
        disableNativeSpellChecker: false,
        browserContextMenuOnCtrl: true,

        toolbar_Basic:
        [
            ['Source', '-', 'Bold', 'Italic', 'Underline', '-', 'Link', 'Unlink', '-', 'NumberedList','BulletedList', 'Blockquote']
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
                ? ['AudioRecorder','ResourceSearch', 'Image','Movie','Table','HorizontalRule','Smiley','SpecialChar','fmath_formula','FontAwesome']
                : ['AudioRecorder','Image','Movie','Table','HorizontalRule','Smiley','SpecialChar','fmath_formula','FontAwesome']),
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
        autosave_saveDetectionSelectors : "form input[type='button'],form input[type='submit']",
        //SAK-29598 - Add more templates to CK Editor
        templates_files: [basePath+"templates/default.js"],
        templates: 'customtemplates'
    };

    //To add extra plugins outside the plugins directory, add them here! (And in the variable)
    (function() {
        // SAK-30370 present a nice and simple editor without plugins to the user on a tiny screen.
        if (getWidth() < 800) {
            ckconfig.toolbar = 'Basic';
        }
        else {
            CKEDITOR.plugins.addExternal('lineutils',basePath+'lineutils/', 'plugin.js');
            CKEDITOR.plugins.addExternal('widget',basePath+'widget/', 'plugin.js');
            CKEDITOR.plugins.addExternal('iframedialog',basePath+'iframedialog/', 'plugin.js');
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
        }
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
              var result;
              if (typeof onShow !== 'undefined' && typeof onShow.call === 'function') {
                  result = onShow.call(this);
              }
              var pos = findPos(e.editor.container.$);
              //SAK-29830 - On some pages it was moving too far down the pages, on others it was still moving too far. This fix is intended to cut that significantly.
              this.move(this.getPosition().x, pos[1]*0.25);
              return result;
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

