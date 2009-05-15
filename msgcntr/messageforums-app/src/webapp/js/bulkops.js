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
		// stores link object since multiple access methods
		var readEl, unreadEl, deleteEl, moveEl;
		var IEbrowser;
		var markAsread = formRef + ':markAsread';
		var markAsUnread = formRef + ':markAsUnread';
		var deleteMarked = formRef + ':deleteMarked';
		var deleteChecked = formRef + ':deleteChecked';
		var moveChecked = formRef + ':moveCheckedToFolder';
		
		if (document.all)
		{
			readEl = document.all[markAsread];
			unreadEl = document.all[markAsUnread];
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
			unreadEl = document.getElementById(markAsUnread);
			if (document.getElementById(deleteMarked))
				deleteEl = document.getElementById(deleteMarked);
			else
				deleteEl = document.getElementById(deleteChecked);
			moveEl = document.getElementById(moveChecked);
			IEbrowser=false;
		}
		
		if(!readOnClick) {
			readOnClick = readEl.onclick;
		}
		if(!unreadOnClick) {
			unreadOnClick = unreadEl.onclick;
		}
		if(!deleteOnClick) {
			deleteOnClick = deleteEl.onclick;
		}
		if(!moveOnClick) {
			moveOnClick = moveEl.onclick;
		}
		
		if (anyChecked)
		{ 
			// toggle onclick functionality
			readEl.onclick = readOnClick;
			unreadEl.onclick = unreadOnClick;
			deleteEl.onclick = deleteOnClick;
			moveEl.onclick = moveOnClick;

			if (IEbrowser) 
			{	// IE - just set disabled property to false	
				readEl.disabled =  false;
				unreadEl.disabled = false;
				deleteEl.disabled = false;
				moveEl.disabled = false;
			}
			else
			{ // non-IE - reset text color
				readEl.style.color = readTextColor;
				unreadEl.style.color = unreadTextColor;
				deleteEl.style.color = deleteTextColor;
				moveEl.style.color = moveTextColor;
			}
		}
		else
		{
			// toggle onclick functionality
			readOnClick = readEl.onclick;
			unreadOnClick = unreadEl.onclick;
			deleteOnClick = deleteEl.onclick;
			moveOnClick = moveEl.onclick;

			// 'disable'
			readEl.onclick = 'return false;';
			unreadEl.onclick = 'return false;';
			deleteEl.onclick = 'return false;';
			moveEl.onclick = 'return false;';

			if (IEbrowser) 
			{ // IE - set disabled property to true
				readEl.disabled = true;
				unreadEl.disabled = true;
				deleteEl.disabled = true;
				moveEl.disabled = true;
			}
			else
			{ // non-IE - set color to grey and onclick to return false
				readTextColor = readEl.style.color;
				readEl.style.color = 'grey';
				
				unreadTextColor = unreadEl.style.color;
				unreadEl.style.color = 'grey';

				deleteTextColor = deleteEl.style.color;
				deleteEl.style.color = 'grey';

				moveTextColor = moveEl.style.color;
				moveEl.style.color = 'grey';
			}
		}
	}		
