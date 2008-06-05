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