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
 * Daniel Robinson (d.b.robinson@lancaster.ac.uk)
 * Adrian Fish (a.fish@lancaster.ac.uk)
 */
var ADMIN = 'admin';

var DEFAULT_GROUP_ID = 'all';
var DEFAULT_ENROLLMENT_STATUS = 'All';
var DEFAULT_SORT_LIST = [[0,0]];

var STATE_OVERVIEW = 'overview';
var STATE_PICTURES = 'pics';
var STATE_GROUP_MEMBERSHIP = 'group_membership';
var STATE_ENROLLMENT_STATUS = 'status'
var STATE_VIEW_PROFILE = 'profile';
var STATE_PERMISSIONS = 'permissions';

var SORT_NAME = 'sortName';
var SORT_DISPLAY_ID = 'displayId';
var SORT_EMAIL = 'email';
var SORT_ROLE = 'role';
var SORT_STATUS	= "status";
var SORT_CREDITS	= "credits";

var columnSortFields = [];

/* Stuff that we always expect to be setup */
var language = null;
var rosterSiteId = null;
var rosterCurrentUserPermissions = null;
var rosterCurrentUser = null;

var rosterProfileUrl = null;

// so we can return to the previous state after viewing permissions
var rosterLastStateNotPermissions = null;

// These are default behaviours, and are global so the tool remembers
// the user's choices.
var grouped = roster_group_ungrouped;
var hideNames = false;
var viewSingleColumn = false;
var groupToView = null;
var groupToViewText = roster_sections_all;
var enrollmentSetToView = null;
var enrollmentSetToViewText = null;
var enrollmentStatusToViewText = roster_enrollment_status_all;

// sakai.properties
var defaultSortColumn = SORT_NAME;
var firstNameLastName = false;
var hideSingleGroupFilter = false;
var viewEmail = true;
var viewUserDisplayId = true;
// end of sakai.properties

var sortColumn = null;
var overviewSortParams = null;
var groupSortParams = null;
var enrollmentSortParams = null;

// sortEnd is used to update this so we know which column and direction the
// tables are sorted in when exporting
var currentSortColumn = 0;
var currentSortDirection = 0;

// tablesorter parser for URLs
$.tablesorter.addParser({
	id: 'urls',is: function(s) { return false; },
	format: function(s) { return s.replace(new RegExp(/<.*?>/),""); },
	type: 'text'
});

/* New Roster2 functions */
(function() {
		
	// We need the toolbar in a template so we can swap in the translations
	SakaiUtils.renderTrimpathTemplate('roster_navbar_template', {},
			'roster_navbar');
	
	$('#navbar_overview_link').bind('click', function(e) {
		return switchState(STATE_OVERVIEW);
	});

	$('#navbar_pics_link').bind('click', function(e) {
		return switchState(STATE_PICTURES);
	});

	$('#navbar_group_membership_link').bind('click', function(e) {
		return switchState(STATE_GROUP_MEMBERSHIP);
	});
	
	$('#navbar_enrollment_status_link').bind('click', function(e) {
		return switchState(STATE_ENROLLMENT_STATUS);
	});
	
    $('#navbar_permissions_link').bind('click', function(e) {
        return switchState(STATE_PERMISSIONS);
    });
        
	var arg = SakaiUtils.getParameters();
	
	if (!arg || !arg.siteId) {
		alert('The site id  MUST be supplied as a page parameter');
		return;
	}
	rosterSiteId = arg.siteId;

	rosterCurrentUser = SakaiUtils.getCurrentUser();

	if (!rosterCurrentUser) {
		alert("No current user. Have you logged in?");
		return;
	}
	
	setLanguage(arg.language);
	
	getRosterCurrentUserPermissions();
	
	// process sakai.properties
	if (arg.firstNameLastName) {
		if ('true' == arg.firstNameLastName) {
			firstNameLastName = true;
		} else {
			// default = false
			firstNameLastName = false;
		}
	}
			
	if (arg.hideSingleGroupFilter) {
		if ('true' == arg.hideSingleGroupFilter) {
			hideSingleGroupFilter = true;
		} else {
			// default = false
			hideSingleGroupFilter = false;
		}
	}
	
	if (arg.viewEmail) {
		if ('false' == arg.viewEmail) {
			viewEmail = false;
		} else {
			// default = true
			viewEmail = true;
		}
	}
	
	if (arg.viewUserDisplayId) {
		if ('false' == arg.viewUserDisplayId) {
			viewUserDisplayId = false;
		} else {
			// default = true
			viewUserDisplayId = true;
		}
	}
	
	if (arg.defaultSortColumn) {
		
		if (SORT_NAME == arg.defaultSortColumn ||
				SORT_DISPLAY_ID == arg.defaultSortColumn ||
				SORT_ROLE == arg.defaultSortColumn ||
				SORT_STATUS == arg.defaultSortColumn ||
				SORT_CREDITS == arg.defaultSortColumn) {
			
			defaultSortColumn = arg.defaultSortColumn;
		} else if (SORT_EMAIL == arg.defaultSortColumn && true == viewEmail) {
			// if chosen sort is email, check that email column is viewable
			defaultSortColumn = arg.defaultSortColumn;
		}
	}
	
	sortColumn = defaultSortColumn;
	// end of sakai.properties
	
	// build root profile URL (the user ID is appended later for each user)
	rosterProfileUrl = "/sakai-roster-tool/roster.html?state=profile&siteId=" +
		rosterSiteId + "&language="  + language +
		"&defaultSortColumn" + defaultSortColumn +
		"&firstNameLastName=" + firstNameLastName +
		"&hideSingleGroupFilter=" + hideSingleGroupFilter +
		"&viewUserDisplayId=" + viewUserDisplayId +
		"&viewEmail=" + viewEmail + "&userId=";

	if (window.frameElement) {
		window.frameElement.style.minHeight = '600px';
	}
		
	// Now switch into the requested state
	switchState(arg.state, arg);

})();

