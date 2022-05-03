// TODO Review commented lines
// TODO Review need for shadown DOM
import { html } from "@assets/lit-element/lit-element.js";
import { SakaiElement } from "../../../sakai-element.js";
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

export class SuiTable extends SakaiElement {

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
      showFilters: { attribute: "show-filters", type: Boolean },
      showActions: { attribute: "show-actions", type: Boolean },
      tableActions: { attribute: "table-actions", type: Array },
      links: { type: Array },
      permissions: { type: Array },
      i18n: { attribute: false, type: Object },
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
    this.showActions = false;
    this.showFilters = false;
    // this.pageSize = 3;
    
    this.loadTranslations("sui-table").then(r => this.i18n = r);
  }

  connectedCallback() {

    super.connectedCallback();

    if (this.columns.length > 0 && !this.columns.filter(e => e.field === "id")) {
      this.columns.push({ field: "id" });
    }
    if (this.id && !this.title) {
      this.title = this.id;
    }
  }

  async firstUpdated() {

    // Give the browser a chance to paint
    await new Promise((r) => setTimeout(r, 0));
    this.rows = await this.fetchRows();
  }

  updated(changedProps) {

    //TODO how to check that the table exists??
    if (changedProps.has("showFilters")) {
      // this._table.on("tableBuilt", () => {
      //   this.debug
      //     ? console.log(
      //         `sui-table ${this.title} tableBuilt`
      //       )
      //     : null;
      //     const updatedColumns = this._table.getColumnLayout();
      //     updatedColumns.forEach((column) => {
      //       column.headerFilter = this.showFilters;
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

      const tabulatorColumns = this.columns.map(column => {
        const columnIterable = {
          title: "",
          field: "",
          sorter: "string",
          formatter: "plaintext",
          headerFilter: this.showFilters,
        };
        if (Array.isArray(column.field)) {
          column.field.forEach((element) => {
            columnIterable.title = column.title ? column.title : element;
            columnIterable.field = element;
          });
        } else if (typeof column.field === "string") {
          columnIterable.title = column.title ? column.title : column.field;
          columnIterable.field = column.field;
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

      //todo check math on 1 to 1 of 1, currently showing 1 to 0 of 0 or showing NaN to NaN of 6
      this._table.on("dataLoaded", (e, data) => {
        const currentPage = this._table.getPage() ? this._table.getPage() : 1;
        this.totalItems = this.dataKey
          ? this.rows[`${this.dataKey}`].length
          : this.rows.length;
        this.currentItemMin = (currentPage - 1) * this.pageSize + 1;
        this.currentItemMax = currentPage * this.pageSize;
      });

      this._table.on("pageLoaded", pageNumber => {
        this.pageSize = this._table.getPageSize();
        this.totalItems = this._table.getDataCount();
        this.currentItemMin = (pageNumber - 1) * this.pageSize + 1;
        this.currentItemMax =
          (pageNumber - 1) * this.pageSize +
          this._table.getData("visible").length;
      });
      this._table.on("rowMouseOver", (e, row) => {
        // Display the row actions for this item
        if (this.links) {
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
      this._table.on("rowSelected", data => {
        if (!this.showActions) {
          this.showActions = true;
        }
        this.requestUpdate();
      });
      this._table.on("rowDeselected", data => {
        if (!this.alwaysShowlinks && this._table.getSelectedRows().length === 0) {
          this.showActions = false;
          this.requestUpdate();
        }
      });
    }
    super.update(changedProps);
  }

  // TODO add manual refetch options
  async fetchRows(value) {

    if (!this.siteId) {
      console.error(`sui-table ${this.title} fetchRows no siteId`);
      return;
    }

    let result = value;
    this.dataPromise = await fetch(this.dataUrl)
      .then(response => {

        if (response.ok) {
          return response.json();
        }

        throw new Error("Network error while fetching rows");
      })
      .then(data => {

        result = data;

        // If columns were not passed in via props, use the first entity in the data
        // to define the columns
        if (this.columns.length === 0 && typeof result[0] === "object") {
          Object.keys(result[0]).forEach((key) => {
            this.columns.push({
              title: key,
              field: key,
            });
          });
        }
        result.forEach(element => {
          if (this.links) {

            const actionSnippet = this.links.map(action => {
              // TODO review and dry this up
              return `
                <li class="nav-item">
                  <a title="${action.title ? action.title : ""}"
                      aria-label="${action.ariaLabel ? action.ariaLabel : ""}"
                      icon="${action.icon ? action.icon : ""}"
                      class="${action.class
                                ? `link link-${action.title} pt-0 ${action.class}`
                                : `link link-${action.title}`}"
                      href="${this.links.filter(link => link.rel === action.rel).length === 1
                                ? this.links.filter((link) => link.rel === action.rel)[0].href : "#"}"
                      onclick="${action.onclick ? action.onclick : ""}">
                    ${action.icon ? `<sui-icon class="sui-icon" type="${action.icon}"></sui-icon>` : ""}
                    ${action.title ? action.title : ""}
                  </a>
                </li>`;
            })
            .join("");
            
            element[this.columns[0].field] = `
              <div class="links d-flex justify-content-between">
                <div class="item-text text-truncate">
                  ${element[this.columns[0].field]}
                </div>
                <ul class="links-list nav flex-nowrap d-none ">${actionSnippet}</ul>
              </div>`;
          }
        });
      });
    return result;
  }

  render() {

    this._tableActions = [];
    for (const action of this.tableActions) {
      this._tableActions.push(html`
      <li class="nav-item">
        <a title="${action.title ? action.title : ""}"
            aria-label="${action.ariaLabel ? action.ariaLabel : ""}"
            icon="${action.icon ? action.icon : ""}"
            class="${action.class ? action.class : "nav-link"}"
            href="${action.href ? action.href : "#"}">
          ${action.icon ? html`<sui-icon class="sui-icon" type="${action.icon}"></sui-icon>` : ""}
          ${action.title ? action.title : ""}
        </a>
      </li>
      `);
    }
    // Every table has Bulks Actions button
    //TODO wrap all actions in permission check
    //TODO post TRINITY-52 i think authz will be in rest api and will need refactor
    //TODO i18n strings
    this._tableActions.push(html`
      <sui-button
        buttonTitle="${
          this.showActions ? "Hide Bulk Actions" : "Show Bulk Actions"
        }"
        label="Toggle display of Bulk Actions"
        icon="check-square"
        id="${this.id}BulkActionsToggle"
        @click="${() => this.showActions = !this.showActions}"
      ></sui-button>
    `);

    // Every table has Filter button
    //TODO i18n strings
    this._tableActions.push(html`
      <li class="nav-item">
        <sui-button
          buttonTitle="${this.showFilters ? "Hide Filters" : "Show Filters"}"
          label="Toggle display of Filters"
          icon="filter"
          id="${this.id}FilterToggle"
          @click="${() => {
            this.showFilters = !this.showFilters;
          }}"
        ></sui-button>
      </li>
    `);

    return html`
      <ul class="sakai-table-toolBar nav nav-pills d-flex p-1">
        ${this._tableActions}
        <li id="sakai-table-pagerContainer" class="sakai-table-pagerContainer nav-item ms-auto">
          <div class="sakai-table-pagerLabel">
            Showing ${this.currentItemMin} to ${this.currentItemMax} of ${this.totalItems}
          </div>
        </li>
      </ul>
      <ul id="links"
          class="nav p-3 bg-dark text-white btn-group ${this.showActions ? "d-flex" : "d-none"} align-items-center"
          role="group"
          aria-label="Row Actions">
        <li id="rowsSelectedCounter" class="nav-item">XX selected</li>
        <sui-button
          buttonTitle="Edit"
          label="Edit"
          icon="pencil"
          href="#"
          buttonClass="nav-item btn-link link-secondary"
        ></sui-button>
        <sui-button
          buttonTitle="Duplicate"
          label="Duplicate"
          icon="clone"
          href="#"
          buttonClass="nav-item btn-link link-secondary"
        ></sui-button>
        <sui-button
          buttonTitle="Remove"
          label="Remove"
          icon="trash"
          href="#"
          class="nav-item btn-link link-danger"
        ></sui-button>
        <div class="nav-item d-flex flex-fill justify-content-end">
          <sui-button
            label="Close Row Actions"
            buttonTitle="Close"
            icon="close"
            @click=${() => {
              this.showActions = false;
              this.requestUpdate();
            }}
            buttonClass="btn-link link-secondary"
          ></sui-button>
        </div>
      </ul>
      </div>
      <div id="suiTable${this.id}"
          class="sui-table table ${this.class ? this.class : "table-striped table-hover"}">
      </div>
      <div class="sakai-table-endBar" count="19" current="1"></div>
    `;
  }
}
