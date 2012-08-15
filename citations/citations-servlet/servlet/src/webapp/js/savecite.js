var savecite = savecite || {};

savecite.init = function(){
	$('#removeCitation').click(function(eventObject){
		var citationId = $(eventObject.target).siblings('.citationId').text();
		var collectionId = $(eventObject.target).siblings('.collectionId').text();
		$.ajax({
			url: '',
			method: 'DELETE',
			data: {
				'citationId': citationId,
				'collectionId': collectionId
			},
			success: function(data, textStatus, jqXHR) {
				alert('success ' + data);
			},
			error: function(jqXHR, textStatus, errorThrown) {
				alert('error ' + jqXHR);
			}
		});
	});
	
	alert("document is ready now");
	
};

$(document).ready(function(){
	savecite.init();
};