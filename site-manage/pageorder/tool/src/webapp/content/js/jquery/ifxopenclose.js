/**
 * Interface Elements for jQuery
 * FX - open/close/switch
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
 * Applies an open/close animation to element
 */
jQuery.fn.extend(
	{
		/**
		 * @name CloseVertically
		 * @description closes the element vertically
		 * @param Mixed speed animation speed, integer for miliseconds, string ['slow' | 'normal' | 'fast']
		 * @param Function callback (optional) A function to be executed whenever the animation completes.
		 * @param String easing (optional) The name of the easing effect that you want to use.
		 * @type jQuery
		 * @cat Plugins/Interface
		 * @author Stefan Petre
		 */
		CloseVertically : function (speed, callback, easing) {
			return this.queue('interfaceFX', function(){
				new jQuery.fx.OpenClose(this, speed, callback, 'vertically', 'close', easing);
			});
		},
		
		/**
		 * @name CloseHorizontally
		 * @description closes the element horizontally
		 * @param Mixed speed animation speed, integer for miliseconds, string ['slow' | 'normal' | 'fast']
		 * @param Function callback (optional) A function to be executed whenever the animation completes.
		 * @param String easing (optional) The name of the easing effect that you want to use.
		 * @type jQuery
		 * @cat Plugins/Interface
		 * @author Stefan Petre
		 */
		CloseHorizontally : function (speed, callback, easing) {
			return this.queue('interfaceFX', function(){
				new jQuery.fx.OpenClose(this, speed, callback, 'horizontally', 'close', easing);
			});
		},
		
		/**
		 * @name SwitchHorizontally
		 * @description opens/closes the element horizontally
		 * @param Mixed speed animation speed, integer for miliseconds, string ['slow' | 'normal' | 'fast']
		 * @param Function callback (optional) A function to be executed whenever the animation completes.
		 * @param String easing (optional) The name of the easing effect that you want to use.
		 * @type jQuery
		 * @cat Plugins/Interface
		 * @author Stefan Petre
		 */
		SwitchHorizontally : function (speed, callback, easing) 
		{
			return this.queue('interfaceFX', function(){
				if (jQuery.css(this, 'display') == 'none') {
					new jQuery.fx.OpenClose(this, speed, callback, 'horizontally', 'open', easing);
				} else {
					new jQuery.fx.OpenClose(this, speed, callback, 'horizontally', 'close', easing);
				}
			});
		},
		
		/**
		 * @name SwitchVertically
		 * @description opens/closes the element vertically
		 * @param Mixed speed animation speed, integer for miliseconds, string ['slow' | 'normal' | 'fast']
		 * @param Function callback (optional) A function to be executed whenever the animation completes.
		 * @param String easing (optional) The name of the easing effect that you want to use.
		 * @type jQuery
		 * @cat Plugins/Interface
		 * @author Stefan Petre
		 */
		SwitchVertically : function (speed, callback, easing) 
		{
			return this.queue('interfaceFX', function(){
				if (jQuery.css(this, 'display') == 'none') {
					new jQuery.fx.OpenClose(this, speed, callback, 'vertically', 'open', easing);
				} else {
					new jQuery.fx.OpenClose(this, speed, callback, 'vertically', 'close', easing);
				}
			});
		},
		
		/**
		 * @name OpenVertically
		 * @description opens the element vertically
		 * @param Mixed speed animation speed, integer for miliseconds, string ['slow' | 'normal' | 'fast']
		 * @param Function callback (optional) A function to be executed whenever the animation completes.
		 * @param String easing (optional) The name of the easing effect that you want to use.
		 * @type jQuery
		 * @cat Plugins/Interface
		 * @author Stefan Petre
		 */
		OpenVertically : function (speed, callback, easing) {
			return this.queue('interfaceFX', function(){
				new jQuery.fx.OpenClose(this, speed, callback, 'vertically', 'open', easing);
			});
		},
		
		/**
		 * @name OpenHorizontally
		 * @description opens the element horizontally
		 * @param Mixed speed animation speed, integer for miliseconds, string ['slow' | 'normal' | 'fast']
		 * @param Function callback (optional) A function to be executed whenever the animation completes.
		 * @param String easing (optional) The name of the easing effect that you want to use.
		 * @type jQuery
		 * @cat Plugins/Interface
		 * @author Stefan Petre
		 */
		OpenHorizontally : function (speed, callback, easing) {
			return this.queue('interfaceFX', function(){
				new jQuery.fx.OpenClose(this, speed, callback, 'horizontally', 'open', easing);
			});
		}
	}
);

