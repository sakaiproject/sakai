import { html } from "lit-element";
import "../sui-table.js";

export default {
  title: "Sakai UI/SuiTable",
  argTypes: {
    id: { control: "string" },
    class: { control: "string" },
    siteId: { attribute: "site-id", control: "string" },
    rows: { control: Object },
    columns: { control: Array },
    height: { control: "string" },
    dataUrl: { attribute: "data-url", control: "string" },
    currentItemMin: { control: "number" },
    currentItemMax: { control: "number" },
    totalItems: { control: "number" },
    pageSize: { attribute: "page-size", control: "number" },
    dataKey: { attribute: "data-key", control: "string" },
    alwaysShowlinks: {
      attribute: "always-show-links",
      control: Boolean,
    },
    title: { attribute: "title", control: "string" },
    filterVisibility: { attribute: "show-filters", control: Boolean },
    linksVisbility: { attribute: "show-actions", control: Boolean },
    // tableActions: { attribute: "table-actions", control: Array },
    links: { control: Array },
    debug: { control: Boolean },
  },
};

const Template = ({ id }) => {
  return html`
    <sui-table
      .debug
			.id=${id}
			.show-actions
			.show-filters
			.table-actions='[
        {
          "type": "sui-button",
          "icon": "plus-circle",
          "class": "nav-link active",
          "title": "$tlang.getString("gen.new") $tlang.getString("meta.title")",
          "onclick": "location=\"#toolLink("$action" "doNewannouncement")\"",
          "href": "#toolLink("$action" "doNewannouncement")"
        }
      ]''
      .links='[
        {
          "title": "Edit",
          "icon": "pencil",
          "class": "nav-link",
          "rel": "doReviseannouncement"
        },
        {
          "title": "Duplicate",
          "icon": "clone",
          "class": "nav-link",
          "rel": "doDuplicateAnnouncement"
        },
        {
          "title": "View",
          "icon": "play",
          "class": "nav-link",
          "rel": "self"
        },
        {
          "title": "Delete",
          "icon": "trash",
          "class": "nav-link link-danger",
          "rel": "doDelete_announcement_link"
        }
      ]'
      .columns='[
        {"field": "subject", "title": "$tlang.getString("gen.subject")"},
        {"field": "access", "title": "$tlang.getString("gen.visible")"},
        {"field": "author", "title": "$tlang.getString("gen.from")"},
        {"field": "date", "title": "$tlang.getString("gen.date")"},
        {"field": "release", "title": "$tlang.getString("gen.releasedate")"},
        {"field": "retract", "title": "$tlang.getString("gen.retractdate")"}
      ]'
      .site-id="NBKV_974E_4401"
      .data-url="/api/sites/NBKV_974E_4401/announcements"></sui-table>
  `;
};

export const SuiTable = (args) => Template(args);
SuiTable.args = {
  id: 'sui-table-id',
};
