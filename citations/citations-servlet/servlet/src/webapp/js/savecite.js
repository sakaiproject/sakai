var savecite = savecite || {};

savecite.init = function() {
	$('.removeCitation').on('click', function(eventObject) {
		var row = $(eventObject.target).closest('li');
		var citationId = $(row).find('.citationId').text();
		var resourceId = $('#resourceId').text();
		$.ajax({
			url: '/savecite/' + resourceId + '/' + citationId,
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
	$('#removeAllCitations').on('click', function(eventObject){
		var resourceId = $('#resourceId').text();
		$('.citationId').each(function(index, element){
			var citationId = $(element).text();
			var row = $(element).closest('li');
			$('#removeAllCitations').ajaxStart(function(eventObject){
				$('#removeAllCitations').ajaxStop(function(eventObject){
					window.close();
				});
			});
			$.ajax({
				url: '/savecite/' + resourceId + '/' + citationId,
				type: 'DELETE',
				success: function(data, textStatus, jqXHR) {
					row.empty();
					row.remove();
				},
				error: function(jqXHR, textStatus, errorThrown) {
					alert('error ' + jqXHR);
				}
			});
		});
	});
};

$(document).ready(function() {
	savecite.init();
});

