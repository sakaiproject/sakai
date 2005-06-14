/*
 *                       Navigo Software License
 *
 * Copyright 2003, Trustees of Indiana University, The Regents of the University
 * of Michigan, and Stanford University, all rights reserved.
 *
 * This work, including software, documents, or other related items (the
 * "Software"), is being provided by the copyright holder(s) subject to the
 * terms of the Navigo Software License. By obtaining, using and/or copying this
 * Software, you agree that you have read, understand, and will comply with the
 * following terms and conditions of the Navigo Software License:
 *
 * Permission to use, copy, modify, and distribute this Software and its
 * documentation, with or without modification, for any purpose and without fee
 * or royalty is hereby granted, provided that you include the following on ALL
 * copies of the Software or portions thereof, including modifications or
 * derivatives, that you make:
 *
 *    The full text of the Navigo Software License in a location viewable to
 *    users of the redistributed or derivative work.
 *
 *    Any pre-existing intellectual property disclaimers, notices, or terms and
 *    conditions. If none exist, a short notice similar to the following should
 *    be used within the body of any redistributed or derivative Software:
 *    "Copyright 2003, Trustees of Indiana University, The Regents of the
 *    University of Michigan and Stanford University, all rights reserved."
 *
 *    Notice of any changes or modifications to the Navigo Software, including
 *    the date the changes were made.
 *
 *    Any modified software must be distributed in such as manner as to avoid
 *    any confusion with the original Navigo Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 * IN THE SOFTWARE.
 *
 * The name and trademarks of copyright holder(s) and/or Indiana University,
 * The University of Michigan, Stanford University, or Navigo may NOT be used
 * in advertising or publicity pertaining to the Software without specific,
 * written prior permission. Title to copyright in the Software and any
 * associated documentation will at all times remain with the copyright holders.
 * The export of software employing encryption technology may require a specific
 * license from the United States Government. It is the responsibility of any
 * person or organization contemplating export to obtain such a license before
 * exporting this Software.
 */
 
//Minutes after which to change the display (setting to 6 will change at 5:59)
var warningMinutes = 5;

var message_on_occasion="Time's up!"

var countdownwidth='100%'
var countdownheight='35px'
var countdownbgcolor=''
var opentags='<font face="Verdana" size="0.8em"><b>'
var closetags='</b></font>'
var opencolortags='<font face="Verdana" color="red" size="0.8em"><b>'
var newtag = '';
var strD = '';
var strH = '';
var strM = '';


function update() {
  now = new Date(now.valueOf()+1000);
  var remainingseconds = (end - now);
  var remaining = remainingseconds;
  days = (remaining - (remaining % 86400000)) / 86400000;
  remaining = remaining - (days * 86400000);
  hours = (remaining - (remaining % 3600000)) / 3600000;
  remaining = remaining - (hours * 3600000);
  minutes = (remaining - (remaining % 60000)) / 60000;
  remaining = remaining - (minutes * 60000);
  seconds = (remaining - (remaining % 1000)) / 1000;

  if (remainingseconds > 0) {
    newtag = opentags;
    if ((warningMinutes * 60000) > remainingseconds) {
    	alert("You only have " +warningMinutes +" minutes left");
    }
  }
  else{
  	autoSubmit();
  }
}

/************************************
 * The following section contains code for the countdown script
 ***********************************/

//Dynamic countdown Script II- © Dynamic Drive (www.dynamicdrive.com)
//Support for hour minutes and seconds added by Chuck Winrich (winrich@babson.edu) on 12-12-2001
//Stripped out some of the unnecessary code for this situation - Pamela Song on 6/2/2004
//For full source code, 100's more DHTML scripts, visit http://www.dynamicdrive.com

function start_countdown(){
  if (document.layers)
    document.countdownnsmain.visibility="show"
  else if (document.all||document.getElementById)
    crosscount=document.getElementById&&!document.all?document.getElementById("countdownie") : countdownie
}

function format_time_left(){
  strD = days;
  strH = hours;
  strM = minutes;
  strS = seconds;
  strReturn = '';
  
  if (days > 0)
    strD = days + " Days, ";
  else
    strD = "";
    
  if (hours < 10)
  	strH = "0" + hours;
  if (minutes < 10)
  	strM = "0" + minutes;
  if (seconds < 10)
  	strS = "0" + seconds;

  if (days == 0 && hours == 0 && minutes < 1)
    strReturn = newtag+"Time Remaining " + strD+strH+":"+strM+":"+strS+closetags;
  else
    strReturn = newtag+"Time Remaining " + strD+strH+":"+strM+closetags;
  
  return strReturn;
}
/**
if (document.all||document.getElementById)
  document.write('<table width="95%"><tr><td align="right"><span align="center" id="countdownie" style="width:'+countdownwidth+'; background-color:'+countdownbgcolor+'"></span></td></tr></table>')
**/
/************************************
 * End code for Countdown Script
 ***********************************/

/************************************
 * The following section contains code for the Progress Bar
 ***********************************/

