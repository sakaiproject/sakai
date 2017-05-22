function DynamicList(baseId_, templateId_, className_, anchor_)
{
	var this_ = this;
	
	this.baseId = baseId_;
	this.lastId = baseId_;
	this.templateId = templateId_;
	
	this.className = className_;
	this.anchor = anchor_;

	this.row = 0;
	this.count = 0;
		
	this_.selectionList = [];
	this_.lastActive = null;
	
	this.fillElements = function()
	{
		var elements = $('#'+this.baseId).val().split('#-#');
		if(elements != null)
		{
			for(var i=0; i < elements.length; i++)
			{
				var tokens = elements[i].split('#:#');
				if(tokens != null && tokens.length >= 1)
				{
					var newSelection = this.addElement(tokens[0], true);
					if(tokens.length == 2)
					{
						try{
							var coords = jQuery.parseJSON(tokens[1]);
							
							newSelection.setCoords(coords);
						}catch(err){}
					}
				}
			}
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
				if(coords !== null)
				{
					coords_str += '{"x1":'+coords.x1;
					coords_str += ',"y1":'+coords.y1;
					coords_str += ',"x2":'+coords.x2;
					coords_str += ',"y2":'+coords.y2+"}";
				}
			}
			value += $('#value_'+id).val()+"#:#"+coords_str;
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
					
				if(coords.x1 === null || coords.x1 === undefined || 
					coords.x2 === null || coords.x2 === undefined || 
					coords.y1 === null || coords.y1 === undefined || 
					coords.y2 === null || coords.y2 === undefined)
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
			{
				var id = key.replace('sel_', '');
				
				var obj = $('#dlContainer_'+id);
				
				this_.selectionList[key].remove();
				delete this_.selectionList[key];
			}
		}
	}
	
	this.reorderElements = function() {
		var i = 1;
		for(var key in this_.selectionList)
		{
			if(this_.selectionList[key] != null && !jQuery.isFunction(this_.selectionList[key]))
			{
				var id = key.replace('sel_', 'dlContainer_');
				$('#'+id).find('span[name=position_]').html(i);
				this_.selectionList[key].setText(i);
				i++;
			}
		}
	}
	
	this.getObject = function(value_, checked_,new_)
	{
		var tempClone = document.getElementById(this.templateId).cloneNode(true);
		var clonedObj = $(tempClone);
		clonedObj.attr('id', 'dlContainer_'+this.row+this.count);		
		
		var valueObj = clonedObj.find('input[name^=value_]');
		valueObj.attr('id', 'value_'+this.row+this.count);
		if (new_){
			valueObj.attr('placeholder', (value_ != undefined) ? value_ : '');
		}else if (value_ == "null"){
			valueObj.attr('placeholder', '');		
		}else{
			valueObj.attr('value', (value_ != undefined) ? value_ : '');
		}
		
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
		
		if(this.count == 0)		
			clonedObj.find('input[name^=btnRemove_]').remove();
		else
			clonedObj.find('input[name^=btnRemove_]').attr('id', 'btnRemove_'+this.row+this.count);
			
		clonedObj.show();
				
		return clonedObj;
	}
	
	this.addElement = function(value_, checked_,new_)
	{
		var newSelection = new selectionAuthor('sel_'+this.row+this.count, this.className, this.anchor);
		this_.selectionList['sel_'+this.row+this.count] = newSelection;

		var clonedObj = this.getObject(value_, checked_,new_);
		$('#'+this.lastId).after(clonedObj);
		this.lastId = clonedObj.attr('id');		
		this.count++;

		this.reorderElements();		
		
		return newSelection;
	}
	
	this.removeElement = function(btnElement)
	{
		var id = btnElement.id.replace('btnRemove_', '');
		var obj = $('#dlContainer_'+id);
		if(obj != null)
		{			
			if(this.lastId == obj.attr('id'))
				this.lastId = obj.prev().attr('id').replace(/:/g, '\\:');			
			obj.remove();
			
			if(this_.selectionList['sel_'+id] != undefined)
			{
				this_.selectionList['sel_'+id].remove();
				delete this_.selectionList['sel_'+id];
			}
			
			this.reorderElements();
		}	
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