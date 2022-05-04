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
// TODO format dates from api
// TODO select all checkbox, how many is it really selecting?

export class SuiTable extends SakaiElement {

  static get properties() {

    return {
      tableClass: { attribute: "table-class", type: String },
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
      tableTitle: { attribute: "title", type: String },
      showFilters: { attribute: "show-filters", type: Boolean },
      showActions: { attribute: "show-actions", type: Boolean },
      tableActions: { attribute: "table-actions", type: Array },
      links: { type: Array },
      permissions: { type: Array },
      i18n: { attribute: false, type: Object },
      debug: { type: Boolean },
    };
  }

  constructor() {

    super();

    this.debug = false;
    // this.siteId = "";
    this.columns = [];
    // this.rows = [];
    this._table = {};
    this._numSelected = 0;
    this.currentItemMin = 1;
    this.currentItemMax = 0;
    this.totalItems = 0;
    this.showActions = false;
    this.showFilters = false;
    this.pageSize = 10;
    
    this.loadTranslations({bundle: "sui-table"}).then(r => this.i18n = r);
  }

  connectedCallback() {

    this.debug
      ? console.debug(`sui-table ${this.tableTitle} connected`)
      : null;

    super.connectedCallback();

    if (this.columns.length > 0 && !this.columns.filter(e => e.field === "id")) {
      this.columns.push({ field: "id" });
    }
    if (this.id && !this.tableTitle) {
      this.tableTitle = this.id;
    }
  }

  attributeChangedCallback(name, oldVal, newVal) {
    this.debug
      ? console.debug(
          `sui-table ${this.tableTitle} attribute change: `,
          name,
          typeof newVal,
          newVal
        )
      : null;
    super.attributeChangedCallback(name, oldVal, newVal);
  }

  async firstUpdated() {
    this.debug
    ? console.debug(`sui-table ${this.tableTitle} start firstUpdated`)
    : null;
    // Give the browser a chance to paint
    // TODO see how we can remove this and make it more async
    await new Promise((r) => setTimeout(r, 0));
    this.rows = await this.fetchRows();
  }


