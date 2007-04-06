<%@page import = "org.adl.util.EnvironmentVariable,org.adl.util.debug.LogConfig,java.io.*,java.util.logging.*"%>
<%
   /***************************************************************************
   **
   ** Filename: LMSFrame.jsp
   **
   ** File Description: This page contains the API Adapter applet.  The API 
   **                   Adapter applet has no visual display elements and is 
   **                   therefore invisible to the user.  Note that the API 
   **                   Adapter object is exposed to SCOs via the LMSMain.htm 
   **                   page.  The SCOs communicate with the Run-time 
   **                   Environment through this API.  This page also contains
   **                   the Run-time Environment login button and 
   **                   the button for Next, Previous, Suspend, and Quit.
   ** Author: ADL Technical Team 
   **
   ** Contract Number:
   ** Company Name: CTC
   **
   ** Module/Package Name:
   ** Module/Package Description: 
   **
   ** Design Issues:
   **
   ** Implementation Issues:
   ** Known Problems:
   ** Side Effects:
   **
   ** References: ADL SCORM
   **
   /***************************************************************************
   **
   ** Advanced Distributed Learning Co-Laboratory (ADL Co-Lab) Hub grants you 
   ** ("Licensee") a non-exclusive, royalty free, license to use, modify and 
   ** redistribute this software in source and binary code form, provided that 
   ** i) this copyright notice and license appear on all copies of the software; 
   ** and ii) Licensee does not utilize the software in a manner which is 
   ** disparaging to ADL Co-Lab Hub.
   **
   ** This software is provided "AS IS," without a warranty of any kind.  ALL 
   ** EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING 
   ** ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE 
   ** OR NON-INFRINGEMENT, ARE HEREBY EXCLUDED.  ADL Co-Lab Hub AND ITS LICENSORS 
   ** SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF 
   ** USING, MODIFYING OR DISTRIBUTING THE SOFTWARE OR ITS DERIVATIVES.  IN NO 
   ** EVENT WILL ADL Co-Lab Hub OR ITS LICENSORS BE LIABLE FOR ANY LOST REVENUE, 
   ** PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL, 
   ** INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER CAUSED AND REGARDLESS OF THE 
   ** THEORY OF LIABILITY, ARISING OUT OF THE USE OF OR INABILITY TO USE 
   ** SOFTWARE, EVEN IF ADL Co-Lab Hub HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH 
   ** DAMAGES. 
   **
   ***************************************************************************/
%>

<%
   Logger mLogger = Logger.getLogger("org.adl.util.debug.samplerte");
   try
   {
      
      String adlHome = 
         getServletConfig().getServletContext().getRealPath( "/" );
      adlHome = adlHome.substring(0, adlHome.lastIndexOf(File.separator) ); 

      LogConfig logConfig = new LogConfig();
      logConfig.configure(adlHome, true);

      mLogger.entering("---LMSFrame", "try()");
   }
   catch(Exception e)
   {  
      mLogger.severe("---Caught exception " + e);
   }
%>

<html>
<head>
<meta http-equiv="expires" content="Tue, 20 Aug 1999 01:00:00 GMT">
<meta http-equiv="Pragma" content="no-cache">
<title>SCORM 2004 3rd Edition Sample Run-Time Environment Version 1.0</title>

<link href="../includes/sampleRTE_style.css" rel="stylesheet" type="text/css">

<script language=javascript>
// variable to contain string of API and Datamodel calls made by current SCO
var mlog = "API and Datamodel Calls";

/****************************************************************************
**
** Function: LMSIsInitialized()
** Input:   none
** Output:  boolean
**
** Description:  This function returns a boolean that represents whether or 
**               not LMSInitialize() has been called by the SCO.
**
***************************************************************************/
function LMSIsInitialized()
{
   // Determines if the API (LMS) is in an initialized state.
   // There is no direct method for determining if the LMS API is initialized
   // for example an LMSIsInitialized function defined on the API so we'll try
   // a simple LMSGetValue and trap for the LMS Not Initialized Error
   
   var value = API.LMSGetValue("cmi.core.learner_name");
   var errCode = API.LMSGetLastError().toString();
   if (errCode == 301)
   {
      return false;
   }
   else
   {
      return true;
   }
}

