<template>
  <div class="recording">
    <a :href="url" target="_blank" rel="noreferrer noopener">
      <sakai-icon iconkey="fileVideo"></sakai-icon>
      <div class="text-center text-break">{{ name }}</div>
    </a>
  </div>
</template>

<script>
import sakaiIcon from "./sakai-icon.vue";
export default {
  components: { sakaiIcon },
  data() {
    return {};
  },
  props: {
    id: { type: String, default: null },
    name: { type: String, default: null },
    url: { type: String, default: null },
  }
};
</script>

<style scoped lang="scss">
  #meetings-tool {
    .recording {
      padding: 5px;
      border: thin solid var(--sakai-border-color);
      border-radius: 5px;
      height: 100%;

      &:hover,
      &:focus {
        background-color: var(--tool-menu-item-hover-background-color);
        cursor: pointer;
      }

      a {
        color: var(--tool-menu-item-text-color);
        text-decoration: none;
        display: flex;
        flex-direction: column;
        align-items: center;

        .sakai-icon {
          font-size: 60px;
        }
      }
    }
  }
</style>
