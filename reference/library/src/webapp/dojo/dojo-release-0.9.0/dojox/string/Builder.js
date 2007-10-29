if(!dojo._hasResource["dojox.string.Builder"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["dojox.string.Builder"] = true;
dojo.provide("dojox.string.Builder");

(function(){
	dojox.string.Builder = function(/*String?*/str){
		// summary:
		//		A fast buffer for creating large strings
		// str: The initial string to seed the buffer with
		this.b = dojo.isIE ? [] : "";
		if(str){ this.append(str); }
	};
	
	var m = {
	 	append: function(/*String*/s){ 
			// summary: Append all arguments to the end of the buffer 
			return this.appendArray(dojo._toArray(arguments)); // dojox.string.Builder
		},
		appendArray: function(/*Array*/strings) {
			this.b = String.prototype.concat.apply(this.b, strings);
			return this;
		},
		clear: function(){
			// summary: Remove all characters from the buffer
			this._clear();
			this.length = 0;
			return this;
		},
		replace: function(oldStr,newStr){
			// summary: Replace instances of one string with another in the buffer
			var s = this.toString();
			s = s.replace(oldStr,newStr);
			this._reset(s);
			this.length = s.length;
			return this;
		},
		remove: function(start, len){
			// summary: Remove len characters starting at index start
			if(len == 0){ return this; }
			var s = this.toString();
			this.clear();
			if(start > 0){
				this.append(s.substring(0, start));
			}
			if(start+len < s.length){
				this.append(s.substring(start+len));
			}
			return this;
		},
		insert: function(index, str){
			// summary: Insert string str starting at index
			var s = this.toString();
			this.clear();
			if(index == 0){
				this.append(str);
				this.append(s);
				return this;
			}else{
				this.append(s.substring(0, index));
				this.append(str);
				this.append(s.substring(index));
			}
			return this;
		},
		toString: function(){
			return this.b;
		},
		_clear: function(){
			this.b = "";
		},
		_reset: function(s){
			this.b = s;
		}
	}; // will hold methods for Builder
	
	if(dojo.isIE){
		dojo.mixin(m, {
			toString: function(){ 
				// Summary: Get the buffer as a string
				return this.b.join(""); 
			},
			appendArray: function(strings){
				this.b = this.b.concat(strings);
				return this;
			},
			_clear: function(){
				this.b = [];
			},
			_reset: function(s){
				this.b = [ s ];
			}
		});
	}
	
	dojo.extend(dojox.string.Builder, m);
})();

}
