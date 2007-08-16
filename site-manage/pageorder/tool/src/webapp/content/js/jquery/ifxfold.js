/**
 * Interface Elements for jQuery
 * FX - fold
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
 * Applies a folding animation to element
 */
jQuery.fn.extend(
	{
		/**
		 * @name Fold
		 * @description folds the element
		 * @param Mixed speed animation speed, integer for miliseconds, string ['slow' | 'normal' | 'fast']
		 * @param Integer height the height in pixels to fold element to
		 * @param Function callback (optional) A function to be executed whenever the animation completes.
		 * @param String easing (optional) The name of the easing effect that you want to use.
		 * @type jQuery
		 * @cat Plugins/Interface
		 * @author Stefan Petre
		 */
		Fold : function (speed, height, callback, easing)
		{
			return this.queue('interfaceFX',function(){
				new jQuery.fx.DoFold(this, speed, height, callback, 'fold', easing);
			});
		},
		
		/**
		 * @name UnFold
		 * @description unfolds the element
		 * @param Mixed speed animation speed, integer for miliseconds, string ['slow' | 'normal' | 'fast']
		 * @param Integer height the height in pixels to unfold element to
		 * @param Function callback (optional) A function to be executed whenever the animation completes.
		 * @param String easing (optional) The name of the easing effect that you want to use.
		 * @type jQuery
		 * @cat Plugins/Interface
		 * @author Stefan Petre
		 */
		UnFold : function (speed, height, callback, easing)
		{
			return this.queue('interfaceFX',function(){
				new jQuery.fx.DoFold(this, speed, height, callback, 'unfold', easing);
			});
		},
		
		/**
		 * @name FoldToggle
		 * @description folds/unfolds the element
		 * @param Mixed speed animation speed, integer for miliseconds, string ['slow' | 'normal' | 'fast']
		 * @param Integer height the height in pixels to folds/unfolds element to
		 * @param Function callback (optional) A function to be executed whenever the animation completes.
		 * @param String easing (optional) The name of the easing effect that you want to use.
		 * @type jQuery
		 * @cat Plugins/Interface
		 * @author Stefan Petre
		 */
		FoldToggle : function (speed, height, callback, easing)
		{
			return this.queue('interfaceFX',function(){
				new jQuery.fx.DoFold(this, speed, height, callback, 'toggle', easing);
			});
		}
	}
);

jQuery.fx.DoFold = function (e, speed, height, callback, type, easing)
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
		type = z.el.css('display') == 'none' ? 'unfold' : 'fold';
	}
	//z.el.show();
	z.speed = speed;
	z.height = height && height.constructor == Number ? height : 20;
	z.fx = jQuery.fx.buildWrapper(e);
	z.type = type;
	z.complete = function()
	{
		if (z.callback && z.callback.constructor == Function) {
			z.callback.apply(z.el.get(0));
		}
		if(z.type == 'unfold'){
			z.el.show();
		} else {
			z.el.hide();
		}
		jQuery.fx.destroyWrapper(z.fx.wrapper.get(0), z.fx.oldStyle);
		jQuery.dequeue(z.el.get(0), 'interfaceFX');
	};
	if ( z.type == 'unfold') {
		z.el.show();
		z.fx.wrapper.css('height', z.height + 'px').css('width', '1px');
		
		z.ef = new jQuery.fx(
				z.fx.wrapper.get(0),
				jQuery.speed (
					z.speed,
					z.easing,
					function()
					{
						z.ef = new jQuery.fx(
							z.fx.wrapper.get(0),
							jQuery.speed(
								z.speed,
								z.easing, 
								z.complete
							),
							'height'
						);
						z.ef.custom(z.height, z.fx.oldStyle.sizes.hb);
					}
				), 
				'width'
			);
		z.ef.custom(0, z.fx.oldStyle.sizes.wb);
	} else {
		z.ef = new jQuery.fx(
				z.fx.wrapper.get(0),
				jQuery.speed(
					z.speed,
					z.easing,
					function()
					{
						z.ef = new jQuery.fx(
							z.fx.wrapper.get(0),
							jQuery.speed(
								z.speed,
								z.easing,
								z.complete
							),
							'width'
						);
						z.ef.custom(z.fx.oldStyle.sizes.wb, 0);
					}
				), 
				'height'
			);
		z.ef.custom(z.fx.oldStyle.sizes.hb, z.height);
	}
};

