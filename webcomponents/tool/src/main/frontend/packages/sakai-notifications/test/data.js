export { i18nUrl as profileI18nUrl, i18n as profileI18n } from "../../sakai-profile/test/data.js";
export { i18nUrl, i18n} from "./i18n.js";

export const notificationsUrl = `/users/me/notifications`;
export const notifications = [
  { event: "assn.new", fromUser: "adrian", fromDisplayName: "Adrian Fish", formattedEventDate: "12 Feb, 2021", id: "noti1", title: "Bugs", url: "http://bogus.com/bugs" },
  { event: "annc.new", fromUser: "earle", fromDisplayName: "Earle Nietzel", formattedEventDate: "17 March, 2021", id: "noti2", title: "Worms", url: "http://bogus.com/worms" },
];