jQuery.fx.OpenClose = function (e, speed, callback, direction, type, easing)
{
	if (!jQuery.fxCheckTag(e)) {
		jQuery.dequeue(e, 'interfaceFX');
		return false;
	}
	var z = this;
	var restoreStyle = false;
	z.el = jQuery(e);
	z.easing = typeof callback == 'string' ? callback : easing||null;
	z.callback = typeof callback == 'function' ? callback : null;
	z.type = type;
	z.speed = speed;
	z.oldP = jQuery.iUtil.getSize(e);
	z.oldStyle = {};
	z.oldStyle.position = z.el.css('position');
	z.oldStyle.display = z.el.css('display');
	if (z.oldStyle.display == 'none') {
		oldVisibility = z.el.css('visibility');
		z.el.show();
		restoreStyle = true;
	}
	z.oldStyle.top = z.el.css('top');
	z.oldStyle.left = z.el.css('left');
	if (restoreStyle) {
		z.el.hide();
		z.el.css('visibility', oldVisibility);
	}
	z.oldStyle.width = z.oldP.w + 'px';
	z.oldStyle.height = z.oldP.h + 'px';
	z.oldStyle.overflow = z.el.css('overflow');
	z.oldP.top = parseInt(z.oldStyle.top)||0;
	z.oldP.left = parseInt(z.oldStyle.left)||0;
	//z.el.show();
	
	if (z.oldStyle.position != 'relative' && z.oldStyle.position != 'absolute') {
		z.el.css('position', 'relative');
	}
	z.el.css('overflow', 'hidden')
		.css('height', type == 'open' && direction == 'vertically' ? 1 : z.oldP.h + 'px')
		.css('width', type == 'open' && direction == 'horizontally' ? 1 : z.oldP.w + 'px');
	
	z.complete = function()
	{
		z.el.css(z.oldStyle);
		if (z.type == 'close')
			z.el.hide();
		else 
			z.el.show();
		jQuery.dequeue(z.el.get(0), 'interfaceFX');
	};
	
	switch (direction) {
		case 'vertically':
			z.eh = new jQuery.fx(
				z.el.get(0),
				jQuery.speed(speed-15, z.easing, callback),
				'height'
			);
			z.et = new jQuery.fx(
				z.el.get(0),
				jQuery.speed(
					z.speed,
					z.easing,
					z.complete
				),
				'top'
			);
			if (z.type == 'close') {
				z.eh.custom(z.oldP.h,0);
				z.et.custom(z.oldP.top, z.oldP.top + z.oldP.h/2);
			} else {
				z.eh.custom(0, z.oldP.h);
				z.et.custom(z.oldP.top + z.oldP.h/2, z.oldP.top);
			}
		break;
		case 'horizontally':
			z.eh = new jQuery.fx(
				z.el.get(0),
				jQuery.speed(speed-15, z.easing, callback),
				'width'
			);
			z.et = new jQuery.fx(
				z.el.get(0),
				jQuery.speed(
					z.speed,
					z.easing,
					z.complete
				),
				'left'
			);
			if (z.type == 'close') {
				z.eh.custom(z.oldP.w,0);
				z.et.custom(z.oldP.left, z.oldP.left + z.oldP.w/2);
			} else {
				z.eh.custom(0, z.oldP.w);
				z.et.custom(z.oldP.left + z.oldP.w/2, z.oldP.left);
			}
		break;
	}
};