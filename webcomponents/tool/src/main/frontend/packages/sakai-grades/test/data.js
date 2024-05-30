export const i18nUrl = "/sakai-ws/rest/i18n/getI18nProperties?locale=en_GB&resourceclass=org.sakaiproject.i18n.InternationalizedMessages&resourcebundle=grades";

export const i18n = `
assignment=Assignment
sort_new_low_to_high=New: Lowest first
sort_new_high_to_low=New: Highest first
sort_average_low_to_high=Average: Lowest first
sort_average_high_to_low=Average: Highest first
sort_assignment_a_to_z=Assignment: A-Z
sort_assignment_z_to_a=Assignment: Z-A
sort_course_a_to_z=Course: A-Z
sort_course_z_to_a=Course: Z-A
sort_tooltip=Sort the grades
course_average=avg
score=Score
submissions=submissions
new_submissions=ungraded item(s)
view=View
widget_title=Grades
url_tooltip=Click to be taken to the grades
course_assignment=Course/Assignment
`;

export const userId = "adrian";
export const siteTitle = "XYZ Site";

export const vavavoom = "Vavavoom";
export const vavavoomSite = "Vavavoom Site";

export const gradesUrl= `/api/users/me/grades`;

export const grades = [
  { name: "Infinite Set Theory", ungraded: 3, score: "47.3", notGradedYet: false, url: "/grades/inf", siteTitle: "Natural Sciences 101", siteRole: "Instructor" },
  { name: "Bayesian Inference", ungraded: 6, score: "12.4", notGradedYet: false, url: "/grades/bayes", siteTitle: "Probablity 101", siteRole: "Instructor" },
  { name: "Euler Series", ungraded: 8, score: "23.7", notGradedYet: false, url: "/grades/euler", siteTitle: "French 101", siteRole: "Instructor" },
  { name: "Fast Fourier Transform", ungraded: 0, score: "85", notGradedYet: false, url: "/grades/fft", siteTitle: "Math 101", siteRole: "Student" },
];
