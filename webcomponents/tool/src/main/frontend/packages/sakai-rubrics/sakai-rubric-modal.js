import "./sakai-rubric-student.js";
import { SakaiRubricModal } from "./src/SakaiRubricModal.js";

!customElements.get("sakai-rubric-modal") && customElements.define("sakai-rubric-modal", SakaiRubricModal);
