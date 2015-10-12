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
		return {x: x, y: y};
	}
	
	this.setCoords = function(coords){
		if(coords != null && coords.x != null && coords.y != null)
		{
			x = coords.x;		
			y = coords.y;
			
			move();
		}
	}
	
	this.remove = function()
	{
		divJObj.remove();
	}
	
	function move()
	{
		divJObj.css({
			position: 'absolute',
			zIndex: 5000,
			left: x,
			top: y
		});
		divJObj.show();		
	}
}