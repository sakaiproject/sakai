/* Propogate StyleAble styles into frames */

function insertStyleAbleStyles() {
	var ua = window.navigator.userAgent;
	var msie = ua.indexOf ( "MSIE " );
	if ( msie > 0 )	{
		insertStyleAbleStylesIE();
	} else {
		insertStyleAbleStylesW3C();
	}
}

/* Begin IE */

function insertStyleAbleStylesIE() {
	var styleSheets = document.styleSheets;
	var styleSheet;
	for (var i=0; i < styleSheets.length; i++) {
		if (styleSheets[i].title == "StyleAble") {
			styleSheet = styleSheets[i];
			break;
		}
	}
	if (styleSheet) {
		insertStylesIE(window.frames, styleSheet.cssText);
	}
}

function insertStylesIE(frames, styles) {
	if (frames) {
		for (var i=0; i < frames.length; i++) { 
			var doc = frames[i].document;
			var stylesheet = doc.createStyleSheet();
			stylesheet.cssText = styles;
			insertStylesIE(frames[i].frames, styles);
		}
	}
}

/* End IE */

/* Begin W3C */

function insertStyleAbleStylesW3C() {
	var styleElements = document.getElementsByTagName("style");
	var styleElement;
	for (var i=0; i < styleElements.length; i++) {
		if (styleElements[i].title == "StyleAble") {
			styleElement = styleElements[i];
			break;
		}
	}
	if (styleElement) {
		insertStylesW3C(window.frames, getText(styleElement));
	}
}

function getText(element) {
	var text = "";
	var childNodes = element.childNodes;
	for (var i = 0; i < childNodes.length; i++) {
		if (childNodes[i].nodeType == Node.TEXT_NODE) {
			text = text + childNodes[i].nodeValue;
		}
	}
	return text;
}

function insertStylesW3C(frames, styles) {
	if (frames) {
		for (var i=0; i < frames.length; i++) { 
			var doc = frames[i].document;
			var heads = doc.getElementsByTagName("head");
			if (heads.length > 0) {
				var head = heads[0];
				head.appendChild(createStyleElement(doc, styles));
			}
			insertStylesW3C(frames[i].frames, styles);
		}
	}
}

function createStyleElement(doc, styles) {
	var styleElement = doc.createElement("style");
	styleElement.setAttribute("type", "text/css");
	var textNode = doc.createTextNode(styles);
	styleElement.appendChild(textNode);
	return styleElement;
}

/* End W3C */
