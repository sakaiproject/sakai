function doEditTitle(s)
{

	var newTitle = document.getElementById('mode-pre::title');
	alert(newTitle.value);
	$.get(window.location + "&newTitle=" + newTitle.value, function(xml){
    	alert( $("#mode-pass::value",xml).text() );
  	});
} 
