/**
 * Interface Elements for jQuery
 * FX - slide
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
 * Slides the element
 */
jQuery.fn.extend(
	{
		/**
		 * @name SlideInUp
		 * @description slides the element in up
		 * @param Mixed speed animation speed, integer for miliseconds, string ['slow' | 'normal' | 'fast']
		 * @param Function callback (optional) A function to be executed whenever the animation completes.
		 * @param String easing (optional) The name of the easing effect that you want to use.
		 * @type jQuery
		 * @cat Plugins/Interface
		 * @author Stefan Petre
		 */
		SlideInUp : function (speed,callback, easing)
		{
			return this.queue('interfaceFX', function(){
				new jQuery.fx.slide(this, speed, callback, 'up', 'in', easing);
			});
		},
		
		/**
		 * @name SlideOutUp
		 * @description slides the element out up
		 * @param Mixed speed animation speed, integer for miliseconds, string ['slow' | 'normal' | 'fast']
		 * @param Function callback (optional) A function to be executed whenever the animation completes.
		 * @param String easing (optional) The name of the easing effect that you want to use.
		 * @type jQuery
		 * @cat Plugins/Interface
		 * @author Stefan Petre
		 */
		SlideOutUp : function (speed,callback, easing)
		{
			return this.queue('interfaceFX', function(){
				new jQuery.fx.slide(this, speed, callback, 'up', 'out', easing);
			});
		},
		
		/**
		 * @name SlideToggleUp
		 * @description slides the element in/out up
		 * @param Mixed speed animation speed, integer for miliseconds, string ['slow' | 'normal' | 'fast']
		 * @param Function callback (optional) A function to be executed whenever the animation completes.
		 * @param String easing (optional) The name of the easing effect that you want to use.
		 * @type jQuery
		 * @cat Plugins/Interface
		 * @author Stefan Petre
		 */
		SlideToggleUp : function (speed,callback, easing)
		{
			return this.queue('interfaceFX', function(){
				new jQuery.fx.slide(this, speed, callback, 'up', 'toggle', easing);
			});
		},
		
		/**
		 * @name SlideInDown
		 * @description slides the element in down
		 * @param Mixed speed animation speed, integer for miliseconds, string ['slow' | 'normal' | 'fast']
		 * @param Function callback (optional) A function to be executed whenever the animation completes.
		 * @param String easing (optional) The name of the easing effect that you want to use.
		 * @type jQuery
		 * @cat Plugins/Interface
		 * @author Stefan Petre
		 */
		SlideInDown : function (speed,callback, easing)
		{
			return this.queue('interfaceFX', function(){
				new jQuery.fx.slide(this, speed, callback, 'down', 'in', easing);
			});
		},
		
		/**
		 * @name SlideOutDown
		 * @description slides the element out down
		 * @param Mixed speed animation speed, integer for miliseconds, string ['slow' | 'normal' | 'fast']
		 * @param Function callback (optional) A function to be executed whenever the animation completes.
		 * @param String easing (optional) The name of the easing effect that you want to use.
		 * @type jQuery
		 * @cat Plugins/Interface
		 * @author Stefan Petre
		 */
		SlideOutDown : function (speed,callback, easing)
		{
			return this.queue('interfaceFX', function(){
				new jQuery.fx.slide(this, speed, callback, 'down', 'out', easing);
			});
		},
		
		/**
		 * @name SlideToggleDown
		 * @description slides the element in/out down
		 * @param Mixed speed animation speed, integer for miliseconds, string ['slow' | 'normal' | 'fast']
		 * @param Function callback (optional) A function to be executed whenever the animation completes.
		 * @param String easing (optional) The name of the easing effect that you want to use.
		 * @type jQuery
		 * @cat Plugins/Interface
		 * @author Stefan Petre
		 */
		SlideToggleDown : function (speed,callback, easing)
		{
			return this.queue('interfaceFX', function(){
				new jQuery.fx.slide(this, speed, callback, 'down', 'toggle', easing);
			});
		},
		
		/**
		 * @name SlideInLeft
		 * @description slides the element in left
		 * @param Mixed speed animation speed, integer for miliseconds, string ['slow' | 'normal' | 'fast']
		 * @param Function callback (optional) A function to be executed whenever the animation completes.
		 * @param String easing (optional) The name of the easing effect that you want to use.
		 * @type jQuery
		 * @cat Plugins/Interface
		 * @author Stefan Petre
		 */
		SlideInLeft : function (speed,callback, easing)
		{
			return this.queue('interfaceFX', function(){
				new jQuery.fx.slide(this, speed, callback, 'left', 'in', easing);
			});
		},
		
		/**
		 * @name SlideOutLeft
		 * @description slides the element out left
		 * @param Mixed speed animation speed, integer for miliseconds, string ['slow' | 'normal' | 'fast']
		 * @param Function callback (optional) A function to be executed whenever the animation completes.
		 * @param String easing (optional) The name of the easing effect that you want to use.
		 * @type jQuery
		 * @cat Plugins/Interface
		 * @author Stefan Petre
		 */
		SlideOutLeft :  function (speed,callback, easing)
		{
			return this.queue('interfaceFX', function(){
				new jQuery.fx.slide(this, speed, callback, 'left', 'out', easing);
			});
		},
		
		/**
		 * @name SlideToggleLeft
		 * @description slides the element in/out left
		 * @param Mixed speed animation speed, integer for miliseconds, string ['slow' | 'normal' | 'fast']
		 * @param Function callback (optional) A function to be executed whenever the animation completes.
		 * @param String easing (optional) The name of the easing effect that you want to use.
		 * @type jQuery
		 * @cat Plugins/Interface
		 * @author Stefan Petre
		 */
		SlideToggleLeft : function (speed,callback, easing)
		{
			return this.queue('interfaceFX', function(){
				new jQuery.fx.slide(this, speed, callback, 'left', 'toggle', easing);
			});
		},
		
		/**
		 * @name SlideInRight
		 * @description slides the element in right
		 * @param Mixed speed animation speed, integer for miliseconds, string ['slow' | 'normal' | 'fast']
		 * @param Function callback (optional) A function to be executed whenever the animation completes.
		 * @param String easing (optional) The name of the easing effect that you want to use.
		 * @type jQuery
		 * @cat Plugins/Interface
		 * @author Stefan Petre
		 */
		SlideInRight : function (speed,callback, easing)
		{
			return this.queue('interfaceFX', function(){
				new jQuery.fx.slide(this, speed, callback, 'right', 'in', easing);
			});
		},
		
		/**
		 * @name SlideOutRight
		 * @description slides the element out right
		 * @param Mixed speed animation speed, integer for miliseconds, string ['slow' | 'normal' | 'fast']
		 * @param Function callback (optional) A function to be executed whenever the animation completes.
		 * @param String easing (optional) The name of the easing effect that you want to use.
		 * @type jQuery
		 * @cat Plugins/Interface
		 * @author Stefan Petre
		 */
		SlideOutRight : function (speed,callback, easing)
		{
			return this.queue('interfaceFX', function(){
				new jQuery.fx.slide(this, speed, callback, 'right', 'out', easing);
			});
		},
		
		/**
		 * @name SlideToggleRight
		 * @description slides the element in/out right
		 * @param Mixed speed animation speed, integer for miliseconds, string ['slow' | 'normal' | 'fast']
		 * @param Function callback (optional) A function to be executed whenever the animation completes.
		 * @param String easing (optional) The name of the easing effect that you want to use.
		 * @type jQuery
		 * @cat Plugins/Interface
		 * @author Stefan Petre
		 */
		SlideToggleRight : function (speed,callback, easing)
		{
			return this.queue('interfaceFX', function(){
				new jQuery.fx.slide(this, speed, callback, 'right', 'toggle', easing);
			});
		}
	}
);

