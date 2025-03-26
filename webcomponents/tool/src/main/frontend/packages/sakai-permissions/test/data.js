export const siteId = "xyz";

export { i18nUrl, i18n, toolI18nUrl, toolI18n } from "./i18n.js";

export const groups = [
  { id: "group1", title: "Group 1", reference: "/groups/group1" },
  { id: "group2", title: "Group 2", reference: "/groups/group2" },
  { id: "group3", title: "Group 3", reference: "/groups/group3" },
];

export const permsUrl = `/api/sites/${siteId}/permissions/tool?ref=/site/${siteId}`;
export const perms = {
  available: [ "tool.read", "tool.create", "tool.delete" ],
  on: {
    "maintain": [ "tool.read", "tool.create", "tool.delete" ],
    "access": [ "tool.read" ],
  },
  roleNameMappings: { maintain: "Maintain", access: "Access" },
  groups,
};
