import { GRADE_CHECKED,
          LETTER_GRADE_TYPE,
          SCORE_GRADE_TYPE,
          PASS_FAIL_GRADE_TYPE,
          CHECK_GRADE_TYPE } from "../src/sakai-grader-constants.js";

import { closeTime, firstSubmission, firstSubmissionId, gradableData, gradableId } from "./gradable.js";
import { i18n } from "./i18n.js";

export const i18nUrl = /getI18nProperties.*grader$/;

export { closeTime, firstSubmission, firstSubmissionId, i18n, gradableData, gradableId };

export const filePickerI18nUrl = /getI18nProperties.*file-picker/;

export const filePickerI18n = `
remove=Remove
to_be_added=To be added:
`;

export const textSubmissionId = "submission1";
export const siteId = "site1";
export const siteTitle = "Site 1";
export const submittedText = "My submission";
export const textAttachmentUrl = "http://text.com";

export const fullSubmissionsUrl = `/direct/assignment/fullSubmissions.json?gradableId=${gradableId}&submissionIds=${firstSubmissionId}`;
/*
export const textSubmission = {
  id: "submission1",
  hydrated: true,
  dateSubmitted: "7 Feb 1971",
  properties: {
    allow_resubmit_number: 0,
  },
  resubmitsAllowed: -1,
  submitted: true,
  submitters: [
    {
      sortName: "Fish, Adrian",
      displayId: "fisha",
    },
  ],
  //submittedAttachments: [ textAttachment ],
  submittedText,
};
*/


export const gradesData = {
  students: [],
  grades: [],
};

/*
this._submission.originalityShowing = true;
this._submission.originalityServiceName = "Turnitin";
const supplies = [
  {
    "1": "http://www.balls.com",
    "4": "balls",
  }
];
this._submission.originalitySupplies = supplies;
*/
