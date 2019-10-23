import {SakaiElement} from "./sakai-element.js";
import {html} from "./assets/lit-element/lit-element.js";

class SakaiEditor extends SakaiElement {
    static get properties() {
        return {
            editorId: { attribute: "editor-id", type: String },
            text: String,
            mode: String
        };
    }

    constructor() {
        super();
        console.debug("Sakai Editor constructor");
        this.editorId = "editor";
        this.text = "Add your content here";
        this.mode = "classic";
    }

    render() {
        console.debug("Sakai Editor render");

        return html`
            <div id="${this.editorId}">
                <h2>${this.text}</h2>
            </div>
        `;
    }

    firstUpdated(changedProperties) {
        console.debug("Sakai Editor firstUpdated");
        const element = this.querySelector(`#${this.editorId}`);

        if (this.mode === "inline") {
            CKEDITOR.InlineEditor.create(element)
                .then(editor => {
                    console.debug(editor);
                })
                .catch(error => {
                    console.error(error.stack);
                });
        } else if (this.mode === "balloon") {
            CKEDITOR.BalloonEditor.create(element)
                .then(editor => {
                    console.debug(editor);
                })
                .catch(error => {
                    console.error(error.stack);
                });
        } else {
            // classic editor is the default
            CKEDITOR.ClassicEditor.create(element)
                .then(editor => {
                    console.debug(editor);
                })
                .catch(error => {
                    console.error(error.stack);
                });
        }
    }
}

customElements.define("sakai-editor", SakaiEditor);