// Timer Bar - Version 1.0
// Author: Brian Gosselin of http://scriptasylum.com
// Script featured on http://www.dynamicdrive.com
// Stripped out some of the unnecessary code for this situation - Chris Maurer (chmaurer@iupui.edu) on 11/5/2003
/**
var loadedcolor='lightgrey' ;       // PROGRESS BAR COLOR
var warningcolor='red' ;       // PROGRESS BAR warning COLOR
var unloadedcolor='darkseagreen';     // COLOR OF UNLOADED AREA
var bordercolor='black';            // COLOR OF THE BORDER
var barheight=15;                  // HEIGHT OF PROGRESS BAR IN PIXELS
var barwidth=300;                  // WIDTH OF THE BAR IN PIXELS
var waitTime=0;                    // NUMBER OF SECONDS FOR PROGRESSBAR
var blocksize=0;             // Size of the blocks, based on waitTime

// THE FUNCTION BELOW CONTAINS THE ACTION(S) TAKEN ONCE BAR REACHES 100%.
// IF NO ACTION IS DESIRED, TAKE EVERYTHING OUT FROM BETWEEN THE CURLY BRACES ({})
// BUT LEAVE THE FUNCTION NAME AND CURLY BRACES IN PLACE.
// PRESENTLY, IT IS SET TO DO NOTHING, BUT CAN BE CHANGED EASILY.
// TO CAUSE A REDIRECT TO ANOTHER PAGE, INSERT THE FOLLOWING LINE:
// window.location="http://redirect_page.html";
// JUST CHANGE THE ACTUAL URL OF COURSE :)

var action=function()
{
//Take no action when the timer is complete
}

var ns4=(document.layers)?true:false;
var ie4=(document.all)?true:false;
var loaded=0;
var PBouter;
var PBdone;
var PBbckgnd;
var Pid=0;
var txt='';

if(ns4){
  txt+='<center><table border=0 cellpadding=0 cellspacing=0><tr><td>';
  txt+='<ilayer name="PBouter" visibility="hide" height="'+barheight+'" width="'+barwidth+'">';
  txt+='<layer width="'+barwidth+'" height="'+barheight+'" bgcolor="'+bordercolor+'" top="0" left="0"></layer>';
  txt+='<layer width="'+(barwidth-2)+'" height="'+(barheight-2)+'" bgcolor="'+unloadedcolor+'" top="1" left="1"></layer>';
  txt+='<layer name="PBdone" width="'+(barwidth-2)+'" height="'+(barheight-2)+'" bgcolor="'+loadedcolor+'" top="1" left="1"></layer>';
  txt+='</ilayer>';
  txt+='</td></tr></table></center>';
}
else{
  txt+='<table width="95%"><tr><td align="right"><div id="PBouter" style="position:relative; visibility:hidden; background-color:'+bordercolor+'; width:'+barwidth+'px; height:'+barheight+'px;">';
  txt+='<div style="position:absolute; top:1px; left:1px; width:'+(barwidth-2)+'px; height:'+(barheight-2)+'px; background-color:'+unloadedcolor+'; font-size:1px;"></div>';
  txt+='<div id="PBdone" style="position:absolute; top:1px; left:1px; width:0px; height:'+(barheight-2)+'px; background-color:'+loadedcolor+'; font-size:1px;"></div>';
  txt+='</div></td></tr></table>';
}
document.write(txt);

function displayProgressBar(preTime){
  
  PBouter=(ns4)?findlayer('PBouter',document):(ie4)?document.all['PBouter']:document.getElementById('PBouter');
//  if(ns4)
//    PBouter.visibility="show";
//  else
//    PBouter.style.visibility="visible";
  //Pid=setInterval('incrCount()',95);
  
  loaded = preTime*10;
  incrCount();
  //loaded = loaded+(preTime*10);

}

function incrCount(){
  loaded++;
  if(loaded<0)
    loaded=0;
  if(loaded>=waitTime*10){
    clearInterval(Pid);
    loaded=waitTime*10;
    setTimeout('hidebar()',100);
  }
  resizeEl(PBdone, 0, blocksize*loaded, barheight-2, 0);
}

function hidebar(){
  clearInterval(Pid);
  if(ns4)PBouter.visibility="hide";
  else PBouter.style.visibility="hidden";
  action();
//  autoSubmit();
}

//THIS FUNCTION BY MIKE HALL OF BRAINJAR.COM
function findlayer(name,doc){
  var i,layer;
  for(i=0;i<doc.layers.length;i++){
    layer=doc.layers[i];
    if(layer.name==name)
      return layer;
    if(layer.document.layers.length>0)
      if((layer=findlayer(name,layer.document))!=null)
        return layer;
  }
  return null;
}

function progressBarInit(theTime){
    waitTime = theTime;
    blocksize=(barwidth-2)/waitTime/10;
    PBouter=(ns4)?findlayer('PBouter',document):(ie4)?document.all['PBouter']:document.getElementById('PBouter');
    PBdone=(ns4)?PBouter.document.layers['PBdone']:(ie4)?document.all['PBdone']:document.getElementById('PBdone');
    resizeEl(PBdone,0,0,barheight-2,0);

}

function resizeEl(id,t,r,b,l){
  if(ns4){
    id.clip.left=l;
    id.clip.top=t;
    id.clip.right=r;
    id.clip.bottom=b;
  }
  else id.style.width=r+'px';
}


function handleClick() {
	if(ns4){
		  if(PBouter.visibility !="hide" ){
		  		PBouter.visibility = "hide";
		  		document.getElementById('progressBar').visibility = "hide";
		    document.getElementById('progress_bar_btn').value = "Show Progress Bar";
		  }
		  else{
      PBouter.visibility="show";
      document.getElementById('progressBar').visibility = "show";
      document.getElementById('progress_bar_btn').value = "Hide Progress Bar";
		  }
	 }
	 else{
		  if (PBouter.style.visibility != "hidden") {
		  	 PBouter.style.visibility="hidden";
		    document.getElementById('progressBar').style.visibility = "hidden";
		    document.getElementById('progress_bar_btn').value = "Show Progress Bar";
		  }
		  else {
 		  	PBouter.style.visibility="visible"
		  	 document.getElementById('progressBar').style.visibility = "visible";
		    document.getElementById('progress_bar_btn').value = "Hide Progress Bar";

		  }
	 }
}
**/
function autoSubmit(){
	   document.forms.ASIDeliveryForm.submit();
    alert("Time has expired and your work has been submitted.");
}
/************************************
 * End code 
 ************************************/