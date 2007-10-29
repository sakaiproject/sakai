if(!dojo._hasResource["dojox.gfx"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["dojox.gfx"] = true;
dojo.provide("dojox.gfx");

dojo.require("dojox.gfx.matrix");
dojo.require("dojox.gfx._base");

(function(){
	var renderers = (typeof djConfig["gfxRenderer"] == "string" ?
		djConfig["gfxRenderer"] : "svg,vml,silverlight").split(",");
	for(var i = 0; i < renderers.length; ++i){
		switch(renderers[i]){
			case "svg":
				if(dojo.isIE == 0){ dojox.gfx.renderer = "svg"; }
				break;
			case "vml":
				if(dojo.isIE != 0){ dojox.gfx.renderer = "vml"; }
				break;
			case "silverlight":
				if(window.Silverlight){ dojox.gfx.renderer = "silverlight"; }
				break;
		}
		if(dojox.gfx.renderer){ break; }
	}
})();

// include a renderer conditionally
dojo.requireIf(dojox.gfx.renderer == "svg", "dojox.gfx.svg");
dojo.requireIf(dojox.gfx.renderer == "vml", "dojox.gfx.vml");
dojo.requireIf(dojox.gfx.renderer == "silverlight", "dojox.gfx.silverlight");

}
