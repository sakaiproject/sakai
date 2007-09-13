/*
 * Rollover logic for javascript
 */
var activeSrcList = new Array();
var inactiveSrcList = new Array();
 
function activateBtn(btnId)
{
	var imgsrc = activeSrcList[btnId];

	var btn = document.getElementById(btnId);

	if (btn) {
		if (imgsrc) {
			btn.src = imgsrc.src;
		}
	}
}

function inactivateBtn(btnId)
{
	var imgsrc = inactiveSrcList[btnId];

	var btn = document.getElementById(btnId);

	if (btn) {
		if (imgsrc) {
			btn.src = imgsrc.src;
		}
	}
}
