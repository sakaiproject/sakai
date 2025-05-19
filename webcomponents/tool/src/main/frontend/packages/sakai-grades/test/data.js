export const i18nUrl = /getI18nProperties.*grades/;

export const i18n = `
assignment=Assignment
no_grades=No grades to view
sort_ungraded_least_to_most=Ungraded: Least first
sort_ungraded_most_to_least=Ungraded: Most first
sort_score_low_to_high=Score: Lowest first
sort_score_high_to_low=Score: Highest first
sort_assignment_a_to_z=Assignment: A-Z
sort_assignment_z_to_a=Assignment: Z-A
sort_course_a_to_z=Course: A-Z
sort_course_z_to_a=Course: Z-A
sort_tooltip=Sort the grades
score=Score
score_reveal_msg=Reveal scores by clicking on them
course_score=(avg)
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
  { name: "Infinite Set Theory", ungraded: 3, score: "47.3", notGradedYet: false, url: "/grades/inf", siteTitle: "Natural Sciences 101", canGrade: true },
  { name: "Bayesian Inference", ungraded: 6, score: "12.4", notGradedYet: false, url: "/grades/bayes", siteTitle: "Probablity 101", canGrade: true },
  { name: "Euler Series", ungraded: 8, score: "23.7", notGradedYet: false, url: "/grades/euler", siteTitle: "French 101", canGrade: true },
  { name: "Fast Fourier Transform", ungraded: 0, score: "85", notGradedYet: false, url: "/grades/fft", siteTitle: "Math 101" },
];
