import { SakaiTable } from "./src/SakaiTable.js";

if (!customElements.get("sakai-table")) {
  customElements.define("sakai-table", SakaiTable);
}
