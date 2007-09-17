if(!dojo._hasResource["dojox.charting.tests.Theme"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["dojox.charting.tests.Theme"] = true;
dojo.provide("dojox.charting.tests.Theme");
dojo.require("dojox.charting.Theme");

(function(){
	var dxc=dojox.charting;

	tests.register("dojox.charting.tests.Theme", [
		function testDefineColor(t){
			var h=45;
			var args={ hue:h, num:16 };
			var a=dxc.Theme.defineColors(args);
			var s="<table border=1>";
			for(var i=0; i<a.length; i++){
				if(i%8==0){
					if(i>0) s+="</tr>";
					s+="<tr>";
				}
				s+='<td width=16 bgcolor='+a[i]+'>&nbsp;</td>';
			}
			s+="</tr></table>";
			doh.debug(s);

			var args={ hue:h, num:32 };
			var a=dxc.Theme.defineColors(args);
			var s="<table border=1 style=margin-top:12px;>";
			for(var i=0; i<a.length; i++){
				if(i%8==0){
					if(i>0) s+="</tr>";
					s+="<tr>";
				}
				s+='<td width=16 bgcolor='+a[i]+'>&nbsp;</td>';
			}
			s+="</tr></table>";
			doh.debug(s);

			var args={ hue:h, saturation:20, num:32 };
			var a=dxc.Theme.defineColors(args);
			var s="<table border=1 style=margin-top:12px;>";
			for(var i=0; i<a.length; i++){
				if(i%8==0){
					if(i>0) s+="</tr>";
					s+="<tr>";
				}
				s+='<td width=16 bgcolor='+a[i]+'>&nbsp;</td>';
			}
			s+="</tr></table>";
			doh.debug(s);

			var args={ hue:h, low:10, high:90, num:32 };
			var a=dxc.Theme.defineColors(args);
			var s="<table border=1 style=margin-top:12px;>";
			for(var i=0; i<a.length; i++){
				if(i%8==0){
					if(i>0) s+="</tr>";
					s+="<tr>";
				}
				s+='<td width=16 bgcolor='+a[i]+'>&nbsp;</td>';
			}
			s+="</tr></table>";
			doh.debug(s);
		}
	]);
})();

}
