import {
  html,
  LitElement,
  // unsafeCSS,
} from "@assets/lit-element/lit-element.js";
import { TabulatorFull as Tabulator } from "@assets/tabulator-tables/dist/js/tabulator_esm.min.js";
// import styles from "./sui-table.scss";
import "../../sui-icon/sui-icon.js";

// TODO implement the link action with the new api and permissions scheme
// TODO update all sui-icon and sui-button implementation to use this
// TODO discuss whether to make the action bar a component
// TODO research whether to convert most props to single config object
// TODO discuss shadow dom
// TODO write unit tests
// TODO use property defined sorting column
// TODO review tabulator progressive ajax loading for long lists
// TODO review tabulator persistance
// TODO review alwaysshowlinks, should it just act like filtervisibility?
// TODO review youtube studio again, seems like more prop updating and less class updating
// TODO mobile, should we add a card layout toggler to the desktop ui and auto switch it in mobile?
// TODO review all local props, should they all be class props?
// TODO document the hell outta this thing
// TODO review all requestupdates and see if we can remove them via class props vs local props
// TODO can i move sakai-pagerContainer to the tablulator footer?

export class SuiTable extends LitElement {
  createRenderRoot() {
    // Render to the real dom, not the shadow. We can now pull
    // in Sakai's css and js. This makes any this, and any subclasses,
    // custom elements, not full blown web components
    return this;
  }
  static get properties() {
    return {
      class: { type: String },
      siteId: { attribute: "site-id", type: String },
      rows: { type: Object },
      columns: { type: Array },
      height: { type: String },
      dataUrl: { attribute: "data-url", type: String },
      currentItemMin: { type: Number },
      currentItemMax: { type: Number },
      totalItems: { type: Number },
      pageSize: { attribute: "page-size", type: Number },
      dataKey: { attribute: "data-key", type: String },
      alwaysShowlinks: {
        attribute: "always-show-links",
        type: Boolean,
      },
      title: { attribute: "title", type: String },
      filterVisibility: { attribute: "show-filters", type: Boolean },
      linksVisbility: { attribute: "show-actions", type: Boolean },
      tableActions: { attribute: "table-actions", type: Array },
      links: { type: Array },
      debug: { type: Boolean },
      permissions: { type: Array },
    };
  }

  constructor() {
    super();

    // This prevents duplicate styles from being added to the component
    this.class = this.classList.value;
    this.classList = "";

    // this.siteId = "";
    this.columns = [];
    // this.rows = [];
    this._table = {};
    this.currentItemMin = 1;
    this.currentItemMax = 0;
    this.totalItems = 0;
    this.linksVisbility = false;
    this.filterVisibility = false;
    // this.pageSize = 3;
    this.debug = false;
    this.debug ? console.log(`sui-table ${this.title} end constructor`) : null;
  }

  connectedCallback() {
    super.connectedCallback();

    if (
      this.columns.length > 0 &&
      !this.columns.filter((e) => e.field === "id")
    ) {
      this.columns.push({ field: "id" });
    }
    if (this.id && !this.title) {
      this.title = this.id;
    }
    this.debug
      ? console.log(`sui-table ${this.title} end connectedCallback`)
      : null;
  }

  attributeChangedCallback(name, oldVal, newVal) {
    this.debug
      ? console.log(
          `sui-table ${this.title} attribute change: `,
          name,
          typeof newVal,
          newVal
        )
      : null;
    super.attributeChangedCallback(name, oldVal, newVal);
  }

  async firstUpdated() {
    this.debug
      ? console.log(`sui-table ${this.title} start firstUpdated`)
      : null;
    // Give the browser a chance to paint
    await new Promise((r) => setTimeout(r, 0));
    this.rows = await this.fetchRows();

    this.debug ? console.log(`sui-table ${this.title} end firstUpdated`) : null;
  }

