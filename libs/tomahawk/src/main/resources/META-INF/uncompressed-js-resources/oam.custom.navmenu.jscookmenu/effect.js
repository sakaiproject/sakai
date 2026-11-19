/*
	JSCookMenu Effect (c) Copyright 2002-2006 by Heng Yuan

	http://jscook.sourceforge.net/JSCookMenu/

	Permission is hereby granted, free of charge, to any person obtaining a
	copy of this software and associated documentation files (the "Software"),
	to deal in the Software without restriction, including without limitation
	the rights to use, copy, modify, merge, publish, distribute, sublicense,
	and/or sell copies of the Software, and to permit persons to whom the
	Software is furnished to do so, subject to the following conditions:

	The above copyright notice and this permission notice shall be included
	in all copies or substantial portions of the Software.

	THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
	OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
	ITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
	AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
	LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
	FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
	DEALINGS IN THE SOFTWARE.
*/
//
// utiltity object to simplify common tasks in effects.
//
function CMSpecialEffectInstance (effect, menu)
{
	effect.show = true;
	effect.menu = menu;
	menu.cmEffect = effect;
	this.effect = effect;
}

CMSpecialEffectInstance.prototype.canShow = function (changed)
{
	if (changed)
	{
		if (this.effect.show)
			return false;
		this.effect.show = true;
	}
	else if (!this.effect.show)
		return false;
	return true;
}

CMSpecialEffectInstance.prototype.canHide = function (changed)
{
	var effect = this.effect;
	if (changed)
	{
		if (!effect.show)
			return false;
		effect.show = false;
	}
	else if (effect.show)
		return false;
	return true;
}

//
// public function to be called to initate the display of the
// menu.
//
CMSpecialEffectInstance.prototype.startShowing = function ()
{
	var menu = this.effect.menu;
	menu.style.visibility = 'visible';
	/*@cc_on
		@if (@_jscript_version >= 5.5)
			if (menu.cmFrameObj)
			{
				var frameObj = menu.cmFrameObj;
				frameObj.style.display = 'block';
			}
		@end
	@*/
}

//
// public function to be called after showing effect is finished.
//
CMSpecialEffectInstance.prototype.finishShowing = function ()
{
}

//
// clean up after finish hiding effect.
//
CMSpecialEffectInstance.prototype.finishHiding = function ()
{
	var menu = this.effect.menu;
	menu.style.visibility = 'hidden';
	menu.style.top = '0px';
	menu.style.left = '0px';
	/*@cc_on
		@if (@_jscript_version >= 5.5)
			if (menu.cmFrameObj)
			{
				var frameObj = menu.cmFrameObj;
				frameObj.style.display = 'none';
				frameObj.style.top = '0px';
				frameObj.style.left = '0px';
				menu.cmFrameObj = null;
				cmFreeFrame (frameObj);
			}
		@end
	@*/
	menu.cmEffect = null;
	menu.cmOrient = null;
	this.effect.menu = null;
}

//
// this is the internal class to perform the sliding effect
//
function CMSlidingEffectInstance (menu, orient, speed)
{
	this.base = new CMSpecialEffectInstance (this, menu);

	menu.style.overflow = 'hidden';

	this.x = menu.offsetLeft;
	this.y = menu.offsetTop;

	if (orient.charAt (0) == 'h')
	{
		this.slideOrient = 'h';
		this.slideDir = orient.charAt (1);
	}
	else
	{
		this.slideOrient = 'v';
		this.slideDir = orient.charAt (2);
	}

	this.speed = speed;
	this.fullWidth = menu.offsetWidth;
	this.fullHeight = menu.offsetHeight;
	this.percent = 0;
	/*@cc_on
		@if (@_jscript_version >= 5.5)
			if (menu.cmFrameObj)
			{
				var frameObj = menu.cmFrameObj;
				this.frameX = frameObj.offsetLeft;
				this.frameY = frameObj.offsetTop;
				this.frameWidth = frameObj.offsetWidth;
				this.frameHeight = frameObj.offsetHeight;
			}
		@end
	@*/
}
	// public function to show the menu
CMSlidingEffectInstance.prototype.showEffect = function (changed)
{
	if (!this.base.canShow (changed))
		return;

	var percent = this.percent;
	if (this.slideOrient == 'h')
		this.slideMenuV ();
	else
		this.slideMenuH ();

	if (percent == 0)
	{
		this.base.startShowing ();
	}

	if (percent < 100)
	{
		this.percent += this.speed;
		cmTimeEffect (this.menu.id, this.show, 10);
	}
	else if (this.show)
	{
		this.base.finishShowing ();
	}
}

// public function to hide the menu
CMSlidingEffectInstance.prototype.hideEffect = function (changed)
{
	if (!this.base.canHide (changed))
		return;

	var percent = this.percent;
	if (this.slideOrient == 'h')
		this.slideMenuV ();
	else
		this.slideMenuH ();

	if (percent > 0)
	{
		this.percent -= this.speed;
		cmTimeEffect (this.menu.id, this.show, 10);
	}
	else if (!this.show)
	{
		this.menu.style.clip = 'auto';
		this.base.finishHiding ();
	}
}

