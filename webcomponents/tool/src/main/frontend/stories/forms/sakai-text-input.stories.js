import { html } from 'lit-html';

import '../../js/forms/sakai-text-input.js';

export default {
  title: 'Sakai Text Input'
};

export const BasicDisplay = () => {

  return html`
    <div style="width: 400px;">
      <sakai-text-input value="" id="text-input-story" maxLength="5" errorMessage="You can't insert more than 5 chars"></sakai-text-input>
    </div>
  `;
};
