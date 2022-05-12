<template>
  <div :class="[{ 'form-check': type == 'checkbox' }]">
    <slot name="prepend" />
    <input
      v-model="value"
      @input="handleInput($event.target.value)"
      :id="id"
      :name="name"
      :type="type"
      :role="type == 'search' ? 'search' : undefined"
      :disabled="disabled"
      :placeholder="placeholder"
      :aria-label="arialabel"
      :class="inputClasses"
      :required="required"
    />
    <label v-if="isCheckbox" class="form-check-label" :for="id">{{
      checklabel
    }}</label>
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
      type: [Number, String, Array],
      default: null,
    },
    id: {
      type: String,
      default: null,
    },
    type: {
      type: String,
      default: "text",
    },
    name: {
      type: String,
      default: "input",
    },
    disabled: {
      type: Boolean,
      default: false,
    },
    placeholder: {
      type: String,
      default: null,
    },
    min: {
      //for input number
      type: [String, Number],
      default: null,
    },
    arialabel: {
      type: String,
      default: null,
    },
    checklabel: {
      type: String,
      default: null,
    },
    required: {
      type: Boolean,
      default: false,
    },
  },
  computed: {
    isCheckbox() {
      return this.type === "checkbox";
    },
    inputClasses() {
      let classes = [];
      if (
        this.hasValidation &&
        !(this.validationStatus.skipped ||
        this.validationStatus.isValid)
      ) {
        classes.push("is-invalid");
      }
      if (this.type === "checkbox") {
        classes.push("form-check-input");
      } else {
        classes.push("form-control");
      }
      return classes.join(" ");
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
  .sakai-search {
    background: var(--sakai-background-color-1);
    border: 1px solid var(--sakai-border-color);
    border-radius: 5px;
    width: 100%;
  }
  .sakai-search .sakai-icon {
    color: green;
  }
  input, textarea {
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
  ::placeholder {
    color: var(--sakai-text-color-dimmed);
  }
  input[type="checkbox"] {
    appearance: none;
    height: 15px;
    width: 15px;
    border-radius: 3px;
    background-color: var(--sakai-background-color-1);
    border: 1px solid var(--sakai-border-color);
  }
  input[type="checkbox"]:checked {
    background-image: url("data:image/svg+xml;charset=utf8,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='2 2 12 12'%3E%3Cpath fill='white' d='M10.97 4.97a.75.75 0 0 1 1.07 1.05l-3.99 4.99a.75.75 0 0 1-1.08.02L4.324 8.384a.75.75 0 1 1 1.06-1.06l2.094 2.093 3.473-4.425a.267.267 0 0 1 .02-.022z'/%3E%3C/svg%3E");
    background-color: var(--sakai-color-blue--darker-3);
  }
  input[type="date"]::-webkit-calendar-picker-indicator,
  input[type="datetime-local"]::-webkit-calendar-picker-indicator {
    background-image: url("data:image/svg+xml;charset=utf8,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='-2 -2 20 20'%3E%3Cpath fill='grey' d='M3.5 0a.5.5 0 0 1 .5.5V1h8V.5a.5.5 0 0 1 1 0V1h1a2 2 0 0 1 2 2v11a2 2 0 0 1-2 2H2a2 2 0 0 1-2-2V3a2 2 0 0 1 2-2h1V.5a.5.5 0 0 1 .5-.5zM1 4v10a1 1 0 0 0 1 1h12a1 1 0 0 0 1-1V4H1z'/%3E%3C/svg%3E");
    cursor: pointer;
  }
  input[type="date"]::-webkit-calendar-picker-indicator:focus-visible,
  input[type="datetime-local"]::-webkit-calendar-picker-indicator:focus-visible {
    outline: 3px solid var(--focus-outline-color);
  }
  .search-icon {
    padding: 0 0 0 8px;
    align-self: center;
  }
  .icon-append {
    padding: 0 8px 0 0;
    align-self: center;
  }
  .form-check {
    .form-check-input[disabled]~.form-check-label,
    .form-check-input:disabled~.form-check-label {
        opacity: unset;
        color: var(--sakai-text-color-disabled);
    }
  }
}
</style>
