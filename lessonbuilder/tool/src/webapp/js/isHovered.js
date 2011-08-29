/**
 * jQuery isHovered (http://mktgdept.com/jquery-ishovered)
 * A jQuery plugin to test if an element is currently hovered
 *
 * v0.0.1 - 11 June 2010
 *
 * Copyright (c) 2010 Chad Smith (http://twitter.com/chadsmith)
 * Dual licensed under the MIT and GPL licenses.
 * http://www.opensource.org/licenses/mit-license.php
 * http://www.opensource.org/licenses/gpl-license.php
 *
 * Test if an element is hovered using: $(selector).isHovered() or $.isHovered(selector)
 *
 **/
;(function(b,c){b('*').hover(function(){b(this).data(c,1)},function(){b(this).data(c,0)}).data(c,0);b[c]=function(a){return b(a)[c]()};b.fn[c]=function(a){a=0;b(this).each(function(){a+=b(this).data(c)});return a>0}})(jQuery,'isHovered');