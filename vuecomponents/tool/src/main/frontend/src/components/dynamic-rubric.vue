<template>
  <div ref="component">
    <div>
      <BButton variant="primary" class="sakai-btn" v-if="!saving" @click="saving=true" :title="i18n.edit_criterions">{{i18n.edit_criterions}}</BButton>
      <BButton variant="primary" class="sakai-btn" @click="confirmChangesRubric" v-if="saving" :title="i18n.confirm_changes">{{i18n.confirm_changes}}</BButton>
      <BButton variant="primary" class="sakai-btn" @click="addRow" v-if="saving" :title="i18n.add_criterion">{{i18n.add_criterion}}</BButton>
      <BButton variant="primary" class="sakai-btn" @click="loadInitialData" v-if="saving" :title="i18n.cancel_changes">{{i18n.cancel_changes}}</BButton>
    </div>
    <div class="crit-row" v-for="(crit,index) in criterions" :key="crit.id" >
      <DynamicCriterion @delete-row="deleteRow" @check-updated="checkUpdated" @update-partial="updatePartial" :idpos="index" :ref="'critRef' + index" :crit="crit" :saving="saving"></DynamicCriterion>
    </div>
    <input type="hidden" :name="'updated'+gradingId" :value="updated" />
    <input type="hidden" :name="'newtotal'+gradingId" :value="calculatedPoints" />
    <input type="hidden" :name="'previous'+gradingId" :value="previousGrade" />
  </div>
</template>

<style lang="scss">
@import 'bootstrap/dist/css/bootstrap.css';
@import "../bootstrap-styles/buttons.scss";

.crit-row {
  padding-bottom: 0.5rem;
}

.sakai-btn {
  margin-right: 5px;
  margin-bottom: 8px;
}
</style>

<script>
import {
  BButton,
} from 'bootstrap-vue';
import DynamicCriterion from "../components/dynamic-criterion.vue";
import { updateAdhocRubric, updateEvaluation, getRubricElement } from "../api/dynamic-rubrics-api.js";
import i18nMixin from "../mixins/i18n-mixin.js";
import { isEqual, pick } from 'lodash';

