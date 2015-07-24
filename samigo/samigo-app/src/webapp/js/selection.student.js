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
	
	var divJObj = $(div);
	divJObj.mousedown(function(e){ return false; });


	// select frame (playground :D)
	anchorJObj.mousedown(function(e) {
		//alert('click : '+isActive);
		if(isActive) 
		{
			x = parseInt(e.pageX-anchorJObj.offset().left-(divJObj.width()/2));
			y = parseInt(e.pageY-anchorJObj.offset().top-(divJObj.height()/2));
			
			//x = e.pageX-(divJObj.width()/2);
			//y = e.pageY-(divJObj.height()/2);
					
			move();
		}
	});

	
	
	this.getId = function(){
		return id;
	}
	
	this.setActive = function(val){
		//alert('set to : '+val);
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