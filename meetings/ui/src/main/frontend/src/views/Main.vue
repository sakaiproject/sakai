<template>
  <div>
    <div class="header-menu d-flex flex-column flex-md-row gap-2 div-heigth" :class="editPermission ? 'mb-4' : ''">
      <div class="order-1 me-md-auto">
        <SakaiButton
          v-if="editPermission"
          :text="i18n.create_new_meeting"
          @click="handleCreateNewMeeting"
          class="w-100"
        >
          <template #prepend>
            <SakaiIcon class="me-1" iconkey="plus" />
          </template>
        </SakaiButton>
      </div>
      <SakaiInput
        type="search"
        :placeholder="i18n.search"
        class="order-0 order-md-2 w-auto"
        style="min-width: 20%"
        v-model:value="searchString"
      >
      </SakaiInput>
      <!-- 
      <SakaiDropdownButton class="order-3" :items="items" text="Options">
      </SakaiDropdownButton>
       -->
    </div>
    <div v-if="searching && meetingsList.length > 0">
      <div class="section-heading">
        <h1 id="flush-headingOne" class="h4">{{ i18n.search_results }}</h1>
        <hr aria-hidden="true" class="mb-0 mt-2" />
      </div>
      <div>
        <div class="accordion-body p-0">
          <div v-if="searchResult.length === 0" class="sak-banner-info">
            {{ i18n.no_results }}
          </div>
          <ul
            v-else
            class="
              list-unstyled
              row row-cols-1 row-cols-md-2 row-cols-xl-3 row-cols-xxl-4
              align-content-stretch
            "
          >
            <li
              class="col pt-4"
              v-for="meeting in searchResult"
              :key="meeting.id"
            >
              <SakaiMeetingCard
                class="h-100"
                :id="meeting.id"
                :editable="editPermission"
                :actions="meeting.actions"
                @onDeleted="handleMeetingDelete"
              >
              </SakaiMeetingCard>
            </li>
          </ul>
        </div>
      </div>
    </div>
    <div v-if="meetingsList.length === 0" class="sak-banner-info">
      {{ i18n.no_meetings_scheduled }}
    </div>
    <div v-if="happeningToday.length > 0 && !searching">
      <div class="section-heading">
        <h1 id="flush-headingOne" class="h4">{{ i18n.today }}</h1>
        <hr aria-hidden="true" class="mb-0 mt-2" />
      </div>
      <div>
        <div class="accordion-body p-0 pb-4">
          <ul
            class="
              list-unstyled
              row row-cols-1 row-cols-md-2 row-cols-xl-3 row-cols-xxl-4
              align-content-stretch
            "
          >
            <li
              class="col pt-4"
              v-for="meeting in happeningToday"
              :key="meeting.id"
            >
              <SakaiMeetingCard
                class="h-100"
                :id="meeting.id"
                :editable="editPermission"
                :actions="meeting.actions"
                @onDeleted="handleMeetingDelete"
              >
              </SakaiMeetingCard>
            </li>
          </ul>
        </div>
      </div>
    </div>
    <div v-if="inFuture.length > 0 && !searching">
      <div class="section-heading">
        <h1 class="accordion-header h4" id="flush-headingTwo">{{ i18n.future }}</h1>
        <hr aria-hidden="true" class="mb-0 mt-2" />
      </div>
      <div>
        <ul class="list-unstyled accordion-body p-0 pb-4">
          <li class="row row-cols-1 row-cols-md-2 row-cols-xl-3 row-cols-xxl-4">
            <div class="col pt-4" v-for="meeting in inFuture" :key="meeting.id">
              <SakaiMeetingCard
                class="h-100"
                :id="meeting.id"
                :editable="editPermission"
                :actions="meeting.actions"
                @onDeleted="handleMeetingDelete"
              >
              </SakaiMeetingCard>
            </div>
          </li>
        </ul>
      </div>
    </div>
    <div v-if="inPast.length > 0 && !searching">
      <div
        class="section-heading d-flex align-items-end"
      >
        <h1 class="mb-0 h4" id="flush-headingThree">{{ i18n.past }}</h1>
<!--         
        <div class="ms-auto">
          <div @click="btnPress2 = !btnPress2" class="ms-auto">
            <SakaiDropdownButton :items="showAll" text="Show All" :clear="true"></SakaiDropdownButton>
          </div>
        </div>
-->
      </div>
      <hr aria-hidden="true" class="mb-0 mt-2" />
      <div>
        <div class="accordion-body p-0 pb-4">
          <ul
            class="
              list-unstyled
              row row-cols-1 row-cols-md-2 row-cols-xl-3 row-cols-xxl-4
            "
            v-if="inPast.length > 0"
          >
            <li class="col pt-4" v-for="meeting in inPast" :key="meeting.id">
              <SakaiMeetingCard
                class="h-100"
                :id="meeting.id"
                :editable="editPermission"
                :actions="meeting.actions"
                @onDeleted="handleMeetingDelete"
              >
              </SakaiMeetingCard>
            </li>
          </ul>
        </div>
      </div>
    </div>
  </div>
