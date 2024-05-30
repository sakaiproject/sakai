export const userId = "adrian";

export const i18nUrl = "/sakai-ws/rest/i18n/getI18nProperties?locale=en_GB&resourceclass=org.sakaiproject.i18n.InternationalizedMessages&resourcebundle=search";

export const i18n = `
close_results_tooltip=Close the search results
from_site=from site
no_results=No Results
search_result_title=Title:
search_results=Search Results
search_sakai_placeholder=Search all Sakai
search_this_tool_placeholder=Search this tool
search_tooltip=Search your content
search_placeholder=Enter search term to search Sakai
search_min_length=Please enter at least {} characters
site_label=Site: 
toolname_announcement=Announcement
toolname_assignment=Assignment
toolname_chat=Chat
toolname_commons=Commons
toolname_conversations=Conversations
toolname_forum=Forum
toolname_lesson=Lesson
toolname_resources=Resources
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
