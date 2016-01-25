function DynamicList(baseId_, templateId_, className_, anchor_)
{
	var this_ = this;
	
	this.baseId = baseId_;
	this.lastId = baseId_;
	this.templateId = templateId_;
	
	this.className = className_;
	this.anchor = anchor_;
	
	this.row = templateId_.replace('imageMapTemplate_','');
	this.count = 0;
		
	this.selectionList = [];
	this.lastActive = null;
	
	this.fillElements = function()
	{
		var elements = $('#'+this.baseId).val().split('#-#');
		if(elements != null)
		{
			for(var i=0; i < elements.length; i++)
			{
				var tokens = elements[i].split('#:#');
				if(tokens != null && tokens.length >= 2)
				{				
					try{
						
						var newSelection = this.addElement(tokens[0], tokens[1], true);
						
						if(tokens.length == 3)
						{
							var coords = jQuery.parseJSON(tokens[2]);
							newSelection.setCoords(coords);
						}
					}catch(err){}					
				}
			}
			//Set the first Element Active
			setActive('sel_'+this.row+'0');
		}
	}
	
	this.serializeElements = function()
	{
		var value = '';
		$('div[id^=dlContainer_'+this.row+']').each(function(){
			var id = this.id.replace('dlContainer_', '');
			if(value != '')
				value += '#-#';
				
			var coords_str = '';
			if(this_.selectionList['sel_'+id] != undefined)
			{
				var coords = this_.selectionList['sel_'+id].getCoords();	
				coords_str += '{"x":'+coords.x;
				coords_str += ',"y":'+coords.y+'}';
			}
			value += $(this).find('input[name=id_]').val()+"#:#"+coords_str;
		});
		$('#'+this.baseId).val(value);
		return value;
	}
	
	this.validateElements = function()
	{
		for(var key in this_.selectionList)
		{
			if(this_.selectionList[key] != null && !jQuery.isFunction(this_.selectionList[key]))
			{
				var coords = this_.selectionList[key].getCoords();
				if(coords == null)
					return 0;
					
				if(coords.x === null || coords.x === undefined || 
					coords.y === null || coords.y === undefined)
					return 0;
			}
		}	
		return 1;
	}
	
	this.resetElements = function()
	{
		for(var key in this_.selectionList)
		{		
			if(this_.selectionList[key] != null && !jQuery.isFunction(this_.selectionList[key]))
				this_.selectionList[key].reset();
		}
	}
	
	this.getObject = function(id_, value_, checked_)
	{	
		//var clonedObj = $('#'+this.templateId).clone();
		var tempClone = document.getElementById(this.templateId).cloneNode(true);
		var clonedObj = $(tempClone);
		clonedObj.attr('id', 'dlContainer_'+this.row+this.count);	
		
		if(id_ != undefined)
			clonedObj.find('input[name=id_]').val(id_);	

		clonedObj.find('span[name=position_]').html('Item '+(this.count+1));	
		
		var valueObj = clonedObj.find('span[name^=value_]');
		valueObj.attr('id', 'value_'+this.row+this.count);
		valueObj.html((value_ != undefined) ? value_ : '');
		
		var buttonObj = clonedObj.find('#btnSelect_');
		buttonObj.attr('id', 'btnSelect_'+this.row+this.count);
		if(checked_ === true)
		{
			buttonObj.addClass('selected_button');
			
			setActive('sel_'+this.row+this.count);
		}
		buttonObj.click(function(){
			setActive(this.id.replace('btnSelect_','sel_'));
		});
		clonedObj.show();
				
		return clonedObj;
	}
	
	this.addElement = function(id_, value_, checked_)
	{
		var newSelection = new selectionStudent('sel_'+this.row+this.count, this.className, this.anchor);
		this_.selectionList['sel_'+this.row+this.count] = newSelection;
		newSelection.setText(this.count+1);
		
		var clonedObj = this.getObject(id_, value_, checked_);
		$('#'+this.lastId).after(clonedObj);
		this.lastId = clonedObj.attr('id');		
		this.count++;	
		
		return newSelection;
	}
	
	var setActive = function(index){
		if(this_.lastActive != null)
		{
			if(this_.lastActive.getId() == index) return;
			this_.lastActive.setActive(false);
			$('#'+this_.lastActive.getId().replace('sel_', 'btnSelect_')).removeClass('selected_button').addClass('non_selected_button');
		}
			
		this_.lastActive = this_.selectionList[index];
		if(this_.lastActive == undefined) this_.lastActive = null;					
		if(this_.lastActive != null)
		{
			this_.lastActive.setActive(true);
			$('#'+this_.lastActive.getId().replace('sel_', 'btnSelect_')).removeClass('non_selected_button').addClass('selected_button');
		}
	}
}	