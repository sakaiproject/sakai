import { html } from 'lit-html';
import fetchMock from "fetch-mock";
import { withKnobs, text, boolean } from "@storybook/addon-knobs";
import { withCssResources } from '@storybook/addon-cssresources';

import '../js/sakai-course-card.js';

// Mockup the i18n strings
const i18n = `
options_menu_tooltip=Click to see options for this course
select_tools_to_display=Select tools to display:
favourite_this_course=Favourite this course?
assignments_tooltip=Click to view your assignments for this course
gradebook_tooltip=Click to view the gradebook for this course
forums_tooltip=Click to view the forums for this course
`;

// Mockup the toolname mapping properties
const toolnameMappings = `
assignments=Assignments
gradebook=Gradebook
forums=Forums
`;

export default {
  title: 'Sakai Course Card',
  decorators: [withCssResources, withKnobs, (storyFn) => {
    parent.portal = {locale: "en-GB"};
    const baseUrl = "/sakai-ws/rest/i18n/getI18nProperties?locale=en-GB&resourceclass=org.sakaiproject.i18n.InternationalizedMessages&resourcebundle=";
    const i18nUrl = `${baseUrl}coursecard`;
    const toolnameMappingsUrl = `${baseUrl}toolname-mappings`;
    fetchMock
      .get(i18nUrl, i18n, {overwriteRoutes: true})
      .get(toolnameMappingsUrl, toolnameMappings, {overwriteRoutes: true})
      .get("/direct/site/addfavourite?siteId=xyz", 200, {overwriteRoutes: true})
      .get("/direct/site/removefavourite?siteId=xyz", 200, {overwriteRoutes: true})
      .get("*", 500, {overwriteRoutes: true});
    return storyFn();
  }],
  parameters: {
    cssresources: [
      {
        id: `default`,
        code: `
          <style>
            body {
              --sakai-course-card-width: 403px;
              --sakai-course-card-bg-color: white;
              --sakai-course-card-font-family: roboto, arial;
              --sakai-course-card-info-height: 90px;
              --sakai-course-card-border-width: 0;
              --sakai-course-card-border-color: black;
              --sakai-course-card-border-radius: 4px;
              --sakai-course-card-padding: 20px;
              --sakai-course-card-info-block-bg-color: #0f4b6f;
              --sakai-icon-favourite-color: yellow;
              --sakai-course-card-title-color: white;
              --sakai-course-card-title-font-size: 16px;
              --sakai-course-card-code-color: white;
              --sakai-course-card-code-font-size: 12px;
              --sakai-course-card-tool-alerts-height: 40px;
              --sakai-course-card-tool-alerts-padding:5px;
              --sakai-course-card-border-width: 0;
              --sakai-course-card-border-color: black;
              --sakai-course-card-border-radius: 4px;
              --sakai-course-card-tool-alerts-color: black;
              --sakai-course-card-tool-alert-icon-color: rgb(15,75,111);
              --sakai-options-menu-invoker-color: white;
              --sakai-course-card-options-menu-favourites-block-color: black;
              --sakai-course-card-options-menu-favourites-block-font-size: inherit;
              --sakai-course-card-options-menu-favourites-block-font-weight: bold;
              --sakai-course-card-options-menu-tools-title-font-weight: bold;
            }
          </style>`,
        picked: false,
      },
      {
        id: `anothertheme`,
        code: `
          <style>
            body {
              --sakai-course-card-bg-color: lightgrey;
              --sakai-course-card-font-family: roboto, arial;
              --sakai-course-card-info-height: 90px;
              --sakai-course-card-border-width: 0;
              --sakai-course-card-border-color: black;
              --sakai-course-card-border-radius: 4px;
              --sakai-course-card-padding: 20px;
              --sakai-course-card-info-block-bg-color: #0f4b6f;
              --sakai-icon-favourite-color: blue;
              --sakai-course-card-title-color: orange;
              --sakai-course-card-title-font-size: 18px;
              /* Add more css vars here ! */
            }
          </style>`,
        picked: false,
      },
    ],
  },
};

export const BasicDisplay = () => {

  return html`
    <sakai-course-card />
  `;
};

export const WithImage = () => {

  return html`
    <sakai-course-card course-data='{"id": "xyz", "title": "Marine Biology 101", "code": "MB 101", "alerts": ["gradebook", "forums"], "favourite": false, "image": "https://static.wixstatic.com/media/e441d1_6c7bdbdb1ef84fc6bfc09f8365b77e67~mv2.png"}'
      tool-urls='{"assignments": "http://www.gmail.com"}'>
    </sakai-course-card>
  `;
};

export const TwoWithData = () => {

  const courseData1 = {
    id: "xyz",
    title: "Biogeochemical Oceanography",
    code: "BCO 104",
    alerts: ["forums"],
    favourite: false,
  };

  const courseData2 = {
    id: "xyz",
    title: "Marine Biology 101",
    code: "MB 101",
    alerts: ["gradebook", "forums"],
    favourite: false,
    image: "https://static.wixstatic.com/media/e441d1_6c7bdbdb1ef84fc6bfc09f8365b77e67~mv2.png"
  };

  return html`
    <sakai-course-card course-data=${JSON.stringify(courseData1)}></sakai-course-card>
    <br />
    <br />
    <sakai-course-card course-data=${JSON.stringify(courseData2)} tool-urls='{"assignments": "http://www.gmail.com"}'></sakai-course-card>
  `;
};
