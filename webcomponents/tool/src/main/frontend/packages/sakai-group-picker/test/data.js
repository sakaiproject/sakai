export const i18nUrl = /getI18nProperties.*group-picker$/;

export const i18n = `
group_selector_label=Group selector
any=Any
site=Site
`;

export const label = "Group selector";

export const siteId = "xyz";

export const groupsUrl = `/direct/site/${siteId}/groups.json`;

export const footballRef = `/site/${siteId}/groups/football`;
export const tennisRef = `/site/${siteId}/groups/tennis`;
export const snookerRef = `/site/${siteId}/groups/snooker`;

export const groups = [
  { reference: tennisRef, title: "Tennis" },
  { reference: footballRef, title: "Football" },
  { reference: snookerRef, title: "Snooker" },
];

export const selectedGroups = [ tennisRef, snookerRef ];