function switchState(state, arg, searchQuery) {
	
	// so we can return to the previous state after viewing permissions
	if (state != STATE_PERMISSIONS) {
		rosterLastStateNotPermissions = state;
	}
	
	// for export to Excel
	setColumnSortFields(state);
	
	// $('#cluetip').hide();
		
	var site = getRosterSite();

	// permissions
    if (rosterCurrentUserPermissions.siteUpdate) {
        $('#navbar_permissions_link').show();
    } else {
        $('#navbar_permissions_link').hide();
    }
    
    // enrollment
	if (!rosterCurrentUserPermissions.viewEnrollmentStatus ||
			site.siteEnrollmentSets.length === 0) {
		
		$('#navbar_enrollment_status_link').hide();
	}
	
	// hide group membership link if there are no groups
	if (site.siteGroups.length === 0) {
		$('#navbar_group_membership_link').hide();
	}
		
	if (STATE_OVERVIEW === state) {
		
		configureOverviewTableSort();
		
		var members = getMembers(searchQuery, false, state);
		var roles = getRolesUsingRosterMembers(members, site.userRoles);
		
		SakaiUtils.renderTrimpathTemplate('roster_overview_header_template',
				{'siteTitle':site.title,
				'displayTitleMsg':rosterCurrentUserPermissions.viewAllMembers},
				'roster_header');
		
		if (site.siteGroups.length > 0) {
			
			SakaiUtils.renderTrimpathTemplate('roster_section_filter_template',
					{'groupToViewText':groupToViewText,'siteGroups':site.siteGroups},
					'roster_section_filter');
			
		} else {
			
			SakaiUtils.renderTrimpathTemplate('empty_template', {}, 'roster_section_filter');
		}
		
		SakaiUtils.renderTrimpathTemplate('roster_search_with_participants_template',
				{'roleFragments':getRoleFragments(roles),
				'participants':getCurrentlyDisplayingParticipants(roles)},
				'roster_search');
	
	
		SakaiUtils.renderTrimpathTemplate('roster_overview_template',
				{'language':language, 'membership':members, 'siteId':rosterSiteId,
				'groupToView':groupToView, 'firstNameLastName':firstNameLastName,
				'viewEmail':viewEmail, 'viewUserDisplayId':viewUserDisplayId,
				'viewProfile':rosterCurrentUserPermissions.viewProfile},
				'roster_content');
		
		$(document).ready(function() {
			
			readyExportButton(state);
			readySearchButton(state);
			readyClearButton(state);
			readySectionFilter(site, state);
			
			$('#roster_form_rosterTable').tablesorter(overviewSortParams);
			
			$('#roster_form_rosterTable').bind("sortEnd",function() {
				currentSortColumn = this.config.sortList[0][0];
				currentSortDirection = this.config.sortList[0][1];
		    });
		});
		
	} else if (STATE_PICTURES === state) {
		
		var members = getMembers(searchQuery, true, state);
		var roles = getRolesUsingRosterMembers(members, site.userRoles);
		
		SakaiUtils.renderTrimpathTemplate('roster_pics_header_template',
				{'siteTitle':site.title}, 'roster_header');
		
		if (site.siteGroups.length > 0) {
			
			SakaiUtils.renderTrimpathTemplate('roster_section_filter_template',
					{'groupToViewText':groupToViewText,'siteGroups':site.siteGroups},
					'roster_section_filter');
		} else {
			
			SakaiUtils.renderTrimpathTemplate('empty_template', {}, 'roster_section_filter');			
		}
		
		SakaiUtils.renderTrimpathTemplate('roster_search_with_participants_template',
				{'roleFragments':getRoleFragments(roles),
				'participants':getCurrentlyDisplayingParticipants(roles)},
				'roster_search');
		

		SakaiUtils.renderTrimpathTemplate('roster_pics_template',
				{'language':language, 'membership':members, 'siteId':rosterSiteId,
				'currentUserId':rosterCurrentUser.id,
				'groupToView':groupToView, 'viewSingleColumn':viewSingleColumn,
				'hideNames':hideNames,
				'viewUserDisplayId':viewUserDisplayId,
				'viewProfile':rosterCurrentUserPermissions.viewProfile,
				'viewConnections':(undefined != window.friendStatus)},
				'roster_content');
		
		$(document).ready(function() {
			
			readySearchButton(state);
			readyClearButton(state);
			readySectionFilter(site, state);
			
			readyHideNamesButton(state, searchQuery);
			readyViewSingleColumnButton(state, searchQuery);
		});
		
	} else if (STATE_GROUP_MEMBERSHIP === state) {
		
		configureGroupMembershipTableSort();
		
		var members = getRosterMembership(null, null, null, null, state);
		var roles = getRolesUsingRosterMembers(members, site.userRoles);
		
		SakaiUtils.renderTrimpathTemplate('roster_groups_header_template',
				{'siteTitle':site.title,
				'displayTitleMsg':rosterCurrentUserPermissions.viewAllMembers},
				'roster_header');
						
		SakaiUtils.renderTrimpathTemplate('empty_template', {}, 'roster_search');
						
		if (roster_group_bygroup === grouped) {
			
			SakaiUtils.renderTrimpathTemplate('roster_group_section_filter_template',
					{'arg':arg, 'siteId':rosterSiteId}, 'roster_section_filter');

			SakaiUtils.renderTrimpathTemplate('roster_grouped_template',
					{'language':language, 'membership':members,
					'siteGroups':site.siteGroups, 'rolesText':getRolesByGroupRoleFragments(site, members),
					'siteId':rosterSiteId, 'viewUserDisplayId':viewUserDisplayId,
					'viewProfile':rosterCurrentUserPermissions.viewProfile},
					'roster_content');
			
		} else {
			
			SakaiUtils.renderTrimpathTemplate('roster_group_section_filter_with_participants_template',
					{'arg':arg, 'siteId':rosterSiteId,'roleFragments':getRoleFragments(roles),
				'participants':getCurrentlyDisplayingParticipants(roles)}, 'roster_section_filter');
			
			SakaiUtils.renderTrimpathTemplate('roster_ungrouped_template',
					{'language':language, 'membership':members, 'siteId':rosterSiteId,
					'viewUserDisplayId':viewUserDisplayId, 
					'viewProfile':rosterCurrentUserPermissions.viewProfile},
					'roster_content');
		}
		
		$(document).ready(function() {
			
			readyExportButton(state);
			
			$('#roster_form_group_choice').val(grouped);
			$('#roster_form_group_choice').change(function(e) {
				
				grouped = this.options[this.selectedIndex].text;
				
				switchState('group_membership');
			});
			
			$('table').tablesorter(groupSortParams);
		});
		
	} else if (STATE_VIEW_PROFILE === state) {
		
		var profileMarkup = SakaiUtils.getProfileMarkup(arg.userId);
				
		$('#roster_content').html(profileMarkup);
		
	} else if (STATE_ENROLLMENT_STATUS === state) {
				
		configureEnrollmentStatusTableSort();
		
		if (null === enrollmentSetToView && null != site.siteEnrollmentSets[0]) {
			enrollmentSetToView = site.siteEnrollmentSets[0].id;
		}
		
		var enrollment = getEnrolledMembers(searchQuery);
		
		SakaiUtils.renderTrimpathTemplate('roster_enrollment_header_template',
				{'siteTitle':site.title}, 'roster_header');
		
		SakaiUtils.renderTrimpathTemplate('roster_enrollment_section_filter_template',
				{'enrollmentSets':site.siteEnrollmentSets,
				'enrollmentStatusDescriptions':site.enrollmentStatusDescriptions},
				'roster_section_filter');
				
		SakaiUtils.renderTrimpathTemplate('roster_search_with_students_template',
				{'students':getCurrentlyDisplayingStudents(enrollment, null)},
				'roster_search');
		
		SakaiUtils.renderTrimpathTemplate('roster_enrollment_status_template',
				{'language':language, 'enrollment':enrollment, 'enrollmentStatus':enrollmentStatusToViewText,
				'siteId':rosterSiteId, 'firstNameLastName':firstNameLastName,
				'viewEmail':viewEmail,
				'viewProfile':rosterCurrentUserPermissions.viewProfile},
				'roster_content');
				
		$(document).ready(function() {
			
			readyExportButton(state);
			readyEnrollmentFilters(site.siteEnrollmentSets.length);
			
			readySearchButton(state);
			readyClearButton(state);
			
			$('#roster_form_rosterTable').tablesorter(enrollmentSortParams);
			
			$('#roster_form_rosterTable').bind("sortEnd",function() {
				currentSortColumn = this.config.sortList[0][0];
				currentSortDirection = this.config.sortList[0][1];
		    });
		});
	} else if (STATE_PERMISSIONS === state) {
		
		SakaiUtils.renderTrimpathTemplate('roster_permissions_header_template',
				{'siteTitle':site.title}, 'roster_header');
		
		SakaiUtils.renderTrimpathTemplate('empty_template', {}, 'roster_section_filter');
		SakaiUtils.renderTrimpathTemplate('empty_template', {}, 'roster_search');
		
		SakaiUtils.renderTrimpathTemplate('roster_permissions_template',
				{'permissions': SakaiUtils.getSitePermissionMatrix(rosterSiteId, 'roster')},
				'roster_content');
		
        $(document).ready(function() {
            $('#roster_permissions_save_button').bind('click', function() {
               SakaiUtils.savePermissions(rosterSiteId, 'roster_permission_checkbox',
            		   function() { switchState(rosterLastStateNotPermissions) } );
            });
            
            $('#roster_cancel_button').bind('click',
            		function() { switchState(rosterLastStateNotPermissions) } );
        });
	}
}

