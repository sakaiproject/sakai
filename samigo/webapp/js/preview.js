//Returns String format of ISO date 
function parseISODate(parameterDate)
	{	var duration="";
		pattern =  /^P((\d*)Y)?((\d*)M)?((\d*)W)?((\d*)D)?T?((\d*)H)?((\d*)M)?((\d*)S)?/;
		//	var m = pattern.exec("P2Y3M4W5DT1H4M10S");
		var m = pattern.exec(parameterDate);
		if (m!=null)
		{
		if (m[2] != ""){duration = m[2]+ " Year(s) ";	}
		if (m[4] != ""){duration = duration + m[4]+ " Month(s) ";}
		if (m[6] != ""){duration = duration + m[6]+ " Week(s) ";}
		if (m[8] != ""){duration = duration + m[8]+ " Day(s) ";	}
		if (m[10] != ""){duration = duration + m[10]+ " Hour(s) ";}
		if (m[12] != ""){duration = duration + m[12]+ " Minute(s) ";}
		if (m[14] != ""){duration = duration + m[14]+ " Second(s) ";}
		}
	
		return duration;
    }