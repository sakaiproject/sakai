function setCorrespondingLevel(checkBox){
  //alert(checkBox);
  var2 = checkBox.split(":");
  selectLevel = getTheElement(var2[0]+":"+ var2[1]+":"+ var2[2]+":level");
  //alert(selectLevel);

  changeSettings=getTheElement(var2[0]+":"+ var2[1]+":"+ var2[2]+":changeSetting");
  deletePostings=getTheElement(var2[0]+":"+ var2[1]+":"+ var2[2]+":deletePostings");
  deleteAny=getDeleteAny(deletePostings);
  deleteOwn=getDeleteOwn(deletePostings);
  markAsRead=getTheElement(var2[0]+":"+ var2[1]+":"+ var2[2]+":markAsRead");
  //movePosting=getTheElement(var2[0]+":"+ var2[1]+":"+ var2[2]+":movePosting");
  newForum=getTheElement(var2[0]+":"+ var2[1]+":"+ var2[2]+":newForum");
  newResponse=getTheElement(var2[0]+":"+ var2[1]+":"+ var2[2]+":newR");
  r2R=getTheElement(var2[0]+":"+ var2[1]+":"+ var2[2]+":newRtoR");
  newTopic=getTheElement(var2[0]+":"+ var2[1]+":"+ var2[2]+":newTopic");
  
  postGrades=getTheElement(var2[0]+":"+ var2[1]+":"+ var2[2]+":postGrades");
  // may not have a gradebook so checkbox may not be on page
  // if it is, get its value, if not, set it to false
  var postGradesChecked = (postGrades) ? postGrades.checked : false;
      
  read=getTheElement(var2[0]+":"+ var2[1]+":"+ var2[2]+":read");
  revisePostings=getTheElement(var2[0]+":"+ var2[1]+":"+ var2[2]+":revisePostings")
  reviseAny=getReviseAny(revisePostings);
  reviseOwn= getReviseOwn(revisePostings);
  moderatePostings=getTheElement(var2[0]+":"+ var2[1]+":"+ var2[2]+":moderatePostings");
  identifyAnonAuthors=getTheElement(var2[0]+":"+ var2[1]+":"+ var2[2]+":identifyAnonAuthors");

  if(selectLevel){
    if(!(changeSettings && markAsRead && newForum && newResponse && r2R && newTopic && read && revisePostings && moderatePostings && identifyAnonAuthors && deletePostings)){
      //alert(changeSettings + " " + markAsRead + " " + newForum + " " + newResponse + " " + r2R + " " + newTopic + " " + read  + " " + revisePostings);
      setIndexWithTextValue(selectLevel, custom)
    }
    else{
      //var newArray = [changeSettings.checked,deleteAny,deleteOwn,markAsRead.checked, movePosting.checked, newForum.checked, newResponse.checked, r2R.checked, newTopic.checked, postGrades.checked, read.checked, reviseAny, reviseOwn, moderatePostings.checked];
      var newArray = [changeSettings.checked, markAsRead.checked, newForum.checked, newResponse.checked, r2R.checked, newTopic.checked, postGradesChecked, read.checked, reviseAny, reviseOwn, moderatePostings.checked, identifyAnonAuthors.checked, deleteAny, deleteOwn];
      //alert(newArray);
      //alert(checkLevel(newArray));
      setIndexWithTextValue(selectLevel, checkLevel(newArray));
    }
  }
}

function setIndexWithTextValue(element, textValue){
  for (i=0;i<element.length;i++){
    if (element.options[i].value==textValue){
      element.selectedIndex=i;
	}
  }
}

function getReviseAny(element){
  if(!element){
    //alert("getReviseAny: Returning");
	return false;
  }
  var user_input =  getRadioButtonCheckedValue(element);
  //alert(user_input);
  if(user_input==all)
    return true;
  else
    return false;
}

function getReviseOwn(element){
  if(!element){
    return false;
  }
  var user_input =  getRadioButtonCheckedValue(element);
  //if(user_input==all)
  //  return true;
  if(user_input==own)
    return true;
  else
    return false;
}

