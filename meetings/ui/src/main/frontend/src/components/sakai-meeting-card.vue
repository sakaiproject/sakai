<template>
  <div class="card" aria-describedby="title">
    <div class="card-header h-100">
      <div class="mt-1 mb-2 contextTitle">{{ contextTitle }}</div>
      <h2 id="title" class="card-title" :title="title">{{ title }}</h2>
      <SakaiDropdownButton
        :items="menuitems"
        :circle="true"
        :clear="true"
        :textHidden="true"
        class="card-menu"
        :text="i18n.menu_name"
      >
        <template #append>
          <sakai-icon iconkey="menuKebab" />
        </template>
      </SakaiDropdownButton>
      <SakaiModal
        v-if="editable"
        ref="deleteModal"
        :title="i18n.delete_modal_title"
      >
        <template #body>
          <div class="sak-banner-warn mt-0 mb-0">
            {{ deleteModalMessage }}
          </div>
        </template>
        <template #footer>
          <SakaiButton :primary="true" :text="i18n.delete_modal_confirm" @click="deleteMeeting"></SakaiButton>
          <SakaiButton :text="i18n.delete_modal_cancel" @click="$refs.deleteModal.hide()"></SakaiButton>
        </template>
      </SakaiModal>
      <div v-if="showBannerInfo" class="sak-banner-info">
        {{ i18n.message_link_copied }}
      </div>
      <div class="d-flex flex-row flex-wrap mb-2">
        <div>
          {{ schedule }}
        </div>
        <div v-if="currentStatus != status.over" class="mx-1">
          <span>-</span>
        </div>
        <div
          v-if="currentStatus != status.over"
          class="d-flex flex-row flex-nowrap"
        >
          <div>
            <span class="sr-only">{{ `${i18n.status} ` }}</span>
            <span>{{ statusText }}</span>
          </div>
          <div class="mx-1">
            <sakai-icon
              :iconkey="statusIcon"
              :color="
                currentStatus == status.live ? liveIconColor : otherIconColor
              "
            />
          </div>
        </div>
      </div>
      <sakai-modal :title="this.i18n.decription_modal_title">
        <template #activator>
          <sakai-button
            v-if="description"
            :text="i18n.decription_link_text"
            :link="true"
            tabindex="0"
          ></sakai-button>
        </template>
        <template #body>{{ description }}</template>
      </sakai-modal>
      <sakai-avatar-list
        :userlist="shownParticipants"
        :avatarsize="avatarHeight"
        :length="maxAvatars"
      ></sakai-avatar-list>
    </div>
    <div v-if="showCardBody" class="card-body p-0 d-flex">
      <div class="action-list d-flex">
        <div v-for="action in actions" :key="action.icon">
          <sakai-button
            :circle="true"
            :clear="true"
            :text="action.label"
            :textHidden="true"
          >
            <template #prepend>
              <sakai-icon :iconkey="action.icon" />
            </template>
          </sakai-button>
        </div>
        <slot name="actions"> </slot>
      </div>
      <div class="ms-auto p-1">
        <slot name="right"> </slot>
        <sakai-button
          v-if="showJoinButton"
          :disabled="!live"
          :primary="true"
          @click="joinMeeting"
          :text="i18n.join_action"
        >
        </sakai-button>
      </div>
    </div>
  </div>
  <div v-if="isLoading" class="sakai-modal-overlay">
    <div class="loading-modal-content">
      <p>{{ i18n.loading_report_data }}</p>
      <span class="spinner"></span>
    </div>
  </div>
  <div v-if="errorMessage" class="alert error-alert">
    <span class="alert-icon">⚠️</span>
    <span class="alert-message">{{ i18n.download_report_error_message }}</span>
    <button class="close-alert-btn" @click="clearErrorMessage">✖️</button>
  </div>
  <div v-if="showPreview" class="sakai-modal-overlay" @click.self="showPreview = false">
    <div class="sakai-modal-content">
      <h3 class="modal-title">{{ i18n.preview_report }}</h3>
      <div v-if="filteredCsvData.length === 0" class="no-data-message">
            <p>{{ i18n.no_preview_report }}</p>
      </div>
      <table v-else class="csv-preview-table">
        <thead>
          <tr>
            <th v-for="header in csvHeaders" :key="header">{{ header }}</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="(row, rowIndex) in csvData" :key="rowIndex">
            <td v-for="cell in row" :key="cell">{{ cell }}</td>
          </tr>
        </tbody>
      </table>
      <button class="close-modal-btn" @click="showPreview = false">{{ i18n.close_action }}</button>
    </div>
  </div>
</template>

