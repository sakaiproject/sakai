var savecite = savecite || {};

savecite.init = function() {
	$('.removeCitation').on('click', function(eventObject) {
		var row = $(eventObject.target).closest('tr');
		var citationId = $(row).find('.citationId').text();
		var collectionId = $(row).find('.collectionId').text();
		$.ajax({
			url: '/savecite/' + collectionId + '/' + citationId,
			type: 'DELETE',
			success: function(data, textStatus, jqXHR) {
				row.empty();
				row.remove();
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

