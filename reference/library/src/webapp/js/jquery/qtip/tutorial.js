var sakaiTutorialSkin = 'sakaiTutorial';
var sakaiTutorialStartUrl = "/direct/tutorial/introToSakai_p1.json";
var sakaiTutorialLocationUrl = '/direct/tutorial/introToSakai_pTutorialLocation.json';
var optsCache;
var maxWidth = 500;
var previousClicked = false;
//create sakai tutorial style skin
$.fn.qtip.styles.sakaiTutorial = { // Last part is the name of the style
		width: { max: 800 },
		padding: '14px',
		border: {
			width: 9,
			radius: 9,
			color: '#666666'
		},
		tip: {
			color: '#6699CC'
		},
		name: 'light' // Inherit the rest of the attributes from the preset dark style
}

function startTutorial(opts){
	showTutorialPage(sakaiTutorialStartUrl, opts);
}

function showTutorialPage(url, opts){
	//store options in cache so we can use the same options from start to end of the tutorial:
	if(opts != null)
		optsCache = opts;
	else if(optsCache != null)
		opts = optsCache;
	else{
		opts = {};
		optsCache = opts;
	}
	
	$.getJSON(url, 
			function(response){
				try{
				if(response.data.dialog == 'true'){
					response.data.selection = 'div#tutorial';
				}
				if(!$(response.data.selection).length 
						&& ((!previousClicked && response.data.nextUrl) 
								|| (previousClicked && response.data.previousUrl))){
					//this item doesn't exist, go to the next page if it exists
					if(previousClicked){
						showTutorialPage(response.data.previousUrl);
					}else{
						showTutorialPage(response.data.nextUrl);
					}
				}else{
					previousClicked = false;
					var mxWidth = maxWidth;
					var totalWidth = $(document).width();
					var totalHeight = $(document).height();
					if(totalWidth < mxWidth){
						mxWidth = totalWidth;
					}
					
					if(response.data.dialog == 'true'){
						$(response.data.selection).qtip(
								{
									content: {
										title: {
											text: response.data.title,
											button: '<a href="#" onclick="if(\''+opts.showTutorialLocationOnHide + '\' == \'true\' && \'' + url + '\' != \'' + sakaiTutorialLocationUrl + '\'){showTutorialPage(\''+ sakaiTutorialLocationUrl + '\');}"><img src="/library/image/silk/cancel.png"/></a>'
										},
										text: response.data.body
									},

									position: {
										target: $(document.body), // Position it via the document body...
										corner: 'center' // ...at the center of the viewport
									},
									show: {
										ready: true, // Show it when ready
										solo: true // And hide all other tooltips
									},
									hide: false,
									style: {
										name: sakaiTutorialSkin,
										width: {
											max: mxWidth
										}
									},
									api: {
										onHide: function()
										{
											// javascript to run after hiding
											$(response.data.selection).qtip("destroy");
										}
										
									}
								});
					}else{
						//not dialog:
						$(response.data.selection).qtip(
								{ 
									content: {
										title: {
											text: response.data.title,
											button: '<a href="#" onclick="if(\''+opts.showTutorialLocationOnHide + '\' == \'true\' && \'' + url + '\' != \'' + sakaiTutorialLocationUrl + '\'){showTutorialPage(\''+ sakaiTutorialLocationUrl + '\');}"><img src="/library/image/silk/cancel.png"/></a>'
										},
										text: response.data.body
									},
									position: {
										corner: {
											tooltip: response.data.positionTooltip,
											target: response.data.positionTarget
										}
									},
									style: {
											name: sakaiTutorialSkin,
											tip: true,
											width: {
												max: mxWidth
											}
									},
									show: {
										ready: true, // Show it when ready
										solo: true // And hide all other tooltips
									},
									hide: false,
									api: {
										onHide: function()
										{
											// javascript to run after hiding
											$(response.data.selection).qtip("destroy");
										},
										onShow: function()
										{
											if(response.data.fadeout){
												$(".qtip").delay(10000).fadeOut(2000, function() {
												    // Animation complete.
													$(response.data.selection).qtip("destroy");
												});
											}
										}
									}
								}
						);
					}
				}
				}catch(e){
				//	$(this).qtip("destroy");
				}
		}
	);
}
