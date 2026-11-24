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
no_discussions=No discussions
widget_title=Discussions
save_filter=Save filter
reset_filter=Reset filter
close_filters=Hide filters
sort_label=Sort discussions
sort_none=Sort
sort_messages_most_to_least=Messages: Most to least
sort_messages_least_to_most=Messages: Least to most
sort_forums_most_to_least=Discussions: Most to least
sort_forums_least_to_most=Discussions: Least to most
sort_site_a_to_z=Site: A to Z
sort_site_z_to_a=Site: Z to A
`;

export const userId = "adrian";
export const siteUrl = "/sites/1";

export const userForumsUrl= `/api/users/current/forums/summary`;

export const userForums = [
  { messageUrl: "/forums/1/2", forumUrl: "/forums/1", forumCount: 2, messageCount: 3, siteUrl, siteTitle: "A" },
  { messageUrl: "/forums/2/2", forumUrl: "/forums/2", forumCount: 8, messageCount: 5, siteUrl, siteTitle: "Z" },
];

export const siteId = "xyz";

export const siteForumsUrl= `/api/sites/${siteId}/forums/summary`;

export const siteForums = [
  { messageUrl: "/forums/1/2", forumUrl: "/forums/1", forumCount: 2, messageCount: 3, siteUrl, siteTitle: "A" },
  { messageUrl: "/forums/2/2", forumUrl: "/forums/2", forumCount: 8, messageCount: 5, siteUrl, siteTitle: "Z" },
];
