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

	const itemId = document.querySelector("input[name='itemId']")?.value;

	if (itemId && itemId != -1) {
		const folderConditionPicker = document.getElementById("folder-condition-picker");
		folderConditionPicker.setAttribute("item-id", itemId);
		folderConditionPicker.classList.remove("hidden");
	} else {
		console.error("Could not get itemId for folder-condition-picker");
	}
});
function updatePathInput(data){
	$('#active-folder').val(unescape(data));
	$('#folder-path').val(data);
}
