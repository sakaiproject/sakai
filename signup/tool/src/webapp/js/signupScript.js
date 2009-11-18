	function setEndtimeMonthDateYear(){
			var year = document.getElementById("meeting:startTime.year").value;
			var month = document.getElementById("meeting:startTime.month").value;
			var day = document.getElementById("meeting:startTime.day").value;
			var diff = getSignupDateTime('endTime').getTime() - getSignupDateTime('startTime').getTime();
			
			if (diff >= 0)
				return;//don't modify
				
			document.getElementById("meeting:endTime.year").value=year;	
			document.getElementById("meeting:endTime.month").value=month;
			document.getElementById("meeting:endTime.day").value=day;
		}
	
	function getSignupDateTime(tagValue){						
			var year = document.getElementById("meeting:"+ tagValue+ ".year").value;
			var month = document.getElementById("meeting:"+ tagValue + ".month").value -1;
			var day = document.getElementById("meeting:"+ tagValue + ".day").value;
			var hours = document.getElementById("meeting:"+ tagValue + ".hours").value;
			var minutes= document.getElementById("meeting:"+ tagValue + ".minutes").value;
			var ampm = document.getElementById("meeting:"+ tagValue + ".ampm").value;
			
			var dateTime = new Date(year,month,day,hours,minutes,0,0);
			if (ampm == 1){
				if (hours =='12')
					hours = 12;
				else
					hours = dateTime.getHours() + 12;

				dateTime.setHours(hours);				
			}else if (hours == '12'){				
				dateTime.setHours(0);
			}
			
			return dateTime;			
		}	
	
	function showDetails(imageFolderName1,imagefolderName2,ContentTagName){
				var detailsTag = document.getElementById(ContentTagName);
				switchShowOrHide(detailsTag);
				
				var image1 = document.getElementById(imageFolderName1);
				switchShowOrHide(image1);
				
				var image2 = document.getElementById(imagefolderName2);
				switchShowOrHide(image2);
				
				//control multiple blocks with sequence number starting from 1 to 30 max righ Now
				var i=1;
				while (i < 30){					
						var tag = document.getElementById(ContentTagName +"_" +i)
					if (tag !=null){					
						switchShowOrHide(tag);
					}
					i++;
				}
								
	}
	
	function switchShowOrHide(tag){
		if(tag){
			if(tag.style.display=="none")
				tag.style.display="";
			else
				tag.style.display="none";
		}
	}
	
	
