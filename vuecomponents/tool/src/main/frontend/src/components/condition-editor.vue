<template>
  <div ref="component" class="condition-editor">
    <b>
    <label class="mb-2" for="create-condition">{{ labels.createCondition }}</label>
    </b>
    <form id="create-condition" class="d-flex flex-column flex-lg-row gap-2">
      <div class="d-flex flex-column flex-sm-row align-items-sm-center gap-2">
        <span>{{ i18n["form_require_item_points"] }}</span>
        <div class="flex-grow-1 flex-lg-grow-0">
          <BFormSelect class="form-select" v-model="form.type" :options="operators" :disabled="saving"></BFormSelect>
        </div>
      </div>
      <div class="d-flex align-items-center gap-2 flex-grow-1">
        <BFormGroup>
          <BFormInput type="text" class="argument" v-model="form.argument" :disabled="saving" />
        </BFormGroup>
        <span>{{ i18n["points"] }}</span>
        <BButton class="ms-auto" variant="primary" @click="addCondition" :disabled="!inputValid || saving">
          <BIcon v-if="!saving" icon="plus-circle" aria-hidden="true" />
          <BSpinner v-if="saving" small :aria-label="i18n['saving_condition']" />
          {{ i18n["add_condition"] }}
        </BButton>
      </div>
    </form>
    <div class="mt-2" v-if="conditions.length > 0">
      <b class="mb-2">{{ labels.existingConditions }}</b>
      <BListGroup class="bg-transparent">
        <BListGroupItem class="d-flex align-items-center bg-transparent" v-for="condition in conditions" :key="condition.id">
          <ConditionText :condition="condition" />
          <div class="d-flex ms-auto align-items-center">
            <BBadge v-if="condition.hasParent" variant="info">{{ i18n["tag_in_use"] }}</BBadge>
            <BBadge v-else variant="info">{{ i18n["tag_unused"] }}</BBadge>
             <BButton @click="removeCondition(condition)" variant="icon" :title="i18n['remove_condition']" :disabled="condition.hasParent">
              <BIcon v-if="!condition.saving" icon="trash" aria-hidden="true" font-scale="1.2" />
              <BSpinner v-if="condition.saving" small :aria-label="i18n['removing_condition']" />
            </BButton>
          </div>
        </BListGroupItem>
      </BListGroup>
    </div>
  </div>
</template>

<style lang="scss">
  @import "bootstrap/dist/css/bootstrap.css";
  @import "bootstrap-vue/dist/bootstrap-vue-icons.min.css";
  @import "../bootstrap-styles/badges.scss";
  @import "../bootstrap-styles/buttons.scss";
  @import "../bootstrap-styles/form.scss";
  @import "../bootstrap-styles/list-group.scss";
  @import "../bootstrap-styles/misc.scss";

  .condition {
    display: flex;
    justify-content: space-between;
  }

  .argument {
    width: 5em;
  }
</style>

<script>
// Vue and vue plugins
import Vue from 'vue';
import { BootstrapVueIcons } from 'bootstrap-vue';

// Components
import {
  BButton,
  BFormGroup,
  BFormInput,
  BFormSelect,
  BIcon,
  BListGroup,
  BListGroupItem,
  BSpinner,
  BBadge,
} from 'bootstrap-vue';

import ConditionText from "./condition-text.vue";

// Mixins
import i18nMixin from "../mixins/i18n-mixin.js";

// API
import {
  getConditionsForItem,
  createCondition,
  deleteCondition,
} from "../api/conditions-api.js";

import {
  CONDITION_BUNDLE_NAME,
  CONDITION_OPERATORS,
  ConditionType,
  formatOperator,
  nonParentConditionFilter,
  nonRootConditionFilter,
} from "../utils/condition-utils.js";

Vue.use(BootstrapVueIcons);

const defaultType = "GREATER_THAN";
const defaultArgument = "";

export default {
  name: "condition-editor",
  components: {
    BButton,
    BFormGroup,
    BFormInput,
    BFormSelect,
    BIcon,
    BListGroup,
    BListGroupItem,
    BSpinner,
    BBadge,
    ConditionText,
  },
  mixins: [ i18nMixin ],
  props: {
    siteId: { type: String },
    toolId: { type: String },
    itemId: { type: String },
    labelCreateCondition: { type: String, default: null },
    labelExistingConditions : { type: String, default: null },
  },
  data() {
    return {
      i18nBundleName: CONDITION_BUNDLE_NAME,
      saving: false,
      conditions: [],
      form: {
        type: defaultType,
        argument: defaultArgument,
      },
    };
  },
  methods: {
    onConditionSaved(condition) {
        this.conditions.push(condition);
        this.saving = false;
        this.form.type = defaultType;
        this.form.argument = defaultArgument;
    },
    onConditionRemoved(conditionId) {
      // Remove condition form conditions
      this.conditions.splice(this.conditions.findIndex((c) => c.id === conditionId), 1);
    },
    async addCondition() {
      if (this.inputValid) {
        this.saving = true;

        const condition = {
          type: ConditionType.SCORE,
          siteId: this.siteId,
          toolId: this.toolId,
          itemId: this.itemId,
          operator: this.form.type,
          argument: this.form.argument,
        };

        const createdCondition = await createCondition(condition);

        if (createdCondition) {
          this.onConditionSaved(createdCondition);
        } else {
          this.saving = false;
          throw new Error("Condition not created");
        }
      }
    },
    async removeCondition(condition) {
      // Set saving true for condition
      this.conditions.splice(
        this.conditions.findIndex((c) => c.id === condition.id),
        1,
        { ...condition, saving: true }
      );

      const removedConditionId = await deleteCondition(condition);
      if (removedConditionId != null) {
        this.onConditionRemoved(condition.id);
      } else {
        this.conditions.splice(
          this.conditions.findIndex((c) => c.id === condition.id),
          1,
          { ...condition, saving: false }
        );
        throw new Error("Condition not deleted");
      }
    },
    typeLabel(type) {
      return this.operators.find((t) => t.value === type)?.text ?? "";
    },
    async loadData() {
      const conditions = await getConditionsForItem(this.siteId, this.toolId, this.itemId);
      this.conditions = conditions != null
          ? conditions.filter(nonRootConditionFilter).filter(nonParentConditionFilter)
          : [];
    },
  },
  computed: {
    operators() {
      return CONDITION_OPERATORS.map((operator) => {
        return {
          value: operator,
          text: formatOperator(this.i18n, operator)
        };
      });
    },
    inputValid() {
      const value = this.form.argument.trim();
      return value !== ""
          ? !isNaN(value) && Number(value) >= 0
          : null;
    },
    labels() {
      return {
        createCondition: this.labelCreateCondition ?? this.i18n["create_condition_for_this_item"],
        existingConditions: this.labelExistingConditions ?? this.i18n["existing_conditions_for_this_item"],
      };
    },
  },
  mounted() {
    this.loadData();

    // Setup watcher to load fresh data when siteId, toolId, or itemId changes
    this.$watch((vm) => [vm.siteId, vm.toolId, vm.itemId], () => this.loadData());
  },
};
</script>
