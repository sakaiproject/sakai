import { html } from 'lit-html';

import '../../js/forms/sakai-textarea.js';

export default {
  title: 'Sakai Text Area'
};

export const BasicDisplay = () => {

  return html`
    <div style="width: 400px;">
      <sakai-text-area value=""
          id="user-bio"
          name="userBio"
          maxlength="20"
          cols="45"
          rows="2"
          error-message="You can't insert more than 20 chars">
      </sakai-text-area>
    </div>
  `;
};