<style>
.action-list > div {
  border-right: 1px solid var(--sakai-border-color);
  padding: 0.125rem;
}
</style>

<style scoped lang="scss">
#meetings-tool {
.card {
  border: 1px solid var(--sakai-border-color);
  border-radius: 6px;
}
.card-header {
  background-color: var(--sakai-background-color-2);
  border-radius: 6px 6px 0 0;
}

.card-body {
  background-color: var(--sakai-background-color-1);
  border-radius: 0 0 0px 6px;
}
.card-menu {
  position: absolute;
  right: 0.5rem;
  top: 0.5rem;
}

h2 {
  font-weight: 600;
  font-size: 22px;
}

.contextTitle {
  text-transform: uppercase;
}

}
.sakai-modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background-color: rgba(0, 0, 0, 0.7);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
  transition: opacity 0.3s ease;
}

.sakai-modal-content {
  background-color: #fff;
  padding: 2rem;
  border-radius: 12px;
  max-width: fit-content;
  max-height: 90%;
  overflow-y: auto;
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.2);
  animation: slide-down 0.3s ease;
}

@keyframes slide-down {
  from {
    transform: translateY(-20px);
    opacity: 0;
  }
  to {
    transform: translateY(0);
    opacity: 1;
  }
}

.modal-title {
  font-size: 1.5rem;
  margin-bottom: 1rem;
  color: #333;
}

.csv-preview-table {
  width: 100%;
  border-collapse: collapse;
  margin-top: 1rem;
}

.csv-preview-table th,
.csv-preview-table td {
  border: 1px solid #ddd;
  padding: 0.8rem;
  text-align: left;
}

.csv-preview-table th {
  background-color: #f7f7f7;
  color: #555;
}

.csv-preview-table tr:hover {
  background-color: #f1f1f1;
}

.no-data-message {
  text-align: center;
  font-weight: bold;
  color: #777;
  margin: 1rem 0;
}

.close-modal-btn {
  background-color: #dc3545;
  color: white;
  border: none;
  border-radius: 8px;
  padding: 10px 20px;
  font-size: 1rem;
  font-weight: bold;
  cursor: pointer;
  transition: background-color 0.3s ease, transform 0.3s ease;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  margin-top: 1rem;
}

.close-modal-btn:hover {
  background-color: #c82333;
  transform: translateY(-2px);
}

.loading-indicator {
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  color: #333;
}

.loading-modal-content {
  background-color: #fff;
  padding: 20px;
  border-radius: 8px;
  text-align: center;
  box-shadow: 0px 4px 12px rgba(0, 0, 0, 0.2);
}