jQuery.fx.slide = function(e, speed, callback, direction, type, easing)
{
	if (!jQuery.fxCheckTag(e)) {
		jQuery.dequeue(e, 'interfaceFX');
		return false;
	}
	var z = this;
	z.el = jQuery(e);
	z.easing = typeof callback == 'string' ? callback : easing||null;
	z.callback = typeof callback == 'function' ? callback : null;
	if ( type == 'toggle') {
		type = z.el.css('display') == 'none' ? 'in' : 'out';
	}
	if (!e.ifxFirstDisplay)
		e.ifxFirstDisplay = z.el.css('display');
	z.el.show();
	
	z.speed = speed;
	z.fx = jQuery.fx.buildWrapper(e);
	
	z.type = type;
	z.direction = direction;
	z.complete = function()
	{
		if(z.type == 'out')
			z.el.css('visibility', 'hidden');
		jQuery.fx.destroyWrapper(z.fx.wrapper.get(0), z.fx.oldStyle);
		if(z.type == 'in'){
			z.el.css('display', z.el.get(0).ifxFirstDisplay == 'none' ? 'block' : z.el.get(0).ifxFirstDisplay);
		} else {
			z.el.css('display', 'none');
			z.el.css('visibility', 'visible');
		}
		if (z.callback && z.callback.constructor == Function) {
			z.callback.apply(z.el.get(0));
		}
		jQuery.dequeue(z.el.get(0), 'interfaceFX');
	};
	switch (z.direction) {
		case 'up':
			z.ef = new jQuery.fx(
				z.el.get(0), 
				jQuery.speed(
					z.speed,
					z.easing,
					z.complete
				),
				'top'
			);
			z.efx = new jQuery.fx(
				z.fx.wrapper.get(0), 
				jQuery.speed(
					z.speed,
					z.easing
				),
				'height'
			);
			if (z.type == 'in') {
				z.ef.custom (-z.fx.oldStyle.sizes.hb, 0);
				z.efx.custom(0, z.fx.oldStyle.sizes.hb);
			} else {
				z.ef.custom (0, -z.fx.oldStyle.sizes.hb);
				z.efx.custom (z.fx.oldStyle.sizes.hb, 0);
			}
		break;
		case 'down':
			z.ef = new jQuery.fx(
				z.el.get(0), 
				jQuery.speed(
					z.speed,
					z.easing,
					z.complete
				),
				'top'
			);
			if (z.type == 'in') {
				z.ef.custom (z.fx.oldStyle.sizes.hb, 0);
			} else {
				z.ef.custom (0, z.fx.oldStyle.sizes.hb);
			}
		break;
		case 'left':
			z.ef = new jQuery.fx(
				z.el.get(0), 
				jQuery.speed(
					z.speed,
					z.easing,
					z.complete
				),
				'left'
			);
			z.efx = new jQuery.fx(
				z.fx.wrapper.get(0), 
				jQuery.speed(
					z.speed,
					z.easing
				),
				'width'
			);
			if (z.type == 'in') {
				z.ef.custom (-z.fx.oldStyle.sizes.wb, 0);
				z.efx.custom (0, z.fx.oldStyle.sizes.wb);
			} else {
				z.ef.custom (0, -z.fx.oldStyle.sizes.wb);
				z.efx.custom (z.fx.oldStyle.sizes.wb, 0);
			}
		break;
		case 'right':
			z.ef = new jQuery.fx(
				z.el.get(0), 
				jQuery.speed(
					z.speed,
					z.easing,
					z.complete
				),
				'left'
			);
			if (z.type == 'in') {
				z.ef.custom (z.fx.oldStyle.sizes.wb, 0);
			} else {
				z.ef.custom (0, z.fx.oldStyle.sizes.wb);
			}
		break;
	}
};
