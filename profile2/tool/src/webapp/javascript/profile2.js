	
/* get how far the window has been scrolled down the page */
function getScroll() {
	if (document.all) {
        // We are In MSIE.
        return top.document.documentElement.scrollTop;
    } else {
        // In Firefox
        return top.pageYOffset;
    } 
}

/* fix vertical issue with Wicket Modal window in an iframe. puts it 50px below top of viewport rather than vertically centered. */
function fixWindowVertical() { 
	var myWindow=Wicket.Window.get(); 
	if(myWindow) {
		var top = getScroll() + 50; 
		myWindow.window.style.top = top + "px";
	}
	return false;
} 
	
	
	
