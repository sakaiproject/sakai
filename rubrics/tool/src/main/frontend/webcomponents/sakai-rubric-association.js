import {RubricsElement} from "./rubrics-element.js";
import {html} from "/webcomponents/assets/lit-element/lit-element.js";
import {SakaiRubricsLanguage} from "./sakai-rubrics-language.js";

class SakaiRubricAssociation extends RubricsElement {

  constructor() {

    super();

    this.configurationOptions = [];

    SakaiRubricsLanguage.loadTranslations().then(result => this.i18nLoaded = result );
  }

  set token(newValue) {

    this.rubricsUtils.initLightbox(newValue);
    this._token = "Bearer " + newValue;
  }

  get token() { return this._token; }

  static get properties() {

    return {
      token: { type: String },
      isAssociated: { type: Boolean},
      entityId: { attribute: "entity-id", type: String },
      toolId: { attribute: "tool-id", type: String },
      stateDetails: { attribute: "state-details", type: String },
      dontAssociateLabel: { attribute: "dont-associate-label", type: String },
      associateLabel: { attribute: "associate-label", type: String },
      dontAssociateValue: { attribute: "dont-associate-value", type: Number },
      associateValue: { attribute: "associate-value", type: Number },
      fineTunePoints: { attribute: "fine-tune-points", type: String },
      hideStudentPreview: { attribute: "hide-student-preview", type: String },
      rubrics: { type: Array },
    };
  }

  attributeChangedCallback(name, oldValue, newValue) {

    super.attributeChangedCallback(name, oldValue, newValue);

    if (this.token && this.toolId) {
      this.getAssociation();
    }
  }

  shouldUpdate(changedProperties) {
    return this.i18nLoaded;
  }

  render() {

    return html`
      <h4><sr-lang key="grading_rubric">Grading Rubric</sr-lang></h4>
      <div class="sakai-rubric-association form">
      ${this.rubrics && this.rubrics.length ?
        html`
        <div class="radio">
          <label>
            <input @click="${this.associate}" id="rbcs-dontAssociate" name="rbcs-associate" type="radio" value="${this.dontAssociateValue}">${this.dontAssociateLabel}
          </label>
        </div>

        <div class="radio">
          <label>
            <input @click="${this.associate}" id="rbcs-associate" name="rbcs-associate" type="radio" value="${this.associateValue}">${this.associateLabel}
          </label>
        </div>

        <div class="rubrics-list ${this.enabledClass}">

          <div class="rubrics-selections">
            <select id="rbcs-rubricslist" @change="${this.updateStateDetails}" name="rbcs-rubricslist" class="form-control">
            ${this.rubrics.map(r => html`
              <option value="${r.id}">${r.title}</option>
            `)}
            </select>

            <!-- <a href="#">Create a Rubric</a> -->
            <button @click="${this.showRubric}" id="preview-rubric" class="btn btn-link ${this.enabledClass}">
              <sr-lang key="preview_rubric">Preview Rubric</sr-lang>
            </button>
          </div>

          <div class="rubric-options">
            <div class="checkbox">
              <label>
                <input @change="${this.updateStateDetails}" id="rbcs-config-fineTunePoints" name="rbcs-config-fineTunePoints" type="checkbox" value="1">${this.fineTunePoints}
              </label>
            </div>
            <div class="checkbox">
              <label>
                <input @change="${this.updateStateDetails}" id="rbcs-config-hideStudentPreview" name="rbcs-config-hideStudentPreview" type="checkbox" value="1">${this.hideStudentPreview}
              </label>
            </div>
          </div>
        </div>
        `
        :
        html`
        <span class="sak-banner-info indnt2" style="width: 80%">
          <sr-lang key="no_rubrics">No rubrics have been created.</sr-lang>
        </span>
      `}
      </div>
      <input name="rbcs-state-details" id="rbcs-state-details" type="hidden" value="${this.stateDetails}">
    `;
  }

  getAssociation() {

    $.ajax({
      url: `/rubrics-service/rest/rubric-associations/search/by-tool-item-ids?toolId=${this.toolId}&itemId=${this.entityId}`,
      headers: {"authorization": this.token},
      contentType: "application/json"
    })
    .done(data => {

      var associations = data._embedded['rubric-associations'];
      this.association = associations.length ? associations[0] : false;
      if (this.association) {
        this.isAssociated = 1;
        this.selectedRubric = this.association.rubricId;
      } else {
        this.isAssociated = 0;
      }
      this.getRubrics();
    })
    .fail((jqXHR, textStatus, message) => { console.log(textStatus); console.log(message); });
  }

