/**
 * Copyright (c) 2010 onwards - The Sakai Foundation
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

/* Stuff that we always expect to be setup */
meetings.currentMeetings = [];
meetings.currentRecordings = [];
meetings.checkOneMeetingAvailabilityId = null;
meetings.checkAllMeetingAvailabilityId = null;
meetings.checkRecordingAvailabilityId = null;
meetings.refreshRecordingListId = null;
meetings.errorLog = {};

(function ($) {

    var arg = meetings.startupArgs;

    if (!arg || !arg.siteId) {
        meetings.utils.showMessage(meetings_err_no_siteid, 'error');
        return;
    }

    // load I18N files.
    jQuery.i18n.properties({
        name: 'meetings',
        async: false,
        path: '/meetings-tool/bundle/',
        language: arg.language,
        mode: 'vars'
    });

    // We need the toolbar in a template so we can swap in the translations.
    meetings.utils.render('meetings_toolbar_template', {}, 'meetings_toolbar');

    $('#meetings_home_link').click(function (e) {
        return meetings.switchState('currentMeetings');
    }).show();

    $('#meetings_permissions_link').click(function (e) {
        return meetings.switchState('permissions');
    }).hide();

    $('#meetings_recordings_link').click(function (e) {
        return meetings.switchState('recordings');
    }).hide();

    var settingsCallback = function () {

        meetings.currentUser = meetings.settings.currentUser;
        meetings.userPerms = new MeetingsPermissions(meetings.currentUser.permissions);

        // Now switch into the requested state.
        if (meetings.currentUser != null) {
            meetings.switchState(arg.state, arg);
        } else {
            meetings.utils.showMessage(meetings_err_no_user, 'error');
            jQuery('#meetings_container').empty();
        }

        // If configured, show text notice (first time access).
        meetings.utils.addNotice();
    };

    // Setup Ajax defaults.
    meetings.utils.setupAjax();

    meetings.utils.getSettings(arg.siteId, settingsCallback);
})(jQuery);


