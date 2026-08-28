var orgApacheMyfacesPopupCurrentlyOpenedPopup;
var orgApacheMyfacesPopupFrameUnder;

/**
* fix for the div over control bug in ie
*/
function orgApacheMyfacesPopupfixIE() {
    if(document.all) {
    	if(orgApacheMyfacesPopupCurrentlyOpenedPopup == null) return false;
    	var iframe = document.getElementById(orgApacheMyfacesPopupCurrentlyOpenedPopup.id+"_IFRAME");


		if(iframe == null) {
			orgApacheMyfacesPopupFrameUnder = document.createElement("<iframe src='javascript:false;' id='"+orgApacheMyfacesPopupCurrentlyOpenedPopup.id+"_IFRAME' style='visibility:hidden; position: absolute; top:0px;left:0px; filter:alpha(Opacity=0);' frameborder='0' scroll='none' />");
	   		document.body.insertBefore(orgApacheMyfacesPopupFrameUnder);
   		} else {
   			orgApacheMyfacesPopupFrameUnder = iframe;
   		}

		var popup  = orgApacheMyfacesPopupCurrentlyOpenedPopup;
   		iframe = orgApacheMyfacesPopupFrameUnder;

   		if(popup != null &&
   			(popup.style.display == "block")) {

			popup.style.zIndex	= 99;
   			iframe.style.zIndex = popup.style.zIndex - 1;
   			iframe.style.width 	= popup.offsetWidth;
	    	iframe.style.height = popup.offsetHeight;
	    	iframe.style.top 	= popup.style.top;
    		iframe.style.left 	= popup.style.left;
    		
    		iframe.style.marginTop 		= popup.style.marginTop;
    		iframe.style.marginLeft 	= popup.style.marginLeft;
    		iframe.style.marginRight 	= popup.style.marginRight;
    		iframe.style.marginBottem 	= popup.style.marginBottom;
    		
			iframe.style.display = "block";
			iframe.style.visibility = "visible"; /*we have to set an explicit visible otherwise it wont work*/

   		} else {
   			iframe.style.display = "none";
   		}
    }
    return false;
}


function orgApacheMyfacesPopup(popupId,displayAtDistanceX,displayAtDistanceY)
{
    this.popupId = popupId;
    this.displayAtDistanceX=displayAtDistanceX;
    this.displayAtDistanceY=displayAtDistanceY;
    this.display = orgApacheMyfacesPopupDisplay;
    this.hide = orgApacheMyfacesPopupHide;
    this.redisplay=orgApacheMyfacesPopupRedisplay;
}

function orgApacheMyfacesPopupDisplay(ev)
{

    if(orgApacheMyfacesPopupCurrentlyOpenedPopup!=null)
        orgApacheMyfacesPopupCurrentlyOpenedPopup.style.display="none";

    var elem;
    var x;
    var y;

    if(document.all) /*ie browser detection already in place*/
    {
		if(orgApacheMyfacesPopupFrameUnder!=null)
			orgApacheMyfacesPopupFrameUnder.style.display="none";

        elem = window.event.srcElement;
        x=window.event.clientX;
        x+=orgApacheMyfacesPopupGetScrollingX();
        y=window.event.clientY;
        y+=orgApacheMyfacesPopupGetScrollingY();
    }
    else
    {
        elem = ev.target;
        x=ev.pageX;
        y=ev.pageY;
    }

    x+=this.displayAtDistanceX;
    y+=this.displayAtDistanceY;

    var popupElem = document.getElementById(this.popupId);

    if(popupElem.style.display!="block")
    {
        popupElem.style.display="block";
        popupElem.style.left=""+x+"px";
        popupElem.style.top=""+y+"px";
        orgApacheMyfacesPopupCurrentlyOpenedPopup = popupElem;
    }
    orgApacheMyfacesPopupfixIE();
}


/**
* hide function which
* hides the popup from the browser
*/
function orgApacheMyfacesPopupHide()
{
    var popupElem = document.getElementById(this.popupId);
    popupElem.style.display="none";

	if(document.all && (orgApacheMyfacesPopupFrameUnder != null)) { /*ie specific popup under fix*/
		orgApacheMyfacesPopupFrameUnder.style.display = "none";
	}
	orgApacheMyfacesPopupfixIE();
}

/**
* simple redisplay
* displays an already existing hidden popup
*/
function orgApacheMyfacesPopupRedisplay()
{
    var popupElem = document.getElementById(this.popupId);
    popupElem.style.display="block";
    orgApacheMyfacesPopupCurrentlyOpenedPopup = popupElem;
	orgApacheMyfacesPopupfixIE();
}

function orgApacheMyfacesPopupGetScrollingX() {
    if (self.pageXOffset) {
        return self.pageXOffset;
    } else if (document.documentElement && document.documentElement.scrollLeft) {
        return document.documentElement.scrollLeft;
    } else if (document.body) {
        return document.body.scrollLeft;
    } else {
        return 0;
    }
}

function orgApacheMyfacesPopupGetScrollingY() {
    if (self.pageYOffset) {
        return self.pageYOffset;
    } else if (document.documentElement && document.documentElement.scrollTop) {
        return document.documentElement.scrollTop;
    } else if (document.body) {
        return document.body.scrollTop;
    } else {
        return 0;
    }
}