  getRubrics(data) {

    $.ajax({
      url: "/rubrics-service/rest/rubrics",
      headers: {"authorization": this.token},
      data: data || {}
    })
    .done(data => this.handleRubrics(data))
    .fail((jqXHR, textStatus, message) => { console.log(textStatus); console.log(message); });
  }

  handleRubrics(data) {

    this.rubrics = data._embedded.rubrics;
    if (data.page.size <= this.rubrics.length) {
      this.getRubrics({"size": this.rubrics.length+25});
      return;
    }
    if (this.rubrics.length) {

      //rubrics.initLightbox(this.token);
 
      setTimeout(function() {

        if (this.stateDetails) {
          this.handleStateDetails();
        } else {
          this.handleAssociated();
          this.handleOptions();
        }

        if (this.selectedRubric) {
          this.querySelector(`option[value="${this.selectedRubric}"]`).selected = true;
          document.getElementById("rbcs-associate").checked = true;
          this.enabledClass = "enabled";
        }
        this.dispatchEvent(new CustomEvent('state-details'));
      }.bind(this));
    }
  }

  updateStateDetails() {

    var stateDetails;

    if (this.isAssociated == 1) {

      var checks = this.querySelectorAll('[type="checkbox"]');
      var isChecked = [];
      for (var i = checks.length - 1; i >= 0; i--) {

        if (checks[i].checked) {
          isChecked.push(checks[i].id);
        }
      }

      stateDetails = {
        rubric: document.getElementById("rbcs-rubricslist").value,
        configs: isChecked
      }

      this.stateDetails = escape(JSON.stringify(stateDetails));
      // this.test = JSON.parse(unescape(this.stateDetails));
    } else {
      this.stateDetails = "";
    }

  }

  showRubric(e) {

    e.preventDefault();
    if (this.isAssociated) {
      this.rubricsUtils.showRubric(document.getElementById("rbcs-rubricslist").value);
    } else {
      e.target.blur();
    }
  }

  handleOptions() {

    this.selectedConfigOptions = this.association.parameters ? this.association.parameters : {};

    for (var property in this.selectedConfigOptions) {
      document.getElementById(`rbcs-config-${property}`).checked = this.selectedConfigOptions[property];
    }
  }

  handleAssociated() {

    var optionLabels = this.querySelectorAll(".rubric-options label");
    if (this.isAssociated == 1) {

      this.enabledClass = "enabled"
      setTimeout(function () {

        document.getElementById("rbcs-associate").checked = true;
        document.getElementById("rbcs-rubricslist").removeAttribute('disabled');
        document.getElementById("preview-rubric").removeAttribute('disabled')
        optionLabels.forEach(l => l.classList.remove('disabled'));
        this.querySelectorAll(".rubric-options input[type='checkbox']").forEach(o => o.removeAttribute("disabled"));
      }.bind(this));
    } else {
      this.enabledClass = "disabled"
      setTimeout(function () {

        document.getElementById("rbcs-dontAssociate").checked = true;
        document.getElementById("rbcs-rubricslist").setAttribute('disabled', true)
        document.getElementById("preview-rubric").setAttribute('disabled', true)
        optionLabels.forEach(l => l.classList.add('disabled'));
        this.querySelectorAll('.rubric-options input[type="checkbox"]').forEach(o => o.setAttribute("disabled", ""));
      }.bind(this));
    }
    this.dispatchEvent(new CustomEvent('state-details'));
  }

  associate(e) {

    this.isAssociated = e.target.value;
    this.handleAssociated();
  }

  handleStateDetails() {

    var stateDetails = JSON.parse(unescape(this.stateDetails));
    this.isAssociated = true;
    this.selectedRubric = stateDetails.rubric;
    setTimeout(function() {

      for (var i = stateDetails.configs.length - 1; i >= 0; i--) {
        document.getElementById(stateDetails.configs[i]).checked = true;
      }
      this.handleAssociated();
    }.bind(this));
  }
}

customElements.define("sakai-rubric-association", SakaiRubricAssociation);
