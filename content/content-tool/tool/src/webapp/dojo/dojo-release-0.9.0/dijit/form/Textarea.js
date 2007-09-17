if(!dojo._hasResource["dijit.form.Textarea"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["dijit.form.Textarea"] = true;
dojo.provide("dijit.form.Textarea");

dojo.require("dijit.form._FormWidget");
dojo.require("dojo.i18n");
dojo.requireLocalization("dijit.form", "Textarea", null, "ROOT");

dojo.declare(
	"dijit.form.Textarea",
	dijit.form._FormWidget,
{
	// summary
	//	A textarea that resizes vertically to contain the data.
	//	Takes nearly all the parameters (name, value, etc.) that a vanilla textarea takes.
	//	Cols is not supported and the width should be specified with style width.
	//	Rows is not supported since this widget adjusts the height.
	// usage:
	//	<textarea dojoType="dijit.form.TextArea">...</textarea>
	templateString: (dojo.isIE || dojo.isSafari || dojo.isMozilla) ?
				((dojo.isIE || dojo.isSafari) ? '<fieldset id="${id}" class="dijitInlineBox dijitInputField dijitTextArea"><div dojoAttachPoint="editNode" waiRole="textarea" tabIndex="${tabIndex}" style="text-decoration:none;_padding-bottom:16px;display:block;overflow:auto;" contentEditable="true"></div>'
					: '<span id="${id}" class="dijitReset"><iframe dojoAttachPoint="iframe, styleNode" dojoAttachEvent="onblur:_onIframeBlur" src="javascript:void(0)" class="dijitInlineBox dijitInputField dijitTextArea"></iframe>')
				+ '<textarea name="${name}" value="${value}" dojoAttachPoint="formValueNode" style="display:none;"></textarea>'
				+ ((dojo.isIE || dojo.isSafari) ? '</fieldset>':'</span>')
			: '<textarea id="${id}" name="${name}" value="${value}" dojoAttachPoint="formValueNode,editNode" class="dijitInputField dijitTextArea"></textarea>',

	_nlsResources: null,	// Needed for screen readers on FF2

	focus: function(){
		// summary: Received focus, needed for the InlineEditBox widget
		if(!this.disabled){
			this._changing(); // set initial height
		}
		if(dojo.isMozilla){
			dijit.focus(this.iframe);
		}else{
			dijit.focus(this.focusNode);
		}
	},

	setValue: function(/*String*/ value, /*Boolean, optional*/ priorityChange){
		var editNode = this.editNode;
		if(typeof value == "string"){
			editNode.innerHTML = ""; // wipe out old nodes
			if(value.split){
				var _this=this;
				var isFirst = true;
				dojo.forEach(value.split("\n"), function(line){
					if(isFirst){ isFirst = false; }
					else {
						editNode.appendChild(document.createElement("BR")); // preserve line breaks
					}
					editNode.appendChild(document.createTextNode(line)); // use text nodes so that imbedded tags can be edited
				});
			}else{
				editNode.appendChild(document.createTextNode(value));
			}
			if(this.iframe){
				this.sizeNode = document.createElement('div');
				editNode.appendChild(this.sizeNode);
			}
		}else{
			// blah<BR>blah --> blah\nblah
			// <P>blah</P><P>blah</P> --> blah\nblah
			// <DIV>blah</DIV><DIV>blah</DIV> --> blah\nblah
			// &amp;&lt;&gt; -->&< >
			value = editNode.innerHTML;
			if(this.iframe){ // strip sizeNode
				value = value.replace(/<div><\/div>\r?\n?$/i,"");
			}
			value = value.replace(/\s*\r?\n|^\s+|\s+$|&nbsp;/g,"").replace(/>\s+</g,"><").replace(/<\/(p|div)>$|^<(p|div)[^>]*>/gi,"").replace(/([^>])<div>/g,"$1\n").replace(/<\/p>\s*<p[^>]*>|<br[^>]*>/gi,"\n").replace(/<[^>]*>/g,"").replace(/&amp;/gi,"\&").replace(/&lt;/gi,"<").replace(/&gt;/gi,">");
		}
		this.formValueNode.value = value;
		if(this.iframe){
			var newHeight = this.sizeNode.offsetTop;
			if(editNode.scrollWidth > editNode.clientWidth){ newHeight+=16; } // scrollbar space needed?
			if(this.lastHeight != newHeight){ // cache size so that we don't get a resize event because of a resize event
				if(newHeight == 0){ newHeight = 16; } // height = 0 causes the browser to not set scrollHeight
				dojo.contentBox(this.iframe, {h: newHeight});
				this.lastHeight = newHeight;
			}
		}
		dijit.form.Textarea.superclass.setValue.call(this, value, priorityChange);
	},

	getValue: function(){
		return this.formValueNode.value;
	},

	postMixInProperties: function(){
		dijit.form.Textarea.superclass.postMixInProperties.apply(this,arguments);
		// don't let the source text be converted to a DOM structure since we just want raw text
		if(this.srcNodeRef && this.srcNodeRef.innerHTML != ""){
			this.value = this.srcNodeRef.innerHTML;
			this.srcNodeRef.innerHTML = "";
		}
		if((!this.value || this.value == "") && this.srcNodeRef && this.srcNodeRef.value){
			this.value = this.srcNodeRef.value;
		}
		if(!this.value){ this.value = ""; }
		this.value = this.value.replace(/\r\n/g,"\n").replace(/&gt;/g,">").replace(/&lt;/g,"<").replace(/&amp;/g,"&");
	},

	postCreate: function(){
		if(dojo.isIE || dojo.isSafari){
			this.domNode.style.overflowY = 'hidden';
			this.eventNode = this.focusNode = this.editNode;
			this.connect(this.eventNode, "oncut", this._changing);
			this.connect(this.eventNode, "onpaste", this._changing);
		}else if(dojo.isMozilla){
			var w = this.iframe.contentWindow;
			var d = w.document;
			// In the case of Firefox an iframe is used and when the text gets focus,
			// focus is fired from the document object.  There isn't a way to put a
			// waiRole on the document object and as a result screen readers don't
			// announce the role.  As a result screen reader users are lost.
			//
			// An additional problem is that the browser gives the document object a
			// very cryptic accessible name, e.g.
			// wyciwyg://13/http://archive.dojotoolkit.org/nightly/dojotoolkit/dijit/tests/form/test_InlineEditBox.html
			// When focus is fired from the document object, the screen reader speaks
			// the accessible name.  The cyptic accessile name is confusing.
			//
			// A workaround for both of these problems is to give the iframe's
			// document a title, the name of which is similar to a role name, i.e.
			// "edit area".  This will be used as the accessible name which will replace
			// the cryptic name and will also convey the role information to the user.
			// Because it is read directly to the user, the string must be localized.
			this._nlsResources = dojo.i18n.getLocalization("dijit.form", "Textarea");
			d.open();
			d.write('<html><head><title>' +
				this._nlsResources.iframeTitle1 +	// "edit area"
				'</title></head><body style="margin:0px;padding:0px;border:0px;"></body></html>');
				// body > br style is to remove the <br> that gets added by FF
			d.close();
			this.editNode = d.body;
			this.iframe.style.overflowY = 'hidden';
			// resize is a method of window, not document
			this.eventNode = d;
			this.focusNode = this.editNode;
			this.connect(w, "resize", this._changed); // resize is only on the window object
		}else{
			this.focusNode = this.domNode;
		}
		if(this.eventNode){
			this.connect(this.eventNode, "keypress", this._onKeyPress);
			this.connect(this.eventNode, "mousemove", this._changed);
			this.connect(this.eventNode, "focus", this._focused);
			this.connect(this.eventNode, "blur", this._blurred);
		}
		if(this.editNode){
			this.connect(this.editNode, "change", this._changed); // needed for mouse paste events per #3479
		}
		this.inherited('postCreate', arguments);
	},

	// event handlers, you can over-ride these in your own subclasses
	_focused: function(e){
		dojo.addClass(this.iframe||this.domNode, "dijitInputFieldFocused");
		this._changed(e);
	},

	_blurred: function(e){
		dojo.removeClass(this.iframe||this.domNode, "dijitInputFieldFocused");
		this._changed(e, true);
	},

	_onIframeBlur: function(){
		// Reset the title back to "edit area".
		this.iframe.contentDocument.title = this._nlsResources.iframeTitle1;
	},

	_onKeyPress: function(e){
		if(e.keyCode == dojo.keys.TAB && !e.shiftKey && !e.ctrlKey && !e.altKey && this.iframe){
			// Pressing the tab key in the iframe (with designMode on) will cause the
			// entry of a tab character so we have to trap that here.  Since we don't
			// know the next focusable object we put focus on the iframe and then the
			// user has to press tab again (which then does the expected thing).
			// A problem with that is that the screen reader user hears "edit area"
			// announced twice which causes confusion.  By setting the
			// contentDocument's title to "edit area frame" the confusion should be
			// eliminated.
			this.iframe.contentDocument.title = this._nlsResources.iframeTitle2;
			// Place focus on the iframe. A subsequent tab or shift tab will put focus
			// on the correct control.
			// Note: Can't use this.focus() because that results in a call to 
			// dijit.focus and if that receives an iframe target it will set focus
			// on the iframe's contentWindow.
			this.iframe.focus();  // this.focus(); won't work
			dojo.stopEvent(e);
		}else if(e.keyCode == dojo.keys.ENTER){
			e.stopPropagation();
		}else if(this.inherited("_onKeyPress", arguments) && this.iframe){
			// #3752:
			// The key press will not make it past the iframe.
			// If a widget is listening outside of the iframe, (like InlineEditBox)
			// it will not hear anything.
			// Create an equivalent event so everyone else knows what is going on.
			var te = document.createEvent("KeyEvents");
			te.initKeyEvent("keypress", true, true, null, e.ctrlKey, e.altKey, e.shiftKey, e.metaKey, e.keyCode, e.charCode);
			this.iframe.dispatchEvent(te);
		}
		this._changing();
	},

	_changing: function(e){
		// summary: event handler for when a change is imminent
		setTimeout(dojo.hitch(this, "_changed", e, false), 1);
	},

	_changed: function(e, priorityChange){
		// summary: event handler for when a change has already happened
		if(this.iframe && this.iframe.contentDocument.designMode != "on"){
			this.iframe.contentDocument.designMode="on"; // in case this failed on init due to being hidden
		}
		this.setValue(null, priorityChange);
	}
});

}
