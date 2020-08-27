import { html } from 'lit-html';
import { unsafeHTML } from 'lit-html/directives/unsafe-html';
import fetchMock from "fetch-mock";
import { withA11y } from "@storybook/addon-a11y";
import { styles } from "./styles/sakai-styles.js";
import { forumsI18n } from "./i18n/forums-i18n.js";
import { forumsData } from "./data/forums-data.js";

import '../js/widgets/sakai-forums-widget.js';

export default {
  title: 'Sakai Forums Widget',
  decorators: [withA11y, (storyFn) => {
    parent.portal = {locale: "en-GB"};
    const baseUrl = "/sakai-ws/rest/i18n/getI18nProperties?locale=en-GB&resourceclass=org.sakaiproject.i18n.InternationalizedMessages&resourcebundle=";
    const forumsI18nUrl = `${baseUrl}forums`;
    fetchMock
      .get(forumsI18nUrl, forumsI18n, {overwriteRoutes: true})
      .get("/api/forums.json", forumsData, {overwriteRoutes: true})
      .get("*", 500, {overwriteRoutes: true});
    return storyFn();
  }],
};

export const BasicDisplay = () => {

  return html`
    ${unsafeHTML(styles)}
    <sakai-forums-widget>
  `;
};
