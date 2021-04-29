import { SakaiMaximiseButton } from "../js/sakai-maximise-button.js";
import { TestUtils } from "./test-utils.js";
import fetchMock from "../node_modules/fetch-mock/esm/client.js";
import { maximiseButtonI18n } from "./i18n/maximise-button.js";

describe ("Sakai Maximise Button", () => {

  beforeAll(() => {

    const i18nUrl = "/sakai-ws/rest/i18n/getI18nProperties?locale=&resourceclass=org.sakaiproject.i18n.InternationalizedMessages&resourcebundle=maximise-button";

    fetchMock.get(i18nUrl, maximiseButtonI18n, {overwriteRoutes: true})
    .get("*", 500, {overwriteRoutes: true});
  });

  it ("Renders full screen", async () => {

    const element = await TestUtils.render("sakai-maximise-button", {"full-screen": true});
    setTimeout(() => {
      const compressIcon = element.querySelector("fa-icon[i-class='fas compress-arrows-alt']");
      expect(compressIcon).toBeTruthy();
    }, 1000);
  });

  it ("Renders normal", async () => {

    const element = await TestUtils.render("sakai-maximise-button");
    setTimeout(() => {
      const expandIcon = element.querySelector("fa-icon[i-class='fas compress-arrows-alt']");
      expect(expandIcon).toBeTruthy();
    }, 1000);
  });
});

