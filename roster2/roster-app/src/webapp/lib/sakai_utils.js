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

(function()
{
	if(SakaiUtils == null)
		SakaiUtils = new Object();
		
	SakaiUtils.getCurrentUser = function() {
		var user = null;
		jQuery.ajax( {
	 		url : "/direct/user/current.json",
	   		dataType : "json",
	   		async : false,
	   		cache : false,
		   	success : function(u) {
				user = u;
			},
			error : function(xmlHttpRequest,stat,error) {
				alert("Failed to get the current user. Status: " + stat + ". Error: " + error);
			}
	  	});

		return user;
	}

	SakaiUtils.showSearchResults = function(tool,siteId,searchTerms) {
		var results = [];

    	jQuery.ajax( {
			url : "/direct/search.json?tool=" + tool + "&contexts=" + siteId + "&searchTerms=" + searchTerms,
        	dataType : "json",
        	async : false,
			cache: false,
        	success : function(r) {
        		results = r["search_collection"];
        	},
        	error : function(xmlHttpRequest,status,error) {
				alert("Failed to search. Status: " + status + ". Error: " + error);
			}
		});

		return results;
	}

	SakaiUtils.getProfileMarkup = function(userId) {
		var profile = '';

		jQuery.ajax( {
	       	url : "/direct/profile/" + userId + "/formatted",
	       	dataType : "html",
	       	async : false,
			cache: false,
		   	success : function(p) {
				profile = p;
			},
			error : function(xmlHttpRequest,stat,error) {
				alert("Failed to get profile markup. Status: " + stat + ". Error: " + error);
			}
	   	});

		return profile;
	}
	
	SakaiUtils.readCookie = function(name) {
    	var nameEQ = name + "=";
    	var ca = document.cookie.split(';');
    	for(var i=0;i < ca.length;i++) {
        	var c = ca[i];
        	while (c.charAt(0)==' ') c = c.substring(1,c.length);
        	if (c.indexOf(nameEQ) == 0) return c.substring(nameEQ.length,c.length);
    	}
    	return null;
	}
	
	SakaiUtils.getParameters = function() {
		var arg = new Object();
		var href = document.location.href;

		var paramString = '';
		
		if (href.indexOf( "?") != -1) {
			var paramString = href.split( "?")[1];
		}
		else {
			var paramString = SakaiUtils.readCookie('sakai-tool-params');
		}
			
		if(paramString.indexOf("#") != -1)
			paramString = paramString.split("#")[0];
				
		var params = paramString.split("&");

		for (var i = 0; i < params.length; ++i) {
			var name = params[i].split( "=")[0];
			var value = params[i].split( "=")[1];
			arg[name] = unescape(value);
		}
	
		return arg;
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

	SakaiUtils.getSitePermissionMatrix = function(siteId,scope) {
        var perms = [];

        jQuery.ajax( {
            url : "/direct/site/" + siteId + "/perms/" + scope + ".json",
            dataType : "json",
            async : false,
            cache: false,
            success : function(p) {
                for(role in p.data) {
                    var permSet = {'role':role};

                    for(var i=0,j=p.data[role].length;i<j;i++) {
                        var perm = p.data[role][i].replace(/\./g,"_");
                        eval("permSet." + perm + " = true");
                    }

                    perms.push(permSet);
                }
            },
            error : function(xmlHttpRequest,stat,error) {
                alert("Failed to get permissions. Status: " + stat + ". Error: " + error);
            }
        });

        return perms;
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
            async : false,
            dataType: 'text',
            success : function(result) {
                callback();
            },
            error : function(xmlHttpRequest,status,error) {
                alert("Failed to save permissions. Status: " + status + '. Error: ' + error);
            }
        });

        return false;
    }
	
	SakaiUtils.renderTrimpathTemplate = function(templateName,contextObject,output) {
		var templateNode = document.getElementById(templateName);
		var firstNode = templateNode.firstChild;
		var template = null;

		if ( firstNode && ( firstNode.nodeType === 8 || firstNode.nodeType === 4))
  			template = templateNode.firstChild.data.toString();
		else
   			template = templateNode.innerHTML.toString();

		var trimpathTemplate = TrimPath.parseTemplate(template,templateName);

   		var render = trimpathTemplate.process(contextObject);

		if (output)
			document.getElementById(output).innerHTML = render;

		return render;
	}

	SakaiUtils.setupFCKEditor = function(textarea_id,width,height,toolbarSet,siteId) {
		var oFCKeditor = new FCKeditor(textarea_id);

		oFCKeditor.BasePath = "/library/editor/FCKeditor/";
		oFCKeditor.Width  = width;
		oFCKeditor.Height = height;
		oFCKeditor.ToolbarSet = toolbarSet;
		
		var collectionId = "/group/" + siteId + "/";
		
		oFCKeditor.Config['ImageBrowserURL'] = oFCKeditor.BasePath + "editor/filemanager/browser/default/browser.html?Connector=/sakai-fck-connector/filemanager/connector&Type=Image&CurrentFolder=" + collectionId;
		oFCKeditor.Config['LinkBrowserURL'] = oFCKeditor.BasePath + "editor/filemanager/browser/default/browser.html?Connector=/sakai-fck-connector/filemanager/connector&Type=Link&CurrentFolder=" + collectionId;
		oFCKeditor.Config['FlashBrowserURL'] = oFCKeditor.BasePath + "editor/filemanager/browser/default/browser.html?Connector=/sakai-fck-connector/filemanager/connector&Type=Flash&CurrentFolder=" + collectionId;
		oFCKeditor.Config['ImageUploadURL'] = oFCKeditor.BasePath + "/sakai-fck-connector/filemanager/connector?Type=Image&Command=QuickUpload&Type=Image&CurrentFolder=" + collectionId;
		oFCKeditor.Config['FlashUploadURL'] = oFCKeditor.BasePath + "/sakai-fck-connector/filemanager/connector?Type=Flash&Command=QuickUpload&Type=Flash&CurrentFolder=" + collectionId;
		oFCKeditor.Config['LinkUploadURL'] = oFCKeditor.BasePath + "/sakai-fck-connector/filemanager/connector?Type=File&Command=QuickUpload&Type=Link&CurrentFolder=" + collectionId;

		oFCKeditor.Config['CurrentFolder'] = collectionId;

		oFCKeditor.Config['CustomConfigurationsPath'] = "/library/editor/FCKeditor/config.js";
		oFCKeditor.ReplaceTextarea();
	}
	
}) ();
