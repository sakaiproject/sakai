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
            
        if (roster.STATE_OVERVIEW === state) {

            $('#navbar_overview_link > span').addClass('current');

            $('#roster_header').html('');
            $('#roster_section_filter').html('');
            $('#roster_search').html('');
        
            var overviewCallback = function (members) {
                
                    var renderOverview = function (members) {

                            roster.render('overview',
                                { 'members': members,
                                    'firstNameLastName': roster.firstNameLastName,
                                    'viewEmail': roster.viewEmail,
                                    'viewUserDisplayId': roster.viewUserDisplayId,
                                    'viewProfile': roster.currentUserPermissions.viewProfile,
                                    'viewPicture': true,
                                    'siteGroups': roster.site.siteGroups,
                                    'viewOfficialPhoto': roster.currentUserPermissions.viewOfficialPhoto,
                                    'viewConnections': (undefined != window.friendStatus)},
                                'roster_content');

                            $(document).ready(function () {

                                roster.renderMembers(members, 'roster-members');
                            });
                        };

                    renderOverview(members);
                
                    $(document).ready(function () {
                        
                        roster.readyExportButton(state);
                        roster.readySearchButton(state);
                        roster.readySearchField(searchQuery);
                        roster.readyClearButton(state);

                        var onGroupClicked = function (e) {

                                var groupId = this.id.substring(13);

                                if (groupId === roster.DEFAULT_GROUP_ID) {
                                    groupId = null;
                                }

                                roster.getRosterMembership(groupId, false, null, null, null, false, function (members) {

                                    roster.renderMembers(members, 'roster-members');

                                    $(document).ready(function () {

                                        $('.roster-group-link').click(onGroupClicked);
                                    });
                                });
                            };

                        $('.roster-group-link').click(onGroupClicked);
                            
                        if (roster.currentUserPermissions.viewOfficialPhoto) {

                            $('#roster_official_picture_button').click(function (e) {

                                roster.getMembers(searchQuery, true, state, true, function (members) {

                                    renderOverview(members);
                                    roster.setupHideNamesAndSingleColumnButtons(state,searchQuery);
                                    roster.rosterOfficialPictureMode = true;
                                });
                            });
                
                            $('#roster_profile_picture_button').click(function (e) {

                                roster.getMembers(searchQuery, true, state, false, function (members) {

                                    renderOverview(members);
                                    roster.setupHideNamesAndSingleColumnButtons(state,searchQuery);
                                    roster.rosterOfficialPictureMode = false;
                                });
                            });
                        }
                        
                        if(window.frameElement) {
                            setMainFrameHeight(window.frameElement.id);
                        }
                    });
                };

            if ((arg && arg.forceOfficialPicture) || roster.rosterOfficialPictureMode) {
                roster.getMembers(searchQuery, true, state, true, overviewCallback);
            } else {
                roster.getMembers(searchQuery, true, state, false, overviewCallback);
            }
        } else if (roster.STATE_VIEW_PROFILE === state) {
            
            roster.sakai.getProfileMarkup(arg.userId, function (profileMarkup) {
            
                $('#roster_content').html(profileMarkup);
                
                if(window.frameElement) {
                    setMainFrameHeight(window.frameElement.id);
                }
            });
            
        } else if (roster.STATE_ENROLLMENT_STATUS === state) {

            $('#navbar_enrollment_status_link > span').addClass('current');
            
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
        $('#roster_form_pics_view').click(function (e) {
            
            if (roster.viewSingleColumn) {
                roster.viewSingleColumn = false;
            } else {
                roster.viewSingleColumn = true;
            }
            
            roster.switchState(state, null, searchQuery);
        });
    };

    roster.getRosterMembership = function (groupId, sorted, sortField, sortDirection, state, forceOfficialPicture, callback) {

        var url = "/direct/roster-membership/" + roster.siteId + "/get-membership.json?sorted=" + sorted + "&includeConnectionStatus=true";
        
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
                    m.profileImageUrl = "/direct/profile/" + m.userId + "/image";
                    if (forceOfficialPicture) {
                        m.profileImageUrl += "/official";
                    }
                    m.profileImageUrl += "?siteId=" + roster.siteId;
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

        if (roster.groupToView) {
            // view a specific group (note: search is done within group if selected)

            roster.getRosterMembership(roster.groupToView, sorted, null, null, state, forceOfficialPicture, function (members) {
                callback(roster.filter(members, searchQuery));
            });
        } else {
            roster.getRosterMembership(null, sorted, null, null, state,forceOfficialPicture, function (members) {
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
        if (roster.STATE_OVERVIEW === viewType || 
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

    roster.renderMembers = function (members, target) {

        roster.render('members',
            { 'roster.language': roster.language,
                'members': members,
                'siteId': roster.siteId,
                'groupToView' :roster.groupToView,
                'firstNameLastName': roster.firstNameLastName,
                'viewEmail': roster.viewEmail,
                'viewUserDisplayId': roster.viewUserDisplayId,
                'viewProfile': roster.currentUserPermissions.viewProfile,
                'viewPicture': true,
                'currentUserId': roster.userId,
                'viewOfficialPhoto': roster.currentUserPermissions.viewOfficialPhoto,
                'viewConnections': (undefined != window.friendStatus)},
            target);
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

    Handlebars.registerHelper('translate', function (key) {
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
	
	// We need the toolbar in a template so we can swap in the translations
    roster.render('navbar', {}, 'roster_navbar');
	
	$('#navbar_overview_link > span > a').click(function (e) {
		return roster.switchState(roster.STATE_OVERVIEW);
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