function getRosterSite() {
	
	var site;

	jQuery.ajax({
    	url : "/direct/roster-membership/" + rosterSiteId + "/get-site.json",
      	dataType : "json",
       	async : false,
		cache: false,
	   	success : function(data) {
			site = data;
			if (undefined == site.siteGroups) {
				site.siteGroups = new Array();
			}
			
			if (undefined == site.userRoles) {
				site.userRoles = new Array();
			}
			
			if (undefined == site.siteEnrollmentSets) {
				site.siteEnrollmentSets = new Array();
			}
		}
	});
	
	return site;
	
}

function getRosterMembership(groupId, sorted, sortField, sortDirection, state) {
	
	var membership;
	
	var url = "/direct/roster-membership/" + rosterSiteId + "/get-membership.json?sorted=" + sorted;
	if (groupId) {
		url += "&groupId=" + groupId;
	}
	
	jQuery.ajax({
    	url : url,
      	dataType : "json",
       	async : false,
		cache: false,
	   	success : function(data) {
			membership = data['roster-membership_collection'];
		},
		error : function() {
			membership = new Array();
		}
	});
	
	// set the profile URLs for each user
	for (var i = 0, j = membership.length; i < j; i++) {
		membership[i].profileUrl = rosterProfileUrl + membership[i].userId
	}
	
	if (STATE_PICTURES === state) {
		for (var i = 0, j = membership.length; i < j; i++) {
			membership[i].profileImageUrl = "/direct/profile/" + membership[i].userId + "/image";
			
			// do we have Profile2 1.4?
			if (undefined != window.friendStatus) {
				membership[i].friendStatus = friendStatus(rosterCurrentUser.id, membership[i].userId);
			}
		}
	}
		
	return membership;
}

