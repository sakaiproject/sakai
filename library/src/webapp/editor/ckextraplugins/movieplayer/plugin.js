//Keep track of the DialogId
//http://cksource.com/forums/viewtopic.php?f=11&t=16040&start=10
var tab1doc=null;
var oMovie=null;
var isNew=true;
//var flashPlayer = "/library/editor/ckeditor/plugins/movieplayer/player_flv_maxi.swf";
var flashPlayer = "/library/editor/ckextraplugins/movieplayer/StrobeMediaPlayback.swf";
var youtubePlugin = "/library/editor/ckextraplugins/movieplayer/YouTubePlugin.swf";
var mimeSupported = ['video/mp4','audio/mpeg','application/x-shockwave-flash','video/x-ms-wmv'];

/** Create Movie html */

/** Movie object */
var Movie = function (o){
	this.id = '';
	this.contentType = '';
	this.url = '';
	this.autoplay = '0';
	this.width = '320';
	this.height = '240';
	if(o) this.setObjectElement(o);
};

/** Create Movie html */
Movie.prototype.getInnerHTML = function (objectId){
	var rnd = Math.floor(Math.random()*1000001);
	var s = "";

	// html
	if(mimeSupported.contains(this.contentType)) {
			var addYoutube = "";
			if(this.url.contains('youtube.com/')) {
				this.url = this.url.replace(/youtube\.com\/watch\?v=/i, "youtube.com/v/");
				addYoutube = "&amp;plugin_YouTubePlugin="+youtubePlugin;
			}

			else if (this.url.contains('youtu.be/')) {
			    this.url = this.url.replace("/youtu.be/", "youtube.com/v/");
			    addYoutube = "&amp;plugin_YouTubePlugin="+youtubePlugin;
		        }
	
			// Flash video (FLV)
			s += '<OBJECT id="movie' + rnd + '" ';
			s += '        type="application/x-shockwave-flash" ';
			s += '        data="'+ flashPlayer +'" ';
			s += '        width="'+this.width+'" height="'+this.height+'" >';
			s += '  <PARAM name="movie" value="'+ flashPlayer +'" />';
		    s += '  <PARAM name="FlashVars" value="src='+encodeURI(this.url)+'&amp;showplayer=always&amp;width='+this.width+'&amp;height='+this.height+'&amp;showiconplay=true&amp;autoplay='+this.autoplay+addYoutube+'" />';
				s += '<param name="allowFullScreen" value="true">';
		    s += '</OBJECT>';
	}else{
		// Other video types that need a native plugin
		var pluginspace, codebase, classid;
		if(this.contentType == "video/quicktime") {
			// QUICKTIME
			this.autoplay = (this.autoplay == 'true' || this.autoplay == '1') ? 'true' : 'false';
			s += '<OBJECT id="movie' + rnd + '" ';
			s += '        classid="clsid:02BF25D5-8C17-4B23-BC80-D3488ABDDC6B" ';
			s += '        codebase="http://www.apple.com/qtactivex/qtplugin.cab" '
			s += '        width="'+this.width+'" height="'+this.height+'" >';
		    s += '  <PARAM name="src" value="'+ encodeURI(this.url) +'" />';
			s += '  <PARAM name="autoplay" value="'+this.autoplay+'" />';
			s += '  <PARAM name="controller" value="true" />';
			s += '  <OBJECT type="'+this.contentType+'" ';
			s += '          data="'+ encodeURI(this.url) +'" ';
			s += '          width="'+this.width+'" height="'+this.height+'" ';
			s += '          style="*display:none">'; // for IE6 only
			s += '    <PARAM name="autoplay" value="'+this.autoplay+'" />';
			s += '    <PARAM name="controller" value="true" />';
		    s += '  </OBJECT>';
		    s += '</OBJECT>';	
		    
		}else{
			// WINDOWS MEDIA & OTHERS
			s += '<OBJECT id="movie' + rnd + '" ';
			s += '        type="'+this.contentType+'" ';
			s += '        data="'+ encodeURI(this.url) +'" ';
			s += '        width="'+this.width+'" height="'+this.height+'" >';
		    s += '  <PARAM name="src" value="'+ encodeURI(this.url) +'" />';
			s += '  <PARAM name="autostart" value="'+this.autoplay+'" />';
			s += '  <PARAM name="controller" value="true" />';
		    s += '</OBJECT>';
		    
		}
	    
	}
	
	return s;
}


/** Set movie attribute */
Movie.prototype.setAttribute = function(attr, val) {
	if (val=="true") {
		this[attr]=true;
	} else if (val=="false") {
		this[attr]=false;
	} else {
		this[attr]=val;
	}
}