meetings.switchState = async function (state, arg) {
    if (meetings.checkOneMeetingAvailabilityId != null) clearInterval(meetings.checkOneMeetingAvailabilityId);
    if (meetings.checkAllMeetingAvailabilityId != null) clearInterval(meetings.checkAllMeetingAvailabilityId);
    if (meetings.checkRecordingAvailabilityId != null) clearInterval(meetings.checkRecordingAvailabilityId);
    if (meetings.refreshRecordingListId != null) clearInterval(meetings.refreshRecordingListId);

    meetings.utils.hideMessage();

    // Clean navbar button state.
    $("#meetings_toolbar_items li>span").removeClass('current');

    if ('currentMeetings' === state) {
        $("#meetings_home_link").parent().addClass('current');
        // Show recordings links only if site maintainer or if has specific view permission.
        $('#meetings_recordings_link').unbind('click');
        if ((!meetings.userPerms.meetingsAdmin && !meetings.userPerms.meetingsRecordingView) || !meetings.settings.config.addUpdateFormParameters.recordingEnabled) {
            $('#meetings_recordings_link').parent().parent().hide();
        } else {
            $('#meetings_recordings_link').parent().parent().show();
            $('#meetings_recordings_link').click(function (e) {
                return meetings.switchState('recordings');
            }).show();
        }

        // Show permissions links only if site maintainer.
        $('#meetings_permissions_link').unbind('click');
        if (meetings.userPerms.meetingsAdmin) {
            $('#meetings_permissions_link').parent().parent().show();
            $('#meetings_permissions_link').click(function (e) {
                return meetings.switchState('permissions');
            }).show();
        } else {
            $('#meetings_permissions_link').parent().parent().hide();
        }

        if (meetings.userPerms.meetingsDeleteAny) {
            $('#meetings_end_meetings_link').parent().parent().show();
        } else {
            $('#meetings_end_meetings_link').parent().parent().hide();
        }

        // Show meeting list.
        if (meetings.userPerms.meetingsViewMeetingList) {
            // Show meeting list.
            meetings.utils.render('meetings_rooms_template', {
              siteId: meetings.startupArgs.siteId,
            }, 'meetings_content');

            $(document).ready(() => {
              document.getElementById("meeting-list").addEventListener("create-meeting", () => {
                  return meetings.switchState('addUpdateMeeting');
              });
            });

            /*
            // Show links if user has appropriate permissions.
            if (meetings.userPerms.meetingsCreate) {
                $('#meetings_create_meeting_link').show();
            } else {
                $('#meetings_create_meeting_link').hide();
            }

            if (meetings.settings.config.autorefreshInterval.meetings > 0)
                meetings.checkAllMeetingAvailabilityId = setInterval("meetings.utils.checkAllMeetingAvailability()", meetings.settings.config.autorefreshInterval.meetings);
            */

        } else {
            // Warn about lack of permissions.
            if (meetings.userPerms.siteUpdate) {
                meetings.utils.showMessage(meetings_err_no_tool_permissions_maintainer);
            } else {
                meetings.utils.showMessage(meetings_err_no_tool_permissions);
            }
            $('#meetings_content').empty();
        }

    } else if ('addUpdateMeeting' === state) {
        $('#meetings_recordings_link').parent().parent().hide();
        $('#meetings_end_meetings_link').parent().parent().hide();
        $('#meetings_permissions_link').parent().parent().hide();

        var isNew = !(arg && arg.meetingId);
        var meeting = isNew ? {} : meetings.utils.getMeeting(arg.meetingId);
        const showStartDate = isNew ? false : meeting.startDate && meeting.startDate > 0;
        const showEndDate = isNew ? false : meeting.endDate && meeting.endDate > 0;
        var contextData = {
            'isNew': isNew,
            "showStartDate": showStartDate,
            "showEndDate": showEndDate,
            "canAddCalendar": (isNew && meetings.userPerms.calendarNew) || (!isNew && ( (meeting.ownerId == meetings.currentUser.id && meetings.userPerms.calendarReviseOwn) || (meeting.ownerId != meetings.currentUser.id && meetings.userPerms.calendarReviseAny) )),
            'meeting': meeting,
            'selTypes': meetings.utils.getUserSelectionTypes(),
            'selOptions': meetings.utils.getUserSelectionOptions(),
            'siteId': meetings.startupArgs.siteId,
            'recordingEnabled': meetings.settings.config.addUpdateFormParameters.recordingEnabled,
            'recordingEditable': meetings.settings.config.addUpdateFormParameters.recordingEditable,
            'recordingDefault': meetings.settings.config.addUpdateFormParameters.recordingDefault,
            'durationEnabled': meetings.settings.config.addUpdateFormParameters.durationEnabled,
            'durationDefault': meetings.settings.config.addUpdateFormParameters.durationDefault,
            'waitmoderatorEnabled': meetings.settings.config.addUpdateFormParameters.waitmoderatorEnabled,
            'waitmoderatorEditable': meetings.settings.config.addUpdateFormParameters.waitmoderatorEditable,
            'waitmoderatorDefault': meetings.settings.config.addUpdateFormParameters.waitmoderatorDefault,
            'multiplesessionsallowedEnabled': meetings.settings.config.addUpdateFormParameters.multiplesessionsallowedEnabled,
            'multiplesessionsallowedEditable': meetings.settings.config.addUpdateFormParameters.multiplesessionsallowedEditable,
            'multiplesessionsallowedDefault': meetings.settings.config.addUpdateFormParameters.multiplesessionsallowedDefault,
            'preuploadpresentationEnabled': meetings.settings.config.addUpdateFormParameters.preuploadpresentationEnabled,
            'groupsessionsEnabled': meetings.settings.config.addUpdateFormParameters.groupsessionsEnabled,
            'groupsessionsEditable': meetings.settings.config.addUpdateFormParameters.groupsessionsEditable,
            'groupsessionsDefault': meetings.settings.config.addUpdateFormParameters.groupsessionsDefault,
            'actionUrl': isNew ? "/direct/meetings/new" : "/direct/meetings/" + meeting.id + "/edit"
        };

        meetings.utils.render('meetings_addUpdate_meeting_template', contextData, 'meetings_content');

        $('#startDate1').change(function (e) {

            if ($(this).prop('checked')) {
              $('#startDate2').prop("disabled", false);
              $('#startDate2 + button').prop("disabled", false);
              $('#addToCalendar').prop("disabled", false);
            } else {
              $('#startDate2').prop("disabled", true);
              $('#startDate2 + button').prop("disabled", true);
              $('#addToCalendar').prop("disabled", true);
            }
        });

        // Show the presentation/file upload if meeting has one.
        if (meeting.presentation) {
            var url = meeting.presentation;
            $("#fileUrl").val(url);
            $("#url").attr("href", url);
            $("#url").text(url.substring(url.lastIndexOf("/") + 1));
            $("#fileView").show();
            $("#selectFile").attr("disabled", true);
        }

        $("#selectFile").change(function () {

            meetings.utils.hideMessage();
            if (!this.files[0]) return;

            var acceptedTypes = ['ppt', 'pptx', 'pdf', 'jpeg', 'png', 'gif', 'jpg'];
            var extension = $(this).val().split('.').pop();
            if (acceptedTypes.indexOf(extension) == -1) {
                meetings.utils.showMessage(meetings_warning_bad_filetype, 'warning');
                $(this).val('');
                return;
            }
            var maxFileSize = meetings.startupArgs.maxFileSizeInBytes;
            if (this.files[0].size > maxFileSize * 1024 * 1024) {
                meetings.utils.showMessage(meetings_warning_max_filesize(maxFileSize), 'warning');
                $(this).val('');
                return;
            }
            $("#selectFile").attr("disabled", true);
            meetings.utils.doUpload(this);
        });

        $("#removeUpload").click(function () {
            var resourceId = $("#fileUrl").val();
            resourceId = resourceId.substring(resourceId.indexOf('/attachment'));
            if (!isNew)
                meetings.utils.removeUpload(resourceId, meeting.id);
            else
                meetings.utils.removeUpload(resourceId);
        });

        $('#endDate1').change(function (e) {

            if ($(this).prop('checked')) {
              $('#endDate2').prop("disabled", false);
              $('#endDate2 + button').prop("disabled", false);
            } else {
              $('#endDate2').prop("disabled", true);
              $('#endDate2 + button').prop("disabled", true);
            }
        });

        // Focus on meeting name/title.
        $('#meetings_meeting_name_field').focus();

        // Setup description/welcome msg editor.
        meetings.utils.makeInlineCKEditor('meetings_welcome_message_textarea', 'MEETINGS', '480', '200');

        let startDate = new Date().toISOString();
        if (!isNew && meeting.startDate) {
          startDate = new Date(meeting.startDate).toISOString();
        }

        let endDate = new Date().toISOString();
        if (!isNew && meeting.endDate) {
          endDate = new Date(meeting.endDate).toISOString();
        }

        localDatePicker({
          input: '#startDate2',
          useTime: 1,
          val: startDate,
          parseFormat: 'YYYY-MM-DDTHH:mm:ss.SSSZ',
          ashidden:{
            iso8601: "startDate"
          },
        });

        $('#startDate2 + button').prop("disabled", !showStartDate);

        localDatePicker({
          input: '#endDate2',
          useTime: 1,
          val: endDate,
          parseFormat: 'YYYY-MM-DDTHH:mm:ss.SSSZ',
          ashidden:{
            iso8601: "endDate"
          },
        });

        $('#endDate2 + button').prop("disabled", !showEndDate);

        // Add meeting participants.
        meetings.addParticipantSelectionToUI(meeting, isNew);

        $('#meetings_save').click(function (e) {

            meetings.utils.addUpdateMeeting();
            return false;
        });

        $('#meetings_cancel').click(function (e) {
            if (!meeting.presentation && $('#fileUrl').val())
                $('#removeUpload').click();
            $('#meetings_home_link').click();
        });

        // User warnings.
        if (!meetings.allSiteMembersCanParticipate()) {
            meetings.utils.showMessage(meetings_err_not_everyone_can_participate);
        }
    } else if ('permissions' === state) {
        $("#meetings_permissions_link").parent().addClass('current');
        meetings.utils.render('meetings_permissions_template', {}, "meetings_content");
    } else if ('joinMeeting' === state || 'meetingInfo' === state) {
        if ('joinMeeting' === state) meetings.setMeetingList();
        $('#meetings_recordings_link').parent().parent().hide();
        $('#meetings_end_meetings_link').parent().parent().hide();
        $('#meetings_permissions_link').parent().parent().hide();

        if (arg && arg.meetingId) {
            const meeting = meetings.currentMeetings.find(m => m.id == arg.meetingId);

            if (meeting) {
                let groups = [];
                if (meeting.groupSessions && meetings.settings.config.addUpdateFormParameters.groupsessionsEnabled) {
                    groups = await meetings.utils.getGroups(meeting);
                }

                meeting.multipleSessionsReallyAllowed = meeting.multipleSessionsAllowed && meetings.settings.config.addUpdateFormParameters.multiplesessionsallowedEnabled;
                meeting.hideRecordings = !meetings.userPerms.meetingsRecordingView || !meetings.settings.config.addUpdateFormParameters.recordingEnabled || !meeting.recording;
                meeting.canJoin = meeting.joinable && meetings.userPerms.meetingsParticipate;

                meetings.utils.render('meetings_meeting-info_template', {
                    'meeting': meeting,
                    'groups': groups
                }, 'meetings_content');

                //meetings.utils.checkOneMeetingAvailability(arg.meetingId);
                //meetings.utils.checkRecordingAvailability(arg.meetingId);

                if (meetings.settings.config.autorefreshInterval.meetings > 0) {
                    meetings.checkOneMeetingAvailabilityId = setInterval("meetings.utils.checkOneMeetingAvailability('" + arg.meetingId + "')", meetings.settings.config.autorefreshInterval.meetings);
                }

            } else {
                meetings.utils.hideMessage();
                meetings.utils.showMessage(meetings_err_meeting_unavailable_instr, 'warning', meetings_err_meeting_unavailable, false);
            }
        } else {
            meetings.switchState('currentMeetings');
        }
    } else if ('recordings' === state) {
        $("#meetings_recordings_link").parent().addClass('current');

        // Show meeting list.
        if (meetings.userPerms.meetingsViewMeetingList) {
            // Get recording list.
            meetings.refreshRecordingList();

            meetings.utils.render('meetings_recordings_template', {
                'recordings': meetings.currentRecordings,
                'stateFunction': 'recordings'
            }, 'meetings_content');

            var $rows = $('#meetings_recording_table tbody tr');
            $('.search').keyup(function () {
                var val = $.trim($(this).val()).replace(/ +/g, ' ').toLowerCase();

                $rows.show().filter(function () {
                    var text = $(this).text().replace(/\s+/g, ' ').toLowerCase();
                    return !~text.indexOf(val);
                }).hide();
            });

            if ($('a.preview')) {
                var xOffset = 5;
                var yOffset = 15;

                $('a.preview').hover(function (e) {
                    this.t = this.title;
                    this.title = '';
                    var c = (this.t != '') ? '<br/>' + this.t : '';
                    $('body').append("<p id='preview'><img id='previewImage' src='" + this.href + "' alt='Full size image preview' />" + c + "</p>");
                    $('#preview').css('top', (e.pageY - xOffset) + 'px').css('left', (e.pageX + yOffset) + 'px').fadeIn('fast');
                }, function () {
                    this.title = this.t;
                    $('#preview').remove();
                });
                $('a.preview').mousemove(function (e) {
                    $('#preview').css('top', (e.pageY - xOffset) + 'px').css('left', (e.pageX + yOffset) + 'px');
                });
            }

            // Add parser for customized date format.
            $.tablesorter.addParser({
                id: "meetingsRecDateTimeFormat",
                is: function (s) {
                    return false;
                },
                format: function (s, table) {
                    s = s.replace(/[a-zA-Z].*/g, '');
                    s = s.trim();
                    return $.tablesorter.formatFloat(new Date(s).getTime());
                },
                type: "numeric"
            });

            // Add sorting capabilities.
            $("#meetings_recording_table").tablesorter({
                cssHeader: 'meetings_sortable_table_header',
                cssAsc: 'meetings_sortable_table_header_sortup',
                cssDesc: 'meetings_sortable_table_header_sortdown',
                headers: {
                    1: {
                        sorter: false
                    },
                    3: {
                        sorter: 'meetingsRecDateTimeFormat'
                    },
                    4: {
                        sorter: false
                    }
                },
                // Sort DESC status:
                sortList: (meetings.currentRecordings.length > 0) ? [
                    [0, 0]
                ] : []
            });

            if (meetings.settings.config.autorefreshInterval.recordings > 0)
                meetings.refreshRecordingListId = setInterval("meetings.switchState('recordings')", meetings.settings.config.autorefreshInterval.recordings);
        } else {
            // Warn about lack of permissions.
            if (meetings.userPerms.siteUpdate) {
                meetings.utils.showMessage(meetings_err_no_tool_permissions_maintainer);
            } else {
                meetings.utils.showMessage(meetings_err_no_tool_permissions);
            }
            $('#meetings_content').empty();
        }
    } else if ('recordings_meeting' === state) {
        if (arg && arg.meetingId) {
            if (meetings.userPerms.meetingsViewMeetingList) {
                // Get meeting list.
                meetings.refreshRecordingList(arg.meetingId, arg.groupId);

                meetings.utils.render('meetings_recordings_template', {
                    'recordings': meetings.currentRecordings,
                    'stateFunction': 'recordings_meeting',
                    'meetingId': arg.meetingId
                }, 'meetings_content');

                if ($('a.preview')) {
                    var xOffset = 5;
                    var yOffset = 15;

                    $('a.preview').hover(function (e) {
                        this.t = this.title;
                        this.title = '';
                        var c = (this.t != '') ? '<br/>' + this.t : '';
                        $('body').append("<p id='preview'><img id='previewImage' src='" + this.href + "' alt='Full size image preview' />" + c + "</p>");
                        $('#preview').css('top', (e.pageY - xOffset) + 'px').css('left', (e.pageX + yOffset) + 'px').fadeIn('fast');
                    }, function () {
                        this.title = this.t;
                        $('#preview').remove();
                    });
                    $('a.preview').mousemove(function (e) {
                        $('#preview').css('top', (e.pageY - xOffset) + 'px').css('left', (e.pageX + yOffset) + 'px');
                    });
                }

                // Add parser for customized date format.
                $.tablesorter.addParser({
                    id: "bbbRecDateTimeFormat",
                    is: function (s) {
                        return false;
                    },
                    format: function (s, table) {
                        s = s.replace(/[a-zA-Z].*/g, '');
                        s = s.trim();
                        return $.tablesorter.formatFloat(new Date(s).getTime());
                    },
                    type: "numeric"
                });

                // Add sorting capabilities.
                $("#meetings_recording_table").tablesorter({
                    cssHeader: 'meetings_sortable_table_header',
                    cssAsc: 'meetings_sortable_table_header_sortup',
                    cssDesc: 'meetings_sortable_table_header_sortdown',
                    headers: {
                        1: {
                            sorter: false
                        },
                        3: {
                            sorter: 'bbbRecDateTimeFormat'
                        },
                        4: {
                            sorter: false
                        }
                    },
                    // Sort DESC status:
                    //sortList: (bbbCurrentMeetings.length > 0) ? [[2,1]] : []
                    sortList: (meetings.currentRecordings.length > 0) ? [
                        [0, 0]
                    ] : []
                });

                if (meetings.settings.config.autorefreshInterval.recordings > 0)
                    meetings.refreshRecordingListId = setInterval("meetings.switchState('recordings_meeting',{'meetingId':'" + arg.meetingId + "'})", meetings.settings.config.autorefreshInterval.recordings);
            } else {
                // Warn about lack of permissions.
                if (meetings.userPerms.siteUpdate) {
                    meetings.utils.showMessage(meetings_err_no_tool_permissions_maintainer);
                } else {
                    meetings.utils.showMessage(meetings_err_no_tool_permissions);
                }
                $('#meetings_content').empty();
            }
        } else {
            meetings.switchState('recordings');
        }
    }
};

