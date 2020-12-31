import { expect } from "chai";
import { JSDOM } from "jsdom";
import fetchMock from "fetch-mock";
import { loadProperties } from "../js/sakai-i18n";

describe("Sakai i18n", () => {

  beforeEach(() => {
    const dom = new JSDOM(
      `<html>
         <body>
         </body>
       </html>`,
       { url: 'http://localhost' },
    );

    global.window = dom.window;
    global.document = dom.window.document;
  });

  afterEach(() => {

    window.sessionStorage.clear();
    window.sakai = undefined;
  });

  describe("loadProperties", async () => {


    const i18nUrl = "/sakai-ws/rest/i18n/getI18nProperties?locale=en-GB&resourceclass=org.sakaiproject.i18n.InternationalizedMessages&resourcebundle=test";
    const i18nProperties = `
title=Hello world
    `;

    fetchMock
      .get(i18nUrl, i18nProperties, {overwriteRoutes: true})
      .get("*", 500, {overwriteRoutes: true});

    it("Expects to work with portal.locale set", async () => {

      window.portal = { locale: "en-GB" };
      const r = await loadProperties("test");
      expect(r["title"]).to.equal("Hello world");
    });

    it("Expects to work with an explicit lang", async () => {

      const r = await loadProperties({bundle: "test", lang: "en-GB"});
      expect(r["title"]).to.equal("Hello world");
    });

    it("Expects properties to be in sessionStorage after the call", async () => {

      window.portal = { locale: "en-GB" };
      const r = await loadProperties("test");
      expect(window.sessionStorage.getItem("en-GBtest")).to.not.equal(null);
    });

    it("Expects properties to be not be in sessionStorage if caching is off", async () => {

      window.portal = { locale: "en-GB" };
      expect(window.sessionStorage.getItem("en-GBtest")).to.equal(null);
      const r = await loadProperties({ bundle: "test", cache: false });
      expect(window.sessionStorage.getItem("en-GBtest")).to.equal(null);
    });
  });
});
