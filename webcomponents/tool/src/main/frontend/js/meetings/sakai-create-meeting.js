import { SakaiElement } from "../sakai-element.js";
import { html } from "../assets/lit-html/lit-html.js";
import { utilsMixin } from "./utils-mixin.js";
import "../sakai-icon.js";

class SakaiCreateMeeting extends utilsMixin(SakaiElement) {

  static get properties() {

    return {
      siteId: { attribute: "site-id", type: String },
      settings: { type: Object },
      selectionOptions: { attribute: "selection-options", type: Object },
      selTypes: { attribute: false, type: Object },
      showStartDate: { attribute: false, type: Boolean },
      showEndDate: { attribute: false, type: Boolean },
      checkICalOption: { attribute: "check-ical-option", type: Boolean },
      meeting: { type: Object },
      showingNotificationOptions: { attribute: false, type: Boolean },
      showingICalAlarm: { attribute: false, type: Boolean },
      showingSectionOne: { attribute: false, type: Boolean },
      showingSectionTwo: { attribute: false, type: Boolean },
      showingSectionThree: { attribute: false, type: Boolean },
      showingSectionFour: { attribute: false, type: Boolean },
    };
  }

  constructor() {

    super();

    this.showingICalAlarm = true;
    this.showingSectionOne = true;

    this.loadTranslations("meetings").then(r => this.i18n = r).then(r => {

      this.selTypes = {
        all: {
          id: "ALL",
          title: r.meetings_seltype_all,
        },
        user: {
          id: "USER",
          title: r.meetings_seltype_user,
        },
        group: {
          id: "GROUP",
          title: r.meetings_seltype_group,
        },
        role: {
          id: "ROLE",
          title: r.meetings_seltype_role,
        }
      };
    });

    this.loadTranslations("meetings").then(r => this.i18n = r);
  }

  updateParticipantSelectionUI(e) {

    const selType = e.target.value;
    this.selectedType = selType;
    jQuery('#selOption option').remove();

    if (selType == 'USER' || selType == 'GROUP' || selType == 'ROLE') {
      let opts = null;
      if (selType == 'USER') opts = this.selectionOptions.users;
      if (selType == 'GROUP') opts = this.selectionOptions.groups;
      if (selType == 'ROLE') opts = this.selectionOptions.roles;
      opts.forEach(opt => {
        jQuery('#selOption').append(`<option value="${opt.id}">${opt.title}</option>`);
      });

      $("#selOption").html($("#selOption option").sort((a, b) => {
        return a.text == b.text ? 0 : a.text < b.text ? -1 : 1;
      }));

      const selectedOption = document.getElementById("selOption");
      selectedOption && (this.selectedOptionText = selectedOption.options[selectedOption.selectedIndex].text);

      jQuery('#selOption').removeAttr('disabled');
    } else {
      jQuery('#selOption').attr('disabled', 'disabled');
    }
  }

  optionSelected(e) {

    this.selectedOption = e.target.value;
    this.selectedOptionText = e.target.options[e.target.selectedIndex].text;
  }

  addParticipant() {

    const sel = document.getElementById("selOption");
    const text = sel.options[sel.selectedIndex].text;

    this.meeting.participants.push({
      role: "ATTENDEE",
      selectionId: sel.value,
      selectionType: this.selectedType,
      displayString: text,
    });

    this.requestUpdate();
  }

  removeParticipant(e) {

    const selectionId = e.target.dataset.selectionId;
    const index = this.meeting.participants.findIndex(p => p.selectionId === selectionId);
    this.meeting.participants.splice(index, 1);
    this.requestUpdate();
  }

  toggleStartDate(e) {

    this.showStartDate = !this.showStartDate;

    if (e.target.checked) {
      $('#startDate2 + button').prop("disabled", false);
    } else {
      $('#startDate2 + button').prop("disabled", true);
    }
  }

  toggleEndDate(e) {

    this.showEndDate = !this.showEndDate;

    if (e.target.checked) {
      $('#endDate2 + button').prop("disabled", false);
    } else {
      $('#endDate2 + button').prop("disabled", true);
    }
  }

  toggleShowingNotificationOptions(e) {

    this.meeting.notifyParticipants = e.target.checked;
    this.showingNotificationOptions = e.target.checked;
  }

  cancel() {
    this.dispatchEvent(new CustomEvent("cancel"));
  }