export default {
  name: "dynamic-rubric",
  components: {
    BButton,
    DynamicCriterion,
  },
  mixins: [i18nMixin],
  data () {
    return {
      saving:false,
      partial:0,
      criterions: [{
        id: 0,
        pointsVal: 1.0,
        description: '',
        storedId: null,
        storedRatingId: null,
        selected: false,
      },],
      uniqueKey: 1,
      previousGradeMod: 0,
      rubric: null,
      storedCrit: null,
      updated: false,
      association: null,
      evaluation: { criterionOutcomes: [] },
    };
  },
  props: {
    gradingId: { type: String },
    entityId: { type: String },
    previousGrade: { type: Number },
    siteId: { type: String },
    evaluatedItemOwnerId: { type: String },
    origin: { type: String },
  },
  computed: {
    calculatedPoints() {
      let applyPartial = parseFloat(this.previousGradeMod + this.partial).toFixed(2);
      if (applyPartial < 0) {
        return 0;
      }
      return applyPartial;
    },
  },
  methods: {
    async confirmChangesRubric() {
      var confirmation = confirm(this.i18n.confirm_recalculation);
      if (!confirmation) { return; }

      let jsoned = JSON.stringify(this.rubric);
      let adhocUrl = '/api/sites/' + this.siteId + '/rubrics/adhoc';
      if (this.checkPointsUpdated()) {
        adhocUrl += '?pointsUpdated=true';
      }
      const ret = await updateAdhocRubric(adhocUrl, jsoned);
      if (ret) {
        let url = window.location.href.substring(0, window.location.href.lastIndexOf('/') + 1) + this.origin;
        var samigoIds = this.rubric.title.replace("pub.", "").split(".");
        url += '?resetCache=true&publishedId=' + samigoIds[0] + '&itemId=' + samigoIds[1];
        window.location.href = url;
      }
    },
    addRow(criterion) {
      let newCrit = {
        id: this.uniqueKey++,
      };
      if (criterion.pointsVal) {
        newCrit.pointsVal = criterion.pointsVal;
        newCrit.description = criterion.description;
        newCrit.storedId = criterion.storedId;
        newCrit.storedRatingId = criterion.storedRatingId;
        this.criterions.push(newCrit);
      } else {
        newCrit.pointsVal = 1.0;
        newCrit.description = this.i18n.criterion_default;
        this.criterions.push(newCrit);
        this.checkUpdated();
      }
    },
    checkUpdated(event) {
      this.rubric.criteria = this.processData();
      this.updated = !isEqual(
        this.rubric.criteria.map((crit) => pick(crit, ['id', 'title', 'ratings'])),
        this.storedCrit.map((crit) => pick(crit, ['id', 'title', 'ratings']))
      );
    },
    checkPointsUpdated() {
      return !isEqual(
        this.rubric.criteria.map((crit) => pick(crit, ['id', 'ratings'])),
        this.storedCrit.map((crit) => pick(crit, ['id', 'ratings']))
      );
    },
    updatePartial(event) {
      this.partial = 0;
      Object.keys(this.$refs).forEach((el) => {
        if (this.$refs[el][0]) {
          this.partial += Number(this.$refs[el][0].returnPoints());
        }
      });
    },
    deleteRow(event) {
      this.criterions.splice(event.position, 1);
    },
    processData() {
      var list = new Array();
      this.criterions.forEach((el) => {
        let auxCriterionData = {
          title: el.description,
          id: el.storedId,
        };
        let auxRatingData = {
          title: '-',
          points: Number(el.pointsVal).toFixed(2),
          id: el.storedRatingId,
        };
        var ratinglist = new Array();
        ratinglist.push(auxRatingData);
        auxCriterionData.ratings = ratinglist;
        list.push(auxCriterionData);
      });
      return list;
    },
    releaseChanges() {
      const crit = this.criterions
      .map((c) => {
        return {
          criterionId: c.storedId,
          points: c.pointsVal,
          selectedRatingId: c.selected ? c.storedRatingId : null
        };
      });

      const evaluation = {
        evaluatorId: window.top?.portal?.user?.id,
        id: this.evaluation.id,
        evaluatedItemId: this.gradingId,
        evaluatedItemOwnerId: this.evaluatedItemOwnerId,
        evaluatedItemOwnerType: "USER",
        overallComment: this.calculatedPoints,
        criterionOutcomes: crit,
        associationId: this.association.id,
        status: 2
      };
      if (this.evaluation && this.evaluation.id) {
        evaluation.metadata = this.evaluation.metadata;
      }

      let url = `/api/sites/${this.siteId}/rubric-evaluations`;
      let method = 'POST';
      if (this.evaluation?.id) {
        url += `/${this.evaluation.id}`;
        method = 'PUT';
      }
      updateEvaluation(url, JSON.stringify(evaluation), method);

    },
    async loadInitialData() {
      //restore variables
      this.updated = false;
      this.saving = false;
      this.previousGradeMod = this.previousGrade;
      //check if association exists
      const associationUrl = "/api/sites/" + this.siteId + "/rubric-associations/tools/sakai.samigo/items/" + this.entityId;
      this.association = await getRubricElement(associationUrl);
      //if it exists we get the rubric with the obtained id
      const rubricUrl = '/api/sites/' + this.siteId + '/rubrics/' + this.association.rubricId;
      this.rubric = await getRubricElement(rubricUrl);
      if (this.rubric.criteria.length > 0) {
        this.criterions = [];
      } else {
        this.criterions[0].description = this.i18n.criterion_default;
      }
      this.rubric.criteria.forEach((cr) => {
        let cNew =  {
          pointsVal: Number(cr.ratings[0].points),
          description: cr.title,
          storedId: cr.id,
          storedRatingId: cr.ratings[0].id,        
        };
        this.addRow(cNew);
      })
      this.storedCrit = this.processData();
    
      //get the evaluation
      let evalUrl = `/api/sites/${this.siteId}/rubric-evaluations/tools/sakai.samigo/items/${this.entityId}/evaluations/${this.gradingId}/owners/${this.evaluatedItemOwnerId}`;
      const evaluation = await getRubricElement(evalUrl);
      this.evaluation = evaluation || {
        criterionOutcomes: []
      };
      this.storedEvaluation = this.evaluation;
 
      this.decorateCriteria();
      this.updatePartial();

      // link functions with samigo events
      let $vm = this;
      const host = this.$refs.component.getRootNode()?.host;
      host.cancel = function() {
        $vm.cancelChanges();
      };
      host.release = function() {
        $vm.releaseChanges();
      };
    },
    cancelChanges() {
      // reload saved evaluation and grade
      this.evaluation = this.storedEvaluation;
      this.previousGradeMod = this.previousGrade;
      this.partial = 0;
      this.decorateCriteria();
      this.updatePartial();
    },
    decorateCriteria() {
      this.evaluation.criterionOutcomes.forEach((ed) => {
        this.criterions.forEach((c) => {
          if (ed.criterionId === c.storedId) {
            c.selected = ed.selectedRatingId !== null;
            if (c.selected) { this.previousGradeMod -= c.pointsVal; }
          }
        });
      });
      Object.keys(this.$refs).forEach((el) => {
        if (this.$refs[el][0]) {
          this.$refs[el][0].toggleColor(false);
        }
      })
    },
  },
  mounted () {
    this.loadInitialData();
  }
};
</script>
