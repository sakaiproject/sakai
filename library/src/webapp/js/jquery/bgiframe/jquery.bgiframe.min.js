/*! Copyright (c) 2013 Brandon Aaron (http://brandon.aaron.sh)
 * Licensed under the MIT License (LICENSE.txt).
 *
 * Version 3.0.1
 *
 * Requires jQuery >= 1.2.6
 */
// https://raw.github.com/brandonaaron/bgiframe/master/jquery.bgiframe.js
(function(e){if(typeof define==="function"&&define.amd){define(["jquery"],e)}else if(typeof exports==="object"){module.exports=e}else{e(jQuery)}})(function(e){function t(e){return e&&e.constructor===Number?e+"px":e}e.fn.bgiframe=function(n){n=e.extend({top:"auto",left:"auto",width:"auto",height:"auto",opacity:true,src:"javascript:false;",conditional:/MSIE 6\.0/.test(navigator.userAgent)},n);if(!e.isFunction(n.conditional)){var r=n.conditional;n.conditional=function(){return r}}var i=e('<iframe class="bgiframe"frameborder="0"tabindex="-1"src="'+n.src+'"'+'style="display:block;position:absolute;z-index:-1;"/>');return this.each(function(){var r=e(this);if(n.conditional(this)===false){return}var o=r.children("iframe.bgiframe");var u=o.length===0?i.clone():o;u.css({top:n.top=="auto"?(parseInt(r.css("borderTopWidth"),10)||0)*-1+"px":t(n.top),left:n.left=="auto"?(parseInt(r.css("borderLeftWidth"),10)||0)*-1+"px":t(n.left),width:n.width=="auto"?this.offsetWidth+"px":t(n.width),height:n.height=="auto"?this.offsetHeight+"px":t(n.height),opacity:n.opacity===true?0:undefined});if(o.length===0){r.prepend(u)}})};e.fn.bgIframe=e.fn.bgiframe})
