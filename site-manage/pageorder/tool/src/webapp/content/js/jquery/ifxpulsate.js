/**
 * Interface Elements for jQuery
 * FX - pulsate
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
 * @name Bounce
 * @description makes the element to pulsate
 * @param Mixed speed animation speed, integer for miliseconds, string ['slow' | 'normal' | 'fast']
 * @param Integer times how many times to pulsate
 * @param Function callback (optional) A function to be executed whenever the animation completes.
 * @type jQuery
 * @cat Plugins/Interface
 * @author Stefan Petre
 */
jQuery.fn.Pulsate = function(speed, times, callback) {
	return this.queue('interfaceFX',function(){
		if (!jQuery.fxCheckTag(this)) {
			jQuery.dequeue(this, 'interfaceFX');
			return false;
		}
		var fx = new jQuery.fx.Pulsate(this, speed, times, callback);
		fx.pulse();
	});
};

jQuery.fx.Pulsate = function (el, speed, times, callback)
{	
	var z = this;
	z.times = times;
	z.cnt = 1;
	z.el = el;
	z.speed = speed;
	z.callback = callback;
	jQuery(z.el).show();
	z.pulse = function()
	{
		z.cnt ++;
		z.e = new jQuery.fx(
			z.el, 
			jQuery.speed(
				z.speed, 
				function(){
					z.ef = new jQuery.fx(
						z.el, 
						jQuery.speed(
							z.speed,
							function()
							{
								if (z.cnt <= z.times)
									z.pulse();
								else {
									jQuery.dequeue(z.el, 'interfaceFX');
									if (z.callback && z.callback.constructor == Function) {
										z.callback.apply(z.el);
									}
								}
							}
						), 
						'opacity'
					);
					z.ef.custom(0,1);
				}
			), 
			'opacity'
		);
		z.e.custom(1,0);
	};
};