  updated(changedProps) {

    //TODO how to check that the table exists??
    if (changedProps.has("showFilters")) {
      this._table.on("tableBuilt", () => {
        });
    }

    if (changedProps.has("rows")) {
      // Create the checkbox for selecting a row
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

      // Process columns for the table
      const tabulatorColumns = this.columns.map(column => {
        // Required object properties for each column
        const columnIterable = {
          title: "",
          field: "",
          sorter: "string",
          formatter: "plaintext",
          headerFilter: this.showFilters,
        };

        // Merge the column object with the required properties
        if (Array.isArray(column.field)) {
          column.field.forEach((element) => {
            columnIterable.title = column.title ? column.title : element;
            columnIterable.field = element;
          });
        } else if (typeof column.field === "string") {
          columnIterable.title = column.title ? column.title : column.field;
          columnIterable.field = column.field;
        }

        // Add entity id to the table but don't display it to users
        if (columnIterable.field === "id") {
          columnIterable.visible = false;
        }

        return columnIterable;
      });

      // The first column will always be complex HTML with row actions
      tabulatorColumns[0].formatter = "html";
      // Adjust the width of the first column depending on the number of row actions
      if (this.links) {
        //TODO refactor to be more accurate based on length of action text
        tabulatorColumns[0].minWidth = this.links.length * 100 + 64; //number of row actions + 4em;
      }
      this.debug
        ? console.debug(`sui-table ${this.tableTitle} columns`, tabulatorColumns)
        : null;

      // Constuct the table
      this._table = new Tabulator(`#suiTable${this.id}`, {
        debugEventsExternal: false,
        debugEventsInternal: false,
        // Force the selectorColumn to be the first column
        columns: selectorColumn.concat(tabulatorColumns),
        data: this.dataKey ? this.rows[`${this.dataKey}`] : this.rows,
        height: this.height,
        pagination: true,
        paginationSize: this.pageSize ? this.pageSize : 10,
        paginationButtonCount: 3,
        paginationCounter:"rows",
        paginationSizeSelector: [10, 20, 50, 100, true],
        layout: "fitDataFill",
        // Add the entity id as the id attribute to each row
        rowFormatter: (row) => {
          row.getElement().id = `item-${row.getData().id}`;
        },
      });

      this.debug
        ? console.debug(
          `sui-table ${this.tableTitle} rows`,
          this.dataKey ? this.rows[`${this.dataKey}`] : this.rows
        )
        : null;

      // Calculate the number of pages, and current items shown out of total items
      // Outputs to "Showing currentItemMin to currentItemMax of totalItems"
      // dataLoaded represents the first/initial load of the table
      this._table.on("dataLoaded", (e, data) => {
        this.debug
          ? console.debug(`sui-table ${this.tableTitle} dataLoaded`, e, data)
          : null;
        const currentPage = this._table.getPage() ? this._table.getPage() : 1;
        this.totalItems = this.dataKey
          ? this.rows[`${this.dataKey}`].length
          : this.rows.length;
        this.currentItemMin = (currentPage - 1) * this.pageSize + 1;
        this.currentItemMax = currentPage * this.pageSize;
      });

      // pageLoaded represents a new page being loaded
      this._table.on("pageLoaded", pageNumber => {
        this.debug
          ? console.debug(
              `sui-table ${this.tableTitle} pageLoaded`,
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

      // Rows actions are displayed on row mouseover
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

      // Rows actions are hidden on row mouseout
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

      // When a row is selected, show the row actions and bulk actions
      this._table.on("rowSelected", data => {
        this.debug
          ? console.debug(`sui-table ${this.tableTitle} rowSelected`, data)
          : null;
        if (!this.showActions) {
          this.showActions = true;
        }
        this._numSelected = this._table.getSelectedData().length;
        this.requestUpdate();
      });

      // If no rows are selected, hide the row actions and bulk actions
      this._table.on("rowDeselected", data => {
        this.debug
          ? console.debug(`sui-table ${this.tableTitle} rowDeselected`, data)
          : null;
        this._numSelected = this._table.getSelectedData().length;

        if (!this.alwaysShowlinks && this._table.getSelectedRows().length === 0) {
          this.showActions = false;
        }
        this.requestUpdate();
      });
    }
    super.update(changedProps);
  }

  // TODO add manual refetch options
  async fetchRows(value) {

    //TODO determine if this check is still necessary
    if (!this.siteId) {
      console.error(`sui-table ${this.tableTitle} fetchRows no siteId`);
      return;
    }

    if (!this.dataUrl) {
      console.error(`sui-table ${this.tableTitle} fetchRows no dataURl`);
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

        // If columns were not passed in via props, use the first entry in the data
        // to define the columns
        if (this.columns.length === 0 && typeof result[0] === "object") {
          Object.keys(result[0]).forEach((key) => {
            this.columns.push({
              title: key,
              field: key,
            });
          });
        }
        this.debug
          ? console.debug(`sui-table ${this.tableTitle} fetchRows ${this.dataUrl}`, result)
          : null;

        // Process the data into a format that Tabulator can use
        result.forEach(element => {
          if (this.links) {
            // Construct the row actions HTML to be passed into the first column and displayed on row mouseover
            const actionSnippet = this.links.map(action => {
              // TODO review and dry this up
              // TODO refactor when multiple row actions are supported
              return `
                <li class="nav-item">
                  <a title="${action.title ? action.title : ""}"
                      aria-label="${action.ariaLabel ? action.ariaLabel : ""}"
                      icon="${action.icon ? action.icon : ""}"
                      class="${action.class
                              ? `link link-${action.title} pt-0 ${action.class}`
                              : `link link-${action.title}`}"
                      href="${(action.rel === "self") ? element.link.href : "#"}">
                    ${action.icon ? `<sui-icon class="sui-icon" type="${action.icon}"></sui-icon>` : ""}
                    ${action.title ? action.title : ""}
                  </a>
                </li>`;
            })
            .join("");
            
            // Wrap all the row actions in appropriate HTML
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

  toggleFilters() {
    this.debug
    ? console.debug(
      `sui-table ${this.tableTitle} toggleFilters`
      )
      : null;
      const updatedColumns = this._table.getColumnLayout();
      updatedColumns.forEach((column) => {
        column.headerFilter = this.showFilters;
      });
      this._table.setColumnLayout(updatedColumns);
  }

  render() {

    this._tableActions = [];
    // Process the actions into a format that Tabulator can use
    //TODO wrap all actions in permission check
    //TODO post TRINITY-52 i think authz will be in rest api and will need refactor
    for (const action of this.tableActions) {
      this._tableActions.push(html`
      <li class="nav-item">
        <a title="${action.buttonTitle ? action.buttonTitle : ""}"
            aria-label="${action.ariaLabel ? action.ariaLabel : ""}"
            icon="${action.icon ? action.icon : ""}"
            class="${action.buttonClass ? action.buttonClass : "nav-link"}"
            href="${action.href ? action.href : "#"}">
          ${action.icon ? html`<sui-icon class="sui-icon" type="${action.icon}"></sui-icon>` : ""}
          ${action.buttonTitle ? action.buttonTitle : ""}
        </a>
      </li>
      `);
    }

    // Every table has Bulks Actions button
    this._tableActions.push(html`
      <sui-button
        button-title="${
          this.showActions ? `${this.i18n.hide} ${this.i18n.bulk_actions}` : `${this.i18n.show} ${this.i18n.bulk_actions}`
        }"
        button-label="${this.i18n.toggle_display} ${this.i18n.bulk_actions}"
        icon="check-square"
        id="${this.id}BulkActionsToggle"
        @click="${() => this.showActions = !this.showActions}"
      ></sui-button>
    `);

    // Every table has Filter button
    this._tableActions.push(html`
      <li class="nav-item">
        <sui-button
          button-title="${
            this.showFilters ? `${this.i18n.hide} ${this.i18n.filters}` : `${this.i18n.show} ${this.i18n.filters}`
          }"
          button-label="${this.i18n.toggle_display} ${this.i18n.filters}"
          icon="filter"
          id="${this.id}FilterToggle"
          @click="${() => {
            this.showFilters = !this.showFilters
            this.toggleFilters()
          }}"
        ></sui-button>
      </li>
    `);

    // TODO wire up the rowselectedcounter
    return html`
      <ul class="sakai-table-toolBar nav nav-pills d-flex p-1">
        ${this._tableActions}
        <li id="sakai-table-pagerContainer" class="sakai-table-pagerContainer nav-item ms-auto">
          <div class="sakai-table-pagerLabel">
            ${this.i18n.showing} ${this.currentItemMin} ${this.i18n.to} ${this.currentItemMax} ${this.i18n.of} ${this.totalItems}
          </div>
        </li>
      </ul>
      <ul id="links"
          class="nav p-3 bg-dark text-white btn-group ${this.showActions ? "d-flex" : "d-none"} align-items-center"
          role="group"
          aria-label="${this.i18n.row_actions}">
        <li id="rowsSelectedCounter" class="nav-item">${this._numSelected} ${this.i18n.selected}</li>
        <sui-button
          button-title="${this.i18n.edit}"
          button-label="${this.i18n.edit}"
          icon="pencil"
          href="#"
          button-class="nav-item btn-link link-secondary"
        ></sui-button>
        <sui-button
          button-title="${this.i18n.duplicate}"
          button-label="${this.i18n.duplicate}"
          icon="clone"
          href="#"
          button-class="nav-item btn-link link-secondary"
        ></sui-button>
        <sui-button
          button-title="${this.i18n.remove}"
          button-label="${this.i18n.remove}"
          icon="trash"
          href="#"
          button-class="nav-item btn-link link-danger"
        ></sui-button>
        <div class="nav-item d-flex flex-fill justify-content-end">
          <sui-button
            button-label="${this.i18n.close} ${this.i18n.row_actions}"
            button-title="${this.i18n.close}"
            icon="close"
            @click=${() => {
              this.showActions = false;
              this.requestUpdate();
            }}
            button-class="btn-link link-secondary"
          ></sui-button>
        </div>
      </ul>
      </div>
      <div id="suiTable${this.id}"
          class="sui-table table ${this.tableClass ? this.tableClass : "table-striped table-hover"}">
      </div>
    `;
  }
}