function getRosterEnrollment() {
	
	var enrollment;
	var url = "/direct/roster-membership/" + rosterSiteId + "/get-enrollment.json?enrollmentSetId=" + enrollmentSetToView;
	
	jQuery.ajax({
    	url : url,
      	dataType : "json",
       	async : false,
		cache: false,
	   	success : function(data) {
			enrollment = data['roster-membership_collection'];
		},
		error : function() {
			enrollment = new Array();
		}
	});
	
	// set the profile URLs for each user
	for (var i = 0, j = enrollment.length; i < j; i++) {
		enrollment[i].profileUrl = rosterProfileUrl + enrollment[i].userId
	}
	
	return enrollment;
	
}

function getCurrentlyDisplayingParticipants(roles) {
	
	var participants = 0;
	
	for (var i = 0, j = roles.length; i < j; i++) {
		
		participants = participants + roles[i].roleCount;
	}
	
	return currently_displaying_participants.replace(/\{0\}/, participants);
}

function getCurrentlyDisplayingStudents(enrollment, enrollmentType) {
	
	var currentEnrollments = enrollments_currently_displaying.replace(/\{0\}/,
			enrollment.length);
	
	if (enrollmentType) {
		currentEnrollments = currentEnrollments.replace(/\{1\}/, enrollmentType);
	} else {
		// do all needs no string		
		currentEnrollments = currentEnrollments.replace(/\{1\}/, '');
	}
	
	return currentEnrollments;
}

