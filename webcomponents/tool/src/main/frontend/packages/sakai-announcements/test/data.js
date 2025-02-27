export { i18n, i18nUrl } from "./i18n.js";

export const userId = "adrian";
export const siteId = "xyz";
export const siteTitle = "XYZ Site";

export const vavavoom = "Vavavoom";
export const vavavoomSite = "Vavavoom Site";

export const announcementsUrl= `/api/users/me/announcements`;
export const siteAnnouncementsUrl= `/api/sites/${siteId}/announcements`;

export const announcements = [
  { subject: "Ears", url: "/annc/ears", visible: true, order: 3, siteTitle, siteId },
  { subject: "Chips", url: "/annc/chips", visible: true, order: 1, siteTitle, siteId },
  { subject: vavavoom, url: "/annc/vavavoom", visible: true, order: 2, siteTitle: vavavoomSite, siteId: vavavoom },
];

export const siteAnnouncements = [
  { subject: "Ears", url: "/annc/ears", siteTitle, siteId },
  { subject: "Chips", url: "/annc/chips", siteTitle, siteId },
];
