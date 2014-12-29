// Title: Tigra Hints Config file
// URL: http://www.softcomplex.com/products/tigra_hints/
// Version: 1.3
// Date: 09/03/2003 (mm/dd/yyyy)
// Feedback: feedback@softcomplex.com (specify product title in the subject)
// Note: Permission given to use this script in ANY kind of applications if
//    header lines are left unchanged.
// About us: Our company provides offshore IT consulting services.
//    Contact us at sales@softcomplex.com if you have any programming task you
//    want to be handled by professionals. Our typical hourly rate is $20.
var HINTS_CFG = {
	'top'        : 5, // a vertical offset of a hint from mouse pointer
	'left'       : 5, // a horizontal offset of a hint from mouse pointer
	'css'        : 'hintsClass', // a style class name for all hints, TD object
	'show_delay' : 500, // a delay between object mouseover and hint appearing
	'hide_delay' : 2000, // a delay between hint appearing and hint hiding
	'wise'       : true,
	'follow'     : true,
	'z-index'    : 0 // a z-index for all hint layers
},

HINTS_ITEMS = [
	wrap("Tigra Hints Product Page"),
	wrap("SoftComplex Download Page"),
	wrap("Tigra Hints Forum"),
	wrap_img("01", "A picture of a tiger"),
	wrap_img("70","A picture of a tiger"),
	wrap_img("20","A picture of a puma"),
	'<table border="0" cellspacing="0" cellpadding="0" bgcolor="#4682B4"><tr><td><table border="0" cellspacing="1" cellpadding="3"><tr class="row"><td nowrap><b>currentStyle</b></td><td align="center">supported</td></tr><tr class="row"><td nowrap><b>getComputedStyle()</b></td><td align="center">unsupported</td></tr><tr class="row"><td nowrap><b>style</b></td><td align="center">supported</td></tr></table></td></tr></table>',
	'<table border="0" cellspacing="0" cellpadding="0" bgcolor="#4682B4"><tr><td><table border="0" cellspacing="1" cellpadding="3"><tr class="row"><td><b>currentStyle</b></td><td align="center">unsupported</td></tr><tr class="row"><td nowrap><b>getComputedStyle()</b></td><td align="center">supported</td></tr><tr class="row"><td><b>style</b></td><td align="center">supported</td></tr></table></td></tr></table>',
	'<table border="0" cellspacing="0" cellpadding="0" bgcolor="#4682B4"><tr><td><table border="0" cellspacing="1" cellpadding="3"><tr class="row"><td><b>currentStyle</b></td><td align="center">unsupported</td></tr><tr class="row"><td nowrap><b>getComputedStyle()</b></td><td align="center">unsupported</td></tr><tr class="row"><td><b>style</b></td><td align="center">supported</td></tr></table></td></tr></table>',
	'<table border="0" cellspacing="0" cellpadding="0" bgcolor="#4682B4"><tr><td><table border="0" cellspacing="1" cellpadding="3"><tr class="row"><td><b>cssRules[] </b></td><td align="center">unsupported</td></tr><tr class="row"><td><b>rules[] </b></td><td align="center">supported</td></tr><tr class="row"><td nowrap><b>styleSheets[] </b></td><td align="center">supported</td></tr></table></td></tr></table>',
	'<table border="0" cellspacing="0" cellpadding="0" bgcolor="#4682B4"><tr><td><table border="0" cellspacing="1" cellpadding="3"><tr class="row"><td><b>cssRules[] </b></td><td align="center">supported</td></tr><tr class="row"><td><b>rules[]</b></td><td align="center">unsupported</td></tr><tr class="row"><td nowrap><b>styleSheets[]</b></td><td align="center">supported</td></tr></table></td></tr></table>',
	'<table border="0" cellspacing="0" cellpadding="0" bgcolor="#4682B4"><tr><td><table border="0" cellspacing="1" cellpadding="3"><tr class="row"><td><b>addRule() </b></td><td align="center">supported</td></tr><tr class="row"><td nowrap><b>deleteRule() </b></td><td align="center">unsupported</td></tr><tr class="row"><td nowrap><b>insertRule() </b></td><td align="center">unsupported</td></tr><tr class="row"><td nowrap><b>removeRule()</b></td><td align="center">supported</td></tr></table></td></tr></table>',
	'<table border="0" cellspacing="0" cellpadding="0" bgcolor="#4682B4"><tr><td><table border="0" cellspacing="1" cellpadding="3"><tr class="row"><td><b>addRule()</b></td><td align="center">unsupported</td></tr><tr class="row"><td nowrap><b>deleteRule()</b></td><td align="center">supported</td></tr><tr class="row"><td nowrap><b>insertRule() </b></td><td align="center">supported</td></tr><tr class="row"><td nowrap><b>removeRule()</b></td><td align="center">unsupported</td></tr></table></td></tr></table>',
	'<table border="0" cellspacing="0" cellpadding="0" bgcolor="#4682B4"><tr><td><table border="0" cellspacing="1" cellpadding="3"><tr class="row"><td nowrap><b>createCSS StyleSheet()</b></td><td align="center">unsupported</td></tr><tr class="row"><td nowrap><b>createStyleSheet() </b></td><td align="center">supported</td></tr><tr class="row"><td nowrap><b>getOverrideStyle() </b></td><td align="center">unsupported</td></tr><tr class="row"><td nowrap><b>getPropertyPriority()</b></td><td align="center">unsupported</td></tr></table></td></tr></table>',
	'<table border="0" cellspacing="0" cellpadding="0" bgcolor="#4682B4"><tr><td><table border="0" cellspacing="1" cellpadding="3"><tr class="row"><td nowrap><b>createCSS StyleSheet() </b></td><td align="center">unsupported</td></tr><tr class="row"><td nowrap><b>createStyleSheet() </b></td><td align="center">supported</td></tr><tr class="row"><td nowrap><b>getOverrideStyle() </b></td><td align="center">unsupported</td></tr><tr class="row"><td nowrap><b>getPropertyPriority()</b></td><td align="center">supported</td></tr></table></td></tr></table>',
	'<table border="0" cellspacing="0" cellpadding="0" bgcolor="#4682B4"><tr><td><table border="0" cellspacing="1" cellpadding="3"><tr class="row"><td nowrap><b>createCSS StyleSheet() </b></td><td align="center">unsupported</td></tr><tr class="row"><td nowrap><b>createStyleSheet() </b></td><td align="center">unsupported</td></tr><tr class="row"><td nowrap><b>getOverrideStyle() </b></td><td align="center">unsupported</td></tr><tr class="row"><td nowrap><b>getPropertyPriority()</b></td><td align="center">supported</td></tr></table></td></tr></table>',
	'Softcomplex Inc. is rapidly growing<br>US/Ukrainian company providing high quality <br>software development and IT consulting services.',
	wrap("Tigra Hints Documentation", true)
];

var myHint = new THints (HINTS_CFG, HINTS_ITEMS);

function wrap (s_, b_ques) {
	return "<table cellpadding='0' cellspacing='0' border='0' style='-moz-opacity:90%;filter:progid:DXImageTransform.Microsoft.dropShadow(Color=#777777,offX=4,offY=4)'><tr><td rowspan='2'><img src='img/1"+(b_ques?"q":"")+".gif'></td><td><img src='/img/pixel.gif' width='1' height='15'></td></tr><tr><td background='img/2.gif' height='28' nowrap>"+s_+"</td><td><img src='img/4.gif'></td></tr></table>"
}

function wrap_img (s_file, s_title) {
	return "<table cellpadding=5 bgcolor=white style='border:1px solid #777777'><tr><td><img src='img/k0"+s_file+".jpg' class='picI'></td></tr><tr><td align=center>"+s_title+"</td></tr></table>"
}
