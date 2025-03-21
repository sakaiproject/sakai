export const siteId = "xyz";

export { i18nUrl, i18n, toolI18nUrl, toolI18n } from "./i18n.js";

export const permsUrl = /\/api\/sites.*permissions.*/;
export const perms = {
  available: [ "tool.read", "tool.create", "tool.delete" ],
  on: {
    "maintain": [ "tool.read", "tool.create", "tool.delete" ],
    "access": [ "tool.read" ],
  },
  roleNameMappings: { maintain: "Maintain", access: "Access" }
};
