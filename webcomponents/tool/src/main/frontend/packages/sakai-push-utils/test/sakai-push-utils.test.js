import { setup } from '../src/sakai-push-utils.js';
//import { expect } from '@open-wc/testing';
//import fetchMock from "fetch-mock/esm/client";

describe("sakai-push-utils tests", () => {

  window.top.portal = { locale: 'en_GB' };

  /*
  fetchMock
    .get(data.i18nUrl, data.i18n, { overwriteRoutes: true })
    .get("*", 500, { overwriteRoutes: true });
    */

  it ("sets up uccessfully", async () => {
    setup.then(() => console.log("setup complete"));
  });
});
