// Timer Bar - Version 1.0
// Based on Script by Brian Gosselin of http://scriptasylum.com
// Modifications copyright Sakai Project 2005

//*****************************************************//
//**********  DO NOT EDIT BEYOND THIS POINT  **********//
//*****************************************************//

var ns4=(document.layers)?true:false;
var ie4=(document.all)?true:false;
var blocksize=(barwidth-2)/waitTime/10;
var PBouter;
var PBdone;
var PBbckgnd;
var Pid=0;
var txt='';
var pauseTiming=false;
var warnTime;
var minutes;
var seconds;
var hours;
var running=false;
var timerID=null;
var endH=null;
var endM=null;
var endS=null;

var hasTimeLimit = (document.getElementById('takeAssessmentForm:hasTimeLimit') != null && document.getElementById('takeAssessmentForm:hasTimeLimit').value=="true") || 
					(document.getElementById('tableOfContentsForm:hasTimeLimit') != null && document.getElementById('tableOfContentsForm:hasTimeLimit').value=="true");
var showTimeWarning = (document.getElementById('takeAssessmentForm:showTimeWarning') != null && document.getElementById('takeAssessmentForm:showTimeWarning').value=="true") || 
						(document.getElementById('tableOfContentsForm:showTimeWarning') != null && document.getElementById('tableOfContentsForm:showTimeWarning').value=="true");

//alert("hasTimeLimit=" + hasTimeLimit);
//alert("showTimeWarning=" + showTimeWarning);

if(ns4){
txt+='<table border=0 cellpadding=0 cellspacing=0><tr><td>';
txt+='<ilayer name="PBouter" visibility="hide" height="'+barheight+'" width="'+barwidth+'">';
txt+='<layer width="'+barwidth+'" height="'+barheight+'" bgcolor="'+bordercolor+'" top="0" left="0"></layer>';
txt+='<layer name="tbar" width="'+(barwidth-2)+'" height="'+(barheight-2)+'" bgcolor="'+unloadedcolor+'" top="1" left="1"></layer>';
txt+='<layer name="PBdone" width="'+(barwidth-2)+'" height="'+(barheight-2)+'" bgcolor="'+loadedcolor+'" top="1" left="1"></layer>';
txt+='</ilayer>';
txt+='</td></tr></table>';
}else{
txt+='<div id="PBouter" style="position:relative; visibility:hidden; background-color:'+bordercolor+'; width:'+barwidth+'px; height:'+barheight+'px;">';
txt+='<div id="tbar" style="position:absolute; top:1px; left:1px; width:'+(barwidth-2)+'px; height:'+(barheight-2)+'px; background-color:'+unloadedcolor+'; font-size:1px;"></div>';
txt+='<div id="PBdone" style="position:absolute; top:1px; left:1px; width:0px; height:'+(barheight-2)+'px; background-color:'+loadedcolor+'; font-size:1px;"></div>';
txt+='</div>';
}

document.write(txt);

// sentinels to make sure that timer bar is running continuously
var epoch_milliseconds = new Date().getTime();
var last_epoch_milliseconds = new Date().getTime();
var max_suspense_milliseconds = 10000;//10 seconds....
var payback = 1.5;

function hidebar(){
clearInterval(Pid);
window.status='';
//if(ns4)PBouter.visibility="hide";
//else PBouter.style.visibility="hidden";
if(hasTimeLimit) {
	action();
}
else {	
	action2();
}
}

//THIS FUNCTION BY MIKE HALL OF BRAINJAR.COM
function findlayer(name,doc){
var i,layer;
for(i=0;i<doc.layers.length;i++){
layer=doc.layers[i];
if(layer.name==name)return layer;
if(layer.document.layers.length>0)
if((layer=findlayer(name,layer.document))!=null)
return layer;
 }
return null;
}

function progressBarInit(){
PBouter=(ns4)?findlayer('PBouter',document):(ie4)?document.all['PBouter']:document.getElementById('PBouter');
PBdone=(ns4)?PBouter.document.layers['PBdone']:(ie4)?document.all['PBdone']:document.getElementById('PBdone');
resizeEl(PBdone,0,0,barheight-2,0);

if (hasTimeLimit) {
	//alert("showTimeWarning=" + document.getElementById('tableOfContentsForm:hasTimeLimit').value);
	if(ns4)PBouter.visibility="show";
	else PBouter.style.visibility="visible";
}
else {
	if (showTimeWarning==true) {
		//alert("You have less than 30 min...");
		show30MinWarning();
	}
	//PBouter.display="none";
	//PBdone.display="none";
}
Pid=setInterval('progressTimerBar()', 100);
startTimer();
}

