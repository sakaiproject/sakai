import {SakaiElement} from "./sakai-element.js";
import {html} from "./assets/lit-element/lit-element.js";

class SakaiEditor extends SakaiElement {
    static get properties() {
        return {
            textAreaId: { type: String },
            text: { type: String }
        };
    }

    constructor() {
        super();
        console.log("Sakai Editor constructor");
        this.textAreaId = 'editor';
        this.text = 'Add your content here';
    }

    render() {
        console.log("Sakai Editor render");

        return html`
            <textarea id="${this.textAreaId}">${this.text}</textarea>
        `;
    }

    firstUpdated(changedProperties) {
        console.log("Sakai Editor firstUpdated");
        const textArea = this.querySelector(`#${this.textAreaId}`);
        textArea.focus();

        CKEDITOR.ClassicEditor.create(textArea)
            .then(editor => {
                console.log(editor);
            })
            .catch(error => {
                console.error(error.stack);
            });
    }
}

customElements.define("sakai-editor", SakaiEditor);
