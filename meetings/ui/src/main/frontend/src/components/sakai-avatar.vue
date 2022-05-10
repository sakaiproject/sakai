<template>
  <div class="avatar">
    <img
      v-show="variant == 'image'"
      v-on:load="onImageLoad"
      :src="imageUrl"
      :width="size"
      :height="size"
      :style="imageStyle"
      :alt="altText"
    />
    <div v-if="variant == 'placeholder'" :style="placeholderStyle">
      <sakai-icon iconkey="file_image"></sakai-icon>
    </div>
    <div v-if="variant == 'text'" :style="placeholderStyle">
      <span>{{ text }}</span>
    </div>
  </div>
</template>

<script>
import sakaiIcon from "./sakai-icon.vue";
export default {
  components: { sakaiIcon },
  data() {
    return {
      loaded: false,
      brokenBackgroundColor: "var(--sakai-background-color-3)",
      brokenColor: "var(--sakai-text-color-1)",
      placeholderBackgroundColor: "var(--button-primary-background)",
      placeholderColor: "var(--button-primary-text-color)",
      sqareRadius: "0%",
      dark: false,
    };
  },
  props: {
    size: { type: Number, default: 100 },
    form: {
      type: String,
      default: "circle",
    },
    userId: {
      type: String,
    },
    userName: { type: String },
    siteId: { type: String, default: undefined },
    offical: { type: Boolean, default: false },
    text: { type: String, default: undefined },
  },
  methods: {
    onImageLoad: function () {
      this.loaded = true;
    },
  },
  computed: {
    variant() {
      if (this.text) {
        return "text";
      } else if (this.loaded) {
        return "image";
      } else {
        return "placeholder";
      }
    },
    borderRadius() {
      return (
        "border-radius:" +
        (this.form === "square" ? this.sqareRadius : "50%") +
        ";"
      );
    },
    imageStyle() {
      var style = this.borderRadius;
      if (this.dark) {
        style += "filter:brightness(75%);";
      }
      return style;
    },
    fontSize() {
      if (this.variant == "placeholder") {
        return this.size / 2.5;
      } else if (this.text.length <= 3) {
        return this.size / 2;
      } else {
        return this.size / (this.text.length * (2 / 3));
      }
    },
    placeholderStyle() {
      var style = this.borderRadius;
      if (this.variant === "text") {
        style += "background-color:" + this.placeholderBackgroundColor + ";";
        style += "color:" + this.placeholderColor + ";";
      } else {
        style += "background-color:" + this.brokenBackgroundColor + ";";
        style += "color:" + this.brokenColor + ";";
      }
      style += "width:" + this.size + "px;";
      style += "height:" + this.size + "px;";
      style +=
        "display: flex;flex-direction: column;justify-content: center; text-align: center;";
      style += "font-size:" + this.fontSize + "px;";
      return style;
    },
    imageUrl() {
      var url = window.location.protocol + "//" + window.location.host;
      url += "/direct/profile/";
      url += this.userId + "/image";
      url += this.offical
        ? "/official"
        : this.size <= 80
        ? "/avatar"
        : this.size <= 100
        ? "/thumb"
        : "/default";
      if (this.siteId) {
        url += "?siteId=" + this.siteId;
      }
      return url;
    },
    altText() {
      if (this.userName) {
        return "Profile Image of " + this.userName;
      } else {
        return "Profile Image";
      }
    },
  },
};
</script>
