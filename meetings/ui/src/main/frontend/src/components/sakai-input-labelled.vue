<template>
  <div>
    <label v-if="!isCheckbox" class="mb-1" :for="inputId">{{ title }}</label>
    <SakaiTextarea
      v-if="textarea"
      :id="inputId"
      :disabled="disabled"
      :required="required"
      :maxlength="maxlength"
      :validate="validate"
      v-model:value="value"
      @input="$emit('update:value', $event.target.value)"
      @validation="$emit('validation', $event)"
    />
    <SakaiSelect
      v-else-if="select"
      :items="items"
      :id="inputId"
      :disabled="disabled"
      :multiple="multiple"
      v-model:value="value"
      @change="$emit('update:value', this.value)"
    />
    <SakaiInput
      v-else
      v-model:value="value"
      :id="inputId"
      :type="type"
      :disabled="disabled"
      :required="required"
      :maxlength="maxlength"
      :checklabel="title"
      :validate="validate"
      @input="$emit('update:value', $event.target.value)"
      @validation="$emit('validation', $event)"
    >
      <template #prepend>
        <slot name="prepend" />
      </template>
      <template #append>
        <slot name="append" />
      </template>
    </SakaiInput>
  </div>
</template>

<script>
import { v4 as uuid } from "uuid";
import SakaiInput from "./sakai-input.vue";
import SakaiSelect from "./sakai-select.vue";
import SakaiTextarea from "./sakai-textarea.vue";
import { validateProp } from "../mixins/validation-mixin.js";

export default {
  data() {
    return {
      inputId: "input",
    };
  },
  components: {
    SakaiInput,
    SakaiSelect,
    SakaiTextarea
  },
  props: {
    title: {
      type: String,
      default: "Title",
    },
    textarea: {
      textarea: Boolean,
      default: false,
    },
    select: {
      textarea: Boolean,
      default: false,
    },
    items: {
      type: Array,
    },
    type: {
      type: String,
      default: "text",
    },
    value: {
      type: [String, Boolean, Number, Array],
      default: "",
    },
    required: {
      type: Boolean,
      default: false
    },
    maxlength: {
      type: Number
    },
    disabled: {
      type: Boolean,
      default: false
    },
    multiple: {
      type: Boolean,
      default: false
    },
    validate: validateProp,
  },
  computed: {
    isCheckbox() {
      return this.type === 'checkbox';
    },
  },
  created() {
    this.inputId += uuid().substring(8, 13);
  },
};
</script>
