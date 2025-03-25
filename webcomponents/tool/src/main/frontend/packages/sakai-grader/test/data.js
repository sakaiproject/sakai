export { i18n, i18nUrl, filePickerI18n, filePickerI18nUrl } from "./i18n.js";

export { closeTime, gradableData } from "./gradable.js";

export const siteId = "site1";
export const siteTitle = "Site 1";

export const gradableType = "Topic";
export const gradableRef = "abcd1234";
const gradingItem1 = { id: 1, name: "Football", points: 50, externalId: gradableRef };
const gradingItem2 = { id: 2, name: "Tennis", points: 80 };
export const gradingItemDataWithoutCategories = { items: [ gradingItem1, gradingItem2 ] };
export const gradingItemDataUrl = `/api/sites/${siteId}/grading/item-data`;
export const gradingItemDataWithCategories = { items: [ gradingItem1, gradingItem2 ], categories: [ { id: 1, name: "foods" }, { id: 2, name: "sports" } ] };
