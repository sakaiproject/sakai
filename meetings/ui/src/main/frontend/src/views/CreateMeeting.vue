<template>
  <div class="mt-5 mt-lg-0">
    <SakaiAccordion>
      <SakaiAccordionItem :title="i18n.section_meeting_information" :open="true">
        <div class="pb-4">
          <div class="col-md-6 col-xl-4">
            <SakaiInputLabelled
              :title="i18n.meeting_title"
              v-model="formdata.title"
              :required="true"
              :maxlength="255"
              @validation="setValidation('title', $event)"
            />
          </div>
          <div class="col-md-8 col-xl-5 mt-3">
            <SakaiInputLabelled
              :title="i18n.meeting_description"
              textarea="true"
              :maxlength="4000"
              v-model="formdata.description"
              @validation="setValidation('description', $event)"
            />
          </div>
          <div class="col-md-6 col-xl-4">
            <div class="row mt-3">
              <div class="col">
                <SakaiInputLabelled
                  :title="i18n.video_conferencing_service"
                  select="true"
                  :items="confServiceItems"
                  :disabled="true"
                  v-model="formdata.confService"
                  @validation="setValidation('provider', $event)"
                />
              </div>
            </div>
          </div>
        </div>
      </SakaiAccordionItem>
      <SakaiAccordionItem :title="i18n.section_participants">
        <div class="col-md-6 col-xl-4">
          <div class="row mt-3">
            <div class="col">
              <SakaiRadioGroup
                :label="i18n.participants_selection"
                v-model="formdata.participantOption"
                :value="formdata.participantOption"
                :items="participantOptions"
                :disabled="disableGroupSelection"
              ></SakaiRadioGroup>
              <SakaiInputLabelled
                :title="i18n.select_groups"
                select="true"
                v-model="formdata.groups"
                v-if="formdata.participantOption === 'GROUP'"
                :multiple="true"
                :items="groups"
              />
            </div>
          </div>
        </div>
        <div class="pb-4">
          <div class="sak-banner-info" v-if="disableGroupSelection">
            {{this.i18n.info_no_groups}}
          </div>
        </div>
      </SakaiAccordionItem>
      <SakaiAccordionItem :title="i18n.section_availability">
        <div class="col-md-6 col-xl-4 pb-4">
          <div class="row align-items-md-end mb-3">
            <SakaiInputLabelled
              :title="i18n.open_date"
              type="datetime-local"
              v-model="formdata.dateOpen"
              @update:value.once="!this.hadDateInput"
              @validation="setValidation('dateOpen', $event)"
              :required="true"
            />
          </div>
          <div class="row align-items-md-end mb-3">
            <SakaiInputLabelled
              :title="i18n.close_date"
              type="datetime-local"
              v-model="formdata.dateClose"
              @validation="setValidation('dateClose', $event)"
              :required="true"
              :validate="{
                type: 'custom',
                validationFn: startBeforeEndValidation,
                message: i18n.validation_close_date,
                active: hadDateInput
              }"
            />
          </div>
          <div class="row align-items-md-end mb-3">
            <div class="form-check">
              <input
                class="form-check-input"
                v-model="formdata.saveToCalendar"
                type="checkbox"
                id="save_to_calendar"
              />
              <label class="form-check-label" for="save_to_calendar">
                {{ i18n.add_to_calendar }}
              </label>
            </div>
          </div>
        </div>
      </SakaiAccordionItem>
      <SakaiAccordionItem :title="i18n.section_notifications">
        <div class="col-md-6 col-xl-4 pb-4">
          <div class="row mt-3">
            <div class="col">
              <SakaiInputLabelled
                :title="i18n.notifications"
                select="true"
                :items="notificationTypes"
                v-model="formdata.notificationType"
              />
            </div>
          </div>
        </div>
      </SakaiAccordionItem>
    </SakaiAccordion>
    <div class="d-flex mt-5">
      <SakaiButton
        :text="i18n.save"
        @click="handleSave"
        :primary="true"
        class="me-2"
        :disabled="!allValid"
      />
      <SakaiButton :text="i18n.cancel" @click="handleCancel" />
    </div>
  </div>
