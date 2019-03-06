import {SakaiElement} from "./sakai-element.js";
import {html} from "./node_modules/lit-element/lit-element.js";
import {repeat} from "./node_modules/lit-html/directives/repeat.js";

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

  attributeChangedCallback(name, oldValue, newValue) {

    if (name === "tool") {
      this.loadPermissions(newValue);
    }

    super.attributeChangedCallback(name, oldValue, newValue);
  }

  render() {

    if (this.roles) {
      return html`
        <table id="${this.tool}-permissions-table" class="listHier lines tableList">
          <thead>
            <tr class="listHier">
                <th style="text-align:center;">${this.i18n["title"]}</th>
                ${repeat(this.roles, role => html`
                <th style="text-align:center;">${role}</th>
                `)}
            </tr>
          </thead>
          <tbody>
            ${repeat(this.available , func => html`
            <tr>
              <td align="left">${this.i18n[func]}</td>
              ${repeat(this.roles, role => html`
              <td align="center"><input type="checkbox" class="sakai-permission-checkbox" .checked=${this.on[role].includes(func)} id="${role}:${func}"/></td>
              `)}
            </tr>
            `)}
          </tbody>
        </table>
        <div class="act">
          <input type="button" class="active" value="${this.i18n["save"]}" @click=${this.savePermissions}/>
          <input type="button" value="${this.i18n["cancel"]}"/>
          <span id="${this.tool}-success-message" style="display: none;">${this.i18n["save_success"]}</span>
          <span id="${this.tool}-failure-message" style="display: none;">${this.i18n["save_falure"]}</span>
        </div>
      `;
    } else {
      return html`Waiting for permissions`;
    }
  }

  loadPermissions(tool) {

    fetch(`/direct/permissions/${portal.siteId}/getPerms/${tool}.json`, {cache: "no-cache"})
      .then(res => res.json())
      .then(data => {
        console.log(data);
        this.on = data.on;
        this.available = data.available;
        this.i18n = data.i18n;
        this.roles = Object.keys(this.on);
      })
      .catch(error => console.log(`Failed to load permissions for tool ${tool}`, error));
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

    fetch(`/direct/permissions/${portal.siteId}/setPerms`, {method: "POST", body: new URLSearchParams(params), timeout: 30000})
      .then(res => {
        document.querySelector(`#${this.tool}-success-message`).style.display = "initial";
      })
      .catch(error => console.log(`Failed to save permissions for tool ${this.tool}`, error));
  }
}

customElements.define("sakai-tool-permissions", SakaiToolPermissions);