function getRoleFragments(roles) {
	
	var roleFragments = new Array();
	
	for (var i = 0, j = roles.length; i < j; i++) {
				
		var frag = role_breakdown_fragment.replace(/\{0\}/, roles[i].roleCount);
		frag = frag.replace(/\{1\}/, roles[i].roleType);
		
		if (i != j - 1) {
			frag = frag + ", ";
		}
		
		roleFragments[i] = frag;
	}	
	return roleFragments;
}

function getRolesUsingRosterMembers(members, roleTypes) {
	
	var roles = new Array();
		
	for (var i = 0, j = roleTypes.length; i < j; i++) {
		roles[i] = { roleType: roleTypes[i], roleCount: 0 };
	}
	
	for (var i = 0, j = members.length; i < j; i++) {
		
		for (var k = 0, l = roles.length; k < l; k++) {
			
			if (roles[k].roleType === members[i].role) {
				roles[k].roleCount++;
				continue;
			}
		}
	}
	
	// filter out roles with 0 members of that role type
	var rolesToReturn = new Array();
	var rolesCount = 0;
	for (var i = 0, j = roles.length; i < j; i++) {
		
		if (roles[i].roleCount != 0) {
			rolesToReturn[rolesCount] = roles[i];
			rolesCount++;
		}
	}
	
	return rolesToReturn;
}



function getRolesByGroup(site, members) {
	
	var rolesByGroup = new Array();
	
	for (var i = 0, j = site.siteGroups.length; i < j; i++) {
				
		var groupId = site.siteGroups[i].id;

		rolesByGroup[groupId] = new Object();
		rolesByGroup[groupId].groupId = groupId;
		rolesByGroup[groupId].groupTitle = site.siteGroups[i].title;
		rolesByGroup[groupId].roles = new Array();
		
		for (var k = 0, l = members.length; k < l; k++) {
			
			for (var m = 0, n = site.siteGroups[i].userIds.length; m < n; m++) {
								
				if (members[k].userId === site.siteGroups[i].userIds[m]) {
					
					var role = members[k].role;
					
					// if we haven't processed this type of role before, create it
					if (undefined === rolesByGroup[groupId].roles[role]) {
						rolesByGroup[groupId].roles[role] = { 'roleType':role, 'roleCount':0 }
					}
						
					rolesByGroup[groupId].roles[role].roleCount =
						rolesByGroup[groupId].roles[role].roleCount + 1;		
				}
			}
		}
	}
	
	return rolesByGroup;
}

function getRolesByGroupRoleFragments(site, members) {

	var rolesByGroup = getRolesByGroup(site, members);
	
	var rolesByGroupRoleFragments = new Array();
	
	for (var group in rolesByGroup) {
		
		rolesByGroupRoleFragments[group] = new Object();
		
		var participants = 0;
		rolesByGroupRoleFragments[group].roles = new Array();
		
		var numberOfRoles = 0;
		for (var role in rolesByGroup[group].roles) {
			numberOfRoles++;
		}
		
		var roleNumber = 1;
		for (var role in rolesByGroup[group].roles) {
			
			rolesByGroupRoleFragments[group].roles[role] = new Object();
			rolesByGroupRoleFragments[group].roles[role].frag = 
				role_breakdown_fragment.replace(/\{0\}/,
						rolesByGroup[group].roles[role].roleCount);
			
			rolesByGroupRoleFragments[group].roles[role].frag =
				rolesByGroupRoleFragments[group].roles[role].frag.replace(/\{1\}/,
						rolesByGroup[group].roles[role].roleType);
			
			if (roleNumber != numberOfRoles) {
				rolesByGroupRoleFragments[group].roles[role].frag = 
					rolesByGroupRoleFragments[group].roles[role].frag + ", ";
			}
								
			participants = participants + rolesByGroup[group].roles[role].roleCount;
			
			roleNumber++;
		}
		
		
		rolesByGroupRoleFragments[group].participants =
			currently_displaying_participants.replace(/\{0\}/, participants);
	}
	
	return rolesByGroupRoleFragments;
}

