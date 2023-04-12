<template>
  <div>
    <slot name="prepend" />
    <textarea
      :id="id"
      :disabled="disabled"
      class="form-control"
      :class="inputClasses"
      rows="10"
      v-model="value"
      @input="handleInput($event.target.value)"
    />
    <div v-if="hasValidation" class="invalid-feedback">
      {{ validationStatus.message }}
    </div>
    <slot name="append"></slot>
  </div>
</template>

<script>
import validationMixin from "../mixins/validation-mixin.js";

export default {
  mixins: [validationMixin],
  data() {
    return {
    };
  },
  props: {
    value: {
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
    maxlength: {
      type: Number
    },
  },
  computed: {
    inputClasses() {
      let classes = [];
      if (
        this.hasValidation &&
        !(this.validationStatus.skipped ||
        this.validationStatus.isValid)
      ) {
        classes.push("is-invalid");
      }
      return classes;
    },
  },
  methods: {
    handleInput(value) {
      //Emit update:value to work with v-model:value
      this.$emit("update:value", value);
      //If we are not validating yet, we will want to after the first input
      if (!this.hadInput && this.hasValidation) {
        this.hadInput = true;
      }
    },
  },
};
</script>

<style lang="scss">
#meetings-tool {
  textarea {
    &.form-control {
      color: var(--sakai-text-color-1) !important;
      border: 1px solid var(--sakai-border-color);
      &:focus {
        border: 1px solid var(--sakai-border-color);
      }
      &:disabled {
        color: var(--sakai-text-color-disabled);
      }
    }
  }
}
</style>
