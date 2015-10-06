var checkStatus = false;
var delbutton;
function msg(s) {
    var m = document.getElementById(s);
    if (m == null) {
       return s;
    }else
       return m.innerHTML;;
};
$(function() {
		$('#delete-confirm').dialog({
            autoOpen: false,
            resizable: false,
                modal: true,
            dialogClass: "no-close",
                buttons: [{text:msg("simplepage.delete"),
                          click: function() {
                          checkStatus = true;
                          delbutton.click();
                      }},{text:msg("simplepage.cancel_message"),
                          click: function() {
                          $( this ).dialog( "close" );}}
                ]
        });

		$('#delete-comments-item').click(function(event) {
			// edit row is set by edit-comments. We're current in the dialog. need
			// to look in the actual page row.
			if (editrow.find('.commentDiv').size() == 0)
			    return true;
			delbutton = $('#delete-comments-item');
			return delete_confirm(event, msg("simplepage.deletecommentsubmissionexist"));
		    });

		$('#delete-student-item').click(function(event) {
			// edit row is set by edit-comments. We're current in the dialog. need
			// to look in the actual page row.
			if (editrow.find('.studentLink').size() == 0)
			    return true;
			delbutton = $('#delete-student-item');
			return delete_confirm(event, msg("simplepage.deletestudentsubmissionexist"));
		    });
		    
		$('.del-item-link').attr('title', msg("simplepage.delete-item"));
		
        $('.del-item-link').click(function(event) {
            // edit row is set by edit-comments. We're current in the dialog. need
            // to look in the actual page row.
            $("#delete-item-itemid").val($(this).parents("li").find("span.itemid").text());
            delbutton = $('#delete-item-button');
            return delete_confirm(event, msg("simplepage.delete_page_confirm"));
        });
		$('#delete-item, #delete-multimedia-item, #delete-youtube-item, #delete-movie-item, #delete-question-item, #delete-text-item').click(function(event) {
			delbutton = $(this);
			return delete_confirm(event, msg("simplepage.delete_page_confirm"));
		});

});

function delete_confirm(event, message) {
    if (checkStatus) {
        checkStatus = false;
        $("#delete-confirm").dialog('close');
        return true;
    }
    checkStatus = false;
    $("#delete-confirm-message").text(message);
    $("#delete-confirm").dialog("option", "position", [event.pageX, event.pageY-100]);
    $("#delete-confirm").dialog('open');
    return false;
};
