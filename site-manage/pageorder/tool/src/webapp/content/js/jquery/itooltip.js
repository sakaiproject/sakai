/**
 * Interface Elements for jQuery
 * Tooltip
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
 * Creates tooltips using title attribute
 *
 * 
 * 
 * @name ToolTip
 * @description Creates tooltips using title attribute
 * @param Hash hash A hash of parameters
 * @option String position tooltip's position ['top'|'left'|'right'|'bottom'|'mouse']
 * @options Function onShow (optional) A function to be executed whenever the tooltip is displayed
 * @options Function onHide (optional) A function to be executed whenever the tooltip is hidden
 *
 * @type jQuery
 * @cat Plugins/Interface
 * @author Stefan Petre
 */
jQuery.iTooltip = {
	current : null,
	focused : false,
	oldTitle : null,
	focus : function(e)
	{
		jQuery.iTooltip.focused = true;
		jQuery.iTooltip.show(e, this, true);
	},
	hidefocused : function(e)
	{
		if (jQuery.iTooltip.current != this)
			return ;
		jQuery.iTooltip.focused = false;
		jQuery.iTooltip.hide(e, this);
	},
	show : function(e, el, focused)
	{
		if (jQuery.iTooltip.current != null)
			return ;
		if (!el) {
			el = this;
		}
		
		jQuery.iTooltip.current = el;
		pos = jQuery.extend(
			jQuery.iUtil.getPosition(el),
			jQuery.iUtil.getSize(el)
		);
		jEl = jQuery(el);
		title = jEl.attr('title');
		href = jEl.attr('href');
		if (title) {
			jQuery.iTooltip.oldTitle = title;
			jEl.attr('title','');
			jQuery('#tooltipTitle').html(title);
			if (href)
				jQuery('#tooltipURL').html(href.replace('http://', ''));
			else 
				jQuery('#tooltipURL').html('');
			helper = jQuery('#tooltipHelper');
			if(el.tooltipCFG.className){
				helper.get(0).className = el.tooltipCFG.className;
			} else {
				helper.get(0).className = '';
			}
			helperSize = jQuery.iUtil.getSize(helper.get(0));
			filteredPosition = focused && el.tooltipCFG.position == 'mouse' ? 'bottom' : el.tooltipCFG.position;
			
			switch (filteredPosition) {
				case 'top':
					ny = pos.y - helperSize.hb;
					nx = pos.x;
				break;
				case 'left' :
					ny = pos.y;
					nx = pos.x - helperSize.wb;
				break;
				case 'right' :
					ny = pos.y;
					nx = pos.x + pos.wb;
				break;
				case 'mouse' :
					jQuery('body').bind('mousemove', jQuery.iTooltip.mousemove);
					pointer = jQuery.iUtil.getPointer(e);
					ny = pointer.y + 15;
					nx = pointer.x + 15;
				break;
				default :
					ny = pos.y + pos.hb;
					nx = pos.x;
				break;
			}
			helper.css(
				{
					top 	: ny + 'px',
					left	: nx + 'px'
				}
			);
			if (el.tooltipCFG.delay == false) {
				helper.show();
			} else {
				helper.fadeIn(el.tooltipCFG.delay);
			}
			if (el.tooltipCFG.onShow) 
				el.tooltipCFG.onShow.apply(el);
			jEl.bind('mouseout',jQuery.iTooltip.hide)
			   .bind('blur',jQuery.iTooltip.hidefocused);
		}
	},
	mousemove : function(e)
	{
		if (jQuery.iTooltip.current == null) {
			jQuery('body').unbind('mousemove', jQuery.iTooltip.mousemove);
			return;	
		}
		pointer = jQuery.iUtil.getPointer(e);
		jQuery('#tooltipHelper').css(
			{
				top 	: pointer.y + 15 + 'px',
				left	: pointer.x + 15 + 'px'
			}
		);
	},
	hide : function(e, el)
	{
		if (!el) {
			el = this;
		}
		if (jQuery.iTooltip.focused != true && jQuery.iTooltip.current == el) {
			jQuery.iTooltip.current = null;
			jQuery('#tooltipHelper').fadeOut(1);
			jQuery(el)
				.attr('title',jQuery.iTooltip.oldTitle)
				.unbind('mouseout', jQuery.iTooltip.hide)
				.unbind('blur', jQuery.iTooltip.hidefocused);
			if (el.tooltipCFG.onHide) 
				el.tooltipCFG.onHide.apply(el);
			jQuery.iTooltip.oldTitle = null;
		}
	},
	build : function(options)
	{
		if (!jQuery.iTooltip.helper)
		{
			jQuery('body').append('<div id="tooltipHelper"><div id="tooltipTitle"></div><div id="tooltipURL"></div></div>');
			jQuery('#tooltipHelper').css(
				{
					position:	'absolute',
					zIndex:		3000,
					display: 	'none'
				}
			);
			jQuery.iTooltip.helper = true;
		}
		return this.each(
			function(){
				if(jQuery.attr(this,'title')) {
					this.tooltipCFG = {
						position	: /top|bottom|left|right|mouse/.test(options.position) ? options.position : 'bottom',
						className	: options.className ? options.className : false,
						delay		: options.delay ? options.delay : false,
						onShow		: options.onShow && options.onShow.constructor == Function ? options.onShow : false,
						onHide		: options.onHide && options.onHide.constructor == Function ? options.onHide : false
					};
					var el = jQuery(this);
					el.bind('mouseover',jQuery.iTooltip.show);
					el.bind('focus',jQuery.iTooltip.focus);
				}
			}
		);
	}
};

jQuery.fn.ToolTip = jQuery.iTooltip.build;