export const withQueryParam = (endpoint, name, value) => {

  if (!endpoint) {
    return endpoint;
  }

  const url = new URL(endpoint, window.location.href);
  if (value) {
    url.searchParams.set(name, value);
  } else {
    url.searchParams.delete(name);
  }
  return `${url.pathname}${url.search}`;
};
