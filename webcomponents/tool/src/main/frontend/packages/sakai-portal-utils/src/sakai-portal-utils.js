export const getUserId = () => globalThis.portal?.user?.id || "";
export const getSiteId = () => globalThis.portal?.siteId || "";
export const getUserLocale = () => (globalThis.portal?.locale || globalThis.sakai?.locale?.userLocale || "en-US").replace("_", "-");
export const getOffsetFromServerMillis = () => globalThis.portal?.user.offsetFromServerMillis || 0;
export const getTimezone = () => {
  // Embedded tools have their own window; Sakai's portal context lives in an ancestor.
  let context = globalThis;
  while (context) {
    try {
      const timezone = context.portal?.user?.timezone;
      if (timezone) return timezone;
      if (context.parent === context) break;
      context = context.parent;
    } catch (error) {
      // A Sakai portal may itself be embedded in a page from another origin.
      if (error.name !== "SecurityError") throw error;
      break;
    }
  }
  return "";
};
export const getServiceName = () => globalThis.portal?.serviceName || "Sakai";
export const setupSearch = options => globalThis.portal?.search?.setup(options);
