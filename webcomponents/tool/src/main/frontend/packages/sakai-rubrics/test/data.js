export { i18nUrl, i18n } from "./i18n.js";

export const siteId = "xyz";
export const toolId = "sakai.samigo";
export const entityId = "entity1";
export const evaluatedItemId = "evaluatedItem1";

export const sharedRubricsUrl = "/api/rubrics/shared";
export const sharedRubrics = [
];

export const siteTitle = "XYZ Site";
export const userId = "adrian";

export const ownerId = userId;
export const creatorDisplayName = "User 1";
export const formattedModifiedDate = "7 Feb 1971";

export const criterion1 = {
  id: 1,
  title: "Space",
  description: "Is the place",
  ratings: [
    {
      id: 1,
      title: "Poor",
      description: "A poor performance",
      points: 1,
    },
    {
      id: 2,
      title: "Not bad",
      description: "A barely adequate performance",
      points: 2,
    },
  ],
  pointoverride: "1.2",
};

export const criteria1 = [
  criterion1,
  {
    id: 2,
    title: "Group 1",
    description: "Groups 1 group",
    ratings: [
    ],
  },
];

export const criteria2 = [
  {
    id: 3,
    title: "Ocean",
    description: "Is the graveyard of hubris",
    ratings: [
      {
        id: 3,
        title: "Poor",
        description: "A poor performance",
        points: 1,
      },
      {
        id: 4,
        title: "Crap",
        description: "Crapish",
        points: 2,
      },
    ],
    pointoverride: "1.4",
  },
  {
    id: 4,
    title: "Group 2",
    description: "Groups 2 group",
    ratings: [
    ],
  },
];

export const criteria3 = [
  {
    id: 5,
    title: "C1",
    description: "First criterion",
    ratings: [
      {
        id: 5,
        title: "Rating1",
        description: "First rating",
        points: 1,
      },
      {
        id: 6,
        title: "Rating2",
        description: "Second rating",
        points: 2,
      },
    ]
  },
  {
    id: 6,
    title: "C2",
    description: "Second criterion",
    ratings: [
      {
        id: 7,
        title: "Rating1",
        description: "First rating",
        points: 1,
      },
      {
        id: 8,
        title: "Rating2",
        description: "Second rating",
        points: 2,
      },
    ],
  },
];

export const rubric1 = {
  id: "1",
  title: "Rubric 1",
  ownerId,
  siteTitle,
  locked: true,
  creatorDisplayName,
  formattedModifiedDate,
  criteria: criteria1,
};

export const rubric2 = {
  id: "2",
  title: "Rubric 2",
  ownerId,
  siteTitle,
  creatorDisplayName,
  formattedModifiedDate,
  criteria: criteria2,
};

export const rubric3 = {
  id: "3",
  title: "Rubric 3",
  ownerId,
  siteTitle,
  creatorDisplayName,
  formattedModifiedDate,
  criteria: criteria1,
  locked: true
};

export const rubric4 = {
  id: "4",
  title: "Rubric 4",
  ownerId,
  siteTitle,
  creatorDisplayName,
  formattedModifiedDate,
  criteria: criteria3
};

export const evaluatedItemOwnerId = "fisha";

export const rubricsUrl = /api\/sites\/xyz\/rubrics[\?\w=]*$/;
export const rubrics = [ rubric1, rubric2 ];

export const rubric1Url = `/api/sites/${siteId}/rubrics/${rubric1.id}`;
export const rubric1OwnerUrl = `/api/sites/${ownerId}/rubrics/${rubric1.id}`;
export const rubric3OwnerUrl = `/api/sites/${ownerId}/rubrics/${rubric3.id}`;

export const associationUrl = `/api/sites/${siteId}/rubric-associations/tools/${toolId}/items/${entityId}`;

export const association = {
  rubricId: rubric1.id,
  siteId: siteId,
  parameters: {
    fineTunePoints: true,
  },
};

export const evaluationUrl = `/api/sites/${siteId}/rubric-evaluations/tools/${toolId}/items/${entityId}/evaluations/${evaluatedItemId}/owners/${evaluatedItemOwnerId}`;

export const evaluation = {
  criterionOutcomes: [
    { criterionId: 1, selectedRatingId: 2, comments: "Rubbish", points: 2 }
  ],
};

export const rubric4OwnerUrl = `/api/sites/${ownerId}/rubrics/${rubric4.id}`;
export const rubric4CriteriaSortUrl = `/api/sites/${ownerId}/rubrics/${rubric4.id}/criteria/sort`;
export const rubric4Criteria5Url = `/api/sites/${ownerId}/rubrics/${rubric4.id}/criteria/5`;
export const rubric4Criteria6Url = `/api/sites/${ownerId}/rubrics/${rubric4.id}/criteria/6`;
