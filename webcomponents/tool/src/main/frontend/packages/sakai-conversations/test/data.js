export { i18n, i18nUrl } from "./i18n.js";
import  * as constants from "../src/sakai-conversations-constants.js";

export const siteId = "site1";
export const siteTitle = "Site 1";
export const gradingItemId = 1;
export const tags = [
  { id: 1, label: "eggs" },
  { id: 2, label: "sports" },
];

export const topicsUrl = `/api/sites/${siteId}/topics`;

export const links = [
  {
    rel: "posts",
    href: "/posts-url",
  },
  {
    rel: "bookmark",
    href: "/bookmark-url",
  },
  {
    rel: "lock",
    href: "/lock-url",
  },
  {
    rel: "hide",
    href: "/hide-url",
  },
  {
    rel: "pin",
    href: "/pin-url",
  },
  {
    rel: "delete",
    href: "/delete-url",
  },
  {
    rel: "markpostsviewed",
    href: "/markpostsviewed-url",
  },
];

export const blankTopic = {
  siteId,
  groups: [],
  visibility: constants.SITE,
  canModerate: true,
  url: topicsUrl,
  links,
};

export const discussionTopic = {
  ...blankTopic,
  id: "topic1",
	creator: "fisha",
  creatorDisplayName: "Adrian Fish",
  formattedCreatedDate: "12 Sep 2017 14:12",
  type: constants.DISCUSSION,
  title: "Test Discussion",
  message: "This is a test discussion",
  tags,
  links,
  canModerate: true,
  canEdit: true,
  canDelete: true,
  canViewStatistics: true,
};

export const questionTopic = {
  ...blankTopic,
  id: "question1",
  creatorDisplayName: "Adrian Fish",
  formattedCreatedDate: "12 Sep 2017 14:12",
  type: constants.QUESTION,
  title: "Test Question",
  message: "This is a test question",
  links,
};

export const anonymousTopic = {
  ...blankTopic,
  id: "anonymous1",
  creator: "fisha",
  creatorDisplayName: "Anonymous",
  formattedCreatedDate: "12 Sep 2017 14:12",
  type: constants.DISCUSSION,
  title: "Anonymous Discussion",
  message: "This is an anonymous discussion",
  anonymous: true,
  tags,
  links,
  canModerate: true,
  canEdit: true,
  canDelete: true,
  canViewStatistics: true,
};

export const groups = [
  {
    reference: "/groups/1",
    title: "Group 1",
  },
  {
    reference: "/groups/2",
    title: "Group 2",
  }
];

export const data = {
  tags,
  blankTopic,
  canCreateTopic: true,
  canCreateQuestion: true,
  canCreateDiscussion: true,
  settings: {
    allowAnonPosting: true,
  },
  topics: [],
  siteId,
};

export const topic1 = {
  id: "topic1",
  type: constants.QUESTION,
};

export const answer = {
  id: "answer1",
  creatorDisplayName: "Adrian Fish",
  formattedCreatedDate: "11 Oct 2013 15:12",
  message: "Space is the place",
  links: [
    {
      rel: "self",
      href: "answer1_url",
    }
  ],
};

export const post1 = {
  id: "post1",
  canViewUpvotes: true,
  canUpvote: true,
  creator: "fisha",
  creatorDisplayName: "Adrian Fish",
  formattedCreatedDate: "15 Oct 2017 18:12",
  message: "Moonage Daydream",
  links: [
    {
      rel: "self",
      href: "post1_url",
    },
    {
      rel: "reply",
      href: "post1_reply_url",
    },
    {
      rel: "delete",
      href: "post1_delete_url",
    },
    {
      rel: "hide",
      href: "post1_hide_url",
    },
    {
      rel: "lock",
      href: "post1_lock_url",
    },
    {
      rel: "react",
      href: "post1_react_url",
    },
    {
      rel: "upvote",
      href: "post1_upvote_url",
    },
    {
      rel: "unupvote",
      href: "post1_unupvote_url",
    }
  ],
  reactionTotals: {
    "THUMBS_UP": 3,
    "KEY": 2,
  },
  myReactions:  {
    "THUMBS_UP": true,
    "KEY": false,
    "LOVE_IT": false,
    "GOOD_IDEA": false,
  },
  upvotes: 3,
};

export const thread = {
  id: "thread1",
  creatorDisplayName: "Adrian Fish",
  formattedCreatedDate: "15 Oct 2017 18:12",
  message: "Favourite tunes?",
  isThread: true,
  links: [
    {
      rel: "self",
      href: "post1_url",
    }
  ],
  posts: [
    {
      id: "post1",
      depth: 2,
      creatorDisplayName: "Adrian Fish",
      formattedCreatedDate: "16 Oct 2017 18:12",
      message: "Stairway to Heaven",
    },
    {
      id: "post2",
      depth: 2,
      creatorDisplayName: "Bruce Fish",
      formattedCreatedDate: "17 Oct 2017 18:12",
      message: "Bohemian Rhapsody",
    },
  ],
};

export const commentCreatorId = "user1";
export const commentCreatorDisplayName = "User 1";
export const formattedCommentCreatedDate = "12 Oct 2013 13:46";
export const commentMessage = "Nice post";

export const comment = {
  id: "comment1",
  postId: post1.id,
  creator: commentCreatorId,
  creatorDisplayName: commentCreatorDisplayName,
  formattedCreatedDate: formattedCommentCreatedDate,
  message: commentMessage,
};
