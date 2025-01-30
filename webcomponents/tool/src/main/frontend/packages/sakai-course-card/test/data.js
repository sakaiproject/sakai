export const userId = "xyz";
export const i18nUrl = /getI18nProperties.*coursecard/;
export const i18n = `
options_menu_tooltip=Click to see options for this course
select_tools_to_display=Select tools to display:
favourite_this_course=Favourite this course?
assignments_tooltip=Click to view your assignments for this course
gradebook_tooltip=Click to view the gradebook for this course
forums_tooltip=Click to view the forums for this course
visit=Visit
`;
export const toolnameMappingsUrl = "/sakai-ws/rest/i18n/getI18nProperties?locale=en_GB&resourceclass=org.sakaiproject.i18n.InternationalizedMessages&resourcebundle=toolname-mappings";
export const toolnameMappings = `
assignments=Assignments
gradebook=Gradebook
forums=Discussions
`;

export const course1 = { id: "sports_101", title: "Sports Science 101", course: true, tools: [] };
export const course2 = { id: "perm_found", title: "Permaculture Foundations", course: true, tools: [] };
export const course3 = { id: "eggs", title: "Eggs", course: true, tools: [] };
