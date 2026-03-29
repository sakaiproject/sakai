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
import dayjs from "dayjs";
import validationMixin from "../mixins/validation-mixin.js";

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
    toPickerValString(val) {
      if (!val) {
        return dayjs().format("YYYY-MM-DD HH:mm");
      }
      const d = dayjs(val);
      return d.isValid() ? d.format("YYYY-MM-DD HH:mm") : dayjs().format("YYYY-MM-DD HH:mm");
    },
    syncControlFromModel() {
      const el = this.$refs.field;
      if (!el || !this.pickerReady) {
        return;
      }
      const d = dayjs(this.modelValue);
      if (!d.isValid()) {
        return;
      }
      const formatted = d.format("YYYY-MM-DDTHH:mm");
      if (el.value !== formatted) {
        el.value = formatted;
      }
    },
    initPicker() {
      const el = this.$refs.field;
      if (!el) {
        return;
      }
      const val = this.toPickerValString(this.modelValue);
      if (typeof window.localDatePicker === "function") {
        window.localDatePicker({
          input: el,
          useTime: 1,
          val,
          ashidden: {},
        });
      } else {
        el.type = "datetime-local";
        el.value = dayjs(val.replace(" ", "T")).isValid()
          ? dayjs(val.replace(" ", "T")).format("YYYY-MM-DDTHH:mm")
          : dayjs().format("YYYY-MM-DDTHH:mm");
      }
      el.step = "60";
      this.pickerReady = true;
    },
    onChange() {
      const el = this.$refs.field;
      if (!el) {
        return;
      }
      const raw = el.value;
      const parsed = dayjs(raw);
      const out = parsed.isValid()
        ? parsed.format("YYYY-MM-DDTHH:mm") + ":00"
        : "";
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
