//For usage information, go the tutorial at vuecomponents/docs/i18n.md
import { loadProperties } from "../../../../../../../webcomponents/tool/src/main/frontend/packages/sakai-i18n/src/sakai-i18n.js";

export default {
  methods: {
    getI18nProps(bundleName) {
      loadProperties(bundleName)
        .then((response) => {
          this.i18nObj = response;
          this.i18n = new Proxy(this.i18nObj, {
            get(i18nObj, key) {
              if (key?.startsWith("__")) {
                return i18nObj[key];
              } else {
                const translation = i18nObj[key];

                if (translation) {
                  return translation;
                } else {
                  console.error(`No translation for key '${key}' in bundle '${bundleName}'`);
                  return key;
                }
              }
            },
          });
        })
        .catch((reason) => {
          console.error("I18n strings could not be retrieved -", reason);
        });
    },
    insert(translation, ...inserts) {
      inserts?.forEach((insert, index) => translation = translation?.replace(`{${index}}`, insert));

      return translation;
    }
  },
  created() {
    this.getI18nProps(this.i18nBundleName || this.$options.name);
  },
  data() {
    return { i18n: {}, i18nObj: {} };
  },
};