meetings.allSiteMembersCanParticipate = function () {

    var perms = meetings.utils.getSitePermissions();
    var totalRoles = perms.length;
    var totalRolesThatCanParticipate = 0;
    for (var r = 0; r < perms.length; r++) {
        if (perms[r].meetings_participate) totalRolesThatCanParticipate++;
    }
    return totalRoles == totalRolesThatCanParticipate;
};

meetings.addParticipantSelectionToUI = function (meeting, isNew) {

    var selOptions = meetings.utils.getUserSelectionOptions();
    if (isNew) {
        var defaults = selOptions['defaults'];

        // Meeting creator (default: as moderator.
        var ownerDefault = defaults['bbb.default.participants.owner'];
        if (ownerDefault != 'none') {
            meetings.addParticipantRow('user', meetings.currentUser.id, meetings.currentUser.displayName + ' (' + meetings.currentUser.displayId + ')', ownerDefault == 'moderator');
        }

        // All site participants (default: none).
        var allUsersDefault = defaults['bbb.default.participants.all_users'];
        if (allUsersDefault != 'none') {
            meetings.addParticipantRow('all', null, null, allUsersDefault == 'moderator');
        }

    } else {
        // Existing participants.
        for (var i = 0; i < meeting.participants.length; i++) {
            var selectionType = meeting.participants[i].selectionType;
            var selectionId = meeting.participants[i].selectionId;
            var role = meeting.participants[i].role;

            if (selectionType == 'all') {
                meetings.addParticipantRow('all', null, null, role == 'moderator');
            } else {
                var opts = null;
                if (selectionType == 'user') opts = selOptions['users'];
                if (selectionType == 'group') opts = selOptions['groups'];
                if (selectionType == 'role') opts = selOptions['roles'];

                for (var n = 0; n < opts.length; n++) {
                    if (opts[n]['id'] == selectionId) {
                        meetings.addParticipantRow(selectionType, selectionId, opts[n]['title'], role == 'moderator');
                        break;
                    }
                }
            }
        }
    }
};

