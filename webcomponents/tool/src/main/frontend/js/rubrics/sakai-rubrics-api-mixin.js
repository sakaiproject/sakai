export const rubricsApiMixin = Base => class extends Base {

  apiGetAssociation() {

    const url = `/api/sites/${this.siteId}/rubric-associations/tools/${this.toolId}/items/${this.entityId}`;
    return fetch(url, { credentials: "include" })
      .then(r => {

        if (r.ok) {
          return r.json();
        }

        throw new Error("Network error while getting association");
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

    const url = `/api/sites/${this.siteId}/rubric-evaluations/tools/${this.toolId}/items/${this.entityId}/evaluations/${this.evaluatedItemId}`;
    return fetch(url, { credentials: "include" })
      .then(r => {

        if (r.ok) {
          return r.json();
        }

        if (r.status !== 404) {
          throw new Error("Network error while getting evaluation");
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
