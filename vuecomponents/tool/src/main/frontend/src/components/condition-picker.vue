<template>
  <div class="condition-picker">
    <div class="condition" v-if="conditionsSelectable">
      <b>{{ i18n["pick_condition_as_prereq"] }}</b>
      <BFormGroup :label="i18n['condition']" class="mb-2">
        <BFormSelect plain v-model="selectedConditionOption" :options="conditionOptions"></BFormSelect>
      </BFormGroup>
      <BFormGroup :label="i18n['conjunction']" class="mb-2">
        <BFormSelect plain v-model="selectedConjunction" :options="conjunctionOptions"></BFormSelect>
      </BFormGroup>
      <BButton @click="onAddSubCondition" :disabled="!selectionValid" variant="primary">
        <BSpinner v-if="saving" small :aria-label="i18n['saving_condition_as_prereq']" />
        <BIcon v-else icon="plus-circle" aria-hidden="true" />
        {{ i18n["add_condition_as_prereq"] }}
      </BButton>
    </div>
    <div class="condition" v-else>
      <BAlert show variant="info" class="mb-2">
        {{ i18n["no_condition_to_pick"] }}
      </BAlert>
    </div>
    <div class="mt-2" v-if="andEntities?.length > 0">
      <b class="mb-2">{{ i18n["existing_prereq_conditions_and"] }}</b>
      <BListGroup class="bg-transparent">
        <BListGroupItem class="d-flex align-items-center bg-transparent" v-for="savedEntity in andEntities" :key="savedEntity.condition.id">
          <ConditionText :item="savedEntity.toolItem?.name" :condition="savedEntity.condition" />
          <div class="d-flex ms-auto align-items-center">
            <BButton @click="onRemoveSubCondition(andParentCondition, savedEntity.condition)" variant="icon" :title="i18n['remove_condition']">
              <BIcon icon="trash" aria-hidden="true" font-scale="1.2" />
            </BButton>
          </div>
        </BListGroupItem>
      </BListGroup>
    </div>
    <div class="mt-2" v-if="orEntities?.length > 0">
      <b class="mb-2">{{ i18n["existing_prereq_conditions_or"] }}</b>
      <BListGroup class="bg-transparent">
        <BListGroupItem class="d-flex align-items-center bg-transparent" v-for="savedEntity in orEntities" :key="savedEntity.condition.id">
          <ConditionText :item="savedEntity.toolItem?.name" :condition="savedEntity.condition" />
          <div class="d-flex ms-auto align-items-center">
            <BButton @click="onRemoveSubCondition(orParentCondition, savedEntity.condition)" variant="icon" :title="i18n['remove_condition']">
              <BIcon icon="trash" aria-hidden="true" font-scale="1.2" />
            </BButton>
          </div>
        </BListGroupItem>
      </BListGroup>
    </div>
  </div>
</template>

<style lang="scss">
  @import "bootstrap-vue/dist/bootstrap-vue-icons.min.css";
  @import "bootstrap/dist/css/bootstrap.css";
  @import "../bootstrap-styles/alert.scss";
  @import "../bootstrap-styles/buttons.scss";
  @import "../bootstrap-styles/form.scss";
  @import "../bootstrap-styles/list-group.scss";
  @import "../bootstrap-styles/misc.scss";
</style>

