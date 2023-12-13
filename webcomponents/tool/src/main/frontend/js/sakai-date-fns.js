import { formatDistance } from "./assets/date-fns/esm/index.js";
import { ar, bg, ca, enGB, enUS, es, eu, ro, tr } from "./assets/date-fns/esm/locale/index.js";
import { getUserLocale } from "./sakai-portal-utils.js";

const locales = { ar, bg, ca, enGB, enUS, es, eu, ro, tr };

export const sakaiFormatDistance = (to, from) => {

  const options = { addSuffix: true, lang: getUserLocale() };

  if (options.lang) {
    if (options.lang.includes("-")) {
      const parts = options.lang.split("-");
      // Try the langcountry key, but fallback to just the language if that doesn't exist
      options.locale = locales[parts.join("")] || locales[parts[0]];
    } else {
      options.locale = locales[options.lang];
    }
  } else {
    options.locale = locales[enUS];
  }

  return formatDistance(to, from, options);
};
