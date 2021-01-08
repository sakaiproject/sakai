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
    fetchMock
      .get(/sakai-ws\/rest\/i18n\/getI18nProperties.*/, forumsI18n, {overwriteRoutes: true})
      .get(/api\/users\/.*\/forums/, forumsData, {overwriteRoutes: true})
      .get("*", 500, {overwriteRoutes: true});
    return storyFn();
  }],
};

export const BasicDisplay = () => {

  return html`
    ${unsafeHTML(styles)}
    <div style="width: 400px;">
      <sakai-forums-widget user-id="adrian">
    </div>
  `;
};
