var Dirty = function()
{
	/**
	 * Collect all fossils on the page along with the user input fields
	 */
	function collectFossils()
	{
		var fossilex =  /^(.*)-fossil$/;
		var fossils = {};
		if (elements)
		{
			jQuery(':input').each(function()
			{
				var element = elements[i];
				// see if name exists and matches regex
				if (this.name)
				{
					var matches = this.name.match(fossilex);
					if (matches != null)
					{
						// use the name sans '-fossil' to store the element
						// this saves having to parse the field name again
						// later in processing.
						fossils[matches[1]] = this;
					}
				}
			});
		}
		return fossils;
	}

	return {
		isDirty : function()
		{
			var dirty = false;
			var fossilElements = collectFossils();
			var inputs = [];
			for (propName in fossilElements)
			{
				var fossilElement = fossilElements[propName];
				var fossil = RSF.parseFossil(fossilElement.value);
				jQuery(':' + propName).each(function()
				{
					if (((this.type == 'checkbox') && (this.checked != (fossil.oldvalue == "true")))
							|| ((this.type == 'select') && (this.options[this.selectedIndex].value != fossil.oldvalue))
							|| ((this.type == 'radio') && (this.checked) && (this.value != fossil.oldvalue))
							|| ((this.type == 'text' || this.type == 'textarea' || this.type == 'password') && (this.value != fossil.oldvalue)))
					{
						dirty = true;
						// return false to make jQuery stop processing loop
						return false;
					}
				});
				if (dirty) break;
			}
			return dirty;
		},

		check: function(msg)
		{
			var retval = true;
			if(Dirty.isDirty())
			{
				retval = confirm(msg);
			}
			return retval;
		}
	}; // end return
}(); // end namespace
