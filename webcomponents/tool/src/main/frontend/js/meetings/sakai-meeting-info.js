import { SakaiElement } from "../sakai-element.js";
import { html } from "../assets/lit-html/lit-html.js";
import { unsafeHTML } from "../assets/lit-html/directives/unsafe-html.js";
import { utilsMixin } from "./utils-mixin.js";

class SakaiMeetingInfo extends utilsMixin(SakaiElement) {

  constructor() {

    super();

    this.loadTranslations("meetings").then(r => this.i18n = r);
  }

  static get properties() {

    return {
      groups: { type: Array },
      meeting: { type: Object },
      settings: { type: Object },
      currentUser: { attribute: "current-user", type: Object },
    };
  }

  checkAvailability() {

    this.setMeetingInfo(this.meeting).then(() => {
      this.requestUpdate();
    });
  }

  joinMeetingHandler() {
    console.log(this.meeting.id);
    this.joinMeeting(this.meeting.id, "#joinMeetingLink", this.meeting.multipleSessionsReallyAllowed);
  }

  // Log an event indicating user is joining meeting.
  joinMeeting(meetingId, linkSelector, multipleSessionsAllowed) {

    const nonce = Date.now();
    const url = `/api/sites/${this.meeting.siteId}/meetings/${meetingId}/join?nonce=${nonce}`;
    console.log(url);
    //meetings.utils.hideMessage();
    if (linkSelector) {
      $(linkSelector).attr('href', url);
      if (!multipleSessionsAllowed) {
        $(`#meeting_joinlink_${  meetingId}`).hide();
        $('#meetingStatus').hide();
      }
      //.After joining stop requesting periodic updates.
      clearInterval(this.checkOneMeetingAvailabilityId);
      clearInterval(this.checkRecordingAvailabilityId);

      // After joining execute requesting updates only once.
      const onceAutorefreshInterval = this.settings.autorefreshInterval.meetings > 0 ? this.settings.autorefreshInterval.meetings : 15000;
      this.updateMeetingOnceTimeoutId = setTimeout(() => { this.checkAvailability(); }, onceAutorefreshInterval);
    }
    return true;
  }

  endMeeting(e) {

    const meetingName = e.target.dataset.meetingName;

    const confirmation
      = this.i18n.meetings_action_end_meeting_question.replace("{0}", meetingName);

    if (!confirm(confirmation)) return;

    const meetingId = e.target.dataset.meetingId;

    this.dispatchEvent(new CustomEvent("end-meeting", { detail: { meetingId } }));
  }


  currentUserCanJoin() {

    return this.meeting.joinUrl
      && this.meeting.joinable
      && this.meeting.canJoin;
  }

  shouldUpdate() {
    return this.meeting && this.i18n;
  }

  firstUpdated() {
    this.checkAvailability();
  }

  render() {

    return html`

      <h3>${this.tr("meetings_meetinginfo_title")}</h3>
      <br/>
      <table cellpadding="0" cellspacing="0" border="0" id="meetings_meeting_info_table">
        <tr>
          <td><h5>${this.tr("meetings_info_title")}</h5></td>
          <td id="meetingName">${this.meeting.name}</td>
        </tr>
        <tr>
          <td><h5>${this.tr("meetings_info_description")}</h5></td>
          <td>${unsafeHTML(this.meeting.welcomeMessage)}</td>
        </tr>
        ${this.meeting.startDate ? html`
        <tr>
          <td><h5>${this.tr("meetings_availability_startdate")}</h5></td>
          <td>${this.meeting.formattedStartDate}</td>
        </tr>
        ` : ""}
        ${this.meeting.endDate ? html`
        <tr>
          <td><h5>${this.tr("meetings_availability_enddate")}</h5></td>
          <td>${this.meeting.formattedEndDate}</td>
        </tr>
        ` : ""}
        <tr>
          <td><h5>${this.tr("meetings_meetinginfo_status")}</h5></td>
          <td>
            <span id="meeting_status_notstarted_${this.meeting.id}"
                    class="meetings_status_notstarted"
                    style="display: ${this.meeting.notStarted ? "inline" : "none"}">
                ${this.tr("meetings_status_notstarted")}
            </span>
            <span id="meeting_status_joinable_${this.meeting.id}"
                class="status_joinable_inprogress"
                style="display: ${this.meeting.joinable ? "inline" : "none"}">
              ${this.meeting.joinableMode === "available" ? this.tr("meetings_status_joinable_available") : ""}
              ${this.meeting.joinableMode === "inprogress" ? this.tr("meetings_status_joinable_inprogress") : ""}
              ${this.meeting.joinableMode === "unavailable" ? this.tr("meetings_status_joinable_unavailable") : ""}
              ${this.meeting.joinableMode === "unreachable" ? this.tr("meetings_status_joinable_unreachable") : ""}
            </span>
            <span id="meeting_status_finished_${this.meeting.id}"
                class="meetings_status_finished"
                style="display: ${this.meeting.finished ? "inline" : "none"}">
              ${this.tr("meetings_status_finished")}
            </span>
            <span id="meetingStatus">
              (
              <span id="meeting_joinlink_${this.meeting.id}"
                      style="display: ${this.currentUserCanJoin() ? "inline" : "none"}">
                <a id="joinMeetingLink"
                    target="_blank"
                    href="javascript:;"
                    @click=${this.joinMeetingHandler}
                    title="${this.tr("meetings_meetinginfo_launch_meeting_tooltip")}"
                    style="font-weight:bold">
                  ${this.tr("meetings_meetinginfo_link")}
                </a>
                <i class="fa fa-sign-in"></i>
              </span>
              ${this.meeting.canEnd ? html`
              <div id="end_meeting_intermediate_${this.meeting.id}"
                    class="${this.meeting.joinable && this.meeting.joinableMode === 'inprogress' ? "meetings_end_meeting_shown" : "meetings_end_meeting_hidden"}">
                &nbsp;|&nbsp;
                <a id="end_session_link"
                  href="javascript:;"
                  data-meeting-id="${this.meeting.id}"
                  data-meeting-name="${this.meeting.name}"
                  @click=${this.endMeeting}
                  title="${this.tr("meetings_action_end_meeting_tooltip")}"
                  style="font-weight:bold">
                  ${this.tr("meetings_action_end_meeting")}
                </a>
                <span><i class="fa fa-stop"></i></span>
              </div>
              ` : ""}
              )
            <span>
          </td>
        </tr>
        ${this.meeting.participantCount > 0 ? html`
          <tr id="meetings_meeting_info_participants_count_tr">
            <td><h5>${this.tr("meetings_meetinginfo_participants_count")}</h5></td>
            <td>
              <span id="meetings_meeting_info_participants_count">${this.meeting.participantCount}</span>
              <a href="javascript:;"
                  @click=${this.checkAvailability}
                  title="${this.tr("meetings_meetinginfo_updateinfo_tooltip")}">
                <i class="fa fa-refresh" title="${this.tr("meetings_refresh")}"></i>
              </a>
            </td>
          </tr>
        ` : ""}
        <tr id="meeting_recordings" style="${this.meeting.hideRecordings ? "none" : "inline"}">
          <td><h5>${this.tr("meetings_info_recordings")}</h5></td>
          <td id="recording_link_${this.meeting.id}">
          </td>
        </tr>
      </table>
    `;
  }
}

const tagName = "sakai-meeting-info";
!customElements.get(tagName) && customElements.define(tagName, SakaiMeetingInfo);
