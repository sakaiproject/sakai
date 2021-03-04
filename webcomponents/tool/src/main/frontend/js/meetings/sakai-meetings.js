import { SakaiElement } from "../sakai-element.js";
import { html } from "../assets/lit-element/lit-element.js";
import { utilsMixin } from "./utils-mixin.js";
import "./sakai-meeting-list.js";
import "./sakai-meeting-info.js";
import "./sakai-create-meeting.js";
import "../sakai-permissions.js";
import { STATE_CREATE_MEETING, STATE_MEETING_LIST, STATE_PERMISSIONS, STATE_MEETING_INFO } from "./sakai-meetings-constants.js";

class SakaiMeetings extends utilsMixin(SakaiElement) {

  static get properties() {

    return {
      siteId: { attribute: "site-id", type: String },
      state: { attribute: false, type: String },
      data: { attribute: false, type: Object },
    };
  }

  constructor() {

    super();

    this.state = STATE_MEETING_LIST;
    this.loadTranslations("meetings").then(r => this.i18n = r);
  }

  set siteId(value) {

    this._siteId = value;

    const url = `/api/sites/${value}/meetings/data`;
    fetch(url, { credentials: "include" })
      .then(r => {

        if (r.ok) {
          return r.json();
        }
        throw Error(`Network error while getting settings from ${url}`);

      })
      .then(data => {

        this.data = data;

        this.updateMeetingsInfo(this.data.meetings);

        this.meetingInfoUpdateInterval = setInterval(() => {
          this.updateMeetingsInfo(this.data.meetings);
        }, 20000);

        this.meetingToEdit = data.meetingTemplate;
      })
      .catch (error => console.error(error));
  }

  get siteId() { return this._siteId; }

  updateMeetingsInfo(meetings) {

    meetings.forEach(m => {
      this.setMeetingInfo(m).then(() => this.requestUpdate());
    });
  }

  meetingCreated(e) {

    this.data.meetings.push(e.detail.meeting);
    this.state = STATE_MEETING_LIST;
  }

  editMeeting(e) {

    this.meetingToEdit = e.detail.meeting;
    this.state = STATE_CREATE_MEETING;
  }

  showMeeting(e) {

    this.currentMeeting = e.detail.meeting;
    this.state = STATE_MEETING_INFO;
  }

  handleDeleteMeeting(e) {

    const index = this.data.meetings.findIndex(m => m.id === e.detail.meetingId);
    this.data.meetings.splice(index, 1);
    this.requestUpdate();
  }

  handleEndMeeting(e) {

    const meeting = this.data.meetings.find(m => m.id === e.detail.meetingId);
    this.endMeeting(meeting).then(() => {
      if (this.currentMeeting.id === meeting.id) {
        this.currentMeeting = meeting;
      }
      this.requestUpdate();
    });
  }

  shouldUpdate() {
    return this.siteId && this.data;
  }

  render() {

    return html`
      
      ${this.state === STATE_MEETING_LIST ? html`
      <div id="meetings-create-and-search">
        <div>
          <input type="text" class="search" placeholder="${this.tr("meetings_search")}"/>
        </div>
        <div>
          <input type="button"
              id="meetings_create_meeting_link"
              title="${this.tr("meetings_create_meeting_tooltip")}"
              @click=${() => this.state = STATE_CREATE_MEETING}
              value="${this.tr("meetings_create_meeting_label")}">
          <options-menu placement="bottom-left" style="display: inline-block;">
            <div slot="trigger">
              <a href="javascript:;">
                <sakai-icon type="menu" size="small"></sakai-icon>
              </a>
            </div>
            <div slot="content" id="meetings-options-menu" class="options-menu" role="dialog">

              <div>
                <a href="javascript:;"
                    @click=${() => this.state = STATE_PERMISSIONS}>
                  ${this.i18n.meetings_permissions_label}
                </a>
              </div>
            </div>
          </options-menu>
        </div>
      </div>

      <sakai-meeting-list
          site-id="${this.siteId}"
          meetings="${JSON.stringify(this.data.meetings)}"
          @create-meeting=${() => this.state = STATE_CREATE_MEETING}
          @edit-meeting=${this.editMeeting}
          @show-meeting=${this.showMeeting}
          @end-meeting=${this.handleEndMeeting}
          @delete-meeting=${this.handleDeleteMeeting}
          @show-permissions=${() => this.state = STATE_PERMISSIONS}>
      </sakai-meeting-list>
      ` : ""}
      ${this.state === STATE_PERMISSIONS ? html`
      <sakai-permissions tool="meetings" bundle-key="meetingsperms"></sakai-permissions>
      ` : ""}
      ${this.state === STATE_CREATE_MEETING ? html`
      <sakai-create-meeting
        settings="${JSON.stringify(this.data.settings)}"
        site-id="${this.siteId}"
        meeting="${JSON.stringify(this.meetingToEdit)}"
        @cancel=${() => this.state = STATE_MEETING_LIST}
        @meeting-created=${this.meetingCreated}
        selection-options="${JSON.stringify(this.data.selectionOptions)}"
        ?check-ical-option=${this.data.checkICalOption}>
      </sakai-create-meeting>
      ` : ""}
      ${this.state === STATE_MEETING_INFO ? html`
      <sakai-meeting-info
        settings="${JSON.stringify(this.data.settings)}"
        @end-meeting=${this.handleEndMeeting}
        current-user="${JSON.stringify(this.data.currentUser)}"
        meeting="${JSON.stringify(this.currentMeeting)}"
      >
      </sakai-meeting-info>
      <input type="button"
          @click=${() => this.state = STATE_MEETING_LIST}
          value="Back to meeting list">
      ` : ""}
    `;
  }
}

const tagName = "sakai-meetings";
!customElements.get(tagName) && customElements.define(tagName, SakaiMeetings);