function getMembers(searchQuery, sorted, state) {
		
	var members;
	
	// view all users
	if (groupToViewText === roster_sections_all ||
			groupToViewText === roster_section_sep_line) {
		
		members = getRosterMembership(null, sorted, null, null, state);			
	// view a specific group (note: search is done within group if selected)
	} else {
		members = getRosterMembership(groupToView, sorted, null, null, state);
	}

	if (searchQuery) {
		return getMembersFromSearchQuery(members, searchQuery);		
	} else {
		return members;
	}
}

function getEnrolledMembers(searchQuery) {
	// TODO pass enrollment status required?
	var enrollment = getRosterEnrollment();
		
	if (searchQuery) {
		return getMembersFromSearchQuery(enrollment, searchQuery);		
	} else {
		return enrollment;
	}

}

function getMembersFromSearchQuery(members, searchQuery) {
	
	var membersToReturn = new Array();
	var memberCount = 0;
	
	for (var i = 0, j = members.length; i < j; i++) {
						
		if (members[i].displayName.toLowerCase().indexOf(searchQuery) >= 0 ||
				members[i].displayId.toLowerCase().indexOf(searchQuery) >= 0) {
								
			membersToReturn[memberCount] = members[i];
			memberCount++;
		}
	}
	
	return membersToReturn;
}

function readyClearButton(state) {
	
	$('#roster_form_clear_button').bind('click', function(e) {
		switchState(state);
	});
}

function readyExportButton(viewType) {
		
	$('#export_button').bind('click', function(e) {
	
		e.preventDefault();
		
		var baseUrl = "/direct/roster-export/" + rosterSiteId +
			"/export-to-excel?viewType=" + viewType +
			"&sortField=" + columnSortFields[currentSortColumn] +
			"&sortDirection=" + currentSortDirection;
		
		var facetParams = "&facetName=" + facet_name +
			"&facetUserId=" + facet_userId +
			"&facetEmail=" + facet_email +
			"&facetRole=" + facet_role +
			"&facetGroups=" + facet_groups +
			"&facetStatus=" + facet_status +
			"&facetCredits=" + facet_credits;
		
		if (STATE_OVERVIEW === viewType) {
			var groupId = null;
			if (null != groupToView) {
				groupId = groupToView;
			} else {
				groupId = DEFAULT_GROUP_ID;
			}
			
			window.location.href = baseUrl + "&groupId=" + groupId + facetParams;

		} else if (STATE_GROUP_MEMBERSHIP === viewType) {
		
			var byGroup = false;
			if (grouped === roster_group_bygroup) {
				byGroup = true;
			}
			
			window.location.href = baseUrl + "&byGroup=" + byGroup + facetParams;
			
		} else if (STATE_ENROLLMENT_STATUS === viewType) {
		
			var enrollmentStatus = null;
			if (enrollmentStatusToViewText == roster_enrollment_status_all) {
				enrollmentStatus = DEFAULT_ENROLLMENT_STATUS;
			} else {
				enrollmentStatus = enrollmentStatusToViewText;
			}
			
			window.location.href = baseUrl + 
				"&enrollmentSetId=" + enrollmentSetToView +
				"&enrollmentStatus=" + enrollmentStatus +
				facetParams;
		}
	});
		
	// hide export button if necessary
	if (STATE_OVERVIEW === viewType || STATE_GROUP_MEMBERSHIP === viewType || 
			STATE_ENROLLMENT_STATUS === viewType) {
		
		if (rosterCurrentUserPermissions.rosterExport) {
			$('#export_button').show();
		} else {
			$('#export_button').hide();
		}
	}
}

function readySearchButton(state) {
	
	$('#roster_form_search_button').bind('click', function(e) {
		
		var searchFieldValue = document.forms['roster_form'].elements['roster_form_search_field'].value;

		if (searchFieldValue != roster_search_text && searchFieldValue != "") {
			
			searchQuery = searchFieldValue.toLowerCase();
			switchState(state, null, searchQuery);
		}
	});
}

