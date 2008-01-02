check_checkboxs = function(){
	if(!$("input:checkbox").get(0).checked){
		$("input:checkbox").get(1).checked = false;
		$("input:checkbox").get(1).disabled = true;
	}else{
		$("input:checkbox").get(1).disabled = false;
	}
}

jQuery(document).ready(function(){
	$("input:checkbox").click(check_checkboxs);
});

	