<template>
<fieldset>
  <legend class="form-label">{{ label }}</legend>
  <div class="form-check" v-for="input in inputs">
    <input class="form-check-input" :disabled="disabled" type="radio" :name="name" :value="input.value" :id="input.id" v-model="modelValue">
    <label class="form-check-label" :for="input.id">{{ input.label }}</label>
  </div>
</fieldset>
</template>

<script>
import uidMixin from "../mixins/uid-mixin.js";

export default {
  mixins:[uidMixin],
  data() {
    return {
      modelValue: null
    };
  },
  props: {
    label: {
      type: String,
      default: ""
    },
    value: {
      type: [String, Number],
      default: null
    },
    emits: ['update:modelValue'],
    items: {
      type: Array,
      default: []
    },
    disabled: {
      type: Boolean,
      default: undefined
    }
  },
  computed: {
    name() {
      return `radio-group-${this.uid}`;
    },
    inputs() {
      return this.items.map((item) => {
        item.id = `radio-${this.uid}-${item.value}`;
        return item;
      });
    }
  },
  watch: {
    modelValue(newValue, oldValue) {
      this.$emit("update:modelValue", newValue);
    },
    value(newValue, oldValue) {
      this.modelValue = newValue;
    },
  },
  created() {
    this.modelValue = this.value || null;
  }
};
</script>
