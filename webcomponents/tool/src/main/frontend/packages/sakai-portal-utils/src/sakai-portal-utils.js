export const getUserId = () => window.top?.portal?.user?.id || "";
export const getUserLocale = () => (window.top?.portal?.locale || window.top?.sakai?.locale?.userLocale || "en-US").replace("_", "-");
export const getOffsetFromServerMillis = () => window.top?.portal?.user.offsetFromServerMillis || 0;
export const getTimezone = () => window.top?.portal?.user.timezone || "";
export const setupSearch = options => window.top?.portal?.search?.setup(options);
export const callSubscribeIfPermitted = function () { return window.top?.portal?.notifications?.callSubscribeIfPermitted(); };
export const callSubscribe = function () { return window.top?.portal?.notifications?.callSubscribe(); };
export const clearAppBadge = () => window.top?.portal?.notifications?.clearAppBadge();
export const setAppBadge = number => window.top?.portal?.notifications?.setAppBadge(number);
