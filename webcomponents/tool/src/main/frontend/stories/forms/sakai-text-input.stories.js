import { html } from 'lit-html';

import '../../js/forms/sakai-text-input.js';

export default {
  title: 'Sakai Text Input'
};

export const BasicDisplay = () => {

  return html`
    <div style="width: 400px;">
      <sakai-text-input value=""
          id="first-name-input"
          name="firstName"
          maxlength="5"
          error-message="You can't insert more than 5 chars">
      </sakai-text-input>
    </div>
  `;
};
