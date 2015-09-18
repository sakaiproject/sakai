$(function() {
    $("#grouplist").hide();
    if ($('#grouplist input').size() == 0) {
	$('#editgroups').hide();
    }
    $("#editgroups").click(function() {
         $("#grouplist").show();
         $("#editgroups").hide();
    });
});

