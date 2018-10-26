function setMainFrameHeightFixed(dokkument, outerid) {
    // run the script only if this window's name matches the id parameter
    // this tells us that the iframe in parent by the name of 'id' is the one
    // who spawned us
    if (typeof window.name != "undefined" && outerid != window.name)
	return;

    var frame = parent.document.getElementById(outerid);
    if (frame) {
	var objToResize = (frame.style) ? frame.style : frame;
	var newHeight = RSF.computeDocumentHeight(dokkument);
	newHeight += 20; // double Gonzalo's hallowed fudge factor from Sakai
	// headscripts
	objToResize.height = newHeight + "px";
    }
}

// Very long function name, since this can't be safely put in the RSF namespace!
function addSakaiRSFDomModifyHook(frameID) {
    if (typeof (RSF) != "undefined" && RSF.getDOMModifyFirer) {
	var firer = RSF.getDOMModifyFirer();
	firer.addListener(function() {
	    setMainFrameHeightFixed(document, frameID);
	    setFocus(focus_path);
	});
    }
}
