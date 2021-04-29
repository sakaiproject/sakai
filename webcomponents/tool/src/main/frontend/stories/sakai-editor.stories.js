import { html } from 'lit-html';
import { unsafeHTML } from 'lit-html/directives/unsafe-html';
import { styles } from "./styles/sakai-styles.js";

import '../js/sakai-editor.js';

export default {
  title: 'Sakai Editor'
};

export const BasicDisplay = () => {

  return html`
    ${unsafeHTML(styles)}
    <sakai-editor>
  `;
};