function getDeleteAny(element){
  if(!element){
    return false;
  }
  var user_input =  getRadioButtonCheckedValue(element);
  if(user_input==all)
    return true;
  else
    return false;
}

function getDeleteOwn(element){
  if(!element)
    return false;
    
  var user_input =  getRadioButtonCheckedValue(element);
  //if(user_input==all)
  //  return true;

  if(user_input==own)
    return true;
  else
    return false;
}

function getRadioButtonCheckedValue(element){
  var user_input=none;
  //alert(element.length+element.id);
  var inputs = element.getElementsByTagName ('input');
  for (i=0;i<inputs.length;i++){
    //alert(inputs[i].value+inputs.length+inputs.id);
    if (inputs[i].checked==true){
      user_input = inputs[i].value;
    }
  }
  //alert("Radio checked :"+user_input );
  return user_input;
}

function setRadioButtonValue(element, newValue){
  var inputs = element.getElementsByTagName ('input');
  for (i=0;i<inputs.length;i++){
    if (inputs[i].value==newValue){
      inputs[i].checked=true;
    }
  }
}

function setCorrespondingCheckboxes(checkBox){
  var2 = checkBox.split(":");
  selectLevel = getTheElement(var2[0]+":"+ var2[1]+":"+ var2[2]+":level");

  changeSettings=getTheElement(var2[0]+":"+ var2[1]+":"+ var2[2]+":changeSetting");
  deletePostings=getTheElement(var2[0]+":"+ var2[1]+":"+ var2[2]+":deletePostings");
  deleteAny=getDeleteAny(deletePostings);
  deleteOwn=getDeleteOwn(deletePostings);
  markAsRead=getTheElement(var2[0]+":"+ var2[1]+":"+ var2[2]+":markAsRead");
  //movePosting=getTheElement(var2[0]+":"+ var2[1]+":"+ var2[2]+":movePosting");
  newForum=getTheElement(var2[0]+":"+ var2[1]+":"+ var2[2]+":newForum");
  newResponse=getTheElement(var2[0]+":"+ var2[1]+":"+ var2[2]+":newR");
  r2R=getTheElement(var2[0]+":"+ var2[1]+":"+ var2[2]+":newRtoR");
  newTopic=getTheElement(var2[0]+":"+ var2[1]+":"+ var2[2]+":newTopic");
  postGrades=getTheElement(var2[0]+":"+ var2[1]+":"+ var2[2]+":postGrades");
  read=getTheElement(var2[0]+":"+ var2[1]+":"+ var2[2]+":read");
  revisePostings=getTheElement(var2[0]+":"+ var2[1]+":"+ var2[2]+":revisePostings")
  reviseAny=getReviseAny(revisePostings);
  reviseOwn= getReviseOwn(revisePostings);
  moderatePostings=getTheElement(var2[0]+":"+ var2[1]+":"+ var2[2]+":moderatePostings");
  identifyAnonAuthors=getTheElement(var2[0]+":"+ var2[1]+":"+ var2[2]+":identifyAnonAuthors");

  role=getTheElement(var2[0]+":role");
  if(selectLevel){
    if(!(changeSettings && markAsRead && newForum && newResponse && r2R && newTopic && read && revisePostings && moderatePostings && identifyAnonAuthors && deletePostings)){
      setCheckBoxes(changeSettings, markAsRead, newForum, newResponse,  r2R, newTopic, read, revisePostings, postGrades, moderatePostings, identifyAnonAuthors, deletePostings, noneLevelArray);
    }
    if(selectLevel.options[selectLevel.selectedIndex].value==owner){
      setCheckBoxes(changeSettings, markAsRead, newForum, newResponse,  r2R, newTopic, read, revisePostings, postGrades, moderatePostings, identifyAnonAuthors, deletePostings, ownerLevelArray);      
    }
    else if(selectLevel.options[selectLevel.selectedIndex].value==author){
      setCheckBoxes(changeSettings, markAsRead, newForum, newResponse,  r2R, newTopic, read, revisePostings, postGrades, moderatePostings, identifyAnonAuthors, deletePostings, authorLevelArray);      				    
    }
    else if(selectLevel.options[selectLevel.selectedIndex].value==nonEditingAuthor){
      setCheckBoxes(changeSettings, markAsRead, newForum, newResponse,  r2R, newTopic, read, revisePostings, postGrades, moderatePostings, identifyAnonAuthors, deletePostings, noneditingAuthorLevelArray);   
    }
    else if(selectLevel.options[selectLevel.selectedIndex].value==reviewer){
      setCheckBoxes(changeSettings, markAsRead, newForum, newResponse,  r2R, newTopic, read, revisePostings, postGrades, moderatePostings, identifyAnonAuthors, deletePostings, reviewerLevelArray);
    }
    else if(selectLevel.options[selectLevel.selectedIndex].value==none){
      setCheckBoxes(changeSettings, markAsRead, newForum, newResponse,  r2R, newTopic, read, revisePostings, postGrades, moderatePostings, identifyAnonAuthors, deletePostings, noneLevelArray);    
    }
    else if(selectLevel.options[selectLevel.selectedIndex].value==contributor){
      setCheckBoxes(changeSettings, markAsRead, newForum, newResponse,  r2R, newTopic, read, revisePostings, postGrades, moderatePostings, identifyAnonAuthors, deletePostings, contributorLevelArray);
    }
	else if (selectLevel.options[selectLevel.selectedIndex].value==custom){
		// if set to custom, pop open the custom settings panel
		if (document.getElementById(var2[0]+":"+ var2[1]+":"+ var2[2] + ":permissionSet").parentNode.style.display == "none") {
			$(document.getElementById("revise:perm")).accordion("activate", parseInt(var2[2]));
		}
	}
  }
}


