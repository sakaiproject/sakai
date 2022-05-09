/* This component serves as a placeholder, because we need two components for a
bundle it will be replaced by the avatar component */
<template>
  <span class="hi">{{ i18n.helloworld }}</span>
</template>

<style scoped>
.hi {
  color: var(--sakai-text-color-3);
  background-color: var(--sakai-background-color-3);
  border: 2px solid var(--sakai-border-color-3);
  padding: 5px;
}
</style>

<script>
import i18nMixin from "../mixins/i18n-mixin.js";

export default {
  name: "placeholder",
  mixins: [i18nMixin],
  data() {
    return {
      i18nProps: "hello-world",
    };
  },
};
</script>
