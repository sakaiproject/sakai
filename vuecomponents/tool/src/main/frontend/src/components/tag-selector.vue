<template>
  <div>
    <multiselect
      id="tag-selector-vuecomp"
      name="tag-selector-vuecomp"
      v-model="value"

      :selected-label="i18n.selected"
      :select-label="i18n.select"
      :deselect-label="i18n.deselect"
      :tagPlaceholder="i18n.add_new"
      :placeholder="searchText"

      label="name"
      track-by="code"
      :options="options"
      :multiple="true"
      :taggable="taggable"
      @tag="addTag">
        <span slot="noOptions">{{ i18n.no_options }}</span>
        <span slot="noResult">{{ i18n.no_results }}</span>
    </multiselect>
    <input type="hidden" name="tag[]" v-for="tag in value" :value="tag.code" />
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
      taggable: false,
      value: [],
      options: [],
    };
  },
  props: {
    collectionId: { type: String },
    itemId: { type: String },
    siteId: { type: String },
    tool: { type: String },
    selectedTemp: { type: String },
    extraOptions: { type: String },
    addNew: { type: Boolean }
  },
  computed: {
    searchText() {
      let text = this.i18n.search_filter;
      if(this.taggable) {
        text = this.i18n.search_or_add;
      }
      return text;
    },
  },
  methods: {
    addTag (newTag, isSelected) {
      newTag = newTag.replaceAll(',','');
      const tag = {
        name: newTag,
        code: newTag
      };
      this.options.push(tag);
      if (isSelected){
        this.value.push(tag);
      }
    }
  },
  watch: {
    value(value){
      this.$emit('change', JSON.parse(JSON.stringify(value)));
    },
  },
  async mounted () {
    this.taggable = this.addNew;

    // get available tags
    await fetch('/api/sites/' + this.siteId + '/tools/' + this.tool + '/tags/' + this.collectionId)
      .then((r) => {
        if (r.ok) {
          return r.json();
        }
        throw new Error(`Failed to get tags for id ` + this.collectionId);
      })
      .then((data) => {
        this.options = data.map((tag) => {
          return {
            name: tag.tagLabel,
            code: tag.tagId,
          };
        });
      })
      .catch ((error) => console.error(error));

    if (this.extraOptions) {
      this.extraOptions.split(",").forEach((element) => this.addTag(element, false));
    }
      
    // get selected tags
    if(!this.selectedTemp && this.itemId && this.taggable) {
      fetch('/api/sites/' + this.siteId + '/tools/' + this.tool + '/tags/' + this.collectionId + '/items/' + this.itemId)
        .then((r) => {
          if (r.ok) {
            return r.json();
          }
          throw new Error(`Failed to get selected tags for id ` + this.itemId + ` and collection ` + this.collectionId);
        })
        .then((data) => {
          this.value = data.map((tag) => {
            return {
              name: tag.tagLabel,
              code: tag.tagId,
            };
          });
        })
        .catch ((error) => console.error(error));
    } else if (this.selectedTemp){
      this.selectedTemp.split(',').forEach((element) => {
        const foundTag = this.options.find((option) => option.code === element );
        if (foundTag) {
          this.value.push(foundTag);
        } else if (element.trim() !== '') {
          this.addTag(element, true);
        }
      });
    }
  }
};
</script>
