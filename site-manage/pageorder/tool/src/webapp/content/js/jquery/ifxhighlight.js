/**
 * Interface Elements for jQuery
 * FX - Highlight
 * 
 * http://interface.eyecon.ro
 * 
 * Copyright (c) 2006 Stefan Petre
 * Dual licensed under the MIT (MIT-LICENSE.txt) 
 * and GPL (GPL-LICENSE.txt) licenses.
 *   
 *
 */


/**
 * @name Highlight
 * @description Animates the backgroudn color to create a highlight animation
 * @param Mixed speed animation speed, integer for miliseconds, string ['slow' | 'normal' | 'fast']
 * @param String color color to highlight from
 * @param Function callback (optional) A function to be executed whenever the animation completes.
 * @param String easing (optional) The name of the easing effect that you want to use.
 * @type jQuery
 * @cat Plugins/Interface
 * @author Stefan Petre
 */
jQuery.fn.Highlight = function(speed, color, callback, easing) {
	return this.queue(
		'interfaceColorFX',
		function()
		{
			this.oldStyleAttr = jQuery(this).attr("style") || '';
			easing = typeof callback == 'string' ? callback : easing||null;
			callback = typeof callback == 'function' ? callback : null;
			var oldColor = jQuery(this).css('backgroundColor');
			var parentEl = this.parentNode;
			while(oldColor == 'transparent' && parentEl) {
				oldColor = jQuery(parentEl).css('backgroundColor');
				parentEl = parentEl.parentNode;
			}
			jQuery(this).css('backgroundColor', color);
			
			
			/* In IE, style is a object.. */
			if(typeof this.oldStyleAttr == 'object') this.oldStyleAttr = this.oldStyleAttr["cssText"];
			
			jQuery(this).animate(
				{'backgroundColor':oldColor},
				speed,
				easing,
				function() {
					jQuery.dequeue(this, 'interfaceColorFX');
					if(typeof jQuery(this).attr("style") == 'object') {
						jQuery(this).attr("style")["cssText"] = "";
						jQuery(this).attr("style")["cssText"] = this.oldStyleAttr;
					} else {
						jQuery(this).attr("style", this.oldStyleAttr);	
					}
					if (callback)
						callback.apply(this);
				}
		  	);
		}
	);
};