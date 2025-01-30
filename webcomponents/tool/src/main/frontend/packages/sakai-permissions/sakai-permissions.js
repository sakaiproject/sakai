import { SakaiPermissions } from "./src/SakaiPermissions.js";
export { SakaiPermissions };

!customElements.get("sakai-permissions") && customElements.define("sakai-permissions", SakaiPermissions);