// internal function to scroll a menu left/right
CMSlidingEffectInstance.prototype.slideMenuH = function ()
{
	var percent = this.percent;
	if (percent < 0)
		percent = 0;
	if (percent > 100)
		percent = 100;
	var fullWidth = this.fullWidth;
	var fullHeight = this.fullHeight;
	var x = this.x;
	var space = percent * fullWidth / 100;
	var menu = this.menu;

	if (this.slideDir == 'l')
	{
		menu.style.left = (x + fullWidth - space) + 'px';
		menu.style.clip = 'rect(0px ' + space + 'px ' + fullHeight + 'px 0px)';
	}
	else
	{
		menu.style.left = (x - fullWidth + space) + 'px';
		menu.style.clip = 'rect(0px ' + fullWidth + 'px ' + fullHeight + 'px ' + (fullWidth - space) + 'px)';
	}
	/*@cc_on
		@if (@_jscript_version >= 5.5)
			if (menu.cmFrameObj)
			{
				var frameObj = menu.cmFrameObj;
				if (this.slideDir == 'l')
					frameObj.style.left = (this.frameX + fullWidth - space) + 'px';
				frameObj.style.width = space + 'px';
			}
		@end
	@*/
}

// internal function to scroll a menu up/down
CMSlidingEffectInstance.prototype.slideMenuV = function ()
{
	var percent = this.percent;
	if (percent < 0)
		percent = 0;
	if (percent > 100)
		percent = 100;
	var fullWidth = this.fullWidth;
	var fullHeight = this.fullHeight;
	var y = this.y;
	var space = percent * fullHeight / 100;
	var menu = this.menu;

	if (this.slideDir == 'b')
	{
		menu.style.top = (y - fullHeight + space) + 'px';
		menu.style.clip = 'rect(' + (fullHeight - space) + 'px ' + fullWidth + 'px ' + fullHeight + 'px 0px)';
	}
	else
	{
		menu.style.top = (y + fullHeight - space) + 'px';
		menu.style.clip = 'rect(0px ' + fullWidth + 'px ' + space + 'px 0px)';
	}
	/*@cc_on
		@if (@_jscript_version >= 5.5)
			if (menu.cmFrameObj)
			{
				var frameObj = menu.cmFrameObj;
				if (this.slideDir == 'u')
					frameObj.style.top = (this.frameX - space) + 'px';
				frameObj.style.height = space + 'px';
			}
		@end
	@*/
}

//
// call
//		new CMSlidingEffect (speed)
// to create a new effect object.
//
function CMSlidingEffect (speed)
{
	if (!speed)
		speed = 10;
	else if (speed <= 0)
		speed = 10;
	else if (speed >= 100)
		speed = 100;
	this.speed = speed;
}

CMSlidingEffect.prototype.getInstance = function (menu, orient)
{
	return new CMSlidingEffectInstance (menu, orient, this.speed);
}

//
// this is the internal class to perform the sliding effect
//
function CMFadingEffectInstance (menu, showSpeed, hideSpeed)
{
	this.base = new CMSpecialEffectInstance (this, menu);

	menu.style.overflow = 'hidden';

	this.showSpeed = showSpeed;
	this.hideSpeed = hideSpeed;

	this.opacity = 0;
}

// public function to show the menu
CMFadingEffectInstance.prototype.showEffect = function (changed)
{
	if (!this.base.canShow (changed))
		return;

	var menu = this.menu;
	var opacity = this.opacity;

	this.setOpacity ();

	if (opacity == 0)
	{
		this.base.startShowing ();
	}

	if (opacity < 100)
	{
		this.opacity += 10;
		cmTimeEffect (menu.id, this.show, this.showSpeed);
	}
	else if (this.show)
	{
		this.base.finishShowing ();
	}
}

// public function to hide the menu
CMFadingEffectInstance.prototype.hideEffect = function (changed)
{
	if (!this.base.canHide (changed))
		return;

	var menu = this.menu;
	var opacity = this.opacity;

	this.setOpacity ();

	if (this.opacity > 0)
	{
		this.opacity -= 10;
		cmTimeEffect (menu.id, this.show, this.hideSpeed);
	}
	else if (!this.show)
	{
		this.base.finishHiding ();
	}
}

// internal functions
CMFadingEffectInstance.prototype.setOpacity = function ()
{
	this.menu.style.opacity = this.opacity / 100;
	/*@cc_on
		this.menu.style.filter = 'alpha(opacity=' + this.opacity + ')';
		//this.menu.style.filter = 'progid:DXImageTransform.Microsoft.Alpha(opacity=' + this.opacity + ')';
	@*/
}

function CMFadingEffect (showSpeed, hideSpeed)
{
	this.showSpeed = showSpeed;
	this.hideSpeed = hideSpeed;
}

CMFadingEffect.prototype.getInstance = function (menu, orient)
{
	return new CMFadingEffectInstance (menu, this.showSpeed, this.hideSpeed);
}