/****************************************************************************
**
** Function: login_onclick()
** Input:   none
** Output:  none
**
** Description:  This function changes the content frame to the login page,
**               and "hides" the login button.
**
***************************************************************************/
function login_onclick() 
{
   window.parent.frames['Content'].document.location.href = "LMSLogin.htm";
   if ( document.layers != null )
   {
      swapLayers();
   }
   else if ( document.all != null )
   {
      window.document.forms[0].login.style.visibility = "hidden";
   }
   else
   {
      //Niether IE nor Netscape is being used
      alert("your browser may not be supported");
   }
}


/****************************************************************************
**
** Function: refreshMenu()
** Input:   none
** Output:  none
**
** Description:  This function is called by the API after an LMSCommit.  It
**               causes the menu page to load the latest UI state and update
**               itself.
**
***************************************************************************/
function refreshMenu()
{
   window.parent.frames['code'].document.location.href = "code.jsp";
}

/****************************************************************************
**
** Function: setUIState()
** Input:   boolean - state
** Output:  none
**
** Description:  This function is called twice during an LMSCommit.  It
**               disables the navigation buttons while the commit is active
**               and re-enables the buttons when the commit is finished. 
**
***************************************************************************/
function setUIState( state )
{
  if (! state)
  { 
    document.buttonform.quit.disabled = true;
    document.buttonform.previous.disabled = true;
    document.buttonform.next.disabled = true;
    document.buttonform.suspend.disabled = true;
  }
  else
  { 
    document.buttonform.quit.disabled = false;
    document.buttonform.previous.disabled = false;
    document.buttonform.next.disabled = false;
    document.buttonform.suspend.disabled = false;  
  }
}
/****************************************************************************
**
** Function: nextSCO()
** Input:   none
** Output:  none
**
** Description:  This function is called when the user clicks the "next"
**               button.  The Sequencing Engine is called, and all relevant
**               controls are affected. 
**
***************************************************************************/
function  nextSCO()
{
   // Disable the button controls
   document.forms['buttonform'].next.disabled = true;
   document.forms['buttonform'].previous.disabled = true;
   document.forms['buttonform'].quit.disabled = true;
   document.forms['buttonform'].suspend.disabled = true;   
   
   // This is the launch line for the next SCO...
   // The Sequencing Engine determines which to launch and
   // serves it up into the LMS's content frame or child window - depending
    //on the method that was used to launch the content in the first place.
   var scoWinType = typeof(window.parent.frames['Content'].scoWindow);
   var theURL = "pleaseWait.jsp?button=next";
  
   if (scoWinType != "undefined" && scoWinType != "unknown")
   {
      if (window.parent.frames['Content'].scoWindow != null)
      {
         // there is a child content window so display the sco there.
         window.parent.frames['Content'].scoWindow.document.location.href = theURL;
               }
      else
      {
         window.parent.frames['Content'].document.location.href = theURL;
         
      }
   }
   else
   {
      window.parent.frames['Content'].document.location.href = theURL;
                
   }
}

/****************************************************************************
**
** Function: display_log(String)
** Input:   none
** Output:  none
**
** Description:  This function writes information from API and datamodel calls to        
** a logging window.  The window is scrolled to the bottom to allow the most
** recent log entries to be visible.
**
***************************************************************************/
function display_log( call_string ) 
{   
    mlog += "<br>";
    mlog += call_string;
    top.frames['log'].document.getElementById('log_span').innerHTML = mlog;

	top.frames['log'].location.hash = '#bottom'; 
}       
/****************************************************************************
**
** Function: reset_log_string()
** Input:   none
** Output:  none
**
** Description:  This function clears the logging information from API 
** and datamodel calls when a new course is launched.       
** 
**               
**
***************************************************************************/
function reset_log_string() 
{   
    mlog = "API and Datamodel Calls";
    top.frames['log'].document.getElementById('log_span').innerHTML = mlog;
}       
/****************************************************************************
**
** Function: reset_logging()
** Input:   none
** Output:  none
**
** Description:  This function clears the logging information from API 
** and datamodel calls when a new course is launched.       
** 
**               
**
***************************************************************************/
function reset_logging() 
{   
    mlog = "API and Datamodel Calls";
    top.frames['log'].document.getElementById('log_span').innerHTML = mlog;
    API_1484_11 = this.document.APIAdapter;
    API_1484_11.resetLoggingVariable();
}       

