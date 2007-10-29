// Include this in test pages so that ?test-bidi appended to the URL will result in a page rendered right-to-left
// to test bi-directional support for middle eastern languages.

if (window.location.href.indexOf("test-bidi") > -1) {
	document.getElementsByTagName("html")[0].dir = "rtl";
}