meetings.updateParticipantSelectionUI = function () {

    var selOptions = meetings.utils.getUserSelectionOptions();
    var selType = jQuery('#selType').val();
    jQuery('#selOption option').remove();

    if (selType == 'user' || selType == 'group' || selType == 'role') {
        var opts = null;
        if (selType == 'user') opts = selOptions['users'];
        if (selType == 'group') opts = selOptions['groups'];
        if (selType == 'role') opts = selOptions['roles'];
        for (var i = 0; i < opts.length; i++) {
            jQuery('#selOption').append(
                '<option value="' + opts[i]['id'] + '">' + opts[i]['title'] + '</option>'
            );
        }

        $("#selOption").html($("#selOption option").sort(function (a, b) {
            return a.text == b.text ? 0 : a.text < b.text ? -1 : 1
        }));

        jQuery('#selOption').removeAttr('disabled');
    } else {
        jQuery('#selOption').attr('disabled', 'disabled');
    }
};

/** Insert a Participant row on create/edit meeting page */
meetings.addParticipantRow = function (_selType, _id, _title, _moderator) {

    var selectionType = _selType + '_' + _id;
    var selectionId = _selType + '-' + 'role_' + _id;
    var selectionTitle = null;
    if (_selType == 'all') selectionTitle = '<span class="meetings_role_selection">' + meetings_seltype_all + '</span>';
    if (_selType == 'group') selectionTitle = '<span class="meetings_role_selection">' + meetings_seltype_group + ':</span> ' + _title;
    if (_selType == 'role') selectionTitle = '<span class="meetings_role_selection">' + meetings_seltype_role + ':</span> ' + _title;
    if (_selType == 'user') selectionTitle = '<span class="meetings_role_selection">' + meetings_seltype_user + ':</span> ' + _title;
    var moderatorSelection = _moderator ? ' selected' : '';
    var attendeeSelection = _moderator ? '' : ' selected';

    var trId = 'row-' + _selType + '-' + btoa(_id).slice(0, -2);
    var trRowClass = 'row-' + _selType;
    if (jQuery('#' + trId).length == 0) {
        var row = jQuery(
            '<tr id="' + trId + '" class="' + trRowClass + '" style="display:none">' +
            '<td>' +
            '<a href="#" title="' + meetings_remove + '" onclick="jQuery(this).parent().parent().remove();return false"><img src="/library/image/silk/cross.png" alt="X" style="vertical-align:middle"/></a>&nbsp;' +
            selectionTitle +
            '</td>' +
            '<td>' +
            '<span class="meetings_role_selection_as">' + meetings_as_role + '</span>' +
            '<select name="' + selectionId + '"><option value="attendee"' + attendeeSelection + '>' + meetings_role_atendee + '</option><option value="moderator"' + moderatorSelection + '>' + meetings_role_moderator + '</option></select>' +
            '<input type="hidden" name="' + selectionType + '" value="' + _id + '"/>' +
            '</td>' +
            '</tr>');
        if (jQuery('table#selContainer tbody tr.' + trRowClass + ':last').size() > 0)
            jQuery('table#selContainer tbody tr.' + trRowClass + ':last').after(row);
        else
            jQuery('table#selContainer tbody').append(row);
        row.fadeIn();
    } else {
        jQuery('#' + trId).animate({
            opacity: 'hide'
        }, 'fast', function () {
            jQuery('#' + trId).animate({
                opacity: 'show'
            }, 'slow');
        });
    }
};

