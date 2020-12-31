import { html } from 'lit-html';
import { withKnobs, text, boolean, number, select } from "@storybook/addon-knobs";
import { unsafeHTML } from 'lit-html/directives/unsafe-html';
import { styles } from "./styles/sakai-styles.js";

import '../js/sakai-icon.js';

export default {
  title: 'Sakai Icon',
  decorators: [withKnobs]
};

let sizes = ["small", "medium", "large"];

export const Alerts = () => html`
  ${unsafeHTML(styles)}
  <sakai-icon type="alert" size="${select('size', sizes, 'small')}" />
`;

export const Assignments = () => html`
  ${unsafeHTML(styles)}
  <sakai-icon type="assignments" size="${select('size', sizes, 'small')}" ?has-alerts="${boolean('has-alerts', false)}"/>
`;

export const Favourite = () => html`
  ${unsafeHTML(styles)}
  <sakai-icon type="favourite" size="${select('size', sizes, 'medium')}" />
`;

export const Gradebook = () => html`
  ${unsafeHTML(styles)}
  <sakai-icon type="gradebook" size="${select('size', sizes, 'small')}" />
`;

export const Forums = () => html`
  ${unsafeHTML(styles)}
  <sakai-icon type="forums" size="${select('size', sizes, 'small')}" />
`;

export const Menu = () => html`
  ${unsafeHTML(styles)}
  <sakai-icon type="menu" size="${select('size', sizes, 'small')}" />
`;
