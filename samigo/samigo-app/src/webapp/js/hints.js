// Title: Tigra Hints
// URL: http://www.softcomplex.com/products/tigra_hints/
// Version: 1.3
// Date: 09/03/2003 (mm/dd/yyyy)
// Feedback: feedback@softcomplex.com (specify product title in the subject)
// Note: Permission given to use this script in ANY kind of applications if
//    header lines are left unchanged.
// About us: Our company provides offshore IT consulting services.
//    Contact us at sales@softcomplex.com if you have any programming task you
//    want to be handled by professionals. Our typical hourly rate is $20.

var THintsS = [];
function THints (o_cfg, items) {
	this.n_id = THintsS.length;
	THintsS[this.n_id] = this;
	this.top = o_cfg.top ? o_cfg.top : 0;
	this.left = o_cfg.left ? o_cfg.left : 0;
	this.n_dl_show = o_cfg.show_delay;
	this.n_dl_hide = o_cfg.hide_delay;
	this.b_wise = o_cfg.wise;
	this.b_follow = o_cfg.follow;
	this.x = 0;
	this.y = 0;
	this.divs = [];
	this.show  = TTipShow;
	this.showD = TTipShowD;
	this.hide = TTipHide;
	this.move = TTipMove;
	// register the object in global collection
	this.n_id = THintsS.length;
	THintsS[this.n_id] = this;
	// filter Netscape 4.x out
	if (document.layers) return;
	var b_IE = navigator.userAgent.indexOf('MSIE') > -1,
	s_tag = ['<div id="TTip%name%" style="visibility:hidden;position:absolute;top:0px;left:0px;',   b_IE ? 'width:1px;height:1px;' : '', o_cfg['z-index'] != null ? 'z-index:' + o_cfg['z-index'] : '', '"><table cellpadding="0" cellspacing="0" border="0"><tr><td class="', o_cfg.css, '" nowrap>%text%</td></tr></table></div>'].join('');


	this.getElem = 
		function (id) { return document.all ? document.all[id] : document.getElementById(id); };
	this.showElem = 
		function (id, hide) { this.divs[id].o_css.visibility = hide ? 'hidden' : 'visible'; };
	this.getWinSz = window.innerHeight != null 
		? function (b_hight) { return b_hight ? innerHeight : innerWidth; }
		: function (b_hight) { return document.body[b_hight ? 'clientHeight' : 'clientWidth']; };	
	this.getWinSc = window.innerHeight != null 
		? function (b_hight) { return b_hight ? pageYOffset : pageXOffset; }
		: function (b_hight) { return document.body[b_hight ? 'scrollTop' : 'scrollLeft']; };	
	if (window.opera) {
		this.getSize = function (id, b_hight) { 
			return this.divs[id].o_css[b_hight ? 'pixelHeight' : 'pixelWidth']
		};
		document.onmousemove = function () {
			for (var n_i in THintsS) {
				THintsS[n_i].x = event.clientX;
				THintsS[n_i].y = event.clientY;
				if (THintsS[n_i].b_follow && THintsS[n_i].visible) 
					THintsS[n_i].move(THintsS[n_i].visible);
			}
			return true;
		};
	}
	else {
		this.getSize = function (id, b_hight) { 
			return this.divs[id].o_obj[b_hight ? 'offsetHeight' : 'offsetWidth'] 
		};
		document.onmousemove = b_IE
		? function () {
			for (var n_i in THintsS) {
				THintsS[n_i].x = event.clientX + document.body.scrollLeft;
				THintsS[n_i].y = event.clientY + document.body.scrollTop;
				if (THintsS[n_i].b_follow && THintsS[n_i].visible) 
					THintsS[n_i].move(THintsS[n_i].visible);
			}
			return true;
		} 
		: function (e) {
			for (var n_i in THintsS) {
				THintsS[n_i].x = e.pageX;
				THintsS[n_i].y = e.pageY;
				if (THintsS[n_i].b_follow && THintsS[n_i].visible) 
					THintsS[n_i].move(THintsS[n_i].visible)
			}
			return true;
		};
	}
	for (i in items) {
		document.write (s_tag.replace(/%text%/, items[i]).replace(/%name%/, i));
		this.divs[i] = { 'o_obj' : this.getElem('TTip' + i) };
		this.divs[i].o_css = this.divs[i].o_obj.style;
	}
}

function TTipShow (id) {
	if (document.layers) return;
	this.hide();
	if (this.divs[id]) {
		if (this.n_dl_show) this.divs[id].timer = setTimeout("THintsS[" + this.n_id + "].showD(" + id + ")", this.n_dl_show);
		else this.showD(id);
		this.visible = id;
	}
}

function TTipShowD (id) {
	this.move(id);
	this.showElem(id);
	if (this.n_dl_hide) this.timer = setTimeout("THintsS[" + this.n_id + "].hide()", this.n_dl_hide);
}

function TTipMove (id) {
	var n_x = this.x + this.left, n_y = this.y + this.top;
	if (this.b_wise) {
		var n_w = this.getSize(id), n_h = this.getSize(id, true),
		n_win_w = this.getWinSz(), n_win_h = this.getWinSz(true),
		n_win_l = this.getWinSc(), n_win_t = this.getWinSc(true);
		if (n_x + n_w > n_win_w + n_win_l) n_x = n_win_w + n_win_l - n_w;
		if (n_x < n_win_l) n_x = n_win_l;
		if (n_y + n_h > n_win_h + n_win_t) n_y = n_win_h + n_win_t - n_h;
		if (n_y < n_win_t) n_y = n_win_t;
	}
	this.divs[id].o_css.left = n_x;
	this.divs[id].o_css.top = n_y;
}

function TTipHide () {
	if (this.timer) clearTimeout(this.timer);
	if (this.visible != null) {
		if (this.divs[this.visible].timer) clearTimeout(this.divs[this.visible].timer);
		setTimeout("THintsS[" + this.n_id + "].showElem(" + this.visible + ", true)", 10);
		this.visible = null;
	}
}