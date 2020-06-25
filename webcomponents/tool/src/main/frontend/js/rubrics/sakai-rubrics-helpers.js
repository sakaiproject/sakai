export class SakaiRubricsHelpers {

  static handleErrors(response) {

    if (!response.ok) {
      console.log("Error : " + (response.statusText || response.status));
      throw Error((response.statusText || response.status));
    }
    return response;
  }

  static get(url, token, extraOptions) {

    if (extraOptions.params) {
      var usp = new URLSearchParams();
      Object.entries(extraOptions.params).forEach(([k,v]) => usp.append(k,v));
      url += `?${usp.toString()}`;
    }

    var options = {
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

    var body
      = extraOptions.body ? Object.entries(extraOptions.body).reduce((acc, [k,v]) => acc.append(k,v), new FormData())
        : "{}";

    var options = {
      method: "POST",
      headers: {
        "Authorization": token,
        "Content-Type": "application/json",
      },
      body: body,
    };

    Object.assign(options.headers, extraOptions.extraHeaders);

    return fetch(url, options).then(SakaiRubricsHelpers.handleErrors).then(response => response.json());
  }
}