meetings.updateMeetingInfo = function (meeting) {

    jQuery('#meetings_meeting_info_participants_count').html('?');
    var meetingInfo = meeting;
    if (meetingInfo != null) {
        if (meetingInfo.participantCount != null && parseInt(meetingInfo.participantCount) >= 0) {
            // prepare participant count text
            var attendeeCount = meetingInfo.participantCount - meetingInfo.moderatorCount;
            var moderatorCount = meetingInfo.moderatorCount;
            var attendeeText = attendeeCount + ' ' + (attendeeCount == 1 ? meetings_meetinginfo_participants_atendee : meetings_meetinginfo_participants_atendees);
            var moderatorText = moderatorCount + ' ' + (moderatorCount == 1 ? meetings_meetinginfo_participants_moderator : meetings_meetinginfo_participants_moderators);
            // prepare participant links
            if (attendeeCount > 0) {
                var attendees = '';
                for (var p = 0; p < meetingInfo.attendees.length; p++) {
                    if (meetingInfo.attendees[p].role == 'VIEWER') {
                        if (attendees != '') {
                            attendees += ', ' + meetingInfo.attendees[p].fullName;
                        } else {
                            attendees = meetingInfo.attendees[p].fullName;
                        }
                    }
                }
                attendeeText = '<a id="attendees" title="' + attendees + '" href="javascript:;" onclick="return false;">' + attendeeText + '</a>';
            }
            if (moderatorCount > 0) {
                var moderators = '';
                for (var p = 0; p < meetingInfo.attendees.length; p++) {
                    if (meetingInfo.attendees[p].role == 'MODERATOR') {
                        if (moderators != '') {
                            moderators += ', ' + meetingInfo.attendees[p].fullName;
                        } else {
                            moderators = meetingInfo.attendees[p].fullName;
                        }
                    }
                }
                moderatorText = '<a id="moderators" title="' + moderators + '" href="javascript:;" onclick="return false;">' + moderatorText + '</a>';
            }
            var countText = meetingInfo.participantCount > 0 ?
                meetingInfo.participantCount + ' (' + attendeeText + ' + ' + moderatorText + ')' :
                '0';
            // update participant info & tooltip
            jQuery('#meetings_meeting_info_participants_count').html(countText);
            jQuery('#attendees, #moderators').tipTip({
                activation: 'click',
                keepAlive: 'true'
            });

            for (var p = 0; p < meetingInfo.attendees.length; p++) {
                if ((!meeting.multipleSessionsAllowed || !meetings.settings.config.addUpdateFormParameters.multiplesessionsallowedEnabled) && meetings.currentUser.id === meetingInfo.attendees[p].userID) {
                    $('#meeting_joinlink_' + meeting.id).hide();
                }
            }
        } else if (meetingInfo.participantCount == null || parseInt(meetingInfo.participantCount) == -1) {
            jQuery('#meetings_meeting_info_participants_count_tr').hide();
            return;
        } else {
            jQuery('#meetings_meeting_info_participants_count').html('0');
        }
        jQuery('#meetings_meeting_info_participants_count_tr').fadeIn();
    } else {
        jQuery('#meetings_meeting_info_participants_count_tr').hide();
    }
};

