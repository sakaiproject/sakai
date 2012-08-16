var savecite = savecite || {};

savecite.init = function(){
	$('.removeCitation').on('click', function(eventObject) {
		var row = $(eventObject.target).closest('tr');
		var citationId = $(row).find('.citationId').text();
		var collectionId = $(row).find('.collectionId').text();
		
		$.ajax({
			url: '/' + collectionId + '/' + citationId,
			type: 'DELETE',
			data: 'citationId=' + citationId + '&collectionId=' + collectionId,
			success: function(data, textStatus, jqXHR) {
				row.empty();
				row.remove();
				alert('success ' + data);
			},
			error: function(jqXHR, textStatus, errorThrown) {
				alert('error ' + jqXHR);
			}
		});
		return false;
	});
};

$(document).ready(function() {
	savecite.init();
});

