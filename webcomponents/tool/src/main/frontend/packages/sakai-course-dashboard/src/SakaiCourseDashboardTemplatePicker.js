import { css, html } from "lit";
import { loadProperties } from "@sakai-ui/sakai-i18n";
import { SakaiDialogContent } from "@sakai-ui/sakai-dialog-content";
import "@sakai-ui/sakai-button/sakai-button.js";

export class SakaiCourseDashboardTemplatePicker extends SakaiDialogContent {

  static properties = {

    data: { type: Object },
    template: { type: Object },
    _i18n: { state: true },
  };

  constructor() {

    super();
    loadProperties("dashboard").then(r => this._i18n = r);
  }

  select() {

    this.close();
    this.dispatchEvent(new CustomEvent("template-selected", { detail: { template: this.template }, bubbles: true }));
  }

  title() {
    return html`${this._i18n.template_picker_title}`;
  }

  shouldUpdate(changed) {
    return this._i18n && this.template && super.shouldUpdate(changed);
  }

  templateSelected(e) {
    this.template = parseInt(e.target.dataset.template);
  }

  content() {

    return html`

      <div id="instruction">
        ${this._i18n.template_picker_instruction}
      </div>

      <div id="template-block">
        <div id="template1-block" class=${this.template === 1 ? "selected" : ""}>
          <a href="javascript:;" @click=${this.templateSelected} data-template="1">
            <img data-template="1" src="${this.data.layout1ThumbnailUrl}" class="thumbnail" />
          </a>
          <h2>${this._i18n.option1}</h2>
        </div>
        <div id="template2-block" class=${this.template === 2 ? "selected" : ""}>
          <a href="javascript:;" @click=${this.templateSelected} data-template="2">
            <img data-template="2" src="${this.data.layout2ThumbnailUrl}" class="thumbnail" />
          </a>
          <h2>${this._i18n.option2}</h2>
        </div>
        <div id="template3-block" class=${this.template === 3 ? "selected" : ""}>
          <a href="javascript:;" @click=${this.templateSelected} data-template="3">
            <img data-template="3" src="${this.data.layout3ThumbnailUrl}" class="thumbnail" />
          </a>
          <h2>${this._i18n.option3}</h2>
        </div>
      </div>
    `;
  }

  buttons() {

    return html`
      <sakai-button @click=${this.select} primary>${this._i18n.select}</sakai-button>
    `;
  }

  static styles = [
    SakaiDialogContent.styles,
    css`
    #instruction {
      width: 745px;
      margin-bottom: 20px;
    }

    #template-block {
      display: flex;
    }
    #template-block div {
      flex: 1;
      text-align: center;
    }

    .thumbnail {
      width: 199px;
      height: 132px;
    }

    .selected {
      border: 3px black solid;
    }
    `,
  ];
}