function resizeEl(id,t,r,b,l){
if(ns4){
id.clip.left=l;
id.clip.top=t;
id.clip.right=r;
id.clip.bottom=b;
}else id.style.width=r+'px';
}


function startTimer(){
 //alert("startTimer");
 var timeLeft=waitTime-Math.floor(loaded/10);  //loaded is in 1/10th second so divide 10. 
 var hours=Math.floor(timeLeft/3600);
 var minutes=Math.floor((timeLeft%3600)/60);
 var seconds=(timeLeft%3600)%60;
  running =true;
  begin=new Date(); // get start time
  var setTime=1000*3600*hours+1000*60*minutes+1000*seconds;
  beginH=begin.getHours(); // set start hour, min, sec
  beginM=begin.getMinutes();
  beginS=begin.getSeconds();
  endH=beginH+hours; // set end hour, min, sec.
  endM=beginM+minutes;
  endS=beginS+seconds;
  if(endM>=60){
    endH=endH+1;
    endM=endM-60;
  }
  if(endS>=60){
    endM=endM+1;;
    endS=endS-60;
  }

  showCountDown();
}

function showCountDown(){
  var present=new Date();
  var presentH=present.getHours();
  var presentM=present.getMinutes();
  var presentS=present.getSeconds();
  //alert("present=" + ":" + presentH + ":" + presentM + ":" + presentS + ", end=" + endH + ":" + endM + ":" + endS);
  var theTime=((endH*3600)+(endM*60)+endS) -((presentH*3600)+(presentM*60)+presentS);
  //alert("theTime=" +theTime);
  
  if (theTime >= 86400) {
	  theTime = theTime - 86400;
  }
  
  if (theTime<=0) {
	stopTimer();
	//alert('theTime=' + theTime);
  }
  else {
    var remainH=Math.floor(theTime/3600);
    var remainM=Math.floor((theTime%3600)/60);
    var remainS=(theTime%3600)%60;
    
    var h='';var m='';var s='';
    if(remainH<=9) h='0';
    if(remainM<=9) m='0';
    if(remainS<=9) s='0';
    remainTime=remainH+':'+m+remainM+':'+s+remainS; // remaining time
    if(hasTimeLimit) {
    	document.getElementById('timer').innerHTML=remainTime;
    }
    if(running){
      if(!pauseTiming==true){
      timerID=setTimeout("showCountDown()",1000);}
    }
  }
}

function progressTimerBar(){
    var present=new Date();
    var presentH=present.getHours();
    var presentM=present.getMinutes();
    var presentS=present.getSeconds();
    var remainTime=((endH*3600)+(endM*60)+endS) -((presentH*3600)+(presentM*60)+presentS);
    if (remainTime >= 86400) {
    	remainTime = remainTime - 86400;
    }
	//alert("remainTime=" + remainTime);
    window.status="Loaded....";
    var setRedBar = false;
    if(hasTimeLimit && waitTime>300){ //waitTime is the timeLimit and it is in second
		if(remainTime==300){ //waitTime is the timeLimit and it is in second
		setRedBar = true;
		//alert('You have 5 minutes left. "a"');
		fiveMinutesAction();
		}
		else if (remainTime < 300){ setRedBar = true; } 

		if (setRedBar){
			if(ns4){
				document.tbar.bgColor="red";
			}
			else{
				if(ie4){
					document.all.tbar.style.backgroundColor="red";
				}
				else{
					document.getElementById("tbar").style.backgroundColor="red";
				}
			}
		}
    }
	resizeEl(PBdone, 0, blocksize*(waitTime - remainTime) * 10 , barheight-2, 0);
}

function stopTimer(){
  if(hasTimeLimit) {
    document.getElementById('timer').innerHTML=timeUpMessage;
  }
  clearTimeout(timerID);
running=false;
hidebar(); 
}

progressBarInit();


