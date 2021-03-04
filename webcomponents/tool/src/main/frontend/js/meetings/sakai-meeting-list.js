import { SakaiElement } from "../sakai-element.js";
import { html } from "../assets/lit-html/lit-html.js";
import { utilsMixin } from "./utils-mixin.js";
import "./options-menu.js";

class SakaiMeetingList extends utilsMixin(SakaiElement) {

  static get properties() {

    return {
      siteId: { attribute: "site-id", type: String },
      meetings: { type: Array },
    };
  }

  constructor() {

    super();

    this.loadTranslations("meetings").then(r => this.i18n = r);
  }

  dispatchCreateMeeting() {
    this.dispatchEvent(new CustomEvent("create-meeting"));
  }

  dispatchShowPermissions() {
    this.dispatchEvent(new CustomEvent("show-permissions"));
  }

  dispatchShowMeeting(e) {

    const meeting = this.meetings.find(m => m.id === e.target.dataset.meetingId);
    meeting && this.dispatchEvent(new CustomEvent("show-meeting", { detail: { meeting } }));
  }

  editMeeting(e) {

    const meeting = this.meetings.find(m => m.id === e.target.dataset.meetingId);
    meeting && this.dispatchEvent(new CustomEvent("edit-meeting", { detail: { meeting } }));
  }

  deleteMeeting(e) {

    const meetingName = e.target.dataset.meetingName;

    const confirmation
      = this.i18n.meetings_action_delete_meeting_question.replace("{0}", meetingName);

    if (!confirm(confirmation)) {
      return;
    }

    const meetingId = e.target.dataset.meetingId;

    const url = `/api/sites/${this.siteId}/meetings/${meetingId}`;
    fetch(url, {
      method: "DELETE",
      credentials: "include",
    })
      .then(r => {

        if (!r.ok) {
          throw Error(`Network error while deleting meeting ${meetingId}`);
        } else {
          this.dispatchEvent(new CustomEvent("delete-meeting", { detail: { meetingId } }));
        }
      })
      .catch (error => console.error(error));

  }

  endMeeting(e) {

    const meetingName = e.target.dataset.meetingName;

    const confirmation
      = this.i18n.meetings_action_end_meeting_question.replace("{0}", meetingName);

    if (!confirm(confirmation)) return;

    const meetingId = e.target.dataset.meetingId;

    this.dispatchEvent(new CustomEvent("end-meeting", { detail: { meetingId } }));
  }

  shouldUpdate() {
    return this.meetings && this.i18n;
  }

  firstUpdated() {

    const $rows = $('#meetings-meeting-table tbody tr');
    $('.search').keyup(function () {

      const val = $.trim($(this).val()).replace(/ +/g, ' ').toLowerCase();

      $rows.show().filter(function () {

        const text = $(this).text().replace(/\s+/g, ' ').toLowerCase();
        return !~text.indexOf(val);
      }).hide();
    });
  }

  render() {

    console.log(this.meetings);

    return html`
      ${this.meetings.map(m => html`
      `)}

      <table id="meetings-meeting-table" class="table table-hover table-striped table-bordered">
        <tbody>
          <tr>
            <th class="meetings_name">${this.tr("meetings_th_meetingname")}</th>
            <th class="meetings_status">${this.tr("meetings_th_status")}</th>
            <th class="meetings_startDate">${this.tr("meetings_th_startdate")}</th>
            <th class="meetings_endDate">${this.tr("meetings_th_enddate")}</th>
            <th class="owner">${this.tr("meetings_th_owner")}</th>
          </tr>
          ${this.meetings.map(m => html`
            <tr class="meetingRow">
              <td>
                ${m.joinable ? html`
                  <a href="javascript:;"
                      data-meeting-id="${m.id}"
                      @click=${this.dispatchShowMeeting}
                      title="${this.tr("meetings_meeting_details_tooltip")}">
                    ${m.name}
                  </a>
                ` : html`
                  <span>${m.name}</span>
                `}
                <div class="meeting-actions" style="margin:0; padding:0;">
                <small>
                  ${m.canEdit ? html`
                  <div class="edit_meeting" style="display: inline; margin:0; padding:0;">
                    <a href="javascript:;"
                        data-meeting-id="${m.id}"
                        @click=${this.editMeeting}
                        title="${this.tr("meetings_action_edit_meeting_tooltip")}">
                      ${this.tr("meetings_action_edit_meeting")}
                    </a>
                  </div>
                  ` : ""}
                  ${m.canDelete ? html`
                  <div class="delete_meeting" style="display: inline; margin:0; padding:0;">
                      &nbsp;|&nbsp;
                      <a href="javascript:;"
                          data-meeting-id="${m.id}"
                          data-meeting-name="${m.name}"
                          @click=${this.deleteMeeting}
                          title="${this.tr("meetings_action_delete_meeting_tooltip")}">
                        ${this.tr("meetings_action_delete_meeting")}
                      </a>
                  </div>
                  ` : ""}
                  ${m.canEnd && m.running ? html`
                  <div id="end_meeting_${m.id}"
                      class="${m.endMeetingClass}" >
                    &nbsp;|&nbsp;
                    <a href="javascript:;"
                        data-meeting-id="${m.id}"
                        data-meeting-name="${m.name}"
                        @click=${this.endMeeting}
                        title="${this.tr("meetings_action_end_meeting_tooltip")}">
                      ${this.tr("meetings_action_end_meeting")}
                    </a>
                  </div>
                  ` : ""}
                </small>
                </div>
              </td>
              <td id="meeting_status_${m.id}" class="${m.statusClass}">${m.statusText}</td>
              <td>${m.formattedStartDate}</td>
              <td>${m.formattedEndDate}</td>
              <td>${m.ownerDisplayName}</td>
            </tr>
          `)}
          </tbody>
      </table>
      `;
  }
}

const tagName = "sakai-meeting-list";
!customElements.get(tagName) && customElements.define(tagName, SakaiMeetingList);
