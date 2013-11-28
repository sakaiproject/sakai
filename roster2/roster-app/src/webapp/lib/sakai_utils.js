/**
 * Copyright (c) 2008-2010 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * Adrian Fish (a.fish@lancaster.ac.uk)
 */

var SakaiUtils;

(function() {

	if(SakaiUtils == null) {
		SakaiUtils = new Object();
    }

	SakaiUtils.getProfileMarkup = function(userId, callback) {

		jQuery.ajax( {
	       	url : "/direct/profile/" + userId + "/formatted",
	       	dataType : "html",
			cache: false,
		   	success : function(p) {
				callback(p);
			},
			error : function(xmlHttpRequest,stat,error) {
				alert("Failed to get profile markup. Status: " + stat + ". Error: " + error);
			}
	   	});
	}
	
	SakaiUtils.getCurrentUserPermissions = function(siteId,scope) {
		var permissions = null;
		var permissionsUrl;
		if (undefined === scope) {
			permissionsUrl = "/direct/site/" + siteId + "/userPerms.json";
		} else {
			permissionsUrl = "/direct/site/" + siteId + "/userPerms/" + scope + ".json";
		}
		
		jQuery.ajax( {
	 		url : permissionsUrl,
	   		dataType : "json",
	   		async : false,
	   		cache : false,
		   	success : function(perms,status) {
				permissions = perms.data;
			},
			error : function(xmlHttpRequest,stat,error) {
				alert("Failed to get the current user permissions. Status: " + stat + ". Error: " + error);
			}
	  	});
	  	
	  	return permissions;
	}

	SakaiUtils.getSitePermissionMatrix = function(siteId, callback) {

        jQuery.ajax( {
            url : "/direct/site/" + siteId + "/perms/roster.json",
            dataType : "json",
            cache: false,
            success : function(p) {
                var perms = [];
                for(role in p.data) {
                    var permSet = {'role':role};

                    for(var i=0,j=p.data[role].length;i<j;i++) {
                        var perm = p.data[role][i].replace(/\./g,"_");
                        eval("permSet." + perm + " = true");
                    }

                    perms.push(permSet);
                }
                callback(perms);
            },
            error : function(xmlHttpRequest,stat,error) {
                alert("Failed to get permissions. Status: " + stat + ". Error: " + error);
            }
        });
    }

	SakaiUtils.savePermissions = function(siteId,checkboxClass,callback) {
        var boxes = $('.' + checkboxClass);
        var myData = {};
        for(var i=0,j=boxes.length;i<j;i++) {
            var box = boxes[i];
            if(box.checked)
                myData[box.id] = 'true';
            else
                myData[box.id] = 'false';
        }

        jQuery.ajax( {
            url : "/direct/site/" + siteId + "/setPerms",
            type : 'POST',
            data : myData,
            timeout: 30000,
            dataType: 'text',
            success : function(result) {
                callback();
            },
            error : function(xmlHttpRequest,status,error) {
                alert("Failed to save permissions. Status: " + status + '. Error: ' + error);
            }
        });
    }
	
	SakaiUtils.renderTrimpathTemplate = function(templateName,contextObject,output) {

		var templateNode = document.getElementById(templateName);
		var firstNode = templateNode.firstChild;
		var template = null;

		if ( firstNode && ( firstNode.nodeType === 8 || firstNode.nodeType === 4)) {
  			template = templateNode.firstChild.data.toString();
        } else {
   			template = templateNode.innerHTML.toString();
        }

		var trimpathTemplate = TrimPath.parseTemplate(template,templateName);

   		var render = trimpathTemplate.process(contextObject);

		if (output) {
			document.getElementById(output).innerHTML = render;
        }

		return render;
	}

}) ();
