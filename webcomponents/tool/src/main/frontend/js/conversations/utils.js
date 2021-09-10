export const findPost = (topic, options) => {

  const transform1 = (reply) =>
    [ reply, ...transformAll(reply) ];

  const transformAll = (postable) => {

    if (!postable.posts) postable.posts = [];
    return postable.posts.flatMap(r => transform1(r));
  };

  const flattened = transformAll(topic);

  if (options.postId) {
    return flattened.find(p => p.id === options.postId);
  } else if (options.isInstructor) {
    return flattened.filter(p => p.isInstructor === options.isInstructor).length;
  }
};

export const getPostsForTopic = (topic) => {

  const url = `/api/sites/${topic.siteId}/topics/${topic.id}/posts`;
  return fetch(url, { credentials: "include" })
  .then(r => {
    
    if (!r.ok) {
      throw new Error(`Network error while retrieving  posts for topic ${topic.id}`);
    } else {
      return r.json();
    }
  })
  .catch(error => console.error(error));
};

export const debounce = (func, timeout = 300) => {

  let timer;
  return (...args) => {

    console.log("aadsas asdfasdf");

    clearTimeout(timer);
    timer = setTimeout(() => { func.apply(this, args); }, timeout);
  };
};
