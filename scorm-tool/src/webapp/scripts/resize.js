function initResizing() {
	onResize();
}

function onResize() {
	if (document.getElementById("scormContent")) {
		var windowHeight = getInnerHeight();
		var headerHeight = document.getElementById("scormButtonPanel").offsetHeight;
		var footerHeight = document.getElementById("scormFooter").offsetHeight;
		var mainHeight = windowHeight - (headerHeight + footerHeight) -4;
		
		var windowWidth = getInnerWidth();
		var navWidth = document.getElementById("scormNavPanel").offsetWidth;
		var mainWidth = windowWidth - navWidth;
	
		document.getElementById("scormNavPanel").style.height = mainHeight+"px";
		document.getElementById("scormContentPanel").style.height = mainHeight+"px";
		document.getElementById("scormContent").style.height = mainHeight+"px";
		document.getElementById("scormContent").style.width = mainWidth + "px";
	}
}

function getInnerHeight() {
	var innerHeight = 0;
	if( typeof( window.innerHeight ) == 'number' ) {
		//Non-IE
		innerHeight = window.innerHeight;
	} else if( document.documentElement && document.documentElement.clientHeight ) {
		//IE 6+ in 'standards compliant mode'
		innerHeight = document.documentElement.clientHeight;
	} else if( document.body && document.body.clientHeight ) {
		//IE 4 compatible
		innerHeight = document.body.clientHeight;
	}
	return innerHeight;
}

function getInnerWidth() {
	var innerWidth = 0;
	if( typeof( window.innerWidth ) == 'number' ) {
		//Non-IE
		innerWidth = window.innerWidth;
	} else if( document.documentElement && document.documentElement.clientWidth ) {
		//IE 6+ in 'standards compliant mode'
		innerWidth = document.documentElement.clientWidth;
	} else if( document.body && document.body.clientWidth ) {
		//IE 4 compatible
		innerWidth = document.body.clientWidth;
	}
	return innerWidth;
}