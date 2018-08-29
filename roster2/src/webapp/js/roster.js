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

    roster.setupPrintButton = function () {

        $('.roster-print-button').click(function (e) {

            var button = $(this);

            button.prop('disabled', true);

            e.preventDefault();
            roster.renderMembership({renderAll: true, callback: function () {

                    $('#roster-members-content').waitForImages(function () {

                        button.prop('disabled', false);
                        window.print();
                    });
                }
            });
        });
    };

    /**
    *   Check if there is no scroll rendered and there are more pages
    */
    roster.checkScroll = function () {

        // Check if body height is lower than window height (scrollbar missed, maybe you need to get more pages automatically)
        // -12 is to account for ajax loading image height
        if (($("body").height() - 12) <= $(window).height()) {
            setTimeout(function () {
                var renderedMembers = $(".roster-member").size();
                // Without filter conditions get more pages if there are more members than rendered and rendered > 0
                // If you have an active filter maybe you could display less members than total
                // So get more pages only if rendered match a page size
                if (roster.site.membersTotal > renderedMembers && renderedMembers > 0 && renderedMembers % roster.pageSize === 0) {
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

    roster.addViewModeHandlers = function() {
        $('#roster-card-view-radio').click(roster.clickViewCardRadio);
        $('#roster-photo-view-radio').click(roster.clickViewPhotogridRadio);
        $('#roster-spreadsheet-view-radio').click(roster.clickViewSpreadsheetRadio);
    }

    roster.addPhotoSourceHandlers = function() {
        if (roster.currentUserPermissions.viewOfficialPhoto) {

            $('#roster_official_picture_button').click(function (e) {
                roster.officialPictureMode = true;
                roster.renderMembership({ replace: true});
            });

            $('#roster_profile_picture_button').click(function (e) {
                roster.officialPictureMode = false;
                roster.renderMembership({ replace: true });
            });
        }
    }

    roster.addHideOptionHandlers = function() {
        $('#roster-hide-pictures-checkbox').click(function (e) {
            if ($(this).prop('checked')) {
                $('.roster-picture-cell').addClass('roster-hide-pictures');
                $('#roster-picture-header-cell').addClass('roster-hide-pictures');
            } else {
                $('.roster-picture-cell').removeClass('roster-hide-pictures');
                $('#roster-picture-header-cell').removeClass('roster-hide-pictures');
            }
            roster.checkScroll();
        });
        $('#roster-hide-names-checkbox').click(function (e) {
            if ($(this).prop('checked')) {
                $('.roster-name-cell, .roster-userId-cell, .roster-role-cell').addClass('roster-hide-names');
            } else {
                $('.roster-name-cell, .roster-userId-cell, .roster-role-cell').removeClass('roster-hide-names');
            }
            roster.checkScroll();
        });
    }

    roster.changeActiveTab = function(state) {
        roster.initNavBar();
        var activeID = '';
        if (roster.STATE_OVERVIEW === state) {
            activeID = '#navbar_overview_link';
        } else if (roster.STATE_ENROLLMENT_STATUS === state) {
            activeID = '#navbar_enrollment_status_link';
        } else if (roster.STATE_PERMISSIONS === state) {
            activeID = '#navbar_permissions_link';
        }

        if (activeID !== '') {
            $(activeID + ' > span').addClass('current');
            var tabText = $(activeID + ' > span > a').text();
            $(activeID + ' > span > a').remove();
            $(activeID + ' > span').text(tabText);
        }
    }

    roster.switchState = function (state, args) {

        roster.currentState = state;
        roster.changeActiveTab(state);

        // so we can return to the previous state after viewing permissions
        if (state !== roster.STATE_PERMISSIONS) {
            roster.rosterLastStateNotPermissions = state;
        }
        
        // permissions
        if (roster.showPermsToMaintainers) {
            $('#navbar_permissions_link').show();
        } else {
            $('#navbar_permissions_link').hide();
        }
        
        // don't show enrollments tab if user doesn't have permission, or there's no enrollment sets attached to the site
        if (!roster.currentUserPermissions.viewEnrollmentStatus ||
                roster.site.siteEnrollmentSets.length === 0) {

            $('#navbar_enrollment_status_link').hide();

            // this can happen if roster.default.state=3
            if (roster.STATE_ENROLLMENT_STATUS === state) {
                state = roster.DEFAULT_STATE;
            }
        }
            
        if (roster.STATE_OVERVIEW === state) {

            roster.enrollmentSetToView = null;
            roster.enrollmentStatus = 'all';
            roster.groupToView = (args && args.group) ? args.group : null;
            roster.roleToView = null;
            roster.nextPage = 0;

            roster.render('overview',
                { siteGroups: roster.site.siteGroups,
                    membersTotal: roster.i18n.currently_displaying_participants.replace(/\{0\}/, roster.site.membersTotal),
                    roleFragments: roster.getRoleFragments(roster.site.roleCounts),
                    roles: roster.site.userRoles.sort(),
                    checkOfficialPicturesButton: roster.officialPictureMode,
                    viewGroup : roster.currentUserPermissions.viewGroup,
                    viewOfficialPhoto: roster.currentUserPermissions.viewOfficialPhoto },
                'roster_content');

            $(document).ready(function () {

                if (args && args.group) {
                    $('#roster-group-option-' + args.group).prop('selected', true);
                }

                roster.addPhotoSourceHandlers();
                roster.addHideOptionHandlers();
                roster.addViewModeHandlers();
                roster.addExportHandler();
                roster.clickViewCardRadio(false);
                roster.setupPrintButton();
                roster.readySearchButton();
                roster.readySearchField();
                roster.readyClearButton(state);

                $('#roster-groups-selector').change(function (e) {

                    if (this.value === 'all') {
                        roster.groupToView = null;
                        roster.renderMembership({ replace: true });
                    } else {
                        roster.renderGroupMembership(this.value);
                    }
                    
                    roster.updateUserFilter();
                });

                $('#roster-roles-selector').change(function (e) {

                    if (this.value === 'all') {
                        roster.roleToView = null;
                    } else {
                        roster.roleToView = this.value;
                    }
                    roster.renderMembership({ replace: true});
                });

                roster.renderMembership({ replace: true });
            });

            $(window).off('scroll.roster').on('scroll.roster', roster.getScrollFunction({}));
        } else if (roster.STATE_ENROLLMENT_STATUS === state) {

            roster.nextPage = 0;
            roster.roleToView = null;
            roster.groupToView = null;
            
            if (null === roster.enrollmentSetToView && null !== roster.site.siteEnrollmentSets[0]) {
                roster.enrollmentSetToView = roster.site.siteEnrollmentSets[0].id;
            }

            roster.render('enrollment_status',
                { enrollmentSets: roster.site.siteEnrollmentSets,
                    onlyOne: roster.site.siteEnrollmentSets.length === 1,
                    enrollmentStatusCodes: roster.site.enrollmentStatusCodes,
                    viewOfficialPhoto: roster.currentUserPermissions.viewOfficialPhoto },
                'roster_content');

            $(document).ready(function () {

                roster.addPhotoSourceHandlers();
                roster.addHideOptionHandlers();
                roster.addViewModeHandlers();
                roster.clickViewSpreadsheetRadio();
                roster.addExportHandler();
                roster.setupPrintButton();
                roster.readySearchButton();
                roster.readySearchField();
                roster.readyClearButton(state);

                $('#roster-enrollmentset-selector').change(function (e) {

                    var option = this.options[this.selectedIndex];
                    roster.enrollmentSetToView = option.value;
                    roster.enrollmentSetToViewText = option.text;
                    roster.renderMembership({ replace: true });
                });

                $('#roster-status-selector').change(function (e) {

                    roster.enrollmentStatus = this.value;
                    if (roster.enrollmentStatus === '') roster.enrollmentStatus = 'all';
                    roster.renderMembership({ replace: true });
                });
                roster.renderMembership({ replace: true });
            });
        } else if (roster.STATE_PERMISSIONS === state) {

            roster.sakai.getSitePermissionMatrix(roster.siteId, function (permissions) {

                roster.site.permissions = permissions;

                var roles = Object.keys(permissions).map(function (role) {
                        return {name: role};
                    });
            
                roster.render('permissions', { siteTitle: roster.site.title, showVisits: roster.showVisits, roles: roles }, 'roster_content');
                
                $(document).ready(function () {

                    $('#roster_permissions_save_button').click(function () {

                       roster.sakai.savePermissions(roster.siteId, 'roster_permission_checkbox',
                               function () { window.location.reload(); } );
                    });
                    
                    $('#roster_cancel_button').click(function () { roster.switchState(roster.rosterLastStateNotPermissions); } );
                });
            });
        }
    };

    roster.renderGroupMembership = function (groupId) {

        if (groupId === roster.DEFAULT_GROUP_ID) {
            groupId = null;
        }

        $('#roster-search-field').val('');

        roster.groupToView = groupId;
        roster.renderMembership({ replace: true });
    };

    roster.renderMembership = function (options) {

        var enrollmentsMode = roster.currentState === roster.STATE_ENROLLMENT_STATUS;

        if (options.replace) {
            $('#roster-members').empty();
            roster.nextPage = 0;

            // Render the table header
            roster.render('members_header', {
                viewEmail: roster.viewEmail,
                viewUserDisplayId: roster.viewUserDisplayId,
                viewUserProperty: roster.viewUserProperty,
                viewProfile: roster.currentUserPermissions.viewProfile,
                viewGroup : roster.currentUserPermissions.viewGroup,
                viewPicture: true,
                viewSiteVisits: roster.currentUserPermissions.viewSiteVisits,
                viewConnections: ((undefined !== window.friendStatus) && roster.viewConnections),
                enrollmentsMode: enrollmentsMode,
                showVisits: roster.showVisits,
                }, 'roster-members-content');

            $(window).off('scroll.roster');
        }

        if (options.renderAll) {
            $('#roster-members').empty();
            $(window).off('scroll.roster');
        }

        var url = "/direct/roster-membership/" + roster.siteId;
        
        if (options.userIds) {
            url += "/get-users.json?userIds=" + options.userIds.join(',');
            if (roster.enrollmentSetToView) {
                url += "&enrollmentSetId=" + roster.enrollmentSetToView;
            }
        } else {
            url += '/get-membership.json?';
            if (options.renderAll) {
                url += 'all=true';
            } else {
                url += 'page=' + roster.nextPage;
            }
            if (roster.groupToView) {
                url += "&groupId=" + roster.groupToView;
            } else if (roster.enrollmentSetToView) {
                url += "&enrollmentSetId=" + roster.enrollmentSetToView;
            }

            if (roster.roleToView) {
                url += "&roleId=" + encodeURIComponent(roster.roleToView);
            }
        }

        if (roster.enrollmentStatus) {
            url += '&enrollmentStatus=' + roster.enrollmentStatus;
        }

        // Calculate dynamic page sizes if not already set
        if (roster.cardsPageSize === undefined || roster.gridPageSize === undefined) {
            roster.calculatePageSizes();
        }

        if ($('#roster_content').hasClass('view_mode_cards')) {
            roster.pageSize = roster.cardsPageSize;
        } else if ($('#roster_content').hasClass('view_mode_photogrid')) {
            roster.pageSize = roster.gridPageSize;
        }

        url += '&pageSize=' + roster.pageSize;

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

                    m.profileImageUrl = "/direct/profile/" + m.userId + "/image";
                    if (roster.officialPictureMode) {
                        m.profileImageUrl += "/official";
                    }
                    m.profileImageUrl += "?siteId=" + encodeURIComponent(roster.siteId);

                    var groupIds = Object.keys(m.groups);
                    m.hasGroups = groupIds.length > 0;
                    var groups = groupIds.map(function (id) { return {id: id, title: m.groups[id]} });
                    m.groups = groups;
                    m.singleGroup = null;
                    if (groupIds.length === 1) {
                        var singleGroupId = groupIds[0];
                        m.singleGroup = { id: singleGroupId, title: m.groups[singleGroupId] };
                    }

                    if (roster.showVisits) {
                        if (m.totalSiteVisits > 0) {
                            m.formattedLastVisitTime = roster.formatDate(m.lastVisitTime);
                        } else {
                            m.formattedLastVisitTime = roster.i18n.no_visits_yet;
                        }
                    }
                });

                roster.renderMembers(members, $('#roster-members'), enrollmentsMode);

                $(document).ready(function () {

                    if ($('#roster-hide-pictures-checkbox').prop('checked')) {
                        $('.roster-picture-cell').addClass('roster-hide-pictures');
                        $('#roster-picture-header-cell').addClass('roster-hide-pictures');
                    }
                    if ($('#roster-hide-names-checkbox').prop('checked')) {
                        $('.roster-name-cell, .roster-userId-cell, .roster-role-cell').addClass('roster-hide-names');
                    }

                    $('.roster-group-link').click(function (e) {

                        var value = $(this).attr('data-groupid');

                        if (roster.currentState === roster.STATE_ENROLLMENT_STATUS) {
                            roster.switchState(roster.STATE_OVERVIEW, {group: value});
                        } else {
                            $('#roster-group-option-' + value).prop('selected', true);
                            roster.renderGroupMembership(value);
                        }
                    });

                    $('.roster-groups-selector').off('change').on('change', function(e) {
                        var value = this.value;

                        if (roster.currentState === roster.STATE_ENROLLMENT_STATUS) {
                            roster.switchState(roster.STATE_OVERVIEW, {group: value});
                        } else {
                            $('#roster-group-option-' + value).prop('selected', true);
                            roster.renderGroupMembership(value);
                        }
                    });

                    profile.attachPopups($('a.profile'));

                    if (options.userIds) {
                        $(window).off('scroll.roster');
                    } else {
                        if (!options.renderAll) {
                            roster.nextPage += 1;
                            $(window).off('scroll.roster').on('scroll.roster', roster.getScrollFunction({enrollmentsMode: enrollmentsMode}));
                        }
                    }

                    loadImage.hide();

                    if (options.callback) {
                        options.callback();
                    }
                });
            },
            error: function (jqXHR, textStatus, errorThrown) {
                console.log('Failed to get membership data. textStatus: ' + textStatus + '. errorThrown: ' + errorThrown);
            }
        });
    };

    roster.readyClearButton = function (state) {
        
        $('#roster_form_clear_button').click(function (e) {

            roster.roleToView = null;
            roster.groupToView = null;
            roster.switchState(state);
        });
    };

    roster.search = function (query) {

        if (query !== roster.i18n.roster_search_text && query !== "") {
            var userIds = [];
            var userId = roster.searchIndex[query];
            if (!userId) {
                roster.searchIndexKeys.forEach(function (displayName) {

                    var regex = new RegExp(query, 'i');
                    if (regex.test(displayName)) {
                        userIds.push(roster.searchIndex[displayName]);
                    }
                });

                if (userIds.length > 5) {
                    // Limit to 5 users
                    userIds = userIds.slice(0, 5);
                }
            } else {
                userIds.push(userId);
            }

            if (userIds.length > 0) {
                roster.renderMembership({ replace: true, userIds: userIds });
            } else {
                $('#roster-members').html('<div id="roster-information">' + roster.i18n.no_participants + '</div>');
                $('#roster-members-total').hide();
                $('#roster_type_selector').hide();
            }
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

        field.keydown(function (e) {

            if (e.which === 13) {
                e.preventDefault();
                $('#roster-search-button').click();
            }
        });

        field.autocomplete({
            source: roster.searchIndexKeys,
            select: function (event, ui) {
                roster.search(ui.item.value);
            }
        });
    };

    roster.renderMembers = function (members, target, enrollmentsMode, renderAll) {

        var templateData = {
                members: members,
                groupToView :roster.groupToView,
                firstNameLastName: roster.firstNameLastName,
                viewEmail: roster.viewEmail,
                viewUserDisplayId: roster.viewUserDisplayId,
                viewUserProperty: roster.viewUserProperty,
                viewProfile: roster.currentUserPermissions.viewProfile,
                viewGroup : roster.currentUserPermissions.viewGroup,
                viewPicture: true,
                currentUserId: roster.userId,
                viewOfficialPhoto: roster.currentUserPermissions.viewOfficialPhoto,
                enrollmentsMode: enrollmentsMode,
                viewSiteVisits: roster.currentUserPermissions.viewSiteVisits,
                viewConnections: ((undefined !== window.friendStatus) && roster.viewConnections),
                showVisits: roster.showVisits
            };

        if (!renderAll) {
            $(window).off('scroll.roster.rendered').on('scroll.roster.rendered', roster.checkScroll);
        }
        var t = Handlebars.templates['members'];
        target.append(t(templateData));
        if (!renderAll) {
            $(window).trigger('scroll.roster.rendered');
        }
    };

    roster.getScrollFunction = function (options) {

        var scroller = function () {

            var wintop = $(window).scrollTop(), docheight = $(document).height(), winheight = $(window).height();
     
            if  ((wintop/(docheight-winheight)) > 0.95 || $("body").data("scroll-roster") === true) {
                $("body").data("scroll-roster", false);
                $(window).off('scroll.roster');
                roster.renderMembership(options);
            }
        };

        return scroller;
    };

    roster.getRoleFragments = function (roleCounts) {

        return Object.keys(roleCounts).sort().map(function (key) {
            var frag = roster.i18n.role_breakdown_fragment.replace(/\{0\}/, roleCounts[key]);
            return frag.replace(/\{1\}/, '<span class="role">' + key + '</span>');
        }).join(", ");
    };

    roster.formatDate = function (time) {

        var d = new Date(time);
        var hours = d.getHours();
        if (hours < 10)  hours = '0' + hours;
        var minutes = d.getMinutes();
        if (minutes < 10) minutes = '0' + minutes;
        return d.getDate() + " " + roster.i18n.months[d.getMonth()] + " " + d.getFullYear() + " @ " + hours + ":" + minutes;
    };

    roster.addExportHandler = function () {

        var button = $('#roster-export-button');

        if (!roster.currentUserPermissions.rosterExport) {
            button.hide();
        } else {
            button.show();

            $('#roster-export-button').click(function (e) {

                e.preventDefault();

                var baseUrl = "/direct/roster-export/" + roster.siteId +
                    "/export-to-excel?viewType=" + roster.currentState;

                if (roster.STATE_OVERVIEW === roster.currentState) {
                    var groupId = null;
                    if (null !== roster.groupToView) {
                        groupId = roster.groupToView;
                    } else {
                        groupId = roster.DEFAULT_GROUP_ID;
                    }

                    if (null !== roster.roleToView) {
                        baseUrl += "&roleId=" + roster.roleToView;
                    }

                    window.location.href = baseUrl + "&groupId=" + groupId;
                } else if (roster.STATE_ENROLLMENT_STATUS === roster.currentState) {

                    window.location.href = baseUrl + 
                        "&enrollmentSetId=" + roster.enrollmentSetToView +
                        "&enrollmentStatus=" + roster.enrollmentStatus;
                }
            });
        }
    };

    roster.clickViewCardRadio = function(render) {

        // Hide and unapply both 'hide photos', and 'hide names' settings
        if ($('#roster-hide-pictures-checkbox').is(':checked')) {
            $('#roster-hide-pictures-checkbox').trigger('click');
        }
        if ($('#roster-hide-names-checkbox').is(':checked')) {
            $('#roster-hide-names-checkbox').trigger('click');
        }
        $('#roster_hide_options').hide();

        $('#roster_content').removeClass('view_mode_spreadsheet view_mode_photogrid');
        $('#roster_content').addClass('view_mode_cards');

        // Re-render table with dynamic page size for card view
        if (render) {
            roster.renderMembership({ replace: true });
        }
    };

    roster.clickViewSpreadsheetRadio = function() {

        // Hide and unapply 'hide names' setting
        $('#roster_hide_options').show();
        if ($('#roster-hide-names-checkbox').is(':checked')) {
            $('#roster-hide-names-checkbox').trigger('click');
        }
        $('label[for=roster-hide-names-checkbox], #roster-hide-names-checkbox').hide();

        // Show 'hide photos' setting
        $('label[for=roster-hide-pictures-checkbox], #roster-hide-pictures-checkbox').show();

        $('#roster_content').removeClass('view_mode_cards view_mode_photogrid');
        $('#roster_content').addClass('view_mode_spreadsheet');
        roster.checkScroll();
    };

    roster.clickViewPhotogridRadio = function() {

        // Hide and unapply 'hide photos' setting
        $('#roster_hide_options').show();
        if ($('#roster-hide-pictures-checkbox').is(':checked')) {
            $('#roster-hide-pictures-checkbox').trigger('click');
        }
        $('label[for=roster-hide-pictures-checkbox], #roster-hide-pictures-checkbox').hide();

        // Show 'hide names' setting
        $('label[for=roster-hide-names-checkbox], #roster-hide-names-checkbox').show();

        $('#roster_content').removeClass('view_mode_cards view_mode_spreadsheet');
        $('#roster_content').addClass('view_mode_photogrid');

        // Re-render table with dynamic page size for grid view
        roster.renderMembership({ replace: true });
    };

    roster.calculatePageSizes = function() {

        // width of card = width + left and right margin
        // height of card = height + top and bottom margin
        var bigCardWidth = 345; // 340px width + 5px margin-right
        var bigCardHeight = 133; // 128px width + 5px margin-bottom
        var smallCardWidth = 160; // 155px width + 5px margin-right
        var smallCardHeight = 177; // 172px width + 5px margin-bottom

        // width of container = width + left and right padding
        var containerWidth = parseInt($('#roster-members-content').width());

        // number of cards per row = containerWidth / cardWith, rounded down to nearest whole number
        var numBigCardsPerRow = Math.floor(containerWidth / bigCardWidth);
        var numSmallCardsPerRow = Math.floor(containerWidth / smallCardWidth);

        // height of container = height + top and bottom padding;
        // #roster-members-content has no height at load, so we approximate using the morpheus page container
        var containerHeight = parseInt($('div.Mrphs-pagebody').height());

        // number of rows per page = containerHeight / cardHeight, rounded down up nearest whole number
        var numBigRowsPerPage = Math.ceil(containerHeight / bigCardHeight);
        var numSmallRowsPerPage = Math.ceil(containerHeight / smallCardHeight);

        roster.cardsPageSize = numBigRowsPerPage * numBigCardsPerRow;
        roster.gridPageSize = numSmallRowsPerPage * numSmallCardsPerRow;
    };

    // Functions and attributes added. All the code from hereon is executed
    // after load.

    $(window).resize(function() {
        roster.checkScroll();
    });

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

    Handlebars.registerHelper('roleAllowed', function (options) {

        var perm = options.hash.permission;
        var role = this.name;

        return roster.site.permissions[role].indexOf(perm) !== -1;
    });

    roster.init = function () {

        roster.i18n = $.i18n.map;

        roster.i18n.months = roster.i18n.months.split(',');

        roster.ADMIN = 'admin';

        roster.STATE_OVERVIEW = 'overview';
        roster.STATE_ENROLLMENT_STATUS = 'status';
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
        roster.nextPage = 0;
        roster.currentState = null;

        if (!roster.currentUserPermissions.viewOfficialPhoto) {
            // The official photo permission should always override the
            // roster.display.officialPicturesByDefault property
            roster.officialPictureMode = false;
        }
                
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

    roster.initNavBar = function() {

        // We need the toolbar in a template so we can swap in the translations
        roster.render('navbar', {}, 'roster_navbar');

        $('#navbar_overview_link > span > a').click(function (e) {
            return roster.switchState(roster.STATE_OVERVIEW);
        });

        $('#navbar_enrollment_status_link > span > a').on('click', function (e) {
            return roster.switchState(roster.STATE_ENROLLMENT_STATUS);
        });

        $('#navbar_permissions_link > span > a').click(function (e) {
            return roster.switchState(roster.STATE_PERMISSIONS);
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
