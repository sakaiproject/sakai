function getViewPreferences(tool) {

  let url = `/direct/userPrefs/key/${portal.user.id}/viewpreferences.json`;

  return new Promise((resolve, reject) => {

    fetch(url, { cache: "no-cache", headers: { "Content-Type": "application/json" },})
    .then(d => d.json())
    .then(json => {

      var preferencesString = decodeURIComponent(json.data[tool]);
      if (preferencesString && preferencesString !== "undefined") {
        resolve(preferencesString);
      } else {
        resolve(null);
      }
    })
    .catch(error => { console.error(error); resolve(null); });
  });
}

function updateViewPreferences(tool, value) {

  let url = `/direct/userPrefs/updateKey/${portal.user.id}/viewpreferences?${tool}=${encodeURIComponent(value)}`;
  fetch(url, { method: "PUT", cache: "no-cache" })
  .catch(error => console.error(
                    `Failed to update view preferences for tool '${tool}'. Take a look at the server logs?`));
}

// Until Chrome Edge comes out, we will be pulling this in old school. Later though ...
//export {getViewPreferences, updateViewPreferences};
