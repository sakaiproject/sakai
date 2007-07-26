	// global counter to determine if any checkboxes are checked
	var numberChecked = 0;
	
	// storage for onclick properties of bulk operations
	// for enabling/disabling
	var readOnClick, deleteOnClick, moveOnClick;
	
	// needed for visual disabling in non-IE browsers
	var readTextColor, deleteTextColor, moveTextColor;
	
	// storage for style information of bulk operations
	// so if 'disabled', remove underline
	var readStyle, deleteStyle, moveStyle;
	
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
		// stores link object since multiple access methods
		var readEl, deleteEl, moveEl;
		var IEbrowser;
		var markAsread = formRef + ':markAsread';
		var deleteMarked = formRef + ':deleteMarked';
		var deleteChecked = formRef + ':deleteChecked';
		var moveChecked = formRef + ':moveCheckedToFolder';
		
		if (document.all)
		{
			readEl = document.all[markAsread];
			if (document.all[deleteMarked])
				deleteEl = document.all[deleteMarked];
			else
				deleteEl = document.all[deleteChecked];
			moveEl = document.all[moveChecked];
			IEbrowser=true;
		}
		else
		{
			readEl = document.getElementById(markAsread);
			if (document.getElementById(deleteMarked))
				deleteEl = document.getElementById(deleteMarked);
			else
				deleteEl = document.getElementById(deleteChecked);
			moveEl = document.getElementById(moveChecked);
			IEbrowser=false;
		}

		if (anyChecked)
		{ 
			// toggle onclick functionality
			readEl.onclick = readOnClick;
			deleteEl.onclick = deleteOnClick;
			moveEl.onclick = moveOnClick;

			if (IEbrowser) 
			{	// IE - just set disabled property to false	
				readEl.disabled =  false;
				deleteEl.disabled = false;
				moveEl.disabled = false;
			}
			else
			{ // non-IE - reset text color
				readEl.style.color = readTextColor;
				deleteEl.style.color = deleteTextColor;
				moveEl.style.color = moveTextColor;
			}
		}
		else
		{
			// toggle onclick functionality
			readOnClick = readEl.onclick;
			deleteOnClick = deleteEl.onclick;
			moveOnClick = moveEl.onclick;

			// 'disable'
			readEl.onclick = 'return false;';
			deleteEl.onclick = 'return false;';
			moveEl.onclick = 'return false;';

			if (IEbrowser) 
			{ // IE - set disabled property to true
				readEl.disabled = true;
				deleteEl.disabled = true;
				moveEl.disabled = true;
			}
			else
			{ // non-IE - set color to grey and onclick to return false
				readTextColor = readEl.style.color;
				readEl.style.color = 'grey';

				deleteTextColor = deleteEl.style.color;
				deleteEl.style.color = 'grey';

				moveTextColor = moveEl.style.color;
				moveEl.style.color = 'grey';
			}
		}
	}		
