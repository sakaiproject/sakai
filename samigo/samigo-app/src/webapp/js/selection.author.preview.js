function selectionAuthor(className, anchor){
	var x1, x2, y1, y2;
	
	var anchorJObj = $('#'+anchor);
			
	var div = document.createElement('div');
	div.className = className.selectionClass;
	anchorJObj.append(div);
	
	var divJObj = $(div);
	
	var span = document.createElement('span');
	span.className = className.textClass;
	divJObj.append(span);
	
	var spanObj = $(span);

	
	this.setText = function(text) {
		spanObj.html(text);
	}	
	
	this.setCoords = function(coords){
		if(coords != null && coords.x1 != null && coords.y1 != null && coords.x2 != null && coords.y2 != null)
		{			
			x1 = coords.x1;
			x2 = coords.x2;
			y1 = coords.y1;
			y2 = coords.y2;
			
			move();
		}
	}
	
	function move(){
		// Calculate the div select rectancle for positive and negative values
		var TOP = parseInt(Math.max(0, (y1 < y2) ? y1 : y2));
		var LEFT = parseInt(Math.max(0, (x1 < x2) ? x1 : x2));
		var WIDTH = (x1 < x2) ? x2 - x1 : x1 - x2;
		var HEIGHT = (y1 < y2) ? y2 - y1 : y1 - y2;
			
		// Use CSS to place your select div
		divJObj.css({
			position: 'absolute',
			zIndex: 5000,
			left: LEFT,
			top: TOP,
			width: WIDTH,
			height: HEIGHT
		});
		divJObj.show();
	}
}