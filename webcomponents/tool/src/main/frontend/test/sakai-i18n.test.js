import { loadProperties } from "../js/sakai-i18n.js";
import fetchMock from "../node_modules/fetch-mock/esm/client.js";

describe ("Sakai i18n", () => {


  beforeEach(() => {
    fetchMock.restore();
  });

  afterEach(() => {

    window.sessionStorage.clear();
    window.sakai = undefined;
  });

  describe("loadProperties", () => {

    it ("Expects to work with portal.locale set", async () => {

      const i18nUrl = "/sakai-ws/rest/i18n/getI18nProperties?locale=en-GB&resourceclass=org.sakaiproject.i18n.InternationalizedMessages&resourcebundle=test";

      fetchMock.get(i18nUrl, "title=Hello world", {overwriteRoutes: true})
      .get("*", 500, {overwriteRoutes: true});

      window.portal = { locale: "en-GB" };
      const r = await loadProperties("test");
      expect(r["title"]).toEqual("Hello world");
    });

    it ("Expects to work with an explicit lang", async () => {

      const i18nUrl = "/sakai-ws/rest/i18n/getI18nProperties?locale=en-GB&resourceclass=org.sakaiproject.i18n.InternationalizedMessages&resourcebundle=test";

      fetchMock.get(i18nUrl, "title=Hello world", {overwriteRoutes: true})
      .get("*", 500, {overwriteRoutes: true});

      const r = await loadProperties({bundle: "test", lang: "en-GB"});
      expect(r["title"]).toEqual("Hello world");
    });

    it ("Expects properties to be in sessionStorage after the call", async () => {

      const i18nUrl = "/sakai-ws/rest/i18n/getI18nProperties?locale=en-GB&resourceclass=org.sakaiproject.i18n.InternationalizedMessages&resourcebundle=test";

      fetchMock.get(i18nUrl, "title=Hello world", {overwriteRoutes: true})
      .get("*", 500, {overwriteRoutes: true});

      window.portal = { locale: "en-GB" };
      const r = await loadProperties("test");
      expect(window.sessionStorage.getItem("en-GBtest")).toBeTruthy();
    });

    it ("Expects properties to not be in sessionStorage if caching is off", async () => {

      const i18nUrl = "/sakai-ws/rest/i18n/getI18nProperties?locale=en-GB&resourceclass=org.sakaiproject.i18n.InternationalizedMessages&resourcebundle=test";

      fetchMock.get(i18nUrl, "title=Hello world", {overwriteRoutes: true})
      .get("*", 500, {overwriteRoutes: true});

      window.portal = { locale: "en-GB" };
      expect(window.sessionStorage.getItem("en-GBtest")).toBeNull();
      const r = await loadProperties({ bundle: "test", cache: false });
      expect(window.sessionStorage.getItem("en-GBtest")).toBeNull();
    });
  });
});
