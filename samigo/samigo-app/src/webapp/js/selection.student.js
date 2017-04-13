function selectionStudent(id_, className, anchor){
	var x, y;
	
	var id = id_;

	//Variable indicates wether a mousedown event within your select happend or not
	var isActive = false;
	
	var anchorJObj = $('#'+anchor);
				
	var div = document.createElement('div');
	div.setAttribute('id', id);
	div.className = className;
	anchorJObj.append(div);
	var img = anchorJObj.find('img');
	
	var divJObj = $(div);
	divJObj.mousedown(function(e){ return false; });


	// select frame (playground :D)
	img.mousedown(function(e) {
		if(isActive) 
		{
			x = parseInt(e.pageX + anchorJObj.scrollLeft() - anchorJObj.offset().left - (divJObj.width()/2));
			y = parseInt(e.pageY + anchorJObj.scrollTop() - anchorJObj.offset().top - (divJObj.height()/2));

			move();
		}
	});

	
	
	this.getId = function(){
		return id;
	}
	
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
	
	this.reset = function()
	{
		divJObj.hide();
		x = null;
		y = null;
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