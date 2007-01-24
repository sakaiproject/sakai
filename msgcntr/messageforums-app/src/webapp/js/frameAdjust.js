function adjustMainFrameHeight(id)
{

	// run the script only if this window's name matches the id parameter
	// this tells us that the iframe in parent by the name of 'id' is the one who spawned us
	if (typeof window.name != "undefined" && id != window.name) return;

	//calculate the height of the longest div

    var maxHeight = maxDivHeight();
	//alert(maxHeight);

	var frame = parent.document.getElementById(id);
	if (frame)
	{
		// reset the scroll
		parent.window.scrollTo(0,0);

		var objToResize = (frame.style) ? frame.style : frame;
		var height;

		var scrollH = document.body.scrollHeight;
		var offsetH = document.body.offsetHeight;
		var clientH = document.body.clientHeight;
		var innerDocScrollH = null;

		if (typeof(frame.contentDocument) != 'undefined' || typeof(frame.contentWindow) != 'undefined')
		{
			// very special way to get the height from IE on Windows!
			// note that the above special way of testing for undefined variables is necessary for older browsers
			// (IE 5.5 Mac) to not choke on the undefined variables.
 			var innerDoc = (frame.contentDocument) ? frame.contentDocument : frame.contentWindow.document;
			innerDocScrollH = (innerDoc != null) ? innerDoc.body.scrollHeight : null;
		}


		if (document.all && innerDocScrollH != null)
		{
			// IE on Windows only
			height = innerDocScrollH;
		}
		else
		{
			// every other browser!
			height = offsetH;
		}

		// here we fudge to get a little bigger
		//gsilver: changing this from 50 to 10, and adding extra bottom padding to the portletBody
		var newHeight = height + 10 + maxHeight;

		// no need to be smaller than...
		//if (height < 200) height = 200;
		objToResize.height=newHeight + "px";


		var s = " scrollH: " + scrollH + " offsetH: " + offsetH + " clientH: " + clientH + " innerDocScrollH: " + innerDocScrollH + " Read height: " + height + " Set height to: " + newHeight;
		window.status = s;
	}

}

function maxDivHeight(){

     var divs,contDivs,maxHeight,divHeight,d;

     // get all <div> elements in the document

     divs=document.getElementsByTagName('div');
     contDivs=[];
     // initialize maximum height value
     maxHeight=0;

     // iterate over all <div> elements in the document
     for(var i=0;i<divs.length;i++){
          // make collection with <div> elements with class attribute 'container'
          if(/\bdhtmlPopup\b/.test(divs[i].className)){

                d=divs[i];
                contDivs[contDivs.length]=d;
                // determine height for <div> element

                if(d.offsetHeight){
                     divHeight=d.offsetHeight;
                }
                else if(d.style.pixelHeight){
                     divHeight=d.style.pixelHeight;
                }
                // calculate maximum height
                maxHeight=Math.max(maxHeight,divHeight);
          }
 }
  return maxHeight;
}
