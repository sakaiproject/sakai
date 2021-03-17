import { html } from 'lit-html';
import { unsafeHTML } from 'lit-html/directives/unsafe-html';
import fetchMock from "fetch-mock";
import { withA11y } from "@storybook/addon-a11y";
import { styles } from "./styles/sakai-styles.js";
import { conversationsI18n } from "./i18n/conversations-i18n.js";
import { conversationsData } from "./data/conversations-data.js";

import '../js/conversations/sakai-conversations.js';

export default {
  title: 'Sakai Conversations',
  decorators: [withA11y, (storyFn) => {
    parent.portal = {locale: "en-GB"};
    fetchMock
      .get(/sakai-ws\/rest\/i18n\/getI18nProperties.*/, conversationsI18n, {overwriteRoutes: true})
      .get(/api\/sites\/.*\/conversations/, conversationsData, {overwriteRoutes: true})
      .get("*", 500, {overwriteRoutes: true});
    return storyFn();
  }],
};

export const MobileDisplay = () => {

  return html`
    ${unsafeHTML(styles)}
    <div>
      <sakai-conversations site-id="playpen"></sakai-conversations>
    </div>
  `;
};
