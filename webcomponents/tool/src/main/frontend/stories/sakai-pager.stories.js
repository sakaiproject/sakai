import { html } from 'lit-html';
import { unsafeHTML } from 'lit-html/directives/unsafe-html';
import { styles } from "./styles/sakai-styles.js";

import '../js/sakai-pager.js';

export default {
  title: 'Sakai Pager',
  decorators: [storyFn => {
    parent.portal = {locale: "en-GB"};
    return storyFn();
  }],
};

export const BasicDisplay = () => {

  return html`
    ${unsafeHTML(styles)}
    <sakai-pager count="20" current="1"></sakai-pager>
  `;
};
