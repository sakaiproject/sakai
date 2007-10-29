if(!dojo._hasResource["dojox.fx.easing"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["dojox.fx.easing"] = true;
dojo.provide("dojox.fx.easing");
/*
	dojox.fx.easing is in this little file so you don't need dojox.fx to utilize this.
	dojox.fx has a lot of fun animations, but this module is optimized for size ... 

*/
dojox.fx.easing = {
	// summary: Collection of easing functions to use beyond the default dojo._defaultEasing
	// 
	// description:
	//	Easing functions are used to manipulate the iteration through
	//	an _Animation's _Line. _Line being the properties of an Animation,
	//	and the easing function progresses through that Line determing
	//	how quickly (or slowly) it should go. 
	//	
	//	example:
	//		dojo.require("dojox.fx.easing");
	//		var anim = dojo.fadeOut({
	//			node: 'node',	
	//			duration: 2000,
	//			easing: dojox.fx.easing.easeIn
	//		}).play();
	//
	easeIn: function(/* Decimal? */n){
		// summary: an easing function that speeds an _Animation up closer to end
		return Math.pow(n, 3);
	},

	easeOut: function(/* Decimal? */n){ 
		// summary: an easing function that slows an _Animation down towards end
		return (1 - Math.pow(1-n,3));
	},

	easeInOut: function(/* Decimal? */n){
		// summary: an easing function that "humps" in the middle of an _Animation?
		return ((3 * Math.pow(n, 2)) - (2 * Math.pow(n, 3)))
	}
};

}
