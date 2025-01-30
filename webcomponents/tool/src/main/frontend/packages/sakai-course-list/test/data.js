import * as courseCardData from "../../sakai-course-card/test/data.js";

export const userId = "xyz";
export const i18nUrl = /getI18nProperties.*courselist/;
export const i18n = `
all_courses=All Courses
all_projects=All Projects
code_a_to_z=Code: A-Z
code_z_to_a=Code: Z-A
course_filter_label=Course filter
course_sort_label=Course sort order
favourites=Favourites
new_activity=New Activity
term=Term
term_filter_label=Term filter
term_filter_none_option=Select a term
title_a_to_z=Title: A-Z
title_z_to_a=Title: Z-A
view_all_sites=View All Sites
`;

const sites = [
   courseCardData.course1,
   courseCardData.course2,
   courseCardData.course3,
];

const terms = [
  { id: "spring", name: "Spring Term" },
  { id: "summer", name: "Summer Term" },
  { id: "michaelmas", name: "Michaelmas Term" },
];

courseCardData.course1.term = "Spring Term";
courseCardData.course2.term = "Summer Term";

export const courseListUrl = `/api/users/${userId}/sites?pinned=true`;
export const courseList = { sites, terms };