function setCheckBoxes(changeSettings, markAsRead, newForum, newResponse,  r2R, newTopic, read, revisePostings, postGrades, moderatePostings, identifyAnonAuthors, deletePostings, arrayLevel){	
  changeSettings.checked= arrayLevel[0];

  markAsRead.checked= arrayLevel[1];
  //movePosting.checked= arrayLevel[4];
  newForum.checked= arrayLevel[2];
  newResponse.checked= arrayLevel[3];
  r2R.checked= arrayLevel[4];
  newTopic.checked= arrayLevel[5];
  if (postGrades) postGrades.checked= arrayLevel[6];
  read.checked= arrayLevel[7];
  //revisePostings,
  if(arrayLevel[8]==true){
    setRadioButtonValue(revisePostings, all);
  }
  else if(arrayLevel[9]==true){
    setRadioButtonValue(revisePostings, own);
  }
  else{
    setRadioButtonValue(revisePostings, none);
  }
  moderatePostings.checked= arrayLevel[10];

  identifyAnonAuthors.checked= arrayLevel[11];
  
  //deletePostings
  if(arrayLevel[12]==true){
    setRadioButtonValue(deletePostings, all);
  }
  else if(arrayLevel[13]==true){
    setRadioButtonValue(deletePostings, own);
  }
  else{
    setRadioButtonValue(deletePostings, none);
  }
  
}
$(function(){
    $(document.getElementById("revise:perm")).accordion({
        header: '.permissionCustomize',
        active: false,
        collapsible: true,
        heightStyle: "content",
        change: function(event, ui){
            resizeFrame('grow');
        } 
    });
});


function disableOrEnableModeratePerm() {
	moderateSelection = getTheElement("revise:moderated");
	user_input = getRadioButtonCheckedValue(moderateSelection);		
	
	if (user_input) {
		var i = 0;
		while (true) {
			moderatePostings = getTheElement("revise:perm:" + i + ":moderatePostings");
			if (moderatePostings) {
				if (user_input == "true") {
					// if the user has enabled moderating, we need to enable the moderate perm checkbox
					moderatePostings.disabled = false;
				}
				else {
					// if it is disabled, disable the checkbox
					moderatePostings.disabled = true;
				}
			}
			else {
				break;
			}
			i++;
		}
	}
}
