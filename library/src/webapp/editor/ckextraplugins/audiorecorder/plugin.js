//Keep track of the DialogId
//http://cksource.com/forums/viewtopic.php?f=11&t=16040&start=10
var tab1doc=null;
var tab1window=null;
var oAudio=null;
var isNew=true;
var flashPlayer = "/library/editor/ckextraplugins/movieplayer/StrobeMediaPlayback.swf";
var youtubePlugin = "/library/editor/ckextraplugins/movieplayer/YouTubePlugin.swf";
var videoMimeSupported = ['video/mp4','audio/mpeg','application/x-shockwave-flash','video/x-ms-wmv'];
var audioMimeSupported = ['audio/x-wav'];
//IE8 compatibility
if (!String.prototype.trim) {
    String.prototype.trim = function () {
        return this.replace(/^\s+|\s+$/g, '');
    };
}

if (!window.location.origin) { // Some browsers (mainly IE) does not have this property, so we need to build it manually...
  window.location.origin = window.location.protocol + '//' + window.location.hostname + (window.location.port ? (':' + window.location.port) : '');
}


/** Create Movie html */

/** Movie object */
var Audio = function (o){
	this.id = '';
	this.contentType = '';
	this.url = '';
	this.autoplay = '0';
	this.width = '320';
	this.height = '240';
	if(o) this.setObjectElement(o);
};

//Creates an element for CKEditor
Audio.prototype.createCKElement = function () {
    var mediaurl = this.url.trim();

	var audioElement = new CKEDITOR.dom.element('audio');
	audioElement.setAttributes({'class': 'audioaudio'});
	audioElement.setAttributes({'controls': 'controls'});
	var sourceElement = new CKEDITOR.dom.element('source');
	sourceElement.setAttributes({'src': mediaurl});
	sourceElement.setAttributes({'type': this.contentType});
	audioElement.append(sourceElement);
	return audioElement;
}

/** Set movie attribute */
Audio.prototype.setAttribute = function(attr, val) {
	if (val=="true") {
		this[attr]=true;
	} else if (val=="false") {
		this[attr]=false;
	} else {
		this[attr]=val;
	}
}

CKEDITOR.plugins.add( 'audiorecorder',
{
	 requires : ['flash', 'iframedialog' ],
   //http://alfonsoml.blogspot.com/2009/12/plugin-localization-in-ckeditor-vs.html
   lang: ['en'],

   init: function( editor )
   {
      var command = editor.addCommand( 'audiorecorder.cmd', new CKEDITOR.dialogCommand( 'audiorecorder.dlg' ) );
      var thispath = this.path;

      editor.ui.addButton( 'AudioRecorder',
         {
         label: editor.lang.audiorecorder.tooltip,
         command: 'audiorecorder.cmd',
	 icon:  thispath + 'audiorecorder.gif'
      });
	//audio_plugin.js
    CKEDITOR.dialog.add( 'audiorecorder.dlg', function( api ) {
            var dialogDef = {
                title : editor.lang.audiorecorder.dlgtitle, minWidth : 550, minHeight : 230,
                contents : [ {
                        id : 'tab1', label : '', title : '', expand : true, padding : 0,
                        elements : [
				{
                                type : 'iframe',
                                //TODO: Pass the name as a extra parameter to iframe if possible instead
                                src : thispath + 'audiorecorder.html?parentname='+api.name,
                                width : 550, height : 500 - (CKEDITOR.env.ie ? 10 : 0),
                                onContentLoad : function() {
                                    //Save the frameId
                                    var iframe = document.getElementById( this._.frameId );
                                    tab1window=iframe.contentWindow;
                                    tab1doc=tab1window.document;
                                }
                            }
			    /*
                            {
                                type : 'button',
                                id : 'browse',
                                label : editor.lang.common.browseServer,
                                filebrowser :  {
                                    action : 'Browse',
                                    onSelect : function (fileUrl, data) {
                                      tab1doc.getElementById('txtUrl').value = decodeURI(fileUrl);
                                    }
                                }
                            }
			    */
                        ]
                        }
                ],
				//Don't show any buttons
                buttons : [],
				onHide : function () {
					var editor = this._.editor;
					//Try to stop the recording and update the iframe when the dialog is closed
					$("#audio-stop").each(function() {
						$(this).click();
					});
                    // If there's no URL, just stop entirely
					txtUrl = tab1doc.getElementById('txtUrl').value;
					if(txtUrl === undefined || txtUrl.length == 0) {
//                        alert(editor.lang.audiorecorder.error) ;
                        return false ;
                    }

                    var e = (oAudio || new Audio()) ;
                    e.updateObject(tab1doc) ;

					//Fix for IE8 because createFromHtml doesn't work, so have to create it by hand
					var audioElement = e.createCKElement();

					editor.insertElement(audioElement);
				}

            };

            return dialogDef;
        }) ;


   },

});

/** Update Audio object from Form */
Audio.prototype.updateObject = function (tab1doc){
	this.url = tab1doc.getElementById('txtUrl').value;
	this.contentType = this.getContentType(this.url);
    //Might not have this defined
    if (tab1doc.getElementById('txtWidth')) {
        this.width = (isNaN(tab1doc.getElementById('txtWidth').value)) ? 0 : parseInt(tab1doc.getElementById('txtWidth').value);
        this.height = (isNaN(tab1doc.getElementById('txtHeight').value)) ? 0 : parseInt(tab1doc.getElementById('txtHeight').value);
        this.autoplay = (tab1doc.getElementById('chkAutoplay').checked) ? '1' : '0';
    }
};

/** Get the file extension  */
Audio.prototype.getExtension = function (url) {
    var ext = url.match(/\.(wav|avi|asf|fla|flv|mov|mp3|mp4|mpg|mpeg|qt|swf|wma|wmv)\s*$/i);
    if(ext != null && ext.length && ext.length > 0) {
        ext = ext[1];
    }else{
        if(url.contains('youtube.com/')) {
            ext = 'swf';
        }else{
            ext = '';
        }
    }
    return ext;
};

/** Configure content type basing on provided url */
Audio.prototype.getContentType = function (url) {
    var ext = this.getExtension(url);
    var contentType =
            (ext=="mpg"||ext=="mpeg") ? "video/mpeg":
            (ext=="mp4") ? "video/mp4":
            (ext=="mp3") ? "audio/mpeg":
            (ext=="flv"||ext=="fla"||ext=="swf") ? "application/x-shockwave-flash":
            (ext=="wmv"||ext=="wm" ||ext=="avi") ? "video/x-ms-wmv":
            (ext=="asf") ? "video/x-ms-asf":
            (ext=="wav") ? "audio/x-wav":
            (ext=="wma") ? "audio/x-ms-wma":
            (ext=="mov"||ext=="qt") ? "video/quicktime" : "video/x-ms-wmv";
    return contentType;
};

Audio.prototype.setObjectElement = function (e){
		if (!e) return ;
		this.width = GetAttribute( e, 'width', this.width );
		this.height = GetAttribute( e, 'height', this.height );
};

String.prototype.endsWith = function(str)
{return (this.match(str+"$")==str)}

String.prototype.contains = function(str)
{return (this.match(str)==str)}

Array.prototype.contains = function(obj) {
    var i = this.length;
    while (i--) {
        if (this[i] === obj) {
            return true;
        }
    }
    return false;
}