</template>
<script>
import SakaiMeetingCard from "../components/sakai-meeting-card.vue";
import SakaiInput from "../components/sakai-input.vue";
import SakaiButton from "../components/sakai-button.vue";
import SakaiDropdownButton from "../components/sakai-dropdown-button.vue";
import SakaiIcon from "../components/sakai-icon.vue";
import constants from "../resources/constants.js";
import i18nMixn from "../mixins/i18n-mixn.js";
import dayjs from "dayjs";
import isTodayPlugin from "dayjs/plugin/isToday";
dayjs.extend(isTodayPlugin)
// eslint-disable-next-line

export default {
  name: "main",
  components: {
    SakaiMeetingCard,
    SakaiInput,
    SakaiButton,
    SakaiDropdownButton,
    SakaiIcon,
  },
  mixins: [i18nMixn],
  data() {
    return {
      meetingsList: [],
      searchString: '',
      editPermission: false,
      btnPress2: false,
      items: [
        {
          id: 0,
          icon: "permissions",
          string: "Permissions",
          route: "/permissions",
        },
        {
          id: 1,
          icon: "template",
          string: "Templates",
          action: this.handleTemplates,
        },
      ],
      showAll: [
        {
          id: 0,
          icon: "all",
          string: "All",
          action: this.handleShowAll,
        },
        {
          id: 1,
          icon: "play",
          string: "Recordings",
          action: this.handleShowRecordings,
        },
      ]
    };
  },
  methods: {
    showError(message) {
        this.$emit('showError', message);
    },
    handleCreateNewMeeting() {
      this.$router.push({ name: "EditMeeting" });
    },
    handleTemplates() {
    },
    handleMeetingDelete(id) {
      if (id) {
        this.meetingsList = this.meetingsList.filter( (meeting) => meeting.id !== id );
      }
    },
    handleMeetingEdit() {
    },
    handleShowAll() {
    },
    handleShowRecordings() {
    },
    async loadMeetingsList() {
      const response = await fetch(constants.toolPlacement + "/meetings/site/" + this.$route.params.siteid);
      if(response.ok) {
        const data = await response.json();
        data.forEach(meeting => {
          //Format dates to localized format
          meeting.startDate = dayjs(meeting.startDate)
            .tz(portal.user.timezone, true).format();
          meeting.endDate = dayjs(meeting.endDate)
            .tz(portal.user.timezone, true).format();
          //Set 'live' value
          meeting.live = false;
          if (dayjs().isAfter(dayjs(meeting.startDate)) && dayjs().isBefore(dayjs(meeting.endDate))) {
              meeting.live = true;
          }
        });
        //Assign data to meetings List
        this.meetingsList = [...data];
      } else {
        this.showError(this.i18n.error_load_meetings);
      }
    },
    meetingsComperator(a,b) {
      return dayjs(a.startDate).isBefore(b.startDate) ? -1 : 1;
    },
    async loadEditPermission() {
      const response = await fetch(`${constants.toolPlacement}/meetings/user/editperms/site/${this.$route.params.siteid}`)
      if(response.ok) {
        const hasPermission = await response.json();
        this.editPermission = hasPermission; 
      } else {
        this.showError(this.i18n.error_load_permissions);
      }
    },
  },
  computed: {
    happeningToday() {
      //Filter meetingsList for meetings that happen today, and are not over
      return this.meetingsList.filter(
        (meeting) =>
          dayjs(meeting.startDate).isToday() && dayjs(meeting.endDate).isAfter(dayjs()) || meeting.live
      ).sort(this.meetingsComperator);
    },
    inPast() {
      return this.meetingsList.filter(
        (meeting) =>
          dayjs(meeting.startDate).isBefore(dayjs()) && !meeting.live
      ).sort(this.meetingsComperator).reverse();
    },
    inFuture() {
      return this.meetingsList.filter(
        (meeting) =>
          dayjs().isBefore(dayjs(meeting.startDate), "day") && !meeting.live
      ).sort(this.meetingsComperator);
    },
    searching() {
      return this.searchString !== '';
    },
    searchResult() {
      let searchString = this.searchString.toLowerCase();
      if (!this.searching) { return [] }
      return this.meetingsList.filter(
        (meeting) =>
          meeting.title.toLowerCase().search(searchString) > -1 || meeting.description.toLowerCase().search(searchString) > -1
      );
    },
  },
  mounted() {
    this.loadEditPermission();
    this.loadMeetingsList();
  },
};
</script>