CKEDITOR.plugins.add( 'movieplayer',
{

	 requires : [ 'fakeobjects', 'flash', 'iframedialog' ],
   //http://alfonsoml.blogspot.com/2009/12/plugin-localization-in-ckeditor-vs.html
   lang: ['en','pt'],
   getPlaceholderCss : function () {
       return 'img.cke_movieplayer' +
		    '{' +
		    'background-image: url(' + CKEDITOR.getUrl( this.path + 'images/placeholder.png' ) + ');' +
		    'background-position: center center;' +
		    'background-repeat: no-repeat;' +
		    'border: 1px solid #a9a9a9;' +
		    'width: 80px;' +
		    'height: 80px;' +
		    '}';
   },
   onLoad: function() {
       //v4
       if (CKEDITOR.addCss) {
	   CKEDITOR.addCss(this.getPlaceholderCss());
       }
   },
   init: function( editor )
   {
      var command = editor.addCommand( 'movie.cmd', new CKEDITOR.dialogCommand( 'movie.dlg' ) );
      var thispath = this.path;

      editor.ui.addButton( 'Movie',
         {
         label: editor.lang.movieplayer.MoviePlayerTooltip,
         command: 'movie.cmd',
	 icon:  thispath + 'filmreel.gif'
      });
      //v3
      if (editor.addCss) { 
	  editor.addCss(this.getPlaceholderCss());
      }
	//movie_plugin.js
    CKEDITOR.dialog.add( 'movie.dlg', function( api ) {
            var dialogDef = {
                title : editor.lang.movieplayer.MoviePlayerDlgTitle, minWidth : 390, minHeight : 230,
                contents : [ {
                        id : 'tab1', label : '', title : '', expand : true, padding : 0,
                        elements : [ {
                                type : 'iframe',
                                //TODO: Pass the name as a extra parameter to iframe if possible instead
                                src : thispath + 'movieplayer.html?parentname='+api.name,
                                width : 450, height : 260 - (CKEDITOR.env.ie ? 10 : 0),
                                onContentLoad : function() {
                                    //Save the frameId
                                    var iframe = document.getElementById( this._.frameId );
                                    tab1doc=iframe.contentWindow.document;
                                }
                            },
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
                        ]
                        }
                ],
                buttons : [ CKEDITOR.dialog.okButton, CKEDITOR.dialog.cancelButton ],
                onOk : function() {
                    // Accessing dialog elements:
                    if(tab1doc.getElementById('txtUrl').value.length == 0) {
                        alert(editor.lang.movieplayer.MoviePlayerNoUrl) ;
                        return false ;
                    }
                    //TODO: What's this do?
                    //oEditor.FCKUndo.SaveUndoStep();

                    var e = (oMovie || new Movie()) ;
                    updateMovieObject(e,tab1doc) ;
										//debugger;
										var realElement = CKEDITOR.dom.element.createFromHtml(e.getInnerHTML());
										var fakeElement;
                    if(!isNew) {
                        //TODO: Is this still a problem with Safari?
                        if(!navigator.userAgent.contains('Safari')) {
                            //FCK.Selection.Delete();
                        }
												fakeElement= this._.editor.createFakeElement( realElement, 'cke_movieplayer', 'movieplayer', true );
                    }else{
												fakeElement= this._.editor.createFakeElement( realElement, 'cke_movieplayer', 'movieplayer', true );
                    }

										this._.editor.insertHtml(fakeElement.getOuterHtml());
                }

            };

            return dialogDef;
        }) ;


   }
});

/** Update Movie object from Form */
function updateMovieObject(e,tab1doc){
	e.url = tab1doc.getElementById('txtUrl').value;
	e.contentType = getContentType(e.url);
	e.width = (isNaN(tab1doc.getElementById('txtWidth').value)) ? 0 : parseInt(tab1doc.getElementById('txtWidth').value);
	e.height = (isNaN(tab1doc.getElementById('txtHeight').value)) ? 0 : parseInt(tab1doc.getElementById('txtHeight').value);
	e.autoplay = (tab1doc.getElementById('chkAutoplay').checked) ? '1' : '0';
}

/** Get the file extension  */
function getExtension(url) {
    var ext = url.match(/\.(avi|asf|fla|flv|mov|mp3|mp4|mpg|mpeg|qt|swf|wma|wmv)$/i);
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
}

/** Configure content type basing on provided url */
function getContentType(url) {
    var ext = getExtension(url);
    var contentType =
            (ext=="mpg"||ext=="mpeg") ? "video/mpeg":
            (ext=="mp4") ? "video/mp4":
            (ext=="mp3") ? "audio/mpeg":
            (ext=="flv"||ext=="fla"||ext=="swf") ? "application/x-shockwave-flash":
            (ext=="wmv"||ext=="wm" ||ext=="avi") ? "video/x-ms-wmv":
            (ext=="asf") ? "video/x-ms-asf":
            (ext=="wma") ? "audio/x-ms-wma":
            (ext=="mov"||ext=="qt") ? "video/quicktime" : "video/x-ms-wmv";
    return contentType;
}

Movie.prototype.setObjectElement = function (e){
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
