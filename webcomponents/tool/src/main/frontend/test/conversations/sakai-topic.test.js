import { SakaiTopic } from "../../js/conversations/sakai-topic.js";
import { TestUtils } from "../test-utils.js";
import fetchMock from "../../node_modules/fetch-mock/esm/client.js";
import { conversationsI18n } from "./i18n/conversations.js";

describe ("Sakai Topic", () => {

  beforeAll(() => {

    const i18nUrl = "/sakai-ws/rest/i18n/getI18nProperties?locale=&resourceclass=org.sakaiproject.i18n.InternationalizedMessages&resourcebundle=conversations";

    fetchMock.get(i18nUrl, conversationsI18n, {overwriteRoutes: true})
    .get("*", 500, {overwriteRoutes: true});
  });

  it ("Renders a topic", async () => {
  });
});

