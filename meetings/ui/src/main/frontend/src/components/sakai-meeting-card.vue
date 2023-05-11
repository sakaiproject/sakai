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
          text="Join Meeting"
        >
        </sakai-button>
      </div>
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
      showBannerInfo: false
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
        { "string": this.i18n.get_link_action, "icon": "link", "action": this.getMeetingLink, "url": this.url, "show": this.editable },
        { "string": this.i18n.check_recordings_action, "icon": "videocamera", "action": this.checkMeetingRecordings, "show": true },
        { "string": this.i18n.delete_action, "icon": "delete", "action": this.askDeleteMeeting, "show": this.editable}
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
      const storage = document.createElement('textarea');
      storage.value = this.url;
      this.$el.appendChild(storage);
      storage.select();
      storage.setSelectionRange(0, 99999);
      document.execCommand('copy');
      this.$el.removeChild(storage);
      this.showBannerInfo = true;
      setTimeout(function(){ this.showBannerInfo = false; }.bind(this), 3000);
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
