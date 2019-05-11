import {SakaiElement} from "./sakai-element.js";
import {html} from "./assets/lit-element/lit-element.js";
import {repeat} from "./assets/lit-html/directives/repeat.js";

class SakaiToolPermissions extends SakaiElement {

  constructor() {

    super();

    this.available;
    this.i18n;
    this.on;
    this.roles;
  }

  static get properties() {

    return {
      tool: {type: String},
      roles: {type: Array},
    };
  }

  set tool(newValue) {

    this._tool = newValue;
    this.loadPermissions();
  }

  get tool() { return this._tool; }

  updated(changedProperties) {
    this.attachHandlers();
  }

  render() {

    if (this.roles) {
      return html`

        <div class="permissions-undo-button"" style="float:left;padding-top:.5em">
          <input type="button" value="${this.i18n["undo"]}" aria-label="${this.i18n["undo"]}" @click=${this.resetPermissions} />
        </div>
        <table id="${this.tool}-permissions-table" class="permissions-table listHier checkGrid specialLink" cellspacing="0" summary="${this.i18n["per.lis"]}" border="0" style="width:auto">
          <tr>
            <th id="permission">
              <a href="#" title="${this.i18n["per.lis.head.title"]}">${this.i18n["per.lis.head"]}</a>
            </th>
            ${this.roles.map(role => html`
            <th class="role" data-role="${role}"><a href="#" title="${this.i18n["per.lis.role.title"]}" data-role="${role}">${role}</a></th>
            `)}
          </tr>
          ${this.available.map(perm => html`
          <tr>
            <td class="permissionDescription unclicked" scope="row">
              <a href="#" title="${this.i18n["per.lis.perm.title"]}">
                ${this.i18n[perm]}
              </a>
            </td>
            ${this.roles.map(role => html`
            <td class="${role}-checkbox-cell checkboxCell">
              <label for="${role}:${perm}" class="sr-only">
                <span>${this.i18n["gen.enable"]} ${role}</span>
              </label>
              <input type="checkbox" class="sakai-permission-checkbox" aria-label="${this.i18n["gen.enable"]} ${role}" .checked=${this.on[role].includes(perm)} id="${role}:${perm}"/>
            </td>
            `)}
          </tr>
          `)}
        </table>
        <div class="act">
          <input type="button" class="active" value="${this.i18n["save"]}" aria-label="${this.i18n["save"]}" @click=${this.savePermissions}/>
          <input type="button" value="${this.i18n["cancel"]}" aria-label="${this.i18n["save"]}" @click=${this.resetPermissions}/>
          <span id="${this.tool}-success-message" class="permissions-save-message" style="display: none;">${this.i18n["save_success"]}</span>
          <span id="${this.tool}-failure-message" class="permissions-save-message" style="display: none;">${this.i18n["save_falure"]}</span>
        </div>
      `;
    } else {
      return html`Waiting for permissions`;
    }
  }

  loadPermissions() {

    fetch(`/direct/permissions/${portal.siteId}/getPerms/${this.tool}.json`, {cache: "no-cache", credentials: "same-origin"})
      .then(res => res.json() )
      .then(data => {

        this.on = data.on;
        this.available = data.available;
        this.i18n = data.i18n;
        this.roles = Object.keys(this.on);
      })
      .catch(error => console.log(`Failed to load permissions for tool ${this.tool}`, error));
  }

  savePermissions() {

    const boxes = document.querySelectorAll(`#${this.tool}-permissions-table input[type="checkbox"]`);
    const myData = {};
    const params = Array.from(boxes).reduce((acc,b) => {

      if (b.checked) {
        return acc + `${b.id}=true&`;
      } else {
        return acc + `${b.id}=false&`;
      }
    }, "");

    fetch(`/direct/permissions/${portal.siteId}/setPerms`, {method: "POST", credentials: "same-origin", body: new URLSearchParams(params), timeout: 30000})
      .then(res => {
        document.querySelector(`#${this.tool}-success-message`).style.display = "initial";
      })
      .catch(error => console.log(`Failed to save permissions for tool ${this.tool}`, error));
  }

  resetPermissions() {
    window.location.reload();
  }

  attachHandlers() {

    $('.permissions-table input:checkbox').change(function (e) {
      $(e.target).parents('td').toggleClass('active', e.target.checked);
    }).change();
    $(".permissions-table tr:even").addClass("evenrow");
    // Save the default selected
    $('.permissions-table :checked').parents('td').addClass('defaultSelected');

    $('.permissions-table .permissionDescription').hover(function (e) {
      $(e.target).parents('tr').children('td').toggleClass('rowHover', e.type === "mouseenter");
    });

    $('.permissions-table th').hover(function (e) {

      const role = e.target.dataset.role;
      $('.' + role + "-checkbox-cell").add(e.target).toggleClass('rowHover', e.type === "mouseenter");
    });

    $('.permissions-table th#permission').hover(function (event) {
      $('.permissions-table td.checkboxCell').toggleClass('rowHover', event.type === "mouseenter");
    });

    $('.permissions-table th#permission a').click(function (e) {

      $('.permissions-table input').prop('checked', ($('.permissions-table :checked').length === 0)).change();
      e.preventDefault();
    });
    $('.permissions-table .permissionDescription a').click(function (e) {

      var anyChecked = $(e.target).parents('tr').find('input:checked').not('[disabled]').length > 0;
      $(e.target).parents('tr').find('input:checkbox').not('[disabled]').prop('checked', !anyChecked).change();
      e.preventDefault();
    });
    $('.permissions-table th.role a').click(function (e) {

      const role = e.target.dataset.role;

      var col = ($(e.target).parent('th').prevAll().size());
      var anyChecked = $('.permissions-table .' + role + '-checkbox-cell input:checked').not('[disabled]').length > 0;
      $('.permissions-table .' + role + '-checkbox-cell input').not('[disabled]').prop('checked', !anyChecked).change();
      e.preventDefault();
    });

    $('#clearall').click(function (e) {

      $(".permissions-table input").not('[disabled]').prop("checked", false).change();
      e.preventDefault();
    });
  }
}

customElements.define("sakai-tool-permissions", SakaiToolPermissions);
