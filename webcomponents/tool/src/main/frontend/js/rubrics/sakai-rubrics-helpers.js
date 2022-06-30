export class SakaiRubricsHelpers {

  static handleErrors(response) {

    if (!response.ok) {
      console.error(`Error : ${  response.statusText || response.status}`);
      throw Error((response.statusText || response.status));
    }
    return response;
  }

  static get(baseUrl, extraOptions) {

    let url = baseUrl;
    if (extraOptions.params) {
      const usp = new URLSearchParams();
      Object.entries(extraOptions.params).forEach(([k, v]) => usp.append(k, v));
      url += `?${usp.toString()}`;
    }

    const options = {
      method: "GET",
      credentials: "include",
      headers: {
        "Accept": "application/json",
        "Content-Type": "application/json",
      }
    };

    Object.assign(options.headers, extraOptions.extraHeaders);

    return fetch(url, options).then(SakaiRubricsHelpers.handleErrors).then(response => response.json());
  }

  static post(url, extraOptions) {

    const body
      = extraOptions.body ? Object.entries(extraOptions.body).reduce((acc, [k, v]) => acc.append(k, v), new FormData())
        : "{}";

    const options = {
      method: "POST",
      credentials: "include",
      headers: { "Content-Type": "application/json" },
      body,
    };

    Object.assign(options.headers, extraOptions.extraHeaders);

    return fetch(url, options).then(SakaiRubricsHelpers.handleErrors).then(response => response.json());
  }
}
