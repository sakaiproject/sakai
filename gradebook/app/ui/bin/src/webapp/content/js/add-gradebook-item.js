check_checkboxs = function(){
	if(!$("input:checkbox[@name='release']").get(0).checked){
		$("input:checkbox[@name='course_grade']").get(0).checked = false;
		$("input:checkbox[@name='course_grade']").get(0).disabled = true;
	}else{
		$("input:checkbox[@name='course_grade']").get(0).disabled = false;
	}
}

jQuery(document).ready(function(){
    check_checkboxs();
	$("input:checkbox[@name='release'], input:checkbox[@name='course_grade']").click(check_checkboxs);
});

	