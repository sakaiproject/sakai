import { html } from 'lit-html';
import { withKnobs, text, boolean, number, select } from "@storybook/addon-knobs";

import '../js/sakai-icon.js';

export default {
  title: 'Sakai Icon',
  decorators: [withKnobs]
};

let sizes = ["small", "medium", "large"];

export const Alerts = () => html`
  <sakai-icon type="alert" size="${select('size', sizes, 'small')}" />
`;

export const Assignments = () => html`
  <sakai-icon type="assignments" size="${select('size', sizes, 'small')}" ?has-alerts="${boolean('has-alerts', false)}"/>
`;

export const Favourite = () => html`
  <sakai-icon type="favourite" size="${select('size', sizes, 'medium')}" />
`;

export const Gradebook = () => html`
  <sakai-icon type="gradebook" size="${select('size', sizes, 'small')}" />
`;

export const Forums = () => html`
  <sakai-icon type="forums" size="${select('size', sizes, 'small')}" />
`;

export const Menu = () => html`
  <sakai-icon type="menu" size="${select('size', sizes, 'small')}" />
`;
