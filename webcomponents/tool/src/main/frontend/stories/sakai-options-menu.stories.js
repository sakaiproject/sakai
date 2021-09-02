import { html } from 'lit-html';
import { unsafeHTML } from 'lit-html/directives/unsafe-html';
import '../js/sakai-options-menu.js';
import { sakaiStyles } from "./styles/sakai-styles.js";

export default {
  title: 'Sakai Options Menu',
};

const markup = (placement) => {

  return html`
  ${unsafeHTML(sakaiStyles)}
  <style>
    sakai-kebab-menu {
      margin: 400px;
    }
  </style>
  <sakai-options-menu placement="${placement}">
    <span slot="invoker"><sakai-icon slot="invoker" type="cog" size="small"></sakai-icon>Settings</span>
    <ul slot="content">
      <li>Chickens</li>
      <li>Pheasants</li>
    </ul>
  </sakai-options-menu>
  `;
};

export const Bottom = () => {
  return markup("bottom");
};

export const Top = () => {
  return markup("top");
};

export const Left = () => {
  return markup("left");
};

export const Right = () => {
  return markup("right");
};
