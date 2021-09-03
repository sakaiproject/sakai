import { topic1 } from "./topic1.js";

export const topicListData = `
[
  ${topic1},
  {
    "id": "topic2",
    "siteId": "playpen",
    "aboutReference": "/site/playpen",
    "title": "Are Einstein-Rosen bridges wormholes?",
    "message": "Are wormholes, as they are protrayed in science fiction, Einstein-Rosen bridges? Answers on a postcard.",
    "numberOfPosts": 0,
    "creator": "arthur",
    "created": 1619694887566,
    "creatorDisplayName": "Arthur Dent",
    "canEdit": true,
    "canDelete": true,
    "canPost": true,
    "canPin": true,
    "canModerate": true,
    "canReact": true,
    "isInstructor": true,
    "links": [
      { "href": "/api/sites/playpen/topics/topic2/pinned", "rel": "pin" },
      { "href": "/api/sites/playpen/topics/topic2/bookmarked", "rel": "bookmark" },
      { "href": "/api/sites/playpen/topics/topic2/reactions", "rel": "react" },
      { "href": "/api/sites/playpen/topics/topic2/reactions", "rel": "hide" },
      { "href": "/api/sites/playpen/topics/topic2/reactions", "rel": "lock" }
    ],
    "myReactions": {
      "LOVE_IT": true,
      "GOOD_QUESTION": true,
      "GOOD_IDEA": true,
      "KEY": true
    },
    "reactionTotals": {
      "LOVE_IT": 3,
      "GOOD_QUESTION": 1,
      "GOOD_IDEA": 1,
      "KEY": 1
    },
    "pinned": false,
    "read": false,
    "resolved": false,
    "tags": [
      {
        "id": 1,
        "label": "pheasant"
      },
      {
        "id": 4,
        "label": "bigbird"
      },
      {
        "id": 2,
        "label": "chicken"
      },
      {
        "id": 3,
        "label": "turkey"
      }
    ],
    "type": "QUESTION",
    "visibility": "SITE"
  },
  {
    "id": "topic3",
    "siteId": "playpen",
    "aboutReference": "/site/playpen",
    "title": "Dirty fries. Discuss.",
    "message": "Dirty fries. Is a little bit of something always good for you?",
    "numberOfPosts": 2,
    "creator": "theophilus",
    "created": 1619694887566,
    "creatorDisplayName": "Theophilus P Wildebeest",
    "canEdit": true,
    "canDelete": true,
    "canPost": true,
    "canPin": true,
    "canModerate": true,
    "canReact": true,
    "links": [
      { "href": "/api/sites/playpen/topics/topic2/pinned", "rel": "pin" },
      { "href": "/api/sites/playpen/topics/topic2/bookmarked", "rel": "bookmark" },
      { "href": "/api/sites/playpen/topics/topic2/reactions", "rel": "react" },
      { "href": "/api/sites/playpen/topics/topic2/reactions", "rel": "hide" },
      { "href": "/api/sites/playpen/topics/topic2/reactions", "rel": "lock" }
    ],
    "myReactions": {
      "LOVE_IT": true,
      "GOOD_QUESTION": true,
      "GOOD_IDEA": true,
      "KEY": true
    },
    "reactionTotals": {
      "LOVE_IT": 3,
      "GOOD_QUESTION": 1,
      "GOOD_IDEA": 1,
      "KEY": 1
    },
    "pinned": true,
    "read": false,
    "resolved": true,
    "tags": [
      {
        "id": 1,
        "label": "pheasant"
      },
      {
        "id": 4,
        "label": "bigbird"
      }
    ],
    "type": "QUESTION",
    "visibility": "SITE"
  },
  {
    "id": "topic4",
    "siteId": "playpen",
    "aboutReference": "/site/playpen",
    "title": "Topic 4",
    "draft": true,
    "message": "Topic 4",
    "numberOfPosts": 0,
    "created": 1619694887566,
    "creator": "mike",
    "creatorDisplayName": "Michael Greene",
    "canEdit": true,
    "canDelete": true,
    "canPost": true,
    "canPin": true,
    "canModerate": true,
    "canReact": true,
    "links": [
      { "href": "/api/sites/playpen/topics/topic2/pinned", "rel": "pin" },
      { "href": "/api/sites/playpen/topics/topic2/bookmarked", "rel": "bookmark" },
      { "href": "/api/sites/playpen/topics/topic2/reactions", "rel": "react" },
      { "href": "/api/sites/playpen/topics/topic2/reactions", "rel": "hide" },
      { "href": "/api/sites/playpen/topics/topic2/reactions", "rel": "lock" }
    ],
    "myReactions": {
      "LOVE_IT": true,
      "GOOD_QUESTION": true,
      "GOOD_IDEA": true,
      "KEY": true
    },
    "reactionTotals": {
      "LOVE_IT": 3,
      "GOOD_QUESTION": 1,
      "GOOD_IDEA": 1,
      "KEY": 1
    },
    "read": false,
    "bookmarked": false,
    "resolved": false,
    "tags": [
      {
        "id": 4,
        "label": "bigbird"
      }
    ],
    "type": "QUESTION",
    "visibility": "SITE"
  }
]
`;
