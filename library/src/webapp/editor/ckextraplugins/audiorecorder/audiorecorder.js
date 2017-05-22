var CKEDITOR   = window.parent.CKEDITOR;
var oEditor = CKEDITOR.instances[getParameterByName("parentname")];
var oMovie     = null;
var isNew	   = true;
//var flashPlayer = "/library/editor/CKeditor/plugins/movieplayer/player_flv_maxi.swf";
var flashPlayer = "/library/editor/CKeditor/plugins/movieplayer/StrobeMediaPlayback.swf";


function GetAttribute(element,value,defaultvalue) {
    if (element.attributes[value])
        return element.attributes[value];
    return defaultvalue
}

//From FCKEditor fck_dialog_common.js
// Gets a element by its Id. Used for shorter coding.

function GetE( elementId )
{
    return document.getElementById( elementId )  ;
}

function ShowE( element, isVisible )
{
        if ( typeof( element ) == 'string' )
                element = GetE( element ) ;
        element.style.display = isVisible ? '' : 'none' ;
}

//http://stackoverflow.com/questions/901115/get-query-string-values-in-javascript
function getParameterByName(name)
{
  name = name.replace(/[\[]/, "\\\[").replace(/[\]]/, "\\\]");
  var regexS = "[\\?&]" + name + "=([^&#]*)";
  var regex = new RegExp(regexS);
  var results = regex.exec(window.location.href);
  if(results == null)
    return "";
  else
    return decodeURIComponent(results[1].replace(/\+/g, " "));
}
/** Initial setup */
window.onload = function() { 
	// Load selected movie
	loadMovieSelection();
}


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


/** Load FCKEditor selection */
function loadMovieSelection() {
	oMovie = new Movie();
        var oSel = oEditor.getSelection().getSelectedElement();
	if(oSel != null) {
        var realHtml = oSel.data('cke-realelement')
        var realFragment = realHtml && new CKEDITOR.htmlParser.fragment.fromHtml( decodeURIComponent( realHtml ) );
        var oSelConv = realFragment && realFragment.children[ 0 ];
       
        if(oSelConv) {
			oSel = oSelConv;
		}

		if (oSel.id != null && oSel.id.match(/^movie[0-9]*$/)) {
			oMovie.setAttribute('id', oSel.id);
		}
        for ( var i = 0; i < oSel.children.length; i++) {
            var name = GetAttribute(oSel.children[i], 'name');
            var value = GetAttribute(oSel.children[i], 'value');
            if (name == 'FlashVars') {
                // Flash video
                var vars = value.split('&');
                for ( var fv = 0; fv < vars.length; fv++) {
                    var varsT = vars[fv].split('=');
                    name = varsT[0];
                    // If the text was escaped, we split on & and left amp; 
                    name = name.replace('amp;', '');
                    value = varsT[1];
                    if (name == 'flv' || name == 'src') {
                        oMovie.setAttribute('url', decodeURI(value));
                    } else {
                        oMovie.setAttribute(name, value);
                    }
                }
            } else if (name == 'autostart' || name == 'autoplay') {
                value = (value == 'true' || value == '1') ? 1 : 0;
                oMovie.setAttribute('autoplay', value);
            } else if (name == 'src') {
                oMovie.setAttribute('url', decodeURI(value));
            } else {
                // Other movie types
                oMovie.setAttribute(name, value);
            }
            isNew = false;
		}
	}
	
	/*
	// Read current settings (existing movie)
	//GetE('txtUrl').value = oMovie.url;
	if(!isNew) {
		oMovie.contentType = getContentType(oMovie.url);
	}
	GetE('txtWidth').value = oMovie.width;
	GetE('txtHeight').value = oMovie.height;
	GetE('chkAutoplay').checked	= oMovie.autoplay == '1';
	
	//TODO: Show/Hide according to settings, but only if that button works
	//ShowE('tdBrowse', CK.config.LinkBrowserURL);
	return oMovie;
  */
}


/** Browse/upload a file on server */
function BrowseServer() {
    CKEDITOR.tools.callFunction( browseServer );
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
};


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

/** Set selected URL from Browser */
function SetUrl(url) {
	GetE('txtUrl').value = url;
}

