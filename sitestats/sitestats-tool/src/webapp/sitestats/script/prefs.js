function selectTool(checkObj){
	for(var i=0; i<checkObj.parentNode.childNodes.length-1; i++){
 		try{
 			if(checkObj.parentNode.childNodes[i].nodeName == 'SPAN'){
				if(checkObj.checked){
					checkObj.parentNode.childNodes[i].className= 'nodeToolSelected';
				}else{
					checkObj.parentNode.childNodes[i].className= 'nodeToolUnselected';
				}
			}
		}catch(err1){} 
	}
	// cascade (un)select
	var checkTable = checkObj.parentNode.parentNode.parentNode.parentNode;
	var fatherSpan = checkTable.parentNode;
	for(var i=0; i<fatherSpan.childNodes.length; i++){
		if(fatherSpan.childNodes[i] == checkTable){
			var childrenSpan = fatherSpan.childNodes[i+1];
			for(var j=0; j<childrenSpan.childNodes.length; j++){
				if(childrenSpan.childNodes[j].nodeName == 'TABLE'){
					var tableLine = childrenSpan.childNodes[j];
					var childCheck = tableLine.childNodes[0].childNodes[0].childNodes[2].childNodes[0];
					childCheck.checked = checkObj.checked;
					selectEvent(childCheck);
				}
			}
			break;
		}
	}
}

function selectEvent(checkObj){
	for(var i=0; i<checkObj.parentNode.childNodes.length; i++){
 		try{
 			if(checkObj.parentNode.childNodes[i].nodeName == 'SPAN'){
				if(checkObj.checked){
					checkObj.parentNode.childNodes[i].className= 'nodeEventSelected';
				}else{
					checkObj.parentNode.childNodes[i].className= 'nodeEventUnselected';
				}
			}
		}catch(err1){} 
	}
    checkEventsSelected(checkObj);
}

function checkEventsSelected(checkObj){
    var eventsSpan = checkObj.parentNode.parentNode.parentNode.parentNode.parentNode;
    var allEventCount = 0;
    var selectedEventCount = 0;
    for(var i=0; i<eventsSpan.childNodes.length; i++){
        var spanChild = eventsSpan.childNodes[i];
        if(spanChild.nodeName == 'TABLE'){
            allEventCount++;
            var childCheck = spanChild.childNodes[0].childNodes[0].childNodes[2].childNodes[0];
            if(childCheck.checked){
                selectedEventCount++;
            }
        }
    }
    //alert('#total: '+allEventCount+', #selected: '+selectedEventCount);        
	var fatherSpan = eventsSpan.parentNode;
    for(var i=0; i<fatherSpan.childNodes.length; i++){
		if(fatherSpan.childNodes[i] == eventsSpan){
			var toolCheckTable = fatherSpan.childNodes[i-1];
            var toolCheck = toolCheckTable.childNodes[0].childNodes[0].childNodes[1].childNodes[2];
            if(selectedEventCount == 0){
                toolCheck.className = 'nodeToolUnselected';
            }else if(selectedEventCount == allEventCount){
                toolCheck.className = 'nodeToolSelected';
            }else{
                toolCheck.className = 'nodeToolPartialSelected';
            }
        }
    }
}