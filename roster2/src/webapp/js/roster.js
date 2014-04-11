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
 * See the License for the specific roster.startupArgs.language governing permissions and
 * limitations under the License.
 */

/**
 * Daniel Robinson (d.b.robinson@lancaster.ac.uk)
 * Adrian Fish (a.fish@lancaster.ac.uk)
 */
(function ($) {

    roster.ADMIN = 'admin';
    
    roster.STATE_OVERVIEW = 'overview';
    roster.STATE_PICTURES = 'pics';
    roster.STATE_GROUP_MEMBERSHIP = 'group_membership';
    roster.STATE_ENROLLMENT_STATUS = 'status';
    roster.STATE_VIEW_PROFILE = 'profile';
    roster.STATE_PERMISSIONS = 'permissions';
    
    roster.DEFAULT_GROUP_ID = 'all';
    roster.DEFAULT_ENROLLMENT_STATUS = 'All';
    roster.DEFAULT_SORT_LIST = [[0,0]];
    roster.DEFAULT_STATE = roster.STATE_OVERVIEW;
    
    roster.SORT_NAME = 'sortName';
    roster.SORT_DISPLAY_ID = 'displayId';
    roster.SORT_EMAIL = 'email';
    roster.SORT_ROLE = 'role';
    roster.SORT_STATUS	= "status";
    roster.SORT_CREDITS	= "credits";
    
    roster.columnSortFields = [];
    
    /* Stuff that we always expect to be setup */
    roster.startupArgs.language = null;
    roster.rosterCurrentUserPermissions = null;
    roster.site = null;
    
    // so we can return to the previous state after viewing permissions
    roster.rosterLastStateNotPermissions = null;
    
    // These are default behaviours, and are global so the tool remembers
    // the user's choices.
    roster.grouped = roster_group_ungrouped;
    roster.hideNames = false;
    roster.viewSingleColumn = false;
    roster.groupToView = null;
    roster.groupToViewText = roster_sections_all;
    roster.enrollmentSetToView = null;
    roster.enrollmentSetToViewText = null;
    roster.enrollmentStatusToViewText = roster_enrollment_status_all;
    
    roster.sortColumn = null;
    
    // sortEnd is used to update this so we know which column and direction the
    // tables are sorted in when exporting
    roster.currentSortColumn = 0;
    roster.currentSortDirection = 0;
    
    // tablesorter parser for URLs
    $.tablesorter.addParser({
    	id: 'urls',
        is: function (s) { return false; },
    	format: function (s) { return s.replace(new RegExp(/<.*?>/),""); },
    	type: 'text'
    });
    
    // low resolution mode presents a drop-down menu and uses single-column mode
    roster.lowResModeWidth = 768;
    roster.lowResMode = null;
    
    roster.rosterOfficialPictureMode = false;
    
    roster.getRosterSite = function () {
    	
    	$.ajax({
        	url: "/direct/roster-membership/" + roster.startupArgs.siteId + "/get-site.json",
          	dataType: "json",
           	async: false,
    		cache: false,
    	   	success: function (data) {

    			roster.site = data;
    			if (undefined == roster.site.siteGroups) {
    				roster.site.siteGroups = new Array();
    			}
    			
    			if (undefined == roster.site.userRoles) {
    				roster.site.userRoles = new Array();
    			}
    			
    			if (undefined == roster.site.siteEnrollmentSets) {
    				roster.site.siteEnrollmentSets = new Array();
    			}
    		}
    	});
    };
    
    roster.switchState = function (state, arg, searchQuery) {
    
        $('#roster_navbar > li > span').removeClass('current');
    	
    	// so we can return to the previous state after viewing permissions
    	if (state != roster.STATE_PERMISSIONS) {
    		roster.rosterLastStateNotPermissions = state;
    	}
    	
    	// for export to Excel
    	roster.setColumnSortFields(state);
    			
    	// permissions
    	if (roster.startupArgs.siteMaintainer && 'true' == roster.startupArgs.siteMaintainer) {
            $('#navbar_permissions_link').show();
        } else {
            $('#navbar_permissions_link').hide();
        }
        
        // enrollment
    	if (!roster.rosterCurrentUserPermissions.viewEnrollmentStatus ||
    			roster.site.siteEnrollmentSets.length === 0) {
    		
    		$('#navbar_enrollment_status_link').hide();
    		
    		// this can happen if roster.default.state=3
    		if (roster.STATE_ENROLLMENT_STATUS === state) {
    			state = roster.DEFAULT_STATE;
    		}
    	}
    	
    	// hide group membership link if there are no groups
    	if (roster.site.siteGroups.length === 0) {
    		$('#navbar_group_membership_link').hide();
    		
    		// this can happen if roster.default.state=2
    		if (roster.STATE_GROUP_MEMBERSHIP === state) {
    			state = roster.DEFAULT_STATE;
    		}
    	}
    		
    	if (roster.STATE_OVERVIEW === state) {
    
    	    $('#navbar_overview_link > span').addClass('current');
    	
    		if (roster.lowResMode) {
    			$('#roster_navbar_dropdown').val(roster.STATE_OVERVIEW);
    		}
    		
    		roster.getMembers(searchQuery, false, state, false, function (members) {
    
                var roles = roster.getRolesUsingRosterMembers(members, roster.site.userRoles);
                
                SakaiUtils.renderTrimpathTemplate('roster_overview_header_template',
                        {'siteTitle':roster.site.title,
                        'displayTitleMsg':roster.rosterCurrentUserPermissions.viewAllMembers},
                        'roster_header');
    		
                if (roster.site.siteGroups.length > 0) {
                    
                    SakaiUtils.renderTrimpathTemplate('roster_section_filter_template',
                            {'groupToViewText':roster.groupToViewText,'siteGroups':roster.site.siteGroups},
                            'roster_section_filter');
                    
                } else {
                    
                    SakaiUtils.renderTrimpathTemplate('empty_template', {}, 'roster_section_filter');
                }
    		
                SakaiUtils.renderTrimpathTemplate('roster_search_with_participants_template',
                        {'roleFragments':roster.getRoleFragments(roles),
                        'participants':roster.getCurrentlyDisplayingParticipants(roles)},
                        'roster_search');
            
            
                SakaiUtils.renderTrimpathTemplate('roster_overview_template',
                        {'roster.startupArgs.language':roster.startupArgs.language, 'membership':members, 'siteId':roster.startupArgs.siteId,
                        'groupToView':roster.groupToView, 'firstNameLastName':roster.startupArgs.firstNameLastName,
                        'viewEmail':roster.startupArgs.viewEmail, 'viewUserDisplayId':roster.startupArgs.viewUserDisplayId,
                        'viewProfile':roster.rosterCurrentUserPermissions.viewProfile},
                        'roster_content');
    		
                $(document).ready(function () {
                    
                    roster.readyExportButton(state);
                    roster.readySearchButton(state);
                    roster.readyClearButton(state);
                    roster.readySectionFilter(state);
    
                    var overviewSortParams = roster.getOverviewTableSort();
                    
                    $('#roster_form_rosterTable').tablesorter(overviewSortParams);
                    
                    $('#roster_form_rosterTable').bind("sortEnd", function () {
    
                        roster.currentSortColumn = this.config.sortList[0][0];
                        roster.currentSortDirection = this.config.sortList[0][1];
                    });
    
                    roster.addProfilePopups();
                    
                    if(window.frameElement) {
                        setMainFrameHeight(window.frameElement.id);
                    }
                });
            });
    		
    	} else if (roster.STATE_PICTURES === state) {
    
    	    $('#navbar_pics_link > span').addClass('current');
    	
    		if (roster.lowResMode) {
    
    			$('#roster_navbar_dropdown').val(roster.STATE_PICTURES);
    			roster.viewSingleColumn = true;
    		}
    
    		SakaiUtils.renderTrimpathTemplate('roster_pics_header_template',
    				{'siteTitle':roster.site.title}, 'roster_header');
    
    		if (roster.site.siteGroups.length > 0) {
    			
    			SakaiUtils.renderTrimpathTemplate('roster_section_filter_template',
    					{'groupToViewText':roster.groupToViewText,'siteGroups':roster.site.siteGroups},
    					'roster_section_filter');
    		} else {
    			
    			SakaiUtils.renderTrimpathTemplate('empty_template', {}, 'roster_section_filter');			
    		}
    
            var callback = function (members) {
    
                    var roles = roster.getRolesUsingRosterMembers(members, roster.site.userRoles);
                    
                    SakaiUtils.renderTrimpathTemplate('roster_search_with_participants_template',
                            {'roleFragments':roster.getRoleFragments(roles),
                            'participants':roster.getCurrentlyDisplayingParticipants(roles)},
                            'roster_search');
    		
    
                    SakaiUtils.renderTrimpathTemplate('roster_pics_wrapper_template',
                                {'viewOfficialPhoto': roster.rosterCurrentUserPermissions.viewOfficialPhoto},
                                'roster_content');
    
                    $(document).ready(function () {
                        
                        if(roster.rosterCurrentUserPermissions.viewOfficialPhoto) {
    
                            $('#roster_official_picture_button').click(function (e) {
    
                                roster.getMembers(searchQuery, true, state, true, function (members) {
    
                                    SakaiUtils.renderTrimpathTemplate('roster_pics_template',
                                        {'language':roster.startupArgs.language,
                                        'membership':members,
                                        'siteId':roster.startupArgs.siteId,
                                        'currentUserId':roster.startupArgs.userId,
                                        'groupToView':roster.groupToView,
                                        'viewSingleColumn':roster.viewSingleColumn,
                                        'hideNames':roster.hideNames,
                                        'firstNameLastName':roster.startupArgs.firstNameLastName,
                                        'viewUserDisplayId':roster.startupArgs.viewUserDisplayId,
                                        'viewEmail':roster.startupArgs.viewEmail,
                                        'viewProfile':roster.rosterCurrentUserPermissions.viewProfile,
                                        'viewConnections':(undefined != window.friendStatus)}, // do we have Profile2 1.4 for adding, removing etc. connections?
                                        'roster_pics');
                                    roster.setupHideNamesAndSingleColumnButtons(state,searchQuery);
    
                                    roster.addProfilePopups();
    
                                    roster.rosterOfficialPictureMode = true;
                                });
                            });
                
                            $('#roster_profile_picture_button').click(function (e) {
    
                                roster.getMembers(searchQuery, true, state, false, function (members) {
    
                                    SakaiUtils.renderTrimpathTemplate('roster_pics_template',
                                        {'language':roster.startupArgs.language,
                                        'membership':members,
                                        'siteId':roster.startupArgs.siteId,
                                        'currentUserId':roster.startupArgs.userId,
                                        'groupToView':roster.groupToView,
                                        'viewSingleColumn':roster.viewSingleColumn,
                                        'hideNames':roster.hideNames,
                                        'firstNameLastName':roster.startupArgs.firstNameLastName,
                                        'viewUserDisplayId':roster.startupArgs.viewUserDisplayId,
                                        'viewEmail':roster.startupArgs.viewEmail,
                                        'viewProfile':roster.rosterCurrentUserPermissions.viewProfile,
                                        'viewConnections':(undefined != window.friendStatus)}, // do we have Profile2 1.4 for adding, removing etc. connections?
                                        'roster_pics');
                                    roster.setupHideNamesAndSingleColumnButtons(state,searchQuery);
    
                                    roster.addProfilePopups();
    
                                    roster.rosterOfficialPictureMode = false;
                                });
                            });
                        }
    
                        roster.readySearchButton(state);
                        roster.readyClearButton(state);
                        roster.readySectionFilter(state);
    
                        SakaiUtils.renderTrimpathTemplate('roster_pics_template',
                            {'language':roster.startupArgs.language,
                            'membership':members,
                            'siteId':roster.startupArgs.siteId,
                            'currentUserId':roster.startupArgs.userId,
                            'groupToView':roster.groupToView,
                            'viewSingleColumn':roster.viewSingleColumn,
                            'hideNames':roster.hideNames,
                            'firstNameLastName':roster.startupArgs.firstNameLastName,
                            'viewUserDisplayId':roster.startupArgs.viewUserDisplayId,
                            'viewEmail':roster.startupArgs.viewEmail,
                            'viewProfile':roster.rosterCurrentUserPermissions.viewProfile,
                            'viewConnections':(undefined != window.friendStatus)}, // do we have Profile2 1.4 for adding, removing etc. connections?
                            'roster_pics');
                        
                        roster.setupHideNamesAndSingleColumnButtons(state,searchQuery);
    
                        roster.addProfilePopups();
    
                        if(window.frameElement) {
                            setMainFrameHeight(window.frameElement.id);
                        }
    		        });
                };
    
            if((arg && arg.forceOfficialPicture) || roster.rosterOfficialPictureMode == true) {
                roster.getMembers(searchQuery, true, state, true, callback);
            } else {
                roster.getMembers(searchQuery, true, state, false, callback);
            }
    		
    	} else if (roster.STATE_GROUP_MEMBERSHIP === state) {
    
    	    $('#navbar_group_membership_link > span').addClass('current');
    		
    		if (roster.lowResMode) {
    			$('#roster_navbar_dropdown').val(roster.STATE_GROUP_MEMBERSHIP);
    		}
    		
    		roster.getRosterMembership(null, null, null, null, state, false, function (members) {
    
                var roles = roster.getRolesUsingRosterMembers(members, roster.site.userRoles);
    		
                SakaiUtils.renderTrimpathTemplate('roster_groups_header_template',
                        {'siteTitle':roster.site.title,
                        'displayTitleMsg':roster.rosterCurrentUserPermissions.viewAllMembers},
                        'roster_header');
    						
                SakaiUtils.renderTrimpathTemplate('empty_template', {}, 'roster_search');
    						
                if (roster_group_bygroup === roster.grouped) {
                    
                    SakaiUtils.renderTrimpathTemplate('roster_group_section_filter_template',
                            {'arg':arg, 'siteId':roster.startupArgs.siteId}, 'roster_section_filter');
    
                    SakaiUtils.renderTrimpathTemplate('roster_grouped_template',
                            {'language':roster.startupArgs.language,
                            'membership':members,
                            'siteGroups':roster.site.siteGroups,
                            'rolesText':roster.getRolesByGroupRoleFragments(members),
                            'siteId':roster.startupArgs.siteId,
                            'firstNameLastName':roster.startupArgs.firstNameLastName,
                            'viewUserDisplayId':roster.startupArgs.viewUserDisplayId,
                            'viewProfile':roster.rosterCurrentUserPermissions.viewProfile},
                            'roster_content');
                    
                } else {
                    
                    SakaiUtils.renderTrimpathTemplate('roster_group_section_filter_with_participants_template',
                            {'arg':arg, 'siteId':roster.startupArgs.siteId,'roleFragments':roster.getRoleFragments(roles),
                        'participants':roster.getCurrentlyDisplayingParticipants(roles)}, 'roster_section_filter');
                    
                    SakaiUtils.renderTrimpathTemplate('roster_ungrouped_template',
                            {'language':roster.startupArgs.language, 'membership':members, 'siteId':roster.startupArgs.siteId,
                            'viewUserDisplayId':roster.startupArgs.viewUserDisplayId, 
                            'firstNameLastName':roster.startupArgs.firstNameLastName,
                            'viewProfile':roster.rosterCurrentUserPermissions.viewProfile},
                            'roster_content');
                }
    		
                $(document).ready(function () {
                    
                    roster.readyExportButton(state);
                    
                    $('#roster_form_group_choice').val(roster.grouped);
                    $('#roster_form_group_choice').change(function (e) {
                        
                        roster.grouped = this.options[this.selectedIndex].text;
                        
                        roster.switchState('group_membership');
                    });
    
    		        var groupSortParams = roster.getGroupMembershipTableSort();
                    
                    $('table').tablesorter(groupSortParams);
    
                    roster.addProfilePopups();
                    
                    if(window.frameElement) {
                        setMainFrameHeight(window.frameElement.id);
                    }
                });
            });
    		
    	} else if (roster.STATE_VIEW_PROFILE === state) {
    		
    		SakaiUtils.getProfileMarkup(arg.userId, function (profileMarkup) {
    		
                $('#roster_content').html(profileMarkup);
                
                if(window.frameElement) {
                    setMainFrameHeight(window.frameElement.id);
                }
            });
    		
    	} else if (roster.STATE_ENROLLMENT_STATUS === state) {
    
    	    $('#navbar_enrollment_status_link > span').addClass('current');
    		
    		if (roster.lowResMode) {
    			$('#roster_navbar_dropdown').val(roster.STATE_ENROLLMENT_STATUS);
    		}
    		
    		if (null === roster.enrollmentSetToView && null != roster.site.siteEnrollmentSets[0]) {
    			roster.enrollmentSetToView = roster.site.siteEnrollmentSets[0].id;
    		}
    
    		SakaiUtils.renderTrimpathTemplate('roster_enrollment_header_template',
    				{'siteTitle':roster.site.title}, 'roster_header');
    
    		SakaiUtils.renderTrimpathTemplate('roster_enrollment_section_filter_template',
    				{'enrollmentSets':roster.site.siteEnrollmentSets,
    				'enrollmentStatusDescriptions':roster.site.enrollmentStatusDescriptions},
    				'roster_section_filter');
    		
    		roster.getEnrolledMembers(searchQuery, function (enrollment) {
    				
                SakaiUtils.renderTrimpathTemplate('roster_search_with_students_template',
                        {'students':roster.getCurrentlyDisplayingStudents(enrollment, null)},
                        'roster_search');
                
                SakaiUtils.renderTrimpathTemplate('roster_enrollment_status_template',
                        {'language':roster.startupArgs.language, 'enrollment':enrollment, 'enrollmentStatus':roster.enrollmentStatusToViewText,
                        'siteId':roster.startupArgs.siteId, 'firstNameLastName':roster.startupArgs.firstNameLastName,
                        'viewEmail':roster.startupArgs.viewEmail,
                        'viewProfile':roster.rosterCurrentUserPermissions.viewProfile},
                        'roster_content');
                        
                $(document).ready(function () {
                    
                    roster.readyExportButton(state);
                    roster.readyEnrollmentFilters(roster.site.siteEnrollmentSets.length);
                    
                    roster.readySearchButton(state);
                    roster.readyClearButton(state);
    
    		        var enrollmentSortParams = roster.getEnrollmentStatusTableSort();
                    
                    $('#roster_form_rosterTable').tablesorter(enrollmentSortParams);
                    
                    $('#roster_form_rosterTable').bind("sortEnd",function () {
                        roster.currentSortColumn = this.config.sortList[0][0];
                        roster.currentSortDirection = this.config.sortList[0][1];
                    });
    
                    roster.addProfilePopups();
    
                    if(window.frameElement) {
                        setMainFrameHeight(window.frameElement.id);
                    }
                });
            });
    	} else if (roster.STATE_PERMISSIONS === state) {
    
    	    $('#navbar_permissions_link > span').addClass('current');
    		
    		SakaiUtils.renderTrimpathTemplate('roster_permissions_header_template',
    				{'siteTitle':roster.site.title}, 'roster_header');
    		
    		SakaiUtils.renderTrimpathTemplate('empty_template', {}, 'roster_section_filter');
    		SakaiUtils.renderTrimpathTemplate('empty_template', {}, 'roster_search');
    
            SakaiUtils.getSitePermissionMatrix(roster.startupArgs.siteId, function (permissions) {
    		
                SakaiUtils.renderTrimpathTemplate('roster_permissions_template',
                        {'permissions': permissions},
                        'roster_content');
                
                $(document).ready(function () {
                    $('#roster_permissions_save_button').click(function () {
                       SakaiUtils.savePermissions(roster.startupArgs.siteId, 'roster_permission_checkbox',
                               function () { roster.switchState(roster.rosterLastStateNotPermissions) } );
                    });
                    
                    $('#roster_cancel_button').click(function () { roster.switchState(roster.rosterLastStateNotPermissions) } );
                });
            });
    	}
    };
    
    roster.setupHideNamesAndSingleColumnButtons = function (state,searchQuery) {
    
        roster.readyHideNamesButton(state, searchQuery);
        if (roster.lowResMode) {			
    	    $('#roster_form_pics_view').hide();
    	} else {
    	    roster.readyViewSingleColumnButton(state, searchQuery);
        }
    };
    
    roster.getRosterMembership = function (groupId, sorted, sortField, sortDirection, state, forceOfficialPicture, callback) {
    
    	var url = "/direct/roster-membership/" + roster.startupArgs.siteId + "/get-membership.json?sorted=" + sorted;
    	
    	// if pictures AND we have Profile2 1.4 (for adding, removing etc. connections)
    	if (roster.STATE_PICTURES === state && undefined != window.friendStatus) {
    		url += "&includeConnectionStatus=true";
    	} else {
    		url += "&includeConnectionStatus=false";
    	}
    	
    	if (groupId) {
    		url += "&groupId=" + groupId;
    	}
    
    	$.ajax({
        	url: url,
          	dataType: "json",
    		cache: false,
    	   	success: function (data) {
    
    			var membership = data['roster-membership_collection'];
    
                for (var i = 0, j = membership.length; i < j; i++) {
                    membership[i].formattedProfileUrl = "/direct/profile/" + membership[i].userId + "/formatted?siteId=" + roster.startupArgs.siteId;
                    if (roster.STATE_PICTURES === state) {
                        membership[i].profileImageUrl = "/direct/profile/" + membership[i].userId + "/image";
                        if(forceOfficialPicture == true) {
                            membership[i].profileImageUrl += "/official";
                        }
                        membership[i].profileImageUrl += "?siteId=" + roster.startupArgs.siteId;
                    }
                }
    
                callback(membership);
    		},
    		error: function () {
                callback(new Array());
    		}
    	});
    };
    
    roster.getRosterEnrollment = function (callback) {
    	
    	var url = "/direct/roster-membership/" + roster.startupArgs.siteId + "/get-enrollment.json?enrollmentSetId=" + roster.enrollmentSetToView;
    	
    	$.ajax({
        	url: url,
          	dataType: "json",
    		cache: false,
    	   	success: function (data) {
    			callback(data['roster-membership_collection']);
    		},
    		error: function () {
    			callback(new Array());
    		}
    	});
    };
    
    roster.getCurrentlyDisplayingParticipants = function (roles) {
    	
    	var participants = 0;
    	
    	for (var i = 0, j = roles.length; i < j; i++) {
    		
    		participants = participants + roles[i].roleCount;
    	}
    	
    	return currently_displaying_participants.replace(/\{0\}/, participants);
    };
    
    roster.getCurrentlyDisplayingStudents = function (enrollment, enrollmentType) {
    	
    	var currentEnrollments = enrollments_currently_displaying.replace(/\{0\}/,
    			enrollment.length);
    	
    	if (enrollmentType) {
    		currentEnrollments = currentEnrollments.replace(/\{1\}/, enrollmentType);
    	} else {
    		// do all needs no string		
    		currentEnrollments = currentEnrollments.replace(/\{1\}/, '');
    	}
    	
    	return currentEnrollments;
    };
    
    roster.getRoleFragments = function (roles) {
    	
    	var roleFragments = [];
    	
    	for (var i = 0, j = roles.length; i < j; i++) {
    				
    		var frag = role_breakdown_fragment.replace(/\{0\}/, roles[i].roleCount);
    		frag = frag.replace(/\{1\}/, roles[i].roleType);
    		
    		if (i != j - 1) {
    			frag = frag + ", ";
    		}
    		
    		roleFragments[i] = frag;
    	}	
    	return roleFragments;
    };
    
    roster.getRolesUsingRosterMembers = function (members, roleTypes) {
    	
    	var roles = [];
    		
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
    };
    
    roster.getRolesByGroup = function (members) {
    	
    	var rolesByGroup = [];
    	
    	for (var i = 0, j = roster.site.siteGroups.length; i < j; i++) {
    				
    		var groupId = roster.site.siteGroups[i].id;
    
    		rolesByGroup[groupId] = {};
    		rolesByGroup[groupId].groupId = groupId;
    		rolesByGroup[groupId].groupTitle = roster.site.siteGroups[i].title;
    		rolesByGroup[groupId].roles = new Array();
    		
    		for (var k = 0, l = members.length; k < l; k++) {
    			
    			for (var m = 0, n = roster.site.siteGroups[i].userIds.length; m < n; m++) {
    								
    				if (members[k].userId === roster.site.siteGroups[i].userIds[m]) {
    					
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
    };
    
    roster.getRolesByGroupRoleFragments = function (members) {
    
    	var rolesByGroup = roster.getRolesByGroup(members);
    	
    	var rolesByGroupRoleFragments = [];
    	
    	for (var group in rolesByGroup) {
    		
    		rolesByGroupRoleFragments[group] = {};
    		
    		var participants = 0;
    		rolesByGroupRoleFragments[group].roles = [];
    		
    		var numberOfRoles = 0;
    		for (var role in rolesByGroup[group].roles) {
    			numberOfRoles++;
    		}
    		
    		var roleNumber = 1;
    		for (var role in rolesByGroup[group].roles) {
    			
    			rolesByGroupRoleFragments[group].roles[role] = {};
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
    };
    
    roster.getMembers = function (searchQuery, sorted, state, forceOfficialPicture, callback) {
    
    	if (roster.groupToViewText === roster_sections_all || roster.groupToViewText === roster_section_sep_line) {
    
    	    // view all users
    
            roster.getRosterMembership(null, sorted, null, null, state,forceOfficialPicture, function (members) {
                callback(roster.filter(members, searchQuery));
            });
    	} else {
    
    	    // view a specific group (note: search is done within group if selected)
    
    		roster.getRosterMembership(roster.groupToView, sorted, null, null, state, forceOfficialPicture, function (members) {
                callback(roster.filter(members, searchQuery));
            });
    	}
    };
    
    roster.getEnrolledMembers = function (searchQuery, callback) {
    
    	// TODO pass enrollment status required?
    
    	roster.getRosterEnrollment( function (enrollment) {
            callback(roster.filter(enrollment, searchQuery));
        });
    };
    
    roster.filter = function (members, searchQuery) {
    	
        if (searchQuery) {
    
            var membersToReturn = [];
            var memberCount = 0;
            
            for (var i = 0, j = members.length; i < j; i++) {
                                
                if (members[i].displayName.toLowerCase().indexOf(searchQuery) >= 0 ||
                        members[i].displayId.toLowerCase().indexOf(searchQuery) >= 0) {
                                        
                    membersToReturn[memberCount] = members[i];
                    memberCount++;
                }
            }
            
            return membersToReturn;
        } else {
            return members;
        }
    };
    
    roster.readyClearButton = function (state) {
    	
    	$('#roster_form_clear_button').click(function (e) {
    		roster.switchState(state);
    	});
    };
    
    roster.readyExportButton = function (viewType) {
    		
    	$('#export_button').click(function (e) {
    	
    		e.preventDefault();
    		
    		var baseUrl = "/direct/roster-export/" + roster.startupArgs.siteId +
    			"/export-to-excel?viewType=" + viewType +
    			"&sortField=" + roster.columnSortFields[roster.currentSortColumn] +
    			"&sortDirection=" + roster.currentSortDirection;
    		
    		var facetParams = "&facetName=" + facet_name +
    			"&facetUserId=" + facet_userId +
    			"&facetEmail=" + facet_email +
    			"&facetRole=" + facet_role +
    			"&facetGroups=" + facet_groups +
    			"&facetStatus=" + facet_status +
    			"&facetCredits=" + facet_credits;
    		
    		if (roster.STATE_OVERVIEW === viewType) {
    			var groupId = null;
    			if (null != roster.groupToView) {
    				groupId = roster.groupToView;
    			} else {
    				groupId = roster.DEFAULT_GROUP_ID;
    			}
    			
    			window.location.href = baseUrl + "&groupId=" + groupId + facetParams;
    
    		} else if (roster.STATE_GROUP_MEMBERSHIP === viewType) {
    		
    			var byGroup = false;
    			if (roster.grouped === roster_group_bygroup) {
    				byGroup = true;
    			}
    			
    			window.location.href = baseUrl + "&byGroup=" + byGroup + facetParams;
    			
    		} else if (roster.STATE_ENROLLMENT_STATUS === viewType) {
    		
    			var enrollmentStatus = null;
    			if (roster.enrollmentStatusToViewText == roster_enrollment_status_all) {
    				enrollmentStatus = roster.DEFAULT_ENROLLMENT_STATUS;
    			} else {
    				enrollmentStatus = roster.enrollmentStatusToViewText;
    			}
    			
    			window.location.href = baseUrl + 
    				"&enrollmentSetId=" + roster.enrollmentSetToView +
    				"&enrollmentStatus=" + enrollmentStatus +
    				facetParams;
    		}
    	});
    		
    	// hide export button if necessary
    	if (roster.STATE_OVERVIEW === viewType || roster.STATE_GROUP_MEMBERSHIP === viewType || 
    			roster.STATE_ENROLLMENT_STATUS === viewType) {
    		
    		if (roster.rosterCurrentUserPermissions.rosterExport) {
    			$('#export_button').show();
    		} else {
    			$('#export_button').hide();
    		}
    	}
    };
    
    roster.readySearchButton = function (state) {
    	
    	$('#roster_form_search_button').click(function (e) {
    		
    		var searchFieldValue = document.forms['roster_form'].elements['roster_form_search_field'].value;
    
    		if (searchFieldValue != roster_search_text && searchFieldValue != "") {
    			
    			searchQuery = searchFieldValue.toLowerCase();
    			roster.switchState(state, null, searchQuery);
    		}
    	});
    };
    
    roster.readySectionFilter = function (state) {
    	
    	if (roster.site.siteGroups.length > 0) {
    		
    		$('#roster_form_section_filter').val(roster.groupToViewText);
    		$('#roster_form_section_filter').change(function (e) {
    			
    			if (this.options[this.selectedIndex].value != roster_section_sep_line) {
    				
    				roster.groupToView = this.options[this.selectedIndex].value;
    				roster.groupToViewText = this.options[this.selectedIndex].text;
    		
    				roster.switchState(state);
    			}
    		});
    	}
    };
    
    roster.readyEnrollmentFilters = function (numberOfEnrollmentSets) {
    			
    	if (numberOfEnrollmentSets > 0) {
    		
    		$('#roster_form_enrollment_set_filter').val(roster.enrollmentSetToViewText);
    		$('#roster_form_enrollment_set_filter').change(function (e) {
    			roster.enrollmentSetToView = this.options[this.selectedIndex].value;
    			roster.enrollmentSetToViewText = this.options[this.selectedIndex].text;
    			
    			roster.switchState(roster.STATE_ENROLLMENT_STATUS);
    		});
    	}
    	
    	$('#roster_form_enrollment_status_filter').val(roster.enrollmentStatusToViewText);
    	$('#roster_form_enrollment_status_filter').change(function (e) {
    		
    		roster.enrollmentStatusToViewText = this.options[this.selectedIndex].text;
    				
    		roster.switchState(roster.STATE_ENROLLMENT_STATUS);
    	});
    	
    };
    
    roster.readyHideNamesButton = function (state, searchQuery) {
    
    	$('#roster_form_hide_names').click(function (e) {
    		
    		if (true === roster.hideNames) {
    			roster.hideNames = false;
    		} else {
    			roster.hideNames = true;
    		}
    		
    		roster.switchState(state, null, searchQuery);
    	});
    };
    
    roster.readyViewSingleColumnButton = function (state, searchQuery) {
    	
    	$('#roster_form_pics_view').click(function (e) {
    		
    		if (true === roster.viewSingleColumn) {
    			roster.viewSingleColumn = false;
    		} else {
    			roster.viewSingleColumn = true;
    		}
    		
    		roster.switchState(state, null, searchQuery);
    	});
    };
    
    roster.getRosterCurrentUserPermissions = function () {
    		
    	if (roster.startupArgs.userId === roster.ADMIN) {
    		
    		var data = ['roster.export',
    				'roster.viewallmembers',
    				'roster.viewenrollmentstatus',
    				'roster.viewgroup',
    				'roster.viewhidden',
    				'roster.viewprofile',
    				'site.upd'];
    
    		roster.rosterCurrentUserPermissions = new RosterPermissions(data);
    		
    	} else {
    		roster.rosterCurrentUserPermissions = new RosterPermissions(
    			SakaiUtils.getCurrentUserPermissions(roster.startupArgs.siteId));		
    	}
    	
    };
    
    roster.getOverviewTableSort = function () {
    
        var overviewSortParams = null;
    	
    	// having a URL sorter on non-URLs work fine, so no need to check
    	
    	// user display ID has view profile URL attached to it
    	if (true === roster.startupArgs.viewUserDisplayId) {
    		if (true === roster.startupArgs.viewEmail) {
    			overviewSortParams = {headers:{1: {sorter:'urls'}, 2: {sorter:'urls'}}, sortList:roster.DEFAULT_SORT_LIST};
    		} else {
    			overviewSortParams = {headers:{1: {sorter:'urls'}}, sortList:roster.DEFAULT_SORT_LIST};
    		}
    	} else {
    		if (true === roster.startupArgs.viewEmail) {
    			overviewSortParams = {headers:{0: {sorter:'urls'}, 1: {sorter:'urls'}}, sortList:roster.DEFAULT_SORT_LIST};
    		} else {
    			overviewSortParams = {headers:{}, sortList:roster.DEFAULT_SORT_LIST};
    		}
    	}
    	
    	// now set the initial sort column
    	if (roster.SORT_NAME === roster.sortColumn) {
    		overviewSortParams.sortList = [[0,0]];
    	} else if (roster.SORT_DISPLAY_ID === roster.sortColumn) {
    		
    		if (true === roster.startupArgs.viewUserDisplayId) {
    			overviewSortParams.sortList = [[1,0]];
    		}
    		
    	} else if (roster.SORT_EMAIL === roster.sortColumn) {
    		
    		if (true === roster.startupArgs.viewEmail) {
    			
    			if (true === roster.startupArgs.viewUserDisplayId) {
    				overviewSortParams.sortList = [[2,0]];
    			} else {
    				overviewSortParams.sortList = [[1,0]];
    			}
    		}
    		
    	} else if (roster.SORT_ROLE === roster.sortColumn) {
    	
    		if (true === roster.startupArgs.viewEmail) {
    			
    			if (true === roster.startupArgs.viewUserDisplayId) {
    				overviewSortParams.sortList = [[3,0]];
    			} else {
    				overviewSortParams.sortList = [[2,0]];
    			}
    		} else {
    			
    			if (true === roster.startupArgs.viewUserDisplayId) {
    				overviewSortParams.sortList = [[2,0]];
    			} else {
    				overviewSortParams.sortList = [[1,0]];
    			}
    		}
    	}
    
        return overviewSortParams;
    };
    
    roster.getGroupMembershipTableSort = function () {
    
        var groupSortParams = null;
    
    	// group membership has user display ID but no email column
    		
    	if (true === roster.startupArgs.viewUserDisplayId) {
    		// user ID column (1) has view profile URL attached to it
    		groupSortParams = {headers:{1: {sorter:'urls'}, 3: {sorter:false}}, sortList:roster.DEFAULT_SORT_LIST};
    	} else {
    		// user name column (0) has view profile URL attached to it
    		groupSortParams = {headers:{0: {sorter:'urls'}, 2: {sorter:false}}, sortList:roster.DEFAULT_SORT_LIST};
    	}
    	
    	// now set the initial sort column
    	if (roster.SORT_NAME === roster.sortColumn) {
    		groupSortParams.sortList = [[0,0]];
    	} else if (roster.SORT_DISPLAY_ID === roster.sortColumn) {
    		
    		if (true === roster.startupArgs.viewUserDisplayId) {
    			groupSortParams.sortList = [[1,0]];
    		}
    	} else if (roster.SORT_ROLE === roster.sortColumn) {
    		
    		if (true === roster.startupArgs.viewUserDisplayId) {
    			groupSortParams.sortList = [[2,0]];
    		} else {
    			groupSortParams.sortList = [[1,0]];
    		}
    	}
    
        return groupSortParams;
    };
    
    roster.getEnrollmentStatusTableSort = function () {
    
        var enrollmentSortParams = null;
    	
    	// enrollment status has both user display ID and email column, but we
    	// probably don't want to hide user display IDs on the enrollment table
    	
    	if (true == roster.startupArgs.viewEmail) {
    		enrollmentSortParams = {headers:{1: {sorter:'urls'}, 2: {sorter:'urls'}}, sortList:[[0,0]]};
    	} else {
    		enrollmentSortParams = {headers:{1: {sorter:'urls'}}, sortList:[[0,0]]};
    	}
    	
    	// now set the initial sort column
    	// enrollment table doesn't have role, so use name as default sort column
    	if (roster.SORT_NAME === roster.sortColumn || roster.SORT_ROLE === roster.sortColumn) {
    		enrollmentSortParams.sortList = [[0,0]];
    	} else if (roster.SORT_DISPLAY_ID === roster.sortColumn) {
    		enrollmentSortParams.sortList = [[1,0]];
    	} else if (roster.SORT_EMAIL === roster.sortColumn) {
    		
    		if (true === roster.startupArgs.viewEmail) {
    			enrollmentSortParams.sortList = [[2,0]];
    		}
    		
    	} else if (roster.SORT_STATUS === roster.sortColumn) {
    	
    		if (true === roster.startupArgs.viewEmail) {
    			enrollmentSortParams.sortList = [[3,0]];
    		} else {
    			enrollmentSortParams.sortList = [[2,0]];
    		}
    	} else if (roster.SORT_CREDITS === roster.sortColumn) {
    		
    		if (true === roster.startupArgs.viewEmail) {
    			enrollmentSortParams.sortList = [[4,0]];
    		} else {
    			enrollmentSortParams.sortList = [[3,0]];
    		}
    	}
    
        return enrollmentSortParams;
    };
    
    /**
     * This computes the columns array which is used to determine the sortField
     * when exporting to Excel
     */
    roster.setColumnSortFields = function (state) {
    	
    	roster.columnSortFields[0] = roster.SORT_NAME;
    	
    	if (roster.STATE_OVERVIEW === state) {
    		
    		if (true === roster.startupArgs.viewUserDisplayId && true === roster.startupArgs.viewEmail) {
    			roster.columnSortFields[1] = roster.SORT_DISPLAY_ID;
    			roster.columnSortFields[2] = roster.SORT_EMAIL;
    			roster.columnSortFields[3] = roster.SORT_ROLE;
    		} else if (true === roster.startupArgs.viewUserDisplayId) {
    			roster.columnSortFields[1] = roster.SORT_DISPLAY_ID;
    			roster.columnSortFields[2] = roster.SORT_ROLE;
    		} else if (true === roster.startupArgs.viewEmail) {
    			roster.columnSortFields[1] = roster.SORT_EMAIL;
    			roster.columnSortFields[2] = roster.SORT_ROLE;
    		}
    		
    	} else if (roster.STATE_GROUP_MEMBERSHIP === state) {
    		
    		// n.b. no sort by groups column
    		
    		if (true === roster.startupArgs.viewUserDisplayId) {
    			roster.columnSortFields[1] = roster.SORT_DISPLAY_ID;
    			roster.columnSortFields[2] = roster.SORT_ROLE;
    		} else {
    			roster.columnSortFields[1] = roster.SORT_ROLE;
    		}
    		
    	} else if (roster.STATE_ENROLLMENT_STATUS === state) {
    		
    		if (true === roster.startupArgs.viewEmail) {
    			roster.columnSortFields[2] = roster.SORT_EMAIL;
    			roster.columnSortFields[3] = roster.SORT_STATUS;
    			roster.columnSortFields[4] = roster.SORT_CREDITS;
    		} else {
    			roster.columnSortFields[2] = roster.SORT_STATUS;
    			roster.columnSortFields[3] = roster.SORT_CREDITS;
    		}
    	}	
    };
    
    roster.clearIfDefaultString = function (formField, defaultString) {
    
    	if (formField.value == defaultString) {
    		formField.value = "";
    	}
    };
    
    roster.handleEnterKey = function (field, event) {
    
    	var keyCode = event.keyCode ? event.keyCode : event.which ? event.which
    			: event.charCode;
    	if (keyCode == 13) {
    		document.getElementById('roster_form_search_button').click();
    		//return false;
    	}
    	return true;
    };
    
    roster.addProfilePopups = function () {
    
        $('a.profile').cluetip({
            width: '620px',
            cluetipClass: 'roster',
            sticky: true,
            dropShadow: false,
            arrows: true,
            mouseOutClose: true,
            closeText: '<img src="/library/image/silk/cross.png" alt="close" />',
            closePosition: 'top',
            showTitle: false,
            hoverIntent: true,
            ajaxSettings: {type: 'GET'}
        });
    };

	
	if (!roster.startupArgs || !roster.startupArgs.siteId) {
		alert('The site id  MUST be supplied as a bootstrap parameter.');
		return;
	}
	
	if (!roster.startupArgs.userId) {
		alert("No current user. Have you logged in?");
		return;
	}
	
	roster.getRosterSite();
	roster.getRosterCurrentUserPermissions();
	
	roster.lowResMode = screen.width < roster.lowResModeWidth;
	
	// We need the toolbar in a template so we can swap in the translations
	if (roster.lowResMode) {
		SakaiUtils.renderTrimpathTemplate('roster_navbar_low_res_template', {
			groups:roster.site.siteGroups.length > 0,
			enrollment:roster.rosterCurrentUserPermissions.viewEnrollmentStatus &&
				roster.site.siteEnrollmentSets.length > 0,
			permissions:roster.rosterCurrentUserPermissions.siteUpdate
			},
			'roster_navbar');
		
		$('#roster_navbar_dropdown').change(function () {
			roster.switchState(this.value);
        });
	} else {
		SakaiUtils.renderTrimpathTemplate('roster_navbar_template', {},
			'roster_navbar');
	}
	
	$('#navbar_overview_link > span > a').click(function (e) {
		return roster.switchState(roster.STATE_OVERVIEW);
	});

	$('#navbar_pics_link > span > a').click(function (e) {
		return roster.switchState(roster.STATE_PICTURES);
	});

	$('#navbar_group_membership_link > span > a').click(function (e) {
		return roster.switchState(roster.STATE_GROUP_MEMBERSHIP);
	});
	
	$('#navbar_enrollment_status_link > span > a').click(function (e) {
		return roster.switchState(roster.STATE_ENROLLMENT_STATUS);
	});
	
    $('#navbar_permissions_link > span > a').click(function (e) {
        return roster.switchState(roster.STATE_PERMISSIONS);
    });
        	
	// process sakai.properties
		
    if (roster.SORT_NAME == roster.startupArgs.defaultSortColumn ||
            roster.SORT_DISPLAY_ID == roster.startupArgs.defaultSortColumn ||
            roster.SORT_ROLE == roster.startupArgs.defaultSortColumn ||
            roster.SORT_STATUS == roster.startupArgs.defaultSortColumn ||
            roster.SORT_CREDITS == roster.startupArgs.defaultSortColumn) {
        
        roster.sortColumn = roster.startupArgs.defaultSortColumn;
    } else if (roster.SORT_EMAIL == roster.startupArgs.defaultSortColumn && true == roster.startupArgs.viewEmail) {
        // if chosen sort is email, check that email column is viewable
        roster.sortColumn = roster.startupArgs.defaultSortColumn;
    }
	
	// end of sakai.properties
	
	if (window.frameElement) {
		window.frameElement.style.minHeight = '600px';
	}
		
	// Now switch into the requested state
	roster.switchState(roster.startupArgs.state, roster.startupArgs);

})(jQuery);
