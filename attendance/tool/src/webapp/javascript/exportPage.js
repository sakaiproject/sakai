$(document).ready(function(){
    $('input[type="file"]').change(function(e){
        if (this.files.length > 0) {
	    $("#import-button").prop('disabled', false);
        }
    });
})