  updated(changedProps) {
    this.debug
      ? console.log(`sui-table ${this.title} start updated`, changedProps)
      : null;
    //TODO how to check that the table exists??
    if (changedProps.has("filterVisibility")) {
      // this._table.on("tableBuilt", () => {
      //   this.debug
      //     ? console.log(
      //         `sui-table ${this.title} tableBuilt`
      //       )
      //     : null;
      //     const updatedColumns = this._table.getColumnLayout();
      //     updatedColumns.forEach((column) => {
      //       column.headerFilter = this.filterVisibility;
      //     });
      //     this._table.setColumnLayout(updatedColumns);
      //   });
    }

    if (changedProps.has("rows")) {
      const selectorColumn = [
        {
          formatter: "rowSelection",
          titleFormatter: "rowSelection",
          hozAlign: "center",
          headerSort: false,
          cellClick(e, cell) {
            cell.getRow().toggleSelect();
          },
        },
      ];

      const tabulatorColumns = this.columns.map((i) => {
        const columnIterable = {
          title: "",
          field: "",
          sorter: "string",
          formatter: "plaintext",
          headerFilter: this.filterVisibility,
        };
        if (Array.isArray(i.field)) {
          i.field.forEach((element) => {
            columnIterable.title = i.title ? i.title : element;
            columnIterable.field = element;
          });
        } else if (typeof i.field === "string") {
          columnIterable.title = i.title ? i.title : i.field;
          columnIterable.field = i.field;
          // columnIterable.headerFilter = "input";
        }

        if (columnIterable.field === "id") {
          columnIterable.visible = false;
        }

        return columnIterable;
      });
      tabulatorColumns[0].formatter = "html";
      if (this.links) {

        tabulatorColumns[0].minWidth = this.links.length * 100 + 64; //number of row actions + 4em;
      }

      this.debug
        ? console.log(`sui-table ${this.title} columns`, tabulatorColumns)
        : null;
      this._table = new Tabulator(`#suiTable${this.id}`, {
        debugEventsExternal: false,
        debugEventsInternal: false,
        columns: selectorColumn.concat(tabulatorColumns),
        data: this.dataKey ? this.rows[`${this.dataKey}`] : this.rows,
        height: this.height,
        pagination: true,
        paginationSize: this.pageSize ? this.pageSize : 10,
        // paginationElement: document.getElementById('sakai-table-pagerContainer'),
        paginationButtonCount: 3,
        paginationSizeSelector: [10, 20, 50, 100, true],
        layout: "fitDataFill",
        rowFormatter: (row) => {
          row.getElement().id = `item-${row.getData().id}`;
        },
      });
      this.debug
        ? console.log(
            `sui-table ${this.title} rows`,
            this.dataKey ? this.rows[`${this.dataKey}`] : this.rows
          )
        : null;

      //todo check math on 1 to 1 of 1, currently showing 1 to 0 of 0 or showing NaN to NaN of 6
      this._table.on("dataLoaded", (e, data) => {
        this.debug
          ? console.log(`sui-table ${this.title} dataLoaded`, e, data)
          : null;
        const currentPage = this._table.getPage() ? this._table.getPage() : 1;
        this.totalItems = this.dataKey
          ? this.rows[`${this.dataKey}`].length
          : this.rows.length;
        this.currentItemMin = (currentPage - 1) * this.pageSize + 1;
        this.currentItemMax = currentPage * this.pageSize;
      });

      this._table.on("pageLoaded", (pageNumber) => {
        this.debug
          ? console.log(
              `sui-table ${this.title} pageLoaded`,
              this._table.getData("visible").length,
              this._table.getPageSize()
            )
          : null;
        this.pageSize = this._table.getPageSize();
        this.totalItems = this._table.getDataCount();
        this.currentItemMin = (pageNumber - 1) * this.pageSize + 1;
        this.currentItemMax =
          (pageNumber - 1) * this.pageSize +
          this._table.getData("visible").length;
      });
      this._table.on("rowMouseOver", (e, row) => {
        // Display the row actions for this item
        if (this.links){

          row
          .getElement()
          .querySelector(
            `div[tabulator-field=${this.columns[0].field}] .links-list`
            )
            .classList.remove("d-none");
          }
      });
      this._table.on("rowMouseOut", (e, row) => {
        if (this.links){
        row
          .getElement()
          .querySelector(
            `div[tabulator-field=${this.columns[0].field}] .links-list`
          )
          .classList.add("d-none");
        }
      });
      this._table.on("rowSelected", (data) => {
        this.debug
          ? console.log(`sui-table ${this.title} rowSelected`, data)
          : null;
        if (!this.linksVisbility) {
          this.linksVisbility = true;
        }
        this.requestUpdate();
      });
      this._table.on("rowDeselected", (data) => {
        this.debug
          ? console.log(`sui-table ${this.title} rowDeselected`, data)
          : null;
        if (
          !this.alwaysShowlinks &&
          this._table.getSelectedRows().length === 0
        ) {
          this.linksVisbility = false;
          this.requestUpdate();
        }
      });
    }
    super.update(changedProps);
  }

