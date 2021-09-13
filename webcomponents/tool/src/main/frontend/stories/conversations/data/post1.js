export const post1 = `
{
  "id": "post1",
  "creator": "arthur",
  "created": 1619694887566,
  "creatorDisplayName": "Arthur Dent",
  "message": "If you mean specifically dirty fries, then yes.",
  "upvotes": 5,
  "canUpvote": false,
  "canReact": true,
  "canEdit": true,
  "canDelete": true,
  "canLock": true,
  "canHide": true,
  "canComment": true,
  "canView": true,
  "canVerify": true,
  "isInstructor": true,
  "verified": true,
  "upvoted": false,
  "topic": "topic3",
  "links": [
    { "href": "/api/sites/playpen/topics/topic3/posts/post1/hidden", "rel": "hidden" },
    { "href": "/api/sites/playpen/topics/topic3/posts/post1/locked", "rel": "locked" },
    { "href": "/api/sites/playpen/topics/topic3/posts/post1/reactions", "rel": "reactions" }
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
  "numberOfComments": 1,
  "comments": [
    {
      "id": "comment1",
      "post": "post1",
      "creator": "adrian",
      "created": 1619694887566,
      "canEdit": true,
      "canDelete": true,
      "creatorDisplayName": "Adrian Fish",
      "message": "What?"
    }
  ]
}
`;
