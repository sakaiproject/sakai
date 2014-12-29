	// global counter to determine if any checkboxes are checked
	var numberChecked = 0;
	
	// storage for onclick properties of bulk operations
	// for enabling/disabling
	var readOnClick, unreadOnClick, deleteOnClick, moveOnClick;
	
	// needed for visual disabling in non-IE browsers
	var readTextColor, unreadTextColor, deleteTextColor, moveTextColor;
	
	// storage for style information of bulk operations
	// so if 'disabled', remove underline
	var readStyle, unreadStyle, deleteStyle, moveStyle;
	
	function toggleDisplay(show,hide) 
	{
		if (document.getElementById) 
		{
			target = document.getElementById(show);
			targethide= document.getElementById(hide);
			
			if(target != null)
			{
			 	if (target.style.display == "none")
				{
	      	target.style.display = "";
	        targethide.style.display="none" ;
	       }
	       else 
	       {
	         target.style.display = "none";
	         targethide.style.display="" ;
	       }
			}
		}
	}
	
	function updateCount(checked)
	{ 
		if (checked)
		{
			numberChecked++;
		}
		else
		{
			numberChecked--;
		}
	}
	
	function anyChecked()
	{
		return numberChecked > 0;
	}

	function toggleBulkOperations(anyChecked, formRef)
	{ 
		if(anyChecked){
			$('.ToggleBulk').show();
			$('.ToggleBulkDisabled').hide();
		}else{
			$('.ToggleBulk').hide();
			$('.ToggleBulkDisabled').show();
		}
	}		
