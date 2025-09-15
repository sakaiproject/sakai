export { i18n, i18nUrl } from "./i18n.js";

export const userId = "adrian";

export const profileUrl = `/api/users/${userId}/profile`;
export const emptyProfile = {
  eid: userId,
  type: "Instructor",
  canEdit: true,
  disabled: false,
  creatorDisplayName: "Bob Carolgees",
  modifierDisplayName: "Wilson Pickett",
  formattedCreatedDate: "1 Jan 2020",
  formattedModifiedDate: "2 Jan 2025",
};
