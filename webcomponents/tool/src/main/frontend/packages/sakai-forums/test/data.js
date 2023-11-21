export const i18nUrl = /.*i18n.*resourcebundle=forums/;

export const i18n = `
syn_options=Options
syn_hide=Don't Show
sort_by_messages_tooltip=Sort by message number
syn_private_heading=New Messages
sort_by_forums_tooltip=Sort by number of forum posts
syn_discussion_heading=New in Discussions
sort_by_site_tooltip=Sort by site title
syn_site_heading=Site
syn_hide_tooltip=Hide this site from this forums/messages view
`;

export const userId = "adrian";
export const siteUrl = "/sites/1";

export const userForumsUrl= `/api/users/${userId}/forums`;

export const userForums = [
  { messageUrl: "/forums/1/2", forumUrl: "/forums/1", forumCount: 2, messageCount: 3, siteUrl, siteTitle: "A" },
  { messageUrl: "/forums/2/2", forumUrl: "/forums/2", forumCount: 8, messageCount: 5, siteUrl, siteTitle: "Z" },
];

export const siteId = "xyz";

export const siteForumsUrl= `/api/sites/${siteId}/forums`;

export const siteForums = [
  { messageUrl: "/forums/1/2", forumUrl: "/forums/1", forumCount: 2, messageCount: 3, siteUrl, siteTitle: "A" },
  { messageUrl: "/forums/2/2", forumUrl: "/forums/2", forumCount: 8, messageCount: 5, siteUrl, siteTitle: "Z" },
];