<script>
  // Vue and vue plugins
  import Vue from 'vue';
  import { BootstrapVueIcons } from 'bootstrap-vue';

  // Components
  import {
    BAlert,
    BButton,
    BFormGroup,
    BFormSelect,
    BIcon,
    BListGroup,
    BListGroupItem,
    BSpinner
  } from 'bootstrap-vue';

  import ConditionText from "./condition-text.vue";

  // Mixins
  import i18nMixin from "../mixins/i18n-mixin.js";

  // Utils
  import {
    andParentConditionFilter,
    CONDITION_BUNDLE_NAME,
    formatConditionText,
    makeParentCondition,
    makeRootCondition,
    orParentConditionFilter,
    ConditionOperator,
  } from "../utils/condition-utils.js";

  import {
    isUuid,
  } from "../utils/core-utils.js";

  // Api
  import {
    createCondition,
    getRootCondition,
    getToolItemsWithConditionsForLesson,
    updateCondition,
  } from '../api/conditions-api';

  // Use plugins
  Vue.use(BootstrapVueIcons);

  const defaultToolItems = [];
  const defaultRootCondition = null;
  const defaultSelectedCondition = null;
  const defaultSelectedItem = null;
  const defaultSelectedConjunction = ConditionOperator.AND;

  export default {
    name: "condition-picker",
    components: {
      BAlert,
      BButton,
      BFormGroup,
      BFormSelect,
      BIcon,
      BListGroup,
      BListGroupItem,
      BSpinner,
      ConditionText,
    },
    mixins: [ i18nMixin ],
    props: {
      siteId: {
        type: String,
        required: true,
        validator: isUuid,
      },
      toolId: {
        type: String,
        required: true,
      },
      itemId: {
        type: String,
      },
      lessonId: {
        type: Number,
        required: true,
      },
    },
    data() {
      return {
        i18nBundleName: CONDITION_BUNDLE_NAME,
        toolItems: defaultToolItems,
        rootCondition: defaultRootCondition,
        selectedConditionOption: defaultSelectedCondition,
        selectedConjunction: defaultSelectedConjunction,
        savedConditionId: null,
        saving: false,
        loading: true,
      };
    },
    computed: {
      availableToolItems() {
        return this.toolItems.filter((item) => this.itemId ? item.id != this.itemId : true);
      },
      disabledToolItems() {
        return this.availableToolItems.filter((item) => item.conditions.filter((c) => this.isConditionSelectable(c)).length === 0);
      },
      itemOptions() {
        return this.availableToolItems.map((item) => {
          return {
            value: item.id,
            text: item.name,
            disabled: !!this.disabledToolItems.find((i) => i.id == item.id),
          };
        });
      },
      conditionsSelectable() {
        return this.itemOptions.filter((sco) => !sco.disabled).length > 0;
      },
      selectableConditions() {
        return this.toolItems.filter((item) => this.itemId ? item.id != this.itemId : true);
      },
      conditionOptions() {
        return this.availableToolItems.map((toolItem) => {
          return {
            label: toolItem.name,
            options: toolItem.conditions.map((condition) => {
              return {
                value: condition.id,
                text: formatConditionText(this.i18n, condition, toolItem.name),
                disabled: !!this.savedConditions.find((c) => c.id === condition.id),
              };
            }),
          };
        });
      },
      selectedCondition() {
        return this.availableToolItems
            .flatMap((toolItem) => toolItem.conditions)
            .find((c) => c.id === this.selectedConditionOption) ?? null;
      },
      conjunctionOptions() {
        return [
          {
            value: ConditionOperator.AND,
            text: this.i18n["conjunction_and"],
          },
          {
            value: ConditionOperator.OR,
            text: this.i18n["conjunction_or"],
          },
        ];

      },
      andEntities() {
        return this.rootCondition?.subConditions.find(andParentConditionFilter)?.subConditions.map((condition) => {
          return {
            condition,
            toolItem: this.toolItems.find((item) => item.id == condition.itemId) ?? null,
          };
        });
      },
      orEntities() {
        return this.rootCondition?.subConditions.find(orParentConditionFilter)?.subConditions.map((condition) => {
          return {
            condition,
            toolItem: this.toolItems.find((item) => item.id == condition.itemId) ?? null,
          };
        });
      },
      andParentCondition() {
        return this.rootCondition?.subConditions.find(andParentConditionFilter) ?? null;
      },
      orParentCondition() {
        return this.rootCondition?.subConditions.find(orParentConditionFilter) ?? null;
      },
      savedItem() {
        if (!this.savedConditionId) { return null; }

        return this.toolItems.find((item) => item.conditions.find((condition) => condition.id === this.savedConditionId));
      },
      savedCondition() {
        if (!this.savedItem) { return null; }

        return this.savedItem.conditions.find((condition) => condition.id === this.savedConditionId);
      },
      selectionValid() {
        return !!this.selectedConditionOption;
      },
      savedConditions() {
        return [
          ...this.orParentCondition?.subConditions ?? [],
          ...this.andParentCondition?.subConditions ?? [],
        ];
      },
    },
    methods: {
      isConditionSelectable(condition) {
        return !this.savedConditions.find((c) => c.id === condition.id);
      },
      async requireRootCondition() {
        if (this.rootCondition === defaultRootCondition) {
          this.rootCondition = await createCondition(makeRootCondition(this.siteId, this.toolId, this.itemId));
          this.rootCondition.subConditions = [];
        }

        return true;
      },
      async requireParentCondition(operator) {
        await this.requireRootCondition();

        if ((operator === ConditionOperator.AND && !this.rootCondition.subConditions.find(andParentConditionFilter))
            || (operator === ConditionOperator.OR && !this.rootCondition.subConditions.find(orParentConditionFilter))) {

          const parentCondition = await createCondition(makeParentCondition(this.siteId, operator));

          if (parentCondition) {
            const rootSubConditions = [ ...this.rootCondition.subConditions, parentCondition ];

            this.rootCondition = await updateCondition({ ...this.rootCondition, subConditions: rootSubConditions });
          } else {
            return false;
          }
        }

        return true;
      },
      onAddSubCondition() {
        this.addSubCondition({...this.selectedCondition}).then(
            () => this.onSubConditionAdded(),
            (error) => this.onError(error)
        );
      },
      async addSubCondition(condition) {
        await this.requireParentCondition(this.selectedConjunction);

        const parentCondition = this.selectedConjunction === ConditionOperator.AND
            ? this.rootCondition.subConditions.find(andParentConditionFilter)
            : this.rootCondition.subConditions.find(orParentConditionFilter);

        const updatedParentCondition = await updateCondition({
          ...parentCondition,
          subConditions: [ ...parentCondition.subConditions, condition ],
        });

        const parentConditionIndex = this.rootCondition.subConditions.findIndex((c) => c.id === parentCondition.id);
        this.rootCondition.subConditions.splice(parentConditionIndex , 1, updatedParentCondition);
      },
      onSubConditionAdded() {
        this.selectedConditionOption = defaultSelectedCondition;
        this.selectedConjunction = defaultSelectedConjunction;
      },
      saveCondition() {
        if (this.selectionValid) {
          this.savedConditionId = this.selectedConditionOption;
        }
      },
      changeCondition() {
        this.savedConditionId = defaultSelectedItem;
      },
      onRemoveSubCondition(parentCondition, condition) {
        if (!parentCondition || !condition) {
          this.onError("Condition or parent not defined");
          return;
        }

        // Set saving true for condition
        this.removeSubCondition(parentCondition, condition).then(
            () => { },
            (error) => {
              this.onError(error);
            }
        );
      },
      async removeSubCondition(parentCondition, subCondition) {
        const subConditionIndex = parentCondition.subConditions.findIndex((c) => c.id === subCondition.id);
        const parentConditionIndex = this.rootCondition.subConditions.findIndex((c) => c.id === parentCondition.id);

        if (parentConditionIndex === -1 || subConditionIndex === -1) {
          throw new Error("Parent/child condition not found in root/parent condition");
        }

        // Remove sub condition from parent condition
        const parentSubConditions = [ ...parentCondition.subConditions ];
        parentSubConditions.splice(subConditionIndex, 1);

        // Persist parent condition
        const updatedParentCondition = await updateCondition({
          ...parentCondition,
          subConditions: parentSubConditions ,
        });

        // Replace parent condition with updated parent condition on root condition
        this.rootCondition.subConditions.splice(parentConditionIndex, 1, updatedParentCondition);
      },
      onError(error) {
        console.error(error);
      },
      async loadData() {
        const conditionsPromise = getToolItemsWithConditionsForLesson(this.siteId, this.lessonId);
        const rootConditionPromise = getRootCondition(this.siteId, this.toolId, this.itemId);

        const [ conditions, rootCondition ] = await Promise.all([ conditionsPromise, rootConditionPromise ]);

        this.toolItems = conditions ?? defaultToolItems;
        this.rootCondition = rootCondition ?? defaultRootCondition;
      }
    },
    mounted() {
      if (this.siteId && this.toolId && this.itemId) {
        this.loadData();
      }
      // Setup watcher to load fresh data when siteId, toolId, or itemId changes
      this.$watch((vm) => [vm.siteId, vm.toolId, vm.itemId], () => {
        if (this.siteId && this.toolId && this.itemId) {
          this.loadData();
        }
      });
    }
  };
</script>
