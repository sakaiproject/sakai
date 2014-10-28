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
 * See the License for the specific roster.language governing permissions and
 * limitations under the License.
 */

/**
 * Daniel Robinson (d.b.robinson@lancaster.ac.uk)
 * Adrian Fish (a.fish@lancaster.ac.uk)
 */

(function ($) {

    // jquery.i18n
	$.i18n.properties({
	    name:'ui', 
	    path:'/sakai-roster2-tool/i18n/',
	    mode: 'both',
	    language: roster.language
	});
    
	roster.i18n = $.i18n.map;
	
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
    roster.language = null;
    roster.currentUserPermissions = null;
    roster.site = null;

    // so we can return to the previous state after viewing permissions
    roster.rosterLastStateNotPermissions = null;

    // These are default behaviours, and are global so the tool remembers
    // the user's choices.
    roster.grouped = roster.i18n.roster_group_ungrouped;
    roster.hideNames = false;
    roster.viewSingleColumn = false;
    roster.groupToView = null;
    roster.groupToViewText = roster.i18n.roster_sections_all;
    roster.enrollmentSetToView = null;
    roster.enrollmentSetToViewText = null;
    roster.enrollmentStatusToViewText = roster.i18n.roster_enrollment_status_all;

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

    /**
     * Renders a handlebars template.
     */
    roster.render = function (template, data, outputId) {

        var t = Handlebars.templates[template];
        document.getElementById(outputId).innerHTML = t(data);
    };

    roster.switchState = function (state, arg, searchQuery) {

        $('#roster_navbar > li > span').removeClass('current');
        
        // so we can return to the previous state after viewing permissions
        if (state !== roster.STATE_PERMISSIONS) {
            roster.rosterLastStateNotPermissions = state;
        }
        
        // for export to Excel
        roster.setColumnSortFields(state);
                
        // permissions
        if (roster.siteMaintainer) {
            $('#navbar_permissions_link').show();
        } else {
            $('#navbar_permissions_link').hide();
        }
        
        // enrollment
        if (!roster.currentUserPermissions.viewEnrollmentStatus ||
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
                
                roster.render('overview_header',
                        { 'siteTitle': roster.site.title,
                        'displayTitleMsg': roster.currentUserPermissions.viewAllMembers },
                        'roster_header');
            
                if (roster.site.siteGroups.length > 0) {
                    roster.render('section_filter',
                            { 'groupToViewText': roster.groupToViewText, 'siteGroups': roster.site.siteGroups },
                            'roster_section_filter');
                    
                } else {
                    $('#roster_section_filter').html('');
                }

                var roles = roster.getRolesUsingRosterMembers(members, roster.site.userRoles);
            
                roster.render('search_with_participants',
                        { roleFragments: roster.getRoleFragments(roles),
                            participants: roster.getCurrentlyDisplayingParticipants(roles),
                            searchQuery: searchQuery }, 'roster_search');
            
            
                roster.render('overview',
                        { 'roster.language': roster.language, 'membership': members, 'siteId': roster.siteId,
                        'groupToView' :roster.groupToView, 'firstNameLastName': roster.firstNameLastName,
                        'viewEmail': roster.viewEmail, 'viewUserDisplayId': roster.viewUserDisplayId,
                        'viewProfile': roster.currentUserPermissions.viewProfile},
                        'roster_content');
            
                $(document).ready(function () {
                    
                    roster.readyExportButton(state);
                    roster.readySearchButton(state);
                    roster.readySearchField(searchQuery);
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

            roster.render('pics_header',
                    {'siteTitle': roster.site.title}, 'roster_header');

            if (roster.site.siteGroups.length > 0) {
                roster.render('section_filter',
                        {'groupToViewText': roster.groupToViewText, 'siteGroups': roster.site.siteGroups},
                        'roster_section_filter');
            } else {
                $('#roster_section_filter').html('');			
            }

            var callback = function (members) {

                    var roles = roster.getRolesUsingRosterMembers(members, roster.site.userRoles);
                    
                    roster.render('search_with_participants',
                            {'roleFragments': roster.getRoleFragments(roles),
                            'participants': roster.getCurrentlyDisplayingParticipants(roles)},
                            'roster_search');

                    roster.render('pics_wrapper',
                                { 'viewOfficialPhoto': roster.currentUserPermissions.viewOfficialPhoto },
                                'roster_content');

                    $(document).ready(function () {
                        
                        if (roster.currentUserPermissions.viewOfficialPhoto) {

                            $('#roster_official_picture_button').click(function (e) {

                                roster.getMembers(searchQuery, true, state, true, function (members) {

                                    roster.render('pics',
                                        { 'language': roster.language,
                                        'membership': members,
                                        'siteId': roster.siteId,
                                        'currentUserId': roster.userId,
                                        'groupToView': roster.groupToView,
                                        'viewSingleColumn': roster.viewSingleColumn,
                                        'hideNames': roster.hideNames,
                                        'firstNameLastName': roster.firstNameLastName,
                                        'viewUserDisplayId': roster.viewUserDisplayId,
                                        'viewEmail': roster.viewEmail,
                                        'viewProfile': roster.currentUserPermissions.viewProfile,
                                        'viewConnections': (undefined != window.friendStatus)}, // do we have Profile2 1.4 for adding, removing etc. connections?
                                        'roster_pics');

                                    roster.setupHideNamesAndSingleColumnButtons(state,searchQuery);

                                    roster.addProfilePopups();

                                    roster.rosterOfficialPictureMode = true;
                                });
                            });
                
                            $('#roster_profile_picture_button').click(function (e) {

                                roster.getMembers(searchQuery, true, state, false, function (members) {

                                    roster.render('pics',
                                        {'language': roster.language,
                                        'membership': members,
                                        'siteId': roster.siteId,
                                        'currentUserId': roster.userId,
                                        'groupToView': roster.groupToView,
                                        'viewSingleColumn': roster.viewSingleColumn,
                                        'hideNames': roster.hideNames,
                                        'firstNameLastName': roster.firstNameLastName,
                                        'viewUserDisplayId': roster.viewUserDisplayId,
                                        'viewEmail': roster.viewEmail,
                                        'viewProfile': roster.currentUserPermissions.viewProfile,
                                        'viewConnections':(undefined != window.friendStatus)}, // do we have Profile2 1.4 for adding, removing etc. connections?
                                        'roster_pics');
                                    roster.setupHideNamesAndSingleColumnButtons(state,searchQuery);

                                    roster.addProfilePopups();

                                    roster.rosterOfficialPictureMode = false;
                                });
                            });
                        }

                        roster.readySearchButton(state);
                        roster.readySearchField(searchQuery);
                        roster.readyClearButton(state);
                        roster.readySectionFilter(state);

                        roster.render('pics',
                            {'language': roster.language,
                            'membership': members,
                            'siteId': roster.siteId,
                            'currentUserId': roster.userId,
                            'groupToView': roster.groupToView,
                            'viewSingleColumn': roster.viewSingleColumn,
                            'hideNames': roster.hideNames,
                            'firstNameLastName': roster.firstNameLastName,
                            'viewUserDisplayId': roster.viewUserDisplayId,
                            'viewEmail': roster.viewEmail,
                            'viewProfile': roster.currentUserPermissions.viewProfile,
                            'viewConnections': (undefined != window.friendStatus)}, // do we have Profile2 1.4 for adding, removing etc. connections?
                            'roster_pics');
                        
                        roster.setupHideNamesAndSingleColumnButtons(state,searchQuery);

                        roster.addProfilePopups();

                        if(window.frameElement) {
                            setMainFrameHeight(window.frameElement.id);
                        }
                    });
                };

            if((arg && arg.forceOfficialPicture) || roster.rosterOfficialPictureMode) {
                roster.getMembers(searchQuery, true, state, true, callback);
            } else {
                roster.getMembers(searchQuery, true, state, false, callback);
            }
            
        } else if (roster.STATE_GROUP_MEMBERSHIP === state) {

            $('#navbar_group_membership_link > span').addClass('current');
            
            if (roster.lowResMode) {
                $('#roster_navbar_dropdown').val(roster.STATE_GROUP_MEMBERSHIP);
            }
            
            roster.getRosterMembership(null, null, null, null, state, false, function (siteMembers) {

                var roles = roster.getRolesUsingRosterMembers(siteMembers, roster.site.userRoles);
            
                roster.render('groups_header',
                        { 'siteTitle': roster.site.title,
                        'displayTitleMsg': roster.currentUserPermissions.viewAllMembers }, 'roster_header');
                            
                $('#roster_search').html('');
                            
                if (roster.i18n.roster_group_bygroup === roster.grouped) {
                    
                    roster.render('group_section_filter', {}, 'roster_section_filter');

                    roster.site.siteGroups.forEach(function (group) {

                        group.members = [];

                        group.userIds.forEach(function (userId) {

                            siteMembers.some(function (siteMember) {

                                if (siteMember.userId === userId) {
                                    group.members.push(siteMember);
                                    return true;
                                }
                            });
                        });
                    });

                    roster.render('grouped',
                            { 'language': roster.language,
                            //'siteGroups': roster.site.siteGroups,
                            siteGroups: roster.getRolesByGroupRoleFragments(roster.site.siteGroups, siteMembers),
                            'siteId': roster.siteId,
                            'firstNameLastName': roster.firstNameLastName,
                            'viewUserDisplayId': roster.viewUserDisplayId,
                            'viewProfile': roster.currentUserPermissions.viewProfile }, 'roster_content');
                } else {
                    
                    roster.render('group_section_filter_with_participants',
                            { 'roleFragments': roster.getRoleFragments(roles),
                                'participants': roster.getCurrentlyDisplayingParticipants(roles) }, 'roster_section_filter');
                    
                    roster.render('ungrouped',
                            { 'language': roster.language,
                                'membership': siteMembers,
                                'siteId': roster.siteId,
                                'viewUserDisplayId': roster.viewUserDisplayId, 
                                'firstNameLastName': roster.firstNameLastName,
                                'viewProfile': roster.currentUserPermissions.viewProfile }, 'roster_content');
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
                    
                    if (window.frameElement) {
                        setMainFrameHeight(window.frameElement.id);
                    }
                });
            });
            
        } else if (roster.STATE_VIEW_PROFILE === state) {
            
            roster.sakai.getProfileMarkup(arg.userId, function (profileMarkup) {
            
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

            roster.render('enrollment_header',
                    { 'siteTitle': roster.site.title }, 'roster_header');

            roster.render('enrollment_section_filter',
                    { enrollmentSets: roster.site.siteEnrollmentSets,
                    onlyOne: roster.site.siteEnrollmentSets.length == 1,
                    enrollmentStatusDescriptions: roster.site.enrollmentStatusDescriptions },
                    'roster_section_filter');
            
            roster.getEnrolledMembers(searchQuery, function (enrollment) {
                    
                roster.render('search_with_students',
                        { 'students': roster.getCurrentlyDisplayingStudents(enrollment, null) },
                        'roster_search');

                enrollment.forEach(function (e) {

                    e.canViewEnrollment
                        = roster.enrollmentStatusToViewText === roster.i18n.roster_enrollment_status_all
                            || e.enrollmentStatus === roster.enrollmentStatusToViewText;
                });
                
                roster.render('enrollment_status',
                        { 'language': roster.language,
                            'enrollment': enrollment,
                            'siteId': roster.siteId,
                            'firstNameLastName': roster.firstNameLastName,
                            'viewEmail': roster.viewEmail,
                            'viewProfile': roster.currentUserPermissions.viewProfile }, 'roster_content');
                        
                $(document).ready(function () {
                    
                    roster.readyExportButton(state);
                    roster.readyEnrollmentFilters(roster.site.siteEnrollmentSets.length);
                    
                    roster.readySearchButton(state);
                    roster.readySearchField();
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
            
            roster.render('permissions_header',
                    { 'siteTitle': roster.site.title }, 'roster_header');
            
            $('#roster_section_filter').html('');
            $('#roster_search').html('');

            roster.sakai.getSitePermissionMatrix(roster.siteId, function (permissions) {

                roster.site.permissions = permissions;

                var roles = Object.keys(permissions).map(function (role) {
                        return {name: role};
                    });
            
                roster.render('permissions', { roles: roles }, 'roster_content');
                
                $(document).ready(function () {

                    $('#roster_permissions_save_button').click(function () {

                       roster.sakai.savePermissions(roster.siteId, 'roster_permission_checkbox',
                               function () { roster.switchState(roster.rosterLastStateNotPermissions); } );
                    });
                    
                    $('#roster_cancel_button').click(function () { roster.switchState(roster.rosterLastStateNotPermissions); } );
                });
            });
        }
    };

    roster.setupHideNamesAndSingleColumnButtons = function (state, searchQuery) {

        roster.readyHideNamesButton(state, searchQuery);
        if (roster.lowResMode) {			
            $('#roster_form_pics_view').hide();
        } else {
            $('#roster_form_pics_view').click(function (e) {
                
                if (roster.viewSingleColumn) {
                    roster.viewSingleColumn = false;
                } else {
                    roster.viewSingleColumn = true;
                }
                
                roster.switchState(state, null, searchQuery);
            });
        }
    };

    roster.getRosterMembership = function (groupId, sorted, sortField, sortDirection, state, forceOfficialPicture, callback) {

        var url = "/direct/roster-membership/" + roster.siteId + "/get-membership.json?sorted=" + sorted;
        
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

                membership.forEach(function (m) {

                    m.formattedProfileUrl = "/direct/profile/" + m.userId + "/formatted?siteId=" + roster.siteId;
                    if (roster.STATE_PICTURES === state) {
                        m.profileImageUrl = "/direct/profile/" + m.userId + "/image";
                        if (forceOfficialPicture) {
                            m.profileImageUrl += "/official";
                        }
                        m.profileImageUrl += "?siteId=" + roster.siteId;
                    }
                });

                callback(membership);
            },
            error: function () {
                callback(new Array());
            }
        });
    };

    roster.getRosterEnrollment = function (callback) {
        
        var url = "/direct/roster-membership/" + roster.siteId + "/get-enrollment.json?enrollmentSetId=" + roster.enrollmentSetToView;
        
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
        
        var participants
            = roles.reduce(function (prev, el) { return prev + el.count; }, 0);
        
        return roster.i18n.currently_displaying_participants.replace(/\{0\}/, participants);
    };

    roster.getCurrentlyDisplayingStudents = function (enrollment, enrollmentType) {
        
        var currentEnrollments = roster.i18n.enrollments_currently_displaying.replace(/\{0\}/,
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
        
        return roles.map(function (r) {

            var frag = roster.i18n.role_breakdown_fragment.replace(/\{0\}/, r.count);
            return frag.replace(/\{1\}/, r.roleType);
        }).join();
    };

    roster.getRolesUsingRosterMembers = function (members, roleTypes) {
        
        var roles
            = roleTypes.map(function (t) { return {roleType: t, count: 0}; });
        
        // Count the members in each role
        roles.forEach(function (r) {

            members.forEach(function (m) {
            
                if (m.role === r.roleType) {
                    r.count++;
                }
            });
        });
        
        // Filter out empty roles
        return roles.filter(function (r) { return r.count > 0; });
    };

    roster.addRoleMembershipCountsToGroups = function (groups, members) {
        
        return groups.map(function (group) {
                    
            group.roles = {};

            members.forEach(function (member) {
                
                group.userIds.forEach(function (userId) {

                    if (member.userId === userId) {
                        
                        var role = member.role;
                        
                        if (undefined === group.roles[role]) {
                            group.roles[role] = { roleType: role, count: 0 };
                        }

                        group.roles[role].count += 1;
                    }
                });
            });

            return group;
        });
    };

    roster.getRolesByGroupRoleFragments = function (groups, members) {

        var countedGroups = roster.addRoleMembershipCountsToGroups(groups, members);
        
        return countedGroups.map(function (group) {

            var roleIds = Object.keys(group.roles);
            var numberOfRoles = roleIds.length;
            
            var participantsCount = 0;
            var roleNumber = 1;

            roleIds.forEach(function (id) {

                var role = group.roles[id];
                
                role.fragment
                    = roster.i18n.role_breakdown_fragment
                        .replace(/\{0\}/, role.count)
                            .replace(/\{1\}/, role.roleType);
                
                if (roleNumber != numberOfRoles) {
                    role.fragment +=  ", ";
                }
                                    
                participantsCount += role.count;
                
                roleNumber += 1;
            });
            
            group.participants
                = roster.i18n.currently_displaying_participants.replace(/\{0\}/, participantsCount);

            return group;
        });
    };

    roster.getMembers = function (searchQuery, sorted, state, forceOfficialPicture, callback) {

        if (roster.groupToViewText === roster.i18n.roster_sections_all || roster.groupToViewText === roster.i18n.roster_section_sep_line) {
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
            return members.filter(function (m) {
                                
                if (m.displayName.toLowerCase().indexOf(searchQuery) >= 0 ||
                        m.displayId.toLowerCase().indexOf(searchQuery) >= 0) {
                    return true;
                } else {
                    return false;
                }
            });
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
            
            var baseUrl = "/direct/roster-export/" + roster.siteId +
                "/export-to-excel?viewType=" + viewType +
                "&sortField=" + roster.columnSortFields[roster.currentSortColumn] +
                "&sortDirection=" + roster.currentSortDirection;
            
            var facetParams = "&facetName=" + roster.i18n.facet_name +
                "&facetUserId=" + roster.i18n.facet_userId +
                "&facetEmail=" + roster.i18n.facet_email +
                "&facetRole=" + roster.i18n.facet_role +
                "&facetGroups=" + roster.i18n.facet_groups +
                "&facetStatus=" + roster.i18n.facet_status +
                "&facetCredits=" + roster.i18n.facet_credits;
            
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
            
            if (roster.currentUserPermissions.rosterExport) {
                $('#export_button').show();
            } else {
                $('#export_button').hide();
            }
        }
    };

    roster.readySearchButton = function (state) {
        
        $('#roster_form_search_button').click(function (e) {
            
            var searchFieldValue = $('#roster_form_search_field').val();

            if (searchFieldValue !== roster.i18n.roster_search_text && searchFieldValue !== "") {
                
                searchQuery = searchFieldValue.toLowerCase();
                roster.switchState(state, null, searchQuery);
            }
        });
    };

    roster.readySearchField = function () {

        var field = $('#roster_form_search_field');
        
        field.focus(function (e) {
            if (this.value === roster.i18n.roster_search_text) {
                this.value = "";
            }
        }).keypress(function (e) {
            if (e.which == 13) {
                $('#roster_form_search_button').click();
                return false;
            } else {
                return true;
            }
        });
    };

    roster.readySectionFilter = function (state) {
        
        if (roster.site.siteGroups.length > 0) {
            
            $('#roster_form_section_filter').val(roster.groupToViewText);
            $('#roster_form_section_filter').change(function (e) {
                
                if (this.options[this.selectedIndex].value != roster.i18n.roster_section_sep_line) {
                    
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
            
            if (roster.hideNames) {
                roster.hideNames = false;
            } else {
                roster.hideNames = true;
            }
            
            roster.switchState(state, null, searchQuery);
        });
    };

    roster.getOverviewTableSort = function () {

        var overviewSortParams = null;
        
        // having a URL sorter on non-URLs work fine, so no need to check
        
        // user display ID has view profile URL attached to it
        if (roster.viewUserDisplayId) {
            if (roster.viewEmail) {
                overviewSortParams = { headers: {1: {sorter: 'urls'}, 2: {sorter: 'urls'}}, sortList:roster.DEFAULT_SORT_LIST };
            } else {
                overviewSortParams = { headers: {1: {sorter: 'urls'}}, sortList: roster.DEFAULT_SORT_LIST };
            }
        } else {
            if (roster.viewEmail) {
                overviewSortParams = { headers:{0: {sorter: 'urls'}, 1: {sorter: 'urls'}}, sortList:roster.DEFAULT_SORT_LIST };
            } else {
                overviewSortParams = { headers: {0: {sorter:'urls'}}, sortList:roster.DEFAULT_SORT_LIST };
            }
        }
        
        // now set the initial sort column
        if (roster.SORT_NAME === roster.sortColumn) {
            overviewSortParams.sortList = [[0,0]];
        } else if (roster.SORT_DISPLAY_ID === roster.sortColumn) {
            
            if (roster.viewUserDisplayId) {
                overviewSortParams.sortList = [[1,0]];
            }
            
        } else if (roster.SORT_EMAIL === roster.sortColumn) {
            
            if (roster.viewEmail) {
                
                if (roster.viewUserDisplayId) {
                    overviewSortParams.sortList = [[2,0]];
                } else {
                    overviewSortParams.sortList = [[1,0]];
                }
            }
            
        } else if (roster.SORT_ROLE === roster.sortColumn) {
        
            if (roster.viewEmail) {
                
                if (roster.viewUserDisplayId) {
                    overviewSortParams.sortList = [[3,0]];
                } else {
                    overviewSortParams.sortList = [[2,0]];
                }
            } else {
                
                if (roster.viewUserDisplayId) {
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
            
        if (roster.viewUserDisplayId) {
            // user ID column (1) has view profile URL attached to it
            groupSortParams = { headers: {1: {sorter: 'urls'}, 3: {sorter: false}}, sortList:roster.DEFAULT_SORT_LIST };
        } else {
            // user name column (0) has view profile URL attached to it
            groupSortParams = { headers: {0: {sorter: 'urls'}, 2: {sorter: false}}, sortList:roster.DEFAULT_SORT_LIST };
        }
        
        // now set the initial sort column
        if (roster.SORT_NAME === roster.sortColumn) {
            groupSortParams.sortList = [[0,0]];
        } else if (roster.SORT_DISPLAY_ID === roster.sortColumn) {
            
            if (roster.viewUserDisplayId) {
                groupSortParams.sortList = [[1,0]];
            }
        } else if (roster.SORT_ROLE === roster.sortColumn) {
            
            if (roster.viewUserDisplayId) {
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
        
        if (roster.viewEmail) {
            enrollmentSortParams = { headers: {1: {sorter: 'urls'}, 2: {sorter:'urls'}}, sortList: [[0,0]] };
        } else {
            enrollmentSortParams = { headers: {1: {sorter: 'urls'}}, sortList: [[0,0]] };
        }
        
        // now set the initial sort column
        // enrollment table doesn't have role, so use name as default sort column
        if (roster.SORT_NAME === roster.sortColumn || roster.SORT_ROLE === roster.sortColumn) {
            enrollmentSortParams.sortList = [[0,0]];
        } else if (roster.SORT_DISPLAY_ID === roster.sortColumn) {
            enrollmentSortParams.sortList = [[1,0]];
        } else if (roster.SORT_EMAIL === roster.sortColumn) {
            
            if (roster.viewEmail) {
                enrollmentSortParams.sortList = [[2,0]];
            }
            
        } else if (roster.SORT_STATUS === roster.sortColumn) {
        
            if (roster.viewEmail) {
                enrollmentSortParams.sortList = [[3,0]];
            } else {
                enrollmentSortParams.sortList = [[2,0]];
            }
        } else if (roster.SORT_CREDITS === roster.sortColumn) {
            
            if (roster.viewEmail) {
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
            
            if (roster.viewUserDisplayId && roster.viewEmail) {
                roster.columnSortFields[1] = roster.SORT_DISPLAY_ID;
                roster.columnSortFields[2] = roster.SORT_EMAIL;
                roster.columnSortFields[3] = roster.SORT_ROLE;
            } else if (roster.viewUserDisplayId) {
                roster.columnSortFields[1] = roster.SORT_DISPLAY_ID;
                roster.columnSortFields[2] = roster.SORT_ROLE;
            } else if (roster.viewEmail) {
                roster.columnSortFields[1] = roster.SORT_EMAIL;
                roster.columnSortFields[2] = roster.SORT_ROLE;
            }
            
        } else if (roster.STATE_GROUP_MEMBERSHIP === state) {
            
            // n.b. no sort by groups column
            
            if (roster.viewUserDisplayId) {
                roster.columnSortFields[1] = roster.SORT_DISPLAY_ID;
                roster.columnSortFields[2] = roster.SORT_ROLE;
            } else {
                roster.columnSortFields[1] = roster.SORT_ROLE;
            }
            
        } else if (roster.STATE_ENROLLMENT_STATUS === state) {
            
            if (roster.viewEmail) {
                roster.columnSortFields[2] = roster.SORT_EMAIL;
                roster.columnSortFields[3] = roster.SORT_STATUS;
                roster.columnSortFields[4] = roster.SORT_CREDITS;
            } else {
                roster.columnSortFields[2] = roster.SORT_STATUS;
                roster.columnSortFields[3] = roster.SORT_CREDITS;
            }
        }	
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

    // Functions and attributes added. All the code from hereon is executed
    // after load.

	if (!roster.siteId) {
		alert('The site id  MUST be supplied as a bootstrap parameter.');
		return;
	}
	
	if (!roster.userId) {
		alert("No current user. Have you logged in?");
		return;
	}

    Handlebars.registerPartial('connections', Handlebars.partials['connections']);
    Handlebars.registerPartial('profile_picture', Handlebars.partials['profile_picture']);
    Handlebars.registerPartial('user_info', Handlebars.partials['user_info']);

    Handlebars.registerHelper('translate', function (key) {
        var t = roster.i18n[key];
        if (key === 'title_msg') {
            console.log('VAL:' + t);
        }
        return roster.i18n[key];
    });

    Handlebars.registerHelper('getName', function (firstNameLastName) {
        return (firstNameLastName) ? this.displayName : this.sortName;
    });

    Handlebars.registerHelper('isMe', function (myUserId) {
        return this.userId === myUserId;
    });

    Handlebars.registerHelper('profileDiv', function () {
        return 'profile_friend_' + this.userId;
    });

    Handlebars.registerHelper('unconnected', function () {
	    return this.connectionStatus === CONNECTION_NONE;
    });

    Handlebars.registerHelper('confirmed', function () {
	    return this.connectionStatus === CONNECTION_CONFIRMED;
    });

    Handlebars.registerHelper('requested', function () {
	    return this.connectionStatus === CONNECTION_REQUESTED;
    });

    Handlebars.registerHelper('incoming', function () {
	    return this.connectionStatus === CONNECTION_INCOMING;
    });

    Handlebars.registerHelper('rowComplete', function (index) {

        // index is zero based
	    return (index + 1) % 4 == 0;
    });

    Handlebars.registerHelper('userInfo', function (options) {

        options.hash.displayName = this.displayName;
        options.hash.displayId = this.displayId;
        options.hash.sortName = this.sortName;
        options.hash.userId = this.userId;
        options.hash.formattedProfileUrl = this.formattedProfileUrl;
        options.hash.email = this.email;
        options.hash.connectionStatus = this.connectionStatus;

        var html = Handlebars.partials['user_info'](options.hash);
        return new Handlebars.SafeString(html);
    });

    Handlebars.registerHelper('profilePicture', function (options) {

        options.hash.viewProfile = options.hash.viewProfile === 'true';

        options.hash.formattedProfileUrl = this.formattedProfileUrl;
        options.hash.profileImageUrl = this.profileImageUrl;
        options.hash.displayName = this.displayName;

        var html = Handlebars.partials['profile_picture'](options.hash);
        return new Handlebars.SafeString(html);
    });

    Handlebars.registerHelper('roleAllowed', function (options) {

        var perm = options.hash.permission;
        var role = this.name;

        return roster.site.permissions[role].indexOf(perm) != -1;
    });
	
    $.ajax({
        url: "/direct/roster-membership/" + roster.siteId + "/get-site.json",
        dataType: "json",
        async: false,
        cache: false,
        success: function (data) {

            roster.site = data;
            if (null == roster.site.siteGroups
                    || typeof roster.site.siteGroups === 'undefined') {
                roster.site.siteGroups = [];
            }
            
            if (null == roster.site.userRoles
                    || typeof roster.site.userRoles === 'undefined') {
                roster.site.userRoles = [];
            }
            
            if (null == roster.site.siteEnrollmentSets
                    || typeof roster.site.siteEnrollmentSets === 'undefined') {
                roster.site.siteEnrollmentSets = [];
            }
        }
    });

    // Setup the current user's permissions
    if (roster.userId === roster.ADMIN) {
        // Admin user. Give the full set.
        var data = ['roster.export',
                'roster.viewallmembers',
                'roster.viewenrollmentstatus',
                'roster.viewgroup',
                'roster.viewhidden',
                'roster.viewprofile',
                'site.upd'];

        roster.currentUserPermissions = new roster.RosterPermissions(data);
    } else {
        roster.currentUserPermissions = new roster.RosterPermissions(
            roster.sakai.getCurrentUserPermissions(roster.siteId));
    }
	
	roster.lowResMode = screen.width < roster.lowResModeWidth;
	
	// We need the toolbar in a template so we can swap in the translations
	if (roster.lowResMode) {
		roster.render('navbar_low_res', {
                overviewState: roster.STATE_OVERVIEW,
                picturesState: roster.STATE_PICTURES,
                groupMembershipState: roster.STATE_GROUP_MEMBERSHIP,
                enrollmentStatusState: roster.STATE_ENROLLMENT_STATUS,
                permissionsState: roster.STATE_PERMISSIONS,
			    hasGroups: roster.site.siteGroups.length > 0,
			    canViewEnrollments: roster.currentUserPermissions.viewEnrollmentStatus && roster.site.siteEnrollmentSets.length > 0,
			    canUpdatePermissions: roster.currentUserPermissions.siteUpdate
			},
			'roster_navbar');
		
		$('#roster_navbar_dropdown').change(function () {
			roster.switchState(this.value);
        });
	} else {
		roster.render('navbar', {}, 'roster_navbar');
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
		
    if (roster.SORT_NAME == roster.defaultSortColumn ||
            roster.SORT_DISPLAY_ID == roster.defaultSortColumn ||
            roster.SORT_ROLE == roster.defaultSortColumn ||
            roster.SORT_STATUS == roster.defaultSortColumn ||
            roster.SORT_CREDITS == roster.defaultSortColumn) {
        
        roster.sortColumn = roster.defaultSortColumn;
    } else if (roster.SORT_EMAIL == roster.defaultSortColumn && true == roster.viewEmail) {
        // if chosen sort is email, check that email column is viewable
        roster.sortColumn = roster.defaultSortColumn;
    }
	
	// end of sakai.properties
	
    try {
        if (window.frameElement) {
            window.frameElement.style.minHeight = '600px';
        }
    } catch (err) {}
		
	// Now switch into the requested state
	roster.switchState(roster.state, roster);

}) (jQuery);
