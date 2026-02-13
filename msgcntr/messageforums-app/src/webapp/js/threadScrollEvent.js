window.numRead = 0;

window.addEventListener("load", () => {
	const items = Array.from(document.querySelectorAll('[id]')).filter(it => /^mi-\d+ti-\d+fi-\d+$/.test(it.id));
	const totalWidth = items.length;
	window.numRead = 0;
	let ticking = false;

	//Only make changes when access from the tool or load the tool
	if (showThreadChanges === "true") {
		const conversations = items.map(item => ({
			conversation: item,
			read: false
		}));

		//update with the read
		conversations.forEach(item => {
			const mnew = item.conversation.querySelector(".messageNew");
			if(!mnew) {item.read=true;}
		});

		const notRead = conversations.filter(item => item.read === false);
		window.numRead = totalWidth - notRead.length;

		updateBar(window.numRead, totalWidth);

		const scroller = document.querySelector(".portal-main-container.container-fluid.pt-4")
		const scrollerfn = () => {
			if (!ticking) {
				// Throttle the event to "do something" every 2000ms
				setTimeout(() => {
					elementsInViewPort(notRead, scroller);
					ticking = false;
				}, 2000);

				ticking = true;
			}
		}
		if (scroller) {
			scroller.addEventListener("scroll", scrollerfn);
			scrollerfn();
		}
	} else {
		updateBar(window.numRead, totalWidth);
	}

	function elementsInViewPort(items, scroller) {
		items.forEach(item => {
			const p = item.conversation.querySelector(".textPanel p");
			const toolBar = item.conversation.querySelector(".itemToolBar");
			const mnew = item.conversation.querySelector(".messageNew");
			const parts = item.conversation.id.split(/mi-|ti-|fi-/).filter(Boolean);
			const [mi, ti] = parts;
			if (mnew && !item.read && isBottomInViewport(item.conversation, scroller)) {
				doAjaxRead(mi, ti, toolBar);
				item.read=true;
				window.numRead++;
				updateBar(window.numRead, totalWidth);
			}
		});
	}

});

function isBottomInViewport(item, scroller) {
	const rect = item.getBoundingClientRect();
	const viewport = scroller?.getBoundingClientRect?.() ?? {
		top: 0,
		bottom: window.innerHeight || document.documentElement.clientHeight
	};
	return rect.bottom <= viewport.bottom && rect.bottom >= viewport.top;
}

function updateBar(currentRead, total) {
	const elem = document.getElementById("myBar");
	const pct = Math.round(((currentRead*100)/total)) || 0;
	elem.style.width = pct + '%'; 
	document.getElementById("progress-value").innerHTML = statRead + ' '+ currentRead + '/' + total + ' - ' + pct + '%';
}
