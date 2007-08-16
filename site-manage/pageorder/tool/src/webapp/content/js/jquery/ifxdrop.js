/**
 * Interface Elements for jQuery
 * FX - drop
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
 * Applies a dropping effect to element
 */
jQuery.fn.extend(
	{
		/**
		 * @name DropOutDown
		 * @description drops the element out down
		 * @param Mixed speed animation speed, integer for miliseconds, string ['slow' | 'normal' | 'fast']
		 * @param Function callback (optional) A function to be executed whenever the animation completes.
		 * @param String easing (optional) The name of the easing effect that you want to use.
		 * @type jQuery
		 * @cat Plugins/Interface
		 * @author Stefan Petre
		 */
		DropOutDown : function (speed, callback, easing) {
			return this.queue('interfaceFX',function(){
				new jQuery.fx.DropOutDirectiont(this, speed, callback, 'down', 'out', easing);
			});
		},
		
		/**
		 * @name DropInDown
		 * @description drops the element in down
		 * @param Mixed speed animation speed, integer for miliseconds, string ['slow' | 'normal' | 'fast']
		 * @param Function callback (optional) A function to be executed whenever the animation completes.
		 * @param String easing (optional) The name of the easing effect that you want to use.
		 * @type jQuery
		 * @cat Plugins/Interface
		 * @author Stefan Petre
		 */
		DropInDown : function (speed, callback, easing) {
			return this.queue('interfaceFX',function(){
				new jQuery.fx.DropOutDirectiont(this,  speed, callback, 'down', 'in', easing);
			});
		},
		
		/**
		 * @name DropToggleDown
		 * @description drops the element in/out down
		 * @param Mixed speed animation speed, integer for miliseconds, string ['slow' | 'normal' | 'fast']
		 * @param Function callback (optional) A function to be executed whenever the animation completes.
		 * @param String easing (optional) The name of the easing effect that you want to use.
		 * @type jQuery
		 * @cat Plugins/Interface
		 * @author Stefan Petre
		 */
		DropToggleDown : function (speed, callback, easing) {
			return this.queue('interfaceFX',function(){
				new jQuery.fx.DropOutDirectiont(this,  speed, callback, 'down', 'toggle', easing);
			});
		},
		
		/**
		 * @name DropOutUp
		 * @description drops the element out up
		 * @param Mixed speed animation speed, integer for miliseconds, string ['slow' | 'normal' | 'fast']
		 * @param Function callback (optional) A function to be executed whenever the animation completes.
		 * @param String easing (optional) The name of the easing effect that you want to use.
		 * @type jQuery
		 * @cat Plugins/Interface
		 * @author Stefan Petre
		 */
		DropOutUp : function (speed, callback, easing) {
			return this.queue('interfaceFX',function(){
				new jQuery.fx.DropOutDirectiont(this, speed, callback, 'up', 'out', easing);
			});
		},
		
		/**
		 * @name DropInUp
		 * @description drops the element in up
		 * @param Mixed speed animation speed, integer for miliseconds, string ['slow' | 'normal' | 'fast']
		 * @param Function callback (optional) A function to be executed whenever the animation completes.
		 * @param String easing (optional) The name of the easing effect that you want to use.
		 * @type jQuery
		 * @cat Plugins/Interface
		 * @author Stefan Petre
		 */
		DropInUp : function (speed, callback, easing) {
			return this.queue('interfaceFX',function(){
				new jQuery.fx.DropOutDirectiont(this,  speed, callback, 'up', 'in', easing);
			});
		},
		
		/**
		 * @name DropToggleUp
		 * @description drops the element in/out up
		 * @param Mixed speed animation speed, integer for miliseconds, string ['slow' | 'normal' | 'fast']
		 * @param Function callback (optional) A function to be executed whenever the animation completes.
		 * @param String easing (optional) The name of the easing effect that you want to use.
		 * @type jQuery
		 * @cat Plugins/Interface
		 * @author Stefan Petre
		 */
		DropToggleUp : function (speed, callback, easing) {
			return this.queue('interfaceFX',function(){
				new jQuery.fx.DropOutDirectiont(this,  speed, callback, 'up', 'toggle', easing);
			});
		},
		
		/**
		 * @name DropOutLeft
		 * @description drops the element out left
		 * @param Mixed speed animation speed, integer for miliseconds, string ['slow' | 'normal' | 'fast']
		 * @param Function callback (optional) A function to be executed whenever the animation completes.
		 * @param String easing (optional) The name of the easing effect that you want to use.
		 * @type jQuery
		 * @cat Plugins/Interface
		 * @author Stefan Petre
		 */
		DropOutLeft : function (speed, callback, easing) {
			return this.queue('interfaceFX',function(){
				new jQuery.fx.DropOutDirectiont(this, speed, callback, 'left', 'out', easing);
			});
		},
		
		/**
		 * @name DropInLeft
		 * @description drops the element in left
		 * @param Mixed speed animation speed, integer for miliseconds, string ['slow' | 'normal' | 'fast']
		 * @param Function callback (optional) A function to be executed whenever the animation completes.
		 * @param String easing (optional) The name of the easing effect that you want to use.
		 * @type jQuery
		 * @cat Plugins/Interface
		 * @author Stefan Petre
		 */
		DropInLeft : function (speed, callback, easing) {
			return this.queue('interfaceFX',function(){
				new jQuery.fx.DropOutDirectiont(this,  speed, callback, 'left', 'in', easing);
			});
		},
		
		/**
		 * @name DropToggleLeft
		 * @description drops the element in/out left
		 * @param Mixed speed animation speed, integer for miliseconds, string ['slow' | 'normal' | 'fast']
		 * @param Function callback (optional) A function to be executed whenever the animation completes.
		 * @param String easing (optional) The name of the easing effect that you want to use.
		 * @type jQuery
		 * @cat Plugins/Interface
		 * @author Stefan Petre
		 */
		DropToggleLeft : function (speed, callback, easing) {
			return this.queue('interfaceFX',function(){
				new jQuery.fx.DropOutDirectiont(this,  speed, callback, 'left', 'toggle', easing);
			});
		},
		
		/**
		 * @name DropOutRight
		 * @description drops the element out right
		 * @param Mixed speed animation speed, integer for miliseconds, string ['slow' | 'normal' | 'fast']
		 * @param Function callback (optional) A function to be executed whenever the animation completes.
		 * @param String easing (optional) The name of the easing effect that you want to use.
		 * @type jQuery
		 * @cat Plugins/Interface
		 * @author Stefan Petre
		 */
		DropOutRight : function (speed, callback, easing) {
			return this.queue('interfaceFX',function(){
				new jQuery.fx.DropOutDirectiont(this, speed, callback, 'right', 'out', easing);
			});
		},
		
		/**
		 * @name DropInRight
		 * @description drops the element in right
		 * @param Mixed speed animation speed, integer for miliseconds, string ['slow' | 'normal' | 'fast']
		 * @param Function callback (optional) A function to be executed whenever the animation completes.
		 * @param String easing (optional) The name of the easing effect that you want to use.
		 * @type jQuery
		 * @cat Plugins/Interface
		 * @author Stefan Petre
		 */
		DropInRight : function (speed, callback, easing) {
			return this.queue('interfaceFX',function(){
				new jQuery.fx.DropOutDirectiont(this,  speed, callback, 'right', 'in', easing);
			});
		},
		
		/**
		 * @name DropToggleRight
		 * @description drops the element in/out right
		 * @param Mixed speed animation speed, integer for miliseconds, string ['slow' | 'normal' | 'fast']
		 * @param Function callback (optional) A function to be executed whenever the animation completes.
		 * @param String easing (optional) The name of the easing effect that you want to use.
		 * @type jQuery
		 * @cat Plugins/Interface
		 * @author Stefan Petre
		 */
		DropToggleRight : function (speed, callback, easing) {
			return this.queue('interfaceFX',function(){
				new jQuery.fx.DropOutDirectiont(this,  speed, callback, 'right', 'toggle', easing);
			});
		}
	}
);

