import { html } from 'lit-html';
import { unsafeHTML } from 'lit-html/directives/unsafe-html';
import fetchMock from "fetch-mock";
import { withA11y } from "@storybook/addon-a11y";
import { styles } from "./styles/sakai-styles.js";
import { datepickerI18n } from "./i18n/datepicker-i18n.js";

import '../js/datepicker/sakai-date-picker.js';

export default {
  title: 'Sakai Date Picker',
  decorators: [withA11y, (storyFn) => {
    parent.portal = {locale: "en-GB"};
    const baseUrl = "/sakai-ws/rest/i18n/getI18nProperties?locale=en-GB&resourceclass=org.sakaiproject.i18n.InternationalizedMessages&resourcebundle=";
    const datepickerI18nUrl = `${baseUrl}date-picker-wc`;
    fetchMock
      .get(datepickerI18nUrl, datepickerI18n, {overwriteRoutes: true})
      .get("*", 500, {overwriteRoutes: true});
    return storyFn();
  }],
};

export const BasicDisplay = () => {

  return html`
    ${unsafeHTML(styles)}
    <sakai-date-picker>
  `;
};
