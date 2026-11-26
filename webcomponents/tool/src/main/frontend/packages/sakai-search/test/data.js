export const userId = "adrian";

export const i18nUrl = /getI18nProperties.*search/;

export const i18n = `
close_results_tooltip=Close the search results
from_site=from site
no_results=No Results
result_unavailable=1 item matched the search terms but is not available (the item may have been deleted or moved, or you may not have permission to view it)
results_unavailable=Matching items are not available.
search=Search
search_result_title=Title:
search_results=Search Results
search_sakai_placeholder=Search all Sakai
search_this_tool_placeholder=Search this tool
search_tooltip=Search your content
search_placeholder=Enter search term to search Sakai
search_min_length=Please enter at least {} characters
site_label=Site: 
all_pinned_sites=All Pinned Sites
all_tools=All Tools
site_filter_label=Filter results by site
this_site=This site
tool_filter_label=Filter results by tool
toolname_announcement=Announcement
toolname_assignment=Assignment
toolname_calendar=Calendar
toolname_chat=Chat
toolname_commons=Commons
toolname_conversations=Conversations
toolname_forum=Forum
toolname_lesson=Lesson
toolname_polls=Polls
toolname_resources=Resources
toolname_rubrics=Rubrics
toolname_syllabus=Syllabus
toolname_wiki=Wiki
`;

export const terms = "eggs";
export const siteId = "potatoes101";
export const siteTitle = "Potatoes 101";
export const tool = "conversations";

export const searchUrl = `/api/search?terms=${terms}${siteId ? `&site=${siteId}` : ""}${tool ? `&tool=${tool}` : ""}`;
export const searchResults = [
  { title: "Chips", url: "http://blah.com/chips", tool, siteTitle },
  { title: "Eggs", url: "http://blah.com/eggs", tool, siteTitle },
];

export const sitesUrl = "/api/users/current/sites";
export const sites = { sites: [
  { siteId, title: siteTitle, pinned: true },
  { siteId: "carrots101", title: "Carrots 101", pinned: true },
  { siteId: "turnips101", title: "Turnips 101", pinned: false },
] };

