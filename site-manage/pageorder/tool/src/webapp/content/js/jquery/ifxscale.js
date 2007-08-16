/**
 * Interface Elements for jQuery
 * FX - scale/grow/shrink/puff
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
 * Applies a scallign animation to element
 */
jQuery.fn.extend(
	{
		/**
		 * @name Grow
		 * @description scales the element from 0 to intitial size
		 * @param Mixed speed animation speed, integer for miliseconds, string ['slow' | 'normal' | 'fast']
		 * @param Function callback (optional) A function to be executed whenever the animation completes.
		 * @param String easing (optional) The name of the easing effect that you want to use.
		 * @type jQuery
		 * @cat Plugins/Interface
		 * @author Stefan Petre
		 */
		Grow : function(speed, callback, easing) {
			return this.queue('interfaceFX',function(){
				new jQuery.fx.Scale(this, speed, 1, 100, true, callback, 'grow', easing);
			});
		},
		
		/**
		 * @name Shrink
		 * @description scales the element from intitial size to 0
		 * @param Mixed speed animation speed, integer for miliseconds, string ['slow' | 'normal' | 'fast']
		 * @param Function callback (optional) A function to be executed whenever the animation completes.
		 * @param String easing (optional) The name of the easing effect that you want to use.
		 * @type jQuery
		 * @cat Plugins/Interface
		 * @author Stefan Petre
		 */
		Shrink : function(speed, callback, easing) {
			return this.queue('interfaceFX',function(){
				new jQuery.fx.Scale(this, speed, 100, 1, true, callback, 'shrink', easing);
			});
		},
		
		/**
		 * @name Puff
		 * @description makes element to dispear by scalling to 150% and fading it out
		 * @param Mixed speed animation speed, integer for miliseconds, string ['slow' | 'normal' | 'fast']
		 * @param Function callback (optional) A function to be executed whenever the animation completes.
		 * @param String easing (optional) The name of the easing effect that you want to use.
		 * @type jQuery
		 * @cat Plugins/Interface
		 * @author Stefan Petre
		 */
		Puff : function(speed, callback, easing) {
			return this.queue('interfaceFX',function(){
				var easing = easing || 'easeout';
				new jQuery.fx.Scale(this, speed, 100, 150, true, callback, 'puff', easing);
			});
		},
		
		/**
		 * @name Scale
		 * @description scales the element
		 * @param Mixed speed animation speed, integer for miliseconds, string ['slow' | 'normal' | 'fast']
		 * @param Integer from initial scalling procentage
		 * @param Integer to final scalling procentage
		 * @param Boolean reastore whatever to restore the initital scalling procentage when animation ends
		 * @param Function callback (optional) A function to be executed whenever the animation completes.
		 * @param String easing (optional) The name of the easing effect that you want to use.
		 * @type jQuery
		 * @cat Plugins/Interface
		 * @author Stefan Petre
		 */
		Scale : function(speed, from, to, restore, callback, easing) {
			return this.queue('interfaceFX',function(){
				new jQuery.fx.Scale(this, speed, from, to, restore, callback, 'Scale', easing);
			});
		}
	}
);

