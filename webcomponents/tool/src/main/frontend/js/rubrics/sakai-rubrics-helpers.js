export class SakaiRubricsHelpers {

  static handleErrors(response) {

    if (!response.ok) {
      console.log(`Error : ${  response.statusText || response.status}`);
      throw Error((response.statusText || response.status));
    }
    return response;
  }

  static get(baseUrl, token, extraOptions) {

    let url = baseUrl;
    if (extraOptions.params) {
      const usp = new URLSearchParams();
      Object.entries(extraOptions.params).forEach(([k, v]) => usp.append(k, v));
      url += `?${usp.toString()}`;
    }

    const options = {
      method: "GET",
      headers: {
        "Authorization": token,
        "Accept": "application/json",
        "Content-Type": "application/json",
      }
    };

    Object.assign(options.headers, extraOptions.extraHeaders);

    return fetch(url, options).then(SakaiRubricsHelpers.handleErrors).then(response => response.json());
  }

  static post(url, token, extraOptions) {

    const body
      = extraOptions.body ? Object.entries(extraOptions.body).reduce((acc, [k, v]) => acc.append(k, v), new FormData())
        : "{}";

    const options = {
      method: "POST",
      headers: {
        "Authorization": token,
        "Content-Type": "application/json",
      },
      body,
    };

    Object.assign(options.headers, extraOptions.extraHeaders);

    return fetch(url, options).then(SakaiRubricsHelpers.handleErrors).then(response => response.json());
  }

  static getUserDisplayName(sakaiSessionId, creatorId) {
    return fetch(`/sakai-ws/rest/sakai/getUserDisplayName?sessionid=${sakaiSessionId}&eid=${creatorId}`)
      .then( (response) => response.text() );
  }

  static getSiteTitle(sakaiSessionId, siteId) {
    return fetch(`/sakai-ws/rest/sakai/getSiteTitle?sessionid=${sakaiSessionId}&siteid=${siteId}`)
      .then( (response) => response.text());
  }

}
