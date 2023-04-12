<template>
  <div
    class="sakai-icon"
    aria-hidden="true"
    :style="color ? 'color:' + color : undefined"
  >
    <i :class="iconClass"></i>
  </div>
</template>

<style scoped lang="scss">
#meetings-tool {
  .sakai-icon {
    display: inline-block;
  }
}
</style>

<script>
import icons from "../resources/icons.js";

export default {
  props: {
    iconkey: {
      Type: String,
      default: "error",
      validator(value) {
        return Object.prototype.hasOwnProperty.call(icons, value);
      },
    },
    color: {
      Type: String,
      default: null,
    },
  },
  computed: {
    iconClass() {
      return icons[this.iconkey];
    },
  },
};
</script>
