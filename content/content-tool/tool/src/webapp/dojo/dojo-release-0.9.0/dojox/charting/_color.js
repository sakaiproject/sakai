if(!dojo._hasResource["dojox.charting._color"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["dojox.charting._color"] = true;
dojo.provide("dojox.charting._color");

dojox.charting._color={};
dojox.charting._color.fromHsb=function(/* int */hue, /* int */saturation, /* int */brightness){
	//	summary
	//	Creates an instance of dojo.Color based on HSB input (360, %, %)
	hue=Math.round(hue);
	saturation=Math.round((saturation/100)*255); 
	brightness=Math.round((brightness/100)*255);

	var r, g, b;
	if(saturation==0){
		r=g=b=brightness;
	} else {
		var tint1=brightness, 
			tint2=(255-saturation)*brightness/255, 
			tint3=(tint1-tint2)*(hue%60)/60;
		if(hue<60){ r=tint1, g=tint2+tint3, b=tint2; }
		else if(hue<120){ r=tint1-tint3, g=tint1, b=tint2; }
		else if(hue<180){ r=tint2, g=tint1, b=tint2+tint3; }
		else if(hue<240){ r=tint2, g=tint1-tint3, b=tint1; }
		else if(hue<300){ r=tint2+tint3, g=tint2, b=tint1; }
		else if(hue<360){ r=tint1, g=tint2, b=tint1-tint3; }
	}

	r=Math.round(r); g=Math.round(g); b=Math.round(b);
	return new dojo.Color({ r:r, g:g, b:b });
};

dojox.charting._color.toHsb=function(/* int|Object|dojo.Color */ red, /* int? */ green, /* int? */blue){
	//	summary
	//	Returns the color in HSB representation (360, %, %)
	var r=red,g=green,b=blue;
	if(dojo.isObject(red)){
		r=red.r,g=red.g,b=red.b;
	}
	var min=Math.min(r,g,b);
	var max=Math.max(r,g,b);
	var delta=max-min;

	var hue=0, saturation=(max!=0?delta/max:0), brightness=max/255;
	if(saturation==0){ hue=0; }
	else {
		if(r==max){ hue=((max-b)/delta)-((max-g)/delta); }
		else if(g==max){ hue=2+(((max-r)/delta)-((max-b)/delta)); }
		else { hue=4+(((max-g)/delta)-((max-r)/delta)); }
		hue/=6;
		if(hue<0) hue++;
	}
	
	hue=Math.round(hue*360);
	saturation=Math.round(saturation*100);
	brightness=Math.round(brightness*100);
	return { 
		h:hue, s:saturation, b:brightness,
		hue:hue, saturation:saturation, brightness:brightness
	};	//	Object
};

}
