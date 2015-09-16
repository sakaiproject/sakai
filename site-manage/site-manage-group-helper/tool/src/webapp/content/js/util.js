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
		adjustCount( checkboxes[i], "removeCount" );
	}

	checkEnableRemove();
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
	checkEnableRemove();
}

function checkEnableRemove()
{
	var button = document.getElementById( "delete-groups" );
	if( button )
	{
		var anySelected = false;
		var checkboxes = document.getElementsByName( "delete-group-selection" );
		for( i = 0; i < checkboxes.length; i++ )
		{
			if( checkboxes[i].checked )
			{
				anySelected = true;
				break;
			}
		}

		if( anySelected )
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

function adjustCount(caller, countName)
{
	var counter = document.getElementById(countName);
	if(caller && caller.checked && caller.checked === true)
	{
		counter.value = parseInt(counter.value) + 1;
	}
	else
	{
		counter.value = parseInt(counter.value) - 1;
	}
}
