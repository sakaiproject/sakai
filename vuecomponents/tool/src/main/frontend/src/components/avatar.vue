<template>
  <div
    class="avatar"
    :class="{
      'avatar-legacy-border': legacyborder,
      'avatar-editable': editable,
    }"
  >
    <img
      v-show="variant === 'image'"
      @load="onImageLoad"
      :src="imageUrl"
      :width="size"
      :height="size"
      :style="imageStyle"
      :alt="altText"
    />
    <div v-if="variant === 'text'" :style="textStyle">
      <span>{{ text }}</span>
    </div>
  </div>
</template>

<style scoped>
.avatar {
  display: flex;
  align-items: center;
}
.avatar-editable:hover::after {
  content: "\f040";
  background: var(--sakai-background-color-3);
  position: absolute;
  text-align: center;
  padding: 0 0 4px;
  box-shadow: -1px 0 3px var(--sakai-background-color-3);
  border-radius: 50%;
  font-family: FontAwesome;
  top: 0px;
  bottom: 0px;
  left: 0px;
  right: 0px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  opacity: 0.85;
}
.avatar-legacy-border.avatar-editable:hover::after {
  top: 2px;
  bottom: 2px;
  left: 2px;
  right: 2px;
}
.avatar-legacy-border img {
  border: 2px solid var(--top-header-profile-border-color-top);
  border-right-color: var(--top-header-profile-border-color-right);
  border-bottom-color: var(--top-header-profile-border-color-bottom);
  border-left-color: var(--top-header-profile-border-color-left);
  position: relative;
}
</style>

<script>
import i18nMixin from "../mixins/i18n-mixin.js";

export default {
  name: "avatar",
  mixins: [i18nMixin],
  data() {
    return {
      loaded: false,
      sqareRadius: "0%",
      dark: false,
      updateString: "cachebusting",
      i18n: {
        avatar_image_alt_of_user: "avatar_image_alt_of_user",
        avatar_image_alt_no_user: "avatar_image_alt_no_user",
      },
    };
  },
  props: {
    size: { type: Number, default: 100 },
    form: {
      type: String,
      default: "circle",
    },
    userid: { type: String },
    username: { type: String },
    siteid: { type: String, default: undefined },
    official: { type: Boolean, default: false },
    legacyborder: { type: Boolean, default: false },
    editable: { type: Boolean, default: false },
    cachebusting: { type: Boolean, default: false },
    nodim: { type: Boolean, default: false },
    text: { type: String, default: " " },
  },
  methods: {
    onImageLoad() {
      this.loaded = true;
    },
    forceReload() {
      this.updateString = Math.floor(Math.random() * (10000 - 99999) + 99999);
    },
    checkTheme() {
      this.dark = document.documentElement.classList.contains(
        "sakaiUserTheme-dark"
      );
    },
  },
  computed: {
    variant() {
      if (this.loaded) {
        return "image";
      } else {
        return "text";
      }
    },
    borderRadius() {
      return this.form === "square" ? this.sqareRadius : "50%";
    },
    imageStyle() {
      return `
        border-radius: ${this.borderRadius};
        ${this.dark ? "filter: brightness(85%)" : ""};
      `;
    },
    fontSize() {
      if (this.text.length <= 3) {
        return this.size / 2;
      } else {
        return this.size / (this.text.length * (2 / 3));
      }
    },
    textStyle() {
      return `
        border-radius: ${this.borderRadius};
        ${
          this.variant === "text"
            ? `
          background-color: var(--button-primary-background);
          color: var(--button-primary-text-color);
        `
            : `
          background-color: var(--sakai-background-color-3);
          color: var(--sakai-text-color-1);
        `
        }
        width: ${this.size}px;
        height: ${this.size}px;
        font-size: ${this.fontSize}px;
        display: flex;
        flex-direction: column;
        justify-content: center;
        text-align: center;
      `;
    },
    imageUrl() {
      if (!this.userid) { return undefined };
      let url = window.location.protocol + "//" + window.location.host;
      url += "/direct/profile/";
      url += this.userid + "/image";
      url += this.official
        ? "/official"
        : this.size <= 80
        ? "/avatar"
        : this.size <= 100
        ? "/thumb"
        : "/default";
      if (this.siteid) {
        url += "?siteid=" + this.siteid;
      }
      if (this.cachebusting) {
        url += "?update=" + this.updateString;
      }
      return url;
    },
    altText() {
      if (this.username) {
        return this.i18n.avatar_image_alt_of_user + " " + this.username;
      } else {
        return this.i18n.avatar_image_alt_no_user;
      }
    },
  },
  created() {
    //Listen for a prifile picture change and append a cachebusting string when it happens
    if (this.cachebusting) {
      this.forceReload();
      document.body.addEventListener("avatar-changed", (e) => {
        this.forceReload();
      });
    }
    //Listen for a theme change. On dark theme img will be dimmed
    if (!this.nodim) {
      this.checkTheme();
      document.body.addEventListener("theme-changed", (e) => {
        this.checkTheme();
      });
    }
  },
};
</script>
