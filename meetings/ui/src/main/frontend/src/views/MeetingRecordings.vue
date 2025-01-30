<template>
  <h2 class="mt-5 mt-lg-0">
    {{ i18Title }} <span @click="loadRecordingsList(true)"><SakaiIcon role="button" iconkey="refresh" :title="i18n.refresh" /></span>
  </h2>

  <div class="recording-list">
    <div v-if="loading" class="row row-cols-1 text-center fs-1 mt-3">
      <SakaiIcon iconkey="spinner" />
    </div>
    <div v-else-if="recordingsList.length === 0" class="sak-banner-info">
      {{ i18n.no_recordings_found }}
    </div>
    <ul v-else
      class="
        list-unstyled
        row row-cols-1 row-cols-md-2 row-cols-xl-3 row-cols-xxl-4
      "
    >
      <li
        class="col pt-4"
        v-for="recording in recordingsList"
        :key="recording.id"
      >
          <sakai-recording v-bind="recording" />
      </li>
    </ul>
  </div>

  <div class="mt-5">
    <SakaiButton :text="i18n.back" @click="handleBack" />
  </div>
</template>

<script>

import SakaiButton from "../components/sakai-button.vue";
import SakaiRecording from "../components/sakai-recording.vue";
import SakaiIcon from "../components/sakai-icon.vue";
import constants from "../resources/constants.js";
import i18nMixn from "../mixins/i18n-mixn.js";

import { mapState, mapActions } from 'pinia'
import { useDataStore } from '../stores/dataStore';

export default {
  name: "meeting-recordings",
  components: {
    SakaiButton,
    SakaiRecording,
    SakaiIcon
  },
  mixins: [i18nMixn],
  data() {
    return {
      recordingsList: [],
      loading: false
    };
  },
  props: {
    meetingId: { type: String, default: null },
  },
  computed: {
    ...mapState(useDataStore, { title: store => store.storedData.title }),
    i18Title() {
      return this.i18n.meeting_title?.replace("{}", this.title)
    }
  },
  methods: {
    ...mapActions(useDataStore, ["clearStoredData"]),
    showError(message) {
      this.$emit('showError', message);
    },
    async loadRecordingsList(force) {
      this.loading = true;
      let url = constants.toolPlacement + "/meeting/" + this.meetingId + "/recordings";
      if(force){
        url += "?force=true";
      }
      const response = await fetch(url);
      if(response.ok) {
        this.recordingsList = await response.json();
      } else {
        this.showError(this.i18n.error_load_recordings);
      }
      this.loading = false;
    },
    handleBack() {
      this.clearStoredData();
      this.$router.push({ name: "Main" });
    }
  },
  created() {
    this.loadRecordingsList();
  },
};
</script>

<style scoped lang="scss">
  #meetings-tool {
    h2 {
      font-weight: 600;
      font-size: 22px;
    }
  }
</style>
