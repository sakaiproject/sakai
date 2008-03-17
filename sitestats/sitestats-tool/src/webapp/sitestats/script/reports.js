function checkWhatSelection() {
	var whatSelects = document.getElementsByName('reportsForm:what');
    var visitsRadio = whatSelects[0];
    var toolsRadio = whatSelects[1];
    var resourcesRadio = whatSelects[2];
	var whatToolsRadios = document.getElementById('reportsForm:what-events-by-selectionRadio');
    var selectTools = document.getElementById('reportsForm:what-tools-select');
	var selectEvents = document.getElementById('reportsForm:what-events-select');
	var selectResourcesContainer = document.getElementById('reportsForm:what-resources-select-container');
	var selectResources = document.getElementById('reportsForm:what-resources-select');
	var selectResourcesAction = document.getElementById('reportsForm:what-resourcesAction');
	var checkResources = document.getElementById('reportsForm:what-resources-check');
	var panelResources1 = document.getElementById('reportsForm:what-resourcesPanel1');
	var panelResources2 = document.getElementById('reportsForm:what-resourcesPanel2');
	var checkResourcesAction = document.getElementById('reportsForm:what-resources-action-check');
	if(visitsRadio.checked){
		whatToolsRadios.style.display = 'none';
		selectTools.style.display = 'none';
		selectEvents.style.display = 'none';
		panelResources1.style.display = 'none';
		panelResources2.style.display = 'none';
		selectResourcesAction.style.display = 'none';
		selectResourcesContainer.style.display = 'none';
	}else if(toolsRadio.checked){
		whatToolsRadios.style.display = 'block';
		var whatToolsSelects = document.getElementsByName('reportsForm:what-events-by');
    	var toolsByToolRadio = whatToolsSelects[0];
    	var toolsByEventRadio = whatToolsSelects[1];
    	if(toolsByToolRadio.checked){
    		selectTools.style.display = 'block';
			selectEvents.style.display = 'none';
    	}else{
    		selectTools.style.display = 'none';
			selectEvents.style.display = 'block';
    	}		
		panelResources1.style.display = 'none';
		panelResources2.style.display = 'none';
		selectResourcesAction.style.display = 'none';
		selectResourcesContainer.style.display = 'none';
		setMainFrameHeightNoScroll(window.name);
	}else{
		whatToolsRadios.style.display = 'none';
		selectTools.style.display = 'none';
		selectEvents.style.display = 'none';
		panelResources1.style.display = 'block';
		panelResources2.style.display = 'block';
		selectResourcesAction.style.display = 'block';
		selectResourcesContainer.style.display = 'block';
		if(checkResources.checked){
			selectResources.style.width = '';
        }
		selectResources.disabled = !checkResources.checked;
		selectResourcesAction.disabled = !checkResourcesAction.checked;
		resourcesSelectOnDivScroll();
		setMainFrameHeightNoScroll(window.name);
	}
}

function checkWhenSelection() {
	var whenSelects = document.getElementsByName('reportsForm:when');
    var timeSelection = whenSelects[2];
	if(timeSelection.checked){
		document.getElementById('reportsForm:when-customPanel').style.display = 'block';
		setMainFrameHeightNoScroll(window.name);
	}else{
		document.getElementById('reportsForm:when-customPanel').style.display = 'none';
	}	
}

function checkWhoSelection() {
	var whoSelects = document.getElementsByName('reportsForm:who');
	var roles = whoSelects[1];
	if(roles.checked){
		document.getElementById('reportsForm:who-role-select').style.display = 'block';
	}else{
		document.getElementById('reportsForm:who-role-select').style.display = 'none';
	}
	var groups = whoSelects[2];
	if(groups.checked){
		document.getElementById('reportsForm:who-groups-select').style.display = 'block';
	}else{
		document.getElementById('reportsForm:who-groups-select').style.display = 'none';
	}
	var users = whoSelects[3];
	if(users.checked){
		document.getElementById('reportsForm:who-custom-select').style.display = 'block';
	}else{
		document.getElementById('reportsForm:who-custom-select').style.display = 'none';
	}
}

// Resources select related functions
function resourcesSelectOnDivScroll() {
	var selectResources = document.getElementById("reportsForm:what-resources-select");

    //The following two points achieves two things while scrolling
    //a) On horizontal scrolling: To avoid vertical
    //   scroll bar in select box when the size of 
    //   the selectbox is 8 and the count of items
    //   in selectbox is greater than 8.
    //b) On vertical scrolling: To view all the items in selectbox

    //Check if items in selectbox is greater than 8, 
    //if so then making the size of the selectbox to count of
    //items in selectbox,so that vertival scrollbar
    // won't appear in selectbox
    if (selectResources.options.length > 8){
        selectResources.size=selectResources.options.length;
    }else{
        selectResources.size=8;
    }
}

function resourcesSelectOnFocus() {
	//On focus of Selectbox, making scroll position 
    //of DIV to very left i.e 0 if it is not. The reason behind
    //is, in this scenario we are fixing the size of Selectbox 
    //to 8 and if the size of items in Selecbox is greater than 8 
    //and to implement downarrow key and uparrow key 
    //functionality, the vertical scrollbar in selectbox will
    //be visible if the horizontal scrollbar of DIV is exremely right.
    if (document.getElementById("reportsForm:what-resources-select-container").scrollLeft != 0){
        document.getElementById("reportsForm:what-resources-select-container").scrollLeft = 0;
    }

    var selectResources = document.getElementById('reportsForm:what-resources-select');
    //Checks if count of items in Selectbox is greater 
    //than 8, if yes then making the size of the selectbox to 8.
    //So that on pressing of downarrow key or uparrowkey, 
    //the selected item should also scroll up or down as expected.
    if( selectResources.options.length > 8){
        selectResources.focus();
        selectResources.size=8;
    }
}