/****************************************************************************
**
** Function: doChoiceEvent( navEvent)
** Input:   none
** Output:  none
**
** Description:  This function is called when Terminate has been called by 
** the SCO after a choice navEvent has been set.
**                
**
***************************************************************************/
function  doChoiceEvent( choiceEvent )
{ 
    window.top.frames['Content'].location.href = "pleaseWait.jsp?scoID=" + choiceEvent;

}

/****************************************************************************
**
** Function: invokeSuspendAll()
** Input:   none
** Output:  none
**
** Description:  This function is called when the learner presses the RTE
**				 provided Suspend button.
**                
**
***************************************************************************/
function  invokeSuspendAll()
{   
   API_1484_11 = this.document.APIAdapter;
   API_1484_11.suspendButtonPushed();
   doNavEvent( 'suspendAll' );
}


/****************************************************************************
**
** Function: doNavEvent( navEvent)
** Input:   none
** Output:  none
**
** Description:  This function is called when an LMSFinish has been called by 
** the SCO after a navEvent has been set.
**                
**
***************************************************************************/
function  doNavEvent( navEvent )
{   
   // Disable the button controls
   document.forms['buttonform'].next.disabled = true;
   document.forms['buttonform'].previous.disabled = true;
   document.forms['buttonform'].quit.disabled = true;
   document.forms['buttonform'].suspend.disabled = true;   
   
   // This is the launch line for the next SCO...
   // The Sequencing Engine determines which to launch and
   // serves it up into the LMS's content frame or child window - depending
    //on the method that was used to launch the content in the first place.
   var scoWinType = typeof(window.parent.frames['Content'].scoWindow);
   if ( navEvent == "continue" ) 
   {   
       navEvent = "next";
   }
   if ( navEvent == "previous" ) 
   {   
       navEvent = "prev";
   }
   
   var theURL = "sequencingEngine.jsp?button=" + navEvent;
  
   if (scoWinType != "undefined" && scoWinType != "unknown")
   {
      if (window.parent.frames['Content'].scoWindow != null)
      {
         // there is a child content window so display the sco there.
         window.parent.frames['Content'].scoWindow.document.location.href = theURL;
               }
      else
      {
         window.parent.frames['Content'].document.location.href = theURL;
         
      }
   }
   else
   {
      window.parent.frames['Content'].document.location.href = theURL;         
   }
   if ( document.layers != null )
   {
      swapLayers();
   }
    else if ( document.all != null )
   {
     // window.top.frames[0].document.forms[0].next.disabled = true;
     // window.top.frames[0].document.forms[0].previous.disabled = true;
   }
   else
   {
      //Neither IE nor Netscape is being used
      alert("your browser may not be supported");
   } 


}



/****************************************************************************
**
** Function: previousSCO()
** Input:   none
** Output:  none
**
** Description:  This function is called when the user clicks the "previous"
**               button.  The Sequencing Engine is called, and all relevant
**               controls are affected. 
**
***************************************************************************/
function  previousSCO()
{

   // This function is called when the "Previous" button is clicked.
   // The LMSLesson servlet figures out which SCO to launch and
   // serves it up into the LMS's content frame or child window - depending
   //on the method that was used to launch the content in the first place.

   // Disable the button controls
   document.forms['buttonform'].next.disabled = true;
   document.forms['buttonform'].previous.disabled = true;
   document.forms['buttonform'].quit.disabled = true;
   document.forms['buttonform'].suspend.disabled = true;
      
   var scoWinType = typeof(window.parent.frames['Content'].scoWindow);
   var theURL = "pleaseWait.jsp?button=prev";
   
   if (scoWinType != "undefined" && scoWinType != "unknown")
   {
      if (window.parent.frames['Content'].scoWindow != null)
      {
         // there is a child content window so display the sco there.
         window.parent.frames['Content'].scoWindow.document.location.href = theURL;
      }
      else
      {
         window.parent.frames['Content'].document.location.href = theURL;
        
      }
   }
   else
   {
      window.parent.frames['Content'].document.location.href = theURL;
     
      //  scoWindow is undefined which means that the content frame
      //  does not contain the lesson menu at this time.
   }
   if ( document.layers != null )
   {
      swapLayers();
   }
   else if ( document.all != null )
   {
     // window.document.forms[0].next.disabled = true;
     // window.document.forms[0].previous.disabled = true;
   }
   else
   {
     //Neither IE nor Netscape is being used
      alert("your browser may not be supported");
   }
  
}

/****************************************************************************
**
** Function: closeSCOContent()
** Input:   none
** Output:  none
**
** Description:  This function exits out of the current lesson and presents
**               the RTE menu. 
**
***************************************************************************/
function closeSCOContent()
{
   var scoWinType = typeof(window.parent.frames['Content'].window);
   
   ctrl = window.document.forms['buttonform'].control.value;
   
   if ( ctrl == "auto" )
   {
      
      window.top.frames['Content'].location.href = "LMSMenu.jsp"
      window.top.contentWindow.close();
   }
   else
   {
        
      if (scoWinType != "undefined" && scoWinType != "unknown")
      {
         if (window.parent.frames['Content'].scoWindow != null)
         {      
            // there is a child content window so close it.
            window.parent.frames['Content'].scoWindow.close();
            window.parent.frames['Content'].scoWindow = null;
         }
         window.parent.frames['Content'].document.location.href = "LMSMenu.jsp";
      }
      else
      {
         //  scoWindow is undefined which means that the content frame
         //  does not contain the lesson menu so do nothing...
      }
   }   
}

/****************************************************************************
**
** Function: swapLayers()
** Input:   none
** Output:  none
**
** Description:  This function is used to swap the login and logout buttons
**
***************************************************************************/
function swapLayers()
{
   if ( document.loginLayer.visibility == "hide" )
   {
      document.logoutLayer.visibility = "hide";
      document.loginLayer.visibility = "show";
   }
   else
   {
      document.loginLayer.visibility = "hide";
      document.logoutLayer.visibility = "show";
   }
}

/****************************************************************************
**
** Function: init()
** Input:   none
** Output:  none
**
** Description:  This function sets the API variable and hides the
**               the navigation buttons
**
***************************************************************************/
function init()
{
   API_1484_11 = this.document.APIAdapter;
   window.top.frames['LMSFrame'].document.forms['buttonform'].next.style.visibility = "hidden"; 
   window.top.frames['LMSFrame'].document.forms['buttonform'].previous.style.visibility = "hidden";
}

/****************************************************************************
**
** Function: doConfirms()
** Input:   none
** Output:  none
**
** Description:  This function prompts the user that they may lose
**               data if they exit the course.  If exit is confirmed,
**               the sequencing engine is called with "ExitAll".
**
***************************************************************************/
function doConfirm()
{
    if( confirm("If you quit now the course information may not be saved.  Do you wish to quit?") )
    {
       // Disable the button controls
           document.forms['buttonform'].next.disabled = true;
           document.forms['buttonform'].previous.disabled = true;
           document.forms['buttonform'].quit.disabled = true;
           document.forms['buttonform'].suspend.disabled = true;   
           
           var scoWinType = typeof(window.parent.frames['Content'].scoWindow);
           var theURL = "sequencingEngine.jsp?button=exitAll";
          
           if (scoWinType != "undefined" && scoWinType != "unknown")
           {
              if (window.parent.frames['Content'].scoWindow != null)
              {
                 // there is a child content window so display the sco there.
                 window.parent.frames['Content'].scoWindow.document.location.href = theURL;
              }
              else
              {
                 window.parent.frames['Content'].document.location.href = theURL;
                 
              }
           }
           else
           {
              window.parent.frames['Content'].document.location.href = theURL;
                        
           }
           if ( document.layers != null )
           {
              swapLayers();
           }
            else if ( document.all != null )
           {
             // window.top.frames[0].document.forms[0].next.disabled = true;
             // window.top.frames[0].document.forms[0].previous.disabled = true;
           }
           else
           {
              //Neither IE nor Netscape is being used
              alert("your browser may not be supported");
           }  

    }
    else
    {
    }
}
</script>
</head>

<body onload="init();" id="topNav">

<!--  For MS IE Use the Java 1.4 JRE Plug-in instead of the Browser's JVM
      Netscape 4.x can't use the plug-in because it's liveconnect doesn't 
	  work with the Plug-in
-->                                                                
<form name="buttonform">
 <object classid="clsid:8AD9C840-044E-11D1-B3E9-00805F499D93"
   width="0" height="0" id="APIAdapter"
   codebase="http://java.sun.com/products/plugin/autodl/jinstall-1_4-windows-i586.cab#Version=1,4,0,0">
  This object contains the API Adapter Applet
  <param name = "code" value = "org/adl/samplerte/client/ClientRTS.class" >
  <param name = "codebase" value = "/adl" >
  <param name = "type" value="application/x-java-applet;jpi-version=1.4.2">
  <param name = "mayscript" value="true" >
  <param name = "scriptable" value="true" >
  <param name = "archive" 
           value = "util.jar,cmidatamodel.jar,lmsclient.jar,debug.jar,sequencer.jar,joda-time-1.1.jar,sspserver.jar" >
  <comment>
  <applet code="org/adl/samplerte/client/ClientRTS.class" 
            archive="cmidatamodel.jar,lmsclient.jar,debug.jar,joda-time-1.1.jar,sspserver.jar" 
            codebase="/adl"
            src="/adl" 
            height="1" 
            id="APIAdapter" 
            name="APIAdapter" 
            width="1" 
            mayscript="true">
  </applet>
  </comment>
 </object>
       
         
    <table width="800">
    <tr valign="top"> 
       <td>
          <img border="0" align="Left" src="tiertwo.gif" alt="ADL Logo"/>
       </td>
       <td align="center">   
          <b>
             Advanced Distributed Learning <br>
             Sharable Content Object Reference Model (SCORM<SUP>&reg;</SUP>) 2004 3rd Edition<br>
          </b>
          <b>
             Sample Run-Time Environment<br>
          </b>
       </td>
    </tr>
    <tr>
        <td>&nbsp;</td>
    </tr>
    </table>                   
       
    <!--NOLAYER-->
    <table width="800" align="left" cellspacing=0>
    <tr>
       <td> 
          <input type="button" value="Log In" id="login" 
		  name="login" language="javascript"
                 onclick="return login_onclick();">&nbsp;       
          </td>
       <td align="left">
          <input type="button" value="Suspend" id="suspend" 
		  name="suspend" style="visibility: hidden"
                 language="javascript" onclick="return invokeSuspendAll();"> 
       </td>
       <TD ALIGN="center">
             <INPUT type="button" ALIGN = "right" VALUE="    Quit    " 
			 name="quit" language="javascript"
                ONCLICK="doNavEvent( 'exitAll' );" STYLE="visibility: hidden">
       </TD>
       <td align="left">
          <input type="button" align ="left" value="Glossary" id="glossary" 
		  name="glossary"  language="javascript"
          onclick="return nextSco();" style="visibility: hidden" disabled>&nbsp; 
       </td>
       <td>
	     &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
		 &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
	   </td>
       <td align="center"> 
          
          <input type="button" align ="right" value="<- Previous" id="previous" 
		     name="previous"  language="javascript"
             onclick="return previousSCO();"  style="visibility: hidden"> 
          
       </td>
       <td align="center">
             
             <input type="button" align ="right" value="Continue ->" id="next" 
			        name="next"  language="javascript"
                    onclick="return nextSCO();" style="visibility: hidden">   
       </td>
    </tr>
</table>
    
<div nowrap="true" align = "right" valign="bottom"><i>Version 1.0</i></div>
<input type="hidden" name="control" value="" />
<input type="hidden" name="isNextAvailable" value="" />
<input type="hidden" name="isPrevAvailable" value="" />
<input type="hidden" name="isTOCAvailable" value="" />    
<input type="hidden" name="isSuspendAvailable" value="" />
    
<!--/NOLAYER-->
</form>

</body>
</html>