meetings.setMeetingList = function () {

    meetings.currentMeetings = meetings.utils.getMeetingList(meetings.startupArgs.siteId);

    // Watch for permissions changes, check meeting dates
    meetings.currentMeetings.forEach(m => {

        meetings.utils.setMeetingPermissionParams(m);
        if (m.joinable) {
            m.joinableMode = "";
        }
        meetings.utils.setMeetingJoinableModeParams(m);
    });
};

meetings.refreshRecordingList = function (meetingId, groupId) {

  const getRecordingResponse = (meetingId == null) ? meetings.utils.getSiteRecordingList(meetings.startupArgs.siteId) : meetings.utils.getMeetingRecordingList(meetingId, groupId);

  if (getRecordingResponse.returncode == 'SUCCESS') {
    meetings.currentRecordings = getRecordingResponse.recordings;
    meetings.currentRecordings.forEach(r => {

      let length = parseInt(r.endTime) - parseInt(r.startTime);
      r.formattedDuration = Math.round(length / 60000);

      r.formattedStartTime = r.startTime ? new Date(parseInt(r.startTime)).toLocaleString(portal.locale, { dateStyle: "short", timeStyle: "short" }) : "";
      r.ownerId = "";
      meetings.utils.setRecordingPermissionParams(r);

      let images = [];
      r.playback.forEach(p => {
        if (p.preview && p.preview.length > images.length) {
          images = p.preview;
        }
      });

      if (images.length) {
        r.images = images;
      }
    });
  } else {
    meetings.currentRecordings = [];

    if (getRecordingResponse.messageKey != null) {
      meetings.utils.showMessage(getRecordingResponse.messageKey + ":" + getRecordingResponse.message, 'warning');
    } else {
      meetings.utils.showMessage(meetings_warning_no_server_response, 'warning');
    }
  }
};

meetings.sortDropDown = function (dropDownId) {
    var defaultGroup = $(dropDownId + ' option:first');
    var groupNames = $(dropDownId + ' option:not(:first)').sort(function (a, b) {
        return a.text.toUpperCase() == b.text.toUpperCase() ? 0 : a.text.toUpperCase().localeCompare(b.text.toUpperCase());
    });
    $(dropDownId).html(groupNames).prepend(defaultGroup);
};
