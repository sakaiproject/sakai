export const userId = "xyz";
export const courselistI18nUrl = "/sakai-ws/rest/i18n/getI18nProperties?locale=en_GB&resourceclass=org.sakaiproject.i18n.InternationalizedMessages&resourcebundle=courselist";
export const courselistI18n = `
view_all_sites=View All Sites
favourites=Favourites
all_projects=All Projects
all_courses=All Courses
new_activity=New Activity
title_a_to_z=Title: A-Z
title_z_to_a=Title: Z-A
code_a_to_z=Code: A-Z
code_z_to_a=Code: Z-A
`;
export const courseListUrl = `/api/users/${userId}/sites`;
export const courseList = { sites: [], terms: [] };
