const KEYCODE = {
	UP: 38,
	DOWN: 40,
	LEFT: 37,
	RIGHT: 39,
	ENTER: 13,
	TAB: 9,
	ESC: 27
};

function showError(text){
	let errorElem = document.getElementById('exception_error');
	errorElem.textContent = text;
	errorElem.classList.remove('hidden');
	setTimeout(() => { errorElem.classList.add('hidden'); }, 5000);
}

async function loadItems(refreshSection, sortBy, treeView) {

	let loadingElem = document.getElementById('loading-container');
	loadingElem.classList.remove('hidden');
	
	let elem = document.getElementById('body-container');
	elem.innerHTML = '';

	let url = [[@{/items}]];

	let params = {
		refreshSection: refreshSection,
		sortBy: sortBy,
		treeView: treeView
	}
	url += '?' + ( new URLSearchParams( params ) ).toString();

	let response = await fetch(url);
	if(!response.ok){
		showError([[#{error.items}]]);
		loadingElem.classList.add('hidden');
		return;
	}
	let body = await response.text();
	if(body != ''){
		elem.innerHTML = body;
		
		loadingElem.classList.add('hidden');

		let items = document.getElementsByClassName('empty-thumbnail');

		Array.from(items).forEach((el) => {
			getThumbnail(el);
		});
	}
}

async function getThumbnail(elem){
	let baseURL = [[@{/thumbnail/}]];
	let url = baseURL + elem.dataset.elemid;

	let response = await fetch(url);
	if(!response.ok){
		showError('[(#{error.thumbnail})]');
		return;
	}
	let thumbnail = await response.text();
	if(thumbnail !== ''){
		const img = document.createElement('img');
		img.src = thumbnail;
		elem.replaceChildren(img);
	}
}

async function openLink(elem){
	if(!elem.classList.contains("fa-spinner")) {
		let baseURL = [[@{/link/}]];
		let url = baseURL + elem.dataset.elemid;
		
		elem.classList.remove('fa-play-circle');
		elem.classList.add('fa-spinner', 'fa-spin');
		
		let response = await fetch(url);
		if(!response.ok){
			showError('[(#{error.link})]');
			elem.classList.remove('fa-spinner', 'fa-spin');
			elem.classList.add('fa-play-circle');
			return;
		}
		let link = await response.text();
		elem.classList.remove('fa-spinner', 'fa-spin');
		elem.classList.add('fa-play-circle');
		window.open(link, "_blank");
	}
}

async function loadInfo(elemid, title){
	let baseURL = [[@{/info/}]];
	let url = baseURL + elemid;

	let response = await fetch(url);
	if(!response.ok){
		showError('[(#{error.info})]');
		return;
	}
	let contentHtml = await response.text();

	let elem = document.getElementById('info-modal');
	elem.querySelector('#body-container').innerHTML = contentHtml;
	elem.querySelector('#info-label').textContent = title;

	$('#info-modal').modal('show');
}

function doSearch(elem){
	let searchValue = elem.value.toLowerCase();
	document.getElementById('accordion_all').querySelectorAll('.video-item').forEach((item) => {
		item.classList.remove('hidden');
		if(searchValue){
			let text = item.querySelector('.video-title').textContent.toLowerCase();
			if (!text.includes(searchValue)) {
				item.classList.add('hidden');
			}
		}
	});
}

function toggleView(){
	document.querySelectorAll('.container-view').forEach((item) => {
		if(item.classList.contains("in")){
			item.classList.remove("in");
		} else {
			item.classList.add("in");
			//focus first button
			item.querySelector('[role="button"]').focus();
		}
	});
}

function toggleFolder(typeId, elemId){
	event.preventDefault();
	document.getElementById("section_tree_"+typeId).querySelectorAll('.folder-row').forEach((row) => {
		row.classList.remove('in');
	});
	document.getElementById(elemId).classList.add('in');
}

function onKeyDown(event) {
	//check where we are
	let elem = event.target;

	//navigate table rows
	if(elem.classList.contains('video-item')) {
		event.stopPropagation();

		switch (event.keyCode) {
			case KEYCODE.RIGHT:
				event.preventDefault();
				focusNextItem();
				break;
			case KEYCODE.LEFT:
				event.preventDefault();
				focusPreviousItem();
				break;
			case KEYCODE.UP:
				event.preventDefault();
				focusPreviousRowItem();
				break;
			case KEYCODE.DOWN:
				event.preventDefault();
				focusNextRowItem();
				break;
			case KEYCODE.ENTER:
				event.preventDefault();
				focusFirstButton();
				break;
		}
	}
	
	//navigate buttons inside video-item
	else if(elem.getAttribute('role') == 'button' && elem.closest('.video-item')) {
		event.stopPropagation();
		
		switch (event.keyCode) {
			case KEYCODE.RIGHT:
			case KEYCODE.LEFT:
			case KEYCODE.UP:
			case KEYCODE.DOWN:
				event.preventDefault();
				focusNextButton();
				break;
			case KEYCODE.TAB:
			case KEYCODE.ESC:
				event.preventDefault();
				focusParentItem();
				break;
			case KEYCODE.ENTER:
				event.preventDefault();
				elem.click();
				break;
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

//video-items
function focusNextItem() {
	const elem = document.activeElement;
	if (elem.nextElementSibling && elem.nextElementSibling.classList.contains('video-item')) {
		activateItem(elem.nextElementSibling);
	}
}

function focusPreviousItem() {
	const elem = document.activeElement;
	if (elem.previousElementSibling && elem.previousElementSibling.classList.contains('video-item')) {
		activateItem(elem.previousElementSibling);
	}
}

function getRowSize(elem) {
	let parentElem = elem.closest('.row');
	let firstElem = parentElem.querySelector('.video-item');
	return countEndRight(firstElem) + 1;
}

function countEndRight(elem){
	if (elem.nextElementSibling && elem.nextElementSibling.classList.contains('video-item') && elem.nextElementSibling.offsetLeft > elem.offsetLeft){
		return countEndRight(elem.nextElementSibling) + 1;
	}
	return 0;
}

function focusNextRowItem() {
	let elem = document.activeElement;
	let count = getRowSize(elem);
	let index = 0;
	
	while(index < count){
		if (elem.nextElementSibling && elem.nextElementSibling.classList.contains('video-item')) {
			elem = elem.nextElementSibling;
		}
		index++;
	}
	activateItem(elem);
}

function focusPreviousRowItem() {
	let elem = document.activeElement;
	let count = getRowSize(elem);
	let index = 0;
	
	while(index < count){
		if (elem.previousElementSibling && elem.previousElementSibling.classList.contains('video-item')) {
			elem = elem.previousElementSibling;
		}
		index++;
	}
	activateItem(elem);
}

//in a row, select first child button
function focusFirstButton() {
	let elem = document.activeElement;
	
	let buttons = elem.querySelectorAll('[role="button"]');
	let btn = buttons[0];
	
	if(buttons.length > 1) {
		btn.tabIndex = 0;
		btn.focus();
	} else {
		btn.click();
	}
}

//inside video-item
function focusNextButton() {
	const elem = document.activeElement;
	const parentElem = elem.closest('.video-item');
	
	let buttons = parentElem.querySelectorAll('[role="button"]');
	if(buttons.length > 1) {
		buttons.forEach((btn) => {
			btn.tabIndex = -1;
			if(btn != elem){
				btn.tabIndex = 0;
				btn.focus();
			}
		});
	} else {
		elem.tabIndex = 0;
		elem.focus();
	}
}

function focusParentItem() {
	const elem = document.activeElement;
	let item = elem.closest('.video-item');
	activateItem(item);
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

function activateItem(elem) {
	// reset all items
	let parentElem = elem.closest('.row');
	parentElem.querySelectorAll('.video-item').forEach((item) => {
		item.tabIndex = -1;
		item.querySelectorAll('[role="button"]').forEach((btn) => btn.tabIndex = -1 );
	});
	
	// Make the current item "active"
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
