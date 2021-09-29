import { html } from 'lit-html';

import '../../js/forms/sakai-text-area.js';

export default {
  title: 'Sakai Text Area'
};

export const BasicDisplay = () => {

  return html`
    <div style="width: 400px;">
      <sakai-text-area value="" id="text-area-story" maxLength="20" cols="45" rows="2" errorMessage="You can't insert more than 20 chars"></sakai-text-input>
    </div>
  `;
};
