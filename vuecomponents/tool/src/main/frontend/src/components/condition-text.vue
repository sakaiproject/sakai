<template>
  <div class="condition-text" v-html="formattedText"></div>
</template>

<script>
  // Mixins
  import i18nMixin from "../mixins/i18n-mixin.js";

  // Utils
  import {
    CONDITION_BUNDLE_NAME,
    formatConditionHtml,
  } from "../utils/condition-utils.js";

  export default {
    name: "condition-text",
    mixins: [ i18nMixin ],
    props: {
      item: { type: String },
      condition: { type: Object },
    },
    data() {
      return {
        i18nBundleName: CONDITION_BUNDLE_NAME,
      };
    },
    computed: {
      formattedText() {
        return formatConditionHtml(this.i18n, this.condition, this.item);
      },
    },
  };
</script>
