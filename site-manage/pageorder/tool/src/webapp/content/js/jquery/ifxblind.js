/**
 * Interface Elements for jQuery
 * FX - blind
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
 * Applies a blinding animation to element
 */
jQuery.fn.extend(
	{
		/**
		 * @name BlindUp
		 * @description blinds the element up
		 * @param Mixed speed animation speed, integer for miliseconds, string ['slow' | 'normal' | 'fast']
		 * @param Function callback (optional) A function to be executed whenever the animation completes.
		 * @param String easing (optional) The name of the easing effect that you want to use.
		 * @type jQuery
		 * @cat Plugins/Interface
		 * @author Stefan Petre
		 */
		BlindUp : function (speed, callback, easing)
		{
			return this.queue('interfaceFX',function(){
				new jQuery.fx.BlindDirection(this, speed, callback, 'up', easing);
			});
		},
		
		/**
		 * @name BlindDown
		 * @description blinds the element down
		 * @param Mixed speed animation speed, integer for miliseconds, string ['slow' | 'normal' | 'fast']
		 * @param Function callback (optional) A function to be executed whenever the animation completes.
		 * @param String easing (optional) The name of the easing effect that you want to use.
		 * @type jQuery
		 * @cat Plugins/Interface
		 * @author Stefan Petre
		 */
		BlindDown : function (speed, callback, easing)
		{
			return this.queue('interfaceFX',function(){
				new jQuery.fx.BlindDirection(this, speed, callback, 'down', easing);
			});
		},
		
		/**
		 * @name BlindToggleVertically
		 * @description blinds the element up or down
		 * @param Mixed speed animation speed, integer for miliseconds, string ['slow' | 'normal' | 'fast']
		 * @param Function callback (optional) A function to be executed whenever the animation completes.
		 * @param String easing (optional) The name of the easing effect that you want to use.
		 * @type jQuery
		 * @cat Plugins/Interface
		 * @author Stefan Petre
		 */
		BlindToggleVertically : function (speed, callback, easing)
		{
			return this.queue('interfaceFX',function(){
				new jQuery.fx.BlindDirection(this, speed, callback, 'togglever', easing);
			});
		},
		
		/**
		 * @name BlindLeft
		 * @description blinds the element left
		 * @param Mixed speed animation speed, integer for miliseconds, string ['slow' | 'normal' | 'fast']
		 * @param Function callback (optional) A function to be executed whenever the animation completes.
		 * @param String easing (optional) The name of the easing effect that you want to use.
		 * @type jQuery
		 * @cat Plugins/Interface
		 * @author Stefan Petre
		 */
		BlindLeft : function (speed, callback, easing)
		{
			return this.queue('interfaceFX',function(){
				new jQuery.fx.BlindDirection(this, speed, callback, 'left', easing);
			});
		},
		
		/**
		 * @name BlindRight
		 * @description blinds the element right
		 * @param Mixed speed animation speed, integer for miliseconds, string ['slow' | 'normal' | 'fast']
		 * @param Function callback (optional) A function to be executed whenever the animation completes.
		 * @param String easing (optional) The name of the easing effect that you want to use.
		 * @type jQuery
		 * @cat Plugins/Interface
		 * @author Stefan Petre
		 */
		BlindRight : function (speed, callback, easing)
		{
			return this.queue('interfaceFX',function(){
				new jQuery.fx.BlindDirection(this, speed, callback, 'right', easing);
			});
		},
		
		/**
		 * @name BlindToggleHorizontally
		 * @description blinds the element left and right
		 * @param Mixed speed animation speed, integer for miliseconds, string ['slow' | 'normal' | 'fast']
		 * @param Function callback (optional) A function to be executed whenever the animation completes.
		 * @param String easing (optional) The name of the easing effect that you want to use.
		 * @type jQuery
		 * @cat Plugins/Interface
		 * @author Stefan Petre
		 */
		BlindToggleHorizontally : function (speed, callback, easing)
		{
			return this.queue('interfaceFX',function(){
				new jQuery.fx.BlindDirection(this, speed, callback, 'togglehor', easing);
			});
		}
	}
);

jQuery.fx.BlindDirection = function (e, speed, callback, direction, easing)
{
	if (!jQuery.fxCheckTag(e)) {
		jQuery.dequeue(e, 'interfaceFX');
		return false;
	}
	var z = this;
	z.el = jQuery(e);
	z.size = jQuery.iUtil.getSize(e);
	z.easing = typeof callback == 'string' ? callback : easing||null;
	if (!e.ifxFirstDisplay)
		e.ifxFirstDisplay = z.el.css('display');
	if ( direction == 'togglever') {
		direction = z.el.css('display') == 'none' ? 'down' : 'up';
	} else if (direction == 'togglehor') {
		direction = z.el.css('display') == 'none' ? 'right' : 'left';
	}
	z.el.show();
	z.speed = speed;
	z.callback = typeof callback == 'function' ? callback : null;
	z.fx = jQuery.fx.buildWrapper(e);
	z.direction = direction;
	z.complete = function()
	{
		if (z.callback && z.callback.constructor == Function) {
			z.callback.apply(z.el.get(0));
		}
		if(z.direction == 'down' || z.direction == 'right'){
			z.el.css('display', z.el.get(0).ifxFirstDisplay == 'none' ? 'block' : z.el.get(0).ifxFirstDisplay);
		} else {
			z.el.hide();
		}
		jQuery.fx.destroyWrapper(z.fx.wrapper.get(0), z.fx.oldStyle);
		jQuery.dequeue(z.el.get(0), 'interfaceFX');
	};
	switch (z.direction) {
		case 'up':
			fxh = new jQuery.fx(
				z.fx.wrapper.get(0),
				jQuery.speed(
					z.speed,
					z.easing,
					z.complete
				),
				'height'
			);
			fxh.custom(z.fx.oldStyle.sizes.hb, 0);
		break;
		case 'down':
			z.fx.wrapper.css('height', '1px');
			z.el.show();
			fxh = new jQuery.fx(
				z.fx.wrapper.get(0),
				jQuery.speed(
					z.speed,
					z.easing,
					z.complete
				),
				'height'
			);
			fxh.custom(0, z.fx.oldStyle.sizes.hb);
		break;
		case 'left':
			fxh = new jQuery.fx(
				z.fx.wrapper.get(0),
				jQuery.speed(
					z.speed,
					z.easing,
					z.complete
				),
				'width'
			);
			fxh.custom(z.fx.oldStyle.sizes.wb, 0);
		break;
		case 'right':
			z.fx.wrapper.css('width', '1px');
			z.el.show();
			fxh = new jQuery.fx(
				z.fx.wrapper.get(0),
				jQuery.speed(
					z.speed,
					z.easing,
					z.complete
				),
				'width'
			);
			fxh.custom(0, z.fx.oldStyle.sizes.wb);
		break;
	}
};