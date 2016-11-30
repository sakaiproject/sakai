$(function() {
	var editFolderPath = $( "input[name='edit-folder-path']" ).val();
	var relativePath = $('#folder-path').val();
	if( relativePath !== undefined){
		$('#active-div').show();
		$('.listing').attr('data-directory', relativePath).folderListing({
			onFolderEvent: function(folderCollectionId) {
				var folder = folderCollectionId.attr('rel');
				var directory = getDirectoryFromPath(folder);
				updatePathInput(directory);
			},
			onFileEvent: function(file) {
				window.open(file);
			},
			displayRootDirectory: true,
		});
	}

	if( editFolderPath !== undefined){
		updatePathInput(editFolderPath);
	}
});
function updatePathInput(data){
	$('#active-folder').val(data);
	$('#folder-path').val(data);
}