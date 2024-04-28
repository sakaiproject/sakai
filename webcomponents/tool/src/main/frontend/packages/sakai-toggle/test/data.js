export const i18nUrl = "/sakai-ws/rest/i18n/getI18nProperties?locale=en_GB&resourceclass=org.sakaiproject.i18n.InternationalizedMessages&resourcebundle=submission-messager";

export const i18n = `
select_action=Who do you want to message?
select_group=Only students in a group?
subject=Subject:
subject_placeholder=Type your subject here ...
message=Message:
ungraded_students=Ungraded students
graded_students=Graded students
all_students=All students
send=Send
show_recipients=Show Recipients
error=Failed to send messages.
success=Messages sent!
min_score_label=Min score:
max_score_label=Max score:
more_options=More Options
less_options=Less Options
more_options_tooltip=Show or hide extra options
recipients=Recipients
`;

export const siteId = "xyx";
export const assignmentId = "xyx101";
export const title = "XYX Assignment";
export const selectedGroup = "bears";
export const subject = "Submit!";
export const minScore = "55";
export const maxScore = "75";
export const body = "You need to submit this assignment, you schmuck";
export const recipients = [ 
  { displayName: "Englebert Humperdinck" },
  { displayName: "Flash Gordon" },
  { displayName: "Omar Sharif" },
];

export const groupPickerI18nUrl = "/sakai-ws/rest/i18n/getI18nProperties?locale=en_GB&resourceclass=org.sakaiproject.i18n.InternationalizedMessages&resourcebundle=group-picker";
export const groupPickerI18n = "group_selector_label=Groups";
export const groupsUrl = `/direct/site/${siteId}/groups.json`;
export const groups = [
  { reference: `/site/${siteId}/groups/tennis`, title: "Tennis" },
  { reference: `/site/${siteId}/groups/football`, title: "Football" },
];
