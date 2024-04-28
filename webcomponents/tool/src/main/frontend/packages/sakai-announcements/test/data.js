export const i18nUrl = "/sakai-ws/rest/i18n/getI18nProperties?locale=en_GB&resourceclass=org.sakaiproject.i18n.InternationalizedMessages&resourcebundle=announcements";

export const i18n = `
viewing=(viewing announcements from the last 10 days)
site=Site
search=Search
title=Title
site=Site
site_tooltip=Filter by site
view=View
sort_by_title_tooltip=Sort by title
sort_by_site_tooltip=Sort by title
widget_title=Announcements
url_tooltip=Click to be taken to the announcement
`;

export const userId = "adrian";
export const siteId = "xyz";
export const siteTitle = "XYZ Site";

export const vavavoom = "Vavavoom";
export const vavavoomSite = "Vavavoom Site";

export const announcementsUrl= `/api/users/me/announcements`;
export const siteAnnouncementsUrl= `/api/sites/${siteId}/announcements`;

export const announcements = [
  { subject: "Ears", url: "/annc/ears", siteTitle, siteId },
  { subject: "Chips", url: "/annc/chips", siteTitle, siteId },
  { subject: vavavoom, url: "/annc/vavavoom", siteTitle: vavavoomSite, siteId: vavavoom },
];

export const siteAnnouncements = [
  { subject: "Ears", url: "/annc/ears", siteTitle, siteId },
  { subject: "Chips", url: "/annc/chips", siteTitle, siteId },
];
