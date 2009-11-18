	//var $j = jQuery;
	
	var GROUP_TYPE = 'group';
	var ANNOUNCMENT_TYPE='announcement';
	var INDIVIDUAL_TYPE = 'individual';
	var signupMeetingType=INDIVIDUAL_TYPE;
	function initialLayoutsSetup(){	
		
		var table = document.getElementById("meeting:groupSubradio");
        row = table.getElementsByTagName("tr");
        for (i=0; i<row.length; i++) {
                cell = row[i].getElementsByTagName("td");
                inp = cell[0].getElementsByTagName("input");                         
                if (inp[0].checked &&  i ==1)
                      document.getElementById("meeting:maxAttendee").style.display="none";
         }                                          

		var recurSelectorVal = document.getElementById("meeting:recurSelector").value;
		if(recurSelectorVal =='no_repeat')
			document.getElementById('meeting:utilCalendar').style.display="none";
		else
			document.getElementById('meeting:utilCalendar').style.display="";
			
		var m = document.getElementById('multiple');
        var s = document.getElementById('single');
        var table = document.getElementById('meeting:meetingType');
        var rows = table.getElementsByTagName("tr");
        showSignupBeginDeadline('yes');
        //for case: JSF page refresh due to input error by JSF phaseI checking
        for (i=0; i<rows.length; i++) {

                cells = rows[i].getElementsByTagName("td");
                inp = cells[0].getElementsByTagName("input");
                str = inp[0].value;
           
                if(str==ANNOUNCMENT_TYPE && inp[0].checked) {                     
                    m.style.display="none";
                    s.style.display="none";
                    showSignupBeginDeadline('no');
                }
                if(str==GROUP_TYPE && inp[0].checked) {              
                	s.style.display="";
                	m.style.display="none";
                }

                if(str==INDIVIDUAL_TYPE && inp[0].checked) {				
                	s.style.display="none";
                	m.style.display="";

                }
                
                signupMeetingType=str;//control display and calc.
        }
        
        getSignupDuration();//recalcu
        
        currentSiteSelection();//setup checkbox       
	}



	
		//var m = document.getElementById('multiple');
		//var s = document.getElementById('single');       
		//m.style.display="none";
		//s.style.display="none";	
        

		function switMeetingType(a){ 
		   	  signupMeetingType = a;//global Param
		  	
		      var multipleTag = document.getElementById('multiple');
			  var announcementTag = document.getElementById('meeting:announ');
			  var singleTag = document.getElementById('single');
		 		
		 	  singleTag.style.display="none";     
		      announcementTag.style.display="none";
		      multipleTag.style.display="none";
		      showSignupBeginDeadline('yes');
		       
		        if (a == INDIVIDUAL_TYPE ) {               
		        	multipleTag.style.display="";     		                         
		        }
		        else if (a == GROUP_TYPE ) { 
		        	singleTag.style.display="";                                         		            	                     
		        }    
		        else if (a == ANNOUNCMENT_TYPE) {
					announcementTag.style.display="";
					showSignupBeginDeadline('no');                    		                               
		        }
		 	
			getSignupDuration();                
	
		}
		

 
	