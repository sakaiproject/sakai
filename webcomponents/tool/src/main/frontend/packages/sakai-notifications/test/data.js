export const i18nUrl = /getI18nProperties.*sakai-notifications$/;

export const i18n = `
annc=Announcements
asn=Assignments
commons=Commons
profile=Social
mark_all_viewed=Mark all as viewed
clear_all=Clear All
no_notifications=No notifications
connection_request_received=You received a connection request from {0}
connection_request_accepted={0} accepted your connection request
message_received={0} sent you a message
assignment_created = Created/updated a new assignment "{0}" in "{1}"
assignment_submission_graded = Graded your submission for assignment "{0}" in "{1}"
announcement = Added a new announcement "{0}" in "{1}"
academic_comment_graded = Commented on your post, or on a post you commented on, in "{0}"
`;

export const notificationsUrl = `/users/me/notifications`;
export const notifications = [
  { event: "assn.new", fromUser: "adrian", fromDisplayName: "Adrian Fish", formattedEventDate: "12 Feb, 2021", id: "noti1", title: "Bugs", url: "http://bogus.com/bugs" },
  { event: "annc.new", fromUser: "earle", fromDisplayName: "Earle Nietzel", formattedEventDate: "17 March, 2021", id: "noti2", title: "Worms", url: "http://bogus.com/worms" },
  { event: "profile.friend.request", fromUser: "earle", fromDisplayName: "Earle Nietzel", formattedEventDate: "27 November, 2021", id: "noti3", title: "Friend Me", url: "http://bogus.com/friend" },
  { event: "profile.friend.confirm", fromUser: "adrian", fromDisplayName: "Adrian Fish", formattedEventDate: "29 November, 2021", id: "noti4", title: "Friended", url: "http://bogus.com/friended" },
  { event: "profile.message.sent", fromUser: "adrian", fromDisplayName: "Adrian Fish", formattedEventDate: "30 November, 2021", id: "noti5", title: "Message", url: "http://bogus.com/friended" },
];