function readySectionFilter(site, state) {
	
	if (site.siteGroups.length > 0) {
		
		$('#roster_form_section_filter').val(groupToViewText);
		$('#roster_form_section_filter').change(function(e) {
			
			if (this.options[this.selectedIndex].value != roster_section_sep_line) {
				
				groupToView = this.options[this.selectedIndex].value;
				groupToViewText = this.options[this.selectedIndex].text;
		
				switchState(state);
			}
		});
	}
}

function readyEnrollmentFilters(numberOfEnrollmentSets) {
			
	if (numberOfEnrollmentSets > 0) {
		
		$('#roster_form_enrollment_set_filter').val(enrollmentSetToViewText);
		$('#roster_form_enrollment_set_filter').change(function(e) {
			enrollmentSetToView = this.options[this.selectedIndex].value;
			enrollmentSetToViewText = this.options[this.selectedIndex].text;
			
			switchState(STATE_ENROLLMENT_STATUS);
		});
	}
	
	$('#roster_form_enrollment_status_filter').val(enrollmentStatusToViewText);
	$('#roster_form_enrollment_status_filter').change(function(e) {
		
		enrollmentStatusToViewText = this.options[this.selectedIndex].text;
				
		switchState(STATE_ENROLLMENT_STATUS);
	});
	
}

function readyHideNamesButton(state, searchQuery) {

	$('#roster_form_hide_names').bind('click', function(e) {
		
		if (true === hideNames) {
			hideNames = false;
		} else {
			hideNames = true;
		}
		
		switchState(state, null, searchQuery);
	});
}

function readyViewSingleColumnButton(state, searchQuery) {
	
	$('#roster_form_pics_view').bind('click', function(e) {
		
		if (true === viewSingleColumn) {
			viewSingleColumn = false;
		} else {
			viewSingleColumn = true;
		}
		
		switchState(state, null, searchQuery);
	});
}

function getRosterCurrentUserPermissions() {
		
	if (rosterCurrentUser.id === ADMIN) {
		
		var data = ['roster.export',
				'roster.viewallmembers',
				'roster.viewenrollmentstatus',
				'roster.viewgroup',
				'roster.viewhidden',
				'roster.viewprofile',
				'site.upd'];

		rosterCurrentUserPermissions = new RosterPermissions(data);
		
	} else {
		rosterCurrentUserPermissions = new RosterPermissions(
			SakaiUtils.getCurrentUserPermissions(rosterSiteId));		
	}
	
}

function configureOverviewTableSort() {
	
	// having a URL sorter on non-URLs work fine, so no need to check
	
	// user display ID has view profile URL attached to it
	if (true === viewUserDisplayId) {
		if (true === viewEmail) {
			overviewSortParams = {headers:{1: {sorter:'urls'}, 2: {sorter:'urls'}}, sortList:DEFAULT_SORT_LIST};
		} else {
			overviewSortParams = {headers:{1: {sorter:'urls'}}, sortList:DEFAULT_SORT_LIST};
		}
	} else {
		if (true === viewEmail) {
			overviewSortParams = {headers:{0: {sorter:'urls'}, 1: {sorter:'urls'}}, sortList:DEFAULT_SORT_LIST};
		} else {
			overviewSortParams = {headers:{}, sortList:DEFAULT_SORT_LIST};
		}
	}
	
	// now set the initial sort column
	if (SORT_NAME === sortColumn) {
		overviewSortParams.sortList = [[0,0]];
	} else if (SORT_DISPLAY_ID === sortColumn) {
		
		if (true === viewUserDisplayId) {
			overviewSortParams.sortList = [[1,0]];
		}
		
	} else if (SORT_EMAIL === sortColumn) {
		
		if (true === viewEmail) {
			
			if (true === viewUserDisplayId) {
				overviewSortParams.sortList = [[2,0]];
			} else {
				overviewSortParams.sortList = [[1,0]];
			}
		}
		
	} else if (SORT_ROLE === sortColumn) {
	
		if (true === viewEmail) {
			
			if (true === viewUserDisplayId) {
				overviewSortParams.sortList = [[3,0]];
			} else {
				overviewSortParams.sortList = [[2,0]];
			}
		} else {
			
			if (true === viewUserDisplayId) {
				overviewSortParams.sortList = [[2,0]];
			} else {
				overviewSortParams.sortList = [[1,0]];
			}
		}
	}
}

