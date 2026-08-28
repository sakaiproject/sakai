function myFaces_showPanelTab(
	tabIndex, tabIndexSubmitFieldID,
	headerId, paneId,
	allHeaderCellsIDs, allPanesIDs,
	activeHeaderStyleClass, inactiveHeaderStyleClass,
	activeSubHeaderStyleClass, inactiveSubHeaderStyleClass){

	if( ! document.getElementById ) // Too Old Browser. Fallback on server side switch
		return true;
		
	document.getElementById(tabIndexSubmitFieldID).value = tabIndex;

	// Change Headers styles
	for(var i = 0; i < allHeaderCellsIDs.length; i++){
		var styleClass;
		if( headerId == allHeaderCellsIDs[i] ){
			styleClass = "myFaces_panelTabbedPane_activeHeaderCell";
			if( activeHeaderStyleClass != null )
				styleClass += " "+activeHeaderStyleClass;
		}else{
			styleClass = "myFaces_panelTabbedPane_inactiveHeaderCell";
			if( inactiveHeaderStyleClass != null )
				styleClass += " "+inactiveHeaderStyleClass
		}
		
		var headerCell = document.getElementById(allHeaderCellsIDs[i]);
		headerCell.className = styleClass;
	}
	
	// Sub Headers
	for(var i = 0; i < allHeaderCellsIDs.length; i++){
		var styleClasses = "myFaces_panelTabbedPane_subHeaderCell";
		if( i == 0 )
			styleClasses += " myFaces_panelTabbedPane_subHeaderCell_first";
		if( headerId == allHeaderCellsIDs[i] ){
			styleClasses += " myFaces_panelTabbedPane_subHeaderCell_active";
			if( activeSubHeaderStyleClass != null )
				styleClasses += " "+activeSubHeaderStyleClass;
		}else{
			styleClasses += " myFaces_panelTabbedPane_subHeaderCell_inactive";
			if( inactiveHeaderStyleClass != null )
				styleClasses += " "+inactiveSubHeaderStyleClass;
		}
		
		var subHeaderCell = document.getElementById(allHeaderCellsIDs[i]+"_sub");
		subHeaderCell.className = styleClasses;
	}
	
	// Switch pane content
	for(var i = 0; i < allPanesIDs.length; i++){
		document.getElementById(allPanesIDs[i]).style.display = (paneId == allPanesIDs[i]) ? 'block':'none';
	}

	return false;
}