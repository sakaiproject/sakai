import { SakaiElement } from "./sakai-element.js?version=a82ac356";
import { html } from "./assets/lit-html/lit-html.js?version=a82ac356";

class SakaiLTIIframe extends SakaiElement {
  constructor() {
    super();
    const randomId = Math.floor(Math.random() * 1000000);
    this.randomId = randomId;

    window.addEventListener('message', (e) => {
      try {
        const idval = `sakai-lti-iframe-${randomId}`;
        // https://stackoverflow.com/questions/15329710/postmessage-source-iframe
        let frame_id = false;
        let allow_resize = false;
        Array.prototype.forEach.call(document.getElementsByTagName('iframe'), (element) => {
          if (element.contentWindow === event.source) {
            frame_id = element.getAttributeNode("id").nodeValue;
            allow_resize = element.getAttributeNode("data-allow-resize").nodeValue;
          }
        });
        if ( frame_id != idval ) return;
        if ( allow_resize != "yes" ) return;

        // The message is from our frame
        let message = e.data;
        if ( typeof message == "string" ) message = JSON.parse(e.data);
        if ( message.subject == 'lti.frameResize' ) {
          const height = message.height;
          document.getElementById(idval).height = height;
          console.debug(`Received lti.frameResize height=${height} frame=${idval}`);
        }
      } catch (error) {
        console.debug(error);
      }
    });
  }

  static get properties() {
    return {
      allowResize: {
        attribute: "allow-resize",
        type: String
      },
      newWindowText: {
        attribute: "new-window-text",
        type: String
      },
      launchUrl: {
        attribute: "launch-url",
        type: String
      }
    };
  }

  shouldUpdate() {
    return this.newWindowText && this.launchUrl;
  }

  render() {
    return html`
        <div class="sakai-iframe-launch-button" style="display:none;"><a href="${this.launchUrl}" class="btn btn-primary" role="button" target="_blank">${this.newWindowText}</a>
          </div>
          <div class="sakai-iframe-launch">
                <iframe src="${this.launchUrl}" id="sakai-lti-iframe-${this.randomId}" 
                  style="width: 100%; height: 100%; min-height: 80vh;"
                    width="100%" frameborder="0" marginwidth="0"
                    data-allow-resize="${this.allowResize}"
                    marginheight="0" scrolling="auto"
                     allowfullscreen="true" webkitallowfullscreen="true" mozallowfullscreen="true"
                     allow="camera; microphone">
                  <div class="sakai-iframe-launch-button"<a href="${this.launchUrl}" class="btn btn-primary" role="button" target="_blank">${this.newWindowText}</a></div>
                </iframe>
          </div>
    `;
  }

}

if (!customElements.get("sakai-lti-iframe")) {
  customElements.define("sakai-lti-iframe", SakaiLTIIframe);
}
