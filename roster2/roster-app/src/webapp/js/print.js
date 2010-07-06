// This can be expandable if we want to add other functions to execute when the page is ready

//create onDomReady Event
window.onDomReady = DomReady;

//Setup the event
function DomReady(fn)
{
	//W3C
	if(document.addEventListener)
	{
		document.addEventListener("DOMContentLoaded", fn, false);
	}
	//IE
	else
	{
		document.onreadystatechange = function(){readyState(fn)}
	}
}

//IE execute function
function readyState(fn)
{
	//dom is ready for interaction
	if(document.readyState == "complete")
	{
		fn();
	}
}

//execute as soon as DOM is loaded
window.onDomReady(onReady);

//do on ready
function onReady()
{
	window.print();
}
