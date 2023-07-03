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