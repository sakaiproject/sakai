	
	function setEndtimeMonthDateYear(){
		var year = document.getElementById("meeting:startTime.year").value;
		var month = document.getElementById("meeting:startTime.month").value;
		var day = document.getElementById("meeting:startTime.day").value;
		
		document.getElementById("meeting:endTime.year").value=year;	
		document.getElementById("meeting:endTime.month").value=month;
		document.getElementById("meeting:endTime.day").value=day;
	}

	function currentSiteSelection(){
		var siteId = "meeting:siteSelection";
		var	siteCheckBox = document.getElementById(siteId);
		enableGroupSelection(!siteCheckBox.checked);
	}
			
	function enableGroupSelection(enable){
		var groupIdPrefix = "meeting:currentSiteGroups:"
		var groupIdSuffix = ":groupSelection";
		if(!document.getElementById("meeting:currentSiteGroups"))
			return;	
		var size = document.getElementById("meeting:currentSiteGroups").tBodies[0].rows.length;
		for(var i=0; i<size; i++){
			var groupId = groupIdPrefix+i+groupIdSuffix;
			document.getElementById(groupId).disabled=!enable;
		}
	}
	
	function otherUserSitesSelection(){
		var siteId = "meeting:userSites";
		var sitePrefix = "meeting:userSites:";
		var siteSuffix = ":otherSitesSelection"
		
		var numberOfSites = document.getElementById(siteId).tBodies[0].rows.length;
		for(var i=0; i<numberOfSites; i++){
			var siteId = sitePrefix + i + siteSuffix;
			enableOtherGroupsSelection(sitePrefix + i, !document.getElementById(siteId).checked);
		}
	}
	
	function enableOtherGroupsSelection(prefixId, enable){
		var groupIdPrefix = prefixId + ":userGroups:"
		var groupIdSuffix = ":otherGroupsSelection";
			
		var size = document.getElementById(prefixId+":userGroups").tBodies[0].rows.length;
		for(var i=0; i<size; i++){
			var groupId = groupIdPrefix+i+groupIdSuffix;
			document.getElementById(groupId).disabled=!enable;
		}
	}
	