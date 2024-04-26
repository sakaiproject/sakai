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

  thread.viewed = !thread.posts ? true : !flattenPosts(thread).filter(p => !p.viewed).length;
  thread.expanded = !thread.viewed;
};

export const debounce = (func, timeout = 300) => {

  let timer;
  return (...args) => {

    clearTimeout(timer);
    timer = setTimeout(() => { func.apply(this, args); }, timeout);
  };
};
