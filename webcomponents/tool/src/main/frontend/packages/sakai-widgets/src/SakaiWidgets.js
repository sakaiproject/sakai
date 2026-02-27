export const sakaiWidgets = {

  getIds: () => [ "announcements", "calendar", "courses", "forums", "grades", "tasks" ],
  getWidgets: () => {

    return [
      {
        id: "announcements",
        roles: [ "instructor", "student" ],
        tag: "sakai-announcements-widget",
      },
      {
        id: "calendar",
        roles: [ "instructor", "student" ],
        tag: "sakai-calendar-widget",
      },
      {
        id: "courses",
        roles: [ "instructor", "student" ],
        tag: "sakai-courses-widget",
      },
      {
        id: "forums",
        roles: [ "instructor", "student" ],
        tag: "sakai-forums-widget",
      },
      { id: "grades",
        roles: [ "instructor" ],
        tag: "sakai-grades-widget",
      },
      {
        id: "tasks",
        roles: [ "instructor", "student" ],
        tag: "sakai-tasks-widget",
      },
    ];
  },
};
