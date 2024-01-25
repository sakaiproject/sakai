export const rubricsApiMixin = Base => class extends Base {

  apiGetAssociation() {

    const url = `/api/sites/${this.siteId}/rubric-associations/tools/${this.toolId}/items/${this.entityId}`;
    return fetch(url, { credentials: "include" })
      .then(r => {

        if (r.ok) {
          return r.json();
        }

        throw new Error(`Network error while getting association: ${r.status}`);
      });
  }

  apiGetRubric(rubricId) {

    const rubricUrl = `/api/sites/${this.siteId}/rubrics/${rubricId}`;
    return fetch(rubricUrl, { credentials: "include" })
      .then(r => {

        if (r.ok) {
          return r.json();
        }

        throw new Error("Network error while getting rubric");
      });
  }

  apiGetEvaluation() {

    let url = `/api/sites/${this.siteId}/rubric-evaluations/tools/${this.toolId}/items/${this.entityId}/evaluations/${this.evaluatedItemId}/owners/${this.evaluatedItemOwnerId}`;

    if (this.isPeerOrSelf) {
      url += "?isPeer=true";
    }

    return fetch(url, { credentials: "include" })
      .then(r => {

        if (r.ok) {
          return r.json();
        }

        if (r.status !== 404) {
          throw new Error(`Network error while getting evaluation at ${url}`);
        } else {
          console.warn(this._i18n.no_evaluation_404_warning.replace("{}", this.entityId));
        }
      });
  }

  apiGetAllEvaluations() {

    const url = `/api/sites/${this.siteId}/rubric-evaluations/tools/${this.toolId}/items/${this.entityId}`;
    return fetch(url, { credentials: "include" })
      .then(r => {

        if (r.ok) {
          return r.json();
        }

        throw new Error(`Network error while retrieving evaluations: ${r.status}`);
      });
  }
};