</template>

<script>
import dayjs from "dayjs";
import SakaiAccordionItem from "../components/sakai-accordion-item.vue";
import SakaiAccordion from "../components/sakai-accordion.vue";
import SakaiInputLabelled from "../components/sakai-input-labelled.vue";
import SakaiRadioGroup from "../components/sakai-radio-group.vue";
import SakaiButton from "../components/sakai-button.vue";
import SakaiInput from "../components/sakai-input.vue";
import SakaiIcon from "../components/sakai-icon.vue";
import constants from "../resources/constants.js";
import i18nMixn from "../mixins/i18n-mixn.js";

import { mapState, mapActions } from 'pinia'
import { useDataStore } from '../stores/dataStore';

export default {
  name: "create-meeting",
  components: {
    SakaiAccordionItem,
    SakaiAccordion,
    SakaiInputLabelled,
    SakaiButton,
    SakaiInput,
    SakaiRadioGroup,
    SakaiIcon,
  },
  mixins: [i18nMixn],
  data() {
    return {
      formdata: {
        title: "",
        description: "",
        confService: "microsoft_teams",
        saveToCalendar: false,
        dateOpen: null,
        dateClose: null,
        notificationType: "0",
        groups: [],
        participantOption: "SITE",
      },
      groups: [],
      participants: [],
      selectedParticipants: [],
      confServiceItems: [
        {
          string: "Microsoft Teams",
          value: "microsoft_teams",
        },
      ],
      validations: { title: false, description: true, provider: true, dateOpen: true, dateClose: true },
      hadDateInput: false,
      saveEnabled: true
    };
  },
  props: {
    id: { type: String, default: null },
  },
  computed: {
    ...mapState(useDataStore, ["storedData"]),
    disableGroupSelection() {
      return !this.groups || this.groups.length === 0;
    },
    allValid() {
      return this.saveEnabled && !Object.values(this.validations).includes(false);
    },
    participantOptions() {
      return [
        {
          label: this.i18n.all_participants,
          value: "SITE",
        },
        {
          label: this.i18n.group_participants,
          value: "GROUP",
        },
      ];
    },
    notificationTypes() {
      return [
        {
          string: this.i18n.no_notification,
          value: "0",
        },
        {
          string: this.i18n.all_participants_notification,
          value: "1",
        }
      ];
    }
  },
  methods: {
    ...mapActions(useDataStore, ["clearStoredData"]),
    showError(message) {
      this.$emit('showError', message);
    },
    startBeforeEndValidation() {
      return dayjs(this.formdata.dateOpen).isBefore(dayjs(this.formdata.dateClose)); 
    },
    setValidation(field, valid) {
      this.validations[field] = valid;
    },
    async handleSave() {
      this.saveEnabled = false;
      let saveData = {
        id: this.id,
        title: this.formdata.title,
        siteId: this.$route.params.siteid,
        description: this.formdata.description,
        saveToCalendar: this.formdata.saveToCalendar,
        startDate: dayjs(this.formdata.dateOpen).toISOString(),
        endDate: dayjs(this.formdata.dateClose).toISOString(),
        notificationType: this.formdata.notificationType,
        participantOption: (this.formdata.participantOption === 'SITE' ? 1 : 2),
        groupSelection: this.formdata.groups,
        provider: this.formdata.confService,
      };
      let methodToCall = constants.toolPlacement;
      let restMethod = "POST";
      if (this.id) {
        methodToCall = methodToCall + "/meeting/" + this.id;
        restMethod = "PUT";
      } else {
        methodToCall = methodToCall + "/meeting";
      }
      // Invoke REST Controller - Save (POST or PUT)
      const response = await fetch(methodToCall, {
        credentials: "include",
        method: restMethod,
        cache: "no-cache",
        headers: { "Content-Type": "application/json; charset=utf-8" },
        body: JSON.stringify(saveData),
      });
      if(response.ok) {
        this.clearStoredData();
        this.$router.push({ name: "Main" });
      } else if (response.status === 500) {
        this.showError((this.id) ? this.i18n.error_updating_meeting_500 : this.i18n.error_create_meeting_500);
      } else {
        this.showError(this.i18n.error_create_meeting_unknown);
      }
      this.saveEnabled = true;
    },
    handleCancel() {
      this.clearStoredData();
      this.$router.push({ name: "Main" });
    },
    createRoom(participants) {
      this.selectedParticipants = participants;
    },
    addNotification() {
      let newNotification = { ...this.notificationTemplate };
      if (this.notifications.length > 0) {
        newNotification.id =
          this.notifications[this.notifications.length - 1].id + 1;
      }
      let updatedNotifications = [...this.notifications];
      updatedNotifications.push(newNotification);
      this.notifications = updatedNotifications;
    },
    removeNotification(id) {
      let index = this.notifications.findIndex(
        (notification) => notification.id === id
      );
      if (index > -1) {
        this.notifications.splice(index, 1);
      }
    },
    loadGroups() {
      fetch(
        `${constants.toolPlacement}/meetings/site/${this.$route.params.siteid}/groups`
      )
        .then((r) => {
          if (r.ok) {
            return r.json();
          }
          throw new Error(
            `Failed to get groups for site ${this.$route.params.siteid}`
          );
        })
        .then((data) => {
          this.groups = data.map((group) => {
            return {
              string: group.groupName,
              value: group.groupId,
            };
          });
        })
        .catch((error) => this.showError(error));
    },
    checkProviderConfigurations() {
      fetch(
        `${constants.toolPlacement}/meetings/teams/status`
      )
      .then((r) => {
        if (r.ok) {
          return r.json();
        }
        throw new Error(this.i18n.error_video_conferencing_config);
      })
      .then((data) => {
        if (!data) {
          console.log("mensaje recibido: ", data);
          throw new Error(this.i18n.error_video_conferencing_config);
        }
      })
      .catch((error) => {
          this.formdata.confService = null;
          this.validations.provider = false;
          this.showError(error);
      });
    }
  },
  watch: {
    "formdata.dateOpen"(newDate, oldDate) {
      if(!this.hadDateInput && newDate !== oldDate) {
        this.hadDateInput = true;
      }
    },
    "formdata.dateClose"(newDate, oldDate) {
      if(!this.hadDateInput && newDate !== oldDate) {
        this.hadDateInput = true;
      }
    }
  },
  created() {
    if (this.storedData.title) {
      this.validations.title = true;
      this.formdata.title = this.storedData.title;
    }
    if (this.storedData.description) {
      this.formdata.description = this.storedData.description;
    }
    if (this.storedData.dateOpen) {
      this.formdata.dateOpen = dayjs(this.storedData.dateOpen).format(
        "YYYY-MM-DDTHH:mm:ss"
      );
    } else {
      this.formdata.dateOpen = dayjs().format("YYYY-MM-DDTHH:mm") + ":00";
    }
    if (this.storedData.dateClose) {
      this.formdata.dateClose = dayjs(this.storedData.dateClose).format(
        "YYYY-MM-DDTHH:mm:ss"
      );
    } else {
      this.formdata.dateClose = dayjs().add(1, "hour").format("YYYY-MM-DDTHH:mm") + ":00";
    }
    if(this.storedData.participantOption){
      this.formdata.participantOption = this.storedData.participantOption;
    }
    if(this.storedData.savedToCalendar){
      this.formdata.saveToCalendar = true;
    }
    this.formdata.groups = this.storedData.groupSelection;
    this.loadGroups();
    this.checkProviderConfigurations();
  },
};
</script>
