import { html } from 'lit-html';
import { withKnobs, text, boolean, number } from "@storybook/addon-knobs";

import '../js/sakai-options-menu.js';

export default {
  title: 'Sakai Options Menu',
  decorators: [withKnobs]
};

export const BasicDisplay = () => html`

  <sakai-options-menu placement="${text('placement', 'right')}">
    <div slot="content">
      <div>Select some fruit options!</div>
      <div><input type="checkbox" />Bananas</div>
      <div><input type="checkbox" />Apples</div>
      <div><input type="checkbox" />Orange</div>
    </div>
  </sakai-options-menu>
`;
