import * as courseCardData from "../../sakai-course-card/test/data.js";

export { i18nUrl, i18n } from "./i18n.js";

export const userId = "adrian";

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
