function selectionStudent(className, anchor){
	var x, y;

	//Variable indicates wether a mousedown event within your select happend or not
	var isActive = false;
	
	var anchorJObj = $('#'+anchor);
				
	var div = document.createElement('div');
	div.className = className;
	anchorJObj.append(div);
	
	var divJObj = $(div);

	
	
	this.setActive = function(val){
		isActive = val;
		div.className = className+((val) ? '_selected' : '');
	}
	
	this.setText = function(text) {
		divJObj.html(text);
	}
	
	this.getCoords = function(){
		return {x: x, y: y, click: true};
	}
	
	this.setCoords = function(coords){
		if(coords != null && coords.x != null && coords.y != null)
		{
			x = coords.x;		
			y = coords.y;
			if (coords.click !== true) {
				var half = markerHalfSize();
				x = x + half.x;
				y = y + half.y;
			}
			move();
		}
	}
	
	this.remove = function()
	{
		divJObj.remove();
	}
	
	function markerHalfSize()
	{
		var hidden = divJObj.css('display') === 'none';
		if (hidden) {
			divJObj.css({ visibility: 'hidden', display: 'block' });
		}
		var half = { x: divJObj.width() / 2, y: divJObj.height() / 2 };
		if (hidden) {
			divJObj.css({ visibility: '', display: 'none' });
		}
		return half;
	}
	
	function move()
	{
		var half = markerHalfSize();
		divJObj.css({
			position: 'absolute',
			zIndex: 5000,
			left: x - half.x,
			top: y - half.y
		});
		divJObj.show();		
	}
}
