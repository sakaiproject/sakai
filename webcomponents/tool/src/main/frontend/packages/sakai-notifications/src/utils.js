export const markNotificationsViewed = (siteId, toolId) => {

  const url = `/api/users/me/notifications/markViewed${siteId && toolId ? `?siteId=${siteId}&toolId=${toolId}` : ""}`;
  return fetch(url, { method: "POST", credentials: "include" });
};