.spinner {
  display: inline-block;
  margin-top: 15px;
  width: 40px;
  height: 40px;
  border: 4px solid #ccc;
  border-top-color: #333;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

.alert {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background-color: #f8d7da;
  color: #721c24;
  border: 1px solid #f5c6cb;
  padding: 10px 20px;
  border-radius: 5px;
  box-shadow: 0px 4px 10px rgba(0, 0, 0, 0.1);
  margin: 10px 0;
  font-size: 16px;
}

.error-alert {
  background-color: #f8d7da;
  color: #721c24;
}

.alert-icon {
  margin-right: 10px;
  font-size: 20px;
}

.alert-message {
  flex: 1;
}

.close-alert-btn {
  background: none;
  border: none;
  color: #721c24;
  font-size: 18px;
  cursor: pointer;
}

.close-alert-btn:hover {
  color: #d9534f;
}

</style>

<script>
import "string.prototype.format";
import SakaiAvatarList from "./sakai-avatar-list.vue";
import SakaiIcon from "./sakai-icon.vue";
import SakaiButton from "./sakai-button.vue";
import SakaiDropdownButton from "./sakai-dropdown-button.vue";
import SakaiModal from "./sakai-modal.vue";
import i18nMixn from "../mixins/i18n-mixn.js";

import constants from "../resources/constants.js";

import dayjs from "dayjs";
import relativeTimePlugin from "dayjs/plugin/relativeTime";
import localizedFormatPlugin from "dayjs/plugin/localizedFormat";
import utcPlugin from "dayjs/plugin/utc";
import timezonePlugin from "dayjs/plugin/timezone";

import { mapWritableState } from 'pinia'
import { useDataStore } from '../stores/dataStore';

dayjs.extend(relativeTimePlugin);
dayjs.extend(localizedFormatPlugin);
dayjs.extend(utcPlugin);
dayjs.extend(timezonePlugin);

export default {
  name: "card",
  components: {
    SakaiAvatarList,
    SakaiIcon,
    SakaiButton,
    SakaiDropdownButton,
    SakaiModal,
  },
  mixins: [i18nMixn],
  data() {
    return {
      status: { live: 0, waiting: 1, timeUntil: 2, over: 3 },
      avatarHeight: 40,
      liveIconColor: "var(--sakai-color-red)",
      otherIconColor: "var(--sakai-secondary-color-1)",
      i18n: {
        status: "Status",
      },
      title: { type: String, default: null },
      contextTitle: { type: String, default: null },
      description: { type: String, default: null },
      live: { type: Boolean, default: false },
      url: { type: Boolean, default: null },
      startDate: {
          validator(value) {
            return dayjs(value).isValid();
          },
        },
      endDate: {
          validator(value) {
            return dayjs(value).isValid();
          },
        },
      participants: { type: Array, default: new Array() },
      savedToCalendar: { type: Boolean, default: false },
      participantOption: {type: String, default: null },
      groupSelection: {type: Array, default: new Array() },
      showBannerInfo: false,
      showPreview: false,
      csvHeaders: [],
      csvData: [],
      isLoading: false,
      errorMessage: false,
    };
  },
  props: {
    id: { type: String, default: null },
    editable: { type: Boolean, default: false },
    maxAvatars: {
      default: 5,
      validator(value) {
        return value >= 1;
      },
    },
    actions: { type: Array, default: new Array() },
  },
  computed: {
    ...mapWritableState (useDataStore, ["storedData"]),
    schedule() {
      let start = dayjs(this.startDate);
      let end = dayjs(this.endDate);
      let startTextFormat;
      let endTextFormat;
      if (dayjs().isSame(start, "year")) {
        //starts this year
        if (dayjs().isSame(start, "week")) {
          //starts this week
          if (dayjs().isSame(start, "day")) {
            //starts today
            startTextFormat = "LT";
          } else {
            //starts this week, not today
            startTextFormat = "ddd LT";
          }
        } else {
          //starts this year, not this week
          startTextFormat = "lll";
        }
      } else {
        //starts other year
        startTextFormat = "lll";
      }

      if (start.isSame(end, "day")) {
        //meeting covers just one local day
        endTextFormat = "LT";
      } else {
        //meeting covers more then one local day
        endTextFormat = dayjs().isSame(end, "week") ? "ddd LT" : "lll";
      }
      let startText = start.format(startTextFormat);
      let endText = end.format(endTextFormat);
      return startText + " - " + endText;
    },
    currentStatus() {
      if (this.live) {
        return this.status.live;
      } else {
        if (dayjs().isBefore(dayjs(this.startDate))) {
          return this.status.timeUntil;
        } else if (dayjs().isAfter(dayjs(this.endDate))) {
          return this.status.over;
        } else {
          return this.status.waiting;
        }
      }
    },
    statusText() {
      switch (this.currentStatus) {
        case this.status.live:
          return this.i18n.status_text_live;
        case this.status.waiting:
          return this.i18n.status_text_waiting;
        case this.status.timeUntil:
          return `${this.i18n.status_text_starts} ${dayjs().to(dayjs(this.startDate))}`;
        default:
          return this.i18n.status_text_unknown;
      }
    },
    statusIcon() {
      switch (this.currentStatus) {
        case this.status.live:
          return "live";
        case this.status.waiting:
          return "hourglassEmty";
        case this.status.timeUntil:
          return "bell";
        default:
          return "error";
      }
    },
    menuitems() { 
      return [
        { "string": this.i18n.edit_action, "icon": "edit", "action": this.editMeeting, "show": this.editable },
        { "string": this.i18n.get_link_action, "icon": "link", "action": this.getMeetingLink, "url": this.url, "show": this.editable && this.showJoinButton },
        { "string": this.i18n.check_recordings_action, "icon": "videocamera", "action": this.checkMeetingRecordings, "show": true },
        { "string": this.i18n.delete_action, "icon": "delete", "action": this.askDeleteMeeting, "show": this.editable},
        { "string": this.i18n.attendance_report_action, "icon": "download", "show": true,
        "subMenu": [{ "string": this.i18n.download_report_excel, "icon": "fileCsv", "action": () => this.fetchAttendanceReport('download'), "show": true },
                    { "string": this.i18n.preview_report, "icon": "eye", "action": () => this.fetchAttendanceReport('preview'), "show": true }
        ]}
      ];
    },
    showJoinButton() {
      return this.currentStatus !== this.status.over; 
    },
    showCardBody() {
      let actionsShown = this.actions && this.actions.length > 0;
      return actionsShown || this.showJoinButton;
    },
    shownParticipants() {
      let maxAvatars = Math.round(this.maxAvatars);
      if (maxAvatars && this.participants.length > maxAvatars) {
        let hidden = this.participants.length - (maxAvatars - 1);
        let shown = this.participants.slice(0, maxAvatars - 1);
        let plus = { text: "+" + hidden };
        shown.push(plus);
        return shown;
      } else {
        return this.participants;
      }
    },
    deleteModalMessage() {
      if(this.i18n && this.i18n.delete_modal_message) {
        return this.i18n.delete_modal_message.format(this.title);
      }
    },
    filteredCsvData() {
      return this.csvData.filter(row =>
        row.every(cell => cell !== "" && cell !== null && cell !== undefined)
      );
    }
  },
  methods: {
    joinMeeting(){
     window.open(this.url);
    },
    askDeleteMeeting() {
      this.$refs.deleteModal.show();
    },
    deleteMeeting() {
      this.$refs.deleteModal.hide();
      // Delete meeting
        fetch(constants.toolPlacement + '/meeting/' + this.id, {
          credentials: 'include',
          method: 'DELETE',
          cache: "no-cache",
          headers: { "Content-Type": "application/json; charset=utf-8" },
        })
        .catch((error) => console.error('Error:', error))
        .then((response) => this.$emit('onDeleted', this.id));
    },
    fetchAttendanceReport(action) {
      this.isLoading = true;
      fetch(`${constants.toolPlacement}/meeting/${this.id}/attendanceReport?format=csv`, {
        credentials: 'include',
        method: 'GET',
        cache: "no-cache",
        headers: { "Content-Type": "application/json; charset=utf-8" },
      })
      .then(response => {
        if (!response.ok) {
          throw new Error('Network response was not ok');
        }
        this.errorMessage = false;

        if (action === 'download') {
          return response.blob();
        } else if (action === 'preview') {
          return response.text();
        }
      })
      .then(data => {
        if (action === 'download') {
          const url = window.URL.createObjectURL(data);
          const a = document.createElement('a');
          a.href = url;
          a.download = `attendance_report.csv`;
          document.body.appendChild(a);
          a.click();
          a.remove();
        } else if (action === 'preview') {
          const rows = data.split('\n').map(row => row.split(',')).filter(row => row.some(cell => cell.trim() !== ''));
          this.csvHeaders = rows[0];
          this.csvData = rows.slice(1);
          this.showPreview = true;
        }
        this.isLoading = false;
      })
      .catch(error => {
        this.isLoading = false;
        this.errorMessage = true;
        console.error('Error fetching attendance report:', error);
      });
    },
    clearErrorMessage() {
      this.errorMessage = false;
    },
    editMeeting() {
      let parameters = {
        id: this.id,
        title: this.title,
        description: this.description,
        dateOpen: this.startDate,
        dateClose: this.endDate,
        savedToCalendar: this.savedToCalendar,
        participantOption: this.participantOption,
        groupSelection: this.groupSelection
      };
      this.storedData = parameters;
      this.$router.push({name: "EditMeeting", params: { id: this.id}});
    },
    getMeetingLink() {
      navigator.clipboard.writeText(this.url).then(() => {
        this.showBannerInfo = true;
        setTimeout(() => {
          this.showBannerInfo = false;
        }, 3000);
      }).catch(err => {
        console.error('Error copying to clipboard: ', err);
      });
        return false;
    },
    checkMeetingRecordings() {
      let parameters = {
          meetingId: this.id,
          title: this.title
      };
      this.storedData = parameters;
      this.$router.push({name: "CheckRecordings", params: {meetingId: this.id}});
    }
  },
  mounted () {
      fetch(constants.toolPlacement + '/meeting/'  + this.id)
      .then((r) => {
        if (r.ok) {
          return r.json();
        }
        throw new Error(`Failed to get meetings from ${this.url}`);
      })
      .then((data) => {
            this.live = false;
            this.contextTitle = data.contextTitle;
            this.startDate = dayjs(data.startDate)
              .tz(portal.user.timezone, true).format();
            this.endDate = dayjs(data.endDate)
              .tz(portal.user.timezone, true).format();
            if (dayjs().isAfter(dayjs(data.startDate)) && dayjs().isBefore(dayjs(data.endDate))) {
                this.live = true;
            }
            this.title = data.title;
            this.description = data.description;
            this.savedToCalendar = data.saveToCalendar;
            this.participantOption = data.participantOption;
            this.groupSelection = data.groupSelection;
            this.url = data.url;
            this.provider = data.provider;
            this.participants = data.participants;
      })
      .catch ((error) => console.error(error));
  }
};
</script>

