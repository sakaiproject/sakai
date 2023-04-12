<template>
  <select
    class="form-select"
    v-model="value"
    @change="$emit('update:value', this.value)"
    :name="name"
    :id="id"
    :disabled="disabled ? 'disabled' : undefined"
    :multiple="multiple"
  >
    <option v-for="item in items" :key="item.value" :value="item.value">
      {{ item.string }}
    </option>
  </select>
</template>

<script>
//import "/node_modules/bootstrap/js/dist/dropdown.js";
export default {
  components: {},
  props: {
    id: {
      type: String,
      default: null,
    },
    items: {
      type: Array,
      default: [],
    },
    name: {
      type: String,
      default: "select",
    },
    modelValue: {
      type: [String, Number, Array],
      default: [],
    },
    disabled: {
      type: Boolean,
      default: false
    },
    multiple: {
      type: Boolean,
      default: false
    }
  },
  emits: ['update:modelValue'],
  computed: {
    value: {
      get() {
        return this.modelValue
      },
      set(value) {
        this.$emit('update:modelValue', value)
      }
    }
  }
};
</script>

<style scoped lang="scss">
#meetings-tool {
  select {
    &.form-select {
      color: var(--sakai-text-color-1);
      background-color: var(--sakai-background-color-1);
    border: 1px solid var(--sakai-border-color);
      &:disabled {
        color: var(--sakai-text-color-disabled);
      }
      &:not([multiple]) {
        background-image: url("data:image/svg+xml;charset=utf8,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 16 16'%3E%3Cpath fill='grey' d='M2 5l6 6 6-6'/%3E%3C/svg%3E");
      }
    }
  }
}
</style>
