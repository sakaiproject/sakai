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
		}
	}
	
	this.serializeElements = function()
	{
		return '';
	}
	
	this.validateElements = function()
	{	
		return 1;
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
		
		clonedObj.show();
				
		return clonedObj;
	}
	
	this.addElement = function(id_, value_, checked_)
	{
		var newSelection = new selectionStudent(this.className, this.anchor);
		this_.selectionList['sel_'+this.row+this.count] = newSelection;
		newSelection.setText(this.count+1);
		
		var clonedObj = this.getObject(id_, value_, checked_);
		$('#'+this.lastId).after(clonedObj);
		this.lastId = clonedObj.attr('id');		
		this.count++;	
		
		return newSelection;
	}
}