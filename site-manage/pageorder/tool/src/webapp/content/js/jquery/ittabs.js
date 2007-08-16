/**
 * Interface Elements for jQuery
 * TTabs
 * 
 * http://interface.eyecon.ro
 * 
 * Copyright (c) 2006 Stefan Petre
 * Dual licensed under the MIT (MIT-LICENSE.txt) 
 * and GPL (GPL-LICENSE.txt) licenses.
 *   
 *
 *
 */

jQuery.iTTabs =
{
	doTab : function(e)
	{
		pressedKey = e.charCode || e.keyCode || -1;
		if (pressedKey == 9) {
			if (window.event) {
				window.event.cancelBubble = true;
				window.event.returnValue = false;
			} else {
				e.preventDefault();
				e.stopPropagation();
			}
			if (this.createTextRange) {
				document.selection.createRange().text="\t";
				this.onblur = function() { this.focus(); this.onblur = null; };
			} else if (this.setSelectionRange) {
				start = this.selectionStart;
				end = this.selectionEnd;
				this.value = this.value.substring(0, start) + "\t" + this.value.substr(end);
				this.setSelectionRange(start + 1, start + 1);
				this.focus();
			}
			return false;
		}
	},
	destroy : function()
	{
		return this.each(
			function()
			{
				if (this.hasTabsEnabled && this.hasTabsEnabled == true) {
					jQuery(this).unbind('keydown', jQuery.iTTabs.doTab);
					this.hasTabsEnabled = false;
				}
			}
		);
	},
	build : function()
	{
		return this.each(
			function()
			{
				if (this.tagName == 'TEXTAREA' && (!this.hasTabsEnabled || this.hasTabsEnabled == false)) {
					jQuery(this).bind('keydown', jQuery.iTTabs.doTab);
					this.hasTabsEnabled = true;
				}
			}
		);			
	}
};

jQuery.fn.extend (
	{
		/**
		 * Enable tabs in textareas
		 * 
		 * @name EnableTabs
		 * @description Enable tabs in textareas
		 *
		 * @type jQuery
		 * @cat Plugins/Interface
		 * @author Stefan Petre
		 */
		EnableTabs : jQuery.iTTabs.build,
		/**
		 * Disable tabs in textareas
		 * 
		 * @name DisableTabs
		 * @description Disable tabs in textareas
		 *
		 * @type jQuery
		 * @cat Plugins/Interface
		 * @author Stefan Petre
		 */
		DisableTabs : jQuery.iTTabs.destroy
	}
);