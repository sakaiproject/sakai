<script language=javascript>
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
** Function: openrteui_onclick()
** Input:   none
** Output:  none
**
** Description:  This function opens the RTE UI window. This is intentionally
**      violating some MVC in order to be assured that all is good on the client
**      side (applet loads, etc) before proceeding. This method should only be
**      called by the API_1484_11 Applet or from it's containing window.document.
**
**      The PackageBean fills in tool placement id and the current ScormPackage 
**      bean will have a 'flow' or 'choice' flag to help with the view choice
**
***************************************************************************/
function openrteui_onclick() 
{
	window.open('/portal/tool/<h:outputText value="#{ScormRteTool.toolManager.currentPlacement.id}/runtime/LMSMain.jsp?sakai.session=#{ScormRteTool.sessionManager.currentSession.id}" />','SAKAI SCORM RTE UI','resizable=yes,toolbar=yes,scrollbars=yes, width=800,height=600');
}

/****************************************************************************
**
** Function: openRTEUI()
** Input:   none
** Output:  none
**
** Description:  This function opens the RTE UI window. This is intentionally
**      violating some MVC in order to be assured that all is good on the client
**      side (applet loads, etc) before proceeding. This method should only be
**      called by the API_1484_11 Applet or from it's containing window.document.
**
**      The PackageBean fills in tool placement id and the current ScormPackage 
**      bean will have a 'flow' or 'choice' flag to help with the view choice
**
**   ***DEPRECATED*** - -tis is handled in that servlet proxy (aka Applet, by default)
***************************************************************************/
function openRTEUI() 
{	
alert("opening UI ... API_1484_11="+window.API_1484_11+": "+typeof window.API_1484_11.Initialize);
if(window.API_1484_11.Initialize == "function");
window.API_1484_11.Initialize("");
	win = window.open('','<h:outputText value="#{ScormRteTool.popupWindowName}" />','resizable=yes,toolbar=yes,scrollbars=yes,menubar=yes,width=800,height=600');
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
   window.parent.frames[3].document.location.href = "LMSLogin.htm";
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
   //window.parent.frames[2].document.location.href = "code.jsp";
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
   document.forms[0].next.disabled = true;
   document.forms[0].previous.disabled = true;
   document.forms[0].quit.disabled = true;
   document.forms[0].suspend.disabled = true;   
   
   // This is the launch line for the next SCO...
   // The Sequencing Engine determines which to launch and
   // serves it up into the LMS's content frame or child window - depending
    //on the method that was used to launch the content in the first place.
   var scoWinType = typeof(window.parent.frames[3].scoWindow);
   var theURL = "pleaseWait.jsp?button=next";
  
   if (scoWinType != "undefined" && scoWinType != "unknown")
   {
      if (window.parent.frames[3].scoWindow != null)
      {
         // there is a child content window so display the sco there.
         window.parent.frames[3].scoWindow.document.location.href = theURL;
               }
      else
      {
         window.parent.frames[3].document.location.href = theURL;
         
      }
   }
   else
   {
      window.parent.frames[3].document.location.href = theURL;
                
   }
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
    window.top.frames[3].location.href = "pleaseWait.jsp?scoID=" + choiceEvent;

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
   document.forms[0].next.disabled = true;
   document.forms[0].previous.disabled = true;
   document.forms[0].quit.disabled = true;
   document.forms[0].suspend.disabled = true;   
   
   // This is the launch line for the next SCO...
   // The Sequencing Engine determines which to launch and
   // serves it up into the LMS's content frame or child window - depending
    //on the method that was used to launch the content in the first place.
   var scoWinType = typeof(window.parent.frames[3].scoWindow);
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
      if (window.parent.frames[3].scoWindow != null)
      {
         // there is a child content window so display the sco there.
         window.parent.frames[3].scoWindow.document.location.href = theURL;
               }
      else
      {
         window.parent.frames[3].document.location.href = theURL;
         
      }
   }
   else
   {
      window.parent.frames[3].document.location.href = theURL;
                
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
   document.forms[0].next.disabled = true;
   document.forms[0].previous.disabled = true;
   document.forms[0].quit.disabled = true;
   document.forms[0].suspend.disabled = true;
      
   var scoWinType = typeof(window.parent.frames[3].scoWindow);
   var theURL = "pleaseWait.jsp?button=prev";
   
   if (scoWinType != "undefined" && scoWinType != "unknown")
   {
      if (window.parent.frames[3].scoWindow != null)
      {
         // there is a child content window so display the sco there.
         window.parent.frames[3].scoWindow.document.location.href = theURL;
      }
      else
      {
         window.parent.frames[3].document.location.href = theURL;
        
      }
   }
   else
   {
      window.parent.frames[3].document.location.href = theURL;
     
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
   var scoWinType = typeof(window.parent.frames[3].window);
   
   ctrl = window.document.forms[0].control.value;
   
   if ( ctrl == "auto" )
   {
      
      window.top.frames[3].location.href = "LMSMenu.jsp"
      window.top.contentWindow.close();
   }
   else
   {
        
      if (scoWinType != "undefined" && scoWinType != "unknown")
      {
         if (window.parent.frames[3].scoWindow != null)
         {      
            // there is a child content window so close it.
            window.parent.frames[3].scoWindow.close();
            window.parent.frames[3].scoWindow = null;
         }
         window.parent.frames[3].document.location.href = "LMSMenu.jsp";
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
   API_1484_11 = this.document.API_1484_11;
   window.top.frames[0].document.forms[0].next.style.visibility = "hidden"; 
   window.top.frames[0].document.forms[0].previous.style.visibility = "hidden";
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
           document.forms[0].next.disabled = true;
           document.forms[0].previous.disabled = true;
           document.forms[0].quit.disabled = true;
           document.forms[0].suspend.disabled = true;   
           
           var scoWinType = typeof(window.parent.frames[3].scoWindow);
           var theURL = "sequencingEngine.jsp?button=exitAll";
          
           if (scoWinType != "undefined" && scoWinType != "unknown")
           {
              if (window.parent.frames[3].scoWindow != null)
              {
                 // there is a child content window so display the sco there.
                 window.parent.frames[3].scoWindow.document.location.href = theURL;
              }
              else
              {
                 window.parent.frames[3].document.location.href = theURL;
                 
              }
           }
           else
           {
              window.parent.frames[3].document.location.href = theURL;
                        
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