import { html } from 'lit-html';
import { unsafeHTML } from 'lit-html/directives/unsafe-html';
import { withA11y } from "@storybook/addon-a11y";
import { styles } from "./styles/sakai-styles.js";

import '../js/sakai-editor.js';

export default {
  title: 'Sakai Editor',
  decorators: [withA11y],
};

export const BasicDisplay = () => {

  return html`
    ${unsafeHTML(styles)}
    <sakai-editor>
  `;
};
