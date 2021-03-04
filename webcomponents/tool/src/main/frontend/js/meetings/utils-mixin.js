export const utilsMixin = Base => class extends Base {

  setMeetingInfo(meeting) {

    return fetch(`/api/sites/${meeting.siteId}/meetings/${meeting.id}/info`, {
      credentials: "include",
      headers: { "Content-Type": "application/json"},
    })
    .then(r => {

      if (r.ok) {
        return r.json();
      }
      throw new Error("Network error while setting meeting info");
    })
    .then(info => {

      this.setMeetingInfoParams(meeting, info);
      this.setMeetingJoinableModeParams(meeting);
    })
    .catch(error => console.error(error));
  }

  checkMeetingAvailability(meeting) {

    if (meeting.joinable) {
      if (meeting.joinableMode === "available") {
        if (meeting.multipleSessionsAllowed && this.settings.multiplesessionsallowedEnabled) {
          //$('#meeting_joinlink_' + meeting.id).show();
        } else if (!this.isUserInMeeting(this.currentUser.displayName, meeting)) {
          //$('#meeting_joinlink_' + meeting.id).show();
        } else {
          //$('#meeting_joinlink_' + meeting.id).hide();
        }
        // Update the actionbar on the list.
        if (meeting.canEnd) {
          $(`#end_meeting_${  meeting.id}`)
            .removeClass()
            .addClass('meetings_end_meeting_hidden');
          $(`#end_meeting_intermediate_${  meeting.id}`)
            .removeClass()
            .addClass('meetings_end_meeting_hidden');
        }
        // Update for list.
        $(`#meeting_status_${  meeting.id}`)
          .removeClass()
          .addClass('status_joinable_available')
          .text(this.i18n.meetings_status_joinable_available);
        // Update for detail.
        $(`#meeting_status_joinable_${  meeting.id}`)
          .removeClass()
          .addClass('status_joinable_available')
          .text(this.i18n.meetings_status_joinable_available);
      } else if (meeting.joinableMode === "inprogress") {
        //let end_meetingTextIntermediate = `&nbsp;|&nbsp;&nbsp;<a id="end_session_link" href="javascript:;" onclick="return meetings.utils.endMeeting('${  escape(meeting.name)  }','${  meeting.id  }');" title="${  this.i18n.meetings_action_end_meeting_tooltip  }" style="font-weight:bold">${  this.i18n.meetings_action_end_meeting  }</a>&nbsp;<span><i class="fa fa-stop"></i></span>`;
        if (meeting.multipleSessionsAllowed && this.settings.multiplesessionsallowedEnabled) {
          $(`#meeting_joinlink_${  meeting.id}`).show();
        } else if (!this.isUserInMeeting(this.currentUser.displayName, meeting)) {
          $(`#meeting_joinlink_${  meeting.id}`).show();
        } else {
          $(`#meeting_joinlink_${  meeting.id}`).hide();
          //end_meetingTextIntermediate = `<a id="end_session_link" href="javascript:;" onclick="return meetings.utils.endMeeting('${  escape(meeting.name)  }','${  meeting.id  }');" title="${  this.i18n.meetings_action_end_meeting_tooltip  }" style="font-weight:bold">${  this.i18n.meetings_action_end_meeting  }</a>&nbsp;<span><i class="fa fa-stop"></i></span>`;
        }
        //$('#end_meeting_intermediate_' + meeting.id).toggleClass("meetings_end_meeting_shown").html(end_meetingTextIntermediate);

        // Update the actionbar on the list.
        if (meeting.canEnd) {
          $(`#end_meeting_${  meeting.id}`)
            .removeClass()
            .addClass('meetings_end_meeting_shown');
          $(`#end_meeting_intermediate_${  meeting.id}`)
            .removeClass()
            .addClass('meetings_end_meeting_shown');
        }
        // Update for list.
        $(`#meeting_status_${  meeting.id}`)
          .removeClass()
          .addClass('status_joinable_inprogress')
          .text(this.i18n.meetings_status_joinable_inprogress);
        // Update for detail.
        $(`#meeting_status_joinable_${  meeting.id}`)
          .removeClass()
          .addClass('status_joinable_inprogress')
          .text(this.i18n.meetings_status_joinable_inprogress);
      } else if (meeting.joinableMode === "unavailable") {
        $(`#meeting_joinlink_${  meeting.id}`).fadeOut();
        // Update the actionbar on the list.
        if (meeting.canEnd) {
          $(`#end_meeting_${  meeting.id}`)
            .removeClass()
            .addClass('meetings_end_meeting_hidden');
          $(`#end_meeting_intermediate_${  meeting.id}`)
            .removeClass()
            .addClass('meetings_end_meeting_hidden');
        }
        // Update for list.
        $(`#meeting_status_${  meeting.id}`)
          .removeClass()
          .addClass('status_joinable_unavailable')
          .text(this.i18n.meetings_status_joinable_unavailable);
        // Update for detail.
        $(`#meeting_status_joinable_${  meeting.id}`)
          .removeClass()
          .addClass('status_joinable_unavailable')
          .text(this.i18n.meetings_status_joinable_unavailable);

        $('#meetings_meeting_info_participants_count').html('0');
        $('#meetings_meeting_info_participants_count_tr').fadeOut();
        $('#meetings_meeting_info_participants_count_tr').hide();

      } else if (meeting.joinableMode === "unreachable") {
        $(`#meeting_joinlink_${  meeting.id}`).fadeOut();
        // Update the actionbar on the list.
        if (meeting.canEnd) {
          $(`#end_meeting_${  meeting.id}`)
            .removeClass()
            .addClass('meetings_end_meeting_hidden');
          $(`#end_meeting_intermediate_${  meeting.id}`)
            .removeClass()
            .addClass('meetings_end_meeting_hidden');
        }
        // Update for list.
        $(`#meeting_status_${  meeting.id}`)
          .removeClass()
          .addClass('status_joinable_unreachable')
          .text(this.i18n.meetings_status_joinable_unreachable);
        // Update for detail.
        $(`#meeting_status_joinable_${  meeting.id}`)
          .removeClass()
          .addClass('status_joinable_unreachable')
          .text(this.i18n.meetings_status_joinable_unreachable);

        $('#meetings_meeting_info_participants_count').html('0');
        $('#meetings_meeting_info_participants_count_tr').fadeOut();
        $('#meetings_meeting_info_participants_count_tr').hide();
      }
    } else if (meeting.notStarted) {
      $(`#meeting_joinlink_${  meeting.id}`).fadeOut();
      $(`#meeting_status_${  meeting.id}`)
        .removeClass()
        .addClass('status_notstarted')
        .text(this.i18n.meetings_status_notstarted);
    } else if (meeting.finished) {
      $(`#meeting_joinlink_${  meeting.id}`).fadeOut();
      $(`#meeting_status_${  meeting.id}`)
        .removeClass()
        .addClass('status_finished')
        .text(this.i18n.meetings_status_finished);
    }
  }

  updateMeetingInfo(meeting) {

    jQuery('#meetings_meeting_info_participants_count').html('?');
    const meetingInfo = meeting;
    if (meetingInfo != null) {
      if (meetingInfo.participantCount != null && parseInt(meetingInfo.participantCount) >= 0) {
        // prepare participant count text
        const attendeeCount = meetingInfo.participantCount - meetingInfo.moderatorCount;
        const moderatorCount = meetingInfo.moderatorCount;
        let attendeeText = `${attendeeCount  } ${  attendeeCount == 1 ? this.i18n.meetings_meetinginfo_participants_atendee : this.i18n.meetings_meetinginfo_participants_atendees}`;
        let moderatorText = `${moderatorCount  } ${  moderatorCount == 1 ? this.i18n.meetings_meetinginfo_participants_moderator : this.i18n.meetings_meetinginfo_participants_moderators}`;
        // prepare participant links
        if (attendeeCount > 0) {
          let attendees = '';
          for (let p = 0; p < meetingInfo.attendees.length; p++) {
            if (meetingInfo.attendees[p].role == 'VIEWER') {
              if (attendees != '') {
                attendees += `, ${  meetingInfo.attendees[p].fullName}`;
              } else {
                attendees = meetingInfo.attendees[p].fullName;
              }
            }
          }
          attendeeText = `<a id="attendees" title="${  attendees  }" href="javascript:;" onclick="return false;">${  attendeeText  }</a>`;
        }
        if (moderatorCount > 0) {
          let moderators = '';
          for (let p = 0; p < meetingInfo.attendees.length; p++) {
            if (meetingInfo.attendees[p].role == 'MODERATOR') {
              if (moderators != '') {
                moderators += `, ${  meetingInfo.attendees[p].fullName}`;
              } else {
                moderators = meetingInfo.attendees[p].fullName;
              }
            }
          }
          moderatorText = `<a id="moderators" title="${  moderators  }" href="javascript:;" onclick="return false;">${  moderatorText  }</a>`;
        }
        const countText = meetingInfo.participantCount > 0 ?
          `${meetingInfo.participantCount  } (${  attendeeText  } + ${  moderatorText  })` : '0';
        // update participant info & tooltip
        jQuery('#meetings_meeting_info_participants_count').html(countText);
        /*
        jQuery('#attendees, #moderators').tipTip({
          activation: 'click',
          keepAlive: 'true'
        });
        */

        for (let p = 0; p < meetingInfo.attendees.length; p++) {
          if ((!meeting.multipleSessionsAllowed || !this.settings.multiplesessionsallowedEnabled) && this.currentUser.id === meetingInfo.attendees[p].userID) {
            $(`#meeting_joinlink_${  meeting.id}`).hide();
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
  }

  isUserInMeeting(userName, meeting) {
    return meeting.attendees.find(a => this.currentUser.displayName === a.fullName);
  }

  endMeeting(meeting) {

    const question = this.i18n.meetings_action_end_meeting_question;
    if (!confirm(question)) return;

    const url = `/api/sites/${meeting.siteId}/meetings/${meeting.id}/end`;
    return fetch(url, {
      credentials: "include",
      headers: { "Content-Type": "text/plain"},
    })
    .then(r => {

      if (r.ok) {
        this.setMeetingInfo(meeting);
      }
      throw new Error("Network error while ending meeting");
    })
    .catch(error => console.error(error));
  }

  setMeetingJoinableModeParams(meeting) {

    // If joinable set the joinableMode.
    meeting.joinableMode = "nojoinable";
    if (meeting.joinable) {
      if (meeting.unreachableServer == null) {
        meeting.joinableMode = "";
      } else if (meeting.unreachableServer == "false") {
        meeting.joinableMode = "available";
        $('#meetingStatus').show();
        if (meeting.hasBeenForciblyEnded == "true") {
          meeting.joinableMode = "unavailable";
          $('#meetingStatus').hide();
        } else if (meeting.running) {
          meeting.joinableMode = "inprogress";
          if (!meeting.canEnd && (!meeting.multipleSessionsAllowed || !this.settings.multiplesessionsallowedEnabled) && this.isUserInMeeting(this.currentUser.displayName, meeting)) {
            $('#meetingStatus').hide();
          }
        }
      } else {
        meeting.joinableMode = "unreachable";
      }
    }

    // Update status in the view.
    const statusClass = meeting.joinable ? `status_joinable_${  meeting.joinableMode}` : (meeting.notStarted ? 'status_notstarted' : 'status_finished');
    const statusText = meeting.joinable ? (meeting.joinableMode == 'available' ? this.i18n.meetings_status_joinable_available : meeting.joinableMode == 'inprogress' ? this.i18n.meetings_status_joinable_inprogress : meeting.joinableMode == 'unavailable' ? this.i18n.meetings_status_joinable_unavailable : meeting.joinableMode == 'unreachable' ? this.i18n.meetings_status_joinable_unreachable : '') : (meeting.notStarted ? this.i18n.meetings_status_notstarted : this.i18n.meetings_status_finished);
    $(`#meeting_status_${  meeting.id}`).toggleClass(statusClass).html(statusText);
    // If meeting can be ended, update end action link in the view.
    if (meeting.canEnd) {
      //let end_meetingClass = "meetings_end_meeting_hidden";
      //let end_meetingText = "";
      if (meeting.joinable && meeting.joinableMode == 'inprogress') {
        //end_meetingClass = "meetings_end_meeting_shown";
        if (meeting.groupSessions) {
          //end_meetingText = `${"&nbsp;|&nbsp;&nbsp;" + "<a href=\"javascript:;\" onclick=\"return meetings.utils.endMeeting('"}${  escape(meeting.name)  }','${  meeting.id  }', ${  undefined  }, true);" title="${  this.i18n.meetings_action_end_meeting_tooltip  }">${  this.i18n.meetings_action_end_meeting  }</a>`;
        } else {
          //end_meetingText = `${"&nbsp;|&nbsp;&nbsp;" + "<a href=\"javascript:;\" onclick=\"return meetings.utils.endMeeting('"}${  escape(meeting.name)  }','${  meeting.id  }');" title="${  this.i18n.meetings_action_end_meeting_tooltip  }">${  this.i18n.meetings_action_end_meeting  }</a>`;
        }
      }
      //$('#end_meeting_' + meeting.id).toggleClass(end_meetingClass).html(end_meetingText);
    }
  }

  setMeetingInfoParams(meeting, meetingInfo) {

    // Clear attendees.
    if (meeting.attendees && meeting.attendees.length > 0) {
      delete meeting.attendees;
    }
    meeting.attendees = [];
    meeting.hasBeenForciblyEnded = "false";
    meeting.participantCount = 0;
    meeting.moderatorCount = 0;
    meeting.unreachableServer = "false";

    if (meetingInfo != null && meetingInfo.returncode != null) {
      if (meetingInfo.returncode != 'FAILED') {
        meeting.attendees = meetingInfo.attendees;
        meeting.hasBeenForciblyEnded = meetingInfo.hasBeenForciblyEnded;
        meeting.participantCount = meetingInfo.participantCount;
        meeting.moderatorCount = meetingInfo.moderatorCount;
        meeting.running = meetingInfo.running;
      } else if (meetingInfo.messageKey != 'notFound') {
        // Different errors can be handled here.
        meeting.unreachableServer = "true";
      }
    } else {
      delete meeting.running;
    }
  }
};
