import {SakaiElement} from "./sakai-element.js";
import {html} from "./assets/lit-element/lit-element.js";

class SakaiEditor extends SakaiElement {
    static get properties() {
        return {
            editorId: { type: String },
            text: { type: String },
            mode: { type: String}
        };
    }

    constructor() {
        super();
        console.log("Sakai Editor constructor");
        this.editorId = 'editor';
        this.text = 'Add your content here';
        this.mode = 'classic';
    }

    render() {
        console.log("Sakai Editor render");

        return html`
            <div id="${this.editorId}">
                <h2>${this.text}</h2>
            </div>
        `;
    }

    firstUpdated(changedProperties) {
        console.log("Sakai Editor firstUpdated");
        const element = this.querySelector(`#${this.editorId}`);

        if (this.mode === 'inline') {
            CKEDITOR.InlineEditor.create(element)
                .then(editor => {
                    console.log(editor);
                })
                .catch(error => {
                    console.error(error.stack);
                });
        } else if (this.mode === 'balloon') {
            CKEDITOR.BalloonEditor.create(element)
                .then(editor => {
                    console.log(editor);
                })
                .catch(error => {
                    console.error(error.stack);
                });
        } else {
            // classic editor is the default
            CKEDITOR.ClassicEditor.create(element)
                .then(editor => {
                    console.log(editor);
                })
                .catch(error => {
                    console.error(error.stack);
                });
        }
    }
}

customElements.define("sakai-editor", SakaiEditor);
