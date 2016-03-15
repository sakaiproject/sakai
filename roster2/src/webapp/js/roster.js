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

	/**
	*	Check if there is no scroll rendered and there are more pages
	*/
    roster.checkScroll = function () {
        // Check if body height is lower than window height (scrollbar missed, maybe you need to get more pages automatically)
        if ($("body").height() <= $(window).height()) {
            setTimeout(function () {
            	var renderedMembers = $(".roster-member").size();
            	// Without filter conditions get more pages if there are more members than rendered and rendered > 0
            	// If you have an active filter maybe you could display less members than total
            	// So get more pages only if rendered match a page size (10 is pagesize)
            	if (roster.site.membersTotal > renderedMembers && renderedMembers > 0 && renderedMembers % 10 === 0) {
                	$("body").data("scroll-roster", true);
                	$(window).trigger('scroll.roster');
            	}
            }, 100);
        }
    };
    
    /**
     * Renders a handlebars template.
     */
    roster.render = function (template, data, outputId) {

        var t = Handlebars.templates[template];
        document.getElementById(outputId).innerHTML = t(data);

    };

    roster.switchState = function (state, arg, searchQuery) {

        roster.currentState = state;

        $('#roster_navbar > li > span').removeClass('current');
        
        // so we can return to the previous state after viewing permissions
        if (state !== roster.STATE_PERMISSIONS) {
            roster.rosterLastStateNotPermissions = state;
        }
        
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

        if (!roster.currentUserPermissions.rosterExport) {
            $('#navbar_export_link').hide();
        }
            
        if (roster.STATE_OVERVIEW === state) {

            roster.enrollmentSetToView = null;
            roster.groupToView = null;
            roster.nextPage = 0;

            $('#navbar_overview_link > span').addClass('current');

            $('#roster_header').html('');
            $('#roster_section_filter').html('');
            $('#roster_search').html('');

            var showOfficialPictures = false;

            if ((arg && arg.forceOfficialPicture) || roster.rosterOfficialPictureMode) {
                showOfficialPictures = true;
            }

            roster.render('overview',
                { siteGroups: roster.site.siteGroups,
                    membersTotal: roster.i18n.currently_displaying_participants.replace(/\{0\}/, roster.site.membersTotal),
                    roleFragments: roster.getRoleFragments(roster.site.roleCounts),
                    roles: roster.site.userRoles,
                    checkOfficialPicturesButton: showOfficialPictures,
                    viewOfficialPhoto: roster.currentUserPermissions.viewOfficialPhoto },
                'roster_content');

            $(document).ready(function () {

                $('#roster-groups-selector-top').change(function (e) {

                    if (this.value === 'all') {
                        roster.groupToView = null;
                        roster.renderMembership({ forceOfficialPicture: showOfficialPictures, replace: true });
                    } else {
                        roster.renderGroupMembership(this.value, showOfficialPictures);
                    }
                });

                $('#roster-roles-selector').change(function (e) {

                    if (this.value === 'all') {
                        roster.roleToView = null;
                        roster.renderMembership({ forceOfficialPicture: showOfficialPictures, replace: true });
                    } else {
                        roster.roleToView = this.value;
                        roster.renderMembership({ forceOfficialPicture: showOfficialPictures, replace: true });
                    }
                });

                if (roster.currentUserPermissions.viewOfficialPhoto) {

                    $('#roster_official_picture_button').click(function (e) {

                        roster.rosterOfficialPictureMode = true;
                        roster.renderMembership({ forceOfficialPicture: true, replace: true });
                    });
        
                    $('#roster_profile_picture_button').click(function (e) {

                        roster.rosterOfficialPictureMode = false;
                        roster.renderMembership({ forceOfficialPicture: false, replace: true });
                    });
                }

                roster.readySearchButton();
                roster.readySearchField();
                roster.readyClearButton(state);

                // We don't want parallel membership requests
	            $('#navbar_overview_link > span > a').off('click');

                roster.renderMembership({ forceOfficialPicture: showOfficialPictures, replace: true });
            });

            $(window).off('scroll.roster').on('scroll.roster', roster.getScrollFunction(showOfficialPictures));
        } else if (roster.STATE_VIEW_PROFILE === state) {
            
            roster.sakai.getProfileMarkup(arg.userId, function (profileMarkup) {
            
                $('#roster_content').html(profileMarkup);
                
                if(window.frameElement) {
                    setMainFrameHeight(window.frameElement.id);
                }
            });
            
        } else if (roster.STATE_ENROLLMENT_STATUS === state) {

            roster.nextPage = 0;
            roster.groupToView = null;

            $('#navbar_enrollment_status_link > span').addClass('current');
            
            if (null === roster.enrollmentSetToView && null != roster.site.siteEnrollmentSets[0]) {
                roster.enrollmentSetToView = roster.site.siteEnrollmentSets[0].id;
                roster.groupToView = null;
            }

            var showOfficialPictures = false;

            if ((arg && arg.forceOfficialPicture) || roster.rosterOfficialPictureMode) {
                showOfficialPictures = true;
            }

            roster.render('enrollment_overview',
                { enrollmentSets: roster.site.siteEnrollmentSets,
                    onlyOne: roster.site.siteEnrollmentSets.length == 1,
                    enrollmentStatusCodes: roster.site.enrollmentStatusCodes,
                    viewOfficialPhoto: roster.currentUserPermissions.viewOfficialPhoto },
                'roster_content');

            $(document).ready(function () {

                $('#roster-enrollmentset-selector').change(function (e) {

                    var option = this.options[this.selectedIndex];
                    roster.enrollmentSetToView = option.value;
                    roster.enrollmentSetToViewText = option.text;
                    roster.renderMembership({ forceOfficialPicture: showOfficialPictures, replace: true });
                });

                $('#roster-status-selector').change(function (e) {

                    roster.renderMembership({ forceOfficialPicture: showOfficialPictures, replace: true, enrollmentStatus: this.value });
                });

                if (roster.currentUserPermissions.viewOfficialPhoto) {

                    $('#roster_official_picture_button').click(function (e) {

                        roster.rosterOfficialPictureMode = true;
                        roster.renderMembership({ forceOfficialPicture: true, replace: true });
                    });
        
                    $('#roster_profile_picture_button').click(function (e) {

                        roster.rosterOfficialPictureMode = false;
                        roster.renderMembership({ forceOfficialPicture: false, replace: true });
                    });
                }

                roster.readySearchButton();
                roster.readySearchField();
                roster.readyClearButton(state);

                // We don't want parallel membership requests
                $('#navbar_enrollment_status_link').off('click');

                roster.renderMembership({ forceOfficialPicture: showOfficialPictures, replace: true });
            });
        } else if (roster.STATE_PERMISSIONS === state) {

            $('#navbar_permissions_link > span').addClass('current');
            
            $('#roster_section_filter').html('');
            $('#roster_search').html('');

            roster.sakai.getSitePermissionMatrix(roster.siteId, function (permissions) {

                roster.site.permissions = permissions;

                var roles = Object.keys(permissions).map(function (role) {
                        return {name: role};
                    });
            
                roster.render('permissions', { siteTitle: roster.site.title, roles: roles }, 'roster_content');
                
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

    roster.renderGroupMembership = function (groupId, showOfficialPictures) {

        if (groupId === roster.DEFAULT_GROUP_ID) {
            groupId = null;
        } else {
            $('#roster-members').empty();
        }

        $('#roster-search-field').val('');

        roster.groupToView = groupId;

        roster.renderMembership({ forceOfficialPicture: showOfficialPictures, replace: true });
    };

    roster.renderMembership = function (options) {

        var enrollmentsMode = (roster.enrollmentSetToView) ? true : false;

        if (options.replace) {
            $('#roster-members').empty();
            roster.nextPage = 0;

            $(window).off('scroll.roster').on('scroll.roster', roster.getScrollFunction(options.forceOfficialPicture, options.enrollmentStatus));
        }

        var url = "/direct/roster-membership/" + roster.siteId;
        
        if (options.userId) {
            url += "/get-user.json?userId=" + options.userId;
            if (roster.enrollmentSetToView) {
                url += "&enrollmentSetId=" + roster.enrollmentSetToView;
            }
        } else {
            url += "/get-membership.json?page=" + roster.nextPage;
            if (roster.groupToView) {
                url += "&groupId=" + roster.groupToView;
            } else if (roster.enrollmentSetToView) {
                url += "&enrollmentSetId=" + roster.enrollmentSetToView;
            }

            if (roster.roleToView) {
                url += "&roleId=" + roster.roleToView;
            }
        }

        if (options.enrollmentStatus) {
            url += '&enrollmentStatus=' + options.enrollmentStatus;
        }

        var loadImage = $('#roster-loading-image')
        loadImage.show();

        $.ajax({
            url: url,
            dataType: "json",
            cache: false,
            success: function (data) {

                if (data.status && data.status === 'END') {
                    loadImage.hide();

                    if (roster.nextPage === 0) {
                        var membersTotalString = roster.i18n.currently_displaying_participants.replace(/\{0\}/, 0);
                        $('#roster-members-total').html(membersTotalString);
                        $('#roster-role-totals').html('');
                    }

                    return;
                }

                var members = data.members;

                if (roster.nextPage === 0) {
                    var membersTotalString = roster.i18n.currently_displaying_participants.replace(/\{0\}/, data.membersTotal);
                    $('#roster-members-total').html(membersTotalString);
                    var roleFragments = roster.getRoleFragments(data.roleCounts);
                    $('#roster-role-totals').html(roleFragments);
                }

                members.forEach(function (m) {

                    m.formattedProfileUrl = "/direct/profile/" + m.userId + "/formatted?siteId=" + roster.siteId;
                    m.profileImageUrl = "/direct/profile/" + m.userId + "/image";
                    if (options.forceOfficialPicture) {
                        m.profileImageUrl += "/official";
                    }
                    m.profileImageUrl += "?siteId=" + roster.siteId;
                    var groupIds = Object.keys(m.groups);
                    m.hasGroups = groupIds.length > 0;

                    m.singleGroup = null;
                    if (groupIds.length == 1) {
                        var singleGroupId = groupIds[0];
                        m.singleGroup = { id: groupIds[0], title: m.groups[groupIds[0]] };
                    }

                    m.enrollmentStatusText = roster.site.enrollmentStatusCodes[m.enrollmentStatusId];

                    if (m.totalSiteVisits > 0) {
                        m.formattedLastVisitTime = roster.formatDate(m.lastVisitTime);
                    } else {
                        m.formattedLastVisitTime = roster.i18n.no_visits_yet;
                    }
                });

                roster.renderMembers(members, $('#roster-members'), enrollmentsMode);

                $(document).ready(function () {

                    $('.roster-single-group-link').click(function (e) {

                        var value = $(this).attr('data-groupid');

                        roster.renderGroupMembership(value, options.forceOfficialPicture);

                        $('#roster-group-option-' + value).prop('selected', true);
                    });

                    $('.roster-groups-selector').off('change').on('change', function (e) {

                        var value = this.value;

                        roster.renderGroupMembership(this.value, options.forceOfficialPicture);

                        $('#roster-group-option-' + value).prop('selected', true);
                    });

                    $('a.profile').cluetip({
                        width: '640px',
                        cluetipClass: 'roster',
                        sticky: true,
                        dropShadow: false,
                        arrows: true,
                        mouseOutClose: true,
                        closeText: '<img src="/library/image/silk/cross.png" alt="close" />',
                        closePosition: 'top',
                        showTitle: false,
                        hoverIntent: true
                    });
                });

                if (roster.nextPage === 0) {
                    // We've just pulled the first page ...
                    if (roster.currentState === roster.STATE_OVERVIEW) {
                        // ... and are in OVERVIEW mode, so switch the link back on
                        $('#navbar_overview_link > span > a').click(function (e) {
                            return roster.switchState(roster.STATE_OVERVIEW);
                        });
                    } else if (roster.currentState === roster.STATE_ENROLLMENT_STATUS) {
                        // ... and are in ENROLLMENT_STATUS mode, so switch the link back on
                        $('#navbar_enrollment_status_link > span > a').click(function (e) {
                            return roster.switchState(roster.STATE_ENROLLMENT_STATUS);
                        });
                    }
                }

                roster.nextPage += 1;

                $(window).off('scroll.roster').on('scroll.roster', roster.getScrollFunction(options.forceOfficialPicture));

                loadImage.hide();
            },
            error: function (jqXHR, textStatus, errorThrown) {

                console.log('Failed to get membership data. textStatus: ' + textStatus + '. errorThrown: ' + errorThrown);
            }
        });
    };

    roster.readyClearButton = function (state) {
        
        $('#roster_form_clear_button').click(function (e) {

            roster.roleToView = null;
            roster.switchState(state);
        });
    };

    roster.search = function (query) {

        if (query !== roster.i18n.roster_search_text && query !== "") {
            var userId = roster.searchIndex[query];
            roster.renderMembership({ forceOfficialPicture: false, replace: true, userId: userId });
        }
    };

    roster.readySearchButton = function () {

        $('#roster-search-button').off('click').on('click', function (e) {

            var searchFieldValue = $('#roster-search-field').val();
            roster.search(searchFieldValue);
        });
    };

    roster.readySearchField = function () {

        var field = $('#roster-search-field');

        field.autocomplete({
            source: roster.searchIndexKeys,
            select: function (event, ui) {

                roster.search(ui.item.value);
            }
        });
    };

    roster.renderMembers = function (members, target, enrollmentsMode) {

        var templateData = {
                'roster.language': roster.language,
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
                'viewSiteVisits': roster.currentUserPermissions.viewSiteVisits,
                'viewConnections': ((undefined != window.friendStatus) && roster.viewConnections)
            };

        var templateName = (enrollmentsMode) ? 'enrollments' : 'members';
        $(window).off('scroll.roster.rendered').on('scroll.roster.rendered', roster.checkScroll);
        var t = Handlebars.templates[templateName];
        target.append(t(templateData));
        $(window).trigger('scroll.roster.rendered');
    };

    roster.getScrollFunction = function (showOfficialPictures, enrollmentStatus) {

        var scroller = function () {

            var wintop = $(window).scrollTop(), docheight = $(document).height(), winheight = $(window).height();
     
            if  ((wintop/(docheight-winheight)) > 0.95 || $("body").data("scroll-roster") === true) {
                $("body").data("scroll-roster", false);
                $(window).off('scroll.roster');
                if (showOfficialPictures) {
                    roster.renderMembership({ forceOfficialPicture: true, replace: false, enrollmentStatus: enrollmentStatus });
                } else {
                    roster.renderMembership({ forceOfficialPicture: false, replace: false, enrollmentStatus: enrollmentStatus });
                }
            }
        };

        return scroller;
    };

    roster.getRoleFragments = function (roleCounts) {

        return Object.keys(roleCounts).map(function (key) {

            var frag = roster.i18n.role_breakdown_fragment.replace(/\{0\}/, roleCounts[key]);
            return frag.replace(/\{1\}/, key);
        }).join();
    };

    roster.formatDate = function (time) {

        var d = new Date(time);
        var hours = d.getHours();
        if (hours < 10)  hours = '0' + hours;
        var minutes = d.getMinutes();
        if (minutes < 10) minutes = '0' + minutes;
        return d.getDate() + " " + roster.i18n.months[d.getMonth()] + " " + d.getFullYear() + " @ " + hours + ":" + minutes;
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

    Handlebars.registerHelper('roleAllowed', function (options) {

        var perm = options.hash.permission;
        var role = this.name;

        return roster.site.permissions[role].indexOf(perm) != -1;
    });

    roster.init = function () {

	    roster.i18n = $.i18n.map;

        roster.i18n.months = roster.i18n.months.split(',');

        roster.ADMIN = 'admin';

        roster.STATE_OVERVIEW = 'overview';
        roster.STATE_ENROLLMENT_STATUS = 'status';
        roster.STATE_VIEW_PROFILE = 'profile';
        roster.STATE_PERMISSIONS = 'permissions';

        roster.DEFAULT_GROUP_ID = 'all';
        roster.DEFAULT_ENROLLMENT_STATUS = 'All';
        roster.DEFAULT_STATE = roster.STATE_OVERVIEW;

        /* Stuff that we always expect to be setup */
        roster.language = null;

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
        roster.rosterOfficialPictureMode = false;
        roster.nextPage = 0;
        roster.currentState = null;

        roster.rosterOfficialPictureMode = roster.officialPicturesByDefault;

        // We need the toolbar in a template so we can swap in the translations
        roster.render('navbar', {}, 'roster_navbar');
        
        $('#navbar_overview_link > span > a').click(function (e) {
            return roster.switchState(roster.STATE_OVERVIEW);
        });

        $('#navbar_enrollment_status_link > span > a').click(function (e) {
            return roster.switchState(roster.STATE_ENROLLMENT_STATUS);
        });

        $('#navbar_export_link > span > a').click(function (e) {

            e.preventDefault();
            
            var baseUrl = "/direct/roster-export/" + roster.siteId +
                "/export-to-excel?viewType=" + roster.currentState;
            
            var facetParams = "&facetName=" + roster.i18n.facet_name +
                "&facetUserId=" + roster.i18n.facet_userId +
                "&facetEmail=" + roster.i18n.facet_email +
                "&facetRole=" + roster.i18n.facet_role +
                "&facetGroups=" + roster.i18n.facet_groups +
                "&facetStatus=" + roster.i18n.facet_status +
                "&facetCredits=" + roster.i18n.facet_credits;
            
            if (roster.STATE_OVERVIEW === roster.currentState) {
                var groupId = null;
                if (null != roster.groupToView) {
                    groupId = roster.groupToView;
                } else {
                    groupId = roster.DEFAULT_GROUP_ID;
                }
            
                if (null != roster.roleToView) {
                    baseUrl += "&roleId=" + roster.roleToView;
                }
        
                window.location.href = baseUrl + "&groupId=" + groupId + facetParams;
            } else if (roster.STATE_ENROLLMENT_STATUS === roster.currentState) {
            
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
        
        $('#navbar_permissions_link > span > a').click(function (e) {
            return roster.switchState(roster.STATE_PERMISSIONS);
        });
                
        $.ajax({
            url: '/direct/roster-membership/' + roster.siteId + '/get-search-index.json',
            dataType: "json",
            success: function (data) {
                roster.searchIndex = data.data;
                roster.searchIndexKeys = Object.keys(data.data);
                // Now switch into the requested state
                roster.switchState(roster.state, roster);
            },
            error: function () {
            }
        });
    };

    roster.loadSiteDataAndInit = function () {

        $.ajax({
            url: "/direct/roster-membership/" + roster.siteId + "/get-site.json",
            dataType: "json",
            cache: false,
            success: function (data) {

                roster.site = data || {};

                if (!roster.site.siteGroups) roster.site.siteGroups = [];

                if (!roster.site.userRoles) roster.site.userRoles = [];

                if (!roster.site.siteEnrollmentSets) roster.site.siteEnrollmentSets = [];

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
                    roster.init();
                } else {
                    roster.sakai.setCurrentUserPermissions(roster.siteId, function () { roster.init(); });
                }
            }
        });
    };

    // jquery.i18n
    $.i18n.properties({
        name:'ui',
        path:'/sakai-roster2-tool/i18n/',
        mode: 'both',
        async: true,
        checkAvailableLanguages: true,
        language: roster.language,
        callback: function () {
            roster.loadSiteDataAndInit();
        }
    });
}) (jQuery);
