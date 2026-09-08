export const withQueryParam = (endpoint, name, value) => {

  if (!endpoint) {
    return endpoint;
  }

  const url = new URL(endpoint, window.location.href);
  const parts = [];
  const search = url.search.startsWith("?") ? url.search.slice(1) : url.search;
  if (search) {
    for (const part of search.split("&")) {
      if (!part) {
        continue;
      }
      const key = decodeURIComponent(part.split("=")[0].replaceAll("+", " "));
      if (key !== name) {
        parts.push(part);
      }
    }
  }
  if (value !== undefined && value !== null && value !== "") {
    parts.push(`${encodeURIComponent(name)}=${encodeURIComponent(value)}`);
  }
  return parts.length ? `${url.pathname}?${parts.join("&")}` : url.pathname;
};
