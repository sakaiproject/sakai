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
var webJars = "/library/webjars/"

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
        var elfinderUrl = '/library/editor/elfinder/sakai/elfinder.html?connector=elfinder-connector/elfinder-servlet/connector';

        // Add tilde to userId in order to avoid permission error while getting resources from user workspace
        collectionId = collectionId.replace('/user/','/user/~');

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
        skin: 'moono-lisa',
        defaultLanguage: 'en',
        
        // SAK-31829, SAK-33279 Disable functionality in table plugin
        //https://docs.ckeditor.com/#!/guide/dev_disallowed_content-section-how-to-allow-everything-except...
        allowedContent: {
            $1: {
                // Use the ability to specify elements as an object.
                elements: CKEDITOR.dtd,
                attributes: true,
                styles: true,
                classes: true
            }
        },
        disallowedContent: 'table[cellspacing,cellpadding,border]',

        language: language + (country ? '-' + country.toLowerCase() : ''),
        // This is used for uploading by the autorecorder plugin.
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

        // Fix the smileys to a single location
        smiley_path: "/library/editor/ckeditor/plugins/smiley/images/",

        toolbar_Basic:
        [
            ['Source', '-', 'Bold', 'Italic', 'Underline', '-', 'Link', 'Unlink', '-', 'NumberedList','BulletedList', 'Blockquote']
        ],
        toolbar_Full:
        [
            ['About'],
            ['Source','-','Templates'],
            // Uncomment the next line and comment the following to enable the default spell checker.
            // Note that it uses spellchecker.net, displays ads and sends content to remote servers without additional setup.
            //['Cut','Copy','Paste','PasteText','-','Print', 'SpellChecker', 'Scayt'],
            ['Cut','Copy','Paste','PasteText','-','Print', 'SakaiPreview'],
            ['Undo','Redo','-','Find','Replace','-','SelectAll','RemoveFormat'],
            ['NumberedList','BulletedList','-','Outdent','Indent','Blockquote','CreateDiv'],
            '/',
            ['Bold','Italic','Underline','Strike','-','Subscript','Superscript'],
						['atd-ckeditor'],
            ['JustifyLeft','JustifyCenter','JustifyRight','JustifyBlock'],
            ['BidiLtr', 'BidiRtl' ],
            ['Link','Unlink','Anchor'],
            (sakai.editor.enableResourceSearch
                ? ( sakai.editor.contentItemUrl
                    ? ['ContentItem', 'AudioRecorder','ResourceSearch', 'Image','Movie','Table','HorizontalRule','Smiley','SpecialChar']
                    : ['AudioRecorder','ResourceSearch', 'Image','Movie','Table','HorizontalRule','Smiley','SpecialChar']
                  )
		: ( sakai.editor.contentItemUrl
                    ? ['ContentItem', 'AudioRecorder', 'Image','Movie','Table','HorizontalRule','Smiley','SpecialChar']
                    : ['AudioRecorder', 'Image','Movie','Table','HorizontalRule','Smiley','SpecialChar']
                  )
            ),
            '/',
            ['Styles','Format','Font','FontSize'],
            ['TextColor','BGColor'],
            ['Maximize', 'ShowBlocks']
            ,['A11ychecker']
        ],
        toolbar: 'Full',
        resize_dir: 'both',
        //SAK-23418
        pasteFromWordRemoveFontStyles : false,
        pasteFromWordRemoveStyles : false,
        autosave : {
            saveDetectionSelectors : "form input[type='button'],form input[type='submit']",
            //Delay for autosave
            delay: 120,
            //autosave_messageType can be "no" or "notification"
            messageType : "statusbar"
        },

        //wordcount Plugin see https://github.com/w8tcha/CKEditor-WordCount-Plugin for more config options
        //This value should match the one in antisamy (kernel/kernel-impl/src/main/resources/antisamy/low-security-policy.xml)
        wordcount : {
            "maxCharCount" : 1000000,
            //Previous behavior
            "countSpacesAsCharsHTML" : true,
            "countHTML" : true,
            "showParagraphs" : false,
            "showWordCount" : true,
            "showCharCount" : true,
        },

        //SAK-29598 - Add more templates to CK Editor
        templates_files: [basePath+"templates/default.js"],
        templates: 'customtemplates',
        templates_replaceContent: false
    };

    if (config != null && config.baseFloatZIndex) {
	ckconfig.baseFloatZIndex = config.baseFloatZIndex;
    }

    //To add extra plugins outside the plugins directory, add them here! (And in the variable)
    (function() {
        // SAK-30370 present a nice and simple editor without plugins to the user on a tiny screen.
        // 
        //Check for the portal variable (Should be defined)
        //These are defined in user/user-tool-prefs/tool/src/webapp/prefs/editor.jsp

        var detectWidth = true;
        if (typeof portal != 'undefined' && typeof portal.editor != 'undefined' && typeof portal.editor.type == 'string') {
            if (portal.editor.type == "basic") {
                ckconfig.toolbar = "Basic";
                detectWidth = false;
            }
            else if (portal.editor.type == "full") {
                ckconfig.toolbar = "Full";
                detectWidth = false;
            }
        }

        if (detectWidth == true && getWidth() < 800) {
            ckconfig.toolbar = 'Basic';
        }
        //These could be applicable to the basic toolbar
        CKEDITOR.plugins.addExternal('lineutils',basePath+'lineutils/', 'plugin.js');
        CKEDITOR.plugins.addExternal('widget',basePath+'widget/', 'plugin.js');
        CKEDITOR.plugins.addExternal('iframedialog',basePath+'iframedialog/', 'plugin.js');
        CKEDITOR.plugins.addExternal('movieplayer',basePath+'movieplayer/', 'plugin.js');
        CKEDITOR.plugins.addExternal('audiorecorder',basePath+'audiorecorder/', 'plugin.js');
        CKEDITOR.plugins.addExternal('contentitem',basePath+'contentitem/', 'plugin.js');
        CKEDITOR.plugins.addExternal('sakaipreview',basePath+'sakaipreview/', 'plugin.js');
        
        CKEDITOR.plugins.addExternal('image2',webJars+'image2/${ckeditor.image2.version}/', 'plugin.js');

        //Autosave has a dependency on notification
        CKEDITOR.plugins.addExternal('autosave',webJars+'autosave/${ckeditor.autosave.version}/', 'plugin.js');
        CKEDITOR.plugins.addExternal('wordcount',webJars+'wordcount/${ckeditor.wordcount.version}/', 'plugin.js');
        CKEDITOR.plugins.addExternal('notification',basePath+'notification/', 'plugin.js');
        // Accessibility checker has a dependency on balloonpanel
        CKEDITOR.plugins.addExternal('balloonpanel',webJars+'balloonpanel/${ckeditor.balloonpanel.version}/', 'plugin.js');
        CKEDITOR.plugins.addExternal('a11ychecker',webJars+'a11ychecker/${ckeditor.a11ychecker.version}/', 'plugin.js');
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
        //ckconfig.contentsCss = [basePath+'atd-ckeditor/atd.css'];

        ckconfig.extraPlugins+="${ckeditor-extra-plugins}${ckeditor-a11y-extra-plugins}";

        // Load FontAwesome CSS in case a user wants to manually add FA markup
        ckconfig.contentsCss = [webJars+'fontawesome/4.7.0/css/font-awesome.min.css'];
        //If the siteskin is defined, add the print.css
        if (sakai.editor.sitePrintSkin) {
            ckconfig.contentsCss.push(sakai.editor.sitePrintSkin);
        } 
        CKEDITOR.dtd.$removeEmpty.span = false;
        CKEDITOR.dtd.$removeEmpty['i'] = false;
        //Add greek special characters to set
        ckconfig.specialChars = CKEDITOR.config.specialChars.concat([ ["&alpha;","alpha"],["&beta;","beta"],["&gamma;","gamma"],["&delta;","delta"],["&epsilon;","epsilon"],["&zeta;","zeta"],["&eta;","eta"],["&theta;","theta"], ["&iota;","iota"],["&kappa;","kappa"],["&lambda;","lambda"],["&mu;","mu"],["&nu;","nu"],["&xi;","xi"],["&omicron;","omnicron"],["&pi;","pi"],["&rho;","rho"],["&sigma;","sigma"],["&tau;","tau"],["&upsilon;","upsilon"], ["&phi;","phi"],["&chi;","chi"],["&psi;","psi"],["&omega;","omega"],["&Alpha;","Alpha"],["&Beta;","Beta"],["&Gamma;","Gamma"],["&Delta;","Delta"],["&Epsilon;","Epsilon"],["&Zeta;","Zeta"],["&Eta;","Eta"],["&Theta;","Theta"], ["&Iota;","Iota"],["&Kappa;","Kappa"],["&Lambda;","Lambda"],["&Mu;","Mu"],["&Nu;","Nu"],["&Xi;","Xi"],["&Omicron;","Omnicron"],["&Pi;","Pi"],["&Rho;","Rho"],["&Sigma;","Sigma"],["&Tau;","Tau"],["&Upsilon;","Upsilon"], ["&Phi;","Phi"],["&Chi;","Chi"],["&Psi;","Psi"],["&Omega;","Omega"] ]);

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

