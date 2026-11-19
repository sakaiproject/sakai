function CMSpecialEffectInstance(A,B){A.show=true;A.menu=B;B.cmEffect=A;this.effect=A;}CMSpecialEffectInstance.prototype.canShow=function(A){if(A){if(this.effect.show){return false;}this.effect.show=true;}else{if(!this.effect.show){return false;}}return true;};CMSpecialEffectInstance.prototype.canHide=function(B){var A=this.effect;if(B){if(!A.show){return false;}A.show=false;}else{if(A.show){return false;}}return true;};CMSpecialEffectInstance.prototype.startShowing=function(){var menu=this.effect.menu;menu.style.visibility="visible";
/*@cc_on
		@if (@_jscript_version >= 5.5)
			if (menu.cmFrameObj)
			{
				var frameObj = menu.cmFrameObj;
				frameObj.style.display = 'block';
			}
		@end
	@*/
};CMSpecialEffectInstance.prototype.finishShowing=function(){};CMSpecialEffectInstance.prototype.finishHiding=function(){var menu=this.effect.menu;menu.style.visibility="hidden";menu.style.top="0px";menu.style.left="0px";
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
menu.cmEffect=null;menu.cmOrient=null;this.effect.menu=null;};function CMSlidingEffectInstance(menu,orient,speed){this.base=new CMSpecialEffectInstance(this,menu);menu.style.overflow="hidden";this.x=menu.offsetLeft;this.y=menu.offsetTop;if(orient.charAt(0)=="h"){this.slideOrient="h";this.slideDir=orient.charAt(1);}else{this.slideOrient="v";this.slideDir=orient.charAt(2);}this.speed=speed;this.fullWidth=menu.offsetWidth;this.fullHeight=menu.offsetHeight;this.percent=0;
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
}CMSlidingEffectInstance.prototype.showEffect=function(B){if(!this.base.canShow(B)){return ;}var A=this.percent;if(this.slideOrient=="h"){this.slideMenuV();}else{this.slideMenuH();}if(A==0){this.base.startShowing();}if(A<100){this.percent+=this.speed;cmTimeEffect(this.menu.id,this.show,10);}else{if(this.show){this.base.finishShowing();}}};CMSlidingEffectInstance.prototype.hideEffect=function(B){if(!this.base.canHide(B)){return ;}var A=this.percent;if(this.slideOrient=="h"){this.slideMenuV();}else{this.slideMenuH();}if(A>0){this.percent-=this.speed;cmTimeEffect(this.menu.id,this.show,10);}else{if(!this.show){this.menu.style.clip="auto";this.base.finishHiding();}}};CMSlidingEffectInstance.prototype.slideMenuH=function(){var percent=this.percent;if(percent<0){percent=0;}if(percent>100){percent=100;}var fullWidth=this.fullWidth;var fullHeight=this.fullHeight;var x=this.x;var space=percent*fullWidth/100;var menu=this.menu;if(this.slideDir=="l"){menu.style.left=(x+fullWidth-space)+"px";menu.style.clip="rect(0px "+space+"px "+fullHeight+"px 0px)";}else{menu.style.left=(x-fullWidth+space)+"px";menu.style.clip="rect(0px "+fullWidth+"px "+fullHeight+"px "+(fullWidth-space)+"px)";}
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
};CMSlidingEffectInstance.prototype.slideMenuV=function(){var percent=this.percent;if(percent<0){percent=0;}if(percent>100){percent=100;}var fullWidth=this.fullWidth;var fullHeight=this.fullHeight;var y=this.y;var space=percent*fullHeight/100;var menu=this.menu;if(this.slideDir=="b"){menu.style.top=(y-fullHeight+space)+"px";menu.style.clip="rect("+(fullHeight-space)+"px "+fullWidth+"px "+fullHeight+"px 0px)";}else{menu.style.top=(y+fullHeight-space)+"px";menu.style.clip="rect(0px "+fullWidth+"px "+space+"px 0px)";}
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
};function CMSlidingEffect(A){if(!A){A=10;}else{if(A<=0){A=10;}else{if(A>=100){A=100;}}}this.speed=A;}CMSlidingEffect.prototype.getInstance=function(B,A){return new CMSlidingEffectInstance(B,A,this.speed);};function CMFadingEffectInstance(C,A,B){this.base=new CMSpecialEffectInstance(this,C);C.style.overflow="hidden";this.showSpeed=A;this.hideSpeed=B;this.opacity=0;}CMFadingEffectInstance.prototype.showEffect=function(C){if(!this.base.canShow(C)){return ;}var B=this.menu;var A=this.opacity;this.setOpacity();if(A==0){this.base.startShowing();}if(A<100){this.opacity+=10;cmTimeEffect(B.id,this.show,this.showSpeed);}else{if(this.show){this.base.finishShowing();}}};CMFadingEffectInstance.prototype.hideEffect=function(C){if(!this.base.canHide(C)){return ;}var B=this.menu;var A=this.opacity;this.setOpacity();if(this.opacity>0){this.opacity-=10;cmTimeEffect(B.id,this.show,this.hideSpeed);}else{if(!this.show){this.base.finishHiding();}}};CMFadingEffectInstance.prototype.setOpacity=function(){this.menu.style.opacity=this.opacity/100;
/*@cc_on
		this.menu.style.filter = 'alpha(opacity=' + this.opacity + ')';
		//this.menu.style.filter = 'progid:DXImageTransform.Microsoft.Alpha(opacity=' + this.opacity + ')';
	@*/
};function CMFadingEffect(A,B){this.showSpeed=A;this.hideSpeed=B;}CMFadingEffect.prototype.getInstance=function(B,A){return new CMFadingEffectInstance(B,this.showSpeed,this.hideSpeed);};