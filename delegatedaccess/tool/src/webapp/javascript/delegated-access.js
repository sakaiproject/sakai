function popupWindow(url, blockMessage){
	var popup = window.open(url,"_blank");
	setTimeout( function() {
	    if(!popup || popup.outerHeight === 0) {
	        //First Checking Condition Works For IE & Firefox
	        //Second Checking Condition Works For Chrome
	        alert(blockMessage);
	    } 
	}, 500);
}