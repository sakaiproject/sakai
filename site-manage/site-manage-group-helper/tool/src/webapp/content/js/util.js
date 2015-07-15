function setStateValue()
{
	alert('instate');
	// concatenate the group member option values
	var stateValue = "";
	var values = document.getElementById('newRight');
	alert(values.value);
	alert("sate=" + stateValue);
	document.getElementById('content::state-init').value = stateValue;
}

function toggleCheckboxes( clickedElement )
{
	var checkboxes = document.getElementsByName( "delete-group-selection" );
	for( i = 0; i < checkboxes.length; i++ )
	{
		checkboxes[i].checked = clickedElement.checked;
		adjustCount( checkboxes[i], "removeCount", "delete-groups" );
	}
}

function syncSelectAll()
{
	var allSelected = true;
	var checkboxes = document.getElementsByName( "delete-group-selection" );
	for( i = 0; i < checkboxes.length; i++ )
	{
		if( !checkboxes[i].checked )
		{
			allSelected = false;
			break;
		}
	}

	document.getElementById( "selectAll" ).checked = allSelected;
}

function adjustCount(caller, countName, buttonName)
{
	var counter = document.getElementById(countName);
	var button = document.getElementById(buttonName);
	if(caller && caller.checked && caller.checked === true)
	{
		counter.value = parseInt(counter.value) + 1;
	}
	else
	{
		counter.value = parseInt(counter.value) - 1;
	}

	if(button)
	{
		if(counter.value > 0)
		{
			button.disabled = false;
			button.className='enabled active';
		}
		else
		{
			button.disabled = true;
			button.className='disabled';
		}
	}
}
