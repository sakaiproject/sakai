export { i18n, i18nUrl } from "./i18n.js";
export { i18n as courseListI18n, i18nUrl as courseListI18nUrl } from "../../sakai-course-list/test/i18n.js";
export { baseI18n as dialogContentI18n, baseI18nUrl as dialogContentI18nUrl } from "../../sakai-dialog-content/test/i18n.js";
export { i18n as widgetPanelI18n, i18nUrl as widgetPanelI18nUrl } from "../../sakai-widgets/test/i18n.js";
export { i18n as courseCardI18n, i18nUrl as courseCardI18nUrl } from "../../sakai-course-card/test/i18n.js";
export { courseListUrl, courseList } from "../../sakai-course-list/test/data.js";

export const userId = "adrian";
export const siteId = "xyz";

export const dashboardUrl = `/api/users/${userId}/dashboard`;
export const dashboardData = {
  title: "XYZ Course",
  programme: "XYZ Programme",
  overview: "XYZ Overview",
  widgets: ["widget1", "widget2", "widget3"],
  widgetLayout: [
    ["widget1", "widget2", "widget3"],
  ],
  template: 1,
  image: "/images/xyz.png",
};
