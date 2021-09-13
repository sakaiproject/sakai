import { topicListData } from "./sakai-topic-list.js";

export const playpenData = `
{
  "siteId": "playpen",
  "canUpdatePermissions": true,
  "canCreateTopic": true,
  "canCreateTags": true,
  "showGuidelines": false,
  "canPin": true,
  "isInstructor": true,
  "canModerate": true,
  "canViewSiteStatistics": true,
  "links": [
    { "href": "/api/sites/playpen/conversations/stats", "rel": "stats" }
  ],
  "tags": [
    {
      "id": 1,
      "label": "pheasant"
    },
    {
      "id": 2,
      "label": "chicken"
    },
    {
      "id": 3,
      "label": "turkey"
    },
    {
      "id": 4,
      "label": "bigbird"
    }
  ],
  "groups": [
    {
      "id": "group1",
      "title": "Group 1"
    },
    {
      "id": "group2",
      "title": "Group 2"
    }
  ],
  "topics": ${topicListData},
  "settings": { 
    "allowReactions": true,
    "allowUpvoting": true,
    "allowPinning": true,
    "requireGuidelinesAgreement": true,
    "guidelines": "Be <strong>nice</strong> to each other. Play fair!"
  }
}
`;
