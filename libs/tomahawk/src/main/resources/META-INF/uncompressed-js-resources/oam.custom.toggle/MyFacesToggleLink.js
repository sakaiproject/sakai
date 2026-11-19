function MyFacesToggleLinkUtils() {}
	
MyFacesToggleLinkUtils.scrollTo = function(id){
	var theelement = document.getElementById(id);
	if(theelement.scrollIntoView){
		theelement.scrollIntoView(true); 
	}
}

MyFacesToggleLinkUtils.toggle = function(idsToShowS,idsToHideS,toggleFlag,onClickFocusId) {
	var idsToHide = idsToHideS.split(',');
	for(var i=0;i<idsToHide.length;i++) {document.getElementById(idsToHide[i]).style.display = 'none';}
	var idsToShow = idsToShowS.split(',');
	MyFacesToggleLinkUtils.scrollTo(idsToShow[0]);
	for(var j=0;j<idsToShow.length;j++) {document.getElementById(idsToShow[j]).style.display = 'inline';}
	if(onClickFocusId != '') document.getElementById(onClickFocusId).focus();
	else document.getElementById(idsToShow[0]).focus();
	document.getElementById(toggleFlag).value = '1';
}
