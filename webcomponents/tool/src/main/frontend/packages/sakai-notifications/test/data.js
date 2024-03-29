export const i18nUrl = /getI18nProperties.*sakai-notifications$/;

export const i18n = `
academic_comment_graded=Commented on your post, or on a post you commented on, in "{0}"
annc=Announcements
announcement=Added a new announcement "{0}" in "{1}"
asn=Assignments
assignment_created=Created/updated a new assignment "{0}" in "{1}"
assignment_submission_graded=Graded your submission for assignment "{0}" in "{1}"
clear_all=Clear All
commons=Commons
connection_request_received=You received a connection request from {0}
connection_request_accepted={0} accepted your connection request
hide=Hide
mark_all_viewed=Mark all as viewed
message=Private Messages
message_read=Has read the message "{0}" from the site "{1}"
message_received={0} sent you a message
motd=Message of the Day
no_notifications=No notifications
notifications_not_allowed=You need to accept notifications from your Sakai server.
notifications_not_allowed2=If you don't, you will not get any notifications regarding \
announcements, assignments, etc.
notifications_denied=Notifications have been denied at some point and you will need to use your \
browser settings to reset the permission.
profile=Social
show=Show
samigoCreated=Created/updated new assessment "{0}" in "{1}"
sam=Tests&Quizzes
`;

export const notificationsUrl = `/users/me/notifications`;
export const notifications = [
  { event: "assn.new", fromUser: "adrian", fromDisplayName: "Adrian Fish", formattedEventDate: "12 Feb, 2021", id: "noti1", title: "Bugs", url: "http://bogus.com/bugs" },
  { event: "annc.new", fromUser: "earle", fromDisplayName: "Earle Nietzel", formattedEventDate: "17 March, 2021", id: "noti2", title: "Worms", url: "http://bogus.com/worms" },
  { event: "profile.friend.request", fromUser: "earle", fromDisplayName: "Earle Nietzel", formattedEventDate: "27 November, 2021", id: "noti3", title: "Friend Me", url: "http://bogus.com/friend" },
  { event: "profile.friend.confirm", fromUser: "adrian", fromDisplayName: "Adrian Fish", formattedEventDate: "29 November, 2021", id: "noti4", title: "Friended", url: "http://bogus.com/friended" },
  { event: "profile.message.sent", fromUser: "adrian", fromDisplayName: "Adrian Fish", formattedEventDate: "30 November, 2021", id: "noti5", title: "Message", url: "http://bogus.com/friended" },
];