  save() {

    this.meeting.name = this.querySelector("#meetings-meeting-name-field").value;
    this.meeting.welcomeMessage = this.editor.getData();
    if (this.querySelector("#startDate1").checked) {
      this.meeting.startDate = this.querySelector("#startDate").value;
    } else {
      this.meeting.startDate = null;
    }
    if (this.querySelector("#endDate1").checked) {
      this.meeting.endDate = this.querySelector("#endDate").value;
    } else {
      this.meeting.endDate = null;
    }

    const url = `/api/sites/${this.siteId}/meetings`;
    fetch(url, {
      method: "POST",
      credential: "include",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(this.meeting),
    })
    .then(r => {

      if (r.ok) {
        return r.json();
      }
      throw new Error("Network error while saving meeting");
    })
    .then(meeting => {

      this.meeting = meeting;
      this.dispatchEvent(new CustomEvent("meeting-created", { detail: { meeting } }));
    })
    .catch (error => console.error(error));
  }

  shouldUpdate() {
    return this.i18n && this.settings && this.selTypes && this.selectionOptions;
  }

  firstUpdated() {

    this.editor = sakai.editor.launch("meetings-welcome-message-textarea", { toolbar: "Basic"});

    let startDate = new Date().toISOString();
    if (!this.meeting.isNew && this.meeting.startDate) {
      startDate = new Date(this.meeting.startDate).toISOString();
    }

    let endDate = new Date().toISOString();
    if (!this.meeting.isNew && this.meeting.endDate) {
      endDate = new Date(this.meeting.endDate).toISOString();
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

    $('#startDate2 + button').prop("disabled", !this.showStartDate);

    localDatePicker({
      input: '#endDate2',
      useTime: 1,
      val: endDate,
      parseFormat: 'YYYY-MM-DDTHH:mm:ss.SSSZ',
      ashidden:{
        iso8601: "endDate"
      },
    });

    $('#endDate2 + button').prop("disabled", !this.showEndDate);
  }

  renderOpener(showing) {
    return html`
      <sakai-icon
          type="${showing ? "up" : "down"}"
          size="small">
      </sakai-icon>
    `;
  }

  render() {

    return html`

      <div class="meetings-create-meeting-wrapper">
        <a href="javascript:;" @click=${() => this.showingSectionOne = !this.showingSectionOne}>
          <div class="meetings-create-meeting-header">
            <div>${this.i18n.meetings_info}</div>
            <div>
              ${this.renderOpener(this.showingSectionOne)}
            </div>
          </div>
        </a>
        <div class="meetings-create-meeting-block" style="display: ${this.showingSectionOne ? "block" : "none"}">
          <div class="meetings-input-label">${this.i18n.meetings_info_title}</div>
          <div class="meetings-input-wrapper">
            <input id="meetings-meeting-name-field" type="text" .value=${this.meeting.name} style="width: 400px;">
          </div>
          <div class="meetings-input-label">${this.i18n.meetings_info_description}</div>
          <div class="meetings-input-wrapper">
            <textarea id="meetings-welcome-message-textarea">
              ${this.meeting.isNew ? this.i18n.meetings_default_welcome_description : this.meeting.welcomeMessage}
            </textarea>
          </div>
          ${this.settings.recordingEnabled && this.settings.recordingEditable ? html`
            <div class="meetings-checkbox-wrapper">
              <label>
                <input type="checkbox"
                    @click=${e => this.meeting.recording = e.target.checked}
                    ?checked=${this.meeting.recording}>
                <span class="meetings-input-label">${this.i18n.meetings_info_recording}</span>
              </label>
            </div>
          ` : ""}
          ${this.settings.durationEnabled ? html`
            <div class="meetings-input-label">${this.i18n.meetings_info_duration}</div>
            <div class="meetings-input-wrapper">
              <input id="recordingDuration" name="recordingDuration" type="text" .value=${this.meeting.recordingDuration} style="width: 35px;">
              &nbsp;${this.i18n.meetings_info_recording_duration_units}
            </div>
          ` : ""}
          ${this.settings.waitmoderatorEnabled && this.settings.waitmoderatorEditable ? html`
            <div class="meetings-checkbox-wrapper">
              <label>
                <input
                    @click=${e => this.meeting.waitForModerator = e.target.checked}
                    type="checkbox"
                    ?checked=${this.meeting.isNew ? this.settings.waitmoderatorDefault : this.meeting.waitForModerator}>
                <span class="meetings-input-label">${this.i18n.meetings_info_waitformoderator}</span>
              </label>
            </div>
          ` : ""}
          <!--div class="meetings-checkbox-wrapper">
            <label>
              <input
                  @click=${e => this.meeting.properties.disablePublicChat = e.target.checked}
                  type="checkbox"
                  ?checked=${this.meeting.isNew ? false : this.meeting.properties.disablePublicChat === "true"}>
              <span class="meetings-input-label">${this.i18n.meetings_info_disablepublicchat}</span>
            </label>
          </div>
          <div class="meetings-checkbox-wrapper">
            <label>
              <input
                  @click=${e => this.meeting.properties.disablePrivateChat = e.target.checked}
                  type="checkbox"
                  ?checked=${this.meeting.isNew ? false : this.meeting.properties.disablePrivateChat === "true"}>
              <span class="meetings-input-label">${this.i18n.meetings_info_disableprivatechat}</span>
            </label>
          </div-->
          ${this.settings.multiplesessionsallowedEnabled && this.settings.multiplesessionsallowedEditable ? html`
            <div class="meetings-input-label">${this.i18n.meetings_info_multiplesessionsallowed}</div>
            <div class="meetings-input-block">
              <input
                  id="multipleSessionsAllowed"
                  @click=${e => this.meeting.multipleSessionsAllowed = e.target.checked}
                  type="checkbox"
                  ?checked=${this.meeting.isNew ? this.settings.multiplesessionsallowedDefault : this.meeting.multipleSessionsAllowed}>
            </div>
          ` : ""}
          ${this.settings.preuploadpresentationEnabled ? html`
          <!--tr>
            <td class="meetings_lbl">${this.i18n.meetings_info_preuploadpresentation}</td>
            <td>
              <input type="file" id="selectFile" name="selectFile" style="display:inline;"/>
              <span id="meetings_addFile_ajaxInd"></span>
              <input type="hidden" id="fileUrl" name="presentation" />
              <table id="fileView" class="attachList listHier indnt1" style="margin-bottom:.5em; margin-top:0; width:auto; display:none;">
                <tr>
                  <td><a href="#" id="url" target="_blank"/></td>
                  <td><a href="javascript:;" id="removeUpload">${this.i18n.meetings_info_remove}</a></td>
                </tr>
              </table>
            </td>
          </tr-->
          ` : ""}
        </div>
      </div>

      <div class="meetings-create-meeting-wrapper">
        <a href="javascript:;" @click=${() => this.showingSectionTwo = !this.showingSectionTwo}>
          <div class="meetings-create-meeting-header">
            <div>${this.i18n.meetings_participants}</div>
            <div>
              ${this.renderOpener(this.showingSectionTwo)}
            </div>
          </div>
        </a>
        <div class="meetings-create-meeting-block" style="display: ${this.showingSectionTwo ? "block" : "none"}">
          <div class="meetings-input-label">
            <label for="meetings-participant-type-selector">
              ${this.i18n.meetings_participants_add}
            </label>
          </div>
          <div class="meetings-input-block">
            <select id="meetings-participant-type-selector" @change=${this.updateParticipantSelectionUI}>
              <option value="${this.selTypes.all.id}" selected="selected">${this.selTypes.all.title}</option>
              <option value="${this.selTypes.user.id}">${this.selTypes.user.title}</option>
              ${this.selectionOptions.groups.length > 0 ? html`
              <option value="${this.selTypes.group.id}">${this.selTypes.group.title}</option>
              ` : ""}
              <option value="${this.selTypes.role.id}">${this.selTypes.role.title}</option>
            </select>
            <select id="selOption" @change=${this.optionSelected}></select>
            <input id="meetings_add"
                type="button"
                value="${this.i18n.meetings_add}"
                @click=${this.addParticipant}>
          </div>
          <div class="meetings-input-label" style="padding-top: 6px">${this.i18n.meetings_participants_list}</div>
          <div class="meetings-input-wrapper">
            <div id="meetings-participants-grid">
              ${this.meeting.participants.map(p => html`
                <div>
                  <a href="javascript:;"
                      title="${this.i18n.meetings_remove}"
                      data-selection-id="${p.selectionId}"
                      @click=${this.removeParticipant}>
                    <img src="/library/image/silk/cross.png" alt="X" style="vertical-align:middle"/>
                  </a>
                  ${p.selectionType === "USER" ? html`
                  <span class="meetings_role_selection">${this.i18n.meetings_seltype_user}: ${p.displayString}</span>
                  ` : ""}
                  ${p.selectionType === "ALL" ? html`
                  <span class="meetings_role_selection">${this.i18n.meetings_seltype_all}</span>
                  ` : ""}
                  ${p.selectionType === "ROLE" ? html`
                  <span class="meetings_role_selection">${this.i18n.meetings_seltype_role}: ${p.displayString}</span>
                  ` : ""}
                  ${p.selectionType === "GROUP" ? html`
                  <span class="meetings_role_selection">${this.i18n.meetings_seltype_group}: ${p.displayString}</span>
                  ` : ""}
                </div>
                <div class="meetings-role-selection-as">${this.i18n.meetings_as_role}</div>
                <div>
                  <select name="${p.selectionId}">
                    <option value="ATTENDEE" ?selected=${p.role === "ATTENDEE"}>${this.i18n.meetings_role_atendee}</option>
                    <option value="MODERATOR" ?selected=${p.role === "MODERATOR"}>${this.i18n.meetings_role_moderator}</option>
                  </select>
                </div>
              `)}
            </div>
          </div>
        </div>
      </div>

      <div class="meetings-create-meeting-wrapper">
        <a href="javascript:;" @click=${() => this.showingSectionThree = !this.showingSectionThree}>
          <div class="meetings-create-meeting-header">
            <div>${this.i18n.meetings_availability}</div>
            <div>
              ${this.renderOpener(this.showingSectionThree)}
            </div>
          </div>
        </a>
        <div class="meetings-create-meeting-block" style="display: ${this.showingSectionThree ? "block" : "none"}">
          <div class="meetings-input-label">${this.i18n.meetings_availability_startdate}</div>
          <div class="meetings-input-wrapper">
            <input id="startDate1"
                type="checkbox"
                @click=${this.toggleStartDate}
                ?checked=${!this.meeting.new && this.meeting.startDate}>
            <input id="startDate2" name="startDate2" class="datepicker" value="" ?disabled=${!this.showStartDate}>
            <input
                type="checkbox"
                @click=${e => this.meeting.addToCalendar = e.target.checked}
                checked
                style="${!this.settings.canAddCalendarEvent ? "display:none;" : ""}" ?disabled=${!this.showStartDate}>
            <span style="${!this.settings.canAddCalendarEvent ? "display:none;" : ""}">${this.i18n.meetings_availability_addtocal}</span>
          </div>
          <div class="meetings-input-label">${this.i18n.meetings_availability_enddate}</div>
          <div class="meetings-input-wrapper">
            <input id="endDate1"
                type="checkbox"
                @click=${this.toggleEndDate}
                ?checked=${!this.meeting.new && this.meeting.endDate}>
            <input id="endDate2" name="endDate2" class="datepicker" value="" ?disabled=${!this.showEndDate}>
          </div>
        </div>
      </div>

      <div class="meetings-create-meeting-wrapper">
        <a href="javascript:;" @click=${() => this.showingSectionFour = !this.showingSectionFour}>
          <div class="meetings-create-meeting-header">
            <div>${this.i18n.meetings_notification}</div>
            <div>
              ${this.renderOpener(this.showingSectionFour)}
            </div>
          </div>
        </a>
        <div class="meetings-create-meeting-block" style="display: ${this.showingSectionFour ? "block" : "none"}">
          <div class="meetings-checkbox-wrapper">
            <label>
              <input type="checkbox"
                  @click=${e => { this.meeting.notifyParticipants = e.target.checked; this.requestUpdate(); }}>
              <span>${this.i18n[this.meeting.new ? "meetings_notification_notify" : "meetings_notification_notify_edit"]}</span>
            </label>
          </div>
          ${this.meeting.notifyParticipants ? html`
          <div id="meetings-notification-ical-block">
            <div class="meetings-checkbox-wrapper">
              <label>
                <input type="checkbox"
                    ?checked=${this.meeting.iCalAttached}
                    @click=${e => { this.meeting.iCalAttached = e.target.checked; this.requestUpdate(); }}>
                <span>${this.i18n.meetings_notification_notify_ical}</span>
              </label>
            </div>
            ${this.meeting.iCalAttached ? html`
            <div id="meetings-notification-icalminutes-block">
              <span>${this.i18n.meetings_notification_notify_ical_alarm}</span>
              <input id="iCalAlarmMinutes"
                  name="iCalAlarmMinutes"
                  type="text"
                  .value=${this.meeting.iCalAlarmMinutes}
                  @input=${e => this.meeting.iCalAlarmMinutes = e.target.value}
                  style="width: 35px;">
              <span>${this.i18n.meetings_notification_notify_ical_alarm_units}</span>
            </div>
            ` : ""}
          </div>
        ` : ""}
        </div>
      </div>

      <div class="act">
        <input id="meetings_save" type="button" @click=${this.save} class="active" value="${this.i18n.meetings_save}">
        <input id="meetings_cancel" type="button" @click=${this.cancel} value="${this.i18n.meetings_cancel}">
        <span id="meetings_addUpdate_ajaxInd"></span>
      </div>
    `;
  }
}

const tagName = "sakai-create-meeting";
!customElements.get(tagName) && customElements.define(tagName, SakaiCreateMeeting);
