<template>
  <div>
    <SakaiAccordion>
      <SakaiAccordionItem :title="i18n.section_meeting_information" :open="true">
        <div class="pb-4">
          <div class="col-md-6 col-xl-4">
            <SakaiInputLabelled
              :title="i18n.meeting_title"
              v-model:value="formdata.title"
              :required="true"
              @validation="setValidation('title', $event)"
            />
          </div>
          <div class="col-md-8 col-xl-5 mt-3">
            <SakaiInputLabelled
              :title="i18n.meeting_description"
              textarea="true"
              v-model:value="formdata.description"
            />
          </div>
          <div class="col-md-6 col-xl-4">
            <!--
            <div class="row mt-3 align-items-md-end">
              <div class="col">
                <SakaiInputLabelled title="Preupload presentation" />
              </div>
              <div class="col-sm-12 col-md-auto mt-3">
                <SakaiButton text="Add" class="w-100" />
              </div>
            </div>
            -->
            <div class="row mt-3">
              <div class="col">
                <SakaiInputLabelled
                  :title="i18n.video_conferencing_service"
                  select="true"
                  :items="confServ"
                  :disabled="true"
                  v-model:value="formdata.confService"
                />
              </div>
            </div>
            <!--
            <div class="row mt-3">
              <div class="col">
                <div class="d-flex">
                  <SakaiInputLabelled text="Record Meeting" type="checkbox"/>
                </div>
                <div class="d-flex">
                  <SakaiInput type="checkbox" />
                  <label class="ms-2" for="input">Disable Chat</label>
                </div>
                <div class="d-flex">
                  <SakaiInput type="checkbox" />
                  <label class="ms-2" for="input">Wait For Moderator</label>
                </div>
              </div>
            </div>
            -->
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
                v-model:value="formdata.groups"
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
              v-model:value="formdata.dateOpen"
              @update:value.once="!this.hadDateInput"
              @validation="setValidation('dateOpen', $event)"
              :required="true"
            />
          </div>
          <div class="row align-items-md-end mb-3">
            <SakaiInputLabelled
              :title="i18n.close_date"
              type="datetime-local"
              v-model:value="formdata.dateClose"
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
          <div class="row align-items-md-end mb-3" v-if="displayCalendarCheck">
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
                v-model:value="formdata.notificationType"
              />
            </div>
          </div>
        </div>
      </SakaiAccordionItem>
      <!--
      <SakaiAccordionItem title="5. Meeting Add-ons">
        <div class="pb-4">
          <div class="d-flex">
            <SakaiInput type="checkbox" />
            <label class="ms-2" for="input">Include Whiteboard</label>
          </div>
          <SakaiButton text="Add Poll" :primary="true" class="mt-3" />
        </div>
      </SakaiAccordionItem>
-->
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
        confService: "",
        saveToCalendar: true,
        dateOpen: null,
        dateClose: null,
        notificationType: "0",
        groups: [],
        participantOption: "SITE",
      },
      groups: [],
      participants: [],
      selectedParticipants: [],
      confServ: [
        {
          string: "Microsoft Teams",
          value: "microsoft_teams",
        },
      ],
      partType: [
        {
          string: "All Site Members",
          value: "all_site_members",
        },
        {
          string: "Role",
          value: "role",
        },
        {
          string: "Selections/Groups",
          value: "sections_or_groups",
        },
        {
          string: "Users",
          value: "users",
        },
      ],
      validations: { title: false, dateOpen: true, dateClose: true },
      hadDateInput: false
    };
  },
  props: {
    id: { type: String, default: null },
    title: { type: String, default: "" },
    description: { type: String, default: "" },
    confService: { type: String, default: "microsoft_teams" },
    savedToCalendar: { type: Boolean, default: false },
    dateOpen: {
      validator(value) {
        return dayjs(value).isValid();
      },
    },
    dateClose: {
      validator(value) {
        return dayjs(value).isValid();
      },
    },
    participantOption: { type: String, default: "SITE" },
    groupSelection: {type: Array, default: new Array() },
  },
  computed: {
    disableGroupSelection() {
      return !this.groups || this.groups.length === 0;
    },
    allValid() {
      return !Object.values(this.validations).includes(false);
    },
    displayCalendarCheck() {
      return this.savedToCalendar === "true" ? false : true;
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
        this.$router.push({ name: "Main" });
      } else if (response.status === 500) {
        this.showError(this.i18n.error_create_meeting_500);
      } else {
        this.showError(this.i18n.error_create_meeting_unknown);
      }
    },
    handleCancel() {
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
    if (this.title) {
      this.validations.title = true;
      this.formdata.title = this.title;
    }
    if (this.description) {
      this.formdata.description = this.description;
    }
    if (this.confService) {
      this.formdata.confService = this.confService;
    }
    if (this.dateOpen) {
      this.formdata.dateOpen = dayjs(this.dateOpen).format(
        "YYYY-MM-DDTHH:mm:ss"
      );
    } else {
      this.formdata.dateOpen = dayjs().format("YYYY-MM-DDTHH:mm") + ":00";
    }
    if (this.dateClose) {
      this.formdata.dateClose = dayjs(this.dateClose).format(
        "YYYY-MM-DDTHH:mm:ss"
      );
    } else {
      this.formdata.dateClose = dayjs().add(1, "hour").format("YYYY-MM-DDTHH:mm") + ":00";
    }
    this.formdata.saveToCalendar = this.savedToCalendar === "true";
    this.formdata.participantOption = this.participantOption;
    this.formdata.groups = this.groupSelection;
    this.loadGroups();
  },
};
</script>