jQuery.fx.Scale = function (e, speed, from, to, restore, callback, type, easing)
{
	if (!jQuery.fxCheckTag(e)) {
		jQuery.dequeue(e, 'interfaceFX');
		return false;
	}
	var z = this;
	z.el = jQuery(e);
	z.from = parseInt(from) || 100;
	z.to = parseInt(to) || 100;
	z.easing = typeof callback == 'string' ? callback : easing||null;
	z.callback = typeof callback == 'function' ? callback : null;
	z.duration = jQuery.speed(speed).duration;
	z.restore = restore|| null;
	z.oldP = jQuery.iUtil.getSize(e);
	z.oldStyle = {
		width: z.el.css('width'),
		height: z.el.css('height'),
		fontSize: z.el.css('fontSize')||'100%',
		position : z.el.css('position'),
		display : z.el.css('display'),
		top : z.el.css('top'),
		left : z.el.css('left'),
		overflow : z.el.css('overflow'),
		borderTopWidth : z.el.css('borderTopWidth'),
		borderRightWidth : z.el.css('borderRightWidth'),
		borderBottomWidth : z.el.css('borderBottomWidth'),
		borderLeftWidth : z.el.css('borderLeftWidth'),
		paddingTop : z.el.css('paddingTop'),
		paddingRight : z.el.css('paddingRight'),
		paddingBottom : z.el.css('paddingBottom'),
		paddingLeft : z.el.css('paddingLeft')
	};
	z.width = parseInt(z.oldStyle.width)||e.offsetWidth||0;
	z.height = parseInt(z.oldStyle.height)||e.offsetHeight||0;
	z.top = parseInt(z.oldStyle.top)||0;
	z.left = parseInt(z.oldStyle.left)||0;
	sizes = ['em','px','pt','%'];
	for(i in sizes) {
		if (z.oldStyle.fontSize.indexOf(sizes[i])>0) {
			z.fontUnit = sizes[i];
			z.fontSize = parseFloat(z.oldStyle.fontSize);
		}
		if (z.oldStyle.borderTopWidth.indexOf(sizes[i])>0) {
			z.borderTopUnit = sizes[i];
			z.borderTopSize = parseFloat(z.oldStyle.borderTopWidth)||0;
		}
		if (z.oldStyle.borderRightWidth.indexOf(sizes[i])>0) {
			z.borderRightUnit = sizes[i];
			z.borderRightSize = parseFloat(z.oldStyle.borderRightWidth)||0;
		}
		if (z.oldStyle.borderBottomWidth.indexOf(sizes[i])>0) {
			z.borderBottomUnit = sizes[i];
			z.borderBottomSize = parseFloat(z.oldStyle.borderBottomWidth)||0;
		}
		if (z.oldStyle.borderLeftWidth.indexOf(sizes[i])>0) {
			z.borderLeftUnit = sizes[i];
			z.borderLeftSize = parseFloat(z.oldStyle.borderLeftWidth)||0;
		}
		if (z.oldStyle.paddingTop.indexOf(sizes[i])>0) {
			z.paddingTopUnit = sizes[i];
			z.paddingTopSize = parseFloat(z.oldStyle.paddingTop)||0;
		}
		if (z.oldStyle.paddingRight.indexOf(sizes[i])>0) {
			z.paddingRightUnit = sizes[i];
			z.paddingRightSize = parseFloat(z.oldStyle.paddingRight)||0;
		}
		if (z.oldStyle.paddingBottom.indexOf(sizes[i])>0) {
			z.paddingBottomUnit = sizes[i];
			z.paddingBottomSize = parseFloat(z.oldStyle.paddingBottom)||0;
		}
		if (z.oldStyle.paddingLeft.indexOf(sizes[i])>0) {
			z.paddingLeftUnit = sizes[i];
			z.paddingLeftSize = parseFloat(z.oldStyle.paddingLeft)||0;
		}
	}
	
	
	if (z.oldStyle.position != 'relative' && z.oldStyle.position != 'absolute') {
		z.el.css('position', 'relative');
	}
	z.el.css('overflow', 'hidden');
	z.type = type;
	switch(z.type)
	{
		case 'grow':
			z.startTop = z.top + z.oldP.h/2;
			z.endTop = z.top;
			z.startLeft = z.left + z.oldP.w/2;
			z.endLeft = z.left;
			break;
		case 'shrink':
			z.endTop = z.top + z.oldP.h/2;
			z.startTop = z.top;
			z.endLeft = z.left + z.oldP.w/2;
			z.startLeft = z.left;
			break;
		case 'puff':
			z.endTop = z.top - z.oldP.h/4;
			z.startTop = z.top;
			z.endLeft = z.left - z.oldP.w/4;
			z.startLeft = z.left;
			break;
	}
	z.firstStep = false;
	z.t=(new Date).getTime();
	z.clear = function(){clearInterval(z.timer);z.timer=null;};
	z.step = function(){
		if (z.firstStep == false) {
			z.el.show();
			z.firstStep = true;
		}
		var t = (new Date).getTime();
		var n = t - z.t;
		var p = n / z.duration;
		if (t >= z.duration+z.t) {
			setTimeout(
				function(){
						o = 1;	
					if (z.type) {
						t = z.endTop;
						l = z.endLeft;
						if (z.type == 'puff')
							o = 0;
					}
					z.zoom(z.to, l, t, true, o);
				},
				13
			);
			z.clear();
		} else {
			o = 1;
			if (!jQuery.easing || !jQuery.easing[z.easing]) {
				s = ((-Math.cos(p*Math.PI)/2) + 0.5) * (z.to-z.from) + z.from;
			} else {
				s = jQuery.easing[z.easing](p, n, z.from, (z.to-z.from), z.duration);
			}
			if (z.type) {
				if (!jQuery.easing || !jQuery.easing[z.easing]) {
					t = ((-Math.cos(p*Math.PI)/2) + 0.5) * (z.endTop-z.startTop) + z.startTop;
					l = ((-Math.cos(p*Math.PI)/2) + 0.5) * (z.endLeft-z.startLeft) + z.startLeft;
					if (z.type == 'puff')
						o = ((-Math.cos(p*Math.PI)/2) + 0.5) * (-0.9999) + 0.9999;
				} else {
					t = jQuery.easing[z.easing](p, n, z.startTop, (z.endTop-z.startTop), z.duration);
					l = jQuery.easing[z.easing](p, n, z.startLeft, (z.endLeft-z.startLeft), z.duration);
					if (z.type == 'puff')
						o = jQuery.easing[z.easing](p, n, 0.9999, -0.9999, z.duration);
				}
			}
			z.zoom(s, l, t, false, o);
		}
	};
	z.timer=setInterval(function(){z.step();},13);
	z.zoom = function(percent, left, top, finish, opacity)
	{
		z.el
			.css('height', z.height * percent/100 + 'px')
			.css('width', z.width * percent/100 + 'px')
			.css('left', left + 'px')
			.css('top', top + 'px')
			.css('fontSize', z.fontSize * percent /100 + z.fontUnit);
		if (z.borderTopSize)
			z.el.css('borderTopWidth', z.borderTopSize * percent /100 + z.borderTopUnit);
		if (z.borderRightSize)
			z.el.css('borderRightWidth', z.borderRightSize * percent /100 + z.borderRightUnit);
		if (z.borderBottomSize)
			z.el.css('borderBottomWidth', z.borderBottomSize * percent /100 + z.borderBottomUnit);
		if (z.borderLeftSize)
			z.el.css('borderLeftWidth', z.borderLeftSize * percent /100 + z.borderLeftUnit);
		if (z.paddingTopSize)
			z.el.css('paddingTop', z.paddingTopSize * percent /100 + z.paddingTopUnit);
		if (z.paddingRightSize)
			z.el.css('paddingRight', z.paddingRightSize * percent /100 + z.paddingRightUnit);
		if (z.paddingBottomSize)
			z.el.css('paddingBottom', z.paddingBottomSize * percent /100 + z.paddingBottomUnit);
		if (z.paddingLeftSize)
			z.el.css('paddingLeft', z.paddingLeftSize * percent /100 + z.paddingLeftUnit);
		if (z.type == 'puff') {
			if (window.ActiveXObject)
				z.el.get(0).style.filter = "alpha(opacity=" + opacity*100 + ")";
			z.el.get(0).style.opacity = opacity;
		}
		if (finish){
			if (z.restore){
				z.el.css(z.oldStyle);
			}
			if (z.type == 'shrink' || z.type == 'puff'){
				z.el.css('display', 'none');
				if (z.type == 'puff') {
					if (window.ActiveXObject)
						z.el.get(0).style.filter = "alpha(opacity=" + 100 + ")";
					z.el.get(0).style.opacity = 1;
				}
			}else 
				z.el.css('display', 'block');
			if (z.callback)
				z.callback.apply(z.el.get(0));
			
			jQuery.dequeue(z.el.get(0), 'interfaceFX');
		}
	};
};