  // TODO add manual refetch options
  async fetchRows(value) {
    this.debug ? console.log(`sui-table ${this.title} start fetchRows`) : null;
    if (typeof this.siteId === "undefined" || this.siteId.length === 0) {
      console.error(`sui-table ${this.title} fetchRows no siteId`);
      return;
    }

    let result = value;
    this.dataPromise = await fetch(this.dataUrl)
      .then((response) => response.json())
      .then((data) => {
        result = data;

        // If columns were not passed in via props, use the first entity in the data
        // to define the columns
        console.log(typeof result[0], result[0]);
        if (this.columns.length === 0 && typeof result[0] === "object") {
          Object.keys(result[0]).forEach((key) => {
            this.columns.push({
              title: key,
              field: key,
            });
          });
          this.debug
            ? console.log(
                `sui-table ${this.title} self generated columns`,
                this.columns
              )
            : null;
        }
        result.forEach((element) => {
          if (this.links){

            const actionSnippet = this.links
            .map((action) => {
              return `
            <li class="nav-item">
              <a
              title="${action.title ? action.title : ""}"
              aria-label="${action.ariaLabel ? action.ariaLabel : ""}"
              icon="${action.icon ? action.icon : ""}"
            class="${
              action.class
                ? `link link-${action.title} pt-0 ${action.class}`
                : `link link-${action.title}`
            }"
            // TODO dry this up
            href="${
              this.links.filter((link) => link.rel === action.rel).length ===
              1
              ? this.links.filter((link) => link.rel === action.rel)[0]
              .href
              : "#"
            }"
            onclick="${action.onclick ? action.onclick : ""}"
            >${action.icon ? `<sui-icon class="sui-icon" type="${action.icon}"></sui-icon>` : ""}${action.title ? action.title : ""}</a>
            </li>`;
          })
            .join("");
            
            element[
              this.columns[0].field
            ] = `<div class="links d-flex justify-content-between"><div class="item-text text-truncate">${
              element[this.columns[0].field]
            }</div><ul class="links-list nav flex-nowrap d-none ">${actionSnippet}</ul></div>`;
          }
          });
        });
    this.debug
      ? console.log(`sui-table ${this.title} end fetchRows`, result)
      : null;
    return result;
  }

  render() {
    this.debug ? console.log(`sui-table ${this.title} start render`) : null;
    this._tableActions = [];
    for (const action of this.tableActions) {
      this._tableActions.push(html`
      <li class="nav-item">
      <a
          title="${action.title ? action.title : ""}"
          aria-label="${action.ariaLabel ? action.ariaLabel : ""}"
          icon="${action.icon ? action.icon : ""}"
          class="${action.class ? action.class : "nav-link"}"
          href="${action.href ? action.href : "#"}"
        >${action.icon ? html`<sui-icon class="sui-icon" type="${action.icon}"></sui-icon>` : ""}${action.title ? action.title : ""}</a>
      </li>
      `);
    }
    // Every table has Bulks Actions button
    //TODO wrap all actions in permission check
    //TODO post TRINITY-52 i think authz will be in rest api and will need refactor
    //TODO i18n strings
    this._tableActions.push(html`
      <sui-button
      //todo i18n strings
        title="${
          this.linksVisbility ? "Hide Bulk Actions" : "Show Bulk Actions"
        }"
        aria-label="Toggle display of Bulk Actions"
        icon="check-square"
        id="${this.id}BulkActionsToggle"
        @click="${() => {
          this.linksVisbility = !this.linksVisbility;
        }}"
        debug
      ></sui-button>
    `);

    // Every table has Filter button
    //TODO i18n strings
    this._tableActions.push(html`
      <li class="nav-item">

      <sui-button
      //todo i18n strings
        title="${this.filterVisibility ? "Hide Filters" : "Show Filters"}"
        aria-label="Toggle display of Filters"
        icon="filter"
        id="${this.id}FilterToggle"
        @click="${() => {
          this.filterVisibility = !this.filterVisibility;
        }}"
      ></sui-button>
      </li>
    `);

    return html`
    <ul class="sakai-table-toolBar nav nav-pills d-flex p-1">
      ${this._tableActions}
        <li id="sakai-table-pagerContainer" class="sakai-table-pagerContainer nav-item ms-auto">
          <div class="sakai-table-pagerLabel">
            Showing ${this.currentItemMin} to ${this.currentItemMax} of
            ${this.totalItems}
          </div>
      </li>
      </ul>
      <ul id="links" class="nav p-3 bg-dark text-white btn-group ${
        this.linksVisbility ? "d-flex" : "d-none"
      } align-items-center" role="group" aria-label="Row Actions">
        <li id="rowsSelectedCounter" class="nav-item">XX selected</li>
        <sui-button
          title="Edit"
          aria-label="Edit"
          icon="pencil"
          href="#"
          class="nav-item btn-link link-secondary"
        ></sui-button>
        <sui-button
          title="Duplicate"
          aria-label="Duplicate"
          icon="clone"
          href="#"
          class="nav-item btn-link link-secondary"
        ></sui-button>
        <sui-button
          title="Remove"
          aria-label="Remove"
          icon="trash"
          href="#"
          class="nav-item btn-link link-danger"
        ></sui-button>
        <div class="nav-item d-flex flex-fill justify-content-end">
        <sui-button
          aria-label="Close Row Actions"
          title="Close"
          icon="close"
          @click=${() => {
            this.linksVisbility = false;
            this.requestUpdate();
          }}
          class="btn-link link-secondary"
        ></sui-button>
  </div>
        </ul>

      </div>
      <div
        id="suiTable${this.id}"
        class="sui-table table ${
          this.class ? this.class : "table-striped table-hover"
        }"
      ></div>
      <div class="sakai-table-endBar" count="19" current="1"></div>`;
  }

  static get styles() {
    return [
      // (typeof styles !== 'undefined' ? unsafeCSS(styles) : null)
    ];
  }
}
