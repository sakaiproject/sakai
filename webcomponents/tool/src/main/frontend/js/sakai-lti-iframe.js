import { SakaiElement } from "./sakai-element.js";
import { html } from "./assets/lit-html/lit-html.js";
class SakaiLTIIframe extends SakaiElement {
  constructor() {
    super();
    const randomId = Math.floor(Math.random() * 1000000);
    this.randomId = randomId;
    this.newWindowText = null;

    // Defaults
    this.allow = "camera; microphone";
    this.allowResize = "yes";
    this.height = "1200px";

    this.loadTranslations("lti").then(t => {
      this.i18n = t;
      if (this.newWindowText == null) this.newWindowText = this.i18n.new_window_text;
    });
    setTimeout(elem => {
      console.debug('elem', elem);
      if (typeof elem == 'undefined') return;
      console.debug("timeout check", elem.randomId, elem.launchUrl);
      // Only check off-server launches
      if (elem.launchUrl.indexOf('http://') != 0 && elem.launchUrl.indexOf('https://') != 0) return;
      console.debug('Actually checking...');
      const myframe = document.getElementById(`sakai-lti-iframe-${elem.randomId}`);
      const mybutton = document.getElementById(`sakai-lti-button-${elem.randomId}`);
      let loaded = false;
      try {
        const iframeLoc = String(myframe.contentWindow.location);
        console.debug('iframeLoc', iframeLoc);
        const windowLoc = window.location.href;
        window.console && console.debug("iframe.location=", windowLoc);
        let ipos = -1;
        let slashcount = 0;
        for (let i = 0; i < windowLoc.length; i++) {
          if (windowLoc[i] == '/') slashcount++;
          if (slashcount == 3) {
            ipos = i;
            break;
          }
        }
        if (ipos == -1) {
          loaded = false;
        } else {
          const prefix = windowLoc.substring(0, ipos);
          window.console && console.debug("iframe.prefix=", prefix);
          loaded = iframeLoc.indexOf(prefix) == 0;
        }
      } catch (ex) {
        // This is an expected/normal/hoped for occurance
        loaded = true;
      }
      if (!loaded) {
        mybutton.show();
      } else {
        window.console && console.debug('load success', elem.launchUrl);
      }
    }, 3000, this);
    window.addEventListener('message', e => {
      try {
        const idval = `sakai-lti-iframe-${randomId}`; // https://stackoverflow.com/questions/15329710/postmessage-source-iframe

        let frame_id = false;
        let allow_resize = false;
        Array.from(document.getElementsByTagName('iframe')).forEach(element => {
          if (element.contentWindow === event.source) {
            frame_id = element.getAttributeNode("id").nodeValue;
            allow_resize = element.getAttributeNode("data-allow-resize").nodeValue;
          }
        });
        if (frame_id != idval) return; // The message is from our frame
        if (allow_resize != "yes") return;
        let message = e.data;
        if (typeof message == "string") message = JSON.parse(e.data);
        if (message.subject == 'lti.frameResize') {
          let height = message.height;
          const isnum = /^\d+$/.test(height);
          if ( isnum ) height = height + 'px';
          this.height = height;
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
      },
      allow: {
        attribute: "allow",
        type: String
      },
      height: {
        attribute: "height",
        type: String
      }
    };
  }
  shouldUpdate() {
    return this.newWindowText && this.launchUrl;
  }
  launchPopup() {
    window.open(this.launchUrl, '_blank');
    return false;
  }
  render() {
    return html`
          <div class="sakai-iframe-launch-button" id="sakai-lti-button-${this.randomId}" style="display:none;">
              <p>
              <button @click="${this.launchPopup}" class="btn btn-primary">${this.newWindowText}</button>
              </p>
          </div>
          <div class="sakai-iframe-launch">
                <iframe src="${this.launchUrl}" id="sakai-lti-iframe-${this.randomId}"
                  style="width: 100%; height: ${this.height}; min-height: 80vh;"
                    frameborder="0" marginwidth="0"
                    data-allow-resize="${this.allowResize}"
                    allow="${this.allow}"
                    marginheight="0" scrolling="auto"
                     allowfullscreen="true" webkitallowfullscreen="true" mozallowfullscreen="true"
                  >
                </iframe>
          </div>
    `;
  }
}
if (!customElements.get("sakai-lti-iframe")) {
  customElements.define("sakai-lti-iframe", SakaiLTIIframe);
}
