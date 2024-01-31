<template>
  <div>
    <label class="typo__label">Groups</label>
    <multiselect :value="value" :options="options" :multiple="true" group-values="libs" group-label="language" placeholder="Type to search" track-by="name" label="name" @input="selectUnique">
      <span slot="noResult">Oops! No elements found. Consider changing the search query.</span>
    </multiselect>
    <pre class="language-json"><code>{{ value  }}</code></pre>
  </div>
</template>

<style src="vue-multiselect/dist/vue-multiselect.min.css"></style>
<style type="scss">
.multiselect__placeholder {
  display: inline-block !important;
  margin-bottom: 0px !important;
  padding-top: 0px !important;
  color: var(--sakai-text-color-dimmed) !important;
}

.multiselect__option {
  /* Needs to apply to ...option and ...option::after */
  &, &::after {
    color: var(--sakai-text-color-1) !important;
    background: var(--sakai-background-color-1) !important;
  }

  &.multiselect__option--selected {
    &, &::after {
      color: var(--sakai-text-color-2) !important;
      background: var(--sakai-background-color-2) !important;
    }
  }

  &.multiselect__option--highlight {
    &, &::after {
      color: var(--infoBanner-color) !important;
      background: var(--infoBanner-bgcolor) !important;
    }

    &.multiselect__option--selected {
      &, &::after {
        color: var(--errorBanner-color) !important;
        background: var(--errorBanner-bgcolor) !important;
      }
    }
  }
}

.multiselect__tags {
  padding: 5px !important;
  min-height: 34px;
  background: var(--sakai-background-color-1) !important;
  border-color: var(--sakai-border-color);
}

.multiselect__input {
  background: var(--sakai-background-color-1) !important;
  color: var(--sakai-text-color-1) !important;

  &::placeholder {
    color: var(--sakai-text-color-dimmed) !important;
  }
}

.multiselect__tag {
  display: inline-block;
  min-width: 10px;
  font-size: 12px;
  font-weight: bold;
  line-height: 1;
  vertical-align: middle;
  white-space: nowrap;
  text-align: center;
  border-radius: 10px;
  margin-bottom: 0px !important;
  margin-right: 5px !important;
  color: var(--infoBanner-color) !important;
  background: var(--infoBanner-bgcolor) !important;
}

.multiselect__tag-icon {
  border-radius: 0px;

  &::after {
    color: var(--infoBanner-color) !important;
  }

  &:hover, &:focus {
    background: color-mix(in hsl, var(--infoBanner-bgcolor) 80%, var(--sakai-text-color-1)) !important;
  }

}

.multiselect__input, .multiselect__single {
  margin-top: 10px;
}

.multiselect__content-wrapper {
  border-color: var(--sakai-border-color);
}
</style>

<script>
import Multiselect from 'vue-multiselect';
import i18nMixin from "../mixins/i18n-mixin.js";

// Enable component api
import "../components-api/tag-selector-input-sync.js";

export default {
  name: "tag-selector",
  components: {
    Multiselect
  },
  mixins: [i18nMixin],
  data () {
    return {

      options: [{
          language: 'Javascript',
          libs: [{
              name: 'Vue.js',
              category: 'Front-end'
            },
            {
              name: 'Adonis',
              category: 'Backend'
            }
          ]
        },
        {
          language: 'Ruby',
          libs: [{
              name: 'Rails',
              category: 'Backend'
            },
            {
              name: 'Sinatra',
              category: 'Backend'
            }
          ]
        },
        {
          language: 'Other',
          libs: [{
              name: 'Laravel',
              category: 'Backend'
            },
            {
              name: 'Phoenix',
              category: 'Backend'
            }
          ]
        }
      ],
      value: []
    }
  },
  props: {
    collectionId: { type: String },
    itemId: { type: String },
    siteId: { type: String },
    tool: { type: String },
    selectedTemp: { type: String },
    extraOptions: { type: String }
  },
  computed: {
  },
  methods: {
    selectUnique(ev) {
      if (!ev || ev.length < this.value.length) {
        this.value = ev;
        return;
      }

      let newValue = ev.filter(x => this.value.indexOf(x) === -1)[0];
      let group = this.getGroupByLib(newValue);
      if (this.value.some(x => this.getGroupByLib(x) === group)) {
        this.value = this.value.filter(x => this.getGroupByLib(x) !== group);
        this.value.push(newValue);
      } else
        this.value = ev;
    },
    getGroupByLib(lib) {
      return this.options.filter(x => x.libs.some(y => y.name === lib.name))[0].language;
    }
  },
  async mounted () {

  }
};
</script>
