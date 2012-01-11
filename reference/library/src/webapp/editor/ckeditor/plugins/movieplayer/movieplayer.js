var CKEDITOR   = window.parent.CKEDITOR;
var oEditor = null;
var oMovie     = null;
var isNew	   = true;
var flashPlayer = "/library/editor/CKeditor/plugins/movieplayer/player_flv_maxi.swf";

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
	//TODO:Translate dialog box
	oEditor = CKEDITOR.instances[getParameterByName("parentname")];
    //TODO: This should be translated like the old
	//oEditor.FCKLanguageManager.TranslatePage(document);
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
                    value = varsT[1];
                    if (name == 'flv') {
                        oMovie.setAttribute('url', decodeURI(value));
                    } else {
                        oMovie.setAttribute(name, value);
                    }
                }
            } else if (name == 'autostart' || name == 'autoplay') {
                value = (value == 'true' || value == '1') ? 1 : 0;
                oMovie.setAttribute('autoplay', value);
            } else if (name == 'src'
                || (name == 'movie' && !name.endsWith(flashPlayer))) {
                oMovie.setAttribute('url', decodeURI(value));
            } else {
                // Other movie types
                oMovie.setAttribute(name, value);
            }
            isNew = false;
		}
	}
	
	// Read current settings (existing movie)
	GetE('txtUrl').value = oMovie.url;
	if(!isNew) {
		oMovie.contentType = getContentType(oMovie.url);
	}
	GetE('txtWidth').value = oMovie.width;
	GetE('txtHeight').value = oMovie.height;
	GetE('chkAutoplay').checked	= oMovie.autoplay == '1';
	
	//TODO: Show/Hide according to settings, but only if that button works
	//ShowE('tdBrowse', CK.config.LinkBrowserURL);
	return oMovie;
}


/** Browse/upload a file on server */
function BrowseServer() {
    CKEDITOR.tools.callFunction( browseServer );
}



/** Create Movie html */
Movie.prototype.getInnerHTML = function (objectId){
	var rnd = Math.floor(Math.random()*1000001);
	var s = "";

	// html
	if(this.contentType == "application/x-shockwave-flash") {
		if(getExtension(this.url) == 'flv') {
			// Flash video (FLV)
			s += '<OBJECT id="movie' + rnd + '" ';
			s += '        type="application/x-shockwave-flash" ';
			s += '        data="'+ flashPlayer +'" ';
			s += '        width="'+this.width+'" height="'+this.height+'" >';
		    s += '  <PARAM name="movie" value="'+ flashPlayer +'" />';
		    s += '  <PARAM name="FlashVars" value="flv='+encodeURI(this.url)+'&amp;showplayer=always&amp;width='+this.width+'&amp;height='+this.height+'&amp;showiconplay=true&amp;autoplay='+this.autoplay+'" />';
		    s += '</OBJECT>';
		    
		}else{
			// Fix youtube url
			if(this.url.contains('youtube.com/')) {
				this.url = this.url.replace(/youtube\.com\/watch\?v=/i, "youtube.com/v/");
			}
			
			// Flash object (SWF)
			s += '<OBJECT id="movie' + rnd + '" ';
			s += '        type="application/x-shockwave-flash" ';
			s += '        data="'+ encodeURI(this.url) +'" ';
			s += '        width="'+this.width+'" height="'+this.height+'" >';
		    s += '  <PARAM name="movie" value="'+ encodeURI(this.url) +'" />';
		    s += '  <PARAM name="FlashVars" value="autoplay='+this.autoplay+'" />';
		    s += '</OBJECT>';			
		}

	}else{
		// Other video types
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

