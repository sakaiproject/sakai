if(!dojo._hasResource["dijit.Declaration"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["dijit.Declaration"] = true;
dojo.provide("dijit.Declaration");
dojo.require("dijit._Widget");
dojo.require("dijit._Templated");

dojo.declare(
	"dijit.Declaration",
	dijit._Widget,
	{
		// summary:
		//		The Declaration widget allows a user to declare new widget
		//		classes directly from a snippet of markup.

		_noScript: true,
		widgetClass: "",
		replaceVars: true,
		defaults: null,
		mixins: [],
		buildRendering: function(){
			var src = this.srcNodeRef.parentNode.removeChild(this.srcNodeRef);
			var preambles = dojo.query("> script[type='dojo/method'][event='preamble']", src).orphan();
			var scripts = dojo.query("> script[type^='dojo/']", src).orphan();
			var srcType = src.nodeName;

			var propList = this.defaults||{};

			this.mixins = this.mixins.length ? 
				dojo.map(this.mixins, dojo.getObject) : 
				[ dijit._Widget, dijit._Templated ];

			if(preambles.length){
				// we only support one preamble. So be it.
				propList.preamble = dojo.parser._functionFromScript(preambles[0]);
			}
			propList.widgetsInTemplate = true;
			propList.templateString = "<"+srcType+" class='"+src.className+"'>"+src.innerHTML.replace(/\%7B/g,"{").replace(/\%7D/g,"}")+"</"+srcType+">";

			// strip things so we don't create stuff under us in the initial setup phase
			dojo.query("[dojoType]", src).forEach(function(node){
				node.removeAttribute("dojoType");
			});
			scripts.forEach(function(s){
				if(!s.getAttribute("event")){
					this.mixins.push(dojo.parser._functionFromScript(s));
				}
			}, this);

			// create the new widget class
			dojo.declare(
				this.widgetClass,
				this.mixins,
				propList
			);

			var wcp = dojo.getObject(this.widgetClass).prototype;
			scripts.forEach(function(s){
				if(s.getAttribute("event")){
					dojo.parser._wireUpMethod(wcp, s);
				}
			});
		}
	}
);

}