jQuery.fx.DropOutDirectiont = function (e, speed, callback, direction, type, easing)
{
	if (!jQuery.fxCheckTag(e)) {
		jQuery.dequeue(e, 'interfaceFX');
		return false;
	}
	var z = this;
	z.el = jQuery(e);
	z.easing = typeof callback == 'string' ? callback : easing||null;
	z.oldStyle = {};
	z.oldStyle.position = z.el.css('position');
	z.oldStyle.top = z.el.css('top');
	z.oldStyle.left = z.el.css('left');
	if (!e.ifxFirstDisplay)
		e.ifxFirstDisplay = z.el.css('display');
	if ( type == 'toggle') {
		type = z.el.css('display') == 'none' ? 'in' : 'out';
	}
	z.el.show();
	
	if (z.oldStyle.position != 'relative' && z.oldStyle.position != 'absolute') {
		z.el.css('position', 'relative');
	}
	z.type = type;
	callback = typeof callback == 'function' ? callback : null;
	/*sizes = ['em','px','pt','%'];
	for(i in sizes) {
		if (z.oldStyle.top.indexOf(sizes[i])>0) {
			z.topUnit = sizes[1];
			z.topSize = parseFloat(z.oldStyle.top)||0;
		}
		if (z.oldStyle.left.indexOf(sizes[i])>0) {
			z.leftUnit = sizes[1];
			z.leftSize = parseFloat(z.oldStyle.left)||0;
		}
	}*/
	
	directionIncrement = 1;
	switch (direction){
		case 'up':
			z.e = new jQuery.fx(z.el.get(0), jQuery.speed(speed - 15, z.easing,callback), 'top');
			z.point = parseFloat(z.oldStyle.top)||0;
			z.unit = z.topUnit;
			directionIncrement = -1;
		break;
		case 'down':
			z.e = new jQuery.fx(z.el.get(0), jQuery.speed(speed - 15, z.easing,callback), 'top');
			z.point = parseFloat(z.oldStyle.top)||0;
			z.unit = z.topUnit;
		break;
		case 'right':
			z.e = new jQuery.fx(z.el.get(0), jQuery.speed(speed - 15, z.easing,callback), 'left');
			z.point = parseFloat(z.oldStyle.left)||0;
			z.unit = z.leftUnit;
		break;
		case 'left':
			z.e = new jQuery.fx(z.el.get(0), jQuery.speed(speed - 15, z.easing,callback), 'left');
			z.point = parseFloat(z.oldStyle.left)||0;
			z.unit = z.leftUnit;
			directionIncrement = -1;
		break;
	}
	z.e2 = new jQuery.fx(
		z.el.get(0),
		jQuery.speed
		(
		 	speed, z.easing,
			function()
			{
				z.el.css(z.oldStyle);
				if (z.type == 'out') {
					z.el.css('display', 'none');
				} else 
					z.el.css('display', z.el.get(0).ifxFirstDisplay == 'none' ? 'block' : z.el.get(0).ifxFirstDisplay);
				
				jQuery.dequeue(z.el.get(0), 'interfaceFX');
			}
		 ),
		'opacity'
	);
	if (type == 'in') {
		z.e.custom(z.point+ 100*directionIncrement, z.point);
		z.e2.custom(0,1);
	} else {
		z.e.custom(z.point, z.point + 100*directionIncrement);
		z.e2.custom(1,0);
	}
};