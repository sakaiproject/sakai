import { formatDistance } from "date-fns";
import { ar, bg, ca, enGB, enUS, es, eu, faIR, fr, hi, ja, mn, ptBR, ro, sv, tr, zhCN } from "date-fns/locale";
import { getUserLocale } from "@sakai-ui/sakai-portal-utils";

const locales = { ar, bg, ca, enGB, enUS, es, eu, faIR, fr, hi, ja, mn, ptBR, ro, sv, tr, zhCN };

export const sakaiFormatDistance = (to, from) => {

  const options = { addSuffix: true, lang: getUserLocale(), locale: locales[enUS] };

  if (options.lang) {

    if (options.lang.includes("-")) {
      const parts = options.lang.split("-");
      // Try the langcountry key, but fallback to just the language if that doesn't exist
      options.locale = locales[parts.join("")] || locales[parts[0]];
    } else {
      options.locale = locales[options.lang];
    }
  }

  if (!options.locale) {
    console.warn(`No locale found for lang ${options.lang}. Using en-US ...`);
    options.locale = locales[enUS];
  }

  return formatDistance(to, from, options);
};
