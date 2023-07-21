const KEYCODE = {
	UP: 38,
	DOWN: 40,
	LEFT: 37,
	RIGHT: 39,
	ENTER: 13,
	ESC: 27
};

function showError(text) {
	let errorElem = document.getElementById('exception_error');
	errorElem.textContent = text;
	errorElem.classList.remove('hidden');
	setTimeout(() => { errorElem.classList.add('hidden'); }, 5000);
}

function setLoading(elem) {
	let loadingElem = document.getElementById('loading-container').cloneNode(true);
	loadingElem.removeAttribute('id');
	loadingElem.classList.remove('hidden');
	if(elem){
		//hide container content
		elem.classList.add('hidden');
		//save container reference inside spinner
		loadingElem.container = elem;
		//add spinner
		elem.after(loadingElem);
	}
	return loadingElem;
}

function stopLoading(loadingElem) {
	loadingElem.container?.classList.remove('hidden');
	loadingElem.remove();
}

function addDropzone(container){
	//find all dropzone elements inside container
	container.querySelectorAll('.dropzone').forEach((elem) => {
		let teamElem = elem.closest('.team-container');
		let teamContainer = teamElem.querySelector('.panel-body');
		let folderElem = elem.closest('.folder-container');
		
		let params = { }
		if(teamElem) { params['teamId'] = teamElem.id; }
		if(folderElem) { params['itemId'] = folderElem.id; }
		
		let auxMaxSize = ([[${maxUploadSize}]] > 0) ? { maxFilesize: [[${maxUploadSize}]] } : {};
		
		let myDropzone = new Dropzone(elem, {
			params: params,
			parallelUploads: 2,
			uploadMultiple: false,
			createImageThumbnails: false,
			clickable: true,
			...auxMaxSize,
			acceptedFiles: ".doc, .docx, .xls, .xlsx, .ppt, .pptx",
			complete: function(file) {
				myDropzone.removeFile(file);
				
				//last completed file -> reload items
				if(myDropzone.getQueuedFiles().length == 0 && myDropzone.getUploadingFiles().length == 0) {
					if(folderElem){
						loadItems(teamContainer, teamElem.id, folderElem.id);
					} else {
						loadItems(teamContainer, teamElem.id);
					}
				}
			},
			error: function(file, message) {
				let msg = message;
				if(file.accepted && message.body){
					msg = message.body;
				}
				showError(msg);
			},
			dictFallbackMessage: [[#{dragndrop.dictFallbackMessage}]],
			dictFallbackText: [[#{dragndrop.dictFallbackText}]],
			dictFileTooBig: [[#{dragndrop.dictFileTooBig}]],
			dictInvalidFileType: [[#{dragndrop.dictInvalidFileType}]],
			dictFolderUploadError: [[#{dragndrop.dictFolderUploadError}]],
		});
	});	
}

async function loadItems(container, teamId, itemId, url) {
	var containerElem = container;
	if(typeof container === 'string' || container instanceof String) {
		containerElem = document.getElementById(container);
	}
	let spinner = setLoading(containerElem);

	if(!url){
		url = [[@{/items}]];
	}

	let params = { sortBy: document.getElementById('sortBy').value }
	if(teamId) { params['teamId'] = teamId; }
	if(itemId) { params['itemId'] = itemId; }
	url += '?' + ( new URLSearchParams( params ) ).toString();

	let response = await fetch(url);
	if(!response.ok){
		showError([[#{error.items}]]);
		stopLoading(spinner);
		return;
	}
	let body = await response.text();
	stopLoading(spinner);
	if(body != ''){
		containerElem.innerHTML = body;

		addDropzone(containerElem);
	}
}

async function toggleTeam(teamId) {
	let teamElem = document.getElementById(teamId);

	if (teamElem.getElementsByClassName('drive-item').length === 0) {
		loadTeam(teamId);
	}
}

function loadTeam(teamId) {
	let container = document.getElementById(teamId).querySelector('.panel-body');
	loadItems(container, teamId);
}

function refreshTeam(teamId) {
	let container = document.getElementById(teamId).querySelector('.panel-body');
	let url = [[@{/refresh/}]];
	loadItems(container, teamId, null, url + teamId);
}

function loadFolder(elem) {
	let teamElem = elem.closest('.team-container');
	let container = teamElem.querySelector('.panel-body');
	loadItems(container, teamElem.id, elem.dataset.itemid);
}

function sortItems(elem, value) {
	let sortElem = document.getElementById('sortBy');
	let previousValue = sortElem.value;

	sortElem.value = value + ((previousValue.startsWith(value)) ? ((previousValue.endsWith(':0')) ? ':1' : ':0') : ':0');

	let teamElem = elem.closest('.team-container');
	let folderElem = elem.closest('.folder-container');
	let container = teamElem.querySelector('.panel-body');
	if(folderElem){
		loadItems(container, teamElem.id, folderElem.id);
	} else {
		loadItems(container, teamElem.id);
	}
}

function loadNewItemModal(elem, type) {
	let teamElem = elem.closest('.team-container');
	let folderElem = elem.closest('.folder-container');

	let modalElem = document.getElementById('newitem-modal-body-container');
	modalElem.querySelector('#modalTeamId').value = teamElem.id; 
	modalElem.querySelector('#modalFolderId').value = (folderElem) ? folderElem.id : '';
	modalElem.querySelector('#modalType').value = type;
	modalElem.querySelector('#modalName').value = '';

	$('#newitem-modal').modal('show');
}

async function addItem() {
	$('#newitem-modal').modal('hide');

	let modalElem = document.getElementById('newitem-modal-body-container');
	let teamId = modalElem.querySelector('#modalTeamId').value; 
	let itemId = modalElem.querySelector('#modalFolderId').value;
	let type = modalElem.querySelector('#modalType').value;
	let name = modalElem.querySelector('#modalName').value;

	let container = document.getElementById(teamId).querySelector('.panel-body');
	
	let spinner = setLoading(container);

	let url = [[@{/addItem}]];

	let params = { 
		sortBy: document.getElementById('sortBy').value,
		teamId: teamId,
		type: type,
		name: name
	}
	if(itemId != '') { params['itemId'] = itemId; }


	let response = await fetch(url, {
		method: 'POST',
		headers: {
			'Content-Type': 'application/x-www-form-urlencoded'
		},
		body: new URLSearchParams(params)
	});
	if(!response.ok){
		showError([[#{error.adding_item}]]);
		stopLoading(spinner);
		return;
	}
	let body = await response.text();
	stopLoading(spinner);
	if(body != ''){
		container.innerHTML = body;

		addDropzone(container);
	}
}

function toggleDropdown(elem, folder){
	let dropdownElem = elem.querySelector('.dropdown-menu');
	if(!dropdownElem){
		dropdownElem = document.getElementById('itemDropdownMenu').cloneNode(true);
		dropdownElem.removeAttribute('id');
		if(folder){
			dropdownElem.querySelector('.menuitem-delFile')?.remove();
			dropdownElem.querySelector('.menuitem-getLink')?.remove();
		} else {
			dropdownElem.querySelector('.menuitem-delFolder')?.remove();
		}
		elem.appendChild(dropdownElem);
	}

	if(elem.classList.contains("open")) {
		elem.classList.remove('open');
	} else {
		elem.classList.add('open');
	}
}

function loadConfirmDeleteModal(elem) {
	let buttonElem = elem.closest('.dropdown-toggle');
	let teamElem = elem.closest('.team-container');

	let modalElem = document.getElementById('confirm-delete-modal');
	modalElem.querySelector('#modalTeamId').value = teamElem.id; 
	modalElem.querySelector('#modalItemId').value = buttonElem.dataset.itemid;

	$('#confirm-delete-modal').modal('show');
}

async function deleteItem(){
	$('#confirm-delete-modal').modal('hide');

	let modalElem = document.getElementById('confirm-delete-modal-body-container');
	let teamId = modalElem.querySelector('#modalTeamId').value; 
	let itemId = modalElem.querySelector('#modalItemId').value;

	let container = document.getElementById(teamId).querySelector('.panel-body');
	
	let spinner = setLoading(container);

	let url = [[@{/deleteItem}]];

	let params = { 
		teamId: teamId,
		itemId: itemId
	}
	url += '?' + ( new URLSearchParams( params ) ).toString();
	let response = await fetch(url);
	if(!response.ok){
		showError([[#{error.deleting_item}]]);
		stopLoading(spinner);
		return;
	}
	let body = await response.text();
	stopLoading(spinner);
	if(body != ''){
		container.innerHTML = body;

		addDropzone(container);
	}
}

function getLink(elem){
	let parentElem = elem.closest('.drive-item');
	let itemElem = parentElem.querySelector('.icon-container');

	const storage = document.createElement('textarea');
	storage.value = itemElem.href;

	parentElem.appendChild(storage);
	storage.select();
	storage.setSelectionRange(0, 99999);
	document.execCommand('copy');
	parentElem.removeChild(storage);
	
	let infoElem = document.getElementById('info_banner');
	infoElem.classList.remove('hidden');
	setTimeout(() => { infoElem.classList.add('hidden'); }, 5000);
}

function onKeyDown(event) {
	//check where we are
	let elem = event.target;

	//navigate table rows
	if(elem.classList.contains('table-row')) {
		event.stopPropagation();
		
		switch (event.keyCode) {
			case KEYCODE.RIGHT:
			case KEYCODE.ENTER:
				event.preventDefault();
				focusFirstButton(elem);
				break;
			case KEYCODE.DOWN:
				event.preventDefault();
				focusNextRow(elem);
				break;
			case KEYCODE.UP:
				event.preventDefault();
				focusPreviousRow(elem);
				break;
		}
	}

	//navigate buttons in a row
	else if(elem.getAttribute('role') == 'button' && elem.closest('.table-row')) {
		switch (event.keyCode) {
			case KEYCODE.RIGHT:
				event.stopPropagation();
				event.preventDefault();
				focusNextButton();
				break;
			case KEYCODE.LEFT:
				event.stopPropagation();
				event.preventDefault();
				focusPreviousButton();
				break;
			case KEYCODE.DOWN:
				//special case: dropdown menu buton
				if(!elem.classList.contains('dropdown-toggle')){
					event.stopPropagation();
					event.preventDefault();
					focusNextRow(elem.closest('.table-row'));
				}
				break;
			case KEYCODE.UP:
				event.stopPropagation();
				event.preventDefault();
				focusPreviousRow(elem.closest('.table-row'));
				break;
			case KEYCODE.ESC:
				event.stopPropagation();
				event.preventDefault();
				activateRow(elem.closest('.table-row'));
				break;
		}
	}

	//navigate dropdown menu
	else if(elem.getAttribute('role') == 'menuitem') {
		if (event.keyCode == KEYCODE.UP) {
			if (!elem.closest('li').previousElementSibling) {
				event.stopPropagation();
				event.preventDefault();
				focusParentButton(elem);
			}
		}
	}
	
	//navigate buttons in breadcrumbs
	else if(elem.getAttribute('role') == 'button' && elem.closest('.breadcrumb')) {
		event.stopPropagation();
		
		switch (event.keyCode) {
			case KEYCODE.RIGHT:
				event.preventDefault();
				focusNextBreadcrumb();
				break;
			case KEYCODE.LEFT:
				event.preventDefault();
				focusPreviousBreadcrumb();
				break;
		}
	}

	//all other buttons
	else if(elem.getAttribute('role') == 'button') {
		//do click when enter
		if (event.keyCode == KEYCODE.ENTER) {
			elem.click();
		}
	}
}

function focusNextRow(elem) {
	if (elem.nextElementSibling) {
		if(elem.nextElementSibling.classList.contains('table-row')) {
			activateRow(elem.nextElementSibling);
		}
	}
}

function focusPreviousRow(elem) {
	if (elem.previousElementSibling) {
		if(elem.previousElementSibling.classList.contains('table-row')) {
			activateRow(elem.previousElementSibling);
		}
	}
}

//in a row, select first child button
function focusFirstButton(elem) {
	let item = elem.querySelector('[role="button"]');
	item.tabIndex = 0;
	item.focus();
}

function focusNextButton() {
	const elem = document.activeElement;
	if (elem.nextElementSibling) {
		activateButton(elem.nextElementSibling);
	}
}

function focusPreviousButton() {
	const elem = document.activeElement;
	if (elem.previousElementSibling) {
		activateButton(elem.previousElementSibling);
	} else {
		activateRow(elem.closest('.table-row'));
	}
}

//in a dropdown item, select parent button
function focusParentButton(elem) {
	let item = elem.closest('.dropdown-toggle');
	if(item){
		//dropdown menu in table row
		item.classList.remove('open');
		item.parentNode.classList.remove('open');
		activateButton(item);
	} else {
		//dropdown menu in "+ New" button
		item = elem.closest('.dropdown-menu');
		item?.parentNode.classList.remove('open');
		item.previousElementSibling.tabIndex = 0;
		item.previousElementSibling.focus();
	}
}


//<li>
//	<a role="button"> <-- we are here
//</li>
//<li>
//	<a role="button"> <-- we want to go here
//</li>
function focusNextBreadcrumb() {
	const elem = document.activeElement;
	if (elem.parentNode.nextElementSibling) {
		activateBreadcrumb(elem.parentNode.nextElementSibling.querySelector('[role="button"]'));
	}
}

//<li>
//	<a role="button"> <-- we want to go here
//</li>
//<li>
//	<a role="button"> <-- we are here
//</li>
function focusPreviousBreadcrumb() {
	const elem = document.activeElement;
	if (elem.parentNode.previousElementSibling) {
		activateBreadcrumb(elem.parentNode.previousElementSibling.querySelector('[role="button"]'));
	}
}

function activateRow(elem) {
	// reset all rows
	document.getElementById('body-container').querySelectorAll('.table-row').forEach((row) => {
		row.tabIndex = -1;
		row.classList.remove('focused-row');
		row.querySelectorAll('[role="button"]').forEach((btn) => btn.tabIndex = -1 );
	});
	
	// Make the current row "active"
	elem.tabIndex = 0;
	elem.classList.add('focused-row');
	elem.focus();
}

function activateButton(elem) {
	let row = elem.closest('.table-row');
	//reset all buttons in row
	row?.querySelectorAll('[role="button"]').forEach((btn) => btn.tabIndex = -1 );
	
	// Make the current button "active"
	elem.tabIndex = 0;
	elem.focus();
}

function activateBreadcrumb(elem) {
	if(elem){
		let row = elem.closest('.breadcrumb');
		//reset all breadcrumb buttons
		row?.querySelectorAll('[role="button"]').forEach((btn) => btn.tabIndex = -1 );
		
		// Make the current button "active"
		elem.tabIndex = 0;
		elem.focus();
	}
}
