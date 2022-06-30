export const getUserId = () => window.top?.portal?.user?.id || "";

export const getUserLocale = () => (window.top?.portal?.locale
              || window.top?.sakai?.locale?.userLocale || "en-US").replace("_", "-");

export const getOffsetFromServerMillis = () => window.top?.portal?.user.offsetFromServerMillis || 0;