function configureGroupMembershipTableSort() {
	// group membership has user display ID but no email column
		
	if (true === viewUserDisplayId) {
		// user ID column (1) has view profile URL attached to it
		groupSortParams = {headers:{1: {sorter:'urls'}, 3: {sorter:false}}, sortList:DEFAULT_SORT_LIST};
	} else {
		// user name column (0) has view profile URL attached to it
		groupSortParams = {headers:{0: {sorter:'urls'}, 2: {sorter:false}}, sortList:DEFAULT_SORT_LIST};
	}
	
	// now set the initial sort column
	if (SORT_NAME === sortColumn) {
		groupSortParams.sortList = [[0,0]];
	} else if (SORT_DISPLAY_ID === sortColumn) {
		
		if (true === viewUserDisplayId) {
			groupSortParams.sortList = [[1,0]];
		}
	} else if (SORT_ROLE === sortColumn) {
		
		if (true === viewUserDisplayId) {
			groupSortParams.sortList = [[2,0]];
		} else {
			groupSortParams.sortList = [[1,0]];
		}
	}
}

function configureEnrollmentStatusTableSort() {
	
	// enrollment status has both user display ID and email column, but we
	// probably don't want to hide user display IDs on the enrollment table
	
	if (true == viewEmail) {
		enrollmentSortParams = {headers:{1: {sorter:'urls'}, 2: {sorter:'urls'}}, sortList:[[0,0]]};
	} else {
		enrollmentSortParams = {headers:{1: {sorter:'urls'}}, sortList:[[0,0]]};
	}
	
	// now set the initial sort column
	// enrollment table doesn't have role, so use name as default sort column
	if (SORT_NAME === sortColumn || SORT_ROLE === sortColumn) {
		enrollmentSortParams.sortList = [[0,0]];
	} else if (SORT_DISPLAY_ID === sortColumn) {
		enrollmentSortParams.sortList = [[1,0]];
	} else if (SORT_EMAIL === sortColumn) {
		
		if (true === viewEmail) {
			enrollmentSortParams.sortList = [[2,0]];
		}
		
	} else if (SORT_STATUS === sortColumn) {
	
		if (true === viewEmail) {
			enrollmentSortParams.sortList = [[3,0]];
		} else {
			enrollmentSortParams.sortList = [[2,0]];
		}
	} else if (SORT_CREDITS === sortColumn) {
		
		if (true === viewEmail) {
			enrollmentSortParams.sortList = [[4,0]];
		} else {
			enrollmentSortParams.sortList = [[3,0]];
		}
	}
}

// this computes the columns array which is used to determine the sortField
// when exporting to Excel
function setColumnSortFields(state) {
	
	columnSortFields[0] = SORT_NAME;
	
	if (STATE_OVERVIEW === state) {
		
		if (true === viewUserDisplayId && true === viewEmail) {
			columnSortFields[1] = SORT_DISPLAY_ID;
			columnSortFields[2] = SORT_EMAIL;
			columnSortFields[3] = SORT_ROLE;
		} else if (true === viewUserDisplayId) {
			columnSortFields[1] = SORT_DISPLAY_ID;
			columnSortFields[2] = SORT_ROLE;
		} else if (true === viewEmail) {
			columnSortFields[1] = SORT_EMAIL;
			columnSortFields[2] = SORT_ROLE;
		}
		
	} else if (STATE_GROUP_MEMBERSHIP === state) {
		
		// n.b. no sort by groups column
		
		if (true === viewUserDisplayId) {
			columnSortFields[1] = SORT_DISPLAY_ID;
			columnSortFields[2] = SORT_ROLE;
		} else {
			columnSortFields[1] = SORT_ROLE;
		}
		
	} else if (STATE_ENROLLMENT_STATUS === state) {
		
		if (true === viewEmail) {
			columnSortFields[2] = SORT_EMAIL;
			columnSortFields[3] = SORT_STATUS;
			columnSortFields[4] = SORT_CREDITS;
		} else {
			columnSortFields[2] = SORT_STATUS;
			columnSortFields[3] = SORT_CREDITS;
		}
	}	
}

function setLanguage(locale) {

	if (locale) {
		language = locale;
	} else {
		// test for IE
		if (window.ActiveXObject) {
			language = navigator.userLanguage;
		} else {
			language = navigator.language;
		}
	}
}

/* Original Roster functions */
function clearIfDefaultString(formField, defaultString) {
	if (formField.value == defaultString) {
		formField.value = "";
	}
}

function handleEnterKey(field, event) {
	var keyCode = event.keyCode ? event.keyCode : event.which ? event.which
			: event.charCode;
	if (keyCode == 13) {
		document.getElementById('roster_form_search_button').click();
		//return false;
	}
	return true;
}
