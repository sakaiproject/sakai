<template>
  <div>
    <input
      ref="field"
      type="text"
      :id="id"
      :disabled="disabled"
      autocomplete="off"
      :required="required"
      :aria-label="arialabel || checklabel"
      :class="inputClasses"
      @change="onChange"
    />
    <div v-if="hasValidation" class="invalid-feedback">
      {{ validationStatus.message }}
    </div>
  </div>
</template>

<script>
import validationMixin from "../mixins/validation-mixin.js";
import {
  formatForDatetimeLocalControl,
  formatPickerInitial,
  parseControlValueToIso,
} from "../resources/portal-dayjs.js";

export default {
  name: "SakaiLangDatepickerInput",
  mixins: [validationMixin],
  props: {
    modelValue: {
      type: String,
      default: null,
    },
    id: {
      type: String,
      default: null,
    },
    disabled: {
      type: Boolean,
      default: false,
    },
    required: {
      type: Boolean,
      default: false,
    },
    arialabel: {
      type: String,
      default: null,
    },
    checklabel: {
      type: String,
      default: null,
    },
  },
  emits: ["update:modelValue", "update:value", "validation"],
  computed: {
    value() {
      return this.modelValue == null ? "" : String(this.modelValue);
    },
    inputClasses() {
      const classes = ["form-control"];
      if (
        this.hasValidation &&
        !(this.validationStatus.skipped || this.validationStatus.isValid)
      ) {
        classes.push("is-invalid");
      }
      return classes.join(" ");
    },
  },
  watch: {
    modelValue() {
      this.syncControlFromModel();
    },
  },
  mounted() {
    this.$nextTick(() => this.initPicker());
  },
  methods: {
    syncControlFromModel() {
      const el = this.$refs.field;
      if (!el || !this.pickerReady) {
        return;
      }
      const formatted = formatForDatetimeLocalControl(this.modelValue);
      if (el.value !== formatted) {
        el.value = formatted;
      }
    },
    initPicker() {
      const el = this.$refs.field;
      if (!el) {
        return;
      }
      const val = formatPickerInitial(this.modelValue);
      if (typeof window.localDatePicker === "function") {
        window.localDatePicker({
          input: el,
          useTime: 1,
          val,
          ashidden: {},
        });
      } else {
        el.type = "datetime-local";
        el.value = formatForDatetimeLocalControl(this.modelValue);
      }
      el.step = "60";
      this.pickerReady = true;
    },
    onChange() {
      const el = this.$refs.field;
      if (!el) {
        return;
      }
      const out = parseControlValueToIso(el.value);
      this.$emit("update:modelValue", out);
      this.$emit("update:value", out);
      if (!this.hadInput && this.hasValidation) {
        this.hadInput = true;
      }
    },
  },
  data() {
    return {
      pickerReady: false,
    };
  },
};
</script>
