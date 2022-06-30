import { post3 } from "./post3.js";

export const post2 = `
{
  "id": "post2",
  "creator": "theophilus",
  "created": 1619694887566,
  "creatorDisplayName": "Theophilus P Wildebeest",
  "formattedCreatedDate": "1 day ago",
  "message": "No, they're bad.",
  "canUpvote": true,
  "links": [
    { "href": "/api/sites/playpen/topics/topic3/posts/post2/hidden", "rel": "hidden" },
    { "href": "/api/sites/playpen/topics/topic3/posts/post2/locked", "rel": "locked" },
    { "href": "/api/sites/playpen/topics/topic3/posts/post2/reactions", "rel": "reactions" },
    { "href": "/api/sites/playpen/topics/topic3/posts", "rel": "reply" }
  ],
  "myReactions": {
    "LOVE_IT": true,
    "GOOD_QUESTION": false,
    "GOOD_ANSWER": false,
    "GOOD_COMMENT": false,
    "GOOD_IDEA": true,
    "KEY": true
  },
  "reactionTotals": {
    "LOVE_IT": 3,
    "GOOD_QUESTION": 0,
    "GOOD_ANSWER": 2,
    "GOOD_COMMENT": 0,
    "GOOD_IDEA": 1,
    "KEY": 1
  },
  "canEdit": true,
  "canDelete": true,
  "canView": true,
  "canComment": true,
  "canVerify": true,
  "verified": false,
  "posts": [
    ${post3}
  ],
  "topic": "topic3",
  "numberOfComments": 0
}
`;
