import { SakaiElement } from "@sakai-ui/sakai-element";
import { html } from "lit";

export class SakaiLTIIframe extends SakaiElement {

  static properties = {

    allow: { attribute: "allow", type: String },
    allowResize: { attribute: "allow-resize", type: String },
    height: { attribute: "height", type: String },
    newWindowText: { attribute: "new-window-text", type: String },
    launchUrl: { attribute: "launch-url", type: String },
  };

  constructor() {

    super();

    const randomId = Math.floor(Math.random() * 1000000);
    this.randomId = randomId;
    this.newWindowText = null;

    this.loadTranslations("lti").then(t => {

      this._i18n = t;
      if ( this.newWindowText == null ) this.newWindowText = this._i18n.new_window_text;
      this.requestUpdate();
    });

    setTimeout(elem => {

      console.debug("elem", elem);
      if ( typeof elem == "undefined" ) return;
      console.debug("timeout check", elem.randomId, elem.launchUrl);
      // Only check off-server launches
      if ( elem.launchUrl.indexOf("http://") != 0 && elem.launchUrl.indexOf("https://") != 0 ) return;

      console.debug("Actually checking...");

      const myframe = document.getElementById(`sakai-lti-iframe-${elem.randomId}`);
      const mybutton = document.getElementById(`sakai-lti-button-${elem.randomId}`);
      let loaded = false;

      try {
        const iframeLoc = String(myframe.contentWindow.location);
        console.debug("iframeLoc", iframeLoc);
        const windowLoc = window.location.href;
        window.console && console.debug("iframe.location=", windowLoc);
        let ipos = -1;
        let slashcount = 0;
        for (let i = 0; i < windowLoc.length; i++) {
          if ( windowLoc[i] == "/" ) slashcount++;
          if ( slashcount == 3 ) {
            ipos = i;
            break;
          }
        }
        if ( ipos == -1 ) {
          loaded = false;
        } else {
          const prefix = windowLoc.substring(0, ipos);
          window.console && console.debug("iframe.prefix=", prefix);
          loaded = iframeLoc.indexOf(prefix) == 0;
        }
      } catch (ex) { // This is an expected/normal/hoped for occurance
        console.debug("rationalize iframe with window href, " + ex);
        loaded = true;
      }
      if ( ! loaded ) {
        mybutton.show();
      } else {
        window.console && console.debug("load success", elem.launchUrl);
      }
    }, 3000, this);

    window.addEventListener("message", e => {

      try {
        const idval = `sakai-lti-iframe-${randomId}`; // https://stackoverflow.com/questions/15329710/postmessage-source-iframe

        let frameId = false;
        let allowResize = false;
        Array.from(document.getElementsByTagName("iframe")).forEach(element => {
          if ( element.contentWindow === e.source ) {
            frameId = element.id;
            allowResize = element.dataset.allowResize;
          }
        });
        if ( frameId != idval ) return; // The message is from our frame
        if ( allowResize != "yes" ) return;

        let message = e.data;
        if ( typeof message == "string" ) message = JSON.parse(e.data);
        if ( message.subject == "lti.frameResize" ) {
          const height = message.height;
          const frame = document.getElementById(idval);
          if (frame) {
            const heightStyle = this._normalizeHeightForCss(height);
            if (heightStyle) frame.style.height = heightStyle;
          }
          console.debug(`Received lti.frameResize height=${height} frame=${idval}`);
        }
      } catch (error) {
        console.debug(error);
      }
    });
  }

  shouldUpdate() {
    return this._i18n && this.newWindowText && this.launchUrl;
  }

  /**
   * Normalizes a height value for use in CSS. If value is a bare number
   * (e.g., "500" or 500), appends "px"; otherwise uses the value as-is (e.g.,
   * "80vh", "100%", "500px"). Returns empty string when value is not set.
   */
  _normalizeHeightForCss(value) {
    if (value === undefined || value === null || value === "") return "";
    const str = String(value);
    return /^\d+$/.test(str) ? `${str}px` : str;
  }

  get _heightStyle() {
    const normalized = this._normalizeHeightForCss(this.height);
    return normalized || "1200px";
  }

  launchPopup() {
    window.open(this.launchUrl, "_blank");
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
        <iframe class="sakai-iframe-force-light"
            src="${this.launchUrl}"
            id="sakai-lti-iframe-${this.randomId}"
            style="width: 100%; height: ${this._heightStyle}; overflow: auto;"
            width="100%"
            aria-label="${this.newWindowText}"
            frameborder="0"
            marginwidth="0"
            data-allow-resize="${this.allowResize}"
            marginheight="0"
            scrolling="auto"
            allow="${this.allow || "camera; fullscreen; microphone; local-network-access *"}">
        </iframe>
      </div>
    `;
  }
}
