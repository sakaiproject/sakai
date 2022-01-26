export const getUserId = () => window.top?.portal?.user?.id || "";
export const getUserLocale = () => (window.top?.portal?.locale
              || window.top?.sakai?.locale?.userLocale || "en-US").replace("_", "-");
