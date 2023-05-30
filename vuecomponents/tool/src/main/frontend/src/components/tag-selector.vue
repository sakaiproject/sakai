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
<style>
.multiselect__placeholder {
  display: inline-block !important;
  margin-bottom: 0px !important;
  padding-top: 0px !important;
}

.multiselect.invalid .multiselect__tags {
  border: 1px solid #f86c6b !important;
}

.multiselect__option--highlight {
  background: #red !important;
}

.multiselect__option--highlight:after {
  background: #red !important;
}

.multiselect__tags {
  padding: 5px !important;
  min-height: 10px;
}

.multiselect__tag {
  display: inline-block;
  min-width: 10px;
  font-size: 12px;
  font-weight: bold;
  color: #fff;
  line-height: 1;
  vertical-align: middle;
  white-space: nowrap;
  text-align: center;
  background-color: #555;
  border-radius: 10px;
  background: #e9f5fc !important;
  color: #196390 !important;
  margin-bottom: 0px !important;
  margin-right: 5px !important;
}

.multiselect__tag-icon:after {
  color: rgba(60, 60, 60, 0.5) !important;
}

.multiselect__tag-icon:focus,
.multiselect__tag-icon:hover {
  background: #e9f5fc !important;
}

.multiselect__tag-icon:focus:after,
.multiselect__tag-icon:hover:after {
  color: red !important;
}

.multiselect__input, .multiselect__single {
  margin-top: 10px;
}
</style>

<script>
import Multiselect from 'vue-multiselect';
import i18nMixin from "../mixins/i18n-mixin.js";

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
  async mounted () {
    this.taggable = this.addNew;

    // get available tags
    await fetch('/api/tags/' + this.collectionId)
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
      fetch('/api/tags/items/' + this.itemId)
        .then((r) => {
          if (r.ok) {
            return r.json();
          }
          throw new Error(`Failed to get selected tags for id ` + this.itemId);
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
