export const i18nUrl = /getI18nProperties.*site-picker$/;

export const i18n = `
site_selector_label=Site selector
all_pinned_sites=All
`;

export const siteOneId = "site1";
export const siteOneTitle = "Site One";
export const siteTwoId = "site2";
export const siteTwoTitle = "Site Two";
export const siteThreeId = "site3";
export const siteThreeTitle = "Site Threee";

export const userId = "user1";

export const sitesUrl = `/api/users/${userId}/sites?pinned=true`;

export const sites = [
  { siteId: siteOneId, title: siteOneTitle },
  { siteId: siteTwoId, title: siteTwoTitle },
  { siteId: siteThreeId, title: siteThreeTitle },
];

export const selectedSites = [ siteOneId, siteThreeId ];
