function setCorrespondingLevel(checkBox){
  //alert(checkBox);
  var2 = checkBox.split(":");
  selectLevel = getTheElement(var2[0]+":"+ var2[1]+":"+ var2[2]+":level");
  //alert(selectLevel);

  changeSettings=getTheElement(var2[0]+":"+ var2[1]+":"+ var2[2]+":changeSetting");
  //deletePostings=getTheElement(var2[0]+":"+ var2[1]+":"+ var2[2]+":deletePostings");
  //deleteAny=getDeleteAny(deletePostings);
  //deleteOwn=getDeleteOwn(deletePostings);
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
  //moderatePostings=getTheElement(var2[0]+":"+ var2[1]+":"+ var2[2]+":moderatePostings");

  if(selectLevel){
    if(!(changeSettings && markAsRead && newForum && newResponse && r2R && newTopic && read && revisePostings)){
      //alert(changeSettings + " " + markAsRead + " " + newForum + " " + newResponse + " " + r2R + " " + newTopic + " " + read  + " " + revisePostings);
      setIndexWithTextValue(selectLevel, custom)
    }
    else{
      //var newArray = [changeSettings.checked,deleteAny,deleteOwn,markAsRead.checked, movePosting.checked, newForum.checked, newResponse.checked, r2R.checked, newTopic.checked, postGrades.checked, read.checked, reviseAny, reviseOwn, moderatePostings.checked];
      var newArray = [changeSettings.checked,markAsRead.checked, newForum.checked, newResponse.checked, r2R.checked, newTopic.checked, postGrades.checked, read.checked, reviseAny, reviseOwn];
      //alert(newArray);
      //alert(checkLevel(newArray));
      setIndexWithTextValue(selectLevel, checkLevel(newArray));
    }

    role=getTheElement(var2[0]+":role");

    //alert(role);
    roleValue=role.options[var2[2]].value;
    //alert(roleValue);
    var lev=selectLevel.options[selectLevel.selectedIndex].text;
    //alert(lev);
    var newval=roleValue+"("+lev+")";
    //alert(newval);

    role.options[var2[2]]=new Option(newval, roleValue, true);
    role.options[var2[2]].selected=true;
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
  //deletePostings=getTheElement(var2[0]+":"+ var2[1]+":"+ var2[2]+":deletePostings");
  //deleteAny=getDeleteAny(deletePostings);
  //deleteOwn=getDeleteOwn(deletePostings);
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
  //moderatePostings=getTheElement(var2[0]+":"+ var2[1]+":"+ var2[2]+":moderatePostings");

  role=getTheElement(var2[0]+":role");
  if(selectLevel){
    if(!(changeSettings && markAsRead && newForum && newResponse && r2R && newTopic && read && revisePostings)){
      setCheckBoxes(changeSettings, markAsRead, newForum, newResponse,  r2R, newTopic, read, revisePostings, postGrades, noneLevelArray);
    }
    if(selectLevel.options[selectLevel.selectedIndex].value==owner){
      setCheckBoxes(changeSettings, markAsRead, newForum, newResponse,  r2R, newTopic, read, revisePostings, postGrades, ownerLevelArray);      
    }
    else if(selectLevel.options[selectLevel.selectedIndex].value==author){
      setCheckBoxes(changeSettings, markAsRead, newForum, newResponse,  r2R, newTopic, read, revisePostings, postGrades, authorLevelArray);      				    
    }
    else if(selectLevel.options[selectLevel.selectedIndex].value==nonEditingAuthor){
      setCheckBoxes(changeSettings, markAsRead, newForum, newResponse,  r2R, newTopic, read, revisePostings, postGrades, noneditingAuthorLevelArray);   
    }
    else if(selectLevel.options[selectLevel.selectedIndex].value==reviewer){
      setCheckBoxes(changeSettings, markAsRead, newForum, newResponse,  r2R, newTopic, read, revisePostings, postGrades, reviewerLevelArray);
    }
    else if(selectLevel.options[selectLevel.selectedIndex].value==none){
      setCheckBoxes(changeSettings, markAsRead, newForum, newResponse,  r2R, newTopic, read, revisePostings, postGrades, noneLevelArray);    
    }
    else if(selectLevel.options[selectLevel.selectedIndex].value==contributor){
      setCheckBoxes(changeSettings, markAsRead, newForum, newResponse,  r2R, newTopic, read, revisePostings, postGrades, contributorLevelArray);
    }

    roleValue=role.options[var2[2]].value;
    var lev=selectLevel.options[selectLevel.selectedIndex].text;
    var newval=roleValue+"("+lev+")";
    role.options[var2[2]]=new Option(newval, roleValue, true);
    role.options[var2[2]].selected=true;
  }
}


function setCheckBoxes(changeSettings, markAsRead, newForum, newResponse,  r2R, newTopic, read, revisePostings, postGrades, arrayLevel){	
  changeSettings.checked= arrayLevel[0];
  //deletePostings
  //if(arrayLevel[1]==true){
  //  setRadioButtonValue(deletePostings, all);
  //}
  //else if(arrayLevel[2]==true){
  //  setRadioButtonValue(deletePostings, own);
  //}
  //else{
  //  setRadioButtonValue(deletePostings, none);
  //}


  markAsRead.checked= arrayLevel[1];
  //movePosting.checked= arrayLevel[4];
  newForum.checked= arrayLevel[2];
  newResponse.checked= arrayLevel[3];
  r2R.checked= arrayLevel[4];
  newTopic.checked= arrayLevel[5];
  postGrades.checked= arrayLevel[6];
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
  //moderatePostings.checked= arrayLevel[13];
}

function displayRelevantBlock(){
  role=getTheElement("revise:role");
  i=0;
  while(true){
    spanElement=getTheElement("revise:perm:"+i+":permissionSet");
    if(spanElement){
      rowNode = getSurroundingRowNode(spanElement);

      if (rowNode){    
        rowNode.style.display="none";
      }   
    }       
    else{
      break;
    }
    i++;
  }

  spanElement=getTheElement("revise:perm:"+ role.selectedIndex+":permissionSet");
  if(spanElement){    
    rowNode = getSurroundingRowNode(spanElement);
    if (rowNode){    
      rowNode.style.display="block";
    }    
  }
}

function getSurroundingRowNode(node){

  if (navigator.appName != "Microsoft Internet Explorer"){
    return node;    
  }
 
  while(node){
    if (node.tagName == "TR"){
      break;
    }
    node = node.parentNode;
  }    
  return node;
}
