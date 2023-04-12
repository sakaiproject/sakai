<template>
  <button
    type="button"
    :title="textHidden ? text : undefined"
    :role="role"
    @click="handleClick"
    class="btn"
    :disabled="disabled"
    :class="{ clear, circle, link, 'btn-primary': primary }"
  >
    <slot name="prepend" />
    <span v-if="!textHidden && text.length > 0">{{ text }}</span>
    <slot name="append" />
  </button>
</template>

<script>
export default {
  components: {},
  props: {
    text: {
      type: String,
      default: "Button",
    },
    role: {
      type: String,
      default: "button",
    },
    clear: {
      type: Boolean,
      default: false,
    },
    circle: {
      type: Boolean,
      default: false,
    },
    link: {
      type: Boolean,
      default: false,
    },
    textHidden: {
      type: Boolean,
      default: false,
    },
    primary: {
      type: Boolean,
      default: false,
    },
    disabled: {
      type: Boolean,
      default: false,
    },
  },
  data() {
    return {
      showMenu: true,
    };
  },

  methods: {
    onClick() {
      this.showMenu = !this.showMenu;
    },
    handleClick() {
      
    },
  },
};
</script>

<style scoped lang="scss">
#meetings-tool {
.btn {
  background-color: var(--button-background);
  color: var(--button-text-color);
  border: 1px solid var(--button-border-color);
  padding: 0.3rem 0.7rem;
  height: 100%;
}
.btn:hover,
.btn:focus {
  background-color: var(--button-hover-background);
  color: var(--button-hover-text-color);
  border: 1px solid var(--button-hover-border-color);
}
.btn:focus {
  box-shadow: none;
  outline: 3px solid var(--focus-outline-color);
}
.btn:active {
  background-color: var(--button-active-background);
  border: 1px solid var(--button-active-border-color);
  box-shadow: none;
}
.btn-primary {
  background-color: var(--button-primary-background);
  color: var(--button-primary-text-color);
  border: 1px solid var(--button-primary-border-color);
}
.btn-primary:hover,
.btn-primary:focus {
  background-color: var(--button-primary-hover-background);
  color: var(--button-primary-hover-text-color);
  border: 1px solid var(--button-primary-hover-border-color);
}
.btn-primary:active:focus {
  background-color: var(--button-primary-active-background);
  color: var(--button-primary-active-text-color);
  border: 1px solid var(--button-primary-active-border-color);
  box-shadow: none;
}
.btn:disabled,
.btn-primary:disabled {
  background-color: var(--button-disabled-background);
  color: var(--button-disabled-text-color);
  border: 1px solid var(--button-disabled-border-color);
}
.circle {
  border-radius: 50%;
  width: 2.5rem;
  height: 2.5rem;
  font-size: 1.5rem;
  padding: 0;
}
.clear,
.link,
.link:hover,
.link:focus {
  background-color: transparent;
  color: var(--link-color);
  border: 1px solid transparent;
}
.link {
  padding: 0 0 0.2rem 0;
}
.link:hover,
.link:focus {
  text-decoration: underline;
}
.link:active {
  color: var(--link-active-color) !important;
}
}
</style>
