$(function() {
	var relativePath = $('#folder-path').val();
	if( relativePath !== undefined){
		$('#active-div').show();
		$('.listing').attr('data-directory', relativePath).folderListing({
			onFolderEvent: function(folderCollectionId) {
				var folder = folderCollectionId.attr('rel');
				var directory = getDirectoryFromPath(folder);
				if (directory==='//') {
					directory = '/' + folderCollectionId.text() + '/';
				}
				else {
					var topFolder = encodeURIComponent($('#top-folder').text());
					directory = '/' + topFolder + directory;
				}
				updatePathInput(directory);
			},
			onFileEvent: function(file) {
				window.open(file);
			},
			displayRootDirectory: true
		});
		updatePathInput('/' + encodeURIComponent($('#top-folder').text()) + '/');

	}
});
function updatePathInput(data){
	$('#active-folder').val(unescape(data));
	$('#folder-path').val(data);
}
