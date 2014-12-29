var serverUrl ="http://academicapps1.yu.yale.edu:8080";
var siteId="c8f0e8a4-31fd-4525-ac5e-6788d612347c";
var userId ="50c08b39-0232-4072-b5fc-7c5bc2e3e965";
var viewNextDays='';//view all with value of '';


var showMeJsonObjectEvent = {
	click: function(){
		var procData = siginup_processEventListTimeInJson(data);
		var htmlstring = sdata.html.Template.render("eventlist_template", procData); 
		$("#eventlist_template").html(htmlstring).show();
         }
     };

//Note:here the userId value can be any random number, the server side will get the userId via session
//however, if you are admin role, the userId should be valid userId in order to view this user's Signup Info
var showMySignupAjaxEventList= {
   click: function(){
	var cur_userId = document.getElementById('userId').value;
	viewNextDays=document.getElementById('nextDays').value;
	if(cur_userId && cur_userId.length > 1)
	    userId=cur_userId;

	var cur_serverUrl = document.getElementById('serverUrl').value;
	if(cur_serverUrl && cur_serverUrl.length > 1)
	    serverUrl=cur_serverUrl ;

	
	sdata.Ajax.request({
    		url : serverUrl + "/direct/mySignup/user/" + userId + ".json?viewNextDays=" + viewNextDays + "&rnd=" + Math.random(),
    		httpMethod : "GET",
		onSuccess : function(data) {
       	 var context = eval( '(' + data + ')' );
 		 context = siginup_processMySignupEventListTimeInJson(context);
       	 // evaluate the template in div helloworld_template1 with context
        	 var htmlstring = sdata.html.Template.render("eventlist_template", context); 
            
        	 $("#eventlist_template").html(htmlstring).show(); 
		 $("#eventDetail_template").hide();          		
    	},
    	onFail : function(status) {
		alert("Oh dear no event for signup "+status);
    	}
   });

 }
};

var showMeAjaxEventList= {
   click: function(){
	var cur_siteId = document.getElementById('userSite').value;
	viewNextDays=document.getElementById('nextDays').value;
	if(cur_siteId && cur_siteId.length > 1)
	    siteId=cur_siteId;

	var cur_serverUrl = document.getElementById('serverUrl').value;
	if(cur_serverUrl && cur_serverUrl.length > 1)
	    serverUrl=cur_serverUrl ;

	
	sdata.Ajax.request({
    		url : serverUrl + "/direct/signupEvent/site/" + siteId + ".json?viewNextDays=" + viewNextDays + "&rnd=" + Math.random(),
    		httpMethod : "GET",
		onSuccess : function(data) {
       	 var context = eval( '(' + data + ')' );
 		 context = siginup_processEventListTimeInJson(context);
       	 // evaluate the template in div helloworld_template1 with context
        	 var htmlstring = sdata.html.Template.render("eventlist_template", context); 
            
        	 $("#eventlist_template").html(htmlstring).show(); 
		 $("#eventDetail_template").hide();          		
    	},
    	onFail : function(status) {
		alert("Oh dear no event for signup "+status);
    	}
   });

 }
};

var showEventDetail = {
	click: function(eventId,siteId){
	$("#eventlist_template").hide();
	showEvent(eventId,siteId);
		
         }
     };

var submitEvent ={
	click:function(eventId, tsId, userActType,siteId){
	//alert(eventId +":" +tsId + ":" + userActType +":" + siteId);
	 document.getElementById('eventDetail_template').style.cursor='wait';
	editEvent(eventId,tsId,userActType,siteId);		
     }  
 };

var editEvent= function(eventId, tsId, userActType,siteId){
	var tosend ={"siteId": siteId, "allocToTSid": tsId, "userActionType":userActType};
	
	sdata.Ajax.request({
    		url : serverUrl + "/direct/signupEvent/" + eventId + "/edit",
    		httpMethod : "POST",
		onSuccess : function(data) {
		showEvent(eventId, siteId);		
    	},
    	onFail : function(status) {
		if(status == '1224')
			alert("Oh dear no event for signup -ajax: "+status);
		else{
			showEvent(eventId, siteId);
		}
		
    	},

	postData : tosend,
	contentType : "application/x-www-form-urlencoded"

   });

 };


var showEvent = function(eventId,siteId){
	sdata.Ajax.request({
			//now the siteId parameter is optional. If not supplied, it will return a site with highest permission level 
			// which the user has for this event
    		url : serverUrl + "/direct/signupEvent/" + eventId+ ".json?siteId=" + siteId + "&rmd=" + Math.random(),
    		httpMethod : "GET",
		onSuccess : function(data) {
       	  var context = eval( '(' + data + ')' );
		  context = siginup_processEventTimeslotInJson(context);
       	  // evaluate the template in div helloworld_template1 with context
        	  var htmlstring = sdata.html.Template.render("eventDetail_template", context);
		  //alert(htmlstring); 
            
        	 $("#eventDetail_template").html(htmlstring).show();	
		 document.getElementById('eventDetail_template').style.cursor="default";
    		},
    		onFail : function(status) {
			alert("Oh dear no event for signup "+status);
    		}
  	 });
}

function siginup_processMySignupEventListTimeInJson(json){
	var items = json.mySignup_collection;
	
	if(items==null)
		return;

	for(var i=0; i<items.length; i++){
	   var date = new Date(items[i].myStartTime);
	   items[i].myStartTime=date.toLocaleTimeString();
	   date = new Date(items[i].myEndTime);
	   items[i].myEndTime=date.toLocaleTimeString();
	   items[i].date=date.toDateString();
	}
	return json;
}


function siginup_processEventListTimeInJson(json){
	var items = json.signupEvent_collection;
	if(items==null)
		return;

	for(var i=0; i<items.length; i++){
	   var date = new Date(items[i].startTime);
	   items[i].startTime=date.toLocaleTimeString();
	   date = new Date(items[i].endTime);
	   items[i].endTime=date.toLocaleTimeString();
	   items[i].date=date.toDateString();
	}
	return json;
}

function siginup_processEventTimeslotInJson(json){
	var date = new Date(json.date);
	json.date = date.toLocaleTimeString() + ", " + date.toDateString()
	date = new Date(json.startTime);
      json.startTime = date.toLocaleTimeString();
	date = new Date(json.endTime);
	json.endTime = date.toLocaleTimeString();
	date = new Date(json.signupBegins);
	json.signupBegins= date.toLocaleTimeString() + ", " + date.toDateString();

	var items = json.signupTimeSlotItems;
	if(items==null)
		return;

	for(var i=0; i<items.length; i++){
	   var date = new Date(items[i].startTime);
	   items[i].startTime=date.toLocaleTimeString();
	   date = new Date(items[i].endTime);
	   items[i].endTime=date.toLocaleTimeString();
	}
	return json;
}






