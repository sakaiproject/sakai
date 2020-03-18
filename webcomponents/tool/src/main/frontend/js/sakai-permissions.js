import {SakaiElement} from "./sakai-element.js";
import {html} from "./assets/lit-element/lit-element.js";
import "./sakai-group-picker.js";

/**
 * Handles display and manipulation of permissions for a Sakai tool.
 *
 * Usage, from the Roster tool:
 *
 * <sakai-permissions tool="roster" />
 *
 * This component needs to be able to lookup a tool's translations, and this happens via the
 * sakai-i18n.js module, loading the translations from a Sakai web service. The translations need
 * to be jarred and put in TOMCAT/lib, and the permission translation keys need to start with "perm-",
 * eg: perm-TOOLPERMISSION.
 *
 * Example:
 *
 * perm-roster.viewallmembers = View all participants
 * perm-roster.viewhidden = View hidden participants
 * perm-roster.export = Export roster
 * perm-roster.viewgroup = View groups
 *
 * @author Adrian Fish <adrian.r.fish@gmail.com>
 */
class SakaiPermissions extends SakaiElement {

  constructor() {

    super();

    this.available;
    this.on;
    this.roles;
    this.roleNameMappings;
    this.groups = [];
    this.groupReference = `/site/${portal.siteId}`;

    this.loadTranslations("permissions-wc").then(i18n => {

      this.loadTranslations(this.tool).then(tool => {

        Object.keys(tool).filter(k => k.startsWith("perm-")).forEach(k => i18n[k.substring(5)] = tool[k]);
        this.i18n = i18n;
        this.requestUpdate();
      });
    });
  }

  static get properties() {

    return {
      tool: String,
      roles: {type: Array},
      groups: Array,
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

  shouldUpdate(changed) {
    return this.i18n;
  }

  render() {

    if (this.roles) {
      return html`

        ${this.groups ? html`
          <div>
            <label for="permissons-group-picker">${this.i18n["per.lis.selectgrp"]}</label>
            <sakai-group-picker id="permissions-group-picker" groups="${JSON.stringify(this.groups)}" @group-selected=${this.groupSelected} />
          </div>
        ` : ""}
        <div class="permissions-undo-button"" style="float:left;padding-top:.5em">
          <input type="button" value="${this.i18n["per.lis.restoredef"]}" aria-label="${this.i18n["undo"]}" @click=${this.resetPermissions} />
        </div>
        <table id="${this.tool}-permissions-table" class="permissions-table listHier checkGrid specialLink" cellspacing="0" summary="${this.i18n["per.lis"]}" border="0" style="width:auto">
          <tr>
            <th id="permission">
              <a href="#" title="${this.i18n["per.lis.head.title"]}">${this.i18n["per.lis.head"]}</a>
            </th>
            ${this.roles.map(role => html`
            <th class="role" data-role="${role}"><a href="#" title="${this.i18n["per.lis.role.title"]}" data-role="${role}">${this.roleNameMappings[role]}</a></th>
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
          <input type="button" class="active" value="${this.i18n["gen.sav"]}" aria-label="${this.i18n["gen.sav"]}" @click=${this.savePermissions}/>
          <input type="button" value="${this.i18n["gen.can"]}" aria-label="${this.i18n["gen.can"]}" @click=${this.resetPermissions}/>
          <span id="${this.tool}-failure-message" class="permissions-save-message" style="display: none;">${this.i18n["per.error.save"]}</span>
        </div>
      `;
    } else {
      return html`Waiting for permissions`;
    }
  }

  loadPermissions() {

    fetch(`/direct/permissions/${portal.siteId}/getPerms/${this.tool}.json?ref=${this.groupReference}`, {cache: "no-cache", credentials: "same-origin"})
      .then(res => res.json() )
      .then(data => {

        this.on = data.on;
        this.available = data.available;
        this.groups = data.groups;
        this.roles = Object.keys(this.on);
        this.roleNameMappings = data.roleNameMappings;
      })
      .catch(error => console.error(`Failed to load permissions for tool ${this.tool}`, error));
  }

  savePermissions() {

    document.body.style.cursor = "wait";

    const boxes = document.querySelectorAll(`#${this.tool}-permissions-table input[type="checkbox"]`);
    const myData = {};
    const params = `ref=${this.groupReference}&` + Array.from(boxes).reduce((acc,b) => {

      if (b.checked) {
        return acc + `${b.id}=true&`;
      } else {
        return acc + `${b.id}=false&`;
      }
    }, "");

    fetch(`/direct/permissions/${portal.siteId}/setPerms`, {method: "POST", credentials: "same-origin", body: new URLSearchParams(params), timeout: 30000})
      .then(res => {

        if (res.ok) {
          window.location.reload();
        } else {
          throw new Error("Network response was not ok.");
        }
      })
      .catch(error => {

        document.querySelector(`#${this.tool}-failure-message`).style.display = "inline-block";
        console.error(`Failed to save permissions for tool ${this.tool}`, error)
      })
      .finally(() => document.body.style.cursor = "default");
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

      if (e.target.dataset.role) {
        const role = e.target.dataset.role.replace("\.", "\\.");
        $('.' + role + "-checkbox-cell").add(e.target).toggleClass('rowHover', e.type === "mouseenter");
      }
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

      const role = e.target.dataset.role.replace("\.", "\\.");

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

  groupSelected(e) {

    this.groupReference = e.detail.value;
    this.loadPermissions();
  }
}

customElements.define("sakai-permissions", SakaiPermissions);
