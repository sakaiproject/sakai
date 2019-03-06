export class SakaiRubricsHelpers {

  static handleErrors(response) {

    if (!response.ok) {
      console.log("Error : " + (response.statusText || response.status));
      throw Error((response.statusText || response.status));
    }
    return response;
  }

  static get(url, token, contentType = "application/json") {

    var options = {
      method: "GET",
      headers: {
        "Authorization": token,
        "Accept": "application/json",
        "Content-Type": contentType,
      }
    };

    return fetch(url, options).then(SakaiRubricsHelpers.handleErrors).then(response => response.json());
  }

  static post(url, extraOptions) {

    var body
      = extraOptions.body ? Object.entries(extraOptions.body).reduce((acc, [k,v]) => acc.append(k,v), new FormData())
        : "{}";

    var options = {
      method: "POST",
      headers: { "Content-Type": "application/json", "Authorization": extraOptions.token },
      body: body,
    };

    Object.assign(options.headers, extraOptions.extraHeaders);

    return fetch(url, options).then(SakaiRubricsHelpers.handleErrors).then(response => response.json());
  }
}
