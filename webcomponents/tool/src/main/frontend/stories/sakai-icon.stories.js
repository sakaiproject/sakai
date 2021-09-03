import { html } from 'lit-html';

import "../js/sakai-icon.js";

export default {
  title: 'Sakai Icon',
};

export const BasicDisplay = () => {


  return html`

    <sakai-icon size="large" type="menu" style="color: white;"></sakai-icon>
  `;
};
