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
    // local variables
    var roleContainerPrefix = "roster-members-";
    var roleContainerSuffix = "-container";
    var exportLink;

    roster.setupPrintButton = function () {

        $('.roster-print-button').click(function (e) {

            var button = $(this);

            button.prop('disabled', true);

            e.preventDefault();
            roster.renderMembership({renderAll: true, callback: function () {

                    var container = roster.picturesMode ? $('#roster-members-content') : $('#roster-members');

                    container.waitForImages(function () {

                        button.prop('disabled', false);
                        window.print();
                    });
                }
            });
        });
    };

    roster.setupPicturesButton = function () {

        $('#roster-pictures-only-button').click(function (e) {

            if (this.checked) {
                roster.picturesMode = true;
                roster.renderMembership({ renderAll: true });
            } else {
                roster.picturesMode = false;
                roster.renderMembership({ replace: true });
            }
        });
    };

    /**
    *   Check if there is no scroll rendered and there are more pages
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

    roster.switchState = function (state, arg) {

        roster.currentState = state;

        $('#roster_navbar').find('span').removeClass('current');
        
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
            exportLink.hide();
        }


        if (roster.STATE_OVERVIEW === state) {
            if (roster.currentUserPermissions.rosterExport) {
                exportLink.show();
            }

            roster.enrollmentSetToView = null;
            roster.enrollmentStatus = null;
            roster.groupToView = null;
            roster.roleToView = null;
            roster.nextPage = 0;

            $('#navbar_overview_link').find('span').addClass('current');

            $('#roster_header').html('');
            $('#roster_section_filter').html('');
            $('#roster_search').html('');

            roster.sakai.getSitePermissionMatrix(roster.siteId, function(permissions) {
                roster.site.permissions = permissions;

                roster.render('overview',
                    {
                        siteGroups: roster.site.siteGroups,
                        membersTotal: roster.i18n.currently_displaying_participants.replace(/\{0\}/, roster.site.membersTotal),
                        roleFragments: roster.getRoleFragments(roster.site.roleCounts),
                        roles: roster.site.userRoles,
                        checkOfficialPicturesButton: roster.officialPictureMode,
                        viewGroup: roster.currentUserPermissions.viewGroup,
                        viewOfficialPhoto: roster.currentUserPermissions.viewOfficialPhoto
                    },
                    'roster_content');

                $(document).ready(function () {

                    $('#roster-groups-selector-top').change(function (e) {
                        if (this.value === 'all') {
                            roster.groupToView = null;
                            if(roster.roleToView === null) {
                                renderAll();
                            } else {
                                roster.renderMembership({replace: true});
                            }
                        } else {
                            roster.renderGroupMembership(this.value);
                        }
                    });

                    $('#roster-roles-selector').change(function (e) {
                        if (this.value === 'all') {
                            roster.roleToView = null;
                            if(roster.groupToView === null) {
                                renderAll();
                            } else {
                                roster.renderMembership({replace: true});
                            }
                        } else {
                            roster.roleToView = this.value;
                            roster.renderMembership({replace: true});
                        }
                    });

                    roster.setupPrintButton();
                    roster.setupPicturesButton();

                    if (roster.currentUserPermissions.viewOfficialPhoto) {
                        $('#roster_official_picture_button').click(function (e) {
                            roster.rosterOfficialPictureMode = true;
                            roster.renderMembership({replace: true});
                        });

                        $('#roster_profile_picture_button').click(function (e) {
                            roster.rosterOfficialPictureMode = false;
                            roster.renderMembership({replace: true});
                        });
                    }

                    roster.readySearchButton();
                    roster.readySearchField();
                    roster.readyClearButton(state);

                    renderAll();
                });
            });
            $(window).off('scroll.roster').on('scroll.roster', roster.getScrollFunction({}));
        } else if (roster.STATE_ENROLLMENT_STATUS === state) {
            if (roster.currentUserPermissions.rosterExport) {
                exportLink.show();
            }

            roster.nextPage = 0;
            roster.groupToView = null;

            $('#navbar_enrollment_status_link').find('span').addClass('current');
            
            if (null === roster.enrollmentSetToView && null != roster.site.siteEnrollmentSets[0]) {
                roster.enrollmentSetToView = roster.site.siteEnrollmentSets[0].id;
                roster.groupToView = null;
            }

            roster.render('enrollment_overview',
                { enrollmentSets: roster.site.siteEnrollmentSets,
                    onlyOne: roster.site.siteEnrollmentSets.length === 1,
                    enrollmentStatusCodes: roster.site.enrollmentStatusCodes,
                    viewOfficialPhoto: roster.currentUserPermissions.viewOfficialPhoto },
                'roster_content');

            $(document).ready(function () {

                $('#roster-enrollmentset-selector').change(function (e) {
                    var option = this.options[this.selectedIndex];
                    roster.enrollmentSetToView = option.value;
                    roster.enrollmentSetToViewText = option.text;
                    roster.renderMembership({ replace: true });
                });

                $('#roster-status-selector').change(function (e) {
                    roster.enrollmentStatus = this.value;
                    roster.renderMembership({replace: true});
                });

                roster.setupPrintButton();
                roster.setupPicturesButton();

                if (roster.currentUserPermissions.viewOfficialPhoto) {

                    $('#roster_official_picture_button').click(function (e) {

                        roster.officialPictureMode = true;
                        roster.renderMembership({ replace: true });
                    });
        
                    $('#roster_profile_picture_button').click(function (e) {

                        roster.officialPictureMode = false;
                        roster.renderMembership({ replace: true });
                    });
                }

                roster.readySearchButton();
                roster.readySearchField();
                roster.readyClearButton(state);

                // We don't want parallel membership requests
                $('#navbar_enrollment_status_link').off('click');

                roster.renderMembership({ replace: true });
            });
        } else if (roster.STATE_PERMISSIONS === state) {
            exportLink.hide();
            $('#navbar_permissions_link').find('span').addClass('current');
            
            $('#roster_section_filter').html('');
            $('#roster_search').html('');

            roster.sakai.getSitePermissionMatrix(roster.siteId, function (permissions) {

                roster.site.permissions = permissions;

                var roles = Object.keys(permissions).map(function (role) {
                        return {name: role};
                    });
            
                roster.render('permissions', { siteTitle: roster.site.title, showVisits: roster.showVisits, roles: roles }, 'roster_content');
                
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

    var renderAll = function() {
        // We don't want parallel membership requests
        $('#navbar_overview_link').find('a').off('click');

        var sortedRoles = roster.sortRolesByNumOfPermissions(roster.site.roleCounts, true);
        for(var item in sortedRoles) {
            $("body").removeData(sortedRoles[item]);
        }

        roster.renderMembership({replace: true, role: sortedRoles, overview: true});
    };

    roster.renderButtons = function() {
        roster.render('buttons', {}, 'buttons-container');

        exportLink =  $('#export-link');

        exportLink.click(function (e) {

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
                if (roster.enrollmentStatusToViewText === roster.i18n.roster_enrollment_status_all) {
                    roster.enrollmentStatus = roster.DEFAULT_ENROLLMENT_STATUS;
                } else {
                    roster.enrollmentStatus = roster.enrollmentStatusToViewText;
                }

                window.location.href = baseUrl +
                    "&enrollmentSetId=" + roster.enrollmentSetToView +
                    "&enrollmentStatus=" + roster.enrollmentStatus +
                    facetParams;
            }
        });
    };

    roster.renderGroupMembership = function (groupId) {

        if (groupId === roster.DEFAULT_GROUP_ID) {
            groupId = null;
        }

        $('#roster-search-field').val('');

        roster.groupToView = groupId;
        var sortedRoles = roster.sortRolesByNumOfPermissions(roster.site.roleCounts, true);

        roster.renderMembership({replace: true, role: sortedRoles });
    };

    roster.renderMembership = function (options) {

        var enrollmentsMode = roster.currentState == roster.STATE_ENROLLMENT_STATUS;

        if (roster.picturesMode) {
            // Pictures are always rendered as one batch
            options.renderAll = true;
            options.replace = true;
        }

        if (options.replace) {
            roster.nextPage = 0;

            $(window).off('scroll.roster');
        }

        if (options.renderAll) {
            $('#roster-members-content').empty();
            $(window).off('scroll.roster');
        }

        var url = "/direct/roster-membership/" + roster.siteId;
        var recursive = false;
        
        if (options.userIds) {
            url += "/get-users.json?userIds=" + options.userIds.join(',');
            if (roster.enrollmentSetToView) {
                url += "&enrollmentSetId=" + roster.enrollmentSetToView;
            }
        } else {
            url += "/get-membership.json";
            if (options.renderAll) {
                url += '?all=true';
            } else if (options.rolePage) {
                url += "?page=" +options.rolePage;
            } else {
                url += "?page=" + roster.nextPage;
            }
            if (roster.groupToView) {
                url += "&groupId=" + roster.groupToView;
            } else if (roster.enrollmentSetToView) {
                url += "&enrollmentSetId=" + roster.enrollmentSetToView;
            }

            if (roster.roleToView) {
                url += "&roleId=" + encodeURIComponent(roster.roleToView);
            } else {
                if (options.role) {
                    if (options.role instanceof Array) {
                        url += "&roleId=" + encodeURIComponent(options.role.shift());
                        if (options.role.length > 0) {
                            recursive = true;
                        }
                    } else {
                        url += "&roleId=" + encodeURIComponent(options.role);
                    }
                }
            }
        }

        if (roster.enrollmentStatus) {
            url += '&enrollmentStatus=' + roster.enrollmentStatus;
        }

        var loadImage = $('#roster-loading-image');
        loadImage.show();

        $.ajax({
            url: url,
            dataType: "json",
            cache: false,
            success: function (data) {

                if (data.status && data.status === 'END') {
                    loadImage.hide();

                    if(recursive) {
                        recurse(options);
                    } else {
                        updateCurrentlyShowing();
                        return;
                    }
                }

                var members = data.members;

                var roles = {};
                var numOfRoles = 0;
                members.forEach(function (m) {
                    if(!roles.hasOwnProperty(m.role)) {
                        roles[m.role] = [];
                        roles[m.role].push(m);
                        numOfRoles = numOfRoles + 1;
                    } else {
                        roles[m.role].push(m);
                    }
                    m.profileImageUrl = "/direct/profile/" + m.userId + "/image";
                    if (roster.officialPictureMode) {
                        m.profileImageUrl += "/official";
                    }
                    m.profileImageUrl += "?siteId=" + encodeURIComponent(roster.siteId);

                    var groupIds = Object.keys(m.groups);
                    m.hasGroups = groupIds.length > 0;
                    var groups = groupIds.map(function (id) { return {id: id, title: m.groups[id]} });
                    m.groups = groups;

                    m.enrollmentStatusText = roster.site.enrollmentStatusCodes[m.enrollmentStatusId];

                    if (roster.showVisits) {
                        if (m.totalSiteVisits > 0) {
                            m.formattedLastVisitTime = roster.formatDate(m.lastVisitTime);
                        } else {
                            m.formattedLastVisitTime = roster.i18n.no_visits_yet;
                        }
                    }
                });

                if (roster.picturesMode) {
                    roster.renderPictures(members, $('#roster-members-content'), enrollmentsMode);
                } else {
                    // keep track of each roles page retrieved
                    for(var item in data.roleCounts) {
                        if(roles.hasOwnProperty(item)) {
                            var itemData = $("body").data(item);
                            if(roles[item].length < data.roleCounts[item]) {
                                if(itemData !== undefined) {
                                    // Items currently on page
                                    if(($("#" +roleContainerPrefix + item).children().size() - 2 + roles[item].length) < data.roleCounts[item]){
                                        itemData += 1;
                                        $("body").data(item, itemData);
                                    } else {
                                        $("body").removeData(item);
                                    }
                                } else {
                                    itemData = 1;
                                    $("body").data(item, itemData);
                                }
                            }
                        }
                    }
                    var sorted = roster.sortRolesByNumOfPermissions(roles, false);

                    var sortedLength = sorted.length;
                    for(var i = 0; i < sortedLength; i++) {
                        var id = roleContainerPrefix + sorted[i].replace(/\s+/g, '');
                        roster.renderMembers(roles[sorted[i]], id, enrollmentsMode, data.roleCounts, sorted[i], roster.site.roleCounts);
                    }
                }

                if(recursive) {
                    recurse(options);
                } else {
                    $(document).ready(function () {

                        // Only align cells if NOT in mobile view
                        if($(".roster-mobile-label:visible").size() === 0) {
                            if (!roster.hideNames) {
                                roster.alignNameCellWidths();
                            }
                            if (roster.viewUserDisplayId) {
                                roster.alignUserIdCellWidths();
                            }
                            if (roster.currentUserPermissions.viewEmail) {
                                roster.alignEmailWidths();
                            }
                            if (roster.currentUserPermissions.viewGroup) {
                                roster.alignGroupWidths();
                            }
                            if (roster.viewConnections) {
                                roster.alignConnectionWidths();
                            }
                        } else {
                            roster.alignMobileLabels();
                        }

                        $('.roster-single-group-link').click(function (e) {

                            var value = $(this).attr('data-groupid');
                            roster.renderGroupMembership(value);

                            $('#roster-group-option-' + value).prop('selected', true);
                        });

                        $('.roster-groups-selector').off('change').on('change', function (e) {
                            var value = this.value;

                            roster.renderGroupMembership(value);
                            $('#roster-group-option-' + value).prop('selected', true);
                        });

                        profile.attachPopups($('a.profile'));

                        if (roster.nextPage === 0) {
                            // We've just pulled the first page ...
                            if (roster.currentState === roster.STATE_OVERVIEW) {
                                // ... and are in OVERVIEW mode, so switch the link back on
                                $('#navbar_overview_link').find('a').click(function (e) {
                                    return roster.switchState(roster.STATE_OVERVIEW);
                                });
                            } else if (roster.currentState === roster.STATE_ENROLLMENT_STATUS) {
                                // ... and are in ENROLLMENT_STATUS mode, so switch the link back on
                                $('#navbar_enrollment_status_link').find('a').click(function (e) {
                                    return roster.switchState(roster.STATE_ENROLLMENT_STATUS);
                                });
                            }
                        }

                        if (options.userIds) {
                            $(window).off('scroll.roster');
                        } else {
                            if (!options.renderAll) {
                                roster.nextPage += 1;
                                $(window).off('scroll.roster').on('scroll.roster', roster.getScrollFunction({
                                    enrollmentsMode: enrollmentsMode
                                }));
                            }
                        }

                        loadImage.hide();
                        updateCurrentlyShowing();

                        if (options.callback) {
                            options.callback();
                        }
                    });
                }
            },
            error: function (jqXHR, textStatus, errorThrown) {
                console.log('Failed to get membership data. textStatus: ' + textStatus + '. errorThrown: ' + errorThrown);
            }
        });
    };

    var recurse = function(options) {
        var newRoles = options.role;
        if(newRoles instanceof Array && newRoles.length === 1) {
            newRoles = newRoles.shift();
            options.role = newRoles;
        }
        options.replace = false;

        roster.renderMembership(options);
    };

    var updateCurrentlyShowing = function() {
        var total = 0;
        for(var item in roster.site.roleCounts) {
            total += Number($("#roster-members-" + item.replace(/\s+/g, '') + "-count").text())
        }

        var membersTotalString = roster.i18n.currently_displaying_participants.replace(/\{0\}/, total);
        $('#' + roleContainerPrefix + 'total').html(membersTotalString);
    };

    roster.readyClearButton = function (state) {
        
        $('#roster_form_clear_button').click(function (e) {

            roster.roleToView = null;
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
                $('#roster-members-content').html('<div id="roster-information">' + roster.i18n.no_participants + '</div>');
                $('#' + roleContainerPrefix + 'total').hide();
                $('#roster_type_selector').hide();
                $('#summary').hide();
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

        field.autocomplete({
            source: roster.searchIndexKeys,
            select: function (event, ui) {

                roster.search(ui.item.value);
            }
        });
    };

    roster.renderMembers = function (members, target, enrollmentsMode, roleCounts, role, siteCounts, renderAll) {
        var roleFragment = "";
        if(roleCounts.hasOwnProperty(role)){
            roleFragment = roster.getRoleFragment(role, roleCounts[role]);
        }

        //Determine if page all ready has the roster-member-role container
        var roleContainer = "#" + roleContainerPrefix + role.replace(/\s+/g, '');
        var roleContainerExists = $(roleContainer).length;

        var templateData = {
            members: members,
            groupToView :roster.groupToView,
            firstNameLastName: roster.firstNameLastName,
            viewEmail: roster.viewEmail,
            viewUserDisplayId: roster.viewUserDisplayId,
            viewProfile: roster.currentUserPermissions.viewProfile,
            viewGroup : roster.currentUserPermissions.viewGroup,
            viewPicture: true,
            currentUserId: roster.userId,
            viewOfficialPhoto: roster.currentUserPermissions.viewOfficialPhoto,
            enrollmentsMode: enrollmentsMode,
            viewSiteVisits: roster.currentUserPermissions.viewSiteVisits,
            viewConnections: ((undefined != window.friendStatus) && roster.viewConnections),
            showVisits: roster.showVisits,
            roleFragment : roleFragment,
            rosterMemberId : target,
            freshInstance : (roleContainerExists === 0)
         };

        var headerData = {
            viewEmail: roster.viewEmail,
            viewUserDisplayId: roster.viewUserDisplayId,
            viewProfile: roster.currentUserPermissions.viewProfile,
            viewGroup : roster.currentUserPermissions.viewGroup,
            viewPicture: true,
            viewSiteVisits: roster.currentUserPermissions.viewSiteVisits,
            viewConnections: ((undefined != window.friendStatus) && roster.viewConnections),
            enrollmentsMode: roster.currentState == roster.STATE_ENROLLMENT_STATUS,
            showVisits: roster.showVisits,
            role: role,
            roleFragment: roleFragment,
            rosterMemberId: target,
            container: target + roleContainerSuffix,
            roleCount: roleCounts[role]
        };

        if(!renderAll) {
            $(window).off('scroll.roster.rendered').on('scroll.roster.rendered', roster.checkScroll);
        }
        var t = Handlebars.templates['members'];
        var h = Handlebars.templates['members_header'];
        if (roleContainerExists) {
            $(roleContainer).append(t(templateData));
        } else {
            var sortedRoles = roster.sortRolesByNumOfPermissions(siteCounts, true);
            var index = sortedRoles.indexOf(role);
            var numOfChildren = $('#roster-members-content').children().length;
            if(numOfChildren === 0 || numOfChildren <= index) {
                $('#roster-members-content').append(h(headerData));
            } else {
                if(index === 0) {
                    $('#roster-members-content').prepend(h(headerData));

                } else {
                    $('#roster-members-content').children().eq(index - 1).after(h(headerData));
                }
            }
            $(roleContainer).append(t(templateData));
        }
        roster.addMembersShown(role);
        if(!renderAll) {
            $(window).trigger('scroll.roster.rendered');
        }
    };

    roster.renderPictures = function (members, target, enrollmentsMode, renderAll) {

        var templateData = {
                members: members,
                groupToView :roster.groupToView,
                firstNameLastName: roster.firstNameLastName,
                viewProfile: roster.currentUserPermissions.viewProfile,
                viewPicture: true,
                currentUserId: roster.userId,
                viewOfficialPhoto: roster.currentUserPermissions.viewOfficialPhoto,
                enrollmentsMode: enrollmentsMode
            };
        var t = Handlebars.templates['pictures'];
        target.html(t(templateData));
    };

    roster.getScrollFunction = function (options) {

        return function () {

            var wintop = $(window).scrollTop(), docheight = $(document).height(), winheight = $(window).height();
     
            if  ((wintop/(docheight-winheight)) > 0.95 || $("body").data("scroll-roster") === true) {
                $("body").data("scroll-roster", false);
                $(window).off('scroll.roster');
                var noRemaining = true;
                for(var item in roster.site.roleCounts) {
                    var itemData = $("body").data(item);
                    if(itemData !== undefined) {
                        noRemaining = false;
                        roster.renderMembership({
                            replace: false,
                            enrollmentStatus: options.enrollmentStatus,
                            role: item,
                            rolePage: itemData
                        });
                    }
                }
                if(noRemaining) {
                    roster.renderMembership({
                        replace: false,
                        enrollmentStatus: options.enrollmentStatus
                    });
                }
            }
        };
    };

    roster.floatingHeader =  function() {
        if($(".roster-mobile-label:visible").size() === 0) {
            $(".roster-table").find(".roster-table-header-group").find(".roster-table-row").each(function () {
                var element = $(this);
                var elementTop = element.parent().offset().top;
                var elementHeight = element.parent().parent().height();
                var roleTitle = element.parent().parent().siblings('.roleTitle').children(".roleDetail");
                var winTopWBanner = $(window).scrollTop() + 52;
                var scrollOn = winTopWBanner > elementTop && ($(window).height() - 52) < elementHeight;
                element.toggleClass('scrolling', scrollOn);
                roleTitle.toggleClass('scrolling', scrollOn);
            });
        }
    };

    roster.getRoleFragments = function (roleCounts) {

        return Object.keys(roleCounts).map(function (key) {
            return roster.getRoleFragment(key, roleCounts[key]);
        }).join(", ");
    };

    roster.getRoleFragment = function(role, counts) {
        var frag = roster.i18n.role_breakdown_fragment_0.replace(/\{0\}/, counts);
        frag = frag + " " + roster.i18n.role_breakdown_fragment_1;
        frag = frag + " " + roster.i18n.role_breakdown_fragment_2.replace(/\{0\}/, role );
        return frag;
    };

    roster.sortRolesByNumOfPermissions = function(roles, checkCount) {
        var sortedArray = [];
        var rawPermissions = {};

        for(var item in roles) {
            if(roster.site.permissions.hasOwnProperty(item)) {
                var rolePresent = true;
                if (checkCount) { // check the # of roles from roster.site.roleCounts
                    if(roles[item] === 0) {
                        rolePresent = false;
                    }
                }

                if (rolePresent) {
                    rawPermissions[item] = roster.site.permissions[item].length;
                    sortedArray.push(item);
                }
            }
        }

        sortedArray.sort(function(a, b) {
            var sortA = rawPermissions[a];
            var sortB = rawPermissions[b];

            if(sortA !== sortB) {
                return sortB - sortA;
            } else {
                return a.localeCompare(b);
            }
        });

        return sortedArray;
    };

    roster.formatDate = function (time) {

        var d = new Date(time);
        var hours = d.getHours();
        var rawHours = hours;
        if (hours < 10)  hours = '0' + hours;
        var minutes = d.getMinutes();
        if (minutes < 10) minutes = '0' + minutes;
        var formattedTime = hours + ":" + minutes;

        if(roster.view12HrClock) {
            hours = rawHours;
            var afternoon = false;
            if(hours >= 12) {
                afternoon = true;
                if(hours > 12) {
                    hours = rawHours - 12;
                }
                if(hours < 10 ) {
                    hours = '0' + hours;
                }
            }
            formattedTime = hours + ":" + minutes;
            if(afternoon) {
                formattedTime = formattedTime + " " + roster.i18n.pm;
            } else {
                formattedTime = formattedTime + " " + roster.i18n.am;
            }
        }
        return roster.i18n.lastVisitTimeDate.replace(/\{0\}/, roster.i18n.months[d.getMonth()]).
        replace(/\{1\}/, d.getDate()).replace(/\{2\}/, d.getFullYear()).replace(/\{3\}/, formattedTime);
    };

    roster.addMembersShown = function(role) {
        var roleContainerFirstHalf = roleContainerPrefix + role.replace(/\s+/g, '');
        var roleContainer = roleContainerFirstHalf + roleContainerSuffix;
        var roleCount = parseInt($("#" + roleContainerFirstHalf + "-count").text());
        var numberRoleShown = $('#' + roleContainerFirstHalf).find(".roster-member").length;
        if(numberRoleShown < roleCount) {
            $("#" + roleContainer).find(" .roleShown").text(roster.i18n.role_breakdown_number_shown.replace(/\{0\}/, numberRoleShown) + " ");
        } else {
            $("#" + roleContainer).find(" .roleShown").text("");
        }
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

    Handlebars.registerHelper('capitalizeFirstLetter', function (string) {
        return string.charAt(0).toUpperCase() + string.slice(1);
    });

    Handlebars.registerHelper('ifCond', function (v1, operator, v2, options) {

        switch (operator) {
            case '==':
                return (v1 == v2) ? options.fn(this) : options.inverse(this);
            case '===':
                return (v1 === v2) ? options.fn(this) : options.inverse(this);
            case '<':
                return (v1 < v2) ? options.fn(this) : options.inverse(this);
            case '<=':
                return (v1 <= v2) ? options.fn(this) : options.inverse(this);
            case '>':
                return (v1 > v2) ? options.fn(this) : options.inverse(this);
            case '>=':
                return (v1 >= v2) ? options.fn(this) : options.inverse(this);
            case '&&':
                return (v1 && v2) ? options.fn(this) : options.inverse(this);
            case '||':
                return (v1 || v2) ? options.fn(this) : options.inverse(this);
            default:
                return options.inverse(this);
        }
    });

    Handlebars.registerHelper("debug", function(optionalValue) {
        console.log("Current Context");
        console.log("====================");
        console.log(this);

        if (optionalValue) {
            console.log("Value");
            console.log("====================");
            console.log(optionalValue);
        }
    });

    roster.init = function () {

        roster.i18n = $.i18n.map;

        roster.i18n.months = roster.i18n.months.split(',');

        roster.ADMIN = 'admin';

        roster.STATE_OVERVIEW = 'overview';
        roster.STATE_ENROLLMENT_STATUS = 'status';
        roster.STATE_PRINT = 'print';
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

        // We need the toolbar in a template so we can swap in the translations
        roster.render('navbar', {}, 'roster_navbar');

        roster.renderButtons();
        
        $('#navbar_overview_link').find('a').click(function (e) {
            return roster.switchState(roster.STATE_OVERVIEW);
        });

        $('#navbar_enrollment_status_link').find('a').click(function (e) {
            return roster.switchState(roster.STATE_ENROLLMENT_STATUS);
        });
        
        $('#navbar_permissions_link').find('a').click(function (e) {
            return roster.switchState(roster.STATE_PERMISSIONS);
        });

        $(window).scroll(roster.floatingHeader);
                
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

    roster.alignNameCellWidths = function() {
        roster.maxNameCellWidth = 200;
        roster.nameCellWidth = 80;
        var target = $(".roster-name-cell");
        target.each(function () {
            var width = $(this).width();
            if (width > roster.nameCellWidth) {
                if(width <= roster.maxNameCellWidth) {
                    roster.nameCellWidth = width;
                }
            }
        });

        target.width(roster.nameCellWidth);
    };

    roster.alignUserIdCellWidths = function() {
        roster.maxUseridWidth = 160;
        roster.userIdWidth = 40;

        var target = $('.roster-userid-cell');
        target.each(function () {
           var width = $(this).width();
           if (width > roster.userIdWidth) {
               if (width <= roster.maxUseridWidth) {
                   roster.userIdWidth = width;
               }
           }
        });

        target.width(roster.userIdWidth);
    };

    roster.alignEmailWidths = function() {
        roster.maxEmailWidth = 200;
        roster.emailWidth = 80;

        var target = $('.roster-email');
        target.each(function () {
            var width = $(this).width();
            if (width > roster.emailWidth) {
                if (width <= roster.maxEmailWidth) {
                    roster.emailWidth = width;
                }
            }
        });

        target.width(roster.emailWidth);
    };

    roster.alignGroupWidths = function () {
        roster.maxGroupWidth = 200;
        roster.groupWidth = 100;

        $('.roster-group-inner-cell').each(function () {
            var width = $(this).width();
            if (width > roster.groupWidth) {
                if (width <= roster.maxGroupWidth) {
                    roster.groupWidth = width;
                }
            }
        });

        $('.roster-group-cell').width(roster.groupWidth);
    };

    roster.alignConnectionWidths = function () {
      roster.maxConnectionWidth = 200;
      roster.connectionWidth = 125;

      var target = $('.roster-connections-cell');
      target.each(function () {
          var width = $(this).width();
          if (width > roster.connectionWidth) {
              if (width <= roster.maxConnectionWidth) {
                  roster.connectionWidth = width;
              }
          }
      });

      target.width(roster.connectionWidth);
    };

    roster.clearWidths = function () {
        $(".roster-name-cell").width("");
        $('.roster-userid-cell').width("");
        $('.roster-email').width("");
        $('.roster-group-cell').width("");
        $('.roster-connections-cell').width();
    };

    roster.alignMobileLabels = function () {

        if (!roster.maxMobileLabelWidth) {
            roster.maxMobileLabelWidth = 0;
            $('.roster-mobile-label').each(function (l) {
                var width = $(this).width();
                if (width > roster.maxMobileLabelWidth) roster.maxMobileLabelWidth = width;
            });
        }
        $('.roster-mobile-label').width(roster.maxMobileLabelWidth);
    };

    $(window).resize(function () {
        if($('.roster-mobile-label:visible').size() > 0) {
            roster.clearWidths();
            roster.alignMobileLabels();
        } else {
            roster.alignNameCellWidths();
            roster.alignUserIdCellWidths();
            roster.alignEmailWidths();
            roster.alignGroupWidths();
            roster.alignConnectionWidths();
        }
    });
}) (jQuery);
