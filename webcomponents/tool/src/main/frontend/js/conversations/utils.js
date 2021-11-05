const flattenPosts = startPosts => {

  const flattenAll = (posts = []) => {
    return posts.flatMap(p => [ p, ...flattenPosts(p.posts) ]);
  };

  return flattenAll(startPosts);
};

export const findPost = (topic, options = {}) => {

  const flattened = flattenPosts(topic.continued && !options.postsInView ? topic.allPosts : topic.posts);

  if (options.postId) {
    return flattened.find(p => p.id === options.postId);
  } else if (options.isInstructor) {
    return flattened.filter(p => p.isInstructor === options.isInstructor).length;
  }
};

export const markThreadViewed = thread => {

  if (!thread.posts) {
    thread.viewed = true;
  } else {
    const flattened = flattenPosts(thread);
    thread.viewed = !flattened.filter(p => !p.viewed).length === 0;
  }
  thread.expanded = !thread.viewed;
};

export const debounce = (func, timeout = 300) => {

  let timer;
  return (...args) => {

    clearTimeout(timer);
    timer = setTimeout(() => { func.apply(this, args); }, timeout);
  };
};
