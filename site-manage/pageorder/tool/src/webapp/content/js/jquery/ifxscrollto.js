/**
 * Interface Elements for jQuery
 * FX - scroll to
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
 * Applies a scrolling effect to document until the element gets into viewport
 */
jQuery.fn.extend (
	{
		/**
		 * @name ScrollTo
		 * @description scrolls the document until the lement gets into viewport
		 * @param Mixed speed animation speed, integer for miliseconds, string ['slow' | 'normal' | 'fast']
		 * @param String axis (optional) whatever to scroll on vertical, horizontal or both axis ['vertical'|'horizontal'|null]
		 * @param String easing (optional) The name of the easing effect that you want to use.
		 * @type jQuery
		 * @cat Plugins/Interface
		 * @author Stefan Petre
		 */
		ScrollTo : function(speed, axis, easing) {
			o = jQuery.speed(speed);
			return this.queue('interfaceFX',function(){
				new jQuery.fx.ScrollTo(this, o, axis, easing);
			});
		},
		/**
		 * @name ScrollToAnchors
		 * @description all links to '#elementId' will animate scroll
		 * @param Mixed speed animation speed, integer for miliseconds, string ['slow' | 'normal' | 'fast']
		 * @param String axis (optional) whatever to scroll on vertical, horizontal or both axis ['vertical'|'horizontal'|null]
		 * @param String easing (optional) The name of the easing effect that you want to use.
		 * @type jQuery
		 * @cat Plugins/Interface
		 * @author Stefan Petre
		 */
		/*inspired by David Maciejewski www.macx.de*/
		ScrollToAnchors : function(speed, axis, easing) {
			return this.each(
				function()
				{
					jQuery('a[@href*="#"]', this).click(
						function(e)
						{
							parts = this.href.split('#');
							jQuery('#' + parts[1]).ScrollTo(speed, axis, easing);
							return false;
						}
					);
				}
			)
		}
	}
);

jQuery.fx.ScrollTo = function (e, o, axis, easing)
{
	var z = this;
	z.o = o;
	z.e = e;
	z.axis = /vertical|horizontal/.test(axis) ? axis : false;
	z.easing = easing;
	p = jQuery.iUtil.getPosition(e);
	s = jQuery.iUtil.getScroll();
	z.clear = function(){clearInterval(z.timer);z.timer=null;jQuery.dequeue(z.e, 'interfaceFX');};
	z.t=(new Date).getTime();
	s.h = s.h > s.ih ? (s.h - s.ih) : s.h;
	s.w = s.w > s.iw ? (s.w - s.iw) : s.w;
	z.endTop = p.y > s.h ? s.h : p.y;
	z.endLeft = p.x > s.w ? s.w : p.x;
	z.startTop = s.t;
	z.startLeft = s.l;
	z.step = function(){
		var t = (new Date).getTime();
		var n = t - z.t;
		var p = n / z.o.duration;
		if (t >= z.o.duration+z.t) {
			z.clear();
			setTimeout(function(){z.scroll(z.endTop, z.endLeft)},13);
		} else {
			if (!z.axis || z.axis == 'vertical') {
				if (!jQuery.easing || !jQuery.easing[z.easing]) {
					st = ((-Math.cos(p*Math.PI)/2) + 0.5) * (z.endTop-z.startTop) + z.startTop;
				} else {
					st = jQuery.easing[z.easing](p, n, z.startTop, (z.endTop - z.startTop), z.o.duration);
				}
			} else {
				st = z.startTop;
			}
			if (!z.axis || z.axis == 'horizontal') {
				if (!jQuery.easing || !jQuery.easing[z.easing]) {
					sl = ((-Math.cos(p*Math.PI)/2) + 0.5) * (z.endLeft-z.startLeft) + z.startLeft;
				} else {
					sl = jQuery.easing[z.easing](p, n, z.startLeft, (z.endLeft - z.startLeft), z.o.duration);
				}
			} else {
				sl = z.startLeft;
			}
			z.scroll(st, sl);
		}
	};
	z.scroll = function (t, l){window.scrollTo(l, t);};
	z.timer=setInterval(function(){z.step();},13);
};