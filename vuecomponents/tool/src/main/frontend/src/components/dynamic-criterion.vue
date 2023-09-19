<template>
  <div class="crit-background">
    <div class="row align-items-center">
      <div class="col-md-2">
        <BButton @click="toggleColor" :variant="paintSelection" :class="['select-btn']" :disabled="saving || editing || !criterion.id" :title="i18n.select_criterion">{{idpos+1}}</BButton>
        <input class="points"
          v-if="saving && editing"
          v-model="criterion.pointsVal"
          @change="inputChange"
          :aria-label="i18n.criterion_points"
          type="Number" />
        <span class="points" v-if="!saving || !editing">{{Number(criterion.pointsVal).toFixed(2)}}</span>
      </div>
      <div class="col-md-9">
        <input class="descr"
          v-if="saving && editing"
          @change="inputChange"
          :aria-label="i18n.criterion_description"
          maxlength="255"
          v-model="criterion.description" />
        <span v-if="!saving || !editing">{{criterion.description}}</span>
      </div>
      <div class="col-md-1">
        <BButton v-if="saving && editing" @click="saveChanges" variant="success" :title="i18n.confirm_criterion">
          <BIcon icon="check-lg" aria-hidden="true" font-scale="0.7" />
        </BButton>
        <BButton v-if="saving && !editing" @click="editing=true" :title="i18n.edit_criterion">
          <BIcon icon="pencil-fill" aria-hidden="true" font-scale="0.7"/>
        </BButton>
        <BButton class="remove-btn" v-if="saving" @click="deleteRow" variant="danger" :title="i18n.remove_criterion">
          <BIcon icon="trash-fill" aria-hidden="true" font-scale="0.7" />
        </BButton>
      </div>
    </div>
  </div>
</template>

<style>
.crit-background {
  border: 1px solid var(--sakai-border-color);
  border-radius: 6px;
  background-color: var(--sakai-background-color-2);
  max-width: 1080px;
}
.descr {
    width: 100%;
}
.points {
    max-width: 70px;
    margin-left: 25px;
}
.select-btn {
  padding:5px 10px;
  margin: 5px;
  font-size: medium;
}
.remove-btn {
  margin-left:2px
}
</style>

<script>
import {
  BButton,
  BIcon,
  BootstrapVueIcons,
} from 'bootstrap-vue';
import Vue from 'vue';

Vue.use(BootstrapVueIcons);

import i18nMixin from "../mixins/i18n-mixin.js";
import * as rubricUtils from '../utils/dynamic-rubrics-utils.js';

export default {
  name: "dynamic-criterion",
  components: {
    BButton,
    BIcon,
  },
  mixins: [i18nMixin],
  data () {
    return {
      criterion: {},
      editing: false,
      toggleCount: 0,
    };
  },
  props: {
    idpos: { type: Number },
    crit: { type: Object },
    saving: { type: Boolean },
  },
  computed: {
    paintSelection() {
      if (this.toggleCount && this.criterion.selected) {
        if(this.criterion.pointsVal > 0) {
          return "success";
        } else if(this.criterion.pointsVal < 0) {
          return "danger";
        }
      } else {
        return "secondary";
      }
    },
  },
  methods: {
    toggleColor(isAction = true) {
      if(isAction) {
        this.criterion.selected = !this.criterion.selected;
        this.$emit("update-partial", this.criterion);
      }
      this.toggleCount++;
    },
    inputChange(e) {
      if (this.criterion.pointsVal == 0) {
        this.criterion.pointsVal = 1.0;
        alert(this.i18n.criterion_zero);
      }
      this.$emit("check-updated", this.criterion);      
    },
    returnPoints() {
      this.saveChanges();
      if (this.criterion.selected) { return this.criterion.pointsVal; }
      else { return 0; }
    },
    saveChanges() {
      this.editing = false;
      this.criterion.pointsVal = parseFloat(this.criterion.pointsVal).toFixed(2);
      this.$emit("check-updated", this.criterion);
    },
    deleteRow() {
      if(confirm(this.i18n.confirm_remove)) {
        this.$emit("delete-row",  {
          key: this.criterion.id,
          position: this.idpos,
        });
        this.$emit("check-updated", this.criterion);
      }
    },
    jsoned() {
      return JSON.stringify(this.criterion, rubricUtils.replacer);
    },
  },
  mounted () {
    this.criterion = this.crit;
    this.criterion.pointsVal = parseFloat(this.criterion.pointsVal).toFixed(2);
  },
};
</script>