//common used function in step1.jsp,copyMeeting.jsp,modifyMeeting.jsp
	//var GROUP_TYPE = 'group';
	//var ANNOUNCMENT_TYPE='announcement';
	var INDIVIDUAL_TYPE = 'individual';
	var signupMeetingType=INDIVIDUAL_TYPE;
	function currentSiteSelection(){
		var siteId = "meeting:siteSelection";
		var	siteCheckBox = document.getElementById(siteId);
		enableGroupSelection(!siteCheckBox.checked);
	}
			
	function enableGroupSelection(enable){
		var groupIdPrefix = "meeting:currentSiteGroups:"
		var groupIdSuffix = ":groupSelection";
		if(!document.getElementById("meeting:currentSiteGroups"))
			return;	
		var size = document.getElementById("meeting:currentSiteGroups").tBodies[0].rows.length;
		for(var i=0; i<size; i++){
			var groupId = groupIdPrefix+i+groupIdSuffix;
			document.getElementById(groupId).disabled=!enable;
		}
	}
	
	function otherUserSitesSelection(){
		var siteId = "meeting:userSites";
		var sitePrefix = "meeting:userSites:";
		var siteSuffix = ":otherSitesSelection"
		
		var numberOfSites = document.getElementById(siteId).tBodies[0].rows.length;
		for(var i=0; i<numberOfSites; i++){
			var siteId = sitePrefix + i + siteSuffix;
			enableOtherGroupsSelection(sitePrefix + i, !document.getElementById(siteId).checked);
		}
	}
	
	function enableOtherGroupsSelection(prefixId, enable){
		var groupIdPrefix = prefixId + ":userGroups:"
		var groupIdSuffix = ":otherGroupsSelection";
			
		var size = document.getElementById(prefixId+":userGroups").tBodies[0].rows.length;
		for(var i=0; i<size; i++){
			var groupId = groupIdPrefix+i+groupIdSuffix;
			document.getElementById(groupId).disabled=!enable;
		}
	}
	
	function initGroupTypeRadioButton(){
			var groupRadiosTag = getElementsByName_iefix("input", "meeting:groupSubradio");
			if(groupRadiosTag){
				if(groupRadiosTag[0] && !groupRadiosTag[0].checked)
					switchSingle('true');
					//document.getElementById("meeting:maxAttendee").style.display='none';							
			}
	}
	
	//getElementsByName is now working for IE
	function getElementsByName_iefix(tag, name) {
     		var elem = document.getElementsByTagName(tag);
     		var arr = new Array();
     		if(!elem)
     			return;
     		for(i = 0,iarr = 0; i < elem.length; i++) {
          		att = elem[i].getAttribute("name");
          		if(att == name) {
               		arr[iarr] = elem[i];
               		iarr++;
          		}
     		}
     		return arr;
	}
	
	
	function switchSingle(a) { 	
	 		var s = document.getElementById("meeting:maxAttendee");
	 	 	
	 	 	if(!s)
	 	 		return;
	 	 		
	 		if(a=='true')
	 			s.style.display="none";						
	 		else if (a=='false') { 	
	 			s.style.display="";					
	 		}
	 		
	 		if(document.getElementById("meeting:maxAttendee").value > 500)
                	document.getElementById("meeting:maxAttendee").value="10"; //default value              
              
	 	}
	 	
 	function showSignupBeginDeadline(isShow){
 			var i=1;
			while (document.getElementById("meeting:signup_beginDeadline"+"_" + i)!=null){					
				var tag = document.getElementById("meeting:signup_beginDeadline"+"_" +i)
				if (tag !=null){					
					if (isShow=='yes')
						tag.style.display = "";
					else
						tag.style.display = "none";
				}
				i++;
			}
 		}
 		
			
		function validateMeetingType() {
				//just for groupType value
				if(document.getElementById("meeting:maxAttendee").value > 500)
                	document.getElementById("meeting:maxAttendee").value="10"; //default value                             
        }
    
        
        function isShowCalendar(value){
        	var untilCalendarTag = document.getElementById('meeting:utilCalendar');
        	if(value == 'no_repeat')
        		untilCalendarTag.style.display = "none";
        	else
        		untilCalendarTag.style.display = "";
        } 
  
		var prev_participants =10;//default   
		function validateParticipants() {
  			var participants = document.getElementById("meeting:maxAttendee");   
 			prev_participants = signup_ValidateNumber(prev_participants,participants ,500);			
  		}            
  
		var prev_attendeeNum=1;//default
 		function validateAttendee() {
  			var attendeeNumTag = document.getElementById("meeting:numberOfAttendees");
  			prev_attendeeNum = signup_ValidateNumber(prev_attendeeNum,attendeeNumTag,100);   
  		}            
  

		function signup_ValidateNumber(prev_ValueHolder,elementTag,maxNumAllowed){
				if (elementTag.value.length < 1)
					return prev_ValueHolder;
				else{ if (isNaN(elementTag.value)){
		                 elementTag.value=prev_ValueHolder;
		                 return prev_ValueHolder;
		          	}
		 		else if (maxNumAllowed && (elementTag.value > maxNumAllowed || elementTag.value < 1 )) {
		       			alert("The Number is out of the range from 1" + " to " + maxNumAllowed + ".");
		       			elementTag.value=prev_ValueHolder;
		   	   		}
					else
						prev_ValueHolder= elementTag.value;
		
				}
				
				return prev_ValueHolder;
		
		}
           
		var prev_slotNum=4;//default
		function getSignup_Duration(showDecimal){
					if (signupMeetingType !=INDIVIDUAL_TYPE)
						return;
						
				    var startTimeTag ="startTime";
				    var endTimeTag ="endTime";
				    var slotNumTag = document.getElementById("meeting:numberOfSlot");
					if (!slotNumTag)
 						return;	
 						
				    prev_slotNum = signup_ValidateNumber(prev_slotNum,slotNumTag,100);   
	                    
				  	var slotNum = parseInt(slotNumTag.value);
	                var duration = getSignupDateTime(endTimeTag).getTime() - getSignupDateTime(startTimeTag).getTime();
				    var currentTimeslotDuration = document.getElementById("meeting:currentTimeslotDuration");
				    var slot_duration= isNaN(duration/(slotNum*60*1000))? 0 : duration/(slotNum*60*1000);
				   	if(showDecimal=='yes')
				    	currentTimeslotDuration.value= slot_duration;
				    else
					 	currentTimeslotDuration.value= Math.floor(slot_duration);
	
				    setTimeout( "signup_displaySlotDurationFloorNum();", 1200);//1.2 sec
		}
		
		function getSignupDuration(){
			getSignup_Duration('yes');
		}
		
		function getSignupDurationNoDecimal(){
			getSignup_Duration('no');
		}		
		
		function signup_displaySlotDurationFloorNum(){		
				    var slotDurationTag = document.getElementById("meeting:currentTimeslotDuration");
				    if(slotDurationTag){
				    	var cur_durationVal=parseFloat(slotDurationTag.value);
				    	slotDurationTag.value= Math.floor(cur_durationVal);
				    }
		}
		
		var waiting=false;
		function delayedRecalculateDateTime(){
			if (!waiting){
					waiting = true;
					setEndtimeMonthDateYear();
					getSignupDurationNoDecimal();
				  	setTimeout("waiting=false;", 1500);//1.5 sec
				}			
		}
	
//end
			
		
	function signup_resetIFrameHeight(iFrameId){
		if (!iFrameId)
			return;
			
		var id = iFrameId.replace(/[^a-zA-Z0-9]/g,"x");
		id = "Main" + id;
		setMainFrameHeight(id);//in library: headscripts.js
	}
	
	//replace myface button with nice calendar imageIcon
	function replaceCalendarImageIcon(){
		if (window.ActiveXObject)//no IE browser
			return;
		
		var inputTags = document.getElementsByTagName("input");
		if (!inputTags)
			return;
			
		for(i=0; i<inputTags.length;i++){
			if(inputTags[i].type=='button' && inputTags[i].value == '...'){
			inputTags[i].type ="image";
			inputTags[i].src="/sakai-signup-tool/images/cal.gif";
			inputTags[i].setAttribute("onclick", inputTags[i].getAttribute("onclick") + ";return false;");
			inputTags[i].className="calendarImageIcon";
			}
				
		}
	}		
	

	