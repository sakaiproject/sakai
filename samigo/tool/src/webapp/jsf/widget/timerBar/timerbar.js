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

if(ns4){
txt+='<table border=0 cellpadding=0 cellspacing=0><tr><td>';
txt+='<ilayer name="PBouter" visibility="hide" height="'+barheight+'" width="'+barwidth+'" onmouseup="hidebar()">';
txt+='<layer width="'+barwidth+'" height="'+barheight+'" bgcolor="'+bordercolor+'" top="0" left="0"></layer>';
txt+='<layer name="tbar" width="'+(barwidth-2)+'" height="'+(barheight-2)+'" bgcolor="'+unloadedcolor+'" top="1" left="1"></layer>';
txt+='<layer name="PBdone" width="'+(barwidth-2)+'" height="'+(barheight-2)+'" bgcolor="'+loadedcolor+'" top="1" left="1"></layer>';
txt+='</ilayer>';
txt+='</td></tr></table>';
}else{
txt+='<div id="PBouter" onmouseup="hidebar()" style="position:relative; visibility:hidden; background-color:'+bordercolor+'; width:'+barwidth+'px; height:'+barheight+'px;">';
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

function incrCount(){
  // make sure that timer bar running, JavaScript enabled
  epoch_milliseconds = new Date().getTime();
  var suspense = epoch_milliseconds - last_epoch_milliseconds;

/* the Javascript timer on the page is an estimated time. The assessment time limit is
   based on the server time in 2.1.1. So it is ok if users cheat, the real time will catch up with them.
  // if some wise guy turned off JavaScript more than max_suspense_milliseconds
  if (suspense > max_suspense_milliseconds)
  {
    // add in suspense time in 1/10 seconds
    loaded += Math.floor(suspense/100);
    loaded *= payback; //  apply penalty.
    if (loaded > waitTime * 10)
      loaded = waitTime * 10;
    startTimer(); // crank up timer with new value
    alert("JavaScript must be enabled!");
  }
*/
  last_epoch_milliseconds = epoch_milliseconds;
  
  window.status="Loaded....";
  var setRedBar = false;
  if(waitTime>300){ //waitTime is the timeLimit and it is in second
    warnTime=10*(waitTime-300);
    if (loaded==warnTime){ // loaded is in 1/10th s
      setRedBar = true;
      alert('You have 5 minutes left');
    }
    if (loaded > warnTime){ setRedBar = true; } 
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

  if(!pauseTiming==true)
    loaded++;
  if(loaded<0)loaded=0;
  if(loaded>=waitTime*10){
    clearInterval(Pid);
    loaded=waitTime*10; // loaded is in 1/10th of a second here
    setTimeout('hidebar()',100);
  }
  resizeEl(PBdone, 0, blocksize*loaded, barheight-2, 0);
}

function hidebar(){
clearInterval(Pid);
window.status='';
//if(ns4)PBouter.visibility="hide";
//else PBouter.style.visibility="hidden";
action();
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
if(ns4)PBouter.visibility="show";
else PBouter.style.visibility="visible";
Pid=setInterval('incrCount()',100);
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

  if((endH==presentH)&&(endM==presentM)&&(endS==presentS)){ // reaches end time
    stopTimer();

  }else{
    var theTime=((endH*3600)+(endM*60)+endS) -((presentH*3600)+(presentM*60)+presentS);
    var remainH=Math.floor(theTime/3600);
    var remainM=Math.floor((theTime%3600)/60);
    var remainS=(theTime%3600)%60;
    var h='';var m='';var s='';
    if(remainH<=9) h='0';
    if(remainM<=9) m='0';
    if(remainS<=9) s='0';
    remainTime=remainH+':'+m+remainM+':'+s+remainS; // remaining time
    document.getElementById('timer').innerHTML=remainTime;
    if(running){
      if(!pauseTiming==true)
      timerID=setTimeout("showCountDown()",1000);
    }
  }
}

function stopTimer(){
  document.getElementById('timer').innerHTML="Time's up";
  clearTimeout(timerID);
running=false;
}
progressBarInit